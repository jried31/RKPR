package com.example.ridekeeper;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;

public class WelcomeFragment extends DialogFragment {
	private Button btSignup, btSignin, btResetpwd, btExit;
	private EditText etUsername, etPwd, etEmail = null;
	private ParseUser parseSigningUpUser;
	public static final String LAST_SIGNIN_USERNAME="lastusername";
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_welcome, container, false);
		
		btSignup = (Button) view.findViewById(R.id.button_signup);
		btSignin = (Button) view.findViewById(R.id.button_signin);
		btResetpwd = (Button) view.findViewById(R.id.button_resetpwd);
		btExit = (Button) view.findViewById(R.id.button_exit);

		etUsername = (EditText) view.findViewById(R.id.editText_username);
		etPwd = (EditText) view.findViewById(R.id.editText_pwd);

		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getActivity());
		etUsername.setText( prefs.getString(LAST_SIGNIN_USERNAME, "") );

		btSignup.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (etUsername.getText().toString().isEmpty() ||
						etPwd.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), "Error: Username or password can't be empty", Toast.LENGTH_LONG).show();
					return;
				}else if ( etUsername.getText().toString().contains(" ")){
					Toast.makeText(getActivity(), "Error: Username can't have space", Toast.LENGTH_LONG).show();
					return;
				}else if ( etUsername.getText().toString().length() > 10 ){
					Toast.makeText(getActivity(), "Error: Username is at most 10 characters", Toast.LENGTH_LONG).show();
					return;
				}
				
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences( v.getContext() );
				prefs.edit().putString(LAST_SIGNIN_USERNAME, etUsername.getText().toString()).commit();
				
				showSignUpEmailInput();
			}

		});

		btSignin.setOnClickListener(  new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				btSignin.setEnabled(false);

				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putString(LAST_SIGNIN_USERNAME, etUsername.getText().toString()).commit();

				ParseUser.logInInBackground(etUsername.getText().toString(),
						etPwd.getText().toString(),
						new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null){
							//HelperFuncs.parseUser = user;
							ParseFunctions.updateOwnerIdInInstallation();
							
							//sign into QB
							MyQBUser.signin(etUsername.getText().toString(), MyQBUser.DUMMY_PASSWORD);
							
							//update phone's location to parse
							ParseFunctions.updateLocToParse(getActivity());
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
				showResetEmailInput();
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

	private void showSignUpEmailInput(){
		etEmail = new EditText(getActivity());

		AlertDialog.Builder alertDialogEmailInput = new AlertDialog.Builder(getActivity());
		alertDialogEmailInput.setTitle("Sign Up");
		alertDialogEmailInput.setMessage("Your E-mail:");
		alertDialogEmailInput.setView(etEmail);
		alertDialogEmailInput.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//Start signing up process
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (etEmail.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), "Error: Email can't be empty", Toast.LENGTH_LONG).show();
					return;
				}
				
				btSignup.setEnabled(false);

				parseSigningUpUser = new ParseUser();
				parseSigningUpUser.setUsername( etUsername.getText().toString() );
				parseSigningUpUser.setPassword( etPwd.getText().toString() );
				parseSigningUpUser.setEmail( etEmail.getText().toString() );
				parseSigningUpUser.signUpInBackground( new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							//Update ownerId in Installation table
							ParseFunctions.updateOwnerIdInInstallation();

							//Signup QB user
							MyQBUser.signUpSignin(etUsername.getText().toString(), MyQBUser.DUMMY_PASSWORD);
							
							//Update phone location to Parse
							ParseFunctions.updateLocToParse(getActivity());
							
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

		alertDialogEmailInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		alertDialogEmailInput.show();
	}
	
	private void showResetEmailInput(){
		etEmail = new EditText(getActivity());

		AlertDialog.Builder alertDialogEmailInput = new AlertDialog.Builder(getActivity());
		alertDialogEmailInput.setTitle("Reset Password");
		alertDialogEmailInput.setMessage("Your E-mail:");
		alertDialogEmailInput.setView(etEmail);
		alertDialogEmailInput.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//Start reseting password process
			@Override
			public void onClick(DialogInterface dialog, int which) {
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

		alertDialogEmailInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		alertDialogEmailInput.show();
	}
}
