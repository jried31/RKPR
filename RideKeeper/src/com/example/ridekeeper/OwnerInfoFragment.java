package com.example.ridekeeper;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridekeeper.qb.chat.ChatFragment;
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
    	if (getArguments() != null && getArguments().containsKey(DBGlobals.ARG_VEHICLE_ID)){
        	mVehicleId = getArguments().getString(DBGlobals.ARG_VEHICLE_ID);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		
    	// TODO: what is this code for?
		//if (ParseUser.getCurrentUser() != null &&
		//		ParseUser.getCurrentUser().isAuthenticated() ){ // User was authenticated
		//	
		//	getDialog().setTitle(TITLE);
		//	
		//	// Stanley: why is this being called here? It loads in the info about the
		//	// current user rather than the owner of the vehicle
		//	//loadProfile(view);

		//} else { // Need sign in/up
		//	DialogFragmentMgr.showDialogFragment(getActivity(), new WelcomeFragment(), "Map Dialog", false, null);
		//	view = inflater.inflate(R.layout.fragment_blank, container, false);
		//}
		
		return view;
	}	
	
	private void loadProfile(View view){
		name = (TextView) view.findViewById(R.id.user_profile_name);
		email = (TextView) view.findViewById(R.id.user_profile_email);
		phone = (TextView) view.findViewById(R.id.user_profile_phone);
		
		ParseUser puser =  ParseUser.getCurrentUser();

		name.setText(puser.getString(USERNAME));
		email.setText(puser.getEmail());
		phone.setText(puser.getString(PHONE));
	}
}
