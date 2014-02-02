package com.nmotion.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.nmotion.R;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;
import com.nmotion.android.view.NavigationMenuView;
import com.nmotion.android.view.SlidingMenu;
import com.nmotion.android.view.SlidingMenu.OnMenuCloseListener;
import com.nmotion.android.view.SlidingMenu.OnMenuOpenListener;

public class BaseActivity extends Activity implements OnMenuOpenListener, OnMenuCloseListener {
	private SlidingMenu baseLayout;
	private NavigationMenuView navigationMenuView;
	private EditText searchEditText;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		super.setContentView(R.layout.activity_base);
		baseLayout = (SlidingMenu) findViewById(R.id.base_layout);
		baseLayout.injectMenuById(R.layout.layout_navigation_menu);
		navigationMenuView = (NavigationMenuView) findViewById(R.id.navigation_menu);
		baseLayout.setOnMenuOpenListener(this);
		baseLayout.setOnMenuCloseListener(this);
		searchEditText = ((EditText) baseLayout.getMenu().findViewById(R.id.txt_search_restaurant));
	}

	@Override
	public void setContentView(int layoutResID) {
		baseLayout.injectContentById(layoutResID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.btn_menu).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				baseLayout.animateToggle();
			}
		});
		baseLayout.close();
		navigationMenuView.update();
	}

	public void onSignInClick(View view) {
	        singIn();
	}

	private void singIn() {
		Intent intent = new Intent(getApplicationContext(), LogInScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void onSignOutClick(View view) {
		if (App.getInstance().getCache().getOrders().size() == 0) {
			signOut();
		} else {
			AppUtils.showContinueCancelDialog(this, R.string.txt_your_basket_will_be_cleaned, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					signOut();
					dialog.dismiss();
				}
			});
		}
	}

	private void signOut() {
		App.getInstance().getPreferencesManager().clear();
		App.getInstance().getNetworkService().logOut();
		App.getInstance().getCache().clear();

		Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void onMapClick(View view) {
		Intent intent = new Intent(getApplicationContext(), RestaurantsMapScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void onListClick(View view) {
		Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		if (!baseLayout.isMenuClosed()) {
			baseLayout.animateMenuClose();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onMenuOpened() {
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int length = s.length();
				if (length >= Config.SEARCH_SYMBOLS_LENGTH) {
					Intent intent = new Intent(BaseActivity.this, RestaurantsListScreen.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("data_search", s.toString());
					startActivity(intent);
				}
			}
		});

	}

	@Override
	public void onMenuClosed() {
		AppUtils.hideKeyBoard(this);
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
	        /*AppUtils.showToast(this,  "CRASH");*/
	    }
	}
}