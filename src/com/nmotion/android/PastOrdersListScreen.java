package com.nmotion.android;

import java.util.ArrayList;
import java.util.Collections;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.adapters.PastOrdersAdapter;
import com.nmotion.android.models.PastOrder;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;

public class PastOrdersListScreen extends ListActivity implements OnItemClickListener {

	private ArrayList<PastOrder> orders;
	private PastOrdersAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_history_screen);
		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_my_orders);

		// findViewById(R.id.btn_menu_edit).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_menu_account).setVisibility(View.VISIBLE);
		orders = new ArrayList<PastOrder>();
		adapter = new PastOrdersAdapter(orders, this);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		new GetPastOrdersTask().execute();
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

	public void onAccountClick(View view) {
		finish();
	}

	public void onEditClick(View view) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, PastOrderDetailsScreen.class);
		intent.putExtra("data", String.valueOf(orders.get(position).id));
		startActivity(intent);
	}

	private class GetPastOrdersTask extends AsyncTask<Void, Void, ArrayList<PastOrder>> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(PastOrdersListScreen.this, getString(R.string.txt_get_order_details), false);
			super.onPreExecute();
		}

		@Override
		protected ArrayList<PastOrder> doInBackground(Void... params) {
			ArrayList<PastOrder> orderListItems = null;
			try {
				orderListItems = App.getInstance().getNetworkService().getCurrentUserOrders(PastOrdersListScreen.this);
			} catch (NetworkException e) {
			}
			return orderListItems;
		}

		@Override
		protected void onPostExecute(ArrayList<PastOrder> result) {
			progressDialog.dismiss();
			if (result != null) {
				orders.clear();
				Collections.sort(result);
				orders.addAll(result);
				adapter.notifyDataSetChanged();
			} else {
				AppUtils.showDialog(PastOrdersListScreen.this, null, getString(R.string.txt_getting_data_from_server_error));
			}
		}
	}

}
