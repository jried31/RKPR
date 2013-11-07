package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class VBSListFragment extends ListFragment{
	private static ParseVehicleArrayAdapter vbsArrayAdapter;
	private static Context myContext;
	
	private static FindCallback<ParseObject> queryVBSCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (e== null){ // no error
				vbsArrayAdapter.clear();

				for (int i = 0; i < objects.size(); i++){
					vbsArrayAdapter.add( (ParseVehicle) objects.get(i));
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
		
	    vbsArrayAdapter = new ParseVehicleArrayAdapter(getActivity(), new ArrayList<ParseVehicle>());		
		setListAdapter(vbsArrayAdapter);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshList();
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
	    String uid = vbsArrayAdapter.getItem( info.position ).getObjectId();
	    
	    switch (item.getItemId()) {
	    case R.id.menuItem_show_on_map:
	    	//Show the google map as DialogFragment
	    	FragmentTransaction ft = getFragmentManager().beginTransaction();
	    	Fragment prev = getFragmentManager().findFragmentByTag("Map Dialog");
	    	if (prev != null) {
	    		ft.remove(prev);
	    	}
	    	ft.addToBackStack(null);
	    	
	    	//Putting the UID of the select vehicle to the Google Map fragment argument
	    	DialogFragment googleMapFragment = new GoogleMapFragment();
	    	Bundle args = new Bundle();
	    	args.putString("UID", uid);
	    	googleMapFragment.setArguments(args);
	    	googleMapFragment.show(ft, "Map Dialog");
	    	
	    	return true;
	    	
	    case R.id.menuItem_chat_room:
	    	Toast.makeText(getActivity(), "Not yet implemented", Toast.LENGTH_SHORT).show();
	        return true;
	    }
	    return false;
	}
	
	//Should be called only when ParseUser.getCurrentUser() is authenticated
	public static void refreshList(){
		if (HelperFuncs.myLocation == null){
			HelperFuncs.getLastGoodLoc();
		}
	
		if (HelperFuncs.myLocation != null){
			HelperFuncs.queryForVBS_NonBlocked(	HelperFuncs.myLocation.getLatitude(),
					HelperFuncs.myLocation.getLongitude(),
					DBGlobals.searchRadius,
					queryVBSCallback);
		}
	}
	
}
