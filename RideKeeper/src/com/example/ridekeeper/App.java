package com.example.ridekeeper;

import android.app.Application;
import android.widget.Toast;

import com.example.ridekeeper.qb.MyQBUser;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.users.model.QBUser;

public class App extends Application{
	public static boolean isMainActivityRunning = false;
	@Override public void onCreate() { 
        super.onCreate();
        
        // MUST Initialize Parse here, otherwise BroadcastReceiver will crash when doing query
        ParseObject.registerSubclass(ParseVehicle.class);
        ParseObject.registerSubclass(ParseChatRoomPhoto.class);
        //Register with Parse server
        Parse.initialize(this,
				"TfBH3NJxzbOaxpksu5YymD4lP9bPlytcfZMG8i5a", 	//Application ID
				"obFmxyzaxx6JWMVMtXpaggDFu2TcARRMqyFQdYpP"); 	//Client Key
        PushService.setDefaultPushCallback(this, MainActivity.class);
    	ParseInstallation.getCurrentInstallation().saveInBackground();
    	
    	//bReceiver = new MyBroadcastReceiver(); //For receiving wake lock and do routine check
    	
    	NotificationMgr.initialize(this);
    	Preferences.loadSettingsFromSharedPref(this);
    	
    	ParseFunctions.updateOwnerIdInInstallation();
    }

}
