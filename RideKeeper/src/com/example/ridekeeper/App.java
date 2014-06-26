package com.example.ridekeeper;

import android.app.Application;

import com.example.ridekeeper.vehicles.ParseVehicle;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;

public class App extends Application{
	public static boolean isMainActivityRunning = false;
	@Override public void onCreate() { 
        super.onCreate();
        
        ParseObject.registerSubclass(ParseVehicle.class);
        //Register with Parse server
        Parse.initialize(this,
				"TfBH3NJxzbOaxpksu5YymD4lP9bPlytcfZMG8i5a", 	//Application ID
				"obFmxyzaxx6JWMVMtXpaggDFu2TcARRMqyFQdYpP"); 	//Client Key
        PushService.setDefaultPushCallback(this, MainActivity.class);
    	ParseInstallation.getCurrentInstallation().saveInBackground();
    	
    	NotificationMgr.initialize(this);
    	Preferences.loadSettingsFromSharedPref(this);
    	
    	ParseFunctions.updateOwnerIdInInstallation();
    }

}
