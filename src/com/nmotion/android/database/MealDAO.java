package com.nmotion.android.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.database.MealDAO.MealsDBSchema.MealsColumns;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MealOption;

public class MealDAO extends BasicDAO<Meal> {

	public interface MealsDBSchema {

		public enum MealsColumns {
			_ID("_id", DBType.INT), MEAL_ID("meal_id", DBType.NUMERIC), NAME("name", DBType.TEXT), DESCRIPTION("description", DBType.TEXT), IMAGE("image", DBType.TEXT), IMAGE_thumb("image_thumb",
					DBType.TEXT), PRICE("price", DBType.FLOAT), POSITION("position", DBType.INT), DEFAULT_OPTION_ID("default_option_id", DBType.INT), MENU_CATEGORY_ID("menu_category_id", DBType.INT), DISCOUNT_PRICE(
					"discount_price", DBType.FLOAT), DISCOUNT_PERCENT("discount_percent", DBType.INT), PRICE_INCLUDING_TAX("price_including_tax", DBType.FLOAT);

			private String columnName;
			private DBType type;

			MealsColumns(String columnName, DBType type) {
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

	public interface MealsColumnsInt {

		int _ID = 0;
		int MEAL_ID = 1;
		int NAME = 2;
		int DESCRIPTION = 3;
		int IMAGE = 4;
		int IMAGE_THUMB = 5;
		int PRICE = 6;
		int POSITION = 7;
		int DEFAULT_OPTION_ID = 8;
		int MENU_CATEGORY_ID = 10;
		int DISCOUNT_PRICE = 11;
		int DISCOUNT_PERCENT = 12;
	}

	private MealOptionsDAO mealOptonsDAO;

	public MealDAO(SQLiteDatabase db) {
		super(db, Tables.MEALS_TABLE);
		mealOptonsDAO = new MealOptionsDAO(db);
	}

	@Override
	protected ContentValues createValues(Meal item) {
		ContentValues values = new ContentValues();
		values.put(MealsColumns.MEAL_ID.getName(), item.id);
		values.put(MealsColumns.NAME.getName(), item.name);
		values.put(MealsColumns.DESCRIPTION.getName(), item.description);
		values.put(MealsColumns.IMAGE.getName(), item.image);
		values.put(MealsColumns.IMAGE_thumb.getName(), item.imageThumb);
		values.put(MealsColumns.PRICE.getName(), item.priceIncludingTax);
		values.put(MealsColumns.PRICE.getName(), item.price);
		values.put(MealsColumns.PRICE_INCLUDING_TAX.getName(), item.priceIncludingTax);
		values.put(MealsColumns.POSITION.getName(), item.position);
		values.put(MealsColumns.DEFAULT_OPTION_ID.getName(), item.defaultOptionId);
		values.put(MealsColumns.MENU_CATEGORY_ID.getName(), item.menuCategoryId);
		values.put(MealsColumns.DISCOUNT_PERCENT.getName(), item.discountPercent);
		values.put(MealsColumns.DISCOUNT_PRICE.getName(), item.discountPriceIncludingTax);
		mealOptonsDAO.storeItems(item.optionsList);
		mealOptonsDAO.storeItems(item.ingridientsList);
		return values;
	}

	@Override
	protected Meal parseValues(ContentValues values) {
		Meal meal = new Meal();
		meal.id = values.getAsInteger(MealsColumns.MEAL_ID.getName());
		meal.name = values.getAsString(MealsColumns.NAME.getName());
		meal.description = values.getAsString(MealsColumns.DESCRIPTION.getName());
		meal.image = values.getAsString(MealsColumns.IMAGE.getName());
		meal.imageThumb = values.getAsString(MealsColumns.IMAGE_thumb.getName());
		meal.price = values.getAsFloat(MealsColumns.PRICE_INCLUDING_TAX.getName());
		meal.priceIncludingTax = values.getAsFloat(MealsColumns.PRICE.getName());
		meal.position = values.getAsInteger(MealsColumns.POSITION.getName());
		meal.defaultOptionId = values.getAsInteger(MealsColumns.DEFAULT_OPTION_ID.getName());
		meal.menuCategoryId = values.getAsInteger(MealsColumns.MENU_CATEGORY_ID.getName());
		meal.discountPercent = values.getAsInteger(MealsColumns.DISCOUNT_PERCENT.getName());
		meal.discountPriceIncludingTax = values.getAsFloat(MealsColumns.DISCOUNT_PRICE.getName());
		meal.optionsList = (ArrayList<MealOption>) mealOptonsDAO.readOptionsByMealId(meal.id);
		meal.ingridientsList = (ArrayList<MealOption>) mealOptonsDAO.readIngridientsByMealId(meal.id);
		return meal;
	}

	@Override
	public List<Meal> readItems() {
		return readItems(null, null, null, null, null);
	}

	@Override
	public void deleteItem(Meal item) {
		String selection = MealsColumns.MEAL_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.id) };
		deleteItems(selection, selectionArgs);
	}

	@Override
	public void deleteItems() {
		deleteItems(null, null);
	}

	public Meal readItemById(int mealId) {
		String selection = MealsColumns.MEAL_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(mealId) };
		List<Meal> items = readItems(selection, selectionArgs, null, null, null);
		if (items != null && items.size() > 0) {
			return items.get(0);
		} else {
			return null;
		}
	}

	public void updateItem(Meal item) {
		String selection = MealsColumns.MEAL_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.id) };
		updateItem(createValues(item), selection, selectionArgs);
	}

}
