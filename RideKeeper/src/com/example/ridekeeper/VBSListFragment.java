package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


public class VBSListFragment extends ListFragment{
	private VehicleArrayAdapter mVehicleArrayAdapter;
	
	private FindCallback<ParseObject> queryVBSCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			
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
					
					for (int i=0; i < HelperFuncs.myVBSList.size(); i++){
						ParseGeoPoint p =  HelperFuncs.myVBSList.get(i).getParseGeoPoint("pos");
						mVehicleArrayAdapter.getItem(i).setMake( Double.toString(p.getLatitude()) );
						mVehicleArrayAdapter.getItem(i).setModel( Double.toString(p.getLongitude()) );
					}
				}else{ //No VBS nearby
					mVehicleArrayAdapter.clear();
				}

				mHandler.postDelayed(runQueryVBS, 20000); //Refresh rate = 20 seconds if no error
				
			}else{ //error occurred when query to Parse
				Toast.makeText(getActivity(), "Error querying Parse server", Toast.LENGTH_SHORT).show();
				//HelperFuncs.myVBSList.removeAll(objects);
				
				mHandler.postDelayed(runQueryVBS, 20000);  //Refresh rate = 20 seconds if error occurs
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
			if (HelperFuncs.myLocation == null){
				HelperFuncs.getLastGoodLoc();
				
				mHandler.postDelayed(runQueryVBS, 3000);
			}else{
				HelperFuncs.queryForVBS_NonBlocked(	HelperFuncs.myLocation.getLatitude(),
												HelperFuncs.myLocation.getLongitude(),
												10, //search within 10 miles radius
												queryVBSCallback);
			}
		}
	};


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

	    mVehicleArrayAdapter = new VehicleArrayAdapter(getActivity(), new ArrayList<Vehicle>());		
		setListAdapter(mVehicleArrayAdapter);
		
		//Dynamically update the list
		mHandler.postDelayed(runQueryVBS, 1000);
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the list
		super.onDestroy();
	}
	
}
