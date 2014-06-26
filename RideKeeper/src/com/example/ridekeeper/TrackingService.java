package com.example.ridekeeper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.example.ridekeeper.util.LocationUtils;
import com.example.ridekeeper.vehicles.ParseVehicle;
import com.parse.Parse;
import com.parse.ParseObject;

public class TrackingService extends Service implements LocationListener{

	// Location manager
	private LocationManager mLocationManager;
	
	@Override
	public void onCreate() {

        ParseObject.registerSubclass(ParseVehicle.class);
        //Register with Parse server
        Parse.initialize(this,
				"TfBH3NJxzbOaxpksu5YymD4lP9bPlytcfZMG8i5a", 	//Application ID
				"obFmxyzaxx6JWMVMtXpaggDFu2TcARRMqyFQdYpP"); 	//Client Key
		
	    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    
	    if (gpsEnabled)
	    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DBGlobals.RECORDING_GPS_INTERVAL_DEFAULT,DBGlobals.RECORDING_GPS_DISTANCE_DEFAULT, this);
	    else
	    	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DBGlobals.RECORDING_NETWORK_PROVIDER_INTERVAL_DEFAULT,DBGlobals.RECORDING_NETWORK_PROVIDER_DISTANCE_DEFAULT, this);
	}
	
	@Override
	public void onDestroy(){
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Check whether location is valid, drop if invalid
	      if (!LocationUtils.isValidLocation(location)) {
	        return;
	      }
		
	      //Check whether location reading is accurate
	      if (!location.hasAccuracy() || location.getAccuracy() >= DBGlobals.RECORDING_GPS_ACCURACY_DEFAULT) {
	          return;
	        }
	      
	      // Fix for phones that do not set the time field
	      if (location.getTime() == 0L) {
	        location.setTime(System.currentTimeMillis());
	      }
	      //------------------
	      
		// update location list
	    ParseFunctions.updateLocToParse(location); 
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
