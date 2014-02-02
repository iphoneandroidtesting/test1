package com.nmotion.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.image.loader.ImageFetcher;
import com.nmotion.R;
import com.nmotion.android.models.Restaurant;

public class RestaurantInfoBlock extends LinearLayout {

	private TextView restaurantNameTV;
	private TextView restaurantAddressTV;
	private ImageView restaurantLogo;

	public RestaurantInfoBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RestaurantInfoBlock(Context context) {
		super(context);
		init();
	}

	private void init() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.restaurant_info_block_layout, this);
		restaurantNameTV = (TextView) findViewById(R.id.restaurant_name_text_view);
		restaurantAddressTV = (TextView) findViewById(R.id.restaurant_address_text_view);
		restaurantLogo = (ImageView) findViewById(R.id.img_restaurant);
		setBackgroundResource(R.drawable.bg_gray);
	}

	public void bindData(Restaurant restaurant) {
		if (restaurant != null) {
			ImageFetcher imageResizer = new ImageFetcher(getContext(), 0, 0);
			imageResizer.setLoadingImage(R.drawable.photo_def_small);
			restaurantNameTV.setText(restaurant.name);
			restaurantAddressTV.setText(restaurant.address + " " + restaurant.postalCode);
			imageResizer.loadImage(restaurant.image, restaurantLogo);
		}
	}
}
