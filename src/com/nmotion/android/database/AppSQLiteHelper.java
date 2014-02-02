package com.nmotion.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nmotion.android.database.CreditCardsDBSchema.CreditCardsColumns;
import com.nmotion.android.database.MealDAO.MealsDBSchema.MealsColumns;
import com.nmotion.android.database.MealOptionsDAO.MealsOptionsDBSchema.MealsOptionsColumns;
import com.nmotion.android.database.MenuCategoriesDAO.MenuCategoriesDBSchema.MenuCategoriesColumns;
import com.nmotion.android.database.OrderedMealDAO.OrderedMealDBSchema.OrderedMealsColumns;
import com.nmotion.android.database.RestaurantsDAO.RestaurantDBSchema.RestaurantColumns;
import com.nmotion.android.database.SingleObjectsDAO.OrderDetailsDBSchema.SingleObjectsColumns;

public class AppSQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "app.db";
	private static final int DATABASE_VERSION = 11;
	public static final String CONTENT_AUTHORITY = "com.nmotion.android.database";

	public AppSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder(1024);
		sql.append("CREATE TABLE ").append(Tables.CREDIT_CARDS_TABLE).append(" (");
		for (CreditCardsColumns column : CreditCardsColumns.values()) {
			if (CreditCardsColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.append("UNIQUE (").append(CreditCardsColumns.CARD_ID.getName()).append(") ON CONFLICT IGNORE)");
		db.execSQL(sql.toString());
		sql.setLength(0);

		sql.append("CREATE TABLE ").append(Tables.ORDERED_MEALS_TABLE).append(" (");
		for (OrderedMealsColumns column : OrderedMealsColumns.values()) {
			if (OrderedMealsColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.replace(sql.length() - 1, sql.length(), ")");
		db.execSQL(sql.toString());
		sql.setLength(0);

		sql.append("CREATE TABLE ").append(Tables.RESTAURANTS_TABLE).append(" (");
		for (RestaurantColumns column : RestaurantColumns.values()) {
			if (RestaurantColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.append("UNIQUE (").append(RestaurantColumns.RESTAURANT_ID.getName()).append(") ON CONFLICT IGNORE)");
		db.execSQL(sql.toString());
		sql.setLength(0);

		sql.append("CREATE TABLE ").append(Tables.MENU_CATEGORIES_TABLE).append(" (");
		for (MenuCategoriesColumns column : MenuCategoriesColumns.values()) {
			if (MenuCategoriesColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.append("UNIQUE (").append(MenuCategoriesColumns.CATEGORY_ID.getName()).append(") ON CONFLICT IGNORE)");
		db.execSQL(sql.toString());
		sql.setLength(0);

		sql.append("CREATE TABLE ").append(Tables.MEALS_TABLE).append(" (");
		for (MealsColumns column : MealsColumns.values()) {
			if (MealsColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.append("UNIQUE (").append(MealsColumns.MEAL_ID.getName()).append(") ON CONFLICT IGNORE)");
		db.execSQL(sql.toString());
		sql.setLength(0);

		sql.append("CREATE TABLE ").append(Tables.MEAL_OPTIONS_AND_INGRIDIENTS).append(" (");
		for (MealsOptionsColumns column : MealsOptionsColumns.values()) {
			if (MealsOptionsColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.append("UNIQUE (").append(MealsOptionsColumns._ID.getName()).append(") ON CONFLICT IGNORE)");
		db.execSQL(sql.toString());
		sql.setLength(0);

		sql.append("CREATE TABLE ").append(Tables.SINGLE_OBJECTS_TABLE).append(" (");
		for (SingleObjectsColumns column : SingleObjectsColumns.values()) {
			if (SingleObjectsColumns._ID.equals(column)) {
				sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			} else {
				sql.append(column.getName()).append(column.getType().getName());
			}
		}
		sql.append("UNIQUE (").append(SingleObjectsColumns.OBJECT_NAME.getName()).append(") ON CONFLICT REPLACE)");
		db.execSQL(sql.toString());
		sql.setLength(0);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
		db.execSQL("DROP TABLE IF EXISTS " + Tables.ORDERED_MEALS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.CREDIT_CARDS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.RESTAURANTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.MEALS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.MENU_CATEGORIES_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.MEAL_OPTIONS_AND_INGRIDIENTS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.SINGLE_OBJECTS_TABLE);

		onCreate(db);
	}
}
