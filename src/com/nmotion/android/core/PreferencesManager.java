package com.nmotion.android.core;

import com.nmotion.android.models.User;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesManager {
        public static final String TERMS_ACCEPTED_KEY="terms";
        public static final String CRASH_KEY="crash";
	public static final String USER_EMAIL = "user_email";
	public static final String USER_PASSWORD = "user_password";
	public static final String USER_VIA_FACEBOOK = "user_via_facebook";
	public static final String LAST_USER_PASSWORD = "last_user_password";
	public static final String LAST_USER_LOGIN = "last_user_login";
	public static final String LANGUAGE = "language";
	public static final String DISTANCE = "distance";

	public static final String SALES_TAX = "sales_tax";
	public static final String DISCOUNT = "nmotion_discount";
	public static final String RESTAURANT_SEARCH_RADIUS = "restaurant_search_radius";

	public static final String IS_CHEKIN_FACEBOOK = "is_checkin_facebook";

	public static final String CHECKIN = "checkin.";
	public static final String CHECKIN_MODE = "checkin_mode";
	public static final int NO_CHECKIN_MODE = 0;
	public static final int IN_HOUSE_CHECKIN_MODE = 1;
	public static final int TAKE_AWAY_CHECKIN_MODE = 2;
	public static final int ROOM_SERVICE_CHECKIN_MODE = 3;
	//public static final String LAST_CHECKIN_IS_TAKEAWAY = "last_checkin_is_take_away";

	private SharedPreferences _preferences;

	public PreferencesManager(SharedPreferences preferences) {
		_preferences = preferences;
	}

	public void clear() {
		getPreferences().edit().clear().commit();
	}

	public float getFloat(String key, float defaultValue) {
		return getPreferences().getFloat(key, defaultValue);
	}

	public void setFloat(String key, float value) {
		Editor editor = getPreferences().edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public int getInt(String key, int defaultValue) {
		return getPreferences().getInt(key, defaultValue);
	}

	public void setInt(String key, int value) {
		Editor editor = getPreferences().edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public long getLong(String key, long defaultValue) {
		return getPreferences().getLong(key, defaultValue);
	}

	public void setLong(String key, long value) {
		Editor editor = getPreferences().edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	public boolean contains(String key){
	    return getPreferences().contains(key);
	}

	public String getString(String key, String defaultValue) {
		return getPreferences().getString(key, defaultValue);
	}

	public void setString(String key, String value) {
		Editor editor = getPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return getPreferences().getBoolean(key, defaultValue);
	}

	public void setBoolean(String key, boolean value) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void setCurrentUserData(String email, String pass, boolean isFacebookAuth) {
		Editor editor = getPreferences().edit();
		editor.putString(PreferencesManager.USER_EMAIL, email);
		editor.putString(PreferencesManager.USER_PASSWORD, pass);
		editor.putBoolean(PreferencesManager.USER_VIA_FACEBOOK, isFacebookAuth);

		editor.commit();
	}

	public SharedPreferences getPreferences() {
		return _preferences;
	}

	public boolean isCheckedIn(String eMail, int restaurantId) {
		return restaurantId == getInt(CHECKIN.concat(eMail), -1)
		        || restaurantId == getInt(CHECKIN.concat("unknown"), -1);
	}

	public boolean isUserAlreadyCheckedin(User currentUser) {
		return getInt(CHECKIN.concat(currentUser.eMail), -1) != -1
		      || getInt(CHECKIN.concat("unknown"), -1) != -1;
	}

	public void checkIn(String eMail, int restaurantId, /*boolean isRestaurantTakeAway, */int checkInMode) {
		setInt(CHECKIN.concat(eMail), restaurantId);
		setInt(CHECKIN_MODE, checkInMode);
		//setBoolean(LAST_CHECKIN_IS_TAKEAWAY, isRestaurantTakeAway);
	}

	public void checkOut(String eMail, int restaurantId) {
		setInt(CHECKIN.concat(eMail), -1);
		setInt(CHECKIN.concat("unknown"), -1);
		setInt(CHECKIN_MODE, NO_CHECKIN_MODE);
	}

	/*public boolean isLastCheckinedRestaurantTakeaway() {
		return getBoolean(LAST_CHECKIN_IS_TAKEAWAY, false);
	}*/
	
	public int getCheckInMode(){
	    return getInt(CHECKIN_MODE, NO_CHECKIN_MODE);
	}

	public void clearCheckout() {
		for (String key : getPreferences().getAll().keySet()) {
			if (key.contains(CHECKIN)) {
				getPreferences().edit().remove(key).commit();
				break;
			}
		}
		//getPreferences().edit().remove(LAST_CHECKIN_IS_TAKEAWAY).commit();
	}
	
	public boolean isCrash(){
	    return getBoolean(CRASH_KEY, false);
	}
	
	public void setCrashFlag(){
	    setBoolean(CRASH_KEY, true);
	}
	
	public void clearCrashFlag(){
	    getPreferences().edit().remove(CRASH_KEY).commit();
	}
}
