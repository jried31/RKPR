package com.example.ridekeeper;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ParseCustomReceiver extends BroadcastReceiver{
	
	/* Sample Push 
	    {
		  "action": "ALERT",
		  "status": "stolen" 
		}
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			//String action = intent.getAction();
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			String detail = json.getString("status");

			if (App.isMainActivityRunning){
				HelperFuncs.vibrationShort();
			}else{
				HelperFuncs.vibrationLong();
				HelperFuncs.playAlarmTone();
			}
			
			if (detail.equalsIgnoreCase("nearby")){
				HelperFuncs.CreateNotif(context, "There are vehicle(s) being stolen nearby", "Click for more info");
			}else if (detail.equalsIgnoreCase("tilted")){
				HelperFuncs.CreateNotif(context, "Your vehicle has been tilted!!", "");
			}else if (detail.equalsIgnoreCase("lifted")){
				HelperFuncs.CreateNotif(context, "Your vehicle has been lifted!!", "");
			}else if (detail.equalsIgnoreCase("stolen")){
				HelperFuncs.CreateNotif(context, "Your vehicle has been STOLEN!!", "");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
}
