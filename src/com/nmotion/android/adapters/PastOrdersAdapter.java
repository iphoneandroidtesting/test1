package com.nmotion.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.models.PastOrder;

public class PastOrdersAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<PastOrder> history;

	public PastOrdersAdapter(ArrayList<PastOrder> history, Context context) {
		this.history = history;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return history.size();
	}

	@Override
	public PastOrder getItem(int position) {
		return history.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		PastOrder currentOrder = history.get(position);

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.past_order_list_element_layout, null);
			holder.createTime = (TextView) convertView.findViewById(R.id.txt_order_create_date);
			holder.restorauntName = (TextView) convertView.findViewById(R.id.txt_order_restoraunt_name);
			holder.price = (TextView) convertView.findViewById(R.id.txt_order_total_price);
			// holder.price = (TextView)
			// convertView.findViewById(R.id.history_list_element_price_text_view);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.createTime.setText(DateFormat.format("dd/MM/yy", currentOrder.createdAt));
		holder.restorauntName.setText(currentOrder.restaurantName);
		holder.price.setText("DKK " + String.format("%.2f", currentOrder.orderTotal));

		return convertView;
	}

	private class ViewHolder {
		public TextView restorauntName, price, createTime;
	}
}
