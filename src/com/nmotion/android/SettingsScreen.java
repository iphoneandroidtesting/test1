package com.nmotion.android;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;

public class SettingsScreen extends BaseActivity implements OnCheckedChangeListener {

	private CheckBox chkFacebookCheckin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_settings_screen);

		chkFacebookCheckin = (CheckBox) findViewById(R.id.chk_facebook_checkin);
		chkFacebookCheckin.setOnCheckedChangeListener(this);
	}

	public void onCheckinFacebook(View view) {
		chkFacebookCheckin.toggle();
	}

	public void onChangeYourLocationClick(View view) {
		Intent intent = new Intent(getApplicationContext(), ChangeYourLocationScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void onChangeLanguageClick(View view) {
		Intent intent = new Intent(getApplicationContext(), ChangeLanguageScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.IS_CHEKIN_FACEBOOK, isChecked);
	}

	@Override
	protected void onResume() {  
		String localeStr = App.getInstance().getPreferencesManager().contains(PreferencesManager.LANGUAGE) ? App.getInstance().getPreferencesManager().getString(PreferencesManager.LANGUAGE, "da") : Locale.getDefault().getLanguage();
		((TextView) findViewById(R.id.txt_language)).setText(localeStr.equals("en") ? getString(R.string.txt_english) : getString(R.string.txt_danish));
		if (App.getInstance().getNetworkService().isLoggedIn()) {
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.GONE);
			findViewById(R.id.btn_menu_sign_out).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.btn_menu_sign_out).setVisibility(View.GONE);
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.VISIBLE);
		}
		if (App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.USER_VIA_FACEBOOK, false)) {
			findViewById(R.id.facebook_checkin_layout).setVisibility(View.VISIBLE);
		}
		chkFacebookCheckin.setChecked(App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.IS_CHEKIN_FACEBOOK, false));
		super.onResume();		
	}
}