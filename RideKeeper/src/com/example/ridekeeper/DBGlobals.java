package com.example.ridekeeper;

 class DBGlobals {
    public static final String DB_SCHEMA_NAME = "RIDEKEEPER";
	public static final int 
					VBS_LIST=0,
					MAP=1,
					CHAT_ROOM=2,
					MY_PROFILE=3,
					MY_VEHICLE=4,
					SETTINGS=5;
	
	public static final double searchRadius = 10; //radius to scan for VBS (in miles)
	public static final int vbsPosMapUpdateRate = 2000; //update rate for VBS position on the map (in ms);
}