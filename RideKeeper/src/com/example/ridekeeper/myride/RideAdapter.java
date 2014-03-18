package com.example.ridekeeper.myride;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ridekeeper.R;

public class RideAdapter extends ArrayAdapter<Ride> {
    private Context context;
    private List<Ride> rides;

    public RideAdapter(Context context, List<Ride> values) {
        super(context, R.layout.fragment_ride_item, values);
        this.context = context;
        this.rides = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_ride_item, parent, false);
        TextView dateView = (TextView) rowView.findViewById(R.id.ride_date);
        TextView distanceView = (TextView) rowView.findViewById(R.id.ride_distance);
        dateView.setText(Ride.prettyRideDateFormat.format(rides.get(position).getStartDate()));
        distanceView.setText(new DecimalFormat("#.##").format(rides.get(position).getDistance()/1000));
        return rowView;
    }
}
