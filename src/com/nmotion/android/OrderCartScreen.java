package com.nmotion.android;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.nmotion.R;
import com.nmotion.android.adapters.OrderCartScreenAdapter;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.network.NetworkService;
import com.nmotion.android.network.SimpleResult;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.view.RestaurantInfoBlock;
import com.nmotion.android.view.TopPanelLayout;

public class OrderCartScreen extends Activity {

    private ListView listView;
    private OrderCartScreenAdapter adapter;
    DecimalFormat decimalFormat;
    private ArrayList<OrderedMeal> selectedItemForRemove = new ArrayList<OrderedMeal>();
    private TextView total;
    private ArrayList<OrderedMeal> orders;
    private int restaurantId = -1;
    private RestaurantInfoBlock restaurantInfoBlock;
    OrderedMeal oMeal;
    private ViewAnimator deleteAnimator;
    private boolean isDeleteMode;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_order_cart);
        decimalFormat = new DecimalFormat(getString(R.string.txt_price_string));
        ((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_order_cart);
        findViewById(R.id.btn_menu_add_more).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_menu_delete).setVisibility(View.VISIBLE);

        listView = (ListView) findViewById(R.id.list_view);
        orders = App.getInstance().getCache().getOrders();
        adapter = new OrderCartScreenAdapter(getApplicationContext(), orders, onDeleteCheckBoxClick);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        total = (TextView) findViewById(R.id.txt_total);

        deleteAnimator = (ViewAnimator) findViewById(R.id.order_cart_edit_animator);

        if (!orders.isEmpty()) {
            OrderedMeal order = orders.get(0);
            restaurantId = order.restaurant.id;
            restaurantInfoBlock = (RestaurantInfoBlock) findViewById(R.id.restaurantInfoBlock);
            restaurantInfoBlock.bindData(App.getInstance().getCache().getRestaurantById(restaurantId));
            total.setText(getString(R.string.txt_total, App.getInstance().getCache().getTotalPrice()));
            ((TopPanelLayout) findViewById(R.id.top_panel_layout)).updatePanelInfo(restaurantId, false);
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (App.getInstance().getPreferencesManager().isCrash()){
            AppUtils.showDialog(this, "Error", getString(R.string.crash_message)).setOnDismissListener(new OnDismissListener() {                    
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();                        
                }
            });
        }
    }

    @Override
    protected void onResume() {
        adapter.notifyDataSetChanged();        
        total.setText(getString(R.string.txt_total, decimalFormat.format(App.getInstance().getCache().getTotalPrice())));
        super.onResume();
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int index, long id) {
            Intent intent = new Intent(parent.getContext(), MealsOptionsScreen.class);
            intent.putExtra(MealsOptionsScreen.DATA_ORDER_ID, index);
            intent.putExtra(MealsOptionsScreen.EDIT_MODE, true);
            startActivity(intent);
        }
    };

    private boolean isOrderZero() {
        if (App.getInstance().getCache().getTotalPrice() <= 0) {
            Toast.makeText(this, "Amount is not valid", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    public void onOrderClick(View view) {
        if (isOrderZero()) 
            return;
        new SaveOrUpdateOrderTask().execute();
    }

    public void onDeleteClick(View view) {
        if (adapter.isCheckable()) {
            adapter.setCheckableEnable(false);
            ((TextView) findViewById(R.id.btn_menu_delete)).setText(R.string.txt_delete);
        } else {
            adapter.setCheckableEnable(true);
            ((TextView) findViewById(R.id.btn_menu_delete)).setText(R.string.txt_cancel);
        }
        deleteAnimator.showNext();
        adapter.notifyDataSetChanged();
    }

    public void onDeleteDoneClick(View view) {
        for (OrderedMeal order : selectedItemForRemove) {
            App.getInstance().getCache().deleteOrderedMeal(order);
        }
        adapter.notifyDataSetChanged();

        DecimalFormat decimalFormat = new DecimalFormat(getString(R.string.txt_price_string));
        total.setText(getString(R.string.txt_total, decimalFormat.format(App.getInstance().getCache().getTotalPrice())));
        if (isOrderZero()){
            isDeleteMode = true;
            new SaveOrUpdateOrderTask().execute();
        }
    }

    public void onAddMoreClick(View view) {
        Intent intent = null;
        if (restaurantId > 0) {
            intent = new Intent(getApplicationContext(), CategoriesScreen.class);
            intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, orders.size()>0 ? App.getInstance().getCache().getCurrentRestaurantId() : restaurantId);
        } else {
            intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private OnDeleteCheckBoxClick onDeleteCheckBoxClick = new OnDeleteCheckBoxClick() {

        @Override
        public void onDeleteCheckBoxClick(OrderedMeal order, boolean isSelected) {
            if (isSelected) {
                selectedItemForRemove.add(order);
            } else {
                selectedItemForRemove.remove(order);
            }
        }
    };

    public interface OnDeleteCheckBoxClick {
        public void onDeleteCheckBoxClick(OrderedMeal order, boolean isSelected);
    }

    private class SaveOrUpdateOrderTask extends AsyncTask<Void, Void, SimpleResult> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = AppUtils.showProgressDialog(OrderCartScreen.this,
                    getString(R.string.txt_get_order_details), false);
            super.onPreExecute();
        }

        @Override
        protected SimpleResult doInBackground(Void... params) {
            try {
                OrderDetails orderDetails = App.getInstance().getCache().getOrderDetails();
                if (isDeleteMode && orderDetails!=null){
                    App.getInstance().getNetworkService().updateOrderStatus(String.valueOf(orderDetails.orderId), String.valueOf(NetworkService.ORDER_STATUS_CANCELLED), OrderCartScreen.this);
                    isDeleteMode=false;
                    App.getInstance().getCache().deleteOrderDetails();
                    return new SimpleResult(true);
                }else if (orderDetails != null && orderDetails.orderStatus == NetworkService.ORDER_STATUS_NEW_PAYMENT) {
                    orderDetails = App.getInstance().getNetworkService()
                            .updateOrderRequest(orderDetails, orders, OrderCartScreen.this);
                } else {
                    orderDetails = App.getInstance().getNetworkService()
                            .saveOrderRequest(String.valueOf(restaurantId), orders, OrderCartScreen.this);
                }
                isDeleteMode=false;
                App.getInstance().getCache().setOrderDetails(orderDetails);
            } catch (NetworkException e) {
                if (e.getExceptionCode() > 0) {
                    return new SimpleResult(e.getExceptionCode(), e.getExceptionCode()>=NetworkException.EXCEPTION_CODE_MEAL_NOT_AVAILABLE_1 && e.getExceptionCode()<=NetworkException.EXCEPTION_CODE_MEAL_NOT_AVAILABLE_4 ? e.error : e.getMessage());
                } else if (e.getHttpCode() > 0) {
                    return new SimpleResult(e.getHttpCode(), e.getMessage());
                } else {
                    return new SimpleResult(e.getStatusCode(), e.error);
                }
            }
            return new SimpleResult(true);
        }

        @Override
        protected void onPostExecute(final SimpleResult result) {
            progressDialog.dismiss();
            switch (result.getCode()) {
                case SimpleResult.STATUSE_DONE_OK:
                    Intent intent = new Intent(getApplicationContext(), OrderDetailsScreen.class);
                    startActivity(intent);
                    break;

                case NetworkException.EXCEPTION_CODE_ORDER_BEING_PAID:
                    AppUtils.showDialog(OrderCartScreen.this, null, getString(R.string.txt_order_being_paid));
                    break;

                case NetworkException.EXCEPTION_CODE_ORDER_ALREADY_HAS_BEEN_PAID:
                    AppUtils.showDialog(OrderCartScreen.this, null, getString(R.string.txt_order_has_been_paid))
                            .setOnDismissListener(new OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent intent = new Intent(getApplicationContext(), CategoriesScreen.class);
                                    intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, restaurantId);
                                    App.getInstance().getCache().deleteOrderedMeals();
                                    App.getInstance().getCache().deleteOrderDetails();
                                    startActivity(intent);
                                }
                            });
                    break;
                case NetworkException.EXCEPTION_CODE_MEAL_NOT_AVAILABLE_1:
                case NetworkException.EXCEPTION_CODE_MEAL_NOT_AVAILABLE_2:
                case NetworkException.EXCEPTION_CODE_MEAL_NOT_AVAILABLE_3:
                case NetworkException.EXCEPTION_CODE_MEAL_NOT_AVAILABLE_4:
                    if (result.getMessage()==null){
                        String message = getString(R.string.txt_getting_data_from_server_error);
                        if (result.getMessage() != null) {
                            message = result.getMessage();
                        }
                        AppUtils.showDialog(OrderCartScreen.this, null, message);
                        break;
                    }
                    for (OrderedMeal meal : orders) {
                        if (meal.meal.id == Long.parseLong(result.getMessage())){
                            oMeal = meal;
                            break;
                        }
                    }
                    AppUtils.showContinueCancelDialog(OrderCartScreen.this, oMeal.meal.name+" "+getString(R.string.meal_not_available_message), new OnClickListener() {                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                    App.getInstance().getCache().deleteOrderedMeal(oMeal);
                                    orders.remove(oMeal);
                                    adapter.notifyDataSetChanged();
                                    total.setText(getString(R.string.txt_total, decimalFormat.format(App.getInstance().getCache().getTotalPrice())));
                        }
                    });
                break;
                case NetworkException.HTTP_CODE_PRECONDITION_FAILED:
                    App.getInstance().getPreferencesManager().checkOut(App.getInstance().getNetworkService().getCurrentUser().eMail, restaurantId);
                    String message = getString(R.string.txt_getting_data_from_server_error);
                    if (result.getMessage() != null) {
                            message = result.getMessage();
                    }
                    if (message.toLowerCase().contains("this value is not valid")
                            || message.toLowerCase().contains("one meal at least"))
                        message = getString(R.string.options_ingrids_deleted_messaege);
                    AppUtils.showDialog(OrderCartScreen.this, null, message, new OnClickListener() {                                
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                           Intent intent = new Intent(OrderCartScreen.this, RestaurantInfoScreen.class);
                           intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, App.getInstance().getCache().getCurrentRestaurantId());
                           App.getInstance().getCache().deleteOrderedMeals();
                           App.getInstance().getCache().deleteOrderDetails();
                           startActivity(intent);
                        }
                    });
                    break;
                default:
                    message = getString(R.string.txt_getting_data_from_server_error);
                    if (result.getMessage() != null) {
                        message = result.getMessage();
                    }
                    AppUtils.showDialog(OrderCartScreen.this, null, message);
                    break;
            }

        }
    }
}
