package com.example.ridekeeper;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyRideListFragment extends ListFragment {
    List<Ride> rides;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(getActivity().openFileInput("rides.dat")));
            StringBuffer data = new StringBuffer();
            data.append("[");
            String line;
            while ((line = inputReader.readLine()) != null) {
                data.append(line + ",");
            }
            data.deleteCharAt(data.length() - 1);   // Remove the final comma
            data.append("]");
            JSONArray ridesJSON = new JSONArray(data.toString());
            rides = new ArrayList<Ride>();
            for (int i = 0; i < ridesJSON.length(); i++) {
                Ride ride = new Ride();
                JSONObject rideJSON = ridesJSON.getJSONObject(i);
                JSONArray ridePoints = rideJSON.getJSONArray("points");
                List<LatLng> points = new ArrayList<LatLng>();
                for (int pointIndex = 0; pointIndex < ridePoints.length(); pointIndex++) {
                    points.add(new LatLng(ridePoints.getJSONObject(pointIndex).getDouble("latitude"), ridePoints.getJSONObject(pointIndex).getDouble("longitude")));
                }
                ride.setPoints(points);
                ride.setDistance(rideJSON.getDouble("distance"));
                ride.setStartDate(Ride.rideDateFormat.parse(rideJSON.getString("date")));
                rides.add(ride);
            }
            RideAdapter adapter = new RideAdapter(getActivity(), rides);
            setListAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "There was an error displaying your rides", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rides_list, container, false);

        Button addRideButton = (Button) view.findViewById(R.id.add_ride);
        addRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content_frame, new MyRideFragment(null)).commit();
            }
        });
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new MyRideFragment(rides.get(position))).commit();
    }
}