package com.nmotion.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.models.CreditCard;

public class CreditCardsAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private boolean isCheckable;
	private List<CreditCard> cards;

	public CreditCardsAdapter(Context context, List<CreditCard> objects) {
		mInflater = LayoutInflater.from(context);
		cards = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		CreditCard creditCard = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_credit_cards, null);
			holder.number = (TextView) convertView.findViewById(R.id.txt_card_number);
			holder.title = (TextView) convertView.findViewById(R.id.txt_card_title);
			holder.checkBoxImg = (CheckBox) convertView.findViewById(R.id.chk_credit_card);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (isCheckable) {
			holder.checkBoxImg.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.drawable.list_view_element_color_selector);
		} else {
			holder.checkBoxImg.setVisibility(View.GONE);
			convertView.setBackgroundDrawable(null);
		}
		holder.number.setText(creditCard.number);
		holder.title.setText(creditCard.title);

		return convertView;
	}

	private class ViewHolder {
		public CheckBox checkBoxImg;
		public TextView title, number;
	}

	public void setCheckableEnable(boolean isCheckable) {
		this.isCheckable = isCheckable;
		notifyDataSetChanged();
	}

	public boolean isCheckable() {
		return isCheckable;
	}

	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public CreditCard getItem(int position) {
		return cards.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}
