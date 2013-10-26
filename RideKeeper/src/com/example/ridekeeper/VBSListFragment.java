package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;


public class VBSListFragment extends ListFragment{
	private VehicleArrayAdapter mVehicleArrayAdapter;
	private boolean canRunVBSQuery = false;
	
	private FindCallback<ParseObject> queryVBSCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (!canRunVBSQuery)
				return;
			
			if (e== null){ // no error
				HelperFuncs.myVBSList = objects;
				if (HelperFuncs.myVBSList.size()>0){ 
					//make sure the list has the exact size as the VBS list
					while( mVehicleArrayAdapter.getCount() < HelperFuncs.myVBSList.size()){
						mVehicleArrayAdapter.add(new Vehicle());
					}
					while( mVehicleArrayAdapter.getCount() > HelperFuncs.myVBSList.size()){
						mVehicleArrayAdapter.remove( mVehicleArrayAdapter.getItem(0) );
					}					
					
					//Update each item in the list
					for (int i=0; i < HelperFuncs.myVBSList.size(); i++){
						ParseObject parseObj = HelperFuncs.myVBSList.get(i);
						//ParseGeoPoint p =  parseObj.getParseGeoPoint("pos");
						Vehicle v = mVehicleArrayAdapter.getItem(i);
						v.setUID( parseObj.getObjectId() );
						v.setMake( parseObj.getString("make") );
						v.setModel( parseObj.getString("model") );
						v.setYear( parseObj.getNumber("year").intValue() );
					}
				}else{ //No VBS nearby
					mVehicleArrayAdapter.clear();
				}
				
				mHandler.postDelayed(runQueryVBS, 20000); //Refresh rate = 20 seconds if no error
				
			}else{ //error occurred when query to Parse
				Toast.makeText(getActivity(), "Error querying Parse server", Toast.LENGTH_SHORT).show();
				//HelperFuncs.myVBSList.removeAll(objects);
				
				mHandler.postDelayed(runQueryVBS, 30000);  //Refresh rate = 30 seconds if error occurs
			}
		}
	};
	
	//Dynamically update VBS on map
	private final Handler mHandler = new Handler();
    private final Runnable runQueryVBS = new Runnable() {
    	@Override
		public void run() {
			// TODO Turn Internet connection on if needed
    		
			//Toast.makeText(context, "Querying VBS", Toast.LENGTH_SHORT).show();
			//Query Parse server for nearby VBS
    		if (!canRunVBSQuery){
    			return;
    		}else if (HelperFuncs.myLocation == null){
				HelperFuncs.getLastGoodLoc();
				
				mHandler.postDelayed(runQueryVBS, 3000);
			}else{
				HelperFuncs.queryForVBS_NonBlocked(	HelperFuncs.myLocation.getLatitude(),
													HelperFuncs.myLocation.getLongitude(),
													DBGlobals.searchRadius,
													queryVBSCallback);
			}
		}
	};

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	    mVehicleArrayAdapter = new VehicleArrayAdapter(getActivity(), new ArrayList<Vehicle>());		
		setListAdapter(mVehicleArrayAdapter);
		
		registerForContextMenu(getListView());
		
		//Dynamically update the list
	}	

	@Override
	public void onResume() {
		super.onResume();
		canRunVBSQuery = true;
		mHandler.postDelayed(runQueryVBS, 2000);
	}
	
	@Override
	public void onPause() {
		canRunVBSQuery = false;
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the list
		super.onDestroy();
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
	    String uid = mVehicleArrayAdapter.getItem( info.position ).getUID();
	    
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
	
}
