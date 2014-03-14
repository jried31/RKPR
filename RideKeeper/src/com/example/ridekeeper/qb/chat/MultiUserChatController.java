package com.example.ridekeeper.qb.chat;

import java.io.File;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.module.chat.utils.QBChatUtils;

// Used as a callback after making connection to chat server
interface AfterConnectCallback{
	void done(String errorMsg);
}

public class MultiUserChatController {
	private static final String TAG = MultiUserChatController.class.getSimpleName();
	public static final String ROOM_SUBFIX = "@muc.chat.quickblox.com";

	private static Connection sConnection = null;
	
	private String mRoomname; // should be in the form of <app_id>_name
	private String mUserJID;  // should be in the form of '17744-1028' (<qb_user_id>-<qb_app_id>)
	private String mPassword;
	private String mNickname;  // the nickname used in the chat room
	private Context mMyContext;
	
	public MultiUserChat muc = null;

	public MultiUserChatController(
			Context context,
			String roomname, 
			String userJID,
			String password, 
			String nickname) {

		mMyContext = context;
		this.mRoomname = roomname;
		this.mUserJID = userJID;  
		this.mPassword = password;
		this.mNickname = nickname;
	}
	
	private class ChatServerConnector extends AsyncTask<AfterConnectCallback, Void, Object>{
		AfterConnectCallback callback;
		
		@Override
		protected Object doInBackground(AfterConnectCallback... params) {
			callback = params[0];
			ConnectionConfiguration config = new ConnectionConfiguration(QBChatUtils.getChatServerDomain()); //***Must run in a thread***
			sConnection = new XMPPConnection(config);
			Connection.DEBUG_ENABLED = true;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			    config.setTruststoreType("AndroidCAStore");
			    config.setTruststorePassword(null);
			    config.setTruststorePath(null);
			} else {
			    config.setTruststoreType("BKS");
			    String path = System.getProperty("javax.net.ssl.trustStore");
			    if (path == null)
			        path = System.getProperty("java.home") + File.separator + "etc"
			            + File.separator + "security" + File.separator
			            + "cacerts.bks";
			    config.setTruststorePath(path);
			}
			
			try {
				//setup connection
	        	sConnection.connect();
	        	Log.d(TAG, "Connecting to chat server");
	        	sConnection.login(mUserJID, mPassword);
	        	//connection.loginAnonymously();
	        	
				return null;
			} catch (XMPPException e) {
				Log.d("CHATROOM", "Failed to connect/join chat server. Error: " + e.getMessage());
				return e.getMessage();
			}
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (result == null){ //successfully connected
				callback.done(null);
			}else{
				callback.done(result.toString());
			}
		}
	}
	
	public void connect(final AfterConnectCallback afterConnectCallback){
		if ( (sConnection == null) || (!sConnection.isConnected())){
			//start the thread to connect to chat server
			new ChatServerConnector().execute(afterConnectCallback);
		}else{
			afterConnectCallback.done(null);
		}
	}
	
	public void disconnect(){
		if (sConnection!=null){
			sConnection.disconnect();
		}
	}
	
	public void join() throws XMPPException{
		muc = new MultiUserChat(sConnection, mRoomname + ROOM_SUBFIX);
		muc.join(mNickname);
	}
	
	public void leaveRoom(){
		if (muc != null){
			muc.leave();
		}
	}
	
	public void sendMessage(String msgString){
		if ( (muc!=null) && (muc.isJoined())){
			try {
				muc.sendMessage(msgString);
				Toast.makeText(mMyContext, "Message sent", Toast.LENGTH_SHORT).show();
			} catch (XMPPException e) {
				Toast.makeText(mMyContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/*
	public void sendMessage(String msgString, Message.Type type){
		if ( (muc!=null) && (muc.isJoined())){
			try {
				Message msg = muc.createMessage();
				msg.setType(type);
				msg.setBody(msgString);
				muc.sendMessage(msg);
				Toast.makeText(myContext, "Message sent", Toast.LENGTH_SHORT).show();
			} catch (XMPPException e) {
				Toast.makeText(myContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	*/

	public void addMsgListener(PacketListener pl){
		muc.addMessageListener(pl);
	}
	
	public void removeMsgListener(PacketListener pl){
		muc.removeMessageListener(pl);
	}
}
