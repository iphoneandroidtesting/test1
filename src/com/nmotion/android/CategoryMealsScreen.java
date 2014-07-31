package com.nmotion.android;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.adapters.MenuCategoryMealsScreenAdapter;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MenuCategory;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;
import com.nmotion.android.utils.Logger;

public class CategoryMealsScreen extends BaseRestaurantScreen {
	public static final String DATA_CATEGORY_ID = "category_id";
	public static final String DATA_RESTAURANT_ID = "restaurant_id";
	private ListView listView;
	private MenuCategoryMealsScreenAdapter mAdapter;
	private EditText search;
	private MenuCategory menuCategory;
	private int menuCategoryId;
	private ArrayList<Meal> meals;
	private DownloadMenuCategoryMealDescriptionTask descrTask;
	private DownloadMenuCategoryMealsTask catTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		enableSignInButton();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_menu_category_list);
		menuCategoryId = getIntent().getExtras().getInt(DATA_CATEGORY_ID);
		menuCategory = App.getInstance().getCache().getMenuCategoryById(menuCategoryId);

		((TextView) findViewById(R.id.txt_screen_name)).setText(menuCategory.name);

		listView = (ListView) findViewById(R.id.list_view);
		meals = App.getInstance().getCache().getMenuCategoryMeals(menuCategoryId);
		mAdapter = new MenuCategoryMealsScreenAdapter(getApplicationContext(), R.layout.list_item_menu_category_meals, meals);
		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(itemClickListener);
		search = (EditText) findViewById(R.id.txt_menu_category_search);
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				int length = s.length();
				if (length == 0) {
					download(null);
				} else if (length >= Config.SEARCH_SYMBOLS_LENGTH) {
					download(s.toString());
				}
			}
		});
		download(null);
	}

	private void download(String string) {
		catTask = new DownloadMenuCategoryMealsTask();
		catTask.execute(String.valueOf(mRestaurantId), String.valueOf(menuCategory.id), string);
	}
	
	@Override
	protected void onStop() {	
	    super.onStop();
	    if (catTask!=null)
	        catTask.cancel(true);
	    if (descrTask!=null)
	        descrTask.cancel(true);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int index, long id) {
			descrTask = new DownloadMenuCategoryMealDescriptionTask();
			descrTask.execute(String.valueOf(mRestaurantId), String.valueOf(menuCategory.id), String.valueOf(meals.get(index).id));
		}
	};

	private class DownloadMenuCategoryMealsTask extends AsyncTask<String, Void, ArrayList<Meal>> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(CategoryMealsScreen.this, R.string.txt_getting_menu_category_meals, false);
		}

		@Override
		protected ArrayList<Meal> doInBackground(String... params) {
			ArrayList<Meal> result = new ArrayList<Meal>();
			try {
				result = App.getInstance().getNetworkService().getMenuCategoryMeals(params[0], params[1], params[2], CategoryMealsScreen.this);
			} catch (NetworkException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Meal> result) {
			meals.clear();
			meals.addAll(result);
			App.getInstance().getCache().setMenuCategoryMeals(menuCategoryId, meals);
			progressDialog.dismiss();
			mAdapter.notifyDataSetChanged();
			if (meals.isEmpty()) {
				findViewById(R.id.txt_menu_category_list_empty).setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			} else {
				findViewById(R.id.txt_menu_category_list_empty).setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}
		}
	}

	private class DownloadMenuCategoryMealDescriptionTask extends AsyncTask<String, Void, Meal> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(CategoryMealsScreen.this, R.string.txt_getting_meal_description, false);
		}

		@Override
		protected Meal doInBackground(String... params) {
			Meal result = null;
			try {
				String restaurantId = params[0];
				String menuCategoryId = params[1];
				String mealId = params[2];
				result = App.getInstance().getNetworkService().getMenuCategoryMealDescription(restaurantId, menuCategoryId, mealId, CategoryMealsScreen.this);
				ArrayList<Meal> meals = App.getInstance().getCache().getMenuCategoryMeals(Integer.parseInt(menuCategoryId));
				for(Meal meal : meals) {
					if("".equals(meal.image) || meal.image == null) {
						Meal loadedMeal = App.getInstance().getNetworkService().getMenuCategoryMealDescription(restaurantId, menuCategoryId, String.valueOf(meal.id), CategoryMealsScreen.this);
						if(loadedMeal != null) {
							App.getInstance().getCache().setMeal(loadedMeal);
						}
					}
				}
			} catch (NetworkException e) {
				Logger.warning(e.toString());
			}
			return result;
		}

		@Override
		protected void onPostExecute(Meal result) {
			progressDialog.dismiss();
			if (result != null) {
		                App.getInstance().getCache().setMeal(result);
				Intent intent = new Intent(CategoryMealsScreen.this, CategoryMealDescriptionScreen.class);
				intent.putExtra(CategoryMealDescriptionScreen.DATA_MENU_CATEGORY_ID, menuCategoryId);
				intent.putExtra(BaseRestaurantScreen.DATA_RESTAURANT_ID, mRestaurantId);
				intent.putExtra(CategoryMealDescriptionScreen.DATA_MEAL_ID, result.id);
				startActivity(intent);
			} else {
				AppUtils.showToast(CategoryMealsScreen.this, R.string.txt_error);
			}
		}
	}
}
