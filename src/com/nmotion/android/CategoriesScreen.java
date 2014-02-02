package com.nmotion.android;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.nmotion.R;
import com.nmotion.android.adapters.MenuCategoriesScreenAdapter;
import com.nmotion.android.models.MenuCategory;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.network.NetworkService;
import com.nmotion.android.network.SimpleResult;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;

public class CategoriesScreen extends BaseRestaurantScreen {
	private ListView listView;
	private MenuCategoriesScreenAdapter mAdapter;
	private EditText search;
	private int clickedIndex=-1;;
	private ArrayList<MenuCategory> categories;
	ArrayList<OrderedMeal> orders;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		enableSignInButton();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_menu_categories);
		listView = (ListView) findViewById(R.id.list_view);

		categories = App.getInstance().getCache().getMenuCategories();

		mAdapter = new MenuCategoriesScreenAdapter(getApplicationContext(), categories);
		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(itemClickListener);

		search = (EditText) findViewById(R.id.txt_menu_category_search);

		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int length = s.length();
				if (length == 0) {
					download(null);
				} else if (length >= Config.SEARCH_SYMBOLS_LENGTH) {
					download(s.toString());
				}
			}

		});
		download(null);
		orders = App.getInstance().getCache().getOrders();
	}

	private void download(String string) {
		new DownloadMenuCategoriesListTask().execute(String.valueOf(mRestaurantId), string);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int index, long id) {
		        clickedIndex=index;
		        if (orders!=null && orders.size()>0 && App.getInstance().getPreferencesManager().isCheckedIn(App.getInstance().getNetworkService().getCurrentUser().eMail, mRestaurantId))
		            new SaveOrUpdateOrderTask().execute();
		        else{
        			Intent intent = new Intent(parent.getContext(), CategoryMealsScreen.class);
        			intent.putExtra(CategoryMealsScreen.DATA_CATEGORY_ID, categories.get(index).id);
        			intent.putExtra(CategoryMealsScreen.DATA_RESTAURANT_ID, mRestaurantId);
        			startActivity(intent);
		        }
		}
	};
	
	private class SaveOrUpdateOrderTask extends AsyncTask<Void, Void, SimpleResult> {
            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                    progressDialog = AppUtils.showProgressDialog(CategoriesScreen.this, getString(R.string.txt_get_order_details), false);
                    super.onPreExecute();
            }
            

            @Override
            protected SimpleResult doInBackground(Void... params) {
                    try {                            
                            OrderDetails orderDetails = App.getInstance().getCache().getOrderDetails();
                            if (orderDetails != null && orderDetails.orderStatus == NetworkService.ORDER_STATUS_NEW_PAYMENT) {
                                    orderDetails = App.getInstance().getNetworkService().updateOrderRequest(orderDetails, orders, CategoriesScreen.this);
                            } else {
                                    orderDetails = App.getInstance().getNetworkService().saveOrderRequest(String.valueOf(mRestaurantId), orders, CategoriesScreen.this);
                            }
                            App.getInstance().getCache().setOrderDetails(orderDetails);
                    } catch (NetworkException e) {
                        if (e.getExceptionCode() > 0) {
                            return new SimpleResult(e.getExceptionCode(), e.getMessage());
                        } else if (e.getHttpCode() > 0) {
                            return new SimpleResult(e.getHttpCode(), e.getMessage());
                        } else {
                            return new SimpleResult(e.getStatusCode(), e.error);
                        }
                    }
                    return new SimpleResult(true);
            }

            @Override
            protected void onPostExecute(SimpleResult result) {
                    progressDialog.dismiss();
                    switch (result.getCode()) {
                    case SimpleResult.STATUSE_DONE_OK:
                            Intent intent = new Intent(CategoriesScreen.this, CategoryMealsScreen.class);
                            intent.putExtra(CategoryMealsScreen.DATA_CATEGORY_ID, categories.get(clickedIndex).id);
                            intent.putExtra(CategoryMealsScreen.DATA_RESTAURANT_ID, mRestaurantId);
                            startActivity(intent);
                            break;
                    case NetworkException.EXCEPTION_CODE_ORDER_BEING_PAID:
                            AppUtils.showDialog(CategoriesScreen.this, null, getString(R.string.txt_order_being_paid));
                            break;
                    case NetworkException.EXCEPTION_CODE_ORDER_ALREADY_HAS_BEEN_PAID:
                            AppUtils.showDialog(CategoriesScreen.this, null, getString(R.string.txt_order_has_been_paid));
                            App.getInstance().getCache().deleteOrderedMeals();
                            App.getInstance().getCache().deleteOrderDetails();
                            break;
                    case NetworkException.HTTP_CODE_PRECONDITION_FAILED:                        
                        App.getInstance().getPreferencesManager().checkOut(App.getInstance().getNetworkService().getCurrentUser().eMail, mRestaurantId);String message = getString(R.string.txt_getting_data_from_server_error);
                    if (result.getMessage() != null) {
                            message = result.getMessage();
                    }
                    if (message.toLowerCase().contains("this value is not valid") 
                            || message.toLowerCase().contains("one meal at least"))
                        message = getString(R.string.options_ingrids_deleted_messaege);
                    AppUtils.showDialog(CategoriesScreen.this, null, message, new OnClickListener() {                                
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                           Intent intent = new Intent(CategoriesScreen.this, RestaurantInfoScreen.class);
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
                            AppUtils.showDialog(CategoriesScreen.this, null, message);
                            break;
                    }

            }
    }

	private class DownloadMenuCategoriesListTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
		        try{
		            progressDialog = AppUtils.showProgressDialog(CategoriesScreen.this, R.string.txt_getting_menu_categories, false);
		        }catch(Exception e){
		            
		        }
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				ArrayList<MenuCategory> categories = App.getInstance().getNetworkService().getMenuCategories(params[0], params[1], CategoriesScreen.this);
				App.getInstance().getCache().setMenuCategories(categories);
			} catch (NetworkException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		    try{
		        progressDialog.dismiss();
                    }catch(Exception e){
                        
                    }
			
			mAdapter.notifyDataSetChanged();
			if (categories.isEmpty()) {
				findViewById(R.id.txt_menu_categories_empty).setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			} else {
				findViewById(R.id.txt_menu_categories_empty).setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}
		}
	}
}
