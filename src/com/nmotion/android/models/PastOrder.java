package com.nmotion.android.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class PastOrder implements Comparable<PastOrder> {

	public long id;
	public String restaurantName;
	//public int restaurantId;
	public float orderTotal;
	public long createdAt;

	public PastOrder(JSONObject object) throws JSONException {

		id = object.getInt(JSONKeys.id);
		//restaurantId = object.getJSONObject(JSONKeys.restaurant).getInt(JSONKeys.id);
		restaurantName = object.getJSONObject(JSONKeys.restaurant).getString(JSONKeys.name);
		orderTotal = (float) object.optDouble(JSONKeys.consolidatedOrderTotal, 0);
		// need to * 1000 because server return date in seconds
		createdAt = object.getLong(JSONKeys.createdAt) * 1000;

	}

	public static ArrayList<PastOrder> parseArrayToList(JSONObject object) throws JSONException {
		ArrayList<PastOrder> orderListItems = new ArrayList<PastOrder>();

		JSONArray entries = object.getJSONArray("entries");
		for (int i = 0; i < entries.length(); i++) {
			orderListItems.add(new PastOrder(entries.getJSONObject(i)));
		}

		return orderListItems;
	}
	
	@Override 
	public int hashCode() {
	    return (int)createdAt;
	};
	
	@Override
	public boolean equals(Object o) {
	    return o instanceof PastOrder && ((PastOrder)o).id==id && ((PastOrder)o).createdAt==createdAt;
	};

	@Override
	public int compareTo(PastOrder another) {
		if (createdAt > another.createdAt) {
			return -1;
		} else if (createdAt < another.createdAt) {
			return 1;
		}
		return 0;
	}
}
