package com.example.ridekeeper;

public class DBGlobals {
	public static final String APP_NAME = "RideKeeper";

	public static enum SelectedFrag {
		STOLEN_VEHICLE, MY_PROFILE, MY_VEHICLES, SETTINGS, MY_RIDE, CHAT_ROOM
	}
	 
	 //Trigonometry constants
	 public static final double PI=3.14159265359;
	 public static final double DEGREES_TO_RADIANS=180/PI;
	 public static final double RADIANS_TO_DEGREES=PI/180;
	 //*****************
	 
	 //Database Constants
	public static final String DB_SCHEMA_NAME = "RIDEKEEPER";

	// Drawer item positions
	public static final int DRAWER_IDX_VEHICLES = 0;
	public static final int DRAWER_IDX_PROFILE = 1;
	public static final int DRAWER_IDX_SETTINGS = 2;

	// Tabbed item positions
	public static final int TAB_IDX_STOLEN_VEHICLES = 0;
	public static final int TAB_IDX_MY_VEHICLES = 1;
	public static final int TAB_IDX_MY_RIDES = 2;
	
	//Alert Levels
	public static final String ALERT_LEVEL_PARKED="PARKED",
			ALERT_LEVEL_MOVED="MOVED",
			ALERT_LEVEL_MOVED_LOCATION="MOVED_LOC",
			ALERT_LEVEL_STOLEN="STOLEN",
			ALERT_LEVEL_STOLEN_LOC="STOLEN_LOC",
			ALERT_LEVEL_RIDING="RIDING",
			ALERT_LEVEL_CRASHED="CRASHED",
			ALERT_LEVEL_NEARBY="NEARBY",
			ALERT_LEVEL_RECOVERED="RVD",
			ALERT_LEVEL_TILT="TILT";
	
	//Map Constants
	public static final double searchRadius = 10; //radius to scan for VBS (in miles)
	public static final int vehiclePosUpdateInGMapRate = 2000; //update rate for VBS position on the map (in ms);
	//public static final int repeatingAlarmRate = 1000 * 60 * 5; //wake up phone and run some tasks every X ms
    public static final int MILLISECONDS_PER_SECOND = 1000; // Milliseconds per second
    public static final int SECONDS_PER_MIN = 60;
	public static final int LOCATION_UPDATE_RATE = MILLISECONDS_PER_SECOND * SECONDS_PER_MIN * 1; //wake up phone and run some tasks every X ms
	//public static final int repeatingAlarmRate = MILLISECONDS_PER_SECOND * 60 * 5; //wake up phone and run some tasks every X ms
	public static final double RADIOUS_OF_EARTH = 6378137.0;   //  WGS-84 ellipsoid parameters
	public static final double MILE_TO_METER = 1609.34; // 1Mile = 1609.34meters
	public final static int  CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 3;// Update frequency in seconds
    public static final long UPDATE_INTERVAL =  MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;  // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL_STOLEN_VEHICLE = MILLISECONDS_PER_SECOND * 8;
    public static final int FASTEST_INTERVAL_IN_SECONDS = 2;  // The fastest update frequency, in seconds
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	//****************
    public static final long LOAD_CHATROOM_DELAY = MILLISECONDS_PER_SECOND * 1;
	
	public static final String PARSE_VEHICLE_TBL = "Vehicle";
	public static final String PARSE_VEHICLE_LOCATION_TBL = "VehicleLocationHistory";
	
	public static final String PARSE_CHATROOM_TBL = "Chatroom";
	public static final String PARSE_CHATROOMPHOTO_TBL = "ChatRoomPhoto";
	public static final String PARSE_INSTL_OWERID = "ownerId";
	
	public static final String PARSE_VEHICLE_TBL_KEY_OWNER_ID = "ownerId";
	public static final String PARSE_VEHICLE_TBL_KEY_VEHICLE_ID = "objectId";
	
    public static final String PARSE_CHAT_TBL_KEY_VEHICLE_ID = "vehicleId";
    public static final String PARSE_CHAT_TBL_KEY_ROOM_NAME = "roomName";
    public static final String PARSE_CHAT_TBL_KEY_MEMBERS = "members";
	
    public static final String UNICODE_BULLET = "\u25CF";

    public static final int RIDE_NOTIFICATION_ID = 1;

	public static final int FIND_MY_VEHICLE_MAP_REFRESH_RATE = 60000;

	public static final long STOLEN_VEHICLE_MAP_ERROR_REFRESH_RATE = 15000;

    public static final String PARSE_RIDE_TBL = "Track";
	
}
