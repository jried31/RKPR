package com.example.ridekeeper;

import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.quickblox.module.ratings.QBRatings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by AlphaO on 2/17/14.
 */
public class MyRideFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mRideMap;
    private MapView mRideMapView;
    private TextView mRideOdometerView;
    private Polyline mRidePolyline;
    private double mRideOdometer = 0;
    private Button beginRideButton;
    private Button endRideButton;
    private SimpleDateFormat rideDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    private Bundle mBundle;

    private Location myLocation;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(20);
        mLocationClient = new LocationClient(getActivity(), this, this);
        mLocationClient.connect();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(getActivity().openFileInput("rides.dat")));
            StringBuffer data = new StringBuffer();
            data.append("[");
            String line;
            while ((line = inputReader.readLine()) != null) {
                data.append(line + ",");
            }
            data.deleteCharAt(data.length() - 1);
            data.append("]");
            Log.v("data", data.toString());
        } catch (Exception e) {
            Log.v("ff", "dd");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_view, container, false);
        try{
            MapsInitializer.initialize(getActivity());
        }catch (GooglePlayServicesNotAvailableException e){

        }
        mRideOdometerView = (TextView) view.findViewById(R.id.odometer);
        mRideMapView = (MapView) view.findViewById(R.id.map);
        mRideMapView.onCreate(mBundle);
        mRideMap = mRideMapView.getMap();
        mRideMap.setMyLocationEnabled(true);
        mRideMap.getUiSettings().setZoomControlsEnabled(true);
        mRideMap.getUiSettings().setCompassEnabled(true);
        mRideMap.getUiSettings().setMyLocationButtonEnabled(true);
        mRideMap.getUiSettings().setAllGesturesEnabled(true);

        PolylineOptions rectOptions = new PolylineOptions();
        mRidePolyline = mRideMap.addPolyline(rectOptions);

        beginRideButton = (Button) view.findViewById(R.id.begin_ride_button);
        endRideButton = (Button) view.findViewById(R.id.end_ride_button);
        beginRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationClient.requestLocationUpdates(mLocationRequest, MyRideFragment.this);
                v.setVisibility(View.GONE);
                endRideButton.setVisibility(View.VISIBLE);
            }
        });

        endRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationClient.removeLocationUpdates(MyRideFragment.this);

                // Save the ride
                List<LatLng> points = mRidePolyline.getPoints();
                JSONObject rideJSON = new JSONObject();
                JSONArray ridePoints = new JSONArray();
                try {
                    for (LatLng point : points) {
                        JSONObject ridePoint = new JSONObject();
                        ridePoint.put("latitude", point.latitude);
                        ridePoint.put("longitude", point.longitude);
                        ridePoints.put(ridePoint);
                    }

                    Date rideDate = new Date();
                    rideJSON.put("date", rideDateFormat.format(rideDate));
                    rideJSON.put("distance", mRideOdometer);
                    rideJSON.put("points", ridePoints);
                    Log.v("points", rideJSON.toString());

                    FileOutputStream fos = getActivity().openFileOutput("rides.dat", Context.MODE_APPEND | Context.MODE_PRIVATE);
                    fos.write((rideJSON.toString() + "\n").getBytes());
                    fos.close();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "There was an error saving your ride.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRideMapView.onResume();
    }

    @Override
    public void onPause() {
        mRideMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {

        mLocationClient.removeLocationUpdates(this);
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mRideMapView.onDestroy();
        mLocationClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {
    }

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
                        getActivity(),
                        DBGlobals.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If we can't connect, display a toast telling the user.
             */
            Toast.makeText(getActivity(), "Unable to get location.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        if (myLocation == null) {
            return;
        }
        Log.v("RideView", myLocation.toString());
        List<LatLng> points = mRidePolyline.getPoints();
        LatLng newPoint = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        points.add(newPoint);
        float[] distance = new float[3];
        if (points.size() > 1) {
            LatLng lastPoint = points.get(points.size() - 1);
            Location.distanceBetween(lastPoint.latitude, lastPoint.longitude, newPoint.latitude, newPoint.longitude, distance);
        }
        mRideOdometer += distance[0];
        mRideOdometerView.setText(new DecimalFormat("#.##").format(mRideOdometer/1000) + " km");
        mRidePolyline.setPoints(points);
    }
}