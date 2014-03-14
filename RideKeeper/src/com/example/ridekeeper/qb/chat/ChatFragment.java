package com.example.ridekeeper.qb.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.ridekeeper.MainActivity;
import com.example.ridekeeper.R;
import com.example.ridekeeper.qb.MyQBUser;
import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.qb.chat.RoomChat.NullChatRoomException;
import com.example.ridekeeper.util.ImageConsumer;
import com.example.ridekeeper.util.ImageFragment;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
//import android.view.ViewGroup.LayoutParams;
//Need this for enlarging photo

public class ChatFragment extends Fragment implements ImageConsumer {
	private static final String TAG = ChatFragment.class.getSimpleName();

	public static final String ARG_ROOM_NAME = "roomName";
	public static final String ARG_TITLE = "title";
	public static final String ARG_ROOM_ACTION = "action";

    // Set gravity to center in OnCreateView
	private static final LayoutParams IMAGE_SMALL_VIEW_LAYOUT = new LayoutParams(170, 170);

	// For UI
	private ImageButton mUploadPhotoBtn;
	private ImageButton mSendBtn;

    public static final String EXTRA_MODE = "mode";
    private EditText mMessageEditText;
    private Mode mMode = Mode.SINGLE;
    private Chat mChat;
    private ChatAdapter mAdapter;
    private ListView mMessagesContainer;
	
    public static enum Mode {SINGLE, GROUP}
	
	// For chat room 
	private String mTitle;
	
	private MainActivity mMainActivity;
	
	private ImageFragment mImageFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		mTitle = args.getString(ChatFragment.ARG_TITLE);
        mMode = (Mode) args.getSerializable(EXTRA_MODE);
		
        mMainActivity = (MainActivity)getActivity();
        mMainActivity.setTitle(mTitle);

    	mImageFragment = ImageFragment.newInstance(this, null,
    			getFragmentManager());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.chat_fragment, container, false);
		
		// initialize the gravity for small image
		IMAGE_SMALL_VIEW_LAYOUT.gravity = Gravity.CENTER;
		
		mMainActivity.setSelectedFrag(DBGlobals.SelectedFrag.CHAT_ROOM);
		mMainActivity.invalidateOptionsMenu();

        mMessagesContainer = (ListView) view.findViewById(R.id.messagesContainer);
        mImageFragment.setContainer(mMessagesContainer);

        mMessageEditText = (EditText) view.findViewById(R.id.messageEdit);
        mSendBtn = (ImageButton) view.findViewById(R.id.chatSendButton);
        TextView meLabel = (TextView) view.findViewById(R.id.meLabel);
        TextView companionLabel = (TextView) view.findViewById(R.id.companionLabel);
        RelativeLayout containerLayout = (RelativeLayout) view.findViewById(R.id.container);
		mUploadPhotoBtn = (ImageButton) view.findViewById(R.id.sendPicBtn);

        mAdapter = new ChatAdapter(mMainActivity, new ArrayList<ChatMessage>(),
        		mImageFragment.getToggleImageSizeListener(this),
        		mImageFragment.getSaveImageToGalleryListener());

        mMessagesContainer.setAdapter(mAdapter);

        switch (mMode) {
            case GROUP:
            	try {
                    mChat = new RoomChat(mMainActivity, this, getArguments());
            	} catch (NullChatRoomException ne) {
            		// TODO: If we can't get the corresponding QBChatRoom, then
            		// show error view
            		return inflater.inflate(R.layout.fragment_blank, container, false);
            	}

                containerLayout.removeView(meLabel);
                containerLayout.removeView(companionLabel);
                break;
            case SINGLE:
            	// If single chat ever needed, this may be helpful
                //chat = new SingleChat(this);
                //int userId = intent.getIntExtra(SingleChat.EXTRA_USER_ID, 0);
                //companionLabel.setText("user(id" + userId + ")");
                //restoreMessagesFromHistory(userId);
                break;
        }
		
		mUploadPhotoBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mImageFragment.showPhotoSelection();
			}
		});
		
		// Send message after user clicks Send button
		View.OnClickListener sendMsgListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	handleSendMsg();
            }
		};
		// Send message after user hits Done in keyboard
        mMessageEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	handleSendMsg();
                }
                
                // Return true so that the default action of hiding the keyboard
                // doesn't occur.
                return true;
            }
        });
        
        mSendBtn.setOnClickListener(sendMsgListener);

		Log.d(TAG, "jabberId: " + MyQBUser.getUserJabberIDfromCache());

        enableSendPic();
        Toast.makeText(mMainActivity, "Joined chat room", Toast.LENGTH_SHORT).show();
		
		return view;
	}
    public void handleSendMsg() {
        String lastMsg = mMessageEditText.getText().toString();
        if (TextUtils.isEmpty(lastMsg)) {
            return;
        }

        mMessageEditText.setText("");
        try {
            mChat.sendMessage(lastMsg);
        } catch (XMPPException e) {
            Log.e(TAG, "failed to send a message", e);
        }

        //if (mMode == Mode.SINGLE) {
        //    showMessage(new ChatMessage(lastMsg, Calendar.getInstance().getTime(), false));
        //}
    }

	public void processBitmap(Bitmap bitmap) {
        sendPhoto(bitmap);
	}

	/**
	 * If a new item has been added or updated, then
	 * refresh the adapter to display.
	 */
	public void refreshAdapterView() {
		// TODO: isn't keep track of user's position, after 
		// notifyDataSetChanged, the screen scrolls all the way down
		// by itself
        // save index and top position
        //int index = mMessagesContainer.getFirstVisiblePosition();
        //View v = mMessagesContainer.getChildAt(0);
        //int top = (v == null) ? 0 : v.getTop();

		mAdapter.notifyDataSetChanged();

        // restore
        //mMessagesContainer.setSelectionFromTop(index, top);
	}
    public void showMessage(ChatMessage message) {
        mAdapter.add(message);
        refreshAdapterView();
        // It automatically scrolls down anyway
        //scrollDown();
    }

    public void showMessage(List<ChatMessage> messages) {
        mAdapter.add(messages);
        refreshAdapterView();
        //scrollDown();
    }

    public void scrollDown() {
        mMessagesContainer.setSelection(mMessagesContainer.getCount() - 1);
    }
	
	@Override
	public void onDestroy() {
        try {
        	if (mChat != null) {
                mChat.release();
        	}
        } catch (XMPPException e) {
            Log.e(TAG, "failed to release chat", e);
        }

		super.onDestroy();
	}

	//how to process incoming message
	private PacketListener myPacketListener = new PacketListener() {
		@Override
		public void processPacket(Packet packet) {
			Message msg = (Message) packet;
			//convert "5111_room123@muc.chat.quickblox.com/romeo" to "romeo"
			final String from = StringUtils.parseResource(msg.getFrom());

			String body = msg.getBody();

			Log.d("DEBUG", "RECEIVED MESSAGE: " + from + ": " + body);
			
			//Check if message is a text message
			/*
			if ( !isSpecialString(body) ) {
				//pushTextToContainer( from + ": " + msg.getBody());
			
			} else if (isSpecialString(body)) { //special string received -> a photo message
				final String photoObjectId = extractFromSpecialString(body);
				final ParseImageView pivPhoto = new ParseImageView(mMainActivity);
				
				pivPhoto.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
				pivPhoto.setDrawingCacheEnabled(true);
				pivPhoto.setAdjustViewBounds(true);
				pivPhoto.setLayoutParams(ChatFragment.IMAGE_SMALL_VIEW_LAYOUT);
				
				// JERRID: Left Here for Background Bubbles
				//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	            //        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);				
				//params.gravity = Gravity.LEFT;
				
	        	//int bgRes = R.drawable.left_message_bg;
				//pivPhoto.setLayoutParams(params);
				//pivPhoto.setBackgroundResource(bgRes);
				
				pivPhoto.setOnClickListener(toggleImageSize);
				pivPhoto.setOnLongClickListener(saveImageToGallery);
				
				
				//pushTextToContainer( from + " posted a photo:");
				pushPhotoToContainer(pivPhoto);

				ParseFunctions.queryForChatPhoto(photoObjectId, new GetCallback<ParseChatRoomPhoto>() {

					@Override
					public void done(ParseChatRoomPhoto chatPhoto, ParseException e) {
						if (e == null){
							chatPhoto.loadPhotoIntoParseImageView(mMainActivity, pivPhoto);
						}else{
							Toast.makeText(mMainActivity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
			*/
		}
	};
	
	private void sendPhoto(Bitmap bitmap){
		// Upload image to Quickblox
		Toast.makeText(mMainActivity, "Sending photo...", Toast.LENGTH_SHORT).show();
		disableSendPic();
		
		// Save on temporarily on device, then delete after upload to Quickblox
        File imageFile = ImageFragment.createAlbumImageFile();

        if (ImageFragment.storeBitmap(bitmap, imageFile)) {
        	uploadImageToQuickBlox(imageFile);
            
            // Show the sent image right away instead of waiting to receive
            // it from quickblox
            Date time = Calendar.getInstance().getTime();
            showMessage(new ChatMessage("", RoomChat.MSG_USER_NAME, time, false, bitmap));
		            
        } else {
        	displaySendImageFailure();
        }

        enableSendPic();
	}

	private void uploadImageToQuickBlox(final File image) {
		// Upload file to Content module
		QBContent.uploadFileTask(image, false, new QBCallbackImpl() {
		    @Override
		    public void onComplete(Result result) {
		        if (result.isSuccess()) {
		            // get uploaded file ID
		            QBFileUploadTaskResult res = (QBFileUploadTaskResult) result;
		            String uid = res.getFile().getUid();
		 
		            // TODO: do we need to save locally?
		            //savePhotoLocally(mMainActivity);

		            try {
                        mChat.sendImageString(uid);
		            } catch (XMPPException xe){
                        Log.e(TAG, "failed to send a special image message", xe);
		            }
		            
		            // TODO: should we delete the temporary image file?
                    if (image.exists()) {
                        Log.wtf(TAG, "Deleting file: " + image.getAbsolutePath());
                        image.delete();
                    }

		        } else {
		        	displaySendImageFailure();
		        }
		    }
		});
	}
	
	private void displaySendImageFailure() {
        Toast.makeText(mMainActivity, "Failed to send image", Toast.LENGTH_SHORT).show();
	}
	
	private void disableSendPic(){
		//mUploadPhotoBtn.setImageResource(R.drawable.camera_grey);
		mUploadPhotoBtn.getBackground().setColorFilter(
				Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);
		mUploadPhotoBtn.setEnabled(false);
	}
	
	private void enableSendPic(){
		mUploadPhotoBtn.setImageResource(R.drawable.ic_action_camera);
		mUploadPhotoBtn.setEnabled(true);
	}
}
