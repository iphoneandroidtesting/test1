package com.nmotion.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.models.PastOrderDetails;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.view.RestaurantInfoBlock;

public class PastOrderDetailsScreen extends Activity {
	private String orderId;
	private String currency;
	private TextView txtProductTotal;
	private TextView txtSalesTax;
	private TextView txtDiscount;
	private TextView txtTip;
	private TextView txtOrdersTotal;
	private float productTotal;
	private float total;
	private float discount;
	private float salesTax;
	private float tips;
	private PastOrderDetails pastOrder;
	private LinearLayout mealsContainer;
	private TextView txtOrderTitle;
	private LayoutInflater inflater;
	private RestaurantInfoBlock restaurantInfoBlock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.past_order_details_screen);
		if (getIntent() != null && getIntent().hasExtra("data")) {
			orderId = getIntent().getStringExtra("data");
			new GetPastOrderDetailTask().execute(orderId);
			currency = getResources().getString(R.string.txt_dkk);

			((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_order_details);

			findViewById(R.id.btn_menu_list).setVisibility(View.VISIBLE);

			txtOrderTitle = (TextView) findViewById(R.id.txt_your_order);

			txtProductTotal = (TextView) findViewById(R.id.txt_product_total);
			txtSalesTax = (TextView) findViewById(R.id.txt_sales_tax);
			txtDiscount = (TextView) findViewById(R.id.txt_discount);
			txtTip = (TextView) findViewById(R.id.txt_tip);
			txtOrdersTotal = (TextView) findViewById(R.id.txt_your_order_total);

			mealsContainer = (LinearLayout) findViewById(R.id.meals_container);

		} else {
			finish();
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

	private void createMealsList(ArrayList<OrderedMeal> orders) {
		inflater = LayoutInflater.from(this);
		for (int i = 0; i < orders.size(); i++) {
			mealsContainer.addView(getOrderItemView(orders.get(i)));
		}
	}
	
	private View getOrderItemView(OrderedMeal order) {
		View itemView = inflater.inflate(R.layout.past_order_item_layout, null);
		((TextView) itemView.findViewById(R.id.txt_meal_name)).setText(order.meal.name);
		TextView priceWithTax = ((TextView) itemView.findViewById(R.id.txt_meal_price));
		if (order.meal.isOptionAvailible()) {
		    System.out.println("RESULT ingrids sum = "+order.meal.getMealExtraIngridientsPriceSum());
		    System.out.println("RESULT option = "+order.meal.getMealOptionPrice());
		    priceWithTax.setText(TextUtils.concat(String.valueOf(order.quantity), ", ", currency, String.format("%.2f",(order.meal.getMealExtraIngridientsPriceSum() + order.meal.getMealOptionPrice()) * (double) order.quantity)));
                }else                    
                    priceWithTax.setText(TextUtils.concat(String.valueOf(order.quantity), ", ", currency, String.valueOf(getMealOneOrderPrice(order))));
		TextView priceWithTaxAndDiscount = ((TextView) itemView.findViewById(R.id.txt_meal_price_discount));
		if (order.meal.priceIncludingTax!=order.meal.discountPriceIncludingTax && order.meal.discountPriceIncludingTax>0){
		    priceWithTax.setPaintFlags(priceWithTax.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		    if (order.meal.isOptionAvailible()) {
		        System.out.println("RESULT ingrids sum = "+order.meal.getMealExtraIngridientsPriceSum());
	                    System.out.println("RESULT option = "+order.meal.getMealOptionPrice());
		        priceWithTaxAndDiscount.setText(TextUtils.concat(String.valueOf(order.quantity), ", ", currency, String.format("%.2f",(order.meal.getMealExtraIngridientsDiscountPriceSum() + order.meal.getMealOptionDiscountPrice()) * (double) order.quantity)));
		    }
		    else
		        priceWithTaxAndDiscount.setText(TextUtils.concat(String.valueOf(order.quantity), ", ", currency, String.valueOf(getMealOneOrderPriceWithDiscount(order))));
		}else
		    priceWithTaxAndDiscount.setVisibility(View.GONE);
		return itemView;
	}

	private void bindRestaurantData() {
		if (pastOrder != null && pastOrder.subOrders != null && pastOrder.subOrders.size() > 0) {
			OrderedMeal order = pastOrder.subOrders.get(0);
			restaurantInfoBlock = (RestaurantInfoBlock) findViewById(R.id.restaurantInfoBlock);
			restaurantInfoBlock.bindData(order.restaurant);
		}
	}

	public float getTotalPrice(ArrayList<OrderedMeal> orders) {
		float total = 0.0f;
		if (orders != null) {
			for (OrderedMeal order : orders) {
				total += getMealOneOrderPrice(order);
			}
		}
		return total;
	}

	private float getMealOneOrderPrice(OrderedMeal order) {
		float orderPrice = 0.00f;
		if (order.meal.isOptionAvailible()) {
			orderPrice += order.quantity * order.meal.getMealOptionPrice();
		} else {
			orderPrice += order.quantity * order.meal.priceIncludingTax;
		}

		if (order.meal.isExtraAvailible()) {
			orderPrice += order.quantity * order.meal.getMealExtraIngridientsPriceSum();
		}
		return orderPrice;
	}
	
	private float getMealOneOrderPriceWithDiscount(OrderedMeal order) {
            float orderPrice = 0.00f;
            if (order.meal.isOptionAvailible()) {
                    orderPrice += order.quantity * order.meal.getMealOptionPrice();
            } else {
                    orderPrice += order.quantity * order.meal.discountPriceIncludingTax;
            }

            if (order.meal.isExtraAvailible()) {
                    orderPrice += order.quantity * order.meal.getMealExtraIngridientsPriceSum();
            }
            return orderPrice;
    }

	private void countOrderValues() {
		productTotal = pastOrder.productTotal;
		discount = -pastOrder.orderDiscount;
		salesTax = pastOrder.salesTax;
		tips = pastOrder.tips;
		total = pastOrder.consolidatedOrderTotal;
	}

	private void updateTextFields() {

		txtProductTotal.setText(currency.concat(String.format("%.2f", productTotal)));
		txtSalesTax.setText(currency.concat(String.format("%.2f", salesTax)));
		txtDiscount.setText(currency.concat(String.format("%.2f", discount)));
		txtTip.setText(currency.concat(String.format("%.2f", tips)));
		txtOrdersTotal.setText(currency.concat(String.format("%.2f", total)));

		txtOrderTitle.setText(String.format(getString(R.string.txt_your_order_details), String.valueOf(orderId), DateFormat.format("dd/MM/yy", pastOrder.createdAt)));
	}

	private void updateUi(PastOrderDetails order) {
		this.pastOrder = order;
		countOrderValues();
		updateTextFields();
	}

	private class GetPastOrderDetailTask extends AsyncTask<String, Void, PastOrderDetails> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(PastOrderDetailsScreen.this, getString(R.string.txt_get_order_details), false);
			super.onPreExecute();
		}

		@Override
		protected PastOrderDetails doInBackground(String... params) {
			String orderId = params[0];
			PastOrderDetails result = null;
			try {
				result = App.getInstance().getNetworkService().getOrder(orderId, PastOrderDetailsScreen.this);
			} catch (NetworkException e) {
			}
			return result;
		}

		@Override
		protected void onPostExecute(PastOrderDetails result) {
			progressDialog.dismiss();
			if (result != null) {
				updateUi(result);
				bindRestaurantData();
				createMealsList(result.subOrders);
			} else {
				AppUtils.showDialog(PastOrderDetailsScreen.this, null, getString(R.string.txt_getting_data_from_server_error));
			}
		}
	}

	private class SendOrderToMailTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(PastOrderDetailsScreen.this, getString(R.string.txt_get_order_details), false);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String orderId = params[0];
			try {
				App.getInstance().getNetworkService().sendOrderToMail(orderId, PastOrderDetailsScreen.this);
			} catch (NetworkException e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result) {
				AppUtils.showToast(PastOrderDetailsScreen.this, getString(R.string.txt_send_successfull));
			} else {
				AppUtils.showDialog(PastOrderDetailsScreen.this, null, getString(R.string.txt_getting_data_from_server_error));
			}
		}

	}

	public void onSendOrderToMail(View view) {
		new SendOrderToMailTask().execute(orderId);
	}

	public void onListClick(View view) {
		finish();
	}

}
