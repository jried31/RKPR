package com.example.ridekeeper;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WelcomeFragment extends DialogFragment {
	private Button btSignup, btSignin, btResetpwd, btExit;
	private EditText etEmail, etPwd;
	
	public static final String USER_NAME="user_name";
	public static final String MY_USER_NAME_HACKFORNOW="jried";
	public static final String EMAIL="email";
	public static final String REALNAME="realName";
	public static final String PHONE="phone";
	public static final String AVATAR="photo";
	public static final String ISSAVED="issaved";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_welcome, container, false);
		
		btSignup = (Button) view.findViewById(R.id.button_signup);
		btSignin = (Button) view.findViewById(R.id.button_signin);
		btResetpwd = (Button) view.findViewById(R.id.button_resetpwd);
		btExit = (Button) view.findViewById(R.id.button_exit);

		etEmail = (EditText) view.findViewById(R.id.editText_email);
		etPwd = (EditText) view.findViewById(R.id.editText_pwd);

		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getActivity());
		etEmail.setText( prefs.getString(EMAIL, "") );

		btSignup.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( etEmail.getText().toString().isEmpty() ||
						etPwd.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), "Email or password can't be empty", Toast.LENGTH_LONG).show();
					return;
				}

				btSignup.setEnabled(false);

				if (HelperFuncs.parseUser==null){
					HelperFuncs.parseUser = new ParseUser();
				}

				HelperFuncs.parseUser.setUsername( etEmail.getText().toString() );
				HelperFuncs.parseUser.setPassword( etPwd.getText().toString() );
				HelperFuncs.parseUser.setEmail( etEmail.getText().toString() );

				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putString(EMAIL, etEmail.getText().toString()).commit();

				HelperFuncs.parseUser.signUpInBackground( new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							//Update ownerId in Installation table
							HelperFuncs.updateOwnerIdInInstallation();

							Toast.makeText(getActivity(), "Your account has been created.", Toast.LENGTH_LONG).show();
							MyProfileFragment.reloadFragment(getActivity());
							dismiss();
						}else{
							Toast.makeText(getActivity(), "Error signing up. " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						btSignup.setEnabled(true);
					}
				});
			}

		});

		btSignin.setOnClickListener(  new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				btSignin.setEnabled(false);

				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putString(EMAIL, etEmail.getText().toString()).commit();

				ParseUser.logInInBackground(etEmail.getText().toString(),
						etPwd.getText().toString(),
						new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null){
							HelperFuncs.parseUser = user;
							HelperFuncs.updateOwnerIdInInstallation();
							Toast.makeText(getActivity(), "You are now signed in!", Toast.LENGTH_LONG).show();
							MyProfileFragment.reloadFragment(getActivity());
							dismiss();
						}else{
							Toast.makeText(getActivity(), "Error signing in. " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						btSignin.setEnabled(true);
					}
				});

			}
		});

		btResetpwd.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btResetpwd.setEnabled(false);
				ParseUser.requestPasswordResetInBackground(etEmail.getText().toString(), new RequestPasswordResetCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							Toast.makeText(getActivity(), "Password reset instruction has been sent to your email." , Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						btResetpwd.setEnabled(true);
					}
				});
			}
		});

		btExit.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.exit(0);
			}
		});
		
		return view;
	}

}


