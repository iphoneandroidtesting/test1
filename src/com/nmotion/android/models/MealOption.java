package com.nmotion.android.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class MealOption {
    public int id;

    public String name;
    public float priceIncludingTax;
    public float discountPriceIncludingTax;

    // priceIncludingTax
    // discountPriceIncludingTax

    public boolean isChoosed;
    public boolean isOption;
    public Integer mealId;
    public int discountPercent;
    public float price;

    // public float discountPrice;

    public MealOption(int mealId, boolean isOption, JSONObject option) throws JSONException {
        id = option.getInt(JSONKeys.id);
        name = option.getString(JSONKeys.name);
        discountPercent = option.optInt(JSONKeys.discountPercent);
        price = (float) option.getDouble(JSONKeys.price);
        // discountPrice = (float) option.optDouble(JSONKeys.discountPrice);

        priceIncludingTax = (float) option.getDouble(JSONKeys.priceIncludingTax);
        discountPriceIncludingTax = (float) option.optDouble(JSONKeys.discountPriceIncludingTax);

        this.mealId = mealId;
        this.isOption = isOption;
    }

    public MealOption(int mealId, JSONObject option) throws JSONException {
        id = option.optJSONObject(JSONKeys.mealOption).getInt(JSONKeys.id);
        name = option.getString(JSONKeys.mealOptionName);
        discountPercent = option.optInt(JSONKeys.discountPercent);
        price = (float) option.getDouble(JSONKeys.mealOptionPrice);

        priceIncludingTax = (float) option.getDouble(JSONKeys.mealOptionPriceIncludingTax);
        discountPriceIncludingTax = (float) option.getDouble(JSONKeys.mealOptionDiscountPriceIncludingTax);
        

        this.mealId = mealId;
        this.isOption = true;
    }

    public MealOption() {

    }
}