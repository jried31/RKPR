package com.example.ridekeeper;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("ValidFragment")
public class MyRideFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mRideMap;
    private MapView mRideMapView;
    private TextView mRideOdometerView;
    private Polyline mRidePolyline;
    private double mRideOdometer = 0;
    private Button mBeginRideButton;
    private Button mEndRideButton;
    private SimpleDateFormat rideDateFormat = new SimpleDateFormat("MM-dd-yyyy");
    private Ride ride;
    private boolean savedRide = false;

    private Bundle mBundle;

    private Location myLocation;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

	public MyRideFragment(Ride ride) {
        super();
        this.ride = ride;
        if (ride != null) {
            savedRide = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(20);
        mLocationClient = new LocationClient(getActivity(), this, this);
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

        mBeginRideButton = (Button) view.findViewById(R.id.begin_ride_button);
        mEndRideButton = (Button) view.findViewById(R.id.end_ride_button);
        mBeginRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationClient.requestLocationUpdates(mLocationRequest, MyRideFragment.this);
                v.setVisibility(View.GONE);
                mEndRideButton.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getActivity(), RideService.class);
                getActivity().startService(intent);
            }
        });

        mEndRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationClient.removeLocationUpdates(MyRideFragment.this);
                mEndRideButton.setVisibility(View.GONE);

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
                Intent intent = new Intent(getActivity(), RideService.class);
                getActivity().stopService(intent);
            }
        });

        if (savedRide) {
            mBeginRideButton.setVisibility(View.GONE);
            List<LatLng> points = mRidePolyline.getPoints();
            points.addAll(ride.getPoints());
            mRidePolyline.setPoints(points);
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for(LatLng point : points) {
                boundsBuilder.include(point);
            }
            LatLngBounds rideBounds = boundsBuilder.build();
            getScreenDimensions();
            mRideOdometerView.setText(new DecimalFormat("#.##").format(ride.getDistance() / 1000.0));
            mRideMap.moveCamera(CameraUpdateFactory.newLatLngBounds(rideBounds, screenWidth, screenHeight, 250));
            mRideMap.addMarker(new MarkerOptions().position(ride.getPoints().get(0)).title("Start"));
            mRideMap.addMarker(new MarkerOptions().position(ride.getPoints().get(ride.getPoints().size() - 1)).title("End"));
        }
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
        if (!savedRide) {
            myLocation = mLocationClient.getLastLocation();
            if (myLocation == null) {
            	Log.d("MyRideFragment.onConnected()", "LocationClient.getLastLocation() == null");
            } else {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16);
                mRideMap.animateCamera(cameraUpdate);
            }
        }
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
        List<LatLng> points = mRidePolyline.getPoints();
        LatLng newPoint = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        points.add(newPoint);
        float[] distance = new float[3];
        if (points.size() > 1) {
            LatLng lastPoint = points.get(points.size() - 2);
            Location.distanceBetween(lastPoint.latitude, lastPoint.longitude, newPoint.latitude, newPoint.longitude, distance);
        }
        mRideOdometer += distance[0];
        mRideOdometerView.setText(new DecimalFormat("#.##").format(mRideOdometer/1000.0));
        mRidePolyline.setPoints(points);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(newPoint, 16);
        mRideMap.animateCamera(cameraUpdate);
    }

    private int screenWidth;
    private int screenHeight;
    private void getScreenDimensions()
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            Point outSize = new Point();
            display.getSize(outSize);
            screenWidth = outSize.x;
            screenHeight = outSize.y;
        }
        else
        {
            screenWidth = display.getWidth();
            screenHeight = display.getHeight();
        }
    }
}