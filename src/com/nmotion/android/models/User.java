package com.nmotion.android.models;

import java.io.Serializable;

import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public long id;
	public String firstName;
	public String lastName;
	public String eMail;

	public User() {

	}

	public User(JSONObject object) {
		id = object.optLong(JSONKeys.id, -1);
		firstName = object.optString(JSONKeys.firstName, null);
		lastName = object.optString(JSONKeys.lastName, null);
		eMail = object.optString(JSONKeys.eMail, null);
	}

	@Override
	public String toString() {

		return "[" + JSONKeys.id + ":" + id + "," + JSONKeys.firstName + ":" + firstName + "," + JSONKeys.lastName + ":" + lastName + "," + JSONKeys.eMail + ":" + eMail + "]";
	}

	public static User getUnknownUser() {
		User user = new User();
		user.id = -1;
		user.firstName = "unknown";
		user.lastName = "unknown";
		user.eMail = "unknown";
		return user;
	}
}
