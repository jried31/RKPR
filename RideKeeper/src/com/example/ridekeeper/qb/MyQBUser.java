package com.example.ridekeeper.qb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.SessionListener;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
//import com.quickblox.module.chat.QBChat;

public class MyQBUser {
	private static final String TAG = MyQBUser.class.getSimpleName();

	private static Context myContext;
	private static QBUser sQbUser = null;

    private static QBChatRoom sCurrentRoom;

	
	//Password for all QBUser accounts
	public static final String DUMMY_PASSWORD = "abcde123";
	public static boolean sessionCreated = false;
	
	public static void signUpSignin(String username, String password) {
		sQbUser = new QBUser(username, password);

		QBUsers.signUpSignInTask(sQbUser,  new QBCallback() {
			@Override
			public void onComplete(Result result, Object context) {}
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					saveUserJabberIDtoCache(getUserJabberID());
				}
			}
		});
	}
	
	public static void signin(String username, String password){
		sQbUser = new QBUser(username, password);

		Log.d(TAG, "Attempting to sign in to Quickblox: " + username);
		QBUsers.signIn(sQbUser, new QBCallback() {
			@Override
			public void onComplete(Result result, Object context) {
			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
                    Log.i(TAG, "QBUsers signin successful");

                    QBChatService.getInstance().loginWithUser(sQbUser, new SessionListener() {
                        @Override
                        public void onLoginSuccess() {
                            Log.i(TAG, "QBChatService login success");
					
                            // TODO: may need this periodic presence in a service to ensure it works
                            // Test case: go to another screen (activity.stop) and then turn off screen for 5 min, then try to
                            // access chatrooms
                            // Expected: we want users to remain logged in to QB or auto sign in again
                            // QuickBlox Chat is a standard XMPP chat and you need to send presence periodically to remain available.
                            // Send presence every 60 seconds
                            QBChatService.getInstance().startAutoSendPresence(60);
                        }

                        @Override
                        public void onLoginError() {
                            Log.i(TAG, "error when login");
                        }

                        @Override
                        public void onDisconnect() {
                            Log.i(TAG, "disconnect when login");
                        }

                        @Override
                        public void onDisconnectOnError(Exception exc) {
                            Log.i(TAG, "disconnect error when login");
                        }
                    });

					saveUserJabberIDtoCache(getUserJabberID());
				}
			}
		});
	}

	public static String getLoginName(){
		if (sQbUser != null){
			return sQbUser.getLogin();	
		}else{
			return null;
		}
	}
	
	public static String getLoginPassword(){
		if (sQbUser != null){
			return sQbUser.getPassword();
		}else{
			return null;
		}
	}
	
	//Get the JabberId of the user for login to chat room
	public static String getUserJabberID(){
		if (sQbUser != null){
			//return QBChat.getChatLoginShort(user);	
			return QBChatUtils.getChatLoginShort(sQbUser);	
		}else{
			return null;
		}
	}
	
	public static String getUserJabberIDfromCache(){
		SharedPreferences pref = myContext.getSharedPreferences("QBUser", 0);
		return pref.getString("userjid", "");
	}
	
	public static void saveUserJabberIDtoCache(String JID){
		SharedPreferences pref = myContext.getSharedPreferences("QBUser", 0);
		pref.edit().putString("userjid", JID).commit();
	}
	
	public static QBUser getCurrentUser(){
		return sQbUser;
	}
	
	public static void initContext(Context context){
		myContext = context;
	}

    public static QBUser getQbUser() {
        return sQbUser;
    }

    public static void setQbUser(QBUser qbUser) {
        sQbUser = qbUser;
    }

    public static QBChatRoom getCurrentRoom() {
        return sCurrentRoom;
    }

    public static void setCurrentRoom(QBChatRoom room) {
        sCurrentRoom = room;
    }
}
