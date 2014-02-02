package com.nmotion.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.models.FriendOrder;

public class FriendsAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<FriendOrder> orders;

	public FriendsAdapter(Context context, ArrayList<FriendOrder> orders) {
		super();
		inflater = LayoutInflater.from(context);
		this.orders = orders;
	}

	@Override
	public int getCount() {
		return orders.size();
	}

	@Override
	public FriendOrder getItem(int position) {
		return orders.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.friend_item, null);
			holder.name = (TextView) convertView.findViewById(R.id.txt_friend_name);
			holder.price = (TextView) convertView.findViewById(R.id.txt_friend_order_price);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		FriendOrder friendOrder = getItem(position);
		String lastName = "anonymous".equals(friendOrder.lastName) ? "" : TextUtils.concat(" ", friendOrder.lastName).toString();
		holder.name.setText(TextUtils.concat(friendOrder.firstName, lastName));
		holder.price.setText(TextUtils.concat("DKK " + friendOrder.orderTotalWhenSlave));
		return convertView;
	}

	private class ViewHolder {
		TextView name, price;
	}
}
