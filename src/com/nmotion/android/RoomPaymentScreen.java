package com.nmotion.android;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RoomPaymentScreen extends Activity {
	public static final String ACTION_DEFAULT = "default_action";
	
	private TextView roomNumberLabel;
	private Button 	 confirmButton;
	
	private OrderDetails orderDetails;
	private Restaurant 	 restaurant;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_payment_screen);
		
		/* Getting environment */
		orderDetails = App.getInstance().getCache().getOrderDetails();
		restaurant = App.getInstance().getCache().getRestaurantById(orderDetails.restaurantId);
		
		
		/* Finding views */
		roomNumberLabel = (TextView) findViewById(R.id.roomNumberLabel);
		confirmButton	= (Button) findViewById(R.id.confirmButton);
		
		/* Preprocessing data */
		String numberLabelText = String.format(getString(R.string.txt_room_will_be_charged), orderDetails.tableNumber);
		
		/* Updating view */
		roomNumberLabel.setText(numberLabelText);
	}
	
	public void onConfirmClick(View view) {
		String orderId = String.format("%d", orderDetails.orderId);
		confirmButton.setEnabled(false);
		AppUtils.showToast(this, R.string.txt_loading);
		new PostPaymentTask().execute(orderId);		
	}
	private class PostPaymentTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			String orderId = params[0];
			boolean success = false;
			try {
				success = App.getInstance().getNetworkService().postRoomPayment(orderId, RoomPaymentScreen.this);
				if(success) {
					System.out.println("Success");
				} else {
					System.out.println("Failure");
				}
			} catch (NetworkException e) {
				
			}
			return success;
		}
		protected void onPostExecute(Boolean result) {
			if(result) {
				App.getInstance().getCache().deleteOrderedMeals();
				App.getInstance().getPreferencesManager().checkOut(
						App.getInstance().getNetworkService().getCurrentUser().eMail, 
						App.getInstance().getCache().getOrderDetails().restaurantId);
				Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
								Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			} else {
				Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
								Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		}
    	
    }

}
