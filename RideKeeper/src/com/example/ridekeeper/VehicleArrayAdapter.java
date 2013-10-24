package com.example.ridekeeper;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VehicleArrayAdapter extends ArrayAdapter<Vehicle> {
  private final Context context;
  private List<Vehicle> values;

  public VehicleArrayAdapter(Context context, List<Vehicle> values) {
    super(context, R.layout.fragment_vehicle_item, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    //Called for each view
    View rowView = inflater.inflate(R.layout.fragment_vehicle_item, parent, false);
    
    TextView makeView = (TextView) rowView.findViewById(R.id.vehicle_item_make);
    TextView modelView = (TextView) rowView.findViewById(R.id.vehicle_item_model);
    TextView statusView = (TextView) rowView.findViewById(R.id.vehicle_item_status);
    TextView yearView = (TextView) rowView.findViewById(R.id.vehicle_item_year);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.vehicle_item_photo);
    
    //Assign values to widgets
    Vehicle vehicle = values.get(position);
    makeView.setText(vehicle.getMake());
    modelView.setText(vehicle.getModel());
    yearView.setText(vehicle.getYear());
    statusView.setText(vehicle.getStatus());
    
    // Change the icon for Windows and iPhone

	// Load profile photo from internal storage
	try {
		String photo = vehicle.getPhotoURI();
		FileInputStream fis = getContext().openFileInput(photo == null ? getContext().getString(R.drawable.avatar):photo);
		Bitmap bmap = BitmapFactory.decodeStream(fis);
		imageView.setImageBitmap(bmap);
		fis.close();
	} catch (IOException e) {
		// Default profile photo if no photo saved before.
		imageView.setImageResource(R.drawable.avatar);
	}
	
    return rowView;
  }
  
  @Override
	public void add(Vehicle object) {
		// TODO Auto-generated method stub
		super.add(object);
	}
} 