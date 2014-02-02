package com.nmotion.android.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class PastOrderMeal extends Meal {

	public PastOrderMeal(JSONObject object) throws JSONException {
		super(-1, object);
		id = object.getJSONObject(JSONKeys.meal).optInt(JSONKeys.id);
		getMealOptions(object);
                getMealIngridients(object);
	}

	protected void getMealIngridients(JSONObject object) {
		if (object.has(JSONKeys.orderMealExtraIngredients)) {
			ingridientsList.clear();
			JSONArray ingridients = object.optJSONArray(JSONKeys.orderMealExtraIngredients);
			for (int i = 0; i < ingridients.length(); i++) {
				try {
					MealOption ingridient = new MealOption(id, false, ingridients.optJSONObject(i));
					ingridient.isChoosed = true;
					ingridientsList.add(ingridient);
				} catch (JSONException e) {
				    e.printStackTrace();
					continue;
				}
			}
		}
	}

	@Override
	protected void getMealOptions(JSONObject object) {
		if (object.has(JSONKeys.mealOption)) {
			optionsList.clear();
			try {
				MealOption mealOption = new MealOption(id, object);
				optionsList.add(mealOption);
				defaultOptionId = mealOption.id;
			} catch (JSONException e) {
			    e.printStackTrace();
			}
		}
	}
}
