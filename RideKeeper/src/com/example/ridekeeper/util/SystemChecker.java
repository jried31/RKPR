package com.example.ridekeeper.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import com.example.ridekeeper.R;

public class SystemChecker {
    /** source: http://developmentality.wordpress.com/2009/10/31/android-dialog-box-tutorial/
     * https://stackoverflow.com/questions/12044552/android-activate-gps-with-alertdialog-how-to-wait-for-the-user-to-take-action
	 * source: https://stackoverflow.com/questions/10311834/android-dev-how-to-check-if-location-services-are-enabled
	 * @return
	 */
	public static void enableLocationProviders(Activity activity) {
        boolean gps_enabled = false;
        boolean network_enabled = false;

        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex){
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex){
	    	Toast.makeText(activity, "NETWORK_PROVIDER exception", Toast.LENGTH_SHORT).show();
        }

        //if(!gps_enabled && !network_enabled){
        if(!gps_enabled && !network_enabled) {
        	buildAlertMessageNoLocationAccess(activity);
        } else if (!gps_enabled) {
            buildAlertMessageNoGps(activity);
	    	Toast.makeText(activity, "GPS not enabled.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "GPS enabled.", Toast.LENGTH_SHORT).show();
        }
	}

    private static void buildAlertMessageNoGps(Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final Activity mainActivity = activity;
        builder.setMessage("Yout GPS seems to be disabled, do you want to enable it?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
                        Intent gpsOptionsIntent = new Intent(  
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
                            mainActivity.startActivity(gpsOptionsIntent);
                                }
				});
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
        builder.show();
    }
    
    private static void buildAlertMessageNoLocationAccess(Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setMessage(activity.getResources().getString(R.string.gps_network_not_enabled));

        final Activity mainActivity = activity;
        dialog.setPositiveButton(activity.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                mainActivity.startActivity(myIntent);
            }
        });
        dialog.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }
    


}
