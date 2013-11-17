package com.example.ridekeeper;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class HelperFuncs {
	private static Ringtone alarmTone;
	private static Vibrator myVibrator;
	private static long[] vibrationPattern = {0, 200, 500, 100, 0, 0, 0, 0};
	private static MediaPlayer mediaPlayer;
	
	public static MyBroadcastReceiver bReceiver;
	
	public static LocationManager locationManager;
	public static Location myLocation;
	
	//Used as a callback for updatetLocation_inBackground()
	interface GetLocCallback{
		void done();;
	}
	
	public static void initialize(Context context){
        bReceiver = new MyBroadcastReceiver(); //For receiving wake lock and do routine check
        initialAlarmTone(context);
        initialVibrator(context);

        mediaPlayer = MediaPlayer.create(context,R.raw.beep3);
        
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        myLocation = HelperFuncs.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        loadSettings(context);
	}
	
	public static void createAndroidNotification(Context context, String title, String contentText){
		NotificationCompat.Builder notifBuilder =
				new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(contentText)
				.setAutoCancel(true);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(new Intent(context, MainActivity.class));
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		notifBuilder.setContentIntent(resultPendingIntent);
		NotificationManager notifManager =
			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(0, notifBuilder.build());
	}
	
	public static void getLastGoodLoc(){
		myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (myLocation == null){
			myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
	}
	
	public static void updatetLocation_inBackground(Context context, final GetLocCallback callback){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				myLocation = location;
				callback.done();
				locationManager.removeUpdates(this);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	public static void updatetLocation_Blocked(Context context){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				myLocation = location;
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		//10 seconds timeout for GPS lock
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
		int counter = 0;
		
		//Block until we have a GPS lock or timeout
		while (myLocation==null || myLocation.getTime() < Calendar.getInstance().getTimeInMillis() - 2*60*1000){
			myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if (counter >= 10) break; //Timeout = 10 seconds
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			counter++;
		}
		
		locationManager.removeUpdates(locationListener);
	}
	
	public static void initialVibrator(Context context){
		myVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	
	public static void vibrationLong(){
		myVibrator.vibrate(vibrationPattern, 0);
	}
	
	public static void vibrationShort(){
		myVibrator.vibrate(300);
	}
	
	public static void stopVibration(){
		myVibrator.cancel();
	}

	public static void initialAlarmTone(Context context){
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		alarmTone = RingtoneManager.getRingtone(context, notification);
	}
	
	public static void playAlarmTone(){
		if (alarmTone!=null)
			alarmTone.play();
	}
	
	public static void stopAlarmTone(){
		if (HelperFuncs.alarmTone!=null && HelperFuncs.alarmTone.isPlaying())
			HelperFuncs.alarmTone.stop();
	}
	
	public static void playBeep(){
		mediaPlayer.start();
	}
	
	//Query for any stolen vehicle that is within a certain miles
	public static List<ParseObject> queryParseForStolenVehicle_Blocked(double lat, double lng, double withInMiles){
		ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL); //Query the VBS table

		ParseGeoPoint myPoint = new ParseGeoPoint(lat, lng);

		//Constraints: Find any VBS within 'withInMiles' miles of given lat, lng
		query.whereWithinMiles("pos", myPoint, withInMiles);
		query.whereEqualTo("stolen", true);
		
		try {
			List<ParseObject> results = query.find();
			return results;
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return null;
	}
	
	public static void queryParseForStolenVehicle_InBackground(double lat, double lng, double withInMiles, FindCallback<ParseObject> callback){
		ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL); //Query the VBS table

		ParseGeoPoint myPoint = new ParseGeoPoint(lat, lng);

		//Constraints: Find any VBS within 'withInMiles' miles of given lat, lng
		query.whereWithinMiles("pos", myPoint, withInMiles);
		query.whereEqualTo("stolen", true);
		
		query.findInBackground(callback);
	}
	
	//Update phone's location to parse server
	public static void updateLocToParse(Context context){
		//Toast.makeText(context, "Update loc to Parse", Toast.LENGTH_SHORT).show();
		//HelperFuncs.updatetLocation_Blocked(context);
		
		updatetLocation_inBackground(context, new HelperFuncs.GetLocCallback() {
			@Override
			public void done() {
				if (myLocation != null){
					ParseGeoPoint myGeo = new ParseGeoPoint( myLocation.getLatitude(),
															 myLocation.getLongitude() );
					ParseInstallation.getCurrentInstallation().put("GeoPoint", myGeo);
					ParseInstallation.getCurrentInstallation().saveInBackground();
				}
			}
		});
	}
	
	//Update the ownerId field in Installation table in Parse
	//Used when user log in or phone reboot
	public static void updateOwnerIdInInstallation(){
		if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
			ParseInstallation.getCurrentInstallation().put(DBGlobals.PARSE_INSTL_OWERID, ParseUser.getCurrentUser().getObjectId());
			ParseInstallation.getCurrentInstallation().saveInBackground();
		}
	}
	
	//Remove the ownerId field in Installation table in Parse
	//Used when user log out
	public static void removeOwnerIdInInstallation(){
		if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
			ParseInstallation.getCurrentInstallation().put(DBGlobals.PARSE_INSTL_OWERID, "");
			ParseInstallation.getCurrentInstallation().saveInBackground();
		}
	}
	
	public static void postToParse(){
		ParseObject VBS = new ParseObject(DBGlobals.PARSE_VEHICLE_TBL);
		VBS.put("lat", 55.442323);
		VBS.put("lng", -77.293853);
		VBS.saveInBackground();
	}
	
	public static void showDialogFragment(Activity activity, DialogFragment fragment, String dialogName, boolean cancelable, Bundle bundle){
    	FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
    	Fragment prev = activity.getFragmentManager().findFragmentByTag(dialogName);
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
    	fragment.setCancelable(cancelable);
    	if (bundle != null){
    		fragment.setArguments(bundle);
    	}
    	fragment.show(ft, dialogName);
    	
	}
	
	public static void loadSettings(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Preferences.other_beep = pref.getBoolean("other_beep", true);
		Preferences.other_alert = pref.getBoolean("other_alert", true);
		Preferences.other_shortvibration = pref.getBoolean("other_shortvibration", true);
		Preferences.other_longvibration = pref.getBoolean("other_longvibration", true);
		
		Preferences.owner_stolen_beep= pref.getBoolean("owner_stolen_beep", true);
		Preferences.owner_stolen_alert= pref.getBoolean("owner_stolen_alert", true);
		Preferences.owner_stolen_shortvibration = pref.getBoolean("owner_stolen_shortvibration", true);
		Preferences.owner_stolen_longvibration = pref.getBoolean("owner_stolen_longvibration", true);
		
		Preferences.owner_lt_beep = pref.getBoolean("owner_lt_beep", true);
		Preferences.owner_lt_alert = pref.getBoolean("owner_lt_alert", true);
		Preferences.owner_lt_shortvibration = pref.getBoolean("owner_lt_shortvibration", true);
		Preferences.owner_lt_longvibration = pref.getBoolean("owner_lt_longvibration", true);
	}
	
	public static void nearbyVBSAlert(Context context, boolean isAppRunning){
		createAndroidNotification(context, "There are vehicle(s) being stolen nearby", "Click for more info");
		
		if (Preferences.other_beep)
			playBeep();
		if (Preferences.other_alert && !isAppRunning)
			playAlarmTone();
		if (Preferences.other_shortvibration || isAppRunning)
			vibrationShort();
		if (Preferences.other_longvibration && !isAppRunning)
			vibrationLong();
	}
	
	public static void ownerVehicleLiftTiltAlert(Context context, boolean isAppRunning){
		createAndroidNotification(context, "Your vehicle has been tilted/lifted!!", "");
		
		if (Preferences.owner_lt_beep)
			playBeep();
		if (Preferences.owner_lt_alert && !isAppRunning)
			playAlarmTone();
		if (Preferences.owner_lt_shortvibration || isAppRunning)
			vibrationShort();
		if (Preferences.owner_lt_longvibration && !isAppRunning)
			vibrationLong();
	}
	
	public static void ownerVehicleStolenAlert(Context context, boolean isAppRunning){
		createAndroidNotification(context, "Your vehicle has been STOLEN!!", "");
		
		if (Preferences.owner_stolen_beep)
			playBeep();
		if (Preferences.owner_stolen_alert && !isAppRunning)
			playAlarmTone();
		if (Preferences.owner_stolen_shortvibration || isAppRunning)
			vibrationShort();
		if (Preferences.owner_stolen_longvibration && !isAppRunning)
			vibrationLong();
	}
}
