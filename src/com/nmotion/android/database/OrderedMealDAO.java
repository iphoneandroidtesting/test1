package com.nmotion.android.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.database.OrderedMealDAO.OrderedMealDBSchema.OrderedMealsColumns;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MealOption;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.models.Restaurant;

public class OrderedMealDAO extends BasicDAO<OrderedMeal> {
	//@formatter:off

	public interface OrderedMealDBSchema {

		public enum OrderedMealsColumns {
			_ID("_id", DBType.INT), 
			ORDER_ID("order_id", DBType.INT), 
			RESTAURANT_ID("restaurant_id", DBType.NUMERIC), 
			QUANTITY("quantity", DBType.INT), 
			MEAL_ID("meal_id", DBType.NUMERIC), 
			COMMENT("comment", DBType.TEXT), 
			SELECTED_MEAL_OPTION("selected_option", DBType.INT), 
			CHOOSED_EXTRAS_ARRAY("extras", DBType.INT),
			MEAL_ORDER_ID("meal_order_id", DBType.INT);

			private String columnName;
			private DBType type;

			OrderedMealsColumns(String columnName, DBType type) {
				this.columnName = columnName;
				this.type = type;
			}

			public String getName() {
				return columnName;
			}

			public DBType getType() {
				return type;
			}
		}
	}
	//@formatter:on

	private RestaurantsDAO restaurantDAO;
	private MealDAO mealsDAO;

	public OrderedMealDAO(SQLiteDatabase db) {
		super(db, Tables.ORDERED_MEALS_TABLE);
		restaurantDAO = new RestaurantsDAO(db);
		mealsDAO = new MealDAO(db);
	}

	@Override
	protected ContentValues createValues(OrderedMeal item) {
		ContentValues values = new ContentValues();
		values.put(OrderedMealsColumns.ORDER_ID.getName(), item.orderId);
		values.put(OrderedMealsColumns.RESTAURANT_ID.getName(), item.restaurant.id);
		values.put(OrderedMealsColumns.MEAL_ID.getName(), item.meal.id);
		values.put(OrderedMealsColumns.QUANTITY.getName(), item.quantity);
		values.put(OrderedMealsColumns.COMMENT.getName(), item.comment);
		values.put(OrderedMealsColumns.SELECTED_MEAL_OPTION.getName(), item.meal.mealOptionsSelectedId);
		values.put(OrderedMealsColumns.CHOOSED_EXTRAS_ARRAY.getName(), convertExtrasArrayToStringIds(item.meal.getChoosedIngridients()));
		values.put(OrderedMealsColumns.MEAL_ORDER_ID.getName(), item.mealOrderId);
		return values;
	}

	public static String convertExtrasArrayToStringIds(ArrayList<MealOption> extras) {
		String str = "";
		for (int i = 0; i < extras.size(); i++) {
			str = str + extras.get(i).id;
			// Do not append comma at the end of last element
			if (i < extras.size() - 1) {
				str = str.concat(",");
			}
		}
		return str;
	}

	public static String[] convertStringToArray(String str) {
		String[] arr = str.split(",");
		return arr;
	}

	@Override
	protected OrderedMeal parseValues(ContentValues values) {
		int orderId = values.getAsInteger(OrderedMealsColumns.ORDER_ID.getName());
		int restaurantId = values.getAsInteger(OrderedMealsColumns.RESTAURANT_ID.getName());
		int mealId = values.getAsInteger(OrderedMealsColumns.MEAL_ID.getName());
		int quantity = values.getAsInteger(OrderedMealsColumns.QUANTITY.getName());
		String comment = values.getAsString(OrderedMealsColumns.COMMENT.getName());
		int mealOptionSelectedId = values.getAsInteger(OrderedMealsColumns.SELECTED_MEAL_OPTION.getName());
		int mealOrderId = values.getAsInteger(OrderedMealsColumns.MEAL_ORDER_ID.getName());
		String[] choosedMeals = convertStringToArray(values.getAsString(OrderedMealsColumns.CHOOSED_EXTRAS_ARRAY.getName()));

		Restaurant restaurant = restaurantDAO.readItemById(restaurantId);
		Meal meal = mealsDAO.readItemById(mealId);
		meal.mealOptionsSelectedId = mealOptionSelectedId;

		if (choosedMeals.length > 0) {
			for (int i = 0; i < choosedMeals.length; i++) {
				for (int j = 0; j < meal.ingridientsList.size(); j++) {
					MealOption ingridient = meal.ingridientsList.get(j);
					try {
						if (ingridient.id == Integer.valueOf(choosedMeals[i])) {
							meal.ingridientsList.get(j).isChoosed = true;
							break;
						}
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
		}
		OrderedMeal item = new OrderedMeal(orderId, restaurant, meal, quantity);
		item.setMealOrderId(mealOrderId);
		item.comment = comment;
		return item;
	}

	@Override
	public List<OrderedMeal> readItems() {
		return readItems(null, null, null, null, null);
	}

	@Override
	public void deleteItem(OrderedMeal item) {
		String selection = OrderedMealsColumns.ORDER_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.orderId) };
		deleteItems(selection, selectionArgs);
	}

	@Override
	public void deleteItems() {
		deleteItems(null, null);
	}

	public void updateItem(OrderedMeal item) {
		String selection = OrderedMealsColumns.ORDER_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.orderId) };
		updateItem(createValues(item), selection, selectionArgs);

	}

}
