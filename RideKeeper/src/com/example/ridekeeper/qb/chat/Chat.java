package com.example.ridekeeper.qb.chat;

import org.jivesoftware.smack.XMPPException;

public interface Chat {
    void sendMessage(String message) throws XMPPException;
	public void sendImageString(String uid) throws XMPPException;

    void release() throws XMPPException;
}
