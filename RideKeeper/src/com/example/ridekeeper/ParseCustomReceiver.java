package com.example.ridekeeper;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* This class will handle all Parse's Push notifications whose "action" is "CUSTOMIZED".
 * This is define in AndroidManifiest.xml ( <action android:name="CUSTOMIZED" /> )
 */
public class ParseCustomReceiver extends BroadcastReceiver {
	
	/* Sample Push 
	    {
		  "action": "CUSTOMIZED",
		  "alertLevel": "MVT",
		  "vehicleName": "XYZ ABC 2010"
		}
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			//String action = intent.getAction();
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			String alertLevel = json.getString("alertLevel");
			String data = json.getString("message");
				
			if (alertLevel.equalsIgnoreCase(DBGlobals.ALERT_LEVEL_STOLEN)){
				NotificationMgr.ownerVehicleStolenAlert(context, data, App.isMainActivityRunning);
			}else if (alertLevel.equalsIgnoreCase(DBGlobals.ALERT_LEVEL_NEARBY)){
				NotificationMgr.nearbyVBSAlert(context, data, App.isMainActivityRunning);
			}else if (alertLevel.equalsIgnoreCase(DBGlobals.ALERT_LEVEL_MOVED)){
				NotificationMgr.ownerVehicleStolenAlert(context, data, App.isMainActivityRunning);
			}else if (alertLevel.equalsIgnoreCase(DBGlobals.ALERT_LEVEL_TILT)){
				NotificationMgr.ownerVehicleLiftTiltAlert(context, data, App.isMainActivityRunning);
			/*}else if (alertType.equalsIgnoreCase("lifted")){
				NotificationMgr.ownerVehicleLiftTiltAlert(context, App.isMainActivityRunning);*/
			} else if (alertLevel.equalsIgnoreCase(DBGlobals.ALERT_LEVEL_RECOVERED)){
				NotificationMgr.nearbyVBSAlert(context, data, App.isMainActivityRunning);
			}else if (alertLevel.equalsIgnoreCase(DBGlobals.ALERT_LEVEL_CRASHED)){
				//CALL POLICE
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
}
