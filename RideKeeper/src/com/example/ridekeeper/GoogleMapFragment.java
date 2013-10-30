package com.example.ridekeeper;

import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;


public class GoogleMapFragment extends DialogFragment implements OnMarkerClickListener, LocationListener {
	private GoogleMap mMap;
	private MapView mMapView;
	private Bundle mBundle;
	
	private String UIDtoTrack = null;	//VBS UID to be tracked, null for all VBS
	private boolean canRunVBSQuery = false;
	
    public Fragment newInstance(Context context) {
    	GoogleMapFragment f = new GoogleMapFragment();
    	
    	return f;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.map_fragment, container, false);
        
        try{
        	MapsInitializer.initialize(getActivity());
        }catch (GooglePlayServicesNotAvailableException e){
        	
        }
        
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        
    	if (mMap==null){
    		mMap = ((MapView) view.findViewById(R.id.map)).getMap();
    	}
		mMap.setMyLocationEnabled(true);
  		//mMap.setOnMarkerClickListener(this);
  		mMap.getUiSettings().setCompassEnabled(true);
  		mMap.getUiSettings().setZoomControlsEnabled(true);
  		
  		
  		//Move camera to current phone's location
  		HelperFuncs.getLastGoodLoc();
  		
  		if (HelperFuncs.myLocation!=null){
  			LatLng myLatLng = new LatLng( HelperFuncs.myLocation.getLatitude(), HelperFuncs.myLocation.getLongitude() );
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng , 15f));
  		}
  		
  		//Start dynamically showing markers on the map
        canRunVBSQuery = true;
        HelperFuncs.removeAllMarker();
        mHandler.postDelayed(runQueryVBS, 1000);
  		
  		//Add a test marker on the map
  		/*
  		MarkerOptions markerOption = new MarkerOptions();
  		LatLng ll = new LatLng(34.068765, -118.446314);
  		markerOption.position(ll).title("UCLA");
  		mMarker = mMap.addMarker(markerOption);
  		*/
  		
  		//for demo: moving the marker in the map
  		//updateMarker();
        
        return view;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	mBundle = savedInstanceState;
    	
    	//Load  UID argument for tracking
    	if (getArguments()!=null && getArguments().containsKey("UID")){
        	UIDtoTrack = getArguments().getString("UID");
        	//Toast.makeText(getActivity(), UIDtoTrack, Toast.LENGTH_SHORT).show();
    	}else{
    		UIDtoTrack = null;
    	}
    	
    	//setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    	setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Light);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mMapView.onResume();
    	canRunVBSQuery = true;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	mMapView.onPause();
    	canRunVBSQuery = false;
    }
    
    @Override
    public void onDestroy() {
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the map
    	HelperFuncs.removeAllMarker();		//Only need to remove onCreate
    	mMapView.onDestroy();
    	super.onDestroy();
    }

	@Override
	public boolean onMarkerClick(final Marker marker){
		//Intent intent = new Intent(this , Report.class);
		//startActivity(intent);
		
		//Toast.makeText(getActivity(), "Clicked!", Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

		//Update phone's GPS location
		HelperFuncs.myLocation = location;
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	
	//Callback when query gets result from Parse
	private FindCallback<ParseObject> queryVBSCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			
			if (e== null){ // no error
				HelperFuncs.myVBSList = objects;
				if (HelperFuncs.myVBSList.size()>0){ 
					
					//make sure we have enough markers
					while ( HelperFuncs.myMarkerList.size() < HelperFuncs.myVBSList.size() ){
				  		MarkerOptions markerOption = new MarkerOptions();
				  		markerOption.position(new LatLng(0, 0)).visible(false);
				  		HelperFuncs.myMarkerList.add(mMap.addMarker(markerOption));
					}
					while ( HelperFuncs.myMarkerList.size() > HelperFuncs.myVBSList.size() ){
						HelperFuncs.myMarkerList.get(0).remove();
						HelperFuncs.myMarkerList.remove(0);
					}
					
					for (int i=0; i < HelperFuncs.myVBSList.size(); i++){
						ParseGeoPoint p =  HelperFuncs.myVBSList.get(i).getParseGeoPoint("pos");
						HelperFuncs.myMarkerList.get(i).setPosition( new LatLng(p.getLatitude(), p.getLongitude()) );
						HelperFuncs.myMarkerList.get(i).setTitle(
										HelperFuncs.myVBSList.get(i).getString("make") + " " +
										HelperFuncs.myVBSList.get(i).getString("model") + " " +
										HelperFuncs.myVBSList.get(i).getNumber("year").toString() + " "
										);
						HelperFuncs.myMarkerList.get(i).setVisible(true);
					}
				}else{ //No VBS nearby
					for (int i=0; i < HelperFuncs.myMarkerList.size(); i++){
						HelperFuncs.myMarkerList.get(i).remove();
						HelperFuncs.myMarkerList.remove(i);
					}
				}
				
				if (canRunVBSQuery)
					mHandler.postDelayed(runQueryVBS, DBGlobals.vbsPosMapUpdateRate); //Refresh rate = 3 seconds if no error

			}else{ //error occurred when query to Parse
				Toast.makeText(getActivity(), "Error querying Parse server", Toast.LENGTH_SHORT).show();
				HelperFuncs.myVBSList.removeAll(objects);

				for (int i=0; i < HelperFuncs.myMarkerList.size(); i++){
					HelperFuncs.myMarkerList.get(i).remove();
					HelperFuncs.myMarkerList.remove(i);
				}
				
				if (canRunVBSQuery)
					mHandler.postDelayed(runQueryVBS, 15000);  //Refresh rate = 15 seconds if error occurs
			}
		}
	};
	

	//Dynamically update VBS on map
	final Handler mHandler = new Handler();
    final Runnable runQueryVBS = new Runnable() {
    	@Override
		public void run() {
			// TODO Turn Internet connection on if needed
    		
			//Toast.makeText(context, "Querying VBS", Toast.LENGTH_SHORT).show();
			//Query Parse server for nearby VBS
			if (HelperFuncs.myLocation == null){
				HelperFuncs.getLastGoodLoc();
				
				mHandler.postDelayed(runQueryVBS, 3000);
			}else{
				if (UIDtoTrack==null){ //Track everything nearby VBS if not UID is given
					HelperFuncs.queryForVBS_NonBlocked(	HelperFuncs.myLocation.getLatitude(),
							HelperFuncs.myLocation.getLongitude(),
							DBGlobals.searchRadius, //search within this miles radius
							queryVBSCallback);
	
				}else{ //Track the VBS with the given UID only
					HelperFuncs.queryForVBSwithUID_NonBlocked(UIDtoTrack, queryVBSCallback);
				}
			}
		}
	};
	

	//For demo: update the marker
	/*
		public void updateMarker(){
			final Handler handler = new Handler();

	        final Runnable r = new Runnable() {
	        	int i = 0;
	    		double lat = 34.068765;
	    		double lng = -118.446314;
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (i == 8){
						i = 0;
					}
					
					mMarker.setPosition(new LatLng( lat, lng ));
			        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 15f));
			        
			        lat += 0.0001;
			        lng += 0.00007;
					handler.postDelayed(this, 1000);
				}
			};
			
			handler.post(r);
		}
	*/
}









