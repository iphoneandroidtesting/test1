//package com.nmotion.android.core;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.Window;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.nmotion.R;
//
//public class OrderModeDialog extends Dialog implements OnClickListener {
//	FrameLayout modeButton1, modeButton2, modeButton3, modeButton4;
//	Context context;
//
//	/*
//	 * CheckoutButton launchPayPalButton; PayPal ppObj;
//	 */
//	getPayPalInstanceTask task;
//	Toast messageToast;
//
//	// boolean containsAlcohol;
//
//	class getPayPalInstanceTask extends AsyncTask<Void, Void, Void> {
//		int errorCode;
//		String errorMessage;
//
//		@Override
//		protected Void doInBackground(Void... arg0) {
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			dismiss();
//		}
//	};
//
//	public OrderModeDialog(Context context, boolean containsAlcohol) {
//		super(context);
//		this.context = context;
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.order_mode_dialog_layout);
//
//		findViewById(R.id.order_mode_dialog_cancel_button).setOnClickListener(this);
//		modeButton1 = (FrameLayout) findViewById(R.id.order_mode_dialog_location_button);
//		modeButton1.setOnClickListener(this);
//		modeButton2 = (FrameLayout) findViewById(R.id.order_mode_dialog_saved_address_button);
//		modeButton2.setOnClickListener(this);
//		modeButton3 = (FrameLayout) findViewById(R.id.order_mode_dialog_new_address_button);
//		modeButton3.setOnClickListener(this);
//		modeButton4 = (FrameLayout) findViewById(R.id.order_mode_dialog_pickup_button);
//		modeButton4.setOnClickListener(this);
//		messageToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
//		if (containsAlcohol) {
//			modeButton1.setEnabled(false);
//			((TextView) findViewById(R.id.order_mode_dialog_location_text)).setTextColor(context.getResources().getColor(R.color.grey_urge));
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
//		if (v == findViewById(R.id.order_mode_dialog_cancel_button)) {
//			cancel();
//		}
//		if (v == modeButton1) {
//			Cache.currentOrderType = 0;
//			Cache.isDelivery = true;
//			App.getInstance().getCache().setByLocationFlag(true);
//			dismiss();
//		}
//		if (v == modeButton2) {
//			Cache.currentOrderType = 3;
//			/*
//			 * Intent intent = new Intent(v.getContext(),
//			 * SavedAddressesScreen.class);
//			 * intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//			 * context.startActivity(intent);
//			 */
//			dismiss();
//		}
//		if (v == modeButton3) {
//			Cache.currentOrderType = 1;
//			/*
//			 * Intent intent = new Intent(v.getContext(),
//			 * EnterAddressScreen.class);
//			 * intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//			 * context.startActivity(intent);
//			 */
//			dismiss();
//		}
//		if (v == modeButton4) {
//			Cache.currentOrderType = 2;
//			Cache.isDelivery = false;
//			Cache.currentRadius = 99999999;
//			dismiss();
//		}
//	}
//}
