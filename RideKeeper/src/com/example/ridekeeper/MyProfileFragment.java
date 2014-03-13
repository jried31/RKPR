package com.example.ridekeeper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridekeeper.util.ImageConsumer;
import com.example.ridekeeper.util.ImageFragment;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MyProfileFragment extends Fragment implements ImageConsumer {
	private static final String TAG = MyProfileFragment.class.getSimpleName();

	private static final String USER_PHOTO_PREFIX = "user_photo";
	
	private ParseImageView mImageView;
	
	private TextView loginname;
	private EditText name, email, phone;
	private Button save, change, signout;
	
	public static final String USER_NAME="user_name";
	public static final String EMAIL="email";
	public static final String REALNAME="name";
	public static final String PHONE="phone";
	public static final String AVATAR="avatar";

	private static final String USER_PHOTO_SUBFIX = ".png";
	
	private ImageFragment mImageFragment;

	//private SharedPreferences sharedPreferences;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	mImageFragment = ImageFragment.newInstance(this, null);
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	fragmentTransaction.add(mImageFragment, ImageFragment.TAG).commit();
	}
	
	public static void reloadFragment(Activity activity){
		activity.getFragmentManager().beginTransaction().replace(R.id.content_frame, new MyProfileFragment()).commit();
	}

	public void processBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
	}

	@Override
	public View onCreateView(
			LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {
		
		View view;
		
		ParseUser user = ParseUser.getCurrentUser();
		if (user != null) {
            Log.d(TAG, "userId: " + user.getObjectId());
		}
		
		//User authenticated, show user profile
		if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated() ){
			
			view =  inflater.inflate(R.layout.fragment_my_profile, container, false);
			authenticatedMode(view);
			
		}else{ // User unauthenticated, show signup dialog
			DialogFragmentMgr.showDialogFragment(getActivity(), new WelcomeFragment(), "User Authentication", false, null);
			//Blank background for user login
			view = inflater.inflate(R.layout.fragment_blank, container, false);
		}
		
		return view;
	}
	
	//Parse user was authenticated
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
				ParseFunctions.removeOwnerIdInInstallation();
				ParseQuery.clearAllCachedResults();
				
				reloadFragment(getActivity());
			}
		});
		
		//Setup for Changing Profile Picture
		change = (Button) view.findViewById(R.id.button_change);

		change.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mImageFragment.showPhotoSelection();
			}
		});
	}

	private void loadSnap( ParseImageView mImageView ) {
		// Load profile photo from internal storage
		try {
			FileInputStream fis = getActivity().openFileInput( USER_PHOTO_PREFIX + ParseUser.getCurrentUser().getObjectId() + USER_PHOTO_SUBFIX );
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
	    }
	    
	}
	
	private void loadProfile(View view){
		loginname = (TextView) view.findViewById(R.id.user_profile_loginname);
		name = (EditText) view.findViewById(R.id.user_profile_name);
		email = (EditText) view.findViewById(R.id.user_profile_email);
		phone = (EditText) view.findViewById(R.id.user_profile_phone);
		mImageView = (ParseImageView) view.findViewById(R.id.user_profile_photo);
		
		ParseUser puser =  ParseUser.getCurrentUser();

		loginname.setText("Your Login Name: " + puser.getUsername());
		name.setText(puser.getString(REALNAME));
		email.setText(puser.getEmail());
		phone.setText(puser.getString(PHONE));
		
		loadSnap(mImageView);
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

		ImageFragment.savePhotoLocally(
				getActivity(), 
				croppedData, 
				USER_PHOTO_PREFIX, 
				ParseUser.getCurrentUser().getObjectId());
		
		ParseFile pfAvatar = new ParseFile("avatar.png", croppedData);
		
		// Update Parse
		//ParseUser.getCurrentUser().setUsername(UserName);
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
