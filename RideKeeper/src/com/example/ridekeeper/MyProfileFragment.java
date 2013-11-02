package com.example.ridekeeper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MyProfileFragment extends Fragment {
	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 100;
	public static final int REQUEST_CODE_CROP_PHOTO = 101;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 102;

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private static final String URI_INSTANCE_STATE_KEY = "saved_uri";

	private Uri mImageCaptureUri;
	private ParseImageView mImageView;
	private boolean isTakenFromCamera;
	
	private EditText name, email, phone, pwd;
	private Button save, change, signup, signin, signout, resetpwd;
	
	public static final String USER_NAME="user_name";
	public static final String MY_USER_NAME_HACKFORNOW="jried";
	public static final String EMAIL="email";
	public static final String REALNAME="realName";
	public static final String PHONE="phone";
	public static final String AVATAR="photo";
	public static final String ISSAVED="issaved";
	
	//private SharedPreferences sharedPreferences;
	
	private void reloadFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new MyProfileFragment()).commit();
	}

	// Handle data after activity returns.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_CODE_SELECT_FROM_GALLERY:
			mImageCaptureUri = data.getData();
			cropImage();
			break;

		case REQUEST_CODE_TAKE_FROM_CAMERA:
			// Send image taken from camera for cropping
			cropImage();
			break;

		case REQUEST_CODE_CROP_PHOTO:
			// Update image view after image crop

			Bundle extras = data.getExtras();

			// Set the picture image in UI
			if (extras != null) {
				mImageView.setImageBitmap((Bitmap) extras.getParcelable("data"));
			}

			// Delete temporary image taken by camera after crop.
			if (isTakenFromCamera) {
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists())
					f.delete();
			}

			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
		View view;
		
		if (HelperFuncs.parseUser != null &&
			HelperFuncs.parseUser.isAuthenticated() ){ // User was authenticated
			
			view =  inflater.inflate(R.layout.fragment_my_profile, container, false);
			authenticatedMode(view);
			
		}else{ // Need sign in/up
			view = inflater.inflate(R.layout.fragment_sign_up_in, container, false);
			signUpsignInMode(view);
		}
		
		return view;
	}

	private void signUpsignInMode(View view){

		signup = (Button) view.findViewById(R.id.button_signup);
		signin = (Button) view.findViewById(R.id.button_signin);
		resetpwd = (Button) view.findViewById(R.id.button_resetpwd);
		
		email = (EditText) view.findViewById(R.id.editText_email);
		pwd = (EditText) view.findViewById(R.id.editText_pwd);
		
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getActivity());
		email.setText( prefs.getString(EMAIL, "") );
		
		signup.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( email.getText().toString().isEmpty() ||
						pwd.getText().toString().isEmpty()){
					Toast.makeText(getActivity(), "Email or password can't be empty", Toast.LENGTH_LONG).show();
					return;
				}
				
				signup.setEnabled(false);
				
				if (HelperFuncs.parseUser==null){
					HelperFuncs.parseUser = new ParseUser();
				}
				
				HelperFuncs.parseUser.setUsername( email.getText().toString() );
				HelperFuncs.parseUser.setPassword( pwd.getText().toString() );
				HelperFuncs.parseUser.setEmail( email.getText().toString() );
				
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putString(EMAIL, email.getText().toString()).commit();
				
				HelperFuncs.parseUser.signUpInBackground( new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							//Update ownerId in Installation table
							HelperFuncs.updateOwnerIdInInstallation();
							
							Toast.makeText(getActivity(), "Your account has been created.", Toast.LENGTH_LONG).show();
							reloadFragment();
						}else{
							Toast.makeText(getActivity(), "Error signing up. " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						signup.setEnabled(true);
					}
				});
			}
			
		});
		
		
		signin.setOnClickListener(  new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				signin.setEnabled(false);
				
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putString(EMAIL, email.getText().toString()).commit();
				
				ParseUser.logInInBackground(email.getText().toString(),
											pwd.getText().toString(),
											new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null){
							HelperFuncs.parseUser = user;
							HelperFuncs.updateOwnerIdInInstallation();
							Toast.makeText(getActivity(), "You are now signed in!", Toast.LENGTH_LONG).show();
							reloadFragment();
						}else{
							Toast.makeText(getActivity(), "Error signing in. " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						signin.setEnabled(true);
					}
				});
				
			}
		});
		
		resetpwd.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetpwd.setEnabled(false);
				ParseUser.requestPasswordResetInBackground(email.getText().toString(), new RequestPasswordResetCallback() {
					@Override
					public void done(ParseException e) {
						if (e==null){
							Toast.makeText(getActivity(), "Password reset instruction has been sent to your email." , Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						resetpwd.setEnabled(true);
					}
				});
			}
		});
		
	}
	
	private void authenticatedMode(View view){
		loadProfile(view);

		save = (Button) view.findViewById(R.id.button_save);
		save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveProfile(v);
			}
		});

		signout = (Button) view.findViewById(R.id.button_signout);
		signout.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ParseUser.logOut();
				HelperFuncs.removeOwnerIdInInstallation();
				reloadFragment();
			}
		});
		
		//Setup for Changing Profile Picture
		change = (Button) view.findViewById(R.id.button_change);

		change.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Save the Content to the Shared Preferences & Upload to Parse

				final Activity parent = getActivity();
				AlertDialog.Builder builder = new AlertDialog.Builder(parent);
				DialogInterface.OnClickListener dlistener;
				builder.setTitle(R.string.photo_picker_title);
				dlistener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						onPhotoPickerItemSelected(item);					
					}
				};

				builder.setItems(R.array.photo_picker_items, dlistener);
				builder.create().show();//.show(getFragmentManager(),getString(R.string.photo_picker_tag));
			}
		});
	}
	
	
	public void onPhotoPickerItemSelected(int item) {
		Intent intent;
		isTakenFromCamera = false;

		switch(item){
		case MyProfileFragment.ID_PHOTO_PICKER_FROM_CAMERA:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			mImageCaptureUri = Uri.fromFile(new File(Environment
					.getExternalStorageDirectory(), "tmp_"
							+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);
			try {
				startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
			isTakenFromCamera = true;
			break;

		case MyProfileFragment.ID_PHOTO_PICKER_FROM_GALLERY:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			mImageCaptureUri = Uri.fromFile(new File(Environment
					.getExternalStorageDirectory(), "tmp_"
							+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);
			try{
				startActivityForResult(intent, REQUEST_CODE_SELECT_FROM_GALLERY);
			}catch(ActivityNotFoundException e){
				e.printStackTrace();
			}
			isTakenFromCamera = false;
			break;

		default:
			return;
		}
	}

	private void loadSnap() {
		// Load profile photo from internal storage
		try {
			FileInputStream fis = getActivity().openFileInput(getString(R.string.photo_filename));
			Bitmap bmap = BitmapFactory.decodeStream(fis);
			mImageView.setImageBitmap(bmap);
			fis.close();
			return;
		} catch (IOException e) {
			// Default profile photo if no photo saved before.
			mImageView.setImageResource(R.drawable.avatar);
		}
		
		// Load from Parse if fail to load from storage
	    ParseFile pfAvatar = ParseUser.getCurrentUser().getParseFile(AVATAR);
	    if (pfAvatar!=null){
	    	mImageView.setParseFile(pfAvatar);
	    	mImageView.loadInBackground( new GetDataCallback() {
				
				@Override
				public void done(byte[] data, ParseException e) {
				}
			});

	    	// TODO: Save image to internal storage
	    }
	    
	}

	private void saveLocSnap(byte[] data){
		try {
			FileOutputStream fos = getActivity().openFileOutput(
					getString(R.string.photo_filename), Activity.MODE_PRIVATE);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2){
			e2.printStackTrace();
		}
	}
	
	// Crop and resize the image for profile
	private void cropImage() {
		// Use existing crop activity.
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

		// Specify image size
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);

		// Specify aspect ratio, 1:1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
		// identify the activity in onActivityResult() when it returns
		startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
	}
	
	private void loadProfile(View view){
		name = (EditText) view.findViewById(R.id.user_profile_name);
		email = (EditText) view.findViewById(R.id.user_profile_email);
		phone = (EditText) view.findViewById(R.id.user_profile_phone);
		mImageView = (ParseImageView) view.findViewById(R.id.user_profile_photo);
		
		ParseUser puser =  ParseUser.getCurrentUser();

		name.setText(puser.getString(REALNAME));
		email.setText(puser.getEmail());
		phone.setText(puser.getString(PHONE));
		
		loadSnap();
	}
	
	private void saveProfile(View view){
		String 	nameVal = name.getText().toString(),
				emailVal = email.getText().toString(),
				phoneVal = phone.getText().toString();
		
		save.setEnabled(false);
		
		// Prepare image for saving
		mImageView.buildDrawingCache();
		Bitmap bmap = mImageView.getDrawingCache();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] croppedData = bos.toByteArray();

		saveLocSnap(croppedData);
		
		ParseFile pfAvatar = new ParseFile(getString(R.string.photo_filename), croppedData);
		
		// Update Parse
		ParseUser.getCurrentUser().setUsername(emailVal);
		ParseUser.getCurrentUser().setEmail(emailVal);
		ParseUser.getCurrentUser().put(REALNAME, nameVal);
		ParseUser.getCurrentUser().put(PHONE, phoneVal);
		ParseUser.getCurrentUser().put(AVATAR, pfAvatar);

		
		ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e==null){
					save.setEnabled(true);
					Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	

	}
}
