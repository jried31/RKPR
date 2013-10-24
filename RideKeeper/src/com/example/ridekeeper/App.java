package com.example.ridekeeper;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class App extends Application{
	@Override public void onCreate() { 
        super.onCreate();
        //Register with Parse server

        
        //Initialize some HelperFuncs obj
        //Toast.makeText(this, "Application start", Toast.LENGTH_SHORT).show();
        HelperFuncs.initialize(this);
    }
	
}
