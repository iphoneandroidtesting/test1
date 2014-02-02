package com.nmotion.android.core;

import java.util.ArrayList;

import android.util.SparseArray;

import com.nmotion.android.App;
import com.nmotion.android.models.CreditCard;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MenuCategory;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.models.User;

/**
 * Memory cache <br>
 * <br>
 * <b>WARNING: if you want add or change some functionality. Be careful, and duplicate all cache in DB because if application process will closing(this may happens in any time) all memory cache will
 * be deleted after main singleton App.java is terminated </b>
 */
public class Cache {
	private ArrayList<Restaurant> restaurants;
	private ArrayList<MenuCategory> menuCategories;
	private SparseArray<ArrayList<Meal>> menuCategoryMeals;
	private ArrayList<OrderedMeal> orders;
	private OrderDetails orderDetails;
	private CreditCard lastUsedCard;

	public void clear() {
		restaurants = null;
		menuCategories = null;
		menuCategoryMeals = null;
		orders = null;
		lastUsedCard = null;
		orderDetails = null;
		App.getInstance().getPreferencesManager().clearCheckout();
		App.getInstance().getAppDataSource().clearDBCache();
	}
	

	public ArrayList<OrderedMeal> getOrders() {
		if (orders == null)
			orders = App.getInstance().getAppDataSource().getOrderedMeals();
		return orders;
	}

	public void deleteOrderedMeals() {
		getOrders().clear();
		App.getInstance().getAppDataSource().deleteAllOrderedMeals();
	}

	public float getTotalPrice() {
		float total = 0.0f;
		if (getOrders() != null) {
			for (OrderedMeal order : getOrders()) {
				if (order.meal.isOptionAvailible()) {
					total += order.quantity * (order.meal.getMealOptionDiscountPrice()<=0 ? order.meal.getMealOptionPrice() : order.meal.getMealOptionDiscountPrice());
				} else {
					total += order.quantity * (order.meal.discountPriceIncludingTax<=0 ? order.meal.priceIncludingTax : order.meal.discountPriceIncludingTax);
				}

				if (order.meal.isExtraAvailible()) {
					total += order.quantity * (order.meal.getMealExtraIngridientsDiscountPriceSum()<=0 ? order.meal.getMealExtraIngridientsPriceSum() : order.meal.getMealExtraIngridientsDiscountPriceSum());
				}
			}
		}
		return total;
	}

	public void replaceAllOrders(ArrayList<OrderedMeal> orders) {
		deleteOrderedMeals();
		App.getInstance().getAppDataSource().addOrderedMeals(orders);
		getOrders().addAll(orders);
	}

	public void addOrder(OrderedMeal orderedMeal) {
		getOrders().add(orderedMeal);
		App.getInstance().getAppDataSource().addOrderedMeal(orderedMeal);
	}

	public ArrayList<Restaurant> getRestaurants() {
		if (restaurants == null)
			restaurants = App.getInstance().getAppDataSource().getRestaurants();
		return restaurants;
	}
	
//	public ArrayList<Restaurant> getRestaurantsForcely() {
//            restaurants = App.getInstance().getAppDataSource().getRestaurants();
//            return restaurants;
//        }
	
	public Restaurant getRestaurantById(int restaurantId){
	    return getRestaurantById(restaurantId, false);
	}

	public Restaurant getRestaurantById(int restaurantId, boolean isGetFromDBForcely) {
		Restaurant restaurant = null;
		if (isGetFromDBForcely){
		    restaurant = App.getInstance().getAppDataSource().getRestaurantById(restaurantId);
		}
		if (getRestaurants() != null) {
			for (Restaurant item : restaurants) {
				if (item.id == restaurantId) {
				    if (isGetFromDBForcely)
				        item = restaurant;
				    else
					restaurant = item;
				    break;
				}
			}
		}
		return restaurant;
	}

	public void setRestaurants(ArrayList<Restaurant> restaurants) {
		getRestaurants().clear();
		getRestaurants().addAll(restaurants);
		App.getInstance().getAppDataSource().setRestaurants(restaurants);
	}

	public ArrayList<MenuCategory> getMenuCategories() {
		if (menuCategories == null)
			menuCategories = App.getInstance().getAppDataSource().getMenuCategories();
		return menuCategories;
	}

	public MenuCategory getMenuCategoryById(int categoryId) {
		MenuCategory category = null;
		if (getMenuCategories() != null) {
			for (MenuCategory item : getMenuCategories()) {
				if (item.id == categoryId) {
					category = item;
				}
			}
		}
		return category;
	}

	public void setMenuCategories(ArrayList<MenuCategory> categories) {
		getMenuCategories().clear();
		getMenuCategories().addAll(categories);
		App.getInstance().getAppDataSource().setMenuCategories(categories);
	}

	public SparseArray<ArrayList<Meal>> getMenuCategoriesAndMeals() {
		if (menuCategoryMeals == null)
			menuCategoryMeals = App.getInstance().getAppDataSource().getMenuCategoryMeals();
		return menuCategoryMeals;
	}

	public ArrayList<Meal> getMenuCategoryMeals(int menuCategoryId) {
		return getMenuCategoriesAndMeals().get(menuCategoryId, new ArrayList<Meal>());
	}

	public void clearMenuCategoryMeals() {
		menuCategoryMeals.clear();
	}

	public Meal getMeal(int menuCategoryId, int mealId) {
		ArrayList<Meal> mealsInCategory = getMenuCategoryMeals(menuCategoryId);
		Meal meal = null;
		if (mealsInCategory != null) {
			for (Meal item : mealsInCategory) {
				if (item.id == mealId) {
					meal = item;
					break;
				}
			}
		}
		return meal;
	}

	public void setMenuCategoryMeals(int categoryId, ArrayList<Meal> menuCategoryMeals) {
		getMenuCategoriesAndMeals().put(categoryId, menuCategoryMeals);
		App.getInstance().getAppDataSource().setMealsInCategory(categoryId, menuCategoryMeals);
	}

	public void storeLastUsedCard(CreditCard creditCard) {
		this.lastUsedCard = creditCard;
	}

	public CreditCard getLastUsedCard() {
		return this.lastUsedCard;
	}

	public int getCurrentRestaurantId() {
		if (getOrders() != null && getOrders().size() > 0) {
			return getOrders().get(0).restaurant.id;
		}
		return -1;
	}

	public void setMeal(Meal meal) {
		ArrayList<Meal> mealsInCategory = getMenuCategoryMeals(meal.menuCategoryId);
		if (mealsInCategory != null) {
			for (int i = 0; i < mealsInCategory.size(); i++) {
				Meal item = mealsInCategory.get(i);
				if (item.id == meal.id) {
					// TODO: hack. need to delete when server will return thumb url in getting meal description request
					meal.imageThumb = item.imageThumb;
					mealsInCategory.set(i, meal);
					break;
				}
			}
		}
		App.getInstance().getAppDataSource().updateMeal(meal);
	}

	public void deleteOrderedMeal(OrderedMeal order) {
		getOrders().remove(order);
		App.getInstance().getAppDataSource().deleteOrderedMeal(order);
	}

	public void updateOrderedMeal(OrderedMeal orderedMeal) {
		App.getInstance().getAppDataSource().updateOrderedMeal(orderedMeal);
	}

	public void setOrderDetails(OrderDetails orderDetails) {
		this.orderDetails = orderDetails;
		App.getInstance().getAppDataSource().setOrderDetails(this.orderDetails);
	}

	public OrderDetails getOrderDetails() {
		if (orderDetails == null) {
			orderDetails = App.getInstance().getAppDataSource().getOrderDetails();
		}
		return orderDetails;
	}

	public void deleteOrderDetails() {
		orderDetails = null;
		App.getInstance().getAppDataSource().deleteOrderDetails();
	}

	public User getCurrentUser() {
		return App.getInstance().getAppDataSource().getCurrentUser();
	}

	public void addSingleObjectToCache(Object object) {
		App.getInstance().getAppDataSource().addSingleObjectToCache(object);
	}
}
