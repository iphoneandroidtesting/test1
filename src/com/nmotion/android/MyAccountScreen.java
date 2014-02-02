package com.nmotion.android;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;

public class MyAccountScreen extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_account);

		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_my_account);
		if (App.getInstance().getNetworkService().isLoggedIn()) {
			findViewById(R.id.btn_menu_sign_out).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.VISIBLE);
		}
		findViewById(R.id.account_screen_register_order_button).setOnClickListener(loginButtonClickListener);

		if (App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.USER_VIA_FACEBOOK, false)) {
			findViewById(R.id.edit_my_info_layout).setVisibility(View.GONE);
		}
	}

	public void onResume() {
		super.onResume();
		String locale = App.getInstance().getPreferencesManager().contains(PreferencesManager.LANGUAGE) ? App.getInstance().getPreferencesManager().getString(PreferencesManager.LANGUAGE, "da") : Locale.getDefault().getLanguage();
		((TextView) findViewById(R.id.txt_language)).setText(locale.equals("en") ? "English" : "Danish");
		if (App.getInstance().getNetworkService().isLoggedIn()) {
			findViewById(R.id.LinearLayout01).setVisibility(View.GONE);
			findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.GONE);
			findViewById(R.id.btn_menu_sign_out).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.scrollView2).setVisibility(View.GONE);
			findViewById(R.id.LinearLayout01).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_menu_sign_out).setVisibility(View.GONE);
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.VISIBLE);
		}
	}

	OnClickListener loginButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(getApplicationContext(), LogInScreen.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}
	};

	OnClickListener logoutButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			App.getInstance().getNetworkService().logOut();
			App.getInstance().getPreferencesManager().clear();
			onResume();
		}
	};

	public void onEditMyInfoClick(View view) {
		Intent intent = new Intent(getApplicationContext(), MyAccountEditScreen.class);
		startActivity(intent);
	}

	public void onChangeLanguageClick(View view) {
		Intent intent = new Intent(getApplicationContext(), ChangeLanguageScreen.class);
		startActivity(intent);
	}

	public void onEditCreditCardClick(View view) {
		Intent intent = new Intent(getApplicationContext(), CreditCardsInfoScreen.class);
		startActivity(intent);
	}

	public void onHistoryClick(View view) {
		Intent intent = new Intent(getApplicationContext(), PastOrdersListScreen.class);
		startActivity(intent);
	}
}
