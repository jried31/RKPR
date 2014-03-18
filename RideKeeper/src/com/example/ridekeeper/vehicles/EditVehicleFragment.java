package com.example.ridekeeper.vehicles;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ridekeeper.R;
import com.example.ridekeeper.util.ImageConsumer;
import com.example.ridekeeper.util.ImageFragment;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditVehicleFragment extends DialogFragment implements ImageConsumer {
	private static final String EDIT_VEHICLE_TITLE = "Edit My Vehicle";
	private static final String ADD_VEHICLE_TITLE = "Add New Vehicle";
	private static final String MODE = "Mode";
	private static final String MODE_ADD = "Add";
	private static final String MODE_EDIT = "Edit";
	private static final String POS = "pos";

    private ParseImageView pivPhoto;
    private EditText etMake, etModel, etYear, etLicense,etTrackerId;
    private Button btSave, btChangePhoto,btToggleTrackerId;
    
	private String mMode = MODE_ADD; //Default is add mode
    private int mPos = 0;
    ParseVehicle vehicle; //current vehicle being add/edit
    private ImageFragment mImageFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mImageFragment = ImageFragment.newInstance(this, null,
    			getFragmentManager());

    	//Checking whether we're adding or editing a vehicle
    	if (getArguments()!=null && getArguments().containsKey(MODE)) {
    		mMode = getArguments().getString(MODE);
    		if (mMode.equals(MODE_EDIT)){
    			mPos = getArguments().getInt(POS);
    		}
    	}

    	setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_DarkActionBar);
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
		if (mMode.equals(MODE_ADD)){
		
            getDialog().setTitle(ADD_VEHICLE_TITLE);

			btSave.setText(MODE_ADD);

			etTrackerId.setEnabled(true);
			vehicle = new ParseVehicle();
			btToggleTrackerId.setVisibility(Button.INVISIBLE);
			vehicle.put("ownerId", ParseUser.getCurrentUser().getObjectId());
			
		} else if (mMode.equals(MODE_EDIT)){
            getDialog().setTitle(EDIT_VEHICLE_TITLE);

			vehicle = MyVehicleListFragment.myVehicleAdapter.getItem(mPos);
			
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
				
				if (mMode.equals(MODE_ADD)){
					MyVehicleListFragment.myVehicleAdapter.add(vehicle);
				}else if (mMode.equals(MODE_EDIT)){
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
    
	public static void addVehicle(FragmentManager fm) {
    	FragmentTransaction ft = fm.beginTransaction();
    	Fragment prev = fm.findFragmentByTag(ADD_VEHICLE_TITLE);
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
		
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString(MODE, MODE_ADD);
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, ADD_VEHICLE_TITLE);
	}
	
	public static void editVehicle(FragmentManager fm, int position){
    	FragmentTransaction ft = fm.beginTransaction();
    	Fragment prev = fm.findFragmentByTag(ADD_VEHICLE_TITLE);
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
		
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString(MODE, MODE_EDIT);
    	args.putInt(POS, position);
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, ADD_VEHICLE_TITLE);
	}
}
