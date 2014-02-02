package com.nmotion.android;

import java.util.HashSet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MealOption;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.view.SelectableViewGroup;
import com.nmotion.android.view.SelectableViewGroup.OnItemSelectChangeListener;
import com.nmotion.android.view.TopPanelLayout;

public class MealsOptionsScreen extends Activity implements OnItemSelectChangeListener {
	public static final String DATA_ORDER_ID = "order_id";
	public static final String EDIT_MODE = "edit_mode";
	private int quantity = 1;
	private boolean isEditMode;
	private TextView restaurantMealQuantity;
	private EditText comment;
	private OrderedMeal orderedMeal;
	private TextView restaurantMealName;
	private SelectableViewGroup mealOptionsContainer;
	private SelectableViewGroup mealExtraIngridientsContainer;
	private String currency;
	private Meal meal;
	private HashSet<Integer> choosedExtraIds = new HashSet<Integer>();
	private int mealOptionSelected;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_meals_option);

		currency = "DKK ";

		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_meals_options);
		//findViewById(R.id.btn_menu_menu).setVisibility(View.VISIBLE);

		mealOptionsContainer = (SelectableViewGroup) findViewById(R.id.meal_options_container);
		mealOptionsContainer.setMultipleSelectEnabled(false);
		mealOptionsContainer.setOnItemSelectChangeListener(this);

		mealExtraIngridientsContainer = (SelectableViewGroup) findViewById(R.id.meal_ingridients_container);
		mealExtraIngridientsContainer.setMultipleSelectEnabled(true);
		mealExtraIngridientsContainer.setCheckedEmptyEnable(true);
		mealExtraIngridientsContainer.setOnItemSelectChangeListener(this);
		int orderId = getIntent().getExtras().getInt(DATA_ORDER_ID);
		isEditMode = getIntent().getExtras().getBoolean(EDIT_MODE,false);
		orderedMeal = App.getInstance().getCache().getOrders().get(orderId);
		quantity = orderedMeal.quantity;
		restaurantMealQuantity = (TextView) findViewById(R.id.txt_restaurant_meal_quantity);
		restaurantMealName = (TextView) findViewById(R.id.txt_mean_name);
		restaurantMealQuantity.setText(String.valueOf(quantity));
		restaurantMealName.setText(orderedMeal.meal.name);
		comment = (EditText) findViewById(R.id.txt_comment);
		comment.setText(orderedMeal.comment);

		meal = orderedMeal.meal;

		((TopPanelLayout) findViewById(R.id.top_panel_layout)).updatePanelInfo(App.getInstance().getCache().getCurrentRestaurantId(), false);

		if (meal.isOptionAvailible()) {
			int mealOptionsSelectedPosition = 0;
			findViewById(R.id.txt_meal_option_options_title).setVisibility(View.VISIBLE);
			for (MealOption option : meal.optionsList) {
				mealOptionsContainer.addItem(getViewOption(option));
				if (option.id == meal.mealOptionsSelectedId) {
					mealOptionsSelectedPosition = mealOptionsContainer.getChildCount() - 1;
				}
			}
			mealOptionsContainer.performItemClick(mealOptionsSelectedPosition);
		}
		if (meal.isExtraAvailible()) {
			findViewById(R.id.txt_meal_option_extra_title).setVisibility(View.VISIBLE);
			for (MealOption ingredient : meal.ingridientsList) {
				mealExtraIngridientsContainer.addItem(getViewIngridient(ingredient));
			}
			for (int i = 0; i < meal.ingridientsList.size(); i++) {
				if (meal.ingridientsList.get(i).isChoosed) {
					mealExtraIngridientsContainer.performItemClick(i);
				}
			}
		}

	}
	
	@Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            if (App.getInstance().getPreferencesManager().isCrash()){
                AppUtils.showDialog(this, "Error", getString(R.string.crash_message)).setOnDismissListener(new OnDismissListener() {                    
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();                        
                    }
                });
            }
        }

	private View getViewIngridient(MealOption ingredient) {
		View view = LayoutInflater.from(this).inflate(R.layout.item_meal_option, null);
		view.setBackgroundColor(getResources().getColor(android.R.color.white));
		// simple way. use one layout but delete background for make difference
		((TextView) view.findViewById(R.id.txt_option_name)).setText(ingredient.name);
		TextView price = (TextView) view.findViewById(R.id.txt_option_price);
		price.setText("+" + currency + ingredient.priceIncludingTax);
		if (ingredient.discountPriceIncludingTax>0 && ingredient.discountPriceIncludingTax!=ingredient.priceIncludingTax){
		    TextView discountPrice = (TextView) view.findViewById(R.id.txt_option_discount_price);
		    discountPrice.setText("+" + currency + ingredient.discountPriceIncludingTax);
                    discountPrice.setVisibility(View.VISIBLE);
                    price.setVisibility(View.GONE);                 
                    //price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
		return view;
	}

	private View getViewOption(MealOption option) {
		View view = LayoutInflater.from(this).inflate(R.layout.item_meal_option, null);
		((TextView) view.findViewById(R.id.txt_option_name)).setText(option.name);
		TextView price = (TextView) view.findViewById(R.id.txt_option_price);
		price.setText(currency + option.priceIncludingTax);
		if (option.discountPriceIncludingTax>0 && option.discountPriceIncludingTax!=option.priceIncludingTax){
		    TextView discountPrice = (TextView) view.findViewById(R.id.txt_option_discount_price);
		    discountPrice.setText(currency + option.discountPriceIncludingTax);
		    discountPrice.setVisibility(View.VISIBLE);
		    price.setVisibility(View.GONE);
		    //price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
		return view;
	}

	public void onOrderClick(View view) {
		updateCart();
		finish();
	}

	public void onMenuClick(View view) {
		Intent intent = new Intent(getApplicationContext(), CategoriesScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, App.getInstance().getCache().getCurrentRestaurantId());
		startActivity(intent);
	}

	public void onPlusClick(View view) {
		quantity++;
		restaurantMealQuantity.setText(String.valueOf(quantity));
	}

	public void onMinusClick(View view) {
		quantity--;
		if (quantity == 0) {
			quantity = 1;
		}
		restaurantMealQuantity.setText(String.valueOf(quantity));
	}

	@Override
	protected void onPause() {
		AppUtils.hideKeyBoard(this);
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
	    if (!isEditMode)
	        App.getInstance().getCache().deleteOrderedMeal(orderedMeal);
	    super.onBackPressed();
	}
	

	private void updateCart() {
		for (int i = 0; i < meal.ingridientsList.size(); i++) {
			meal.ingridientsList.get(i).isChoosed = false;
			if (choosedExtraIds.contains(i)) {
				meal.ingridientsList.get(i).isChoosed = true;
			}
		}
		if (meal.isOptionAvailible()) {
			meal.mealOptionsSelectedId = meal.optionsList.get(mealOptionSelected).id;
		}
		orderedMeal.comment = comment.getText().toString();
		orderedMeal.quantity = quantity;
		App.getInstance().getCache().updateOrderedMeal(orderedMeal);
		startActivity(new Intent(this, OrderCartScreen.class));
	}

	@Override
	public void onItemSelected(View view, int position, boolean isSelected) {
		if (view.getParent().equals(mealExtraIngridientsContainer)) {
			if (!choosedExtraIds.add(position)) {
				choosedExtraIds.remove(position);
			}
		}
		if (view.getParent().equals(mealOptionsContainer)) {
			mealOptionSelected = position;
		}
	}

}
