package com.example.ridekeeper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditVehicleFragment extends DialogFragment {
    private ParseImageView pivPhoto;
    private EditText etMake, etModel, etYear, etLicense;
    private Button btSave;
    
	private String mode = "add"; //Default is add mode
    private int pos = 0;
    ParseVehicle vehicle; //current vehicle being add/edit
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
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
		btSave = (Button) view.findViewById(R.id.button_save_vehicle);
	    
		
		if (mode.equals("add")){
			btSave.setText("Add");
			vehicle = new ParseVehicle();

			vehicle.put("ownerId", ParseUser.getCurrentUser().getObjectId());
			
		}else if (mode.equals("edit")){
			vehicle = MyVehicleListFragment.myVehicleAdapter.getItem(pos);
			
			//Load data from Parse
			etMake.setText(vehicle.getMake());
			etModel.setText(vehicle.getModel());
			etYear.setText(vehicle.getYear().toString());
			etLicense.setText(vehicle.getLicense());
			vehicle.loadPhoto(getActivity(), pivPhoto);
			
			btSave.setText("Save");
		}

		
		pivPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity() , GetPhoto.class);
				startActivityForResult(intent, 0);
				//will get the result in onActivityResult
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
		
		return view;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode==Activity.RESULT_OK){
    		
    		Bundle extras = data.getExtras();
    		if (extras != null) {
    			pivPhoto.setImageBitmap((Bitmap) extras.getParcelable("data"));
			}
    		
    	}
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
