package com.example.ridekeeper;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseImageView;

public class ParseVehicleArrayAdapter extends ArrayAdapter<ParseVehicle> {
  private final Context context;
  ParseVehicle vehicle;
  private List<ParseVehicle> parseVehicleLst;

  public ParseVehicleArrayAdapter(Context context, List<ParseVehicle> values) {
    super(context, R.layout.fragment_vehicle_item, values);
    this.context = context;
    this.parseVehicleLst = values;
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
    ParseImageView imageView = (ParseImageView) rowView.findViewById(R.id.vehicle_item_photo);
    ImageView findBtn = (ImageView)rowView.findViewById(R.id.findVehicleBtn);
    
    //Assign values to widgets
    vehicle = parseVehicleLst.get(position);
    makeView.setText(vehicle.getMake());
    modelView.setText(vehicle.getModel());
    yearView.setText( vehicle.getYear().toString() );
    statusView.setText(vehicle.getStatus()); 


    findBtn.setTag(position);
    findBtn.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            int position = (Integer) v.getTag();
	    	Bundle bundle = new Bundle();
	    	bundle.putString("UID", vehicle.getObjectId());
	    	DialogFragmentMgr.showDialogFragment((Activity)getContext(), new GoogleMapFindVehicleFragment(), "Map Dialog", true, bundle);
		}
	});
	
    
    vehicle.loadPhotoIntoParseImageView(getContext(), imageView);
	// Load profile photo from internal storage
    /*
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
	*/
	
    return rowView;
  }
  
  @Override
	public void add(ParseVehicle object) {
		// TODO Auto-generated method stub()
		super.add(object);
	}
} 