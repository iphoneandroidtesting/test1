package com.nmotion.android.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class MenuCategory {
	public int id;
	public int position;
	public String name;
	public String description;
	public int restaurantId;

	public MenuCategory() {
	}

	public MenuCategory(Integer restaurantId, JSONObject object) {
		this.restaurantId = restaurantId;
		id = object.optInt(JSONKeys.id, -1);
		position = object.optInt(JSONKeys.position, -1);
		name = object.optString(JSONKeys.name, null);
		description = object.optString(JSONKeys.description, null);
	}

	public static ArrayList<MenuCategory> parse(Integer restaurantId, JSONArray array) {
		ArrayList<MenuCategory> result = new ArrayList<MenuCategory>();
		if (array != null && array.length() > 0) {
			int l = array.length();
			JSONObject jsonObject;
			for (int i = 0; i < l; i++) {
				jsonObject = array.optJSONObject(i);
				if (jsonObject != null) {
					result.add(new MenuCategory(restaurantId, jsonObject));
				}
			}
		}
		return result;
	}
}
