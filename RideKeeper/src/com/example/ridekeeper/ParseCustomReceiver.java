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
				
			if (detail.equalsIgnoreCase("nearby")){
				HelperFuncs.nearbyVBSAlert(context, App.isMainActivityRunning);
			}else if (detail.equalsIgnoreCase("tilted")){
				HelperFuncs.ownerVehicleLTAlert(context, App.isMainActivityRunning);
			}else if (detail.equalsIgnoreCase("lifted")){
				HelperFuncs.ownerVehicleLTAlert(context, App.isMainActivityRunning);
			}else if (detail.equalsIgnoreCase("stolen")){
				HelperFuncs.ownerVehicleStolenAlert(context, App.isMainActivityRunning);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
}
