package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridekeeper.route.GMapV2GetRouteDirection;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class GoogleMapFragment extends DialogFragment implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener {
	private GoogleMap mGoogleMap;
	private MapView mMapView;
	private Bundle mBundle;
	private TextView info;
	private String UIDtoTrack = null;	//VBS UID to be tracked, null for all VBS
	private Marker markerVehicle;
	private Location myLocation;
    Document document;
    MarkerOptions markerOptions;
    LocationClient mLocationClient;
	private Polyline newPolyline;
    LocationRequest mLocationRequest;
    
    public Fragment newInstance(Context context) {
    	GoogleMapFragment f = new GoogleMapFragment();
    	return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	mBundle = savedInstanceState;
    	
    	//Load  UID argument for tracking
    	if (getArguments()!=null && getArguments().containsKey("UID")){
        	UIDtoTrack = getArguments().getString("UID");

            mLocationClient = new LocationClient(getActivity(), this, this);
            mLocationClient.connect();
    	}else{
    		Toast.makeText(getActivity(), "No vehicle UID provided to track.", Toast.LENGTH_SHORT).show();
    		UIDtoTrack = "";
    	}
    	
    	setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_googlemap, container, false);
        
        try{
        	MapsInitializer.initialize(getActivity());
        }catch (GooglePlayServicesNotAvailableException e){
        	
        }
        Button navigateBtn = (Button)view.findViewById(R.id.navigate);
        navigateBtn.setVisibility(Button.INVISIBLE);
        info = (TextView)view.findViewById(R.id.info);        
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        
    	if (mGoogleMap==null){
    		mGoogleMap = ((MapView) view.findViewById(R.id.map)).getMap();
    	}
    	progressDialog = new ProgressDialog(getActivity());
    	
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
		mGoogleMap.getUiSettings().setCompassEnabled(true);
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
		mGoogleMap.setTrafficEnabled(true);
        
        return view;
    }
	
	private int screenWidth;
	private int screenHeight;
	private void getSreenDimensions()
	{
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		  {
			  Point outSize = new Point();
			  display.getSize(outSize);
			  screenWidth = outSize.x;
			  screenHeight = outSize.y; 
		  }
		  else
		  {
			  screenWidth = display.getWidth(); 
			  screenHeight = display.getHeight(); 
		  }
	}
	

    /*
     * Creates a bounding box between an object
     */
	private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
	{
		if (firstLocation != null && secondLocation != null)
		{
			LatLngBounds.Builder builder = new LatLngBounds.Builder();    
			builder.include(firstLocation).include(secondLocation);
			
			return builder.build();
		}
		return null;
	}
    
	/*
	 * http://www.movable-type.co.uk/scripts/latlong.html	
 	*/
	/// <summary>
	/// Calculates the end-point from a given source at a given range (meters) and bearing (degrees).
	/// This methods uses simple geometry equations to calculate the end-point.
	/// </summary>
	/// <param name="source">Point of origin</param>
	/// <param name="range">Range in meters</param>
	/// <param name="bearing">Bearing in degrees</param>
	/// <returns>End-point from the source given the desired range and bearing.</returns>
	public static LatLng calculateDerivedPosition(LatLng source, double range, double bearing)
	{
	    double latA = source.latitude * DBGlobals.DEGREES_TO_RADIANS;
	    double lonA = source.longitude * DBGlobals.DEGREES_TO_RADIANS;
	    double angularDistance = range / DBGlobals.RADIOUS_OF_EARTH;
	    double trueCourse = bearing * DBGlobals.DEGREES_TO_RADIANS;

	    double newLat = Math.sin(
	        Math.sin(latA) * Math.cos(angularDistance) + 
	        Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));

	    double dlon = Math.atan2(
	        Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA), 
	        Math.cos(angularDistance) - Math.sin(latA) * Math.sin(newLat));

	    double newLon = ((lonA + dlon + Math.PI) % 2*DBGlobals.PI) - Math.PI;

	    return new LatLng(
	    	newLat * DBGlobals.RADIANS_TO_DEGREES, 
	    	newLon * DBGlobals.RADIANS_TO_DEGREES);
	}
	
    @Override
    public void onResume() {
    	super.onResume();
    	mLocationClient.connect();
  		mHandler.postDelayed(runQueryVBS, DBGlobals.UPDATE_INTERVAL_STOLEN_VEHICLE);
    	mMapView.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mMapView.onPause();
    	mLocationClient.disconnect();
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the map
    }
    
    @Override
    public void onDestroy() {
    	mLocationClient.disconnect();
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the map
    	mMapView.onDestroy();
    	super.onDestroy();
    }

	//Dynamically update vehicle position on map
	final Handler mHandler = new Handler();
    final Runnable runQueryVBS = new Runnable() {
    	@Override
		public void run() {
			ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL);
			query.whereEqualTo("objectId", UIDtoTrack);
			query.findInBackground(queryVehicleCallback);
		}
	};
	
	LatLng vehicleLocation;
	//Callback when query gets result from Parse
	private FindCallback<ParseObject> queryVehicleCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (e== null){ // no error
				if (objects.size() > 0){
					ParseGeoPoint p =  objects.get(0).getParseGeoPoint("pos");
					if(p==null){
						info.setVisibility(View.VISIBLE);
						info.setText(R.string.vehicle_not_found);
				    	
						//If vehicle location is not found wait 15 secs and try again
					}else{
						info.setVisibility(View.INVISIBLE);							
						info.setText("");
						//Get my location and the Vehicles from parse
						myLocation = mLocationClient.getLastLocation();
				 		if (myLocation != null){
				 			LatLng myLocLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude() );
				 			 vehicleLocation = new LatLng(p.getLatitude(), p.getLongitude());
				 			ParseVehicle vehicle = new ParseVehicle();
				 			vehicle.setMake(objects.get(0).getString("make"));
				 			vehicle.setModel(objects.get(0).getString("model"));
				 			vehicle.setYear(objects.get(0).getNumber("year"));
				 			//Compute the route
				 			GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(vehicleLocation,myLocLatLng, vehicle);
				 			asyncTask.execute();
				 		}
					}
				}else{ //Can't find vehicle
					Toast.makeText(getActivity(), R.string.vehicle_not_found, Toast.LENGTH_SHORT).show();
					mHandler.postDelayed(runQueryVBS, 15000);  //Refresh rate = 15 seconds if error occurs
				}
			}else{ //error occurred when query to Parse
				Toast.makeText(getActivity(), R.string.query_exception, Toast.LENGTH_SHORT).show();
				mHandler.postDelayed(runQueryVBS, 15000);  //Refresh rate = 15 seconds if error occurs
			}
		}
	};
	

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		/*
	     * Google Play services can resolve some errors it detects.If the error has a resolution, 
	     * try sending an Intent to start a Google Play services activity that can resolve
	     * error. */
	    if (result.hasResolution()) {
	        try {
	            // Start an Activity that tries to resolve the error
	        	mHandler.removeCallbacksAndMessages(null);
	        	result.startResolutionForResult(getActivity(), DBGlobals.CONNECTION_FAILURE_RESOLUTION_REQUEST);
	            /*
	             * Thrown if Google Play services canceled the original
	             * PendingIntent
	             */
	        } catch (IntentSender.SendIntentException e) {
	            //e.printStackTrace();

	        }
	    } else {
	        /*  * If no resolution is available, display a dialog to the user with the error. */
	        info.setText(result.getErrorCode());
	        info.setVisibility(TextView.VISIBLE);
	    }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		//Start parsecallback
  		mHandler.post(runQueryVBS);
  		myLocation = mLocationClient.getLastLocation();
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng (myLocation.getLatitude(),myLocation.getLongitude()) , 15f));
	}

	@Override
	public void onDisconnected() {
	    Toast.makeText(getActivity(), "Location manager disconnected. Please re-connect.",Toast.LENGTH_SHORT).show();
    	mHandler.removeCallbacksAndMessages(null); //Cancel dynamic update of the map
	}

	private void handleGetDirectionsResult(ArrayList<LatLng> directionPoints, ParseVehicle vehicleInfo) {
		
		PolylineOptions rectLine = new PolylineOptions().width(30).color(Color.BLUE);

		for(int i = 0 ; i < directionPoints.size() ; i++) 
		{          
			rectLine.add(directionPoints.get(i));
		}
		
		if (newPolyline != null)
		{
			newPolyline.remove();
		}
		
		newPolyline = mGoogleMap.addPolyline(rectLine);
		LatLng myloc = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
		LatLngBounds bounds = createLatLngBoundsObject(myloc,vehicleLocation);
		getSreenDimensions();
		//Move camera to my location
		//mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, screenWidth, screenHeight, 150));
		//mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc , 15f));
		
		//set a default location for vehicle view
		markerOptions = new MarkerOptions();
  		markerOptions.position(vehicleLocation).visible(true);
  		markerVehicle = mGoogleMap.addMarker(markerOptions);

			//Vehicles Location
		markerVehicle.setPosition( vehicleLocation );
		markerVehicle.setTitle(	vehicleInfo.getMake() + " " +
				vehicleInfo.getModel() + " " +
				vehicleInfo.getYear() + " "
		);
		
		//Setup next post
  		mHandler.postDelayed(runQueryVBS, DBGlobals.UPDATE_INTERVAL_STOLEN_VEHICLE);
	}
	
	ProgressDialog progressDialog;
	private class GetDirectionsAsyncTask extends AsyncTask<String, Void, ArrayList<LatLng>> {
        Exception exception=null;
        LatLng vehicle,myLocation;
        ParseVehicle vehicleInfo;
        
        
        public GetDirectionsAsyncTask(LatLng vehicle,LatLng myLocation,ParseVehicle vehicleInfo){
        	this.vehicle=vehicle;
        	this.myLocation=myLocation;
        	this.vehicleInfo = vehicleInfo;
        }
        
        @Override
        protected void onPreExecute() {
			info.setVisibility(View.VISIBLE);
			info.setText("Loading route...");
        }

        @Override
        protected ArrayList<LatLng> doInBackground(String... params)
        {
            try
            {
                GMapV2GetRouteDirection md = new GMapV2GetRouteDirection();
                Document doc = md.getDocument(myLocation,vehicle,GMapV2GetRouteDirection.MODE_DRIVING );
                ArrayList<LatLng> directionPoints = md.getDirection(doc);
                return directionPoints;
            }
            catch (Exception e)
            {
            	exception=e;
                return null;
            }
        }
     
        @Override
        protected void onPostExecute(ArrayList<LatLng> result) {
  			mGoogleMap.clear();
  			
  			if(exception == null){        	
  				info.setVisibility(View.INVISIBLE);
  				info.setText("");
  				handleGetDirectionsResult(result,vehicleInfo);
  				
  			}else{
  	        	info.setVisibility(View.VISIBLE);
  	  			info.setText("Unable to calculate route.");
  			}
        }
  }
}





