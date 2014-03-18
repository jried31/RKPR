package com.example.ridekeeper;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ridekeeper.vehicles.ParseVehicle;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class OwnerInfoFragment extends DialogFragment {
	private static final String TAG = OwnerInfoFragment.class.getSimpleName();

	// VBS VEHICLE ID to be tracked, null for all VBS
	private static String mVehicleId = null;
	private static String mOwnerId = null;

	private static final String TITLE = "Owner Information";
	private TextView name, email, phone;
	public static final String EMAIL="email";
	public static final String USERNAME="username";
	public static final String PHONE="phone";
	
	//private SharedPreferences sharedPreferences;
	
	private static FindCallback<ParseObject> queryVehicleOwnerCallback = new FindCallback<ParseObject>() {
		@Override
		public void done(List<ParseObject> objects, ParseException e) {
			if (e== null){ // no error

				Log.d(TAG, "Found " + objects.size() + " matching ownerIds for vid: " + mVehicleId);

				if (objects.size() > 0) {
                    mOwnerId = objects.get(0).toString();
				}
			}else{ //error occurred when query to Parse
				Log.d(TAG, "Error: " + e.getMessage());
			}
		}
	};
	public static void reloadFragment(FragmentActivity activity) {
		activity.getSupportFragmentManager().beginTransaction().
                replace(R.id.content_frame, new OwnerInfoFragment()).commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
        View view =  inflater.inflate(R.layout.fragment_owner_info_public, container, false);

    	//Load  UID argument for tracking
    	if (getArguments() != null && getArguments().containsKey(ParseVehicle.ID)){
        	mVehicleId = getArguments().getString(ParseVehicle.ID);
        	Log.d(TAG, "vehicleId: " + mVehicleId);

        	ParseUser puser;

			try {
				mOwnerId = ParseFunctions.queryForVehicleOwner(mVehicleId);
				puser = ParseUser.getQuery().get(mOwnerId);
                Log.d(TAG, "ownerId: " + mOwnerId);

                name = (TextView) view.findViewById(R.id.user_profile_name);
                email = (TextView) view.findViewById(R.id.user_profile_email);
                phone = (TextView) view.findViewById(R.id.user_profile_phone);

	    		name.setText(puser.getString(USERNAME));
	    		email.setText(puser.getEmail());
	    		phone.setText(puser.getString(PHONE));

                getDialog().setTitle(TITLE);

			} catch (ParseException e) {
				e.printStackTrace();
			}
    	}
		return view;
	}
}
