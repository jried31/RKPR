package com.example.ridekeeper;

import com.parse.ParseImageView;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class EditVehicleFragment extends DialogFragment {
    private ParseImageView pivPhoto;
    private EditText etMake, etModel, etYear, etLicense;
    private Button btEdit;
    
	private String mode = "add"; //Default is add mode
    private int pos = 0;
	
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
		btEdit = (Button) view.findViewById(R.id.button_edit_vehicle);
	    
		
		if (mode.equals("add")){
			btEdit.setText("Add");
		}else if (mode.equals("edit")){
			ParseVehicle vehicle = MyVehicleListFragment.myVehicleAdapter.getItem(pos);
			etMake.setText(vehicle.getMake());
			etModel.setText(vehicle.getModel());
			etYear.setText(vehicle.getYear().toString());
			etLicense.setText(vehicle.getLicense());
			vehicle.loadIntoImage(getActivity(), pivPhoto);
			
			btEdit.setText("Save");
		}

		
		btEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ParseVehicle vehicle = MyVehicleListFragment.myVehicleAdapter.getItem(pos);
				vehicle.setMake(etMake.getText().toString());
				vehicle.setModel(etModel.getText().toString());
				vehicle.setYear( etYear.getText().toString());
				vehicle.setLicense(etLicense.getText().toString());
				
				
				vehicle.loadIntoImage(getActivity(), pivPhoto);
			}
		});
		
		return view;
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
