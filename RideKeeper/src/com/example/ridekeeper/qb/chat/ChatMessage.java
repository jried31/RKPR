package com.example.ridekeeper.qb.chat;

import java.util.Date;

public class ChatMessage {
    private boolean mIncoming;
    private boolean mIsImage;
    private String mText;
    private Date mTime;
    private String mSender;

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
     * Send image message
     */
    public ChatMessage() {
    	
    }

    /**
     * Send text message with sender
     * @param text
     * @param sender
     * @param time
     * @param incoming
     */
    public ChatMessage(String text, String sender, Date time, boolean incoming) {
        this.mText = text;
        this.mSender = sender;
        this.mTime = time;
        this.mIncoming = incoming;
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
    
    public boolean isImage(){
    	return mIsImage;
    }
}
