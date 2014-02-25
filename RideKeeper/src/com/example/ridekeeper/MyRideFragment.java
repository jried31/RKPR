package com.example.ridekeeper;

import android.app.Fragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by AlphaO on 2/17/14.
 */
public class MyRideFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener {
    private GoogleMap mRideMap;
    private MapView mRideMapView;
    private TextView mRideOdometerView;
    private Polyline mRidePolyline;
    private double mRideOdometer = 0;

    private Bundle mBundle;

    private Location myLocation;
    private LocationClient mLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;

        mLocationClient = new LocationClient(getActivity(), this, this);
        mLocationClient.connect();
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

        // Get back the mutable Polyline
        mRidePolyline = mRideMap.addPolyline(rectOptions);

        Button addPointButton = (Button) view.findViewById(R.id.add_point_button);
        addPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (myLocation == null)
                    myLocation = mLocationClient.getLastLocation();
                Log.v("banana", myLocation.toString());
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
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRideMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRideMapView.onPause();
        mLocationClient.disconnect();
    }

    @Override
    public void onDestroy() {
        mRideMapView.onDestroy();
        mLocationClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        myLocation = mLocationClient.getLastLocation();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }
}