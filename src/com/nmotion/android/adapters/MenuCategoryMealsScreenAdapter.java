package com.nmotion.android.adapters;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.image.loader.ImageFetcher;
import com.image.loader.ImageResizer;
import com.nmotion.R;
import com.nmotion.android.models.Meal;

public class MenuCategoryMealsScreenAdapter extends ArrayAdapter<Meal> {
	private LayoutInflater mInflater;
	private ImageResizer imageResizer;
	private DecimalFormat decimalFormat;

	public MenuCategoryMealsScreenAdapter(Context context, int textViewResourceId, List<Meal> objects) {
		super(context, textViewResourceId, objects);
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageResizer = new ImageFetcher(context, 0, 0);
		imageResizer.setLoadingImage(R.drawable.photo_def_small);
		decimalFormat = new DecimalFormat(context.getString(R.string.txt_price_string));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Meal currentMeal = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_menu_category_meals, null);
			holder.name = (TextView) convertView.findViewById(R.id.txt_menu_category_meal_name);
			holder.description = (TextView) convertView.findViewById(R.id.txt_menu_category_meal_description);
			holder.price = (TextView) convertView.findViewById(R.id.txt_menu_category_meal_price);
			holder.discountPrice = (TextView) convertView.findViewById(R.id.txt_menu_category_meal_price_discount);
			holder.imageView = (ImageView) convertView.findViewById(R.id.img_menu_category_meal);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(currentMeal.name);
		holder.description.setText(currentMeal.description);

		holder.price.setText(decimalFormat.format(currentMeal.priceIncludingTax));
		if ((holder.price.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
			holder.price.setPaintFlags((holder.price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)));
		}
		holder.discountPrice.setVisibility(View.GONE);

		if (currentMeal.discountPriceIncludingTax != -1 && currentMeal.discountPriceIncludingTax != 0) {
			holder.discountPrice.setVisibility(View.VISIBLE);
			holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			holder.discountPrice.setText(decimalFormat.format(currentMeal.discountPriceIncludingTax));
		}
		imageResizer.loadImage(currentMeal.imageThumb, holder.imageView);
		return convertView;
	}

	public static class ViewHolder {
		public TextView name, description, price, discountPrice;
		public ImageView imageView;
	}
}
