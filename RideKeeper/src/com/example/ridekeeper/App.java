package com.example.ridekeeper;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.os.Vibrator;

import com.google.android.gms.maps.model.Marker;
import com.parse.Parse;

public class App extends Application{
	@Override public void onCreate() { 
        super.onCreate();
        //Register with Parse server
        Parse.initialize(this,
        				"OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f", 	//Application ID
        				"BJy2YJJA26jnRBalYHQ0VXVtHuZpERFcYqJh1n6S"); 	//Client Key
        
        //Initialize some HelperFuncs obj
        HelperFuncs.initialAlarmTone(this);
        HelperFuncs.initialVibrator(this);
        
        HelperFuncs.myMarkerList = new ArrayList<Marker>();
        HelperFuncs.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        HelperFuncs.myLocation = HelperFuncs.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
    }
}
