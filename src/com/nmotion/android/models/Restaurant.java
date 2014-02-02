package com.nmotion.android.models;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class Restaurant /*extends OverlayItem*/ {

	public int id;
	public String name;
	public String address;
	public String city;
	public String postalCode;
	public String image;
	public String phone;
	public double longitude;
	public double latitude;
	public double distance;
	public String briefDescription;
	public String siteUrl;
	public String feedbackUrl;
	public String videoUrl;
	public boolean isOpen;
	public boolean isTakeAway;
	public boolean isInHouse;
	public boolean isRoomService;

	private ArrayList<Restaurant> list = new ArrayList<Restaurant>();

	public void addList(Restaurant item) {
		list.add(item);
	}

	public ArrayList<Restaurant> getList() {
		return list;
	}

	@Override
	public String toString() {
		return "Restaurant [name=" + name + ", address=" + address + ", longitude=" + longitude + ", latitude=" + latitude + "]";
	}

	/*@Override
	public GeoPoint getPoint() {
		return new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
	}

	@Override
	public String getTitle() {
		return name;
	}*/

	public String getDistance(String format) {
		DecimalFormat decimalFormat = new DecimalFormat(format);
		return decimalFormat.format(distance);
	}

	/*@Override
	public String getSnippet() {
		return null;
		// return address;
	}*/

	public Restaurant() {
		//super(new GeoPoint(0, 0), "", "");
	}

	public Restaurant(String name) {
		//super(new GeoPoint(0, 0), "", "");
		this.name = name;
	}

	public Restaurant(JSONObject object) {
		//super(new GeoPoint(0, 0), "", "");
		id = object.optInt(JSONKeys.id, -1);
		isOpen = object.optBoolean(JSONKeys.isOpen, false);
		isTakeAway = object.optBoolean(JSONKeys.takeAway, false);
		isInHouse = object.optBoolean(JSONKeys.inHouse, false);
		isRoomService = object.optBoolean(JSONKeys.roomService, false);
		name = object.optString(JSONKeys.name, null);
		phone = object.optString(JSONKeys.phone, null);
		briefDescription = object.optString(JSONKeys.fullDescription, null);
		siteUrl = object.optString(JSONKeys.siteUrl, null);
		feedbackUrl = object.optString(JSONKeys.feedbackUrl, null);
		videoUrl = object.optString(JSONKeys.videoUrl, null);
		JSONObject jsonObject = object.optJSONObject(JSONKeys.address);
		if (jsonObject != null) {
			address = jsonObject.optString(JSONKeys.addressLine1, null);
			city = jsonObject.optString(JSONKeys.city, null);
			longitude = jsonObject.optDouble(JSONKeys.longitude);
			latitude = jsonObject.optDouble(JSONKeys.latitude);
			postalCode = jsonObject.optString(JSONKeys.postalCode);
		}

		jsonObject = object.optJSONObject("logoAsset");
		if (jsonObject != null) {
			image = jsonObject.optString(JSONKeys.image);
		}
		distance = object.optDouble(JSONKeys.distance, -1);
	}

	public static ArrayList<Restaurant> parse(JSONArray array) {
		ArrayList<Restaurant> result = new ArrayList<Restaurant>();
		if (array != null && array.length() > 0) {
			JSONObject jsonObject;
			for (int i = 0, l = array.length(); i < l; i++) {
				jsonObject = array.optJSONObject(i);
				if (jsonObject != null) {
					result.add(new Restaurant(jsonObject));
				}
			}
		}
		return result;
	}
}
