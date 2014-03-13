package com.example.ridekeeper.qb.chat;

import org.jivesoftware.smack.XMPPException;

public interface Chat {
    void sendMessage(String message) throws XMPPException;

    void release() throws XMPPException;
}
