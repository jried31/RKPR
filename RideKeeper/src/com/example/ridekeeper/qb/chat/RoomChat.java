package com.example.ridekeeper.qb.chat;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.example.ridekeeper.qb.MyQBUser;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.users.model.QBUser;

public class RoomChat implements Chat, RoomListener, ChatMessageListener {
    private static final String TAG = RoomChat.class.getSimpleName();
    
    public static final String MSG_USER_NAME = "me";
    public static final String MSG_LOADING_IMAGE = "Loading image...";

	// Prefix denotes that the message should be treated as a Quickblox image
	private static final String IMAGE_STRING_PREFIX = "&&$*(";

    public static class NullChatRoomException extends Exception {};
    
    private Activity mMainActivity;
    private QBChatRoom mChatRoom;
    private ChatFragment mChatFragment;

    public RoomChat(Activity mainActivity, ChatFragment chatFragment, Bundle args)
    		throws NullChatRoomException {
        this.mMainActivity = mainActivity;
        this.mChatFragment = chatFragment;

        String chatRoomName = args.getString(ChatFragment.ARG_ROOM_NAME);
        RoomAction action = (RoomAction) args.getSerializable(ChatFragment.ARG_ROOM_ACTION);

        switch (action) {
            case CREATE:
                create(chatRoomName);
                break;
            case JOIN:
            	QBChatRoom chatRoom = MyQBUser.getCurrentRoom();
            	if (chatRoom == null) {
            		throw new NullChatRoomException();
            	} else {
                    join(chatRoom);
            	}
                break;
        }
    }

    @Override
    public void sendMessage(String message) throws XMPPException {
        if (mChatRoom != null) {
            mChatRoom.sendMessage(message);
        } else {
            Toast.makeText(mMainActivity, "Join unsuccessful", Toast.LENGTH_LONG).show();
        };
    }

    @Override
    public void release() throws XMPPException {
        if (mChatRoom != null && QBChatService.getInstance().isLoggedIn()) {
            QBChatService.getInstance().leaveRoom(mChatRoom);
            mChatRoom.removeMessageListener(this);
        }
    }

    @Override
    public void onCreatedRoom(QBChatRoom room) {
        Log.d(TAG, "room was created");
        mChatRoom = room;
        mChatRoom.addMessageListener(this);
    }

    @Override
    public void onJoinedRoom(QBChatRoom room) {
        Log.d(TAG, "joined to room");
        mChatRoom = room;
        mChatRoom.addMessageListener(this);
    }

    @Override
    public void onError(String msg) {
        Log.d(TAG, "error joining to room");
    }

    @Override
    public void processMessage(Message message) {
        Date time = QBChatUtils.parseTime(message);
        if (time == null) {
            time = Calendar.getInstance().getTime();
        }
        final Date finalTime = time;

        // Show message
        String sender = QBChatUtils.parseRoomOccupant(message.getFrom());
        QBUser qbUser = MyQBUser.getQbUser();

        boolean isMessageFromSelf = sender.equals(qbUser.getFullName()) || 
        		sender.equals(qbUser.getId().toString());
        final boolean isIncoming = !isMessageFromSelf;

        final String messageUser = isMessageFromSelf ? MSG_USER_NAME : sender;

        final String body = Html.fromHtml(message.getBody()).toString();

        Log.i(TAG, "Received msg from: " + sender + ": " + body);

        boolean hasImagePrefix = isImageString(body);

        if (hasImagePrefix) { 
            String uid = extractUidFromImageString(body);
            // download file by ID    
            
            // Show the message with loading message placeholder to retain ordering
            // instead of waiting for download from QB
            final ChatMessage chatMessage = 
            		new ChatMessage(MSG_LOADING_IMAGE, messageUser, finalTime, isIncoming);
            mChatFragment.showMessage(chatMessage);

            QBContent.downloadFile(uid, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                	if (result.isSuccess()) { 
                        // extract image
                        QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                        InputStream s = downloadResult.getContentStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(s);

                        chatMessage.setBitmap(bitmap);
                	} else {
                		Log.d(TAG, "special image prefix not an actual QB image");
                		// Display the original special string message
                		chatMessage.setText(body);
                	}
                }
            });
        
        } else {
            mChatFragment.showMessage(new ChatMessage(body, messageUser, time, isIncoming));
        }
    }

	private boolean isImageString(String str){
		return str.startsWith(IMAGE_STRING_PREFIX);
	}
	
	public void sendImageString(String uid) throws XMPPException {
        this.sendMessage(IMAGE_STRING_PREFIX + uid);
	}
	
	private String extractUidFromImageString(String imageStr) {
		return imageStr.substring(IMAGE_STRING_PREFIX.length());
	}
	

    @Override
    public boolean accept(Message.Type messageType) {
        switch (messageType) {
            case groupchat:
                return true;
            default:
                return false;
        }
    }

    public void create(String roomName) {
        // Creates open & persistent room
        QBChatService.getInstance().createRoom(roomName, false, true, this);
    }

    public void join(QBChatRoom room) {
        QBChatService.getInstance().joinRoom(room, this);
    }

    public static enum RoomAction {CREATE, JOIN}
}
