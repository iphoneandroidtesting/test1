package com.nmotion.android;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

import com.nmotion.R;
import com.nmotion.android.adapters.FriendsAdapter;
import com.nmotion.android.models.FriendOrder;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.network.NetworkService;
import com.nmotion.android.network.SimpleResult;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.view.TopPanelLayout;

public class FriendsListScreen extends ListActivity {
	private ArrayList<FriendOrder> orders;
	private FriendsAdapter adapter;
	public ArrayList<FriendOrder> selectedFriends;
	private int restaurantId;

	public static final String DATA_RESTAURANT_ID = "restaurant_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list_screen);

		findViewById(R.id.btn_menu_order).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_menu_done).setVisibility(View.VISIBLE);

		orders = new ArrayList<FriendOrder>();
		adapter = new FriendsAdapter(this, orders);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		setListAdapter(adapter);

		restaurantId = getIntent().getExtras().getInt(DATA_RESTAURANT_ID);
		((TopPanelLayout) findViewById(R.id.top_panel_layout)).updatePanelInfo(restaurantId, false);

		new GetFriendsList().execute(String.valueOf(restaurantId));
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

	class GetFriendsList extends AsyncTask<String, Void, ArrayList<FriendOrder>> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = AppUtils.showProgressDialog(FriendsListScreen.this, getString(R.string.txt_get_order_details), false);
			super.onPreExecute();
		}

		@Override
		protected ArrayList<FriendOrder> doInBackground(String... params) {
			String restaurantId = params[0];
			ArrayList<FriendOrder> result = null;
			try {
				result = App.getInstance().getNetworkService().getCheckinedOrders(restaurantId, FriendsListScreen.this);
			} catch (NetworkException e) {
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<FriendOrder> result) {
			progressDialog.dismiss();
			if (result != null) {
				orders.clear();
				orders.addAll(result);
				adapter.notifyDataSetChanged();

				if (getIntent().getExtras().containsKey(FriendOrder.class.getSimpleName())) {
					selectedFriends = getIntent().getExtras().getParcelableArrayList(FriendOrder.class.getSimpleName());
					for (int i = 0; i < orders.size(); i++) {
						for (int j = 0; j < selectedFriends.size(); j++)
							if (orders.get(i).equals(selectedFriends.get(j))) {
								getListView().setItemChecked(i, true);
							}
					}
				}

			} else {
				AppUtils.showDialog(FriendsListScreen.this, null, getString(R.string.txt_getting_data_from_server_error));
			}
		}
	}

	public void onOrderClick(View view) {
		finish();
	}

	public void onRefreshClick(View view) {
		new GetFriendsList().execute(String.valueOf(restaurantId));
	}

	public void onDoneClick(View view) {
		ArrayList<FriendOrder> resultForLink = new ArrayList<FriendOrder>();
		ArrayList<FriendOrder> resultForUnLink = new ArrayList<FriendOrder>();
		SparseBooleanArray array = getListView().getCheckedItemPositions();
		for (int i = 0; i < array.size(); i++) {
			int idx = array.keyAt(i);
			if (array.valueAt(i)) {
				resultForLink.add(orders.get(idx));
			} else {
				resultForUnLink.add(orders.get(idx));
			}
		}

		getIntent().putExtra(FriendOrder.class.getSimpleName(), resultForLink);
		doLinkAndUnlink(getStringArray(resultForLink), getStringArray(resultForUnLink));
		setResult(RESULT_OK, getIntent());

	}

	private void doLinkAndUnlink(String[] ordersForLink, String[] ordersForUnLink) {
		new LinkUnlinkOrderTask().execute(ordersForLink, ordersForUnLink);
	}

	private String[] getStringArray(ArrayList<FriendOrder> list) {
		String[] resultArray = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			resultArray[i] = String.valueOf(list.get(i).resourceUrl);
		}
		return resultArray;
	}

	private class LinkUnlinkOrderTask extends AsyncTask<String[], Void, SimpleResult> {
		private ProgressDialog progressDialog;
		public static final int STATUSE_DONE_WRONG_LINK = 11;
		public static final int STATUSE_DONE_WRONG_UNLINK = 12;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(FriendsListScreen.this, getString(R.string.txt_get_order_details), false);
			super.onPreExecute();
		}

		@Override
		protected SimpleResult doInBackground(String[]... params) {
			String[] ordersForLink = params[0];
			String[] ordersForUnLink = params[1];
			OrderDetails orderDetails = App.getInstance().getCache().getOrderDetails();
			OrderDetails newOrderDetails = null;
			// This hack need if we linked friends orders before->go to payment page->cancel payment-> and try link order again
			// if order was canceled and not called POST request for create new friends orders linked to old order
			if (orderDetails != null && orderDetails.orderStatus == NetworkService.ORDER_STATUS_PENDING_PAYMENT) {
				try {
					orderDetails = App.getInstance().getNetworkService()
							.saveOrderRequest(String.valueOf(App.getInstance().getCache().getCurrentRestaurantId()), App.getInstance().getCache().getOrders(), FriendsListScreen.this);
				} catch (NetworkException e) {
				}
				App.getInstance().getCache().setOrderDetails(orderDetails);
			}
			String orderId = String.valueOf(orderDetails.orderId);

			if (ordersForUnLink.length > 0) {
				try {
					newOrderDetails = App.getInstance().getNetworkService().requestUnLinkOrders(orderId, ordersForUnLink, FriendsListScreen.this);
					for (int i = 0; i < ordersForUnLink.length; i++) {
					}
				} catch (NetworkException e) {
					return new SimpleResult(STATUSE_DONE_WRONG_UNLINK, e.getMessage());
				}
			}

			if (ordersForLink.length > 0) {
				try {
					newOrderDetails = App.getInstance().getNetworkService().requestLinkOrders(orderId, ordersForLink, FriendsListScreen.this);
					for (int i = 0; i < ordersForLink.length; i++) {
					}
				} catch (NetworkException e) {
					return new SimpleResult(STATUSE_DONE_WRONG_LINK, e.getMessage());
				}
			}
			if (newOrderDetails != null) {
				App.getInstance().getCache().setOrderDetails(newOrderDetails);
			}

			return new SimpleResult(true);
		}

		@Override
		protected void onPostExecute(SimpleResult result) {
			progressDialog.dismiss();
			switch (result.getCode()) {
			case SimpleResult.STATUSE_DONE_OK:
				finish();
				break;
			case STATUSE_DONE_WRONG_LINK:
				AppUtils.showDialog(FriendsListScreen.this, null, result.getMessage());
				break;
			case STATUSE_DONE_WRONG_UNLINK:
				AppUtils.showDialog(FriendsListScreen.this, null, result.getMessage());
				break;
			default:
				AppUtils.showToast(FriendsListScreen.this, R.string.txt_error);
				break;
			}
		}
	}

}
