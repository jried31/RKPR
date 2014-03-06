package com.example.ridekeeper;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationMgr  implements 
    LocationListener,
    GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String EXTRA_LOCATION_MGR = "com.example.ridekeeper.locationmgr";
	public LocationClient locationClient;
	public Location location;
	
	private LocationRequest sLocationRequest;
	private Activity sMainActivity;
	
	//Used as a callback for updatetLocation_inBackground()
	interface GetLocCallback{
		void done();
	}
	
	public LocationMgr(Activity mainActivity){

		sMainActivity = mainActivity;
        locationClient = new LocationClient(sMainActivity, this, this);

        // Create a new global location parameters object
        sLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        sLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        sLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        sLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        
	}

	public void connect() {
		if (servicesConnected()) {
            locationClient.connect();
		}
	}
	
	public void disconnect() {
		locationClient.disconnect();
	}

    public Location getLastGoodLocation() {
        // If Google Play Services is available
        if (locationClient.isConnected()) {
            // Get the current location
            location = locationClient.getLastLocation();
            String debugStr = sMainActivity.getString(R.string.get_last_location) + 
            		" - lat/lng: " + location.getLatitude() + "/" + location.getLongitude();
        	Log.d(LocationUtils.LOCATION_UPDATE, debugStr);
        } else {
        	Log.d(LocationUtils.LOCATION_UPDATE, sMainActivity.getString(R.string.location_client_disconnected));
        }
        
        return location;
    }
	
    /*
	public static void updateLocationInBackground(Context context, final GetLocCallback callback){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				sLocation = location;
				callback.done();
				sLocationClient.removeUpdates(this);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		sLocationClient = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		sLocationClient.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	public static void updatetLocation_Blocked(Context context){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				sLocation = location;
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		sLocationClient = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		//10 seconds timeout for GPS lock
		sLocationClient.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
		int counter = 0;
		
		//Block until we have a GPS lock or timeout
		while (sLocation==null || sLocation.getTime() < Calendar.getInstance().getTimeInMillis() - 2*60*1000){
			sLocation = sLocationClient.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if (counter >= 10) break; //Timeout = 10 seconds
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			counter++;
		}
		
		sLocationClient.removeUpdates(locationListener);
	}
	*/

    /**
     * LocationListener callback
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
    	this.location = location;
    }

    /**
     * Send a request to Location Services
     */
    public void startPeriodicUpdates() {
    	if (locationClient.isConnected()) {
    		Log.d(LocationUtils.LOCATION_UPDATE, sMainActivity.getString(R.string.start_periodic_updates));
            locationClient.requestLocationUpdates(sLocationRequest, this);
    	}
    }

    /**
     * Send a request to
     * Location Services
     */
    public void stopPeriodicUpdates() {
    	if (locationClient.isConnected()) {
    		Log.d(LocationUtils.LOCATION_UPDATE, sMainActivity.getString(R.string.stop_periodic_updates));
            locationClient.removeLocationUpdates(this);
    	}
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
    	Log.d("LocationMgr.onConnected()", "LocationClient connected");
        startPeriodicUpdates();
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
    	Log.d("LocationMgr.onDisconnected()", "LocationClient disconnected");
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        sMainActivity,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(sMainActivity);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.GOOGLE_SERVICE, sMainActivity.getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, sMainActivity, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(sMainActivity.getFragmentManager(), LocationUtils.GOOGLE_SERVICE);
            }
            return false;
        }
    }
    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            sMainActivity,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(sMainActivity.getFragmentManager(), LocationUtils.GOOGLE_SERVICE);
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

	
}
