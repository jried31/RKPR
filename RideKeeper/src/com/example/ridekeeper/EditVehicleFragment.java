package com.example.ridekeeper;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ridekeeper.util.ImageConsumer;
import com.example.ridekeeper.util.ImageFragment;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditVehicleFragment extends DialogFragment implements ImageConsumer {
    private ParseImageView pivPhoto;
    private EditText etMake, etModel, etYear, etLicense,etTrackerId;
    private Button btSave, btChangePhoto,btToggleTrackerId;
    
	private String mode = "add"; //Default is add mode
    private int pos = 0;
    ParseVehicle vehicle; //current vehicle being add/edit
    private ImageFragment mImageFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mImageFragment = ImageFragment.newInstance(this, null,
    			getFragmentManager());

    	//Checking whether we're adding or editing a vehicle
    	if (getArguments()!=null && getArguments().containsKey("mode")){
    		mode = getArguments().getString("mode");
    		if (mode.equals("edit")){
    			pos = getArguments().getInt("pos");
    		}
    	}

    	setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar  );
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_vehicle, container, false);
		
	    pivPhoto = (ParseImageView) view.findViewById(R.id.edit_vehicle_img);
	    etMake = (EditText) view.findViewById(R.id.editText_make);
	    etModel = (EditText) view.findViewById(R.id.editText_model);
	    etYear = (EditText) view.findViewById(R.id.editText_year);
	    etLicense = (EditText) view.findViewById(R.id.editText_license);
	    etTrackerId = (EditText) view.findViewById(R.id.editText_trackerId);
		btSave = (Button) view.findViewById(R.id.button_save_vehicle);
		btToggleTrackerId = (Button)view.findViewById(R.id.toggleTracker);
	    btChangePhoto = (Button) view.findViewById(R.id.button_change_vehicle_photo);
		if (mode.equals("add")){
			btSave.setText("Add");

			etTrackerId.setEnabled(true);
			vehicle = new ParseVehicle();
			btToggleTrackerId.setVisibility(Button.INVISIBLE);
			vehicle.put("ownerId", ParseUser.getCurrentUser().getObjectId());
			
		}else if (mode.equals("edit")){
	    	//Checking whether we're adding or editing a vehicle
	    	if (getArguments()!=null && getArguments().containsKey("mode")){
	    		mode = getArguments().getString("mode");
	    		if (mode.equals("edit")){
	    			pos = getArguments().getInt("pos");
	    		}
	    	}

			vehicle = MyVehicleListFragment.myVehicleAdapter.getItem(pos);
			
			//Load data from Parse
			etMake.setText(vehicle.getMake());
			etModel.setText(vehicle.getModel());
			etYear.setText(vehicle.getYear().toString());
			etLicense.setText(vehicle.getLicense());
			etTrackerId.setText(vehicle.getTrackerId());
			vehicle.loadPhotoIntoParseImageView(getActivity(), pivPhoto);

			//set not editable first
			etTrackerId.setEnabled(false);
			btSave.setText("Save");
		}

		
		btChangePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mImageFragment.showPhotoSelection();
			}
		});
		
		
		btSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btSave.setEnabled(false);
				
				vehicle.setMake(etMake.getText().toString());
				vehicle.setModel(etModel.getText().toString());
				vehicle.setYear( etYear.getText().toString());
				vehicle.setLicense(etLicense.getText().toString());
				vehicle.setTrackerId(etTrackerId.getText().toString());
				vehicle.prepareSavingPhoto(getActivity(), pivPhoto);
				
				if (mode.equals("add")){
					MyVehicleListFragment.myVehicleAdapter.add(vehicle);
				}else if (mode.equals("edit")){
				}
				
				//Add the new vehicle / save modified vehicle
				vehicle.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							//Now the 'objectId' of the vehicle is available.
							vehicle.savePhotoLocally(getActivity());

							Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
							
						}else{
							Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						
						btSave.setEnabled(true);
						
						//MyVehicleListFragment.myVehicleAdapter.notifyDataSetChanged();
						ParseQuery.clearAllCachedResults();
						MyVehicleListFragment.refreshList();
						getFragmentManager().popBackStack(); //Remove the Edit fragment
					}
				});
			}
		});

	    btToggleTrackerId = (Button) view.findViewById(R.id.toggleTracker);
		btToggleTrackerId.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(etTrackerId.isEnabled())
					etTrackerId.setEnabled(false);
				else 
					etTrackerId.setEnabled(true);
			}
		});
		return view;
    }
    
	public void processBitmap(Bitmap bitmap) {
        pivPhoto.setImageBitmap(bitmap);
	}
    
	public static void addVehicle(FragmentManager fm){
    	FragmentTransaction ft = fm.beginTransaction();
    	Fragment prev = fm.findFragmentByTag("Add Vehicle Dialog");
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
		
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString("mode", "add");
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, "Add Vehicle Dialog");
	}
	
	public static void editVehicle(FragmentManager fm, int position){
    	FragmentTransaction ft = fm.beginTransaction();
    	Fragment prev = fm.findFragmentByTag("Add Vehicle Dialog");
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
		
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString("mode", "edit");
    	args.putInt("pos", position);
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, "Add Vehicle Dialog");
	}
}
