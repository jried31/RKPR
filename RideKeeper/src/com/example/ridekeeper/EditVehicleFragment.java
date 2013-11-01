package com.example.ridekeeper;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditVehicleFragment extends DialogFragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	/*
    	mBundle = savedInstanceState;
    	
    	//Load  UID argument for tracking
    	if (getArguments()!=null && getArguments().containsKey("UID")){
        	UIDtoTrack = getArguments().getString("UID");
        	//Toast.makeText(getActivity(), UIDtoTrack, Toast.LENGTH_SHORT).show();
    	}else{
    		UIDtoTrack = null;
    	}
    	*/
    	
    	setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar  );
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_vehicle, container, false);
		return view;
    }
}
