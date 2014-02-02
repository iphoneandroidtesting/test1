package com.nmotion.android.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class PastOrderDetails extends OrderDetails {
	private static final long serialVersionUID = 1L;
	public long orderId;
	public Restaurant restaurant;
	public ArrayList<OrderedMeal> subOrders;

	public PastOrderDetails(JSONObject object) throws JSONException {
		super(object);
		JSONObject data = object.getJSONArray("entries").getJSONObject(0);
		restaurant = new Restaurant(data.getJSONObject(JSONKeys.restaurant));
		JSONArray meals = data.getJSONArray("orderMeals");
		subOrders = new ArrayList<OrderedMeal>();
		for (int i = 0; i < meals.length(); i++) {
			JSONObject item = meals.getJSONObject(i);
			int id = item.getInt(JSONKeys.id);
			int quantity = item.getInt(JSONKeys.quantity);
			PastOrderMeal orderedMeal = new PastOrderMeal(item);
			OrderedMeal order = new OrderedMeal(id, restaurant, orderedMeal, quantity);
			subOrders.add(order);
		}
	}
}