package com.example.ridekeeper.vehicles;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.DialogFragmentMgr;
import com.example.ridekeeper.GoogleMapFindVehicleFragment;
import com.example.ridekeeper.GoogleMapFragment;
import com.example.ridekeeper.R;
import com.example.ridekeeper.R.id;
import com.example.ridekeeper.R.layout;
import com.parse.ParseImageView;

public class ParseVehicleArrayAdapter extends ArrayAdapter<ParseVehicle> {
  private final Context context;
  ParseVehicle vehicle;
  private List<ParseVehicle> parseVehicleLst;
  private static int displayType;

  public ParseVehicleArrayAdapter(Context context, List<ParseVehicle> values,int type) {
    super(context, R.layout.vehicle_item_fragment, values);
    this.context = context;
    this.parseVehicleLst = values;
    ParseVehicleArrayAdapter.displayType = type;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    //Called for each view
    View rowView = inflater.inflate(R.layout.vehicle_item_fragment, parent, false);
    
    TextView makeView = (TextView) rowView.findViewById(R.id.vehicle_item_make);
    TextView modelView = (TextView) rowView.findViewById(R.id.vehicle_item_model);
    TextView statusView = (TextView) rowView.findViewById(R.id.vehicle_item_status);
    TextView yearView = (TextView) rowView.findViewById(R.id.vehicle_item_year);
    ParseImageView imageView = (ParseImageView) rowView.findViewById(R.id.vehicle_item_photo);
    ImageView findVehicleBtn = (ImageView)rowView.findViewById(R.id.findVehicleBtn);
    
    //Assign values to widgets
    vehicle = parseVehicleLst.get(position);
    makeView.setText(vehicle.getMake());
    modelView.setText(vehicle.getModel());
    yearView.setText( vehicle.getYear().toString() );
    statusView.setText(vehicle.getStatus()); 


    findVehicleBtn.setTag(position);
    findVehicleBtn.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            int position = (Integer) v.getTag();

	    	Bundle bundle = new Bundle();
	    	bundle.putString(DBGlobals.ARG_VEHICLE_ID, vehicle.getObjectId());

	    	if (displayType==DBGlobals.TAB_IDX_MY_VEHICLES) {
	    		DialogFragmentMgr.showDialogFragment((FragmentActivity)getContext(), new GoogleMapFindVehicleFragment(), "Map Dialog", true, bundle);
	    	}
	    	else if (displayType == DBGlobals.TAB_IDX_STOLEN_VEHICLES) {
	    		DialogFragmentMgr.showDialogFragment((FragmentActivity)getContext(), new GoogleMapFragment(), "Map Dialog", true, bundle);
	    	}
		}
	});
	
    
    vehicle.loadPhotoIntoParseImageView(getContext(), imageView);
	
    return rowView;
  }
  
  @Override
	public void add(ParseVehicle object) {
		super.add(object);
	}
} 