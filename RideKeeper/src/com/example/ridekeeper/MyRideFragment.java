package com.example.ridekeeper;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
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
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("ValidFragment")
public class MyRideFragment extends Fragment {
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
    private RideService mRideService;
    private boolean mRideServiceBound = false;
    private Timer mServiceTimer;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_view, container, false);

        try{
            MapsInitializer.initialize(getActivity());
        }catch (GooglePlayServicesNotAvailableException e){
        }

        // Set up all of the view elements
        mRideOdometerView = (TextView) view.findViewById(R.id.odometer);
        mRideMapView = (MapView) view.findViewById(R.id.map);
        mBeginRideButton = (Button) view.findViewById(R.id.begin_ride_button);
        mEndRideButton = (Button) view.findViewById(R.id.end_ride_button);

        // Set up the map view
        mRideMapView.onCreate(mBundle);
        mRideMap = mRideMapView.getMap();
        mRideMap.setMyLocationEnabled(true);
        mRideMap.getUiSettings().setZoomControlsEnabled(true);
        mRideMap.getUiSettings().setCompassEnabled(true);
        mRideMap.getUiSettings().setMyLocationButtonEnabled(true);
        mRideMap.getUiSettings().setAllGesturesEnabled(true);

        PolylineOptions rectOptions = new PolylineOptions();
        mRidePolyline = mRideMap.addPolyline(rectOptions);
        Log.v("banana", "creating view");
        mBeginRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                mEndRideButton.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getActivity(), RideService.class);
                getActivity().startService(intent);
                getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                mServiceTimer = new Timer();
                mServiceTimer.scheduleAtFixedRate(new TimerTask() {
                    synchronized public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLocation();
                            }
                        });
                    }
                }, 0, 5000);
            }
        });

        mEndRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                getActivity().unbindService(mConnection);
                getActivity().stopService(intent);
                mServiceTimer.cancel();
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
        } else if (isRideServiceRunning()) {
            mBeginRideButton.setVisibility(View.GONE);
            mEndRideButton.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isRideServiceRunning()) {
            Intent intent = new Intent(getActivity(), RideService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mServiceTimer = new Timer();
            mServiceTimer.scheduleAtFixedRate(new TimerTask() {
                synchronized public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLocation();
                        }
                    });
                }
            }, 0, 5000);
        }
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
        if (isRideServiceRunning()) {
            getActivity().unbindService(mConnection);
            mRideServiceBound = false;
        }
        if (mServiceTimer != null) {
            Log.v("banana", "canceling timer");
            mServiceTimer.cancel();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mRideMapView.onDestroy();
        super.onDestroy();
    }

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

    public void updateLocation() {
        if (!mRideServiceBound) {
            return;
        }
        List<LatLng> points = mRideService.getRidePoints();
        mRideOdometer = Ride.calculateDistance(points);
        mRideOdometerView.setText(new DecimalFormat("#.##").format(mRideOdometer/1000.0));
        mRidePolyline.setPoints(points);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(points.get(points.size()-1), 16);
        mRideMap.animateCamera(cameraUpdate);
        Log.v("banana", "updating location!");
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

    private boolean isRideServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RideService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RideService.RideBinder binder = (RideService.RideBinder) service;
            mRideService = binder.getService();
            mRideServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mRideServiceBound = false;
        }
    };
}