package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.example.ridekeeper.MainActivity.SelectedFrag;
import com.example.ridekeeper.qb.MyQBUser;
import com.example.ridekeeper.qb.chat.ChatFragment;
import com.example.ridekeeper.qb.chat.ChatRoom;
import com.example.ridekeeper.qb.chat.RoomsReceiver;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.quickblox.module.chat.QBChatRoom;


public class StolenVehicleListFragment extends ListFragment {
	private static final String TAG = StolenVehicleListFragment.class.getSimpleName();

	private static RoomsReceiver sRoomsReceiver;

	private static ParseVehicleArrayAdapter stolenVehicleArrayAdapter;
	private static Activity sMainActivity;
	
	private ProgressDialog mProgressDialog;

	private static FindCallback<ParseObject> queryVehicleInMyChatRoomCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (e== null) { // no error
				stolenVehicleArrayAdapter.clear();

				Toast.makeText(
						sMainActivity, 
						"Found " + objects.size() + " stolen vehicles nearby", 
						Toast.LENGTH_SHORT
						).show();

				for (ParseObject obj : objects) {
					ParseVehicle vehicle = (ParseVehicle) obj; 
					String chatRoomName = sRoomsReceiver.getChatRoomName(vehicle.getObjectId());
					vehicle.setChatRoomName(chatRoomName);

					stolenVehicleArrayAdapter.add(vehicle);
				}
			} else { //error occurred when query to Parse
				Toast.makeText(sMainActivity, "Error: " + e.getMessage() , Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		sMainActivity = getActivity();
		
	    stolenVehicleArrayAdapter = new ParseVehicleArrayAdapter(
	    		getActivity(), 
	    		new ArrayList<ParseVehicle>(),
	    		DBGlobals.LIST_STOLEN_VEHICLES);		
		setListAdapter(stolenVehicleArrayAdapter);
		
		registerForContextMenu(getListView());
		
		mProgressDialog = new ProgressDialog(sMainActivity);

		sRoomsReceiver = new RoomsReceiver(sMainActivity);

		/* FOR DEBUG
		Bundle bundle = new Bundle();
    	bundle.putString(ChatFragment.ARG_VEHICLE_ID, "123123");
    	bundle.putString("roomname", "5111_room01"); //FIX THIS
    	DialogFragmentMgr.showDialogFragment(getActivity(), new ChatFragment(), "Chat Dialog", true, bundle);
    	*/
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
			refreshList();
		}
		
		// Set the action bar title upon resume
		// Use case: returning from chat room
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.setDrawerTitle(MainActivity.SelectedFrag.STOLENVEHICLE);
		mainActivity.setSelectedFrag(MainActivity.SelectedFrag.STOLENVEHICLE);
		mainActivity.invalidateOptionsMenu();
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Creates the Edit menu for the specific option
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.menu_vbs_list, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    String vehicleId = stolenVehicleArrayAdapter.getItem( info.position ).getObjectId();

	    Toast.makeText(getActivity(), "vehicleId: " + vehicleId, Toast.LENGTH_SHORT).show();

	    Bundle bundle = new Bundle();

	    switch (item.getItemId()) {
	    case R.id.menuItem_owner_info:
	    	// Displaying the owner's profile for the stolen vehicle
            bundle.putString(DBGlobals.ARG_VEHICLE_ID, vehicleId);

	    	DialogFragmentMgr.showDialogFragment(
	    			getActivity(), 
	    			new OwnerInfoFragment(), 
	    			"Owner Information", 
	    			true, 
	    			bundle);

	    	return true;

	    case R.id.menuItem_show_on_map:
	    	// Putting the UID of the select vehicle to the Google Map fragment argument
            bundle.putString(DBGlobals.ARG_VEHICLE_ID, vehicleId);

	    	DialogFragmentMgr.showDialogFragment(
	    			getActivity(), 
	    			new GoogleMapFragment(), 
	    			"Map Dialog", 
	    			true, 
	    			bundle);

	    	return true;
	    	
	    case R.id.menuItem_chat_room:

            ParseVehicle vehicle = stolenVehicleArrayAdapter.getItem(info.position);

	    	String roomTitle = 	"Room: " +
	    					vehicle.getMake() + " " + 
	    					vehicle.getModel() + " " +
	    					vehicle.getYear().toString();

	    	final String chatRoomName = vehicle.getChatRoomName();
	    	// Create bundle of metadata to pass to ChatFragment
            final Bundle chatBundle = createChatBundle(
            		vehicle.getObjectId(), 
            		roomTitle, 
            		chatRoomName,
            		false);

            Log.d(TAG, "Chat room name: " + chatRoomName);

            if (sRoomsReceiver.isRoomsRetrieved()) {
            	startChatFragment(chatBundle, chatRoomName);
            } else {
            	sRoomsReceiver.showProgressDialog();
            	final Handler chatHandler = new Handler();

            	Runnable chatRunnable = new Runnable() {
            		@Override
            		public void run() {
            			if (sRoomsReceiver.isRoomsRetrieved()) {
                            startChatFragment(chatBundle, chatRoomName);
            			} else if (sRoomsReceiver.progressDialogCanceled()){
            				// do nothing
            			} else {
            				// Continue trying to load QB chatrooms
            				sRoomsReceiver.loadRooms(null);
            				chatHandler.postDelayed(this, DBGlobals.LOAD_CHATROOM_DELAY);
            			}
            		}
            	};
            	
            	chatHandler.postDelayed(chatRunnable, DBGlobals.LOAD_CHATROOM_DELAY);
            }
	        return true;
	    }
	    return false;
	}
	
	private void startChatFragment(Bundle bundle, String chatRoomName) {
        QBChatRoom chatRoom = sRoomsReceiver.getChatRoom(chatRoomName);

        MyQBUser.setCurrentRoom(chatRoom);

        Fragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.content_frame, chatFragment).commit();
	}

    private Bundle createChatBundle(
    		String vehicleId, 
    		String title,
    		String roomName, 
    		boolean createChat) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatFragment.EXTRA_MODE, ChatFragment.Mode.GROUP);

        bundle.putString(DBGlobals.ARG_VEHICLE_ID, vehicleId);
        bundle.putString(ChatFragment.ARG_TITLE, title);

        bundle.putString(ChatFragment.ARG_ROOM_NAME, roomName);

        if (createChat) {
            bundle.putSerializable(ChatFragment.ARG_ROOM_ACTION, ChatRoom.RoomAction.CREATE);
        } else {
            bundle.putSerializable(ChatFragment.ARG_ROOM_ACTION, ChatRoom.RoomAction.JOIN);
        }
        return bundle;
    }
	
	//Should be called only when ParseUser.getCurrentUser() is authenticated
	public static void refreshList(){
		LocationMgr locationMgr = MainActivity.mLocationMgr;
		
        Location location = locationMgr.getLastGoodLocation();
	
		if (location != null){
			Log.d(TAG, "refreshList() refreshing stolen vehicles list");

			Toast.makeText(sMainActivity,
					"My location lat/lng: " + location.getLatitude() + "/" + location.getLongitude(),
					Toast.LENGTH_SHORT).show();

			ParseFunctions.queryForVehicleInMyChatRoom_InBackground(
					location.getLatitude(),
					location.getLongitude(),
					DBGlobals.searchRadius,
					sRoomsReceiver,
					queryVehicleInMyChatRoomCallback);
		} else {
			Log.d(TAG, "refreshList() location is null");
		}
	}
	
	public static void clearList(){
		stolenVehicleArrayAdapter.clear();
	}
}
