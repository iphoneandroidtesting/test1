package com.nmotion.android.utils;

public class Config {
	public static final String LOG_TAG = "Nmotion";
	public static final String TAG = "Nmotion";
	public static final boolean DEBUG_MODE = false;
	public static final int SERVER_INDEX = 2; //0 - stage; 1 - demo; 2 - prod
	/////demo
	public static final boolean LOG_HTTP = true;
	public static final boolean LOG_SDCARD = true;
	public static final int CONNECTION_TIMEOUT = 40000;
	public static final float MAX_ACCURACY_REST = 150.0f;
	public static final float DISTANCE = 20.0f;
	public static final float MAX_ACCURACY = 150.0f;
	public static final boolean getDataFromLocal = false;
	public static final int LIST_VIEW_DIVIDER_HEIGHT = 5;
	public static final int PICKUP_MAX_RADIUS_MILES = 4;
	public static final int VAULES_NO_UPDATE_TIME_LIMIT = 300000;
	public static final int RESTAURANTS_NO_UPDATE_TIME_LIMIT = 0;
	public static final int GPS_NO_UPDATE_TIME_LIMIT = 0;
	public static final String USER_AUTOLOGIN_PREFERENCES_NAME = "user";
	public static final boolean ACTIVATE_DEAD_SESSION_LOGOUT = true;
	public static final boolean SAVE_LOGIN = true;
	public static final int SEARCH_SYMBOLS_LENGTH = 3;
	
	// prod
    public static final String APP_ID = SERVER_INDEX==2 ? "132227170295952" : (SERVER_INDEX==1 ? "330287150421549" :"124395924385734");
    public static final String SERVICE_URI_API_PART = "/api/v2/";
    public static final String SERVICE_URL = SERVER_INDEX==2 ? "http://nmotion.dk" : "http://nmotion.dk"; //"http://79.143.37.62:8088";
    public static final String SERVICE_URI = SERVICE_URL+SERVICE_URI_API_PART;
    public static final String ACCEPT_URL_CALLBACK = SERVICE_URL+"/paymentconfirmation/accepted";
    public static final String CANCEL_URL_CALLBACK = SERVICE_URL+"/paymentconfirmation/cancelled";
    public static final String URL_CALLBACK = SERVICE_URL+"/paymentconfirmation/";

	public static final boolean USE_FAKE_LOCATION = false;
	public static final double FAKE_LAT = 50.425243;
	public static final double FAKE_LON = 30.504566;
}
