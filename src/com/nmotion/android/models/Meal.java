package com.nmotion.android.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class Meal {

	public int id;
	public String name;
	public String description;
	public String image;
	public String imageThumb;
	public int position;

	//public int mealOptionsSelectedPosition1 = 0;
	public int mealOptionsSelectedId = 1;

	public ArrayList<MealOption> optionsList = new ArrayList<MealOption>();
	public ArrayList<MealOption> ingridientsList = new ArrayList<MealOption>();
	public int defaultOptionId;
	public int menuCategoryId;
	public int discountPercent;

	public float price;
	//public float discountPrice;

	public float priceIncludingTax;
	public float discountPriceIncludingTax;

	public Meal() {
	}

	public Meal(int menuCategoryId, JSONObject object) {
		this.menuCategoryId = menuCategoryId;
		parse(object);
	}

	private void parse(JSONObject object) {
		id = object.optInt(JSONKeys.id, -1);
		position = object.optInt(JSONKeys.position, -1);
		price = (float) object.optDouble(JSONKeys.price, 0.00);
		//discountPrice = (float) object.optDouble(JSONKeys.discountPrice, -1);
		discountPercent = object.optInt(JSONKeys.discountPercent, -1);

		priceIncludingTax = (float) object.optDouble(JSONKeys.priceIncludingTax, 0.00);
		discountPriceIncludingTax = (float) object.optDouble(JSONKeys.discountPriceIncludingTax, -1);
		if (priceIncludingTax == discountPriceIncludingTax) {
			discountPriceIncludingTax = -1;
		}
		name = object.optString(JSONKeys.name, null);
		description = object.optString(JSONKeys.description, null);
		defaultOptionId = object.optInt(JSONKeys.mealOptionsDefaultId, 1);
		JSONObject logoAssetObject = object.optJSONObject("logoAsset");
		if (logoAssetObject != null) {
			image = logoAssetObject.optString(JSONKeys.image);
		}

		JSONObject thumbLogoAssetObject = object.optJSONObject("thumbLogoAsset");
		if (thumbLogoAssetObject != null) {
			imageThumb = thumbLogoAssetObject.optString(JSONKeys.image);
		}

		getMealOptions(object);
		getMealIngridients(object);

		mealOptionsSelectedId = defaultOptionId;
	}

	public boolean isOptionAvailible() {
		return optionsList.size() > 0 ? true : false;
	}

	public boolean isExtraAvailible() {
		return ingridientsList.size() > 0 ? true : false;
	}

	public MealOption getSelectedMealOption() {
		if (isOptionAvailible()) {
			for (MealOption option : optionsList) {
				if (option.id == mealOptionsSelectedId) {
					return option;
				}
			}
		}
		return null;
	}

	protected void getMealIngridients(JSONObject object) {
		if (object.has(JSONKeys.mealExtraIngredients)) {
			ingridientsList.clear();
			JSONArray ingridients = object.optJSONArray(JSONKeys.mealExtraIngredients);
			for (int i = 0; i < ingridients.length(); i++) {
			    
				try {
					ingridientsList.add(new MealOption(id, false, ingridients.optJSONObject(i)));
				} catch (JSONException e) {
					continue;
				}
			}
		}
	}

	protected void getMealOptions(JSONObject object) {
		if (object.has(JSONKeys.mealOptions)) {
			optionsList.clear();
			JSONArray options = object.optJSONArray(JSONKeys.mealOptions);
			for (int i = 0; i < options.length(); i++) {
				try {
					MealOption mealOption = new MealOption(id, true, options.optJSONObject(i));
					optionsList.add(mealOption);
				} catch (JSONException e) {
					continue;
				}
			}
		}
	}

	public static ArrayList<Meal> parseJSONToList(int menuCategoryId, JSONArray array) {
		ArrayList<Meal> result = new ArrayList<Meal>();
		if (array != null && array.length() > 0) {
			int l = array.length();
			JSONObject jsonObject;
			for (int i = 0; i < l; i++) {
				jsonObject = array.optJSONObject(i);
				if (jsonObject != null) {
					result.add(new Meal(menuCategoryId, jsonObject));
				}
			}
		}
		return result;
	}

	public float getMealOptionPrice() {
		if (isOptionAvailible()) {
		    if (getSelectedMealOption()==null)
		        return 0.0f;
		    else
			return getSelectedMealOption().priceIncludingTax;
		} else {
			return 0.0f;
		}
	}
	
	public float getMealOptionDiscountPrice() {
            if (isOptionAvailible() && getSelectedMealOption()!=null) {
                    return getSelectedMealOption().discountPriceIncludingTax>0 ? getSelectedMealOption().discountPriceIncludingTax : getSelectedMealOption().priceIncludingTax;
            } else {
                    return 0.0f;
            }
    }

	public ArrayList<MealOption> getChoosedIngridients() {
		ArrayList<MealOption> choosedIngridients = new ArrayList<MealOption>();
		for (MealOption ingridient : ingridientsList) {
			if (ingridient.isChoosed) {
				choosedIngridients.add(ingridient);
			}
		}
		return choosedIngridients;
	}

	public float getMealExtraIngridientsPriceSum() {
		if (isExtraAvailible()) {
			float ingridientsPriceSum = 0.0f;
			int count=0;
			for (MealOption ingridient : ingridientsList) {
				if (ingridient.isChoosed) {
				    ingridientsPriceSum += ingridient.priceIncludingTax;
				}
			}
			return ingridientsPriceSum;
		} else {
			return 0.0f;
		}
	}
	
	public float getMealExtraIngridientsDiscountPriceSum() {
            if (isExtraAvailible()) {
                    float ingridientsPriceSum = 0.0f;
                    for (MealOption ingridient : ingridientsList) {
                            if (ingridient.isChoosed) {
                                    ingridientsPriceSum += ingridient.discountPriceIncludingTax>0 ? ingridient.discountPriceIncludingTax : ingridient.priceIncludingTax;
                            }
                    }
                    return ingridientsPriceSum;
            } else {
                    return 0.0f;
            }
    }

}
