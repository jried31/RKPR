package com.example.ridekeeper.qb.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridekeeper.ParseChatRoomPhoto;
import com.example.ridekeeper.ParseFunctions;
import com.example.ridekeeper.R;
import com.example.ridekeeper.StolenVehicleListFragment;
import com.example.ridekeeper.qb.MyQBUser;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;
//import android.view.ViewGroup.LayoutParams;
//Need this for enlarging photo

public class ChatFragmentOld extends DialogFragment {
	private static final String TAG = ChatFragmentOld.class.getSimpleName();

	public static final String ARG_ROOM_NAME = "roomName";
	public static final String ARG_VEHICLE_ID = "vehicleId";
	public static final String ARG_TITLE = "title";

    // set gravity to center in OnCreateView
	private static final LayoutParams IMAGE_SMALL_VIEW_LAYOUT = new LayoutParams(170, 170);

	// For taking picture:
	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 100;
	public static final int REQUEST_CODE_CROP_PHOTO = 101;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 102;

	private static final String IMAGE_UNSPECIFIED = "image/*";

	//Prefix denotes that an image is part of message
	private static final String SPECIAL_STRING_PREFIX = "&&$*(";

	// For UI
	private ImageView mUploadPhotoBtn;
	private EditText mMessageField;
	private Button mSendMessageBtn;
	private ScrollView mScrollContainer;
	private LinearLayout mMsgContainer;
	
	// For chat room 
	private String mTitle;
	private String mRoomName;
	private String mVehicleId; //vehicle onCreateView(Vehicle's objectId in Parse)
	private MultiUserChatController mMucController;
	
	private Uri mImageCaptureUri;
	private boolean mIsTakenFromCamera;
	// End for taking picture
	
	//For saving image to gallery:
	private File mAlbumDir;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoomName = getArguments().getString(ChatFragmentOld.ARG_ROOM_NAME);
		mVehicleId = getArguments().getString(ChatFragmentOld.ARG_VEHICLE_ID);
		
		mTitle = getArguments().getString(ChatFragmentOld.ARG_TITLE);
		
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chat_fragment_old, container, false);
		
		getDialog().setTitle(mTitle); //set the title of the Chat dialog fragment

		mUploadPhotoBtn = (ImageView) view.findViewById(R.id.imageView_sendPic);
		mMessageField = (EditText) view.findViewById(R.id.editText_msg);
		mSendMessageBtn = (Button) view.findViewById(R.id.buttton_sendMsg);
		
		mScrollContainer = (ScrollView) view.findViewById(R.id.scrollContainer);
		mMsgContainer = (LinearLayout) view.findViewById(R.id.messageContainer);
		
		mUploadPhotoBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPhotoSelection();
			}
		});
		
		mSendMessageBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendText();
			}
		});
		
		IMAGE_SMALL_VIEW_LAYOUT.gravity = Gravity.CENTER;
		
		
		// disable buttons while server is connecting
		disableSendPic();
		mSendMessageBtn.setEnabled(false);
		mSendMessageBtn.setText("Connecting...");

		mMucController = new MultiUserChatController(
				getActivity(),
                mRoomName,
                MyQBUser.getUserJabberIDfromCache(),
                MyQBUser.DUMMY_PASSWORD,
                ParseUser.getCurrentUser().getUsername()); // use parse username as chat room nickname
		
		Log.d(TAG, "jabberId: " + MyQBUser.getUserJabberIDfromCache());

		mMucController.connect( new AfterConnectCallback() {
			@Override
			public void done(String errorMsg) {
				if (errorMsg == null){
					try {
						mMucController.join();
					} catch (XMPPException e) {
						mSendMessageBtn.setText("No Connection");
						Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
						//e.printStackTrace();
						return;
					}

					mMucController.addMsgListener(myPacketListener);
					
					enableSendPic();
					mSendMessageBtn.setEnabled(true);
					mSendMessageBtn.setText("Send");
					Toast.makeText(getActivity(), "Joined chat room", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getActivity(), "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
					mSendMessageBtn.setText("Send");
				}
			}
		});
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		mMucController.removeMsgListener(myPacketListener);
		mMucController.leaveRoom();
		super.onDestroy();
	}
	
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
				Bitmap bitmap = (Bitmap) extras.getParcelable("data");
				SendPhoto(bitmap);
			}

			// Delete temporary image taken by camera after crop.
			if (mIsTakenFromCamera) {
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists())
					f.delete();
			}

			break;
		}
	}
	
	//how to process incoming message
	private PacketListener myPacketListener = new PacketListener() {
		@Override
		public void processPacket(Packet packet) {
			Message msg = (Message) packet;
			//convert "5111_room123@muc.chat.quickblox.com/romeo" to "romeo"
			final String from = StringUtils.parseResource(msg.getFrom());
			String body = msg.getBody();
			Log.d("DEBUG", "RECEIVED MESSAGE: " + from + ": " + msg.getBody());
			
			//Check if message is a text message
			if ( !isSpecialString(body) ){
				pushTextToContainer( from + ": " + msg.getBody());
			
			}else if (isSpecialString(body)){ //special string received -> a photo message
				final String photoObjectId = extractFromSpecialString(body);
				final ParseImageView pivPhoto = new ParseImageView(getActivity());
				
				pivPhoto.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
				pivPhoto.setDrawingCacheEnabled(true);
				pivPhoto.setAdjustViewBounds(true);
				pivPhoto.setLayoutParams(ChatFragmentOld.IMAGE_SMALL_VIEW_LAYOUT);
				
				/*JERRID: Left Here for Background Bubbles
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);				
				params.gravity = Gravity.LEFT;
				
	        	int bgRes = R.drawable.left_message_bg;
				pivPhoto.setLayoutParams(params);
				pivPhoto.setBackgroundResource(bgRes);
				*/
				
				pivPhoto.setOnClickListener(toggleImageSize);
				pivPhoto.setOnLongClickListener(saveImageToGallery);
				
				
				pushTextToContainer( from + " posted a photo:");
				pushPhotoToContainer(pivPhoto);

				ParseFunctions.queryForChatPhoto(photoObjectId, new GetCallback<ParseChatRoomPhoto>() {
					@Override
					public void done(ParseChatRoomPhoto chatPhoto, ParseException e) {
						if (e == null){
							chatPhoto.loadPhotoIntoParseImageView(getActivity(), pivPhoto);
						}else{
							Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}
	};
	
	private void sendText(){
		String tmp = mMessageField.getText().toString();

		if (!tmp.isEmpty()){
			mMucController.sendMessage(tmp);
			mMessageField.setText("");
		}
		
	}
	
	private boolean isSpecialString(String str){
		return str.startsWith(SPECIAL_STRING_PREFIX);
	}
	
	private void sendSpecialString(String str){
		mMucController.sendMessage( SPECIAL_STRING_PREFIX + str);
	}
	
	private String extractFromSpecialString(String special){
		return special.substring(SPECIAL_STRING_PREFIX.length());
	}
	
	private void SendPhoto(Bitmap bitmap){
		//upload image to Parse
		Toast.makeText(getActivity(), "Sending photo...", Toast.LENGTH_SHORT).show();
		disableSendPic();
		
		final ParseChatRoomPhoto chatPhoto = new ParseChatRoomPhoto();
		chatPhoto.setVehicleId(mVehicleId);
		chatPhoto.prepareSavingPhoto(getActivity(), bitmap);
		
		chatPhoto.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null){ //successfully upload the photo to Parse
					chatPhoto.savePhotoLocally(getActivity()); //also save the photo locally
					
					//send the Parse photo objectId string to the chat room
					sendSpecialString( chatPhoto.getObjectId() );
					//mucController.sendMessage(chatPhoto.getObjectId());
				}else{
					Toast.makeText(getActivity(), "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				
				enableSendPic();
			}
		});
	}
	
	//Put a text into the chat window
	private void pushTextToContainer(String msg){
		final TextView textView = new TextView(getActivity());
		textView.setText(msg);
		textView.setTextSize(20);
		textView.setPadding(5, 0, 5, 10);
		
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMsgContainer.addView(textView);
                // Scroll to bottom
                mScrollContainer.post( new Runnable() {
					@Override
					public void run() {
						mScrollContainer.fullScroll(View.FOCUS_DOWN);
					}
				});
            }
        });
	}
	
	//Put a text into the chat window
	private void pushPhotoToContainer(final ParseImageView pivPhoto){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMsgContainer.addView(pivPhoto);
                // Scroll to bottom
                mScrollContainer.post( new Runnable() {
					@Override
					public void run() {
						mScrollContainer.fullScroll(View.FOCUS_DOWN);
					}
				});
            }
        });
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
	
	//For taking picture from camera / gallery:
	private void onPhotoPickerItemSelected(int item) {
		Intent intent;
		mIsTakenFromCamera = false;

		switch(item){
		case ID_PHOTO_PICKER_FROM_CAMERA:
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
			mIsTakenFromCamera = true;
			break;

		case ID_PHOTO_PICKER_FROM_GALLERY:
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
			mIsTakenFromCamera = false;
			break;

		default:
			return;
		}
	}
	
	private void showPhotoSelection(){
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
		builder.create().show();
	}
	
	private void disableSendPic(){
		mUploadPhotoBtn.setImageResource(R.drawable.camera_grey);
		mUploadPhotoBtn.setEnabled(false);
	}
	
	private void enableSendPic(){
		mUploadPhotoBtn.setImageResource(R.drawable.camera);
		mUploadPhotoBtn.setEnabled(true);
	}
	

	
	private View.OnClickListener toggleImageSize = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageView iv = (ImageView) v;
			
			if (iv.getLayoutParams() == IMAGE_SMALL_VIEW_LAYOUT){
				int h = iv.getHeight() * (mMsgContainer.getWidth() / iv.getWidth());
				iv.setLayoutParams(new LayoutParams(mMsgContainer.getWidth(), h ));
			}else{
				iv.setLayoutParams(IMAGE_SMALL_VIEW_LAYOUT);
			}
			
		}
	};
	
	private View.OnLongClickListener saveImageToGallery = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			ImageView iv = (ImageView) v;
			Bitmap bm = iv.getDrawingCache(true);
			
			// Create image file
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "IMG" + timeStamp + "_";
			if (mAlbumDir==null){
				mAlbumDir = getAlbumDir();
			}
			File imageF = new File(mAlbumDir + "/" + imageFileName + ".png");

			try 
	        {
				imageF.createNewFile();
	            FileOutputStream ostream = new FileOutputStream(imageF);
	            bm.compress(CompressFormat.PNG, 100, ostream);
	            ostream.close();
	        } 
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	        }
			
			//Add file to gallery
			Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			Uri contentUri = Uri.fromFile(imageF);
	        mediaScanIntent.setData(contentUri);
	        getActivity().sendBroadcast(mediaScanIntent);
			
			Toast.makeText(getActivity(), "Saved to gallery", Toast.LENGTH_SHORT).show();
			return true;
		}
	};
	
    private File getAlbumDir() {
    	File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "RideKeeper");
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("RideKeeper", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }
	
}
