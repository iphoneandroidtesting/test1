package com.nmotion.android.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.nmotion.android.database.CreditCardsDBSchema.CreditCardsColumnInt;
import com.nmotion.android.database.CreditCardsDBSchema.CreditCardsColumns;
import com.nmotion.android.models.CreditCard;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MenuCategory;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.models.User;

public class AppDataSource {
	private SQLiteDatabase database;
	private AppSQLiteHelper dbHelper;

	public AppDataSource(Context context) {
		dbHelper = new AppSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public void updatetRestaurant(Restaurant item) {
            open();
            RestaurantsDAO restaurantsDAO = new RestaurantsDAO(database);
            restaurantsDAO.updateItem(item);
            close();
	}

	public void setRestaurants(ArrayList<Restaurant> items) {
		open();
		RestaurantsDAO restaurantsDAO = new RestaurantsDAO(database);
		restaurantsDAO.deleteItems();
		restaurantsDAO.storeItems(items);
		close();
	}

	public ArrayList<Restaurant> getRestaurants() {
		open();
		ArrayList<Restaurant> items = (ArrayList<Restaurant>) new RestaurantsDAO(database).readItems();
		close();
		return items;
	}
	
	public Restaurant getRestaurantById(int id){
	        open();
                Restaurant item = new RestaurantsDAO(database).readItemById(id);
                close();
                return item;
	}

	public ArrayList<MenuCategory> getMenuCategories() {
		open();
		ArrayList<MenuCategory> items = (ArrayList<MenuCategory>) new MenuCategoriesDAO(database).readItems();
		close();
		return items;
	}

	public void setMenuCategories(ArrayList<MenuCategory> categories) {
		open();
		MenuCategoriesDAO categoriesDAO = new MenuCategoriesDAO(database);
		categoriesDAO.deleteItems();
		categoriesDAO.storeItems(categories);
		close();
	}

	public void setMealsInCategory(int categoryId, ArrayList<Meal> menuCategoryMeals) {
		open();
		new MealDAO(database).storeItems(menuCategoryMeals);
		close();
	}

	public void addOrderedMeal(OrderedMeal orderedMeal) {
		open();
		new OrderedMealDAO(database).storeItem(orderedMeal);
		close();
	}
	
	public void addOrderedMeals(List<OrderedMeal> items) {
		open();
		new OrderedMealDAO(database).storeItems(items);
		close();
	}
	
	public ArrayList<OrderedMeal> getOrderedMeals() {
		open();
		ArrayList<OrderedMeal> items = (ArrayList<OrderedMeal>) new OrderedMealDAO(database).readItems();
		close();
		return items;
	}

	public void deleteOrderedMeal(OrderedMeal order) {
		open();
		new OrderedMealDAO(database).deleteItem(order);
		close();
	}

	public void deleteAllOrderedMeals() {
		open();
		new OrderedMealDAO(database).deleteItems();
		close();
	}

	public void updateOrderedMeal(OrderedMeal order) {
		open();
		new OrderedMealDAO(database).updateItem(order);
		close();
	}

	public void setOrderDetails(OrderDetails item) {
		open();
		new SingleObjectsDAO(database).storeItem(item);
		close();
	}

	public OrderDetails getOrderDetails() {
		open();
		OrderDetails details = new SingleObjectsDAO(database).readOrderDetails();
		close();
		return details;
	}

	public void deleteOrderDetails() {
		open();
		new SingleObjectsDAO(database).deleteItems();
		close();
	}

	public void deleteCachedSingleObject(Object item) {
		open();
		new SingleObjectsDAO(database).deleteItem(item);
		close();
	}

	public User getCurrentUser() {
		open();
		User user = new SingleObjectsDAO(database).readCurrentUser();
		close();
		return user;
	}

	public void addSingleObjectToCache(Object item) {
		open();
		new SingleObjectsDAO(database).storeItem(item);
		close();
	}

	public void addCard(CreditCard creditCard) {
		ContentValues values = new ContentValues();
		values.put(CreditCardsColumns.USER_ID.getName(), creditCard.user);
		values.put(CreditCardsColumns.USER_NAME.getName(), creditCard.user);
		values.put(CreditCardsColumns.CARD_TITLE.getName(), creditCard.title);
		values.put(CreditCardsColumns.CARD_NUMBER.getName(), creditCard.number);
		values.put(CreditCardsColumns.CARD_ID.getName(), creditCard.card_id);
		database.insert(Tables.CREDIT_CARDS_TABLE, null, values);
	}

	public void addCards(ArrayList<CreditCard> creditCards) {
		for (CreditCard creditCard : creditCards) {
			addCard(creditCard);
		}
	}

	public boolean deleteCard(CreditCard creditCard) {
		String where = CreditCardsColumns.CARD_ID.getName() + "=?";
		String[] whereArgs = { String.valueOf(creditCard.card_id) };
		return database.delete(Tables.CREDIT_CARDS_TABLE, where, whereArgs) > 0;
	}

	public boolean updateCard(CreditCard creditCard) {
		String where = CreditCardsColumns.CARD_ID + "=?";
		String[] whereArgs = { String.valueOf(creditCard.card_id) };
		ContentValues values = new ContentValues();
		values.put(CreditCardsColumns.CARD_TITLE.getName(), creditCard.title);
		return database.update(Tables.CREDIT_CARDS_TABLE, values, where, whereArgs) > 0;
	}

	public void deleteAllCards() {
		database.delete(Tables.CREDIT_CARDS_TABLE, null, null);
	}

	public ArrayList<CreditCard> getAllCreditCard(String userName) {
		ArrayList<CreditCard> creditCards = new ArrayList<CreditCard>();
		String where = CreditCardsColumns.USER_NAME + "=?";
		String[] whereArgs = { String.valueOf(userName) };
		Cursor cursor = database.query(Tables.CREDIT_CARDS_TABLE, null, where, whereArgs, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CreditCard creditCard = cursorToCreditCard(cursor);
			creditCards.add(creditCard);
			cursor.moveToNext();
		}
		cursor.close();
		return creditCards;
	}

	private CreditCard cursorToCreditCard(Cursor cursor) {
		CreditCard creditCard = new CreditCard();
		creditCard.user = cursor.getString(CreditCardsColumnInt.USER_NAME);
		creditCard.title = cursor.getString(CreditCardsColumnInt.CARD_TITLE);
		creditCard.number = cursor.getString(CreditCardsColumnInt.CARD_NUMBER);
		creditCard.card_id = cursor.getString(CreditCardsColumnInt.CARD_ID);

		return creditCard;
	}

	public void updateMeal(Meal meal) {
		open();
		MealDAO mealDAO = new MealDAO(database);
		mealDAO.updateItem(meal);
		close();
	}
	
	public void updateRestaurant(Restaurant restaurant) {
	    open();
	    RestaurantsDAO restaurantDAO = new RestaurantsDAO(database);
	    restaurantDAO.updateItem(restaurant);
	    close();
	}

	public SparseArray<ArrayList<Meal>> getMenuCategoryMeals() {
		SparseArray<ArrayList<Meal>> result = new SparseArray<ArrayList<Meal>>();
		open();
		MealDAO mealDAO = new MealDAO(database);
		ArrayList<Meal> allMeals = (ArrayList<Meal>) mealDAO.readItems();
		if (allMeals != null && allMeals.size() > 0) {
			while (allMeals.size() > 0) {
				ArrayList<Meal> categoryMeals = new ArrayList<Meal>();
				int categoryId = allMeals.get(0).menuCategoryId;
				for (Meal meal : allMeals) {
					if (meal.menuCategoryId == categoryId) {
						categoryMeals.add(meal);
					}
				}
				result.append(categoryId, categoryMeals);
				allMeals.removeAll(categoryMeals);
			}
		}
		close();
		return result;
	}

	public void clearDBCache() {
		open();
		new RestaurantsDAO(database).deleteItems();
		new MenuCategoriesDAO(database).deleteItems();
		new MealDAO(database).deleteItems();
		new OrderedMealDAO(database).deleteItems();
		new MealOptionsDAO(database).deleteItems();
		new SingleObjectsDAO(database).deleteItems();
		close();
	}
}
