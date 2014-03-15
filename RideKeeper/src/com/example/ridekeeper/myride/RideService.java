package com.example.ridekeeper.myride;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.NotificationMgr;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RideService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    private final IBinder mBinder = new RideBinder();
    private Location myLocation;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private List<LatLng> ridePoints = new ArrayList<LatLng>();

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public List<LatLng> getRidePoints() {
        return ridePoints;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    public class RideBinder extends Binder {
        RideService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RideService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(5000);
        //mLocationRequest.setSmallestDisplacement(20);
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        NotificationMgr.createPersistentAndroidNotification(this, "Currently in a Ride", "Click to view");
    }


    @Override
    public void onDestroy() {
        NotificationManager notifManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancel(DBGlobals.RIDE_NOTIFICATION_ID);
        mLocationClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        if (location == null) {
            return;
        }
        LatLng newPoint = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        ridePoints.add(newPoint);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
