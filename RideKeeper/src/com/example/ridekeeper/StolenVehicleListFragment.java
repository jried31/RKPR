package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.content.Context;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class StolenVehicleListFragment extends ListFragment{
	private static final String TAG = StolenVehicleListFragment.class.getSimpleName();

	private static ParseVehicleArrayAdapter stolenVehicleArrayAdapter;
	private static Context myContext;
	
	private static FindCallback<ParseObject> queryVehicleInMyChatRoomCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (e== null){ // no error
				stolenVehicleArrayAdapter.clear();

				Toast.makeText(myContext, "Found " + objects.size() + " stolen vehicles nearby", Toast.LENGTH_SHORT).show();

				for (int i = 0; i < objects.size(); i++){
					stolenVehicleArrayAdapter.add( (ParseVehicle) objects.get(i));
				}
			}else{ //error occurred when query to Parse
				Toast.makeText(myContext, "Error: " + e.getMessage() , Toast.LENGTH_SHORT).show();
			}
		}
	};

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		myContext = getActivity();
		
	    stolenVehicleArrayAdapter = new ParseVehicleArrayAdapter(getActivity(), new ArrayList<ParseVehicle>(),DBGlobals.LIST_STOLEN_VEHICLES);		
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
        bundle.putString(ChatFragment.ARG_VEHICLE_ID, vehicleId);

	    switch (item.getItemId()) {
	    case R.id.menuItem_owner_info:
	    	// Displaying the owner's profile for the stolen vehicle
	    	DialogFragmentMgr.showDialogFragment(getActivity(), new OwnerInfoFragment(), "Owner Information", true, bundle);
	    	return true;

	    case R.id.menuItem_show_on_map:
	    	// Putting the UID of the select vehicle to the Google Map fragment argument
	    	DialogFragmentMgr.showDialogFragment(getActivity(), new GoogleMapFragment(), "Map Dialog", true, bundle);
	    	return true;
	    	
	    case R.id.menuItem_chat_room:
	    	String title = 	"Room: " +
	    					stolenVehicleArrayAdapter.getItem( info.position ).getMake() + " " + 
	    					stolenVehicleArrayAdapter.getItem( info.position ).getModel() + " " +
	    					stolenVehicleArrayAdapter.getItem( info.position ).getYear().toString();
	    	bundle.putString(ChatFragment.ARG_TITLE, title);
	    	bundle.putString(ChatFragment.ARG_ROOM_NAME, "5111_room01"); //FIX THIS
	    	DialogFragmentMgr.showDialogFragment(getActivity(), new ChatFragment(), "Chat Dialog", true, bundle);
	        return true;
	    }
	    return false;
	}
	
	//Should be called only when ParseUser.getCurrentUser() is authenticated
	public static void refreshList(){
		LocationMgr locationMgr = MainActivity.mLocationMgr;
		
		Log.d(TAG,  "refreshList()");

		Location location = locationMgr.location;
		if (location == null){
			Log.d(TAG, "refreshList() calling locationMgr.getLastGoodLocation()");
			location = locationMgr.getLastGoodLocation();
		}
	
		if (location != null){
			Log.d(TAG, "refreshList() refreshing stolen vehicles list");
			ParseFunctions.queryForVehicleInMyChatRoom_InBackground(location.getLatitude(),location.getLongitude(),
					DBGlobals.searchRadius,
					queryVehicleInMyChatRoomCallback);
		} else {
			Log.d(TAG, "refreshList() location is null");
		}
	}
	
	public static void clearList(){
		stolenVehicleArrayAdapter.clear();
	}
}
