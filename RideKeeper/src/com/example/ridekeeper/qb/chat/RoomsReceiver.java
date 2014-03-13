package com.example.ridekeeper.qb.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.example.ridekeeper.MainActivity;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.RoomReceivingListener;

/**
 * Receives all public chatrooms from Quickblox, filtered by
 * the chatrooms returned by the stolen vehicles list
 */
public class RoomsReceiver implements RoomReceivingListener {
    private static final String TAG = RoomsReceiver.class.getSimpleName();

    private static final String KEY_ROOM_NAME = "roomName";
    private static final String APP_ID_PREFIX = MainActivity.APP_ID + "_";
    private static final String QB_CHAT_SERVER_POSTFIX = "@muc.chat.quickblox.com";

    private Map<String, QBChatRoom> mRooms;
    private Map<String, String> mVehiclesToRoomMap; 

    private boolean mRoomsReceived;
    private ProgressDialog mProgressDialog;
    private Activity mMainActivity;
    private boolean mProgressDialogCanceled;

    public RoomsReceiver(Activity mainActivity) {
    	mRooms = new HashMap<String, QBChatRoom>();
    	mRoomsReceived = false;
    	mMainActivity = mainActivity;
    	mProgressDialogCanceled = false;
    }
    
    public boolean isRoomsRetrieved() {
    	return mRoomsReceived;
    }

	public void showProgressDialog() {
    	mProgressDialogCanceled = false;
		mProgressDialog = ProgressDialog.show(
				mMainActivity, 
				null, 
				"Loading chatrooms", 
				true,
				true,
				new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						mProgressDialogCanceled = true;
						
					}
				});
		mProgressDialog.setCanceledOnTouchOutside(false);
	}

	public boolean progressDialogCanceled() {
		return mProgressDialogCanceled;
	}

    @Override
    public void onReceiveRooms(List<QBChatRoom> chatRooms) {
    	mRoomsReceived = true;

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

    	for (QBChatRoom chatRoom : chatRooms) {
    		Log.d(TAG, "onReceiveRooms: " + chatRoom.getName());

    		for (Map.Entry<String, String> entry : mVehiclesToRoomMap.entrySet()) {
    			String vehicleId = entry.getKey();
    			String roomName = entry.getValue();
    			String roomJid = APP_ID_PREFIX + roomName + QB_CHAT_SERVER_POSTFIX;

                if (chatRoom.getJid().equals(roomJid)) {
                    Log.d(TAG, "onReceiveRooms: vehicle/chatroom: " + vehicleId + "/" + roomName);
                    mRooms.put(roomName, chatRoom);
                }
    		}
    	}
    }

    public void loadRooms(Map<String, String> vehiclesToRoomMap) {
    	if (QBChatService.getInstance().isLoggedIn()) {
    		mRoomsReceived = false;
            QBChatService.getInstance().getRooms(this);
    	}
    	if (vehiclesToRoomMap != null) {
            mVehiclesToRoomMap = vehiclesToRoomMap;
    	}
    }
    
    public QBChatRoom getChatRoom(String roomName) {
    	QBChatRoom chatRoom = mRooms.get(roomName);

        if (chatRoom == null) {
            Log.d(TAG, roomName + " object is null");
            for (String room_name : mRooms.keySet()) {
            	Log.d(TAG, "available room: " + room_name);
            }
        }

    	return chatRoom;
    }

    public String getChatRoomName(String vehicleId){
    	return mVehiclesToRoomMap.get(vehicleId);
    }
}
