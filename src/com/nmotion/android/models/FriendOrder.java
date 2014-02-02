package com.nmotion.android.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.nmotion.android.core.JSONKeys;

public class FriendOrder implements Parcelable {
	public long id;
	public String restaurantName;
	public float orderTotal;
	public int restaurantId;
	public long createdAt;
	public String firstName;
	public String lastName;
	public String resourceUrl;
	public float orderTotalWhenSlave;

	public FriendOrder(JSONObject object) throws JSONException {

		id = object.getInt(JSONKeys.id);
		restaurantId = object.getJSONObject(JSONKeys.restaurant).getInt(JSONKeys.id);
		restaurantName = object.getJSONObject(JSONKeys.restaurant).getString(JSONKeys.name);
		orderTotal = (float) object.optDouble(JSONKeys.total, 0.00);
		// need to * 1000 because server return date in seconds
		createdAt = object.getLong(JSONKeys.createdAt) * 1000;
		resourceUrl = object.optString(JSONKeys.resourceUrl, null);
		JSONObject userObject = object.getJSONObject(JSONKeys.user);
		firstName = userObject.optString(JSONKeys.firstName, null);
		lastName = userObject.optString(JSONKeys.lastName, null);
		orderTotalWhenSlave = (float) object.optDouble(JSONKeys.orderTotalWhenSlave, orderTotal);
	}

	public static ArrayList<FriendOrder> parseArrayToList(JSONObject object) throws JSONException {
		ArrayList<FriendOrder> orderListItems = new ArrayList<FriendOrder>();
		JSONArray entries = object.getJSONArray("entries");
		for (int i = 0; i < entries.length(); i++) {
			orderListItems.add(new FriendOrder(entries.getJSONObject(i)));
		}

		return orderListItems;
	}

	@Override
	public boolean equals(Object friendOrder) {
		return friendOrder instanceof FriendOrder ? ((FriendOrder)friendOrder).id == id : null;
	};
	
	@Override
	public int hashCode() {	    
	    return (int)id;
	}

	protected FriendOrder(Parcel in) {
		id = in.readLong();
		restaurantName = in.readString();
		orderTotal = in.readFloat();
		restaurantId = in.readInt();
		createdAt = in.readLong();
		firstName = in.readString();
		lastName = in.readString();
		resourceUrl = in.readString();
		orderTotalWhenSlave = in.readFloat();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(restaurantName);
		dest.writeFloat(orderTotal);
		dest.writeInt(restaurantId);
		dest.writeLong(createdAt);
		dest.writeString(firstName);
		dest.writeString(lastName);
		dest.writeString(resourceUrl);
		dest.writeFloat(orderTotalWhenSlave);
	}

	public static final Parcelable.Creator<FriendOrder> CREATOR = new Parcelable.Creator<FriendOrder>() {
		public FriendOrder createFromParcel(Parcel in) {
			return new FriendOrder(in);
		}

		public FriendOrder[] newArray(int size) {
			return new FriendOrder[size];
		}
	};

}
