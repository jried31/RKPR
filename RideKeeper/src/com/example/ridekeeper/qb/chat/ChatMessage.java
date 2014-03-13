package com.example.ridekeeper.qb.chat;

import java.util.Date;

import android.graphics.Bitmap;

public class ChatMessage {
    private String mText;
    private String mSender;
    private Date mTime;
    private boolean mIncoming;

    private Bitmap mBitmap;

    /**
     * Send text message
     * @param text
     * @param time
     * @param incoming
     */
    public ChatMessage(String text, Date time, boolean incoming) {
        this(text, null, time, incoming);
    }

    /**
     * Send text message with sender
     * @param text
     * @param sender
     * @param time
     * @param incoming
     */
    public ChatMessage(String text, String sender, Date time, boolean incoming) {
    	this(text, sender, time, incoming, null);

    }
    
    public ChatMessage(String text, String sender, Date time,
    		boolean incoming, Bitmap bitmap) {
        this.mText = text;
        this.mSender = sender;
        this.mTime = time;
        this.mIncoming = incoming;
        this.mBitmap = bitmap;
    }


    public boolean isIncoming() {
        return mIncoming;
    }

    public String getText() {
        return mText;
    }

    public Date getTime() {
        return mTime;
    }

    public String getSender() {
        return mSender;
    }
    
    public boolean isImage() {
    	return mBitmap != null;
    }
    
    public Bitmap getBitmap() {
    	return mBitmap;
    }
}
