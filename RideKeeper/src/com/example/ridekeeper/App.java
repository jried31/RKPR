package com.example.ridekeeper;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;

public class App extends Application{
	public static boolean isMainActivityRunning = false;
	public static MyBroadcastReceiver bReceiver;


	@Override public void onCreate() { 
        super.onCreate();
        
        // MUST Initialize Parse here, otherwise BroadcastReceiver will crash when doing query
        ParseObject.registerSubclass(ParseVehicle.class);
        //Register with Parse server
        Parse.initialize(this,
				"OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f", 	//Application ID
				"BJy2YJJA26jnRBalYHQ0VXVtHuZpERFcYqJh1n6S"); 	//Client Key
        PushService.setDefaultPushCallback(this, MainActivity.class);
    	ParseInstallation.getCurrentInstallation().saveInBackground();
    	
    	bReceiver = new MyBroadcastReceiver(); //For receiving wake lock and do routine check
    	
    	NotificationMgr.initialize(this);
    	Preferences.loadSettingsFromSharedPref(this);
    	
    	LocationMgr.initialize(this);
    	ParseFunctions.updateOwnerIdInInstallation();
    }
	
}
