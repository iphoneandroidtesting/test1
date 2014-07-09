package com.nmotion.android.network;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.nmotion.R;
import com.nmotion.android.App;
import com.nmotion.android.core.JSONKeys;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.facebook.Facebook;
import com.nmotion.android.facebook.SessionStore;
import com.nmotion.android.models.FriendOrder;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.MenuCategory;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.models.PastOrder;
import com.nmotion.android.models.PastOrderDetails;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.models.User;
import com.nmotion.android.utils.Config;
import com.nmotion.android.utils.Logger;
import com.nmotion.android.utils.Utils;

public class NetworkService extends HTTPHelper {
    
    public NetworkService(Context context){
        super(context);
    }

    private final static String OPERATION_REGISTER = "users.json";
    private final static String OPERATION_LOGIN = "users/me.json";
    private final static String OPERATION_UPDATE_USER_INFO = "users/%s.json";
    private final static String OPERATION_LINK_USER = "userdevices/%s.json";
    private final static String OPERATION_RESET_PASSWORD = "users/forgot.json";
    private final static String OPERATION_GET_RESTAURANTS = "restaurants/search.json";
    private final static String OPERATION_GET_RESTAURANT = "restaurants/";
    private final static String OPERATION_CHECK_IN = "restaurants/%s/checkin.json";
    private final static String OPERATION_CHECK_OUT = "restaurants/%s/checkout.json";
    private final static String OPERATION_GET_MENU_CATEGORIES = "restaurants/%s/menucategories.json";
    // private final static String OPERATION_GET_MENU_CATEGORY_MEALS =
    // "restaurants/%s/menucategories/%s/meals.json";
    private final static String OPERATION_GET_MENU_CATEGORY_MEALS = "menucategories/%s/meals.json";
    // private final static String OPERATION_GET_MENU_CATEGORY_MEAL_DESCRIPTION
    // = "restaurants/%s/menucategories/%s/meals/%s.json";
    private final static String OPERATION_GET_MENU_CATEGORY_MEAL_DESCRIPTION = "meals/%s.json";

    private final static String OPERATION_GET_ORDER = "orders/%s";
    private final static String OPERATION_SAVE_ORDER = "restaurants/%s/orders.json";
    private final static String OPERATION_UPDATE_ORDER = "orders/%s.json";

    private final static String OPERATION_GET_USER_ORDERS = "users/me/orders";
    private final static String OPERATION_GET_CHECKINED_ORDERS = "restaurants/%s/checkin/orders";

    private static final String OPERATION_GET_CONFIG = "config.json";
    private static final String OPERATION_SEND_TO_MAIL = "orders/%s/sendtoemail.json";
    
    private static final String OPERATION_POST_PAYMENT = "payment.json";
    public static final int ORDER_STATUS_NEW_PAYMENT = 1;
    public static final int ORDER_STATUS_PENDING_PAYMENT = 2;
    public static final int ORDER_STATUS_CANCELLED = 5;

    public static final int COMMON_ERROR = 1;
    public static final int NOT_LOGGED_IN_ERROR = 2;

    private User currentUser;
    Context context;
    private String currentHeader;

    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = App.getInstance().getCache().getCurrentUser();
            if (currentUser == null) {
                currentUser = User.getUnknownUser();
            }
        }
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
        App.getInstance().getCache().addSingleObjectToCache(currentUser);
    }

    public boolean isLoggedIn() {
        return !getCurrentUser().eMail.equals(User.getUnknownUser().eMail);
    }

    private boolean updateHeader() {
        currentHeader = null;
        boolean isFacebookAuth = App.getInstance().getPreferencesManager()
                .getBoolean(PreferencesManager.USER_VIA_FACEBOOK, false);
        if (App.getInstance().getNetworkService().isLoggedIn()) {
            if (isFacebookAuth) {
                Facebook facebook = new Facebook(Config.APP_ID);
                SessionStore.restore(facebook, App.getInstance().getApplicationContext());
                currentHeader = facebook.getAccessToken();
            } else {
                currentHeader = App.getInstance().getPreferencesManager().getString(PreferencesManager.USER_EMAIL, "")
                        + "|"
                        + App.getInstance().getPreferencesManager().getString(PreferencesManager.USER_PASSWORD, "");
            }
        }
        return isFacebookAuth;
    }
    
    Runnable showUpdateWarningDialogRunnable = new Runnable() {
        public void run() {
            AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setMessage(context.getString(R.string.dialog_update_msg));
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.txt_cancel),
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_btn_yes),
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                                    + context.getPackageName()));
                            context.startActivity(goToMarket);
                        }
                    });
            dialog.show();
        }
    };

    private void showUpdateWarningDialog(Context context) {
        this.context=context;
        ((Activity)context).runOnUiThread(showUpdateWarningDialogRunnable);
        
    }

    // return user
    public User register(String email, String password, String fistName, String lastName, Context context)
            throws NetworkException {
        JSONObject object = new JSONObject();
        try {
            object.put("email", email);
            object.put("password", password);
            object.put("firstName", fistName);
            object.put("lastName", lastName);
        } catch (JSONException e) {
            Logger.warning(e.toString());
        }
        JSONObject response = null;
        try {
            response = sendRequestPOST(String.format("%s%s", Config.SERVICE_URI, OPERATION_REGISTER), null, object,
                    null, false);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            setCurrentUser(new User(response.getJSONArray("entries").getJSONObject(0)));
            App.getInstance().getPreferencesManager().setCurrentUserData(email, Utils.md5(password), false);
        } catch (JSONException e1) {
            Logger.warning(e1.toString());
            throw new NetworkException(3, "Wrong server response");
        }
        return getCurrentUser();
    }
    
    /*public void linkUser(Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        ArrayList<String> links = new ArrayList<String>();
        
        links.add(Config.SERVICE_URI_API_PART+String.format(OPERATION_LINK_USER,App.getInstance().getDeviceId())+"; rel=\"owner\"");
        try {
            sendRequestLINK(String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_UPDATE_USER_INFO, currentUser.id)), currentHeader, isFacebookAuth, links);
        } catch (NetworkException e) {
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
    }*/

    public User login(String email, String password, String token, Context context) throws NetworkException {
        JSONObject response = null;
        boolean isFacebookAuth = (token != null);
        try {
            if (isFacebookAuth) {
                response = sendRequestGET(String.format("%s%s", Config.SERVICE_URI, OPERATION_LOGIN), token,
                        isFacebookAuth);
            } else {
                response = sendRequestGET(String.format("%s%s", Config.SERVICE_URI, OPERATION_LOGIN), email + "|"
                        + password, isFacebookAuth);
            }
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            setCurrentUser(new User(response.getJSONArray("entries").getJSONObject(0)));
            App.getInstance().getPreferencesManager().setCurrentUserData(email, password, isFacebookAuth);
        } catch (JSONException e) {
            Logger.warning(e.toString());
            throw new NetworkException(3, "Wrong server response");
        }
        return getCurrentUser();
    }

    public void resetPassword(String email, Context context) throws NetworkException {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            Logger.warning(e.toString());
        }
        //JSONObject response = null;
        try {
            /*response = */sendRequestPOST(String.format("%s%s", Config.SERVICE_URI, OPERATION_RESET_PASSWORD), null, jsonObject,
                    null, false);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
    }

    public User updateUserInfo(String id, String firstName, String lastName, String email, String password, Context context)
            throws NetworkException {
        updateHeader();
        JSONObject object = new JSONObject();
        try {
            object.put("email", email);
            if (password!=null)
                object.put("password", password);
            object.put("firstName", firstName);
            object.put("lastName", lastName);
        } catch (JSONException e) {
            Logger.warning(e.toString());
        }
        JSONObject response = null;
        try {
            response = sendRequestPUT(
                String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_UPDATE_USER_INFO, id)), null, object,
                currentHeader, false);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            setCurrentUser(new User(response.getJSONArray("entries").getJSONObject(0)));
        } catch (JSONException e) {
            Logger.warning(e.toString());
            throw new NetworkException(3, "Wrong server response");
        }
        return getCurrentUser();
    }

    public ArrayList<Restaurant> getRestaurants(String latitude, String longitude, String radius, String search, Context context)
            throws NetworkException {
        OperationParams params = new OperationParams();
        if (latitude != null && longitude != null) {
            String geocodeParam = latitude + "," + longitude + (TextUtils.isEmpty(radius) ? "" : "," + radius);
            params.put("geocode", geocodeParam);
        }

        if (!TextUtils.isEmpty(search)) {
            params.put("query", search);
        }
        JSONObject response = null;
        try {
            response = sendRequestGET(
                String.format("%s%s?%s", Config.SERVICE_URI, OPERATION_GET_RESTAURANTS, params.generateOperationUri()),
                "", false);
        } catch (NetworkException e) {
            System.out.println("GOT EXCEPTION!!!!!!!!!!!!!!1111111ADINADIN");
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        return Restaurant.parse(response.optJSONArray("entries"));
    }

    public Restaurant getRestaurant(long id, Context context) throws NetworkException {
        JSONObject response = null;
        try {
            response = sendRequestGET(
                    String.format("%s%s?%s", Config.SERVICE_URI, OPERATION_GET_RESTAURANT + id + ".json", ""), "", false);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }        
        return new Restaurant(response.optJSONArray("entries").optJSONObject(0));
    }

    public ArrayList<Restaurant> getRestaurants(String latitude, String longitude, String search, Context context)
            throws NetworkException {
        return getRestaurants(latitude, longitude, null, search, context);
    }

    public void checkIn(int restaurantId, String table, int isForce, int isTableEmpty, Context context, int checkInMode, int takeawayPickupTime, String contactPhoneNumber) throws NetworkException {
        if (checkInMode==PreferencesManager.NO_CHECKIN_MODE)
            return;
        boolean isFacebookAuth = updateHeader();
        App.getInstance().getCache().deleteOrderedMeals();
        App.getInstance().getCache().deleteOrderDetails();
        JSONObject object = new JSONObject();
        try {
            if (!table.equals("-1"))
                object.put("table", table);
            else
                object.put("table", "takeaway");
            if (App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.IS_CHEKIN_FACEBOOK, false)) {
                object.put("fbRestaurantCheckin", 1);
            }
            object.put("serviceType", checkInMode);
            if (isForce > 0) {
                object.put("force", 1);
            }
            if (isTableEmpty > 0) {
                object.put("empty", 1);
            }
            if (takeawayPickupTime!=-1)
                object.put("takeawayPickupTime", takeawayPickupTime);
            if(contactPhoneNumber!="")
            	object.put("contactPhoneNumber", contactPhoneNumber);
        } catch (JSONException e) {
            Logger.warning(e.toString());
        }
        try {
            sendRequestPOST(String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_CHECK_IN, restaurantId)), null, object, currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
    }

    public boolean checkOut(int restaurantId, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        JSONObject response = null;
        try {
            response = sendRequestPOST(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_CHECK_OUT, restaurantId)), null,
                    null, currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        return response.optBoolean("success");
    }

    public ArrayList<MenuCategory> getMenuCategories(String restaurantId, String search, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        OperationParams params = new OperationParams();
        if (!TextUtils.isEmpty(search)) {
            params.put("query", search);
        }
        JSONObject response = null;
        try {
            response = sendRequestGET(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_GET_MENU_CATEGORIES, restaurantId)),
                    currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        ArrayList<MenuCategory> results = MenuCategory.parse(Integer.valueOf(restaurantId),
                response.optJSONArray("entries"));
        return results;
    }

    public ArrayList<Meal> getMenuCategoryMeals(String restaurantId, String menuCategoryId, String search, Context context)
            throws NetworkException {
        boolean isFacebookAuth = updateHeader();

        OperationParams params = new OperationParams();
        if (!TextUtils.isEmpty(search)) {
            params.put("query", search);
        }
        JSONObject response = null;
        try {
            response = sendRequestGET(
                    String.format("%s%s", Config.SERVICE_URI,
                            String.format(OPERATION_GET_MENU_CATEGORY_MEALS, /*
                                                                              * restaurantId
                                                                              * ,
                                                                              */menuCategoryId)), currentHeader,
                    isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }        
        ArrayList<Meal> results = Meal.parseJSONToList(Integer.valueOf(menuCategoryId),
                response.optJSONArray("entries"));
        return results;
    }

    public Meal getMenuCategoryMealDescription(String restaurantId, String menuCategoryId, String mealId, Context context)
            throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        JSONObject response = null;
        try {
            response = sendRequestGET(
                    String.format("%s%s", Config.SERVICE_URI,
                            String.format(OPERATION_GET_MENU_CATEGORY_MEAL_DESCRIPTION, /*
                                                                                         * restaurantId
                                                                                         * ,
                                                                                         * menuCategoryId
                                                                                         * ,
                                                                                         */mealId)), currentHeader,
                    isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        return new Meal(Integer.valueOf(menuCategoryId), response.optJSONArray("entries").optJSONObject(0));
    }

    public OrderDetails saveOrderRequest(String restaurantId, ArrayList<OrderedMeal> orders, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        OrderDetails orderDetails = null;
        JSONObject response = null;
        try {
            response = sendRequestPOST(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_SAVE_ORDER, restaurantId)), null,
                    buildOrderJSONForPOST(orders), currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            orderDetails = new OrderDetails(response);
            updateOrderedMeals(orderDetails, response.getJSONArray("entries").getJSONObject(0));
        } catch (JSONException e) {
            Logger.warning(e.toString());
            throw new NetworkException(3, "Wrong server response");
        }
        return orderDetails;
    }

    public OrderDetails updateOrderRequest(OrderDetails orderDetails, ArrayList<OrderedMeal> orders, Context context)
            throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        JSONObject response = null;
        String orderId = String.valueOf(orderDetails.orderId);
        try {
            JSONObject object = buildOrderJSONForPUT(orders);
            object.put("tips", orderDetails.tips);
            response = sendRequestPUT(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_UPDATE_ORDER, orderId)), null,
                    object, currentHeader, isFacebookAuth);
            orderDetails = new OrderDetails(response);
            updateOrderedMeals(orderDetails, response.getJSONArray("entries").getJSONObject(0));
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        } 
        catch (JSONException e) {
            Logger.warning(e.toString());
            orderDetails = null;
            throw new NetworkException(response);
        }      
        return orderDetails;
    }

    public boolean updateOrderStatus(String orderId, String status, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        JSONObject object = new JSONObject();
        //JSONObject response = null;
        try {
            object.put("status", status);
        } catch (Exception e) {
            throw new NetworkException(3, "Wrong request");
        }
        try {
            /*response = */sendRequestPATCH(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_UPDATE_ORDER, orderId)), null,
                    object, currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        return true;
    }

    private JSONObject buildOrderJSONForPOST(ArrayList<OrderedMeal> orders) {
        JSONObject object = new JSONObject();
        JSONArray orderMeals = new JSONArray();
        try {
            for (OrderedMeal order : orders) {
                orderMeals.put(order.toJSONObjectForPOST());
            }
            object.put("orderMeals", orderMeals);
        } catch (Exception e) {
            Logger.warning("!!ORDER NOT FULLY COMPOSED!!: " + e.toString());
        }
        return object;
    }

    private JSONObject buildOrderJSONForPUT(ArrayList<OrderedMeal> orders) {
        JSONObject object = new JSONObject();
        JSONArray orderMeals = new JSONArray();
        try {
            for (OrderedMeal order : orders) {
                orderMeals.put(order.toJSONObjectForPUT());
            }
            object.put("orderMeals", orderMeals);
        } catch (Exception e) {
            Logger.warning("!!ORDER NOT FULLY COMPOSED!!: " + e.toString());
        }
        return object;
    }

    private void updateOrderedMeals(OrderDetails orderDetails, JSONObject response) {
        // TODO: need to reorginize this logic. please. This was hotfix.
        JSONArray orderedMeals = new JSONArray();
        try {
            try {
                orderedMeals = response.getJSONArray("orderMeals");
            } catch (JSONException e) {
                JSONObject orderedMealsObj = response.getJSONObject("orderMeals");
                Iterator<String> it = orderedMealsObj.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    orderedMeals.put(orderedMealsObj.get(key));
                }
            }
            ArrayList<OrderedMeal> basketFromServer = new ArrayList<OrderedMeal>();
            for (int i = 0; i < orderedMeals.length(); i++) {
                JSONObject orderedMealJSON = (JSONObject) orderedMeals.get(i);
                int mealOrderId = orderedMealJSON.optInt(JSONKeys.id, -1);
                OrderedMeal orderedMeal = App.getInstance().getCache().getOrders().get(i);
                orderedMeal.setMealOrderId(mealOrderId);
                basketFromServer.add(orderedMeal);
            }
            App.getInstance().getCache().replaceAllOrders(basketFromServer);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public ArrayList<PastOrder> getCurrentUserOrders(Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        ArrayList<PastOrder> orderListItems = null;
        JSONObject response = null;
        try {
            response = sendRequestGET(String.format("%s%s", Config.SERVICE_URI, OPERATION_GET_USER_ORDERS),
                    currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            orderListItems = PastOrder.parseArrayToList(response);
        } catch (Exception e) {
            throw new NetworkException(3, "Wrong server response");
        }
        return orderListItems;
    }

    public PastOrderDetails getOrder(String orderId, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        PastOrderDetails result = null;
        JSONObject response = null;
        try {
            response = sendRequestGET(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_GET_ORDER, orderId)), currentHeader,
                    isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            result = new PastOrderDetails(response);
        } catch (Exception e) {
            throw new NetworkException(3, "Wrong server response");
        }
        return result;
    }

    public ArrayList<FriendOrder> getCheckinedOrders(String restaurantId, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        ArrayList<FriendOrder> result = null;
        JSONObject response = null;
        try {
            response = sendRequestGET(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_GET_CHECKINED_ORDERS, restaurantId)),
                    currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            result = FriendOrder.parseArrayToList(response);
        } catch (Exception e) {
            throw new NetworkException(3, "Wrong server response");
        }
        return result;
    }

    public boolean requestAppConfig(Context context) throws NetworkException {
        JSONObject response = null;
        try {
            response = sendRequestGET(String.format("%s%s", Config.SERVICE_URI, OPERATION_GET_CONFIG), null,
                    false);
            JSONArray entries = response.optJSONArray("entries");
            for (int i = 0; i < entries.length(); i++) {
                String name = entries.getJSONObject(i).optString(JSONKeys.name);
                int value = entries.getJSONObject(i).optInt(JSONKeys.value);
                App.getInstance().getPreferencesManager().setInt(name, value);
            }
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        } catch (JSONException e) {
            Logger.warning(e.toString());
            throw new NetworkException(3, "Wrong server response");
        }
        return true;
    }

    public boolean sendOrderToMail(String orderId, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        //JSONObject response = null;
        try {
            /*response = */sendRequestPOST(String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_SEND_TO_MAIL, orderId)),
                    currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw new NetworkException(3, "Wrong server response");
        }
        return true;
    }

    public OrderDetails requestLinkOrders(String orderId, String[] linkedOrders, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        OrderDetails result = null;
        ArrayList<String> links = new ArrayList<String>(linkedOrders.length);
        for (int i = 0; i < linkedOrders.length; i++) {
            links.add(TextUtils.concat(linkedOrders[i], "; rel=\"slave\"").toString());
        }
        JSONObject response = null;
        try {
            response = sendRequestLINK(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_UPDATE_ORDER, orderId)),
                    currentHeader, isFacebookAuth, links);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            result = new OrderDetails(response);
        } catch (JSONException e) {
            Logger.warning(e.toString());
            throw new NetworkException(3, "Wrong server response");
        }
        return result;
    }

    public OrderDetails requestUnLinkOrders(String orderId, String[] unLinkedOrders, Context context) throws NetworkException {
        boolean isFacebookAuth = updateHeader();
        OrderDetails result = null;
        ArrayList<String> links = new ArrayList<String>(unLinkedOrders.length);
        for (int i = 0; i < unLinkedOrders.length; i++) {
            links.add(TextUtils.concat(unLinkedOrders[i], "; rel=\"slave\"").toString());
        }
        JSONObject response = null;
        try {
            response = sendRequestUNLINK(
                    String.format("%s%s", Config.SERVICE_URI, String.format(OPERATION_UPDATE_ORDER, orderId)),
                    currentHeader, isFacebookAuth, links);
        } catch (NetworkException e) {
            //e = new NetworkException(response);
            if (e.responseCode==NetworkException.HTTP_CODE_UPDATE)
                showUpdateWarningDialog(context);
            throw e;
        }
        try {
            result = new OrderDetails(response);
        } catch (Exception e) {
            throw new NetworkException(3, "Wrong server response");
        }
        return result;
    }

    public void logOut() {
        currentUser = null;
        App.getInstance().getAppDataSource().deleteCachedSingleObject(User.class.getName());
    }
    
    public boolean postRoomPayment(String orderId, Context context) throws NetworkException {
    	boolean isFacebookAuth = updateHeader();
    	JSONObject object = new JSONObject();
    	JSONObject response;
    	System.out.println("Trying to post payment");
    	try {
    		if(!orderId.equals("") && orderId.matches("\\d+"))
    			object.put("orderId", orderId);
        } catch (JSONException e) {
            Logger.warning(e.toString());
        }
    	
    	try {
            response = sendRequestPOST(String.format("%s%s", Config.SERVICE_URI, OPERATION_POST_PAYMENT), null, object, currentHeader, isFacebookAuth);
        } catch (NetworkException e) {
            throw e;
        }
    	
    	try {
    		
    		System.out.println(response.toString());
    		System.out.println(response.get("success"));
    		
    		return (Boolean) response.get("success");
    	} catch (Exception e) {
    		return false;
    	}
    	
    }
    

}
