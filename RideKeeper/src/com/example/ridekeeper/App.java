package com.example.ridekeeper;

import android.app.Application;

import com.parse.Parse;

public class App extends Application{
	@Override public void onCreate() { 
        super.onCreate();
        
        // MUST Initialize Parse here, otherwise BroadcastReceiver will crash when doing query
        //Register with Parse server
        Parse.initialize(this,
				"OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f", 	//Application ID
				"BJy2YJJA26jnRBalYHQ0VXVtHuZpERFcYqJh1n6S"); 	//Client Key
        
        //Initialize some HelperFuncs obj
        //Toast.makeText(this, "Application start", Toast.LENGTH_SHORT).show();
        HelperFuncs.initialize(this);
    }
	
}
