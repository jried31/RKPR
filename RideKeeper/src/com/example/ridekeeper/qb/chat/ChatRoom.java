package com.example.ridekeeper.qb.chat;

import java.util.Calendar;
import java.util.Date;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.ridekeeper.App;
import com.example.ridekeeper.qb.MyQBUser;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.users.model.QBUser;

public class ChatRoom implements Chat, RoomListener, ChatMessageListener {
    private static final String TAG = ChatRoom.class.getSimpleName();
    private Activity mMainActivity;
    private QBChatRoom mChatRoom;
    private ChatFragment mChatFragment;

    public static class NullChatRoomException extends Exception {};

    public ChatRoom(Activity mainActivity, ChatFragment chatFragment, Bundle args)
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
        }
    }

    @Override
    public void release() throws XMPPException {
        if (mChatRoom != null) {
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
        // Show message
        String sender = QBChatUtils.parseRoomOccupant(message.getFrom());
        QBUser qbUser = MyQBUser.getQbUser();
        if (sender.equals(qbUser.getFullName()) || sender.equals(qbUser.getId().toString())) {
            mChatFragment.showMessage(new ChatMessage(message.getBody(), "me", time, false));
        } else {
            mChatFragment.showMessage(new ChatMessage(message.getBody(), sender, time, true));
        }
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
