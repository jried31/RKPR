package com.example.ridekeeper;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.os.Vibrator;
import android.widget.Toast;

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
        //Toast.makeText(this, "Application start", Toast.LENGTH_SHORT).show();
        HelperFuncs.initialize(this);
    }
	
}
