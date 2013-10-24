package com.example.ridekeeper;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;


public class MyBroadcastReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		Toast.makeText(context, "onReceiving", Toast.LENGTH_SHORT).show();
		
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RideKeeper");
		
		//Toast.makeText(context, "Acquiring wlock", Toast.LENGTH_SHORT).show();
		wl.acquire();
		routineCheck(context);
		//Toast.makeText(context, "Releasing wlock", Toast.LENGTH_SHORT).show();
		wl.release();
	}
	
	public void setRepeatingAlarm(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, MyBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, 60000, pi); //do every 1 minutes
		//Toast.makeText(context, "Alarm started", Toast.LENGTH_SHORT).show();
	}

	public void CancelAlarm(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, MyBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
		//Toast.makeText(context, "Alarm canceled", Toast.LENGTH_SHORT).show();
	}
	
	private void routineCheck(Context context){
		// TODO Turn Internet connection on if needed
		
		//Get phone's GPS location
		//Toast.makeText(context, "Getting GPS", Toast.LENGTH_SHORT).show();
		HelperFuncs.getLocation_Blocked(context);
		
		//Toast.makeText(context, "Querying VBS", Toast.LENGTH_SHORT).show();
		//Query Parse server for nearby VBS
		
		if (HelperFuncs.myLocation!=null){
			List<ParseObject> vbsList = HelperFuncs.queryForVBS_Blocked(HelperFuncs.myLocation.getLatitude(),
					HelperFuncs.myLocation.getLongitude(),
					0.5); //search within 0.5 miles radius
			
			if (vbsList!=null && vbsList.size()>0){ //There is at least one vehicle being stolen nearby
				HelperFuncs.StartVibration();
				HelperFuncs.playAlarmTone();
				HelperFuncs.CreateNotif(context,
				Integer.toString(vbsList.size()) + " vehicle(s) being stolen nearby",
				"Click for more info");
				}else{
				HelperFuncs.CreateNotif(context, "No Vehicle being stolen nearby", "");
			}
		}else{
			HelperFuncs.CreateNotif(context, "Can't get phone's GPS location", "");
		}
	}

}
