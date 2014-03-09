package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.example.ridekeeper.account.MyQBUser;
import com.example.ridekeeper.chat.ChatFragment;
import com.example.ridekeeper.chat.ChatRoom;
import com.example.ridekeeper.chat.RoomsReceiver;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.quickblox.module.chat.QBChatRoom;


public class StolenVehicleListFragment extends ListFragment {
	private static final String TAG = StolenVehicleListFragment.class.getSimpleName();

	private static RoomsReceiver sRoomsReceiver = new RoomsReceiver();

	private static ParseVehicleArrayAdapter stolenVehicleArrayAdapter;
	private static Activity sMainActivity;
	
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
            bundle.putString(ChatFragment.ARG_VEHICLE_ID, vehicleId);

	    	DialogFragmentMgr.showDialogFragment(
	    			getActivity(), 
	    			new OwnerInfoFragment(), 
	    			"Owner Information", 
	    			true, 
	    			bundle);

	    	return true;

	    case R.id.menuItem_show_on_map:
	    	// Putting the UID of the select vehicle to the Google Map fragment argument
            bundle.putString(ChatFragment.ARG_VEHICLE_ID, vehicleId);

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

	    	String chatRoomName = vehicle.getChatRoomName();
            bundle = createChatBundle(
            		vehicle.getObjectId(), 
            		roomTitle, 
            		chatRoomName,
            		false);

            Log.d(TAG, "Chat room name: " + chatRoomName);
            QBChatRoom chatRoom = sRoomsReceiver.getChatRoom(chatRoomName);

            MyQBUser.setCurrentRoom(chatRoom);


	    	DialogFragmentMgr.showDialogFragment(
	    			sMainActivity, 
	    			new ChatFragment(), 
	    			"Chat Dialog", 
	    			true, 
	    			bundle);

	        return true;
	    }
	    return false;
	}

    private Bundle createChatBundle(
    		String vehicleId, 
    		String title,
    		String roomName, 
    		boolean createChat) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatFragment.EXTRA_MODE, ChatFragment.Mode.GROUP);

        bundle.putString(ChatFragment.ARG_VEHICLE_ID, vehicleId);
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
