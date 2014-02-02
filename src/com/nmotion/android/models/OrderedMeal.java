package com.nmotion.android.models;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class OrderedMeal {

	public int orderId;
	public int mealOrderId = -1;
	public Restaurant restaurant;
	public Meal meal;
	public int quantity = 1;
	public String comment;

	public OrderedMeal(int id, Restaurant restaurant, Meal meal, int quantity) {
		this.orderId = id;
		this.restaurant = restaurant;
		this.meal = meal;
		this.quantity = quantity;
	}

	public JSONObject toJSONObjectForPOST() throws JSONException {
		JSONObject object = new JSONObject();
		object.put(JSONKeys.meal, meal.id);
		object.put(JSONKeys.discountPercent, meal.discountPercent);

		object.put(JSONKeys.quantity, quantity);

		object.put(JSONKeys.mealComment, comment);
		if (meal.isOptionAvailible()) {
			object.put(JSONKeys.mealOption, meal.mealOptionsSelectedId);
		}
		if (meal.isExtraAvailible()) {
			JSONArray extras = new JSONArray();
			for (MealOption ingridient : meal.ingridientsList) {
				if (ingridient.isChoosed) {
					JSONObject ingridientJSON = new JSONObject();
					ingridientJSON.put(JSONKeys.mealExtraIngredient, ingridient.id);
					extras.put(ingridientJSON);
				}
			}
			object.put(JSONKeys.orderMealExtraIngredients, extras);
		}
		return object;
	}

	public JSONObject toJSONObjectForPUT() throws JSONException {
		JSONObject object = new JSONObject();
		object.put(JSONKeys.meal, meal.id);
		object.put(JSONKeys.quantity, quantity);
		object.put(JSONKeys.name, URLEncoder.encode(meal.name));
		object.put(JSONKeys.price, meal.price);
		object.put(JSONKeys.discountPercent, meal.discountPercent);
		object.put(JSONKeys.mealComment, comment);
		if (mealOrderId != -1) {
			object.put(JSONKeys.id, mealOrderId);
		}
		if (meal.isOptionAvailible()) {
			object.put(JSONKeys.mealOptionName, meal.getSelectedMealOption().name);
			object.put(JSONKeys.mealOptionPrice, meal.getSelectedMealOption().price);
			object.put(JSONKeys.mealOption, meal.mealOptionsSelectedId);
		}
		if (meal.isExtraAvailible()) {
			JSONArray extras = new JSONArray();
			for (MealOption ingridient : meal.ingridientsList) {
				if (ingridient.isChoosed) {
					JSONObject ingridientJSON = new JSONObject();
					ingridientJSON.put(JSONKeys.mealExtraIngredient, ingridient.id);
					ingridientJSON.put(JSONKeys.name, ingridient.name);
					ingridientJSON.put(JSONKeys.price, ingridient.price);
					extras.put(ingridientJSON);
				}
			}
			object.put(JSONKeys.orderMealExtraIngredients, extras);
		}
		return object;
	}

	public void setMealOrderId(int mealOrderId) {
		this.mealOrderId = mealOrderId;
	}
}
