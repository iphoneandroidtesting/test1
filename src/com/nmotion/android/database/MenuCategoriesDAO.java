package com.nmotion.android.database;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.database.MenuCategoriesDAO.MenuCategoriesDBSchema.MenuCategoriesColumns;
import com.nmotion.android.models.MenuCategory;

public class MenuCategoriesDAO extends BasicDAO<MenuCategory> {

	public interface MenuCategoriesDBSchema {

		public enum MenuCategoriesColumns {
			_ID("_id", DBType.INT), CATEGORY_ID("id", DBType.NUMERIC), NAME("name", DBType.TEXT), DESCRIPTION("description", DBType.TEXT), POSITION("position", DBType.INT), RESTAURANT_ID(
					"restaurant_id", DBType.INT);

			private String columnName;
			private DBType type;

			MenuCategoriesColumns(String columnName, DBType type) {
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

	public interface MenuCategoriesColumnsInt {

		int _ID = 0;
		int CATEGORY_ID = 1;
		int NAME = 2;
		int DESCRIPTION = 3;
		int POSITION = 4;
		int RESTAURANT_ID = 4;
	}

	public MenuCategoriesDAO(SQLiteDatabase db) {
		super(db, Tables.MENU_CATEGORIES_TABLE);
	}

	@Override
	protected ContentValues createValues(MenuCategory item) {
		ContentValues values = new ContentValues();
		values.put(MenuCategoriesColumns.CATEGORY_ID.getName(), item.id);
		values.put(MenuCategoriesColumns.NAME.getName(), item.name);
		values.put(MenuCategoriesColumns.DESCRIPTION.getName(), item.description);
		values.put(MenuCategoriesColumns.POSITION.getName(), item.position);
		values.put(MenuCategoriesColumns.RESTAURANT_ID.getName(), item.restaurantId);
		return values;
	}

	@Override
	protected MenuCategory parseValues(ContentValues values) {
		MenuCategory item = new MenuCategory();
		item.id = values.getAsInteger(MenuCategoriesColumns.CATEGORY_ID.getName());
		item.name = values.getAsString(MenuCategoriesColumns.NAME.getName());
		item.description = values.getAsString(MenuCategoriesColumns.DESCRIPTION.getName());
		item.position = values.getAsInteger(MenuCategoriesColumns.POSITION.getName());
		item.restaurantId = values.getAsInteger(MenuCategoriesColumns.RESTAURANT_ID.getName());
		return item;
	}

	@Override
	public List<MenuCategory> readItems() {
		return readItems(null, null, null, null, null);
	}

	@Override
	public void deleteItem(MenuCategory item) {
		String selection = MenuCategoriesColumns.CATEGORY_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.id) };
		deleteItems(selection, selectionArgs);
	}

	@Override
	public void deleteItems() {
		deleteItems(null, null);
	}
}
