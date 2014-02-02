package com.nmotion.android.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.image.loader.ImageFetcher;
import com.nmotion.R;
import com.nmotion.android.App;
import com.nmotion.android.LogInScreen;

public class TopPanelLayout extends FrameLayout {
	//private int maxHeight;

	public TopPanelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TopPanelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TopPanelLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		setBackgroundResource(R.drawable.bg_gray_bar);
		inflate(getContext(), R.layout.layout_top_restaurant, this);
	}

	private void setRestaurantLogo(int restaurantId) {
		ImageView logoImageView = (ImageView) findViewById(R.id.logo);
		//logoImageView.setMaxHeight(maxHeight);		
		ImageFetcher imageResizer = new ImageFetcher(getContext(), 0, 0);
		imageResizer.setLoadingImage(R.drawable.logo_small);
		imageResizer.loadImage(App.getInstance().getCache().getRestaurantById(restaurantId).image, logoImageView);
	}

	public void updatePanelInfo(int restaurantId, boolean showSignInButton) {
		setRestaurantLogo(restaurantId);
		if (showSignInButton) {
			if (App.getInstance().getNetworkService().isLoggedIn()) {
				findViewById(R.id.btn_menu_sign_out).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.btn_menu_sign_in).setVisibility(View.VISIBLE);
			}
		}
	}

	public void onSignInClick(View view) {
		Intent intent = new Intent(getContext(), LogInScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getContext().startActivity(intent);
	}
}