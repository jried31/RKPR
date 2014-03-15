package com.example.ridekeeper.vehicles;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.R;
import com.example.ridekeeper.R.drawable;
import com.example.ridekeeper.util.ImageFragment;
import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;

@ParseClassName(DBGlobals.PARSE_VEHICLE_TBL)
public class ParseVehicle extends ParseObject {
	private static final String TAG = ParseVehicle.class.getSimpleName();

    public static final String MAKE = "make";
    public static final String MODEL = "model";
    public static final String YEAR = "year";
    public static final String LICENSE = "license";
    public static final String TRACKERID = "trackerId";

    public static final String CHATROOM_NAME = "roomName"; // in Chat table

    public static final String PHOTO = "photo";
    public static final String OWNERID = "ownerId";
    public static final String 	STATUS = "status";
	
	private static final String PHOTOFILE_PREFIX = "vehicle_photo_",
								PHOTOFILE_SUFFIX = ".png";
	private Context myContext;
	private byte[] photoData;
	
	public ParseVehicle() {
	}

	public String getStatus(){
		return getString(STATUS);
	}
	
	public String getMake(){
		return getString(MAKE);
	}
	public void setMake(String make){
		put(MAKE, make);
	}
	
	public String getModel(){
		return getString(MODEL);
	}
	public void setModel(String model){
		put(MODEL, model);
	}
	
	public Number getYear(){
		Number res = getNumber(YEAR);
		if (res==null)
			return 0;
		return res;
	}
	
	public void setStatus(String status){
		put(STATUS, status);
	}
	
	public void setYear(Number year){
		put(YEAR, year);
	}
	public void setYear(String strYear){
		put(YEAR, Integer.parseInt(strYear));
	}
	
	public String getTrackerId(){
		return getString(TRACKERID);
	}
	
	public String getLicense(){
		return getString(LICENSE);
	}
	
	public void setTrackerId(String trackerId){
		put(TRACKERID, trackerId);
	}
	public void setLicense(String license){
		put(LICENSE, license);
	}
	
	public void setChatRoomName(String chatroomName) {
		put(CHATROOM_NAME, chatroomName);
	}
	 
	public String getChatRoomName() {
		return getString(CHATROOM_NAME);
	}

	public ParseFile getPhoto(){
		return getParseFile(PHOTO);
	}
	
	public void setPhoto(ParseFile photo){
		put(PHOTO, photo);
	}
	
	//Preparing the photo data to be saved to Parse
	public void prepareSavingPhoto(Context contexct, ParseImageView mImageView){
		mImageView.buildDrawingCache();
		Bitmap bmap = mImageView.getDrawingCache();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		photoData = bos.toByteArray();
		
		setPhoto( new ParseFile("photo.png", photoData) );
	}
	
	
	public void loadPhotoIntoParseImageView(Context context, ParseImageView mImageView ) {
		myContext = context;
		// Try to load profile photo from internal storage first
		try {
			FileInputStream fis = context.openFileInput( PHOTOFILE_PREFIX + getObjectId() + PHOTOFILE_SUFFIX );
			Bitmap bmap = BitmapFactory.decodeStream(fis);
			mImageView.setImageBitmap(bmap);
			fis.close();
			return;
		} catch (IOException e) {
			// Default profile photo if no photo saved before.
			mImageView.setImageResource(R.drawable.avatar);
		}
		
		// Load from Parse if fail to load from storage
	    ParseFile photo = getPhoto();
	    
	    if (photo!=null){
	    	mImageView.setParseFile(photo);
	    	mImageView.loadInBackground( new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					//save to local disk
					photoData = data;
					savePhotoLocally(myContext);
				}
			});
	    }
	}
	
	public void savePhotoLocally(Context context) {
        ImageFragment.savePhotoLocally(
                context, 
                photoData, 
                PHOTOFILE_PREFIX, 
                getObjectId());
	}
	
}
