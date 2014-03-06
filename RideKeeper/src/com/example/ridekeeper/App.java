package com.example.ridekeeper;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.example.ridekeeper.account.MyQBUser;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;

public class App extends Application{
	public static boolean isMainActivityRunning = false;
	//public static MyBroadcastReceiver bReceiver;
	public static Handler locationTimerHandler = new Handler();
	public static Runnable locationTimerRunnable;

    private static final String APP_ID = "5815";
    private static final String AUTH_KEY = "8htqAuedCPgyW2z";
    private static final String AUTH_SECRET = "6whwzbRPrYSSbmg";

	public static void initLocationUpdateTimer(final Context context) {
		locationTimerRunnable = new Runnable() {
            @Override
            public void run() {
                ParseFunctions.updateLocToParse(context); //Periodically update phone's location to Parse server
                locationTimerHandler.postDelayed(this, LocationUtils.LOCATION_UPDATE_RATE);
            }
        };

		locationTimerHandler.postDelayed(locationTimerRunnable, LocationUtils.LOCATION_UPDATE_RATE);
	}

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
    	
    	//Register with QuickBlox server
    	MyQBUser.initContext(getApplicationContext());
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
    	QBAuth.createSession(null);
    	
		QBAuth.createSession(new QBCallback() {
			@Override
			public void onComplete(Result result) {
		        if (result.isSuccess()) {
		        	MyQBUser.sessionCreated = true;
		        } else {
		        	Toast.makeText(getApplicationContext(), "Error: " + result.getErrors(), Toast.LENGTH_SHORT).show();
		        }
			}
			@Override
			public void onComplete(Result arg0, Object arg1) {
			}
		});
    	
    	NotificationMgr.initialize(this);
    	Preferences.loadSettingsFromSharedPref(this);
    	
    	ParseFunctions.updateOwnerIdInInstallation();
    }
	
}
