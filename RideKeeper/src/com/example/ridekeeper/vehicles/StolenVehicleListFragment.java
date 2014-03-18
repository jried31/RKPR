package com.example.ridekeeper.vehicles;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.DialogFragmentMgr;
import com.example.ridekeeper.GoogleMapStolenVehicleFragment;
import com.example.ridekeeper.OwnerInfoFragment;
import com.example.ridekeeper.ParseFunctions;
import com.example.ridekeeper.R;
import com.example.ridekeeper.qb.MyQBUser;
import com.example.ridekeeper.qb.chat.ChatFragment;
import com.example.ridekeeper.qb.chat.RoomChat;
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
	private static FragmentActivity sMainActivity;

	private static ProgressBar mProgressBar;

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

                mProgressBar.setVisibility(View.GONE);

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		sMainActivity = getActivity();

		View view = inflater.inflate(R.layout.vehicles_list_fragment, container, false);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar); 
		mProgressBar.setVisibility(View.GONE);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		sMainActivity = getActivity();
		
	    stolenVehicleArrayAdapter = new ParseVehicleArrayAdapter(sMainActivity, new ArrayList<ParseVehicle>(),DBGlobals.TAB_IDX_STOLEN_VEHICLES);		

		setListAdapter(stolenVehicleArrayAdapter);
		
		registerForContextMenu(getListView());

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
		//MainActivity mainActivity = (MainActivity) getActivity();
		//mainActivity.setSelectedFrag(DBGlobals.SelectedFrag.STOLEN_VEHICLE);
		//mainActivity.invalidateOptionsMenu();
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
        ParseVehicle vehicle = stolenVehicleArrayAdapter.getItem(info.position);
        
	    String vehicleId = vehicle.getObjectId(),
	    	   trackerId = vehicle.getTrackerId(),
	    	   vehicleMake = vehicle.getMake(),
	    	   vehicleModel = vehicle.getModel(),
	    	   vehicleYear = vehicle.getYear().toString();
	    		
	    Toast.makeText(getActivity(), "vehicleId: " + vehicleId + " Tracker Id: "+trackerId, Toast.LENGTH_SHORT).show();

	    final Bundle bundle = new Bundle();
        bundle.putString(ParseVehicle.ID, vehicleId);
        bundle.putString(ParseVehicle.MAKE, vehicleMake);
        bundle.putString(ParseVehicle.MODEL, vehicleModel);
        bundle.putString(ParseVehicle.YEAR, vehicleYear);
        bundle.putString(ParseVehicle.TRACKER_ID, trackerId);
        
	    switch (item.getItemId()) {
	    case R.id.menuItem_owner_info:
	    	// Displaying the owner's profile for the stolen vehicle
	    	DialogFragmentMgr.showDialogFragment(getActivity(),  new OwnerInfoFragment(),getString(R.string.owner_information_title),true, bundle);
	    	return true;

	    case R.id.menuItem_show_on_map:
	    	DialogFragmentMgr.showDialogFragment(getActivity(), new GoogleMapStolenVehicleFragment(), getString(R.string.vehicle_map_title), true, bundle);
	    	return true;
	    	
	    case R.id.menuItem_chat_room:
	    	String roomTitle = 	"Room: " + vehicleMake + " " +  vehicleModel + " " + vehicleYear;

	    	final String chatRoomName = vehicle.getChatRoomName();
	    	// Create bundle of metadata to pass to ChatFragment
	        bundle.putSerializable(ChatFragment.EXTRA_MODE, ChatFragment.Mode.GROUP);
	        bundle.putString(ChatFragment.ARG_TITLE, roomTitle);
	        bundle.putString(ChatFragment.ARG_ROOM_NAME, chatRoomName); 
	        bundle.putSerializable(ChatFragment.ARG_ROOM_ACTION, RoomChat.RoomAction.JOIN);

            Log.d(TAG, "Chat room name: " + chatRoomName);

            if (sRoomsReceiver.isRoomsRetrieved()) {
            	startChatFragment(bundle, chatRoomName);
            } else {
            	sRoomsReceiver.showProgressDialog();
            	final Handler chatHandler = new Handler();

            	Runnable chatRunnable = new Runnable() {
            		@Override
            		public void run() {
            			if (sRoomsReceiver.isRoomsRetrieved()) {
                            startChatFragment(bundle, chatRoomName);
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

        DialogFragment chatFragment = new ChatFragment();
        //chatFragment.setArguments(bundle);
        String dialogName = bundle.getString(ChatFragment.ARG_TITLE);

        DialogFragmentMgr.showDialogFragment(
                sMainActivity,
                chatFragment, 
                dialogName, 
                true,
                bundle);

        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.addToBackStack(null);
        //ft.replace(R.id.content_frame, chatFragment).commit();
	}

	//Should be called only when ParseUser.getCurrentUser() is authenticated
	public static void refreshList() {
        Log.d(TAG, "refreshList() refreshing stolen vehicles list");
        clearList();
		mProgressBar.setVisibility(View.VISIBLE);

        ParseFunctions.queryForVehicleInMyChatRoom_InBackground(
                sRoomsReceiver,
                queryVehicleInMyChatRoomCallback);
	}
	
	public static void clearList(){
		stolenVehicleArrayAdapter.clear();
	}
}
