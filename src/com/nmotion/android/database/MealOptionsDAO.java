package com.nmotion.android.database;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.database.MealOptionsDAO.MealsOptionsDBSchema.MealsOptionsColumns;
import com.nmotion.android.models.MealOption;

public class MealOptionsDAO extends BasicDAO<MealOption> {

	public interface MealsOptionsDBSchema {

		public enum MealsOptionsColumns {
			_ID("_id", DBType.INT), 
			MEAL_OPTION_ID("meal_option_id", DBType.NUMERIC), 
			NAME("name", DBType.TEXT), 
			PRICE("price", DBType.FLOAT), 
			IS_OPTION("is_option", DBType.INT), 
			MEAL_ID("meal_id",DBType.INT),
			DISCOUNT_PRICE("discount_price", DBType.FLOAT),
			DISCOUNT_PERCENT("discount_percent", DBType.INT);;

			private String columnName;
			private DBType type;

			MealsOptionsColumns(String columnName, DBType type) {
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

	public interface MealsOptionsColumnsInt {

		int _ID = 0;
		int MEAL_OPTION_ID = 1;
		int NAME = 2;
		int PRICE = 3;
		int IS_OPTION = 4;

	}

	public MealOptionsDAO(SQLiteDatabase db) {
		super(db, Tables.MEAL_OPTIONS_AND_INGRIDIENTS);
	}

	@Override
	protected ContentValues createValues(MealOption item) {
		ContentValues values = new ContentValues();
		values.put(MealsOptionsColumns.MEAL_OPTION_ID.getName(), item.id);
		values.put(MealsOptionsColumns.NAME.getName(), item.name);
		values.put(MealsOptionsColumns.PRICE.getName(), item.priceIncludingTax);
		values.put(MealsOptionsColumns.IS_OPTION.getName(), item.isOption ? 1 : 0);
		values.put(MealsOptionsColumns.MEAL_ID.getName(), item.mealId);
		values.put(MealsOptionsColumns.DISCOUNT_PERCENT.getName(), item.discountPercent);
		values.put(MealsOptionsColumns.DISCOUNT_PRICE.getName(), item.discountPriceIncludingTax);
		return values;
	}

	@Override
	protected MealOption parseValues(ContentValues values) {
		MealOption mealOption = new MealOption();
		mealOption.id = values.getAsInteger(MealsOptionsColumns.MEAL_OPTION_ID.getName());
		mealOption.name = values.getAsString(MealsOptionsColumns.NAME.getName());
		mealOption.priceIncludingTax = values.getAsFloat(MealsOptionsColumns.PRICE.getName());
		mealOption.isOption = values.getAsInteger(MealsOptionsColumns.IS_OPTION.getName()) == 1 ? true : false;
		mealOption.mealId = values.getAsInteger(MealsOptionsColumns.MEAL_ID.getName());
		mealOption.discountPercent = values.getAsInteger(MealsOptionsColumns.DISCOUNT_PERCENT.getName());
		mealOption.discountPriceIncludingTax = values.getAsFloat(MealsOptionsColumns.DISCOUNT_PRICE.getName());
		return mealOption;
	}

	@Override
	public List<MealOption> readItems() {
		return readItems(null, null, null, null, null);
	}

	@Override
	public void deleteItem(MealOption item) {
		String selection = MealsOptionsColumns.MEAL_OPTION_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.id) };
		deleteItems(selection, selectionArgs);
	}

	@Override
	public void deleteItems() {
		deleteItems(null, null);
	}

	public List<MealOption> readOptionsByMealId(int mealId) {
		String selection = MealsOptionsColumns.MEAL_ID.getName() + "=? AND " + MealsOptionsColumns.IS_OPTION.getName() + "=1";
		String[] selectionArgs = { String.valueOf(mealId) };
		List<MealOption> items = readItems(selection, selectionArgs, null, null, null);
		return items;
	}

	public List<MealOption> readIngridientsByMealId(int mealId) {
		String selection = MealsOptionsColumns.MEAL_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(mealId) };
		List<MealOption> items = readItems(selection, selectionArgs, null, null, null);
		return items;
	}

}
