package com.nmotion.android.database;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.database.RestaurantsDAO.RestaurantDBSchema.RestaurantColumns;
import com.nmotion.android.models.Restaurant;

public class RestaurantsDAO extends BasicDAO<Restaurant> {

	public interface RestaurantDBSchema {

		public enum RestaurantColumns {
			_ID("_id", DBType.INT), RESTAURANT_ID("id", DBType.NUMERIC), NAME("name", DBType.TEXT), ADRESS("adress", DBType.TEXT), CITY("city", DBType.TEXT), POSTAL_CODE("postal_code", DBType.TEXT), IMAGE("image", DBType.TEXT), PHONE(
					"phone", DBType.TEXT), LONGITUDE("longitude", DBType.FLOAT), LATITUDE("latitude", DBType.FLOAT), DISTANCE("distance", DBType.FLOAT), BRIEF("brief", DBType.TEXT), SITE_URL(
					"site_url", DBType.TEXT), FEEDBACK_URL("feedback_url", DBType.TEXT), VIDEO_URL("video_url", DBType.TEXT), IS_OPEN("is_open", DBType.INT), IS_TAKE_AWAY("is_take_away", DBType.INT), 
					IS_IN_HOUSE("is_in_house", DBType.INT), IS_ROOM_SERVICE("is_room_service", DBType.INT);

			private String columnName;
			private DBType type;

			RestaurantColumns(String columnName, DBType type) {
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

/*	public interface RestaurantColumnsInt {

		int _ID = 0;
		int RESTAURANT_ID = 1;
		int NAME = 2;
		int ADRESS = 3;
		int POSTAL_CODE = 4;
		int IMAGE = 5;
		int PHONE = 6;
		int LONGITUDE = 7;
		int LATITUDE = 8;
		int DISTANCE = 9;
		int BRIEF = 10;
		int SITE_URL = 11;
		int IS_OPEN = 12;
		int FEEDBACK_URL = 13;
		int CITY = 14;
		int VIDEO_URL = 15;
	}*/

	public RestaurantsDAO(SQLiteDatabase db) {
		super(db, Tables.RESTAURANTS_TABLE);
	}

	@Override
	protected ContentValues createValues(Restaurant item) {
		ContentValues values = new ContentValues();
		values.put(RestaurantColumns.RESTAURANT_ID.getName(), item.id);
		values.put(RestaurantColumns.NAME.getName(), item.name);
		values.put(RestaurantColumns.ADRESS.getName(), item.address);
		values.put(RestaurantColumns.CITY.getName(), item.city);
		values.put(RestaurantColumns.POSTAL_CODE.getName(), item.postalCode);
		values.put(RestaurantColumns.IMAGE.getName(), item.image);
		values.put(RestaurantColumns.PHONE.getName(), item.phone);
		values.put(RestaurantColumns.LONGITUDE.getName(), item.longitude);
		values.put(RestaurantColumns.LATITUDE.getName(), item.latitude);
		values.put(RestaurantColumns.BRIEF.getName(), item.briefDescription);
		values.put(RestaurantColumns.SITE_URL.getName(), item.siteUrl);
		values.put(RestaurantColumns.FEEDBACK_URL.getName(), item.feedbackUrl);
		values.put(RestaurantColumns.VIDEO_URL.getName(), item.videoUrl);
		values.put(RestaurantColumns.IS_OPEN.getName(), item.isOpen ? 1 : 0);
		values.put(RestaurantColumns.IS_TAKE_AWAY.getName(), item.isTakeAway ? 1 : 0);
		values.put(RestaurantColumns.IS_IN_HOUSE.getName(), item.isInHouse ? 1 : 0);
		values.put(RestaurantColumns.IS_ROOM_SERVICE.getName(), item.isRoomService ? 1 : 0);
		if (item.distance!=-1)
		    values.put(RestaurantColumns.DISTANCE.getName(), item.distance);
		return values;
	}

	@Override
	protected Restaurant parseValues(ContentValues values) {
		Restaurant item = new Restaurant();
		item.id = values.getAsInteger(RestaurantColumns.RESTAURANT_ID.getName());
		item.name = values.getAsString(RestaurantColumns.NAME.getName());
		item.address = values.getAsString(RestaurantColumns.ADRESS.getName());
		item.city = values.getAsString(RestaurantColumns.CITY.getName());
		item.postalCode = values.getAsString(RestaurantColumns.POSTAL_CODE.getName());
		item.image = values.getAsString(RestaurantColumns.IMAGE.getName());
		item.phone = values.getAsString(RestaurantColumns.PHONE.getName());
		item.longitude = values.getAsDouble(RestaurantColumns.LONGITUDE.getName());
		item.latitude = values.getAsDouble(RestaurantColumns.LATITUDE.getName());
		item.briefDescription = values.getAsString(RestaurantColumns.BRIEF.getName());
		item.siteUrl = values.getAsString(RestaurantColumns.SITE_URL.getName());
		item.feedbackUrl = values.getAsString(RestaurantColumns.FEEDBACK_URL.getName());
		item.videoUrl = values.getAsString(RestaurantColumns.VIDEO_URL.getName());
		item.isOpen = values.getAsInteger(RestaurantColumns.IS_OPEN.getName()) == 1 ? true : false;
		item.isTakeAway = values.getAsInteger(RestaurantColumns.IS_TAKE_AWAY.getName()) == 1 ? true : false;
		item.isInHouse = values.getAsInteger(RestaurantColumns.IS_IN_HOUSE.getName()) == 1 ? true : false;
		item.isRoomService = values.getAsInteger(RestaurantColumns.IS_ROOM_SERVICE.getName()) == 1 ? true : false;
		item.distance = values.getAsDouble(RestaurantColumns.DISTANCE.getName());
		return item;
	}

	@Override
	public List<Restaurant> readItems() {
		return readItems(null, null, null, null, null);
	}

	public Restaurant readItemById(int restaurantId) {
		String selection = RestaurantColumns.RESTAURANT_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(restaurantId) };
		List<Restaurant> items = readItems(selection, selectionArgs, null, null, null);
		if (items != null && items.size() > 0) {
			return items.get(0);
		} else {
			return null;
		}
	}

	@Override
	public void deleteItem(Restaurant item) {
		String selection = RestaurantColumns.RESTAURANT_ID.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.id) };
		deleteItems(selection, selectionArgs);
	}

	@Override
	public void deleteItems() {
		deleteItems(null, null);
	}
	
	public void updateItem(Restaurant item) {
            String selection = RestaurantColumns.RESTAURANT_ID.getName() + "=?";
            String[] selectionArgs = { String.valueOf(item.id) };
            updateItem(createValues(item), selection, selectionArgs);
    }
}
