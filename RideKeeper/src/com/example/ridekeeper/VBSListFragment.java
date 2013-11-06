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


public class VBSListFragment extends ListFragment{
	private static VehicleArrayAdapter vbsArrayAdapter;
	private static boolean canRunVBSQuery = false;
	private static Context myContext;
	
	private static FindCallback<ParseObject> queryVBSCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (!canRunVBSQuery)
				return;
			
			if (e== null){ // no error
				HelperFuncs.myVBSList = objects;
				if (HelperFuncs.myVBSList.size()>0){ 
					//make sure the list has the exact size as the VBS list
					while( vbsArrayAdapter.getCount() < HelperFuncs.myVBSList.size()){
						vbsArrayAdapter.add(new Vehicle());
					}
					while( vbsArrayAdapter.getCount() > HelperFuncs.myVBSList.size()){
						vbsArrayAdapter.remove( vbsArrayAdapter.getItem(0) );
					}
					
					//Update each item in the list
					for (int i=0; i < HelperFuncs.myVBSList.size(); i++){
						ParseObject parseObj = HelperFuncs.myVBSList.get(i);
						//ParseGeoPoint p =  parseObj.getParseGeoPoint("pos");
						Vehicle v = vbsArrayAdapter.getItem(i);
						v.setUID( parseObj.getObjectId() );
						v.setMake( parseObj.getString("make") );
						v.setModel( parseObj.getString("model") );
						v.setYear( parseObj.getNumber("year").intValue() );
					}
				}else{ //No VBS nearby
					vbsArrayAdapter.clear();
				}
				
				//mHandler.postDelayed(runQueryVBS, 20000); //Refresh rate = 20 seconds if no error
				
			}else{ //error occurred when query to Parse
				Toast.makeText(myContext, "Error querying Parse server", Toast.LENGTH_SHORT).show();
				//HelperFuncs.myVBSList.removeAll(objects);
				
				//mHandler.postDelayed(runQueryVBS, 30000);  //Refresh rate = 30 seconds if error occurs
			}
		}
	};
	
	//Dynamically update VBS on map
	public static final Handler mHandler = new Handler();
    public static final Runnable runQueryVBS = new Runnable() {
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

		myContext = getActivity();
		
	    vbsArrayAdapter = new VehicleArrayAdapter(getActivity(), new ArrayList<Vehicle>());		
		setListAdapter(vbsArrayAdapter);
		
		registerForContextMenu(getListView());
		
		//Refresh the list
		mHandler.post(runQueryVBS);
	}

	@Override
	public void onResume() {
		super.onResume();
		canRunVBSQuery = true;
		//mHandler.postDelayed(runQueryVBS, 2000);
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
	    String uid = vbsArrayAdapter.getItem( info.position ).getUID();
	    
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
