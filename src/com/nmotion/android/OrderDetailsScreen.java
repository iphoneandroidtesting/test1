package com.nmotion.android;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.models.FriendOrder;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.network.NetworkService;
import com.nmotion.android.network.SimpleResult;
import com.nmotion.android.utils.AppUtils;

public class OrderDetailsScreen extends BaseRestaurantScreen {
	private float productTotal;
	private float salesTax;
	private float discount;
	private float tips;
	private float total;

	private CheckBox termsBox;
	private View payForFriendsBtn; 
	private TableLayout table;
	private TextView txtProductTotal;
	private TextView txtOrdersTotal;
	private TextView txtDiscount;
	private TextView txtSalesTax;
	private TextView txtTerms;
	private EditText edtTip;
	private String currency;
	private TextView txtTipCurrency;
	private OrderDetails orderDetails;
	private ArrayList<FriendOrder> friendsOrders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		enableSignInButton();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_details_screen);
		payForFriendsBtn = findViewById(R.id.btn_order_for_friends);
		currency = getResources().getString(R.string.txt_dkk);
		table = (TableLayout) findViewById(R.id.table);
		termsBox = (CheckBox) findViewById(R.id.chk_terms);
		termsBox.setChecked(App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.TERMS_ACCEPTED_KEY, false));
		termsBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {                    
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                       App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.TERMS_ACCEPTED_KEY, isChecked);
                    }
                });
		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_order_details);
		findViewById(R.id.btn_menu_add_more).setVisibility(View.VISIBLE);
		txtProductTotal = (TextView) findViewById(R.id.txt_product_total);
		txtSalesTax = (TextView) findViewById(R.id.txt_sales_tax);
		txtDiscount = (TextView) findViewById(R.id.txt_discount);
		txtTerms = (TextView) findViewById(R.id.txt_terms);
		txtTerms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), TermsConditionsScreen.class);
                            startActivity(intent);
                    }
                });
		edtTip = (EditText) findViewById(R.id.edt_tip);
		txtTipCurrency = (TextView) findViewById(R.id.txt_tip_currency);
		txtTipCurrency.setText(currency);
		txtOrdersTotal = (TextView) findViewById(R.id.txt_your_order_total);
		edtTip.setFilters(new InputFilter[] { new CurrencyFormatInputFilter() });
		DibsPaymentScreen.toUpdateOrderAfterCancel=false;
	}

	@Override
	protected void onResume() {
		super.onResume();		
		if (App.getInstance().getPreferencesManager().getCheckInMode()==PreferencesManager.TAKE_AWAY_CHECKIN_MODE)
		    payForFriendsBtn.setVisibility(View.GONE);
		else
		    payForFriendsBtn.setVisibility(View.VISIBLE);
		orderDetails = App.getInstance().getCache().getOrderDetails();
		tips = orderDetails.tips;
		countOrderValues();
		updateTextFields();
		edtTip.setText(String.valueOf(tips));
		edtTip.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					tips = Float.valueOf(s.toString());
				} catch (NumberFormatException e) {
					tips = 0;
				}
				countOrderValues();
				updateTextFields();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {}
		});
		if (DibsPaymentScreen.toUpdateOrderAfterCancel){
		    //App.getInstance().getCache().deleteOrderDetails();
		    //App.getInstance().getCache().setOrderDetails(null);		    
		    friendsOrders=null;
		    new SaveOrUpdateOrderTask().execute();		   
		}		    
	}
	

	@Override
	protected void onPause() {
		App.getInstance().getCache().setOrderDetails(orderDetails);
		super.onPause();
	}

	private void countOrderValues() {
		orderDetails.tips = tips;
		productTotal = orderDetails.consolidatedProductTotal > 0 ? orderDetails.consolidatedProductTotal : orderDetails.productTotal;
		discount = orderDetails.consolidatedDiscount > 0 ? -orderDetails.consolidatedDiscount : -orderDetails.orderDiscount;
		salesTax = orderDetails.consolidatedSalesTax > 0 ? orderDetails.consolidatedSalesTax : orderDetails.salesTax;
		total = productTotal + discount + salesTax + tips;
	}

	private void updateTextFields() {
		txtProductTotal.setText(currency.concat(String.format("%.2f", productTotal)));
		txtSalesTax.setText(currency.concat(String.format("%.2f", salesTax)));
		if (discount==0){
		    table.getChildAt(1).setVisibility(View.GONE);
		    table.getChildAt(2).setVisibility(View.GONE);		    
		}else
		    txtDiscount.setText(currency.concat(String.format("%.2f", discount)));
		txtOrdersTotal.setText(currency.concat(String.format("%.2f", total)));
	}

	public void onAddMoreClick(View view) {
		Intent intent = new Intent(getApplicationContext(), CategoriesScreen.class);
		intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, App.getInstance().getCache().getCurrentRestaurantId());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	
	private boolean isOrderZero(){
	    if (orderDetails.orderTotal<=0){
	        Toast.makeText(this, "Amount is not valid", Toast.LENGTH_LONG).show();
	        return true;
	    }
	    return false;
	}

	public void onOrderClick(View view) {
	        if (isOrderZero())
	            return;
	        if (!termsBox.isChecked()){
	            Toast.makeText(this, getString(R.string.terms_not_accepted_warning), Toast.LENGTH_LONG).show();
	            return;
	        }
		App.getInstance().getCache().setOrderDetails(orderDetails);
		
		new SaveOrUpdateOrderTask().execute();
	}

	public void onOrderForFriendsClick(View view) {
	        if (isOrderZero())
                    return;
		Intent intent = new Intent(this, FriendsListScreen.class);
		intent.putExtra(FriendsListScreen.DATA_RESTAURANT_ID, App.getInstance().getCache().getCurrentRestaurantId());
		if (friendsOrders != null) {
			intent.putExtra(FriendOrder.class.getSimpleName(), friendsOrders);
		}
		startActivityForResult(intent, 100);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			friendsOrders = data.getExtras().getParcelableArrayList(FriendOrder.class.getSimpleName());
			countOrderValues();
			updateTextFields();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class CurrencyFormatInputFilter implements InputFilter {
		Pattern mPattern = Pattern.compile("(0|[0-9]*)(.[0-9]{0,2})?");

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			String result = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
			Matcher matcher = mPattern.matcher(result);
			if (!matcher.matches()) {
				return dest.subSequence(dstart, dend);
			}
			return null;
		}
	}	

	private class SaveOrUpdateOrderTask extends AsyncTask<Void, Void, SimpleResult> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(OrderDetailsScreen.this, getString(R.string.txt_get_order_details), false);
			super.onPreExecute();
		}

		@Override
		protected SimpleResult doInBackground(Void... params) {
			try {
				OrderDetails orderDetails = App.getInstance().getCache().getOrderDetails();
				if (DibsPaymentScreen.toUpdateOrderAfterCancel)
				    orderDetails=null;
				ArrayList<OrderedMeal> orders = App.getInstance().getCache().getOrders();
				if (orderDetails != null && orderDetails.orderStatus == NetworkService.ORDER_STATUS_NEW_PAYMENT) {
					orderDetails = App.getInstance().getNetworkService().updateOrderRequest(orderDetails, orders, OrderDetailsScreen.this);
					if (!DibsPaymentScreen.toUpdateOrderAfterCancel && App.getInstance().getNetworkService().updateOrderStatus(String.valueOf(orderDetails.orderId), String.valueOf(NetworkService.ORDER_STATUS_PENDING_PAYMENT), OrderDetailsScreen.this)) {
						orderDetails.orderStatus = NetworkService.ORDER_STATUS_PENDING_PAYMENT;
					}
				} else {
					orderDetails = App.getInstance().getNetworkService().saveOrderRequest(String.valueOf(App.getInstance().getCache().getCurrentRestaurantId()), orders, OrderDetailsScreen.this);
					if (tips > 0) {
						orderDetails.tips = tips;
						orderDetails = App.getInstance().getNetworkService().updateOrderRequest(orderDetails, orders, OrderDetailsScreen.this);
					}
					String[] friends = getStringArray(friendsOrders);
					if (friends.length > 0) {
						App.getInstance().getNetworkService().requestLinkOrders(String.valueOf(orderDetails.orderId), friends, OrderDetailsScreen.this);
					}
					if (!DibsPaymentScreen.toUpdateOrderAfterCancel && App.getInstance().getNetworkService().updateOrderStatus(String.valueOf(orderDetails.orderId), String.valueOf(NetworkService.ORDER_STATUS_PENDING_PAYMENT), OrderDetailsScreen.this)) {
						orderDetails.orderStatus = NetworkService.ORDER_STATUS_PENDING_PAYMENT;
					}
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

		private String[] getStringArray(ArrayList<FriendOrder> list) {

			if (list != null) {
				String[] resultArray = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					resultArray[i] = String.valueOf(list.get(i).resourceUrl);
				}
				return resultArray;
			} else {
				return new String[0];
			}
		}

		@Override
		protected void onPostExecute(SimpleResult result) {
			orderDetails = App.getInstance().getCache().getOrderDetails();
			progressDialog.dismiss();
			switch (result.getCode()) {
			case SimpleResult.STATUSE_DONE_OK:
				if(App.getInstance().getPreferencesManager().getCheckInMode() == PreferencesManager.ROOM_SERVICE_CHECKIN_MODE) {
					Intent intent = new Intent(getApplicationContext(), RoomPaymentScreen.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					intent.setAction(RoomPaymentScreen.ACTION_DEFAULT);
					startActivity(intent);
				} else if (!DibsPaymentScreen.toUpdateOrderAfterCancel) {
			    	Intent intent = new Intent(getApplicationContext(), DibsPaymentScreen.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					intent.setAction(DibsPaymentScreen.ACTION_SAVE_THEN_PAY);
					startActivity(intent);				 
			    }else
			        DibsPaymentScreen.toUpdateOrderAfterCancel=false;
			    break;
			/*case SimpleResult.STATUSE_DONE_WRONG:
				AppUtils.showDialog(OrderDetailsScreen.this, null, getString(R.string.txt_getting_data_from_server_error));
				break;*/
			case NetworkException.EXCEPTION_CODE_ORDER_BEING_PAID:
				AppUtils.showDialog(OrderDetailsScreen.this, null, getString(R.string.txt_order_being_paid));
				break;
			case NetworkException.EXCEPTION_CODE_ORDER_ALREADY_HAS_BEEN_PAID:
				AppUtils.showDialog(OrderDetailsScreen.this, null, getString(R.string.txt_order_has_been_paid)).setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						Intent intent = new Intent(OrderDetailsScreen.this, CategoriesScreen.class);
						intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, App.getInstance().getCache().getCurrentRestaurantId());
						App.getInstance().getCache().deleteOrderedMeals();
						App.getInstance().getCache().deleteOrderDetails();
						startActivity(intent);

					}
				});
				break;
			case NetworkException.HTTP_CODE_PRECONDITION_FAILED:
	                    App.getInstance().getPreferencesManager().checkOut(App.getInstance().getNetworkService().getCurrentUser().eMail, mRestaurantId);
	                    String message = getString(R.string.txt_getting_data_from_server_error);
                            if (result.getMessage() != null) {
                                    message = result.getMessage();
                            }
                            if (message.toLowerCase().contains("this value is not valid")
                                    || message.toLowerCase().contains("one meal at least"))
                                message = getString(R.string.options_ingrids_deleted_messaege);
                            AppUtils.showDialog(OrderDetailsScreen.this, null, message, new OnClickListener() {                                
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                   dialog.dismiss();
                                   Intent intent = new Intent(OrderDetailsScreen.this, RestaurantInfoScreen.class);
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
                            AppUtils.showDialog(OrderDetailsScreen.this, null, message);
                            break;
			}
		}
	}
}
