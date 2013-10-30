package com.example.ridekeeper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class HelperFuncs {
	private static Ringtone alarmTone;
	private static Vibrator myVibrator;
	private static long[] vibrationPattern = {0, 200, 500, 100, 0, 0, 0, 0};
	
	public static ParseUser parseUser;
	public static MyBroadcastReceiver bReceiver;
	
	public static LocationManager locationManager;
	public static Location myLocation;
	
	public static List<ParseObject> myVBSList;
	public static List<Marker> myMarkerList;
	
	public static void initialize(Context context){
        bReceiver = new MyBroadcastReceiver(); //For receiving wake lock and do routine check
        initialAlarmTone(context);
        initialVibrator(context);

        myMarkerList = new ArrayList<Marker>();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        myLocation = HelperFuncs.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        parseUser = new ParseUser();
        
        //bReceiver.disable(context);
        //bReceiver.setRepeatingAlarm(context);
	}
	
	public static void CreateNotif(Context context, String title, String contentText){
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
	
	//Query for any stolen vehicle that is within a certain miles
	public static List<ParseObject> queryForVBS_Blocked(double lat, double lng, double withInMiles){
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
	
	public static void queryForVBS_NonBlocked(double lat, double lng, double withInMiles, FindCallback<ParseObject> callback){
		ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL); //Query the VBS table

		ParseGeoPoint myPoint = new ParseGeoPoint(lat, lng);

		//Constraints: Find any VBS within 'withInMiles' miles of given lat, lng
		query.whereWithinMiles("pos", myPoint, withInMiles);
		query.whereEqualTo("stolen", true);
		
		query.findInBackground(callback);
	}

	public static void queryForVBSwithUID_NonBlocked(String uid, FindCallback<ParseObject> callback){
		ParseQuery<ParseObject> query = ParseQuery.getQuery(DBGlobals.PARSE_VEHICLE_TBL); //Query the VBS table

		//ParseGeoPoint myPoint = new ParseGeoPoint(lat, lng);
		//Constraints: Find any VBS within 'withInMiles' miles of given lat, lng
		//query.whereWithinMiles("pos", myPoint, withInMiles);
		//query.whereEqualTo("stolen", true);
		query.whereEqualTo("objectId", uid);
		
		query.findInBackground(callback);
	}
	
	
	public static void postToParse(){
		ParseObject VBS = new ParseObject(DBGlobals.PARSE_VEHICLE_TBL);
		VBS.put("lat", 55.442323);
		VBS.put("lng", -77.293853);
		VBS.saveInBackground();
	}
	
	public static void removeAllMarker(){
		while (myMarkerList.size()>0){
			myMarkerList.get(0).remove();
			myMarkerList.remove(0);
		}
	}
}
