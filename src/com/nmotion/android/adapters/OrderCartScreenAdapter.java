package com.nmotion.android.adapters;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.image.loader.ImageFetcher;
import com.image.loader.ImageResizer;
import com.nmotion.R;
import com.nmotion.android.OrderCartScreen.OnDeleteCheckBoxClick;
import com.nmotion.android.models.OrderedMeal;

public class OrderCartScreenAdapter extends ArrayAdapter<OrderedMeal> {
	private LayoutInflater mInflater;
	private ImageResizer imageResizer;
	private OnDeleteCheckBoxClick onDeleteCheckBoxClick;
	private DecimalFormat decimalFormat;
	private boolean isCheckable;

	public OrderCartScreenAdapter(Context context, List<OrderedMeal> objects, OnDeleteCheckBoxClick onDeleteCheckBoxClick) {
		super(context, -1, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageResizer = new ImageFetcher(context, 0, 0);
		imageResizer.setLoadingImage(R.drawable.photo_def_small);
		this.onDeleteCheckBoxClick = onDeleteCheckBoxClick;
		decimalFormat = new DecimalFormat(context.getString(R.string.txt_price_string));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		final OrderedMeal order = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_order_cart, null);
			holder.mealName = (TextView) convertView.findViewById(R.id.txt_meal_neam);
			holder.quantity = (TextView) convertView.findViewById(R.id.txt_quantity);
			holder.price = (TextView) convertView.findViewById(R.id.txt_price);
			holder.discountPrice = (TextView) convertView.findViewById(R.id.txt_menu_category_meal_price_discount);
			holder.imageView = (ImageView) convertView.findViewById(R.id.img_meal);
			holder.toggleButton = (ToggleButton) convertView.findViewById(R.id.btn_delete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.toggleButton.setChecked(false);
		holder.mealName.setText(order.meal.name);
		holder.quantity.setText(order.quantity + ", ");
		if (order.meal.isOptionAvailible()) {
			holder.price.setText(decimalFormat.format((order.meal.getMealExtraIngridientsPriceSum() + order.meal.getMealOptionPrice()) * (double) order.quantity));
		} else {
			holder.price.setText(decimalFormat.format((order.meal.priceIncludingTax + order.meal.getMealExtraIngridientsPriceSum()) * (double) order.quantity));
		}
		if (isCheckable) {
			holder.toggleButton.setVisibility(View.VISIBLE);
			Rect rect = new Rect();
			convertView.getDrawingRect(rect);
			convertView.setTouchDelegate(new TouchDelegate(rect, holder.toggleButton));
			holder.toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					onDeleteCheckBoxClick.onDeleteCheckBoxClick(order, isChecked);
				}

			});
		} else {
			convertView.setTouchDelegate(null);
			holder.toggleButton.setVisibility(View.GONE);
		}
		if ((holder.price.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
			holder.price.setPaintFlags((holder.price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)));
		}
		holder.discountPrice.setVisibility(View.GONE);
		if (order.meal.discountPriceIncludingTax != -1 && order.meal.discountPriceIncludingTax != 0) {
			holder.discountPrice.setVisibility(View.VISIBLE);
			holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			if (order.meal.isOptionAvailible()) {
			    holder.discountPrice.setText(decimalFormat.format((order.meal.getMealExtraIngridientsDiscountPriceSum() + order.meal.getMealOptionDiscountPrice()) * (double) order.quantity));
			}else
			    holder.discountPrice.setText(decimalFormat.format(order.meal.getMealExtraIngridientsDiscountPriceSum() + order.meal.discountPriceIncludingTax));
		}

		imageResizer.loadImage(order.meal.imageThumb==null ? order.meal.image : order.meal.imageThumb, holder.imageView);
		return convertView;
	}

	public void setCheckableEnable(boolean isCheckable) {
		this.isCheckable = isCheckable;
		notifyDataSetChanged();
	}

	public boolean isCheckable() {
		return isCheckable;
	}

	private class ViewHolder {
		public TextView mealName, quantity, price, discountPrice;
		public ImageView imageView;
		public ToggleButton toggleButton;
	}
}
