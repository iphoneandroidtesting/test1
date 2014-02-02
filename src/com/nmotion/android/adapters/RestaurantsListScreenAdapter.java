package com.nmotion.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.image.loader.ImageFetcher;
import com.image.loader.ImageResizer;
import com.nmotion.R;
import com.nmotion.android.models.Restaurant;

public class RestaurantsListScreenAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ImageResizer imageResizer;
	private List<Restaurant> restaurants;

	public RestaurantsListScreenAdapter(Context context, List<Restaurant> objects) {
		restaurants = objects;
		mInflater = LayoutInflater.from(context);
		imageResizer = new ImageFetcher(context, 0, 0);
		imageResizer.setLoadingImage(R.drawable.photo_def_small);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Restaurant currentRestaurant = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_restaurant, null);
			holder.restaurantNameTV = (TextView) convertView.findViewById(R.id.restaurant_name_text_view);
			holder.restaurantAddressTV = (TextView) convertView.findViewById(R.id.restaurant_address_text_view);
			holder.restaurantDistanceToUserTV = (TextView) convertView.findViewById(R.id.restaurant_distance_text_view);
			holder.imageView = (ImageView) convertView.findViewById(R.id.img_restaurant);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.restaurantNameTV.setText(currentRestaurant.name);
		holder.restaurantAddressTV.setText(currentRestaurant.address + ", " + currentRestaurant.postalCode);
		holder.restaurantDistanceToUserTV.setText(currentRestaurant.getDistance(convertView.getContext().getString(R.string.txt_distance_string)));
		imageResizer.loadImage(currentRestaurant.image, holder.imageView);
		return convertView;
	}

	private class ViewHolder {
		public TextView restaurantNameTV, restaurantAddressTV, restaurantDistanceToUserTV;
		public ImageView imageView;
	}

	@Override
	public int getCount() {
		return restaurants.size();
	}

	@Override
	public Restaurant getItem(int position) {

		return restaurants.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}
