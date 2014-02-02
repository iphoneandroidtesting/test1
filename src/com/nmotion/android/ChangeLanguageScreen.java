package com.nmotion.android;

import java.util.Locale;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.utils.AppUtils;

public class ChangeLanguageScreen extends Activity {

	private RadioGroup radioGroup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_change_language);

		findViewById(R.id.btn_menu_done).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_menu_sign_in).setVisibility(View.GONE);

		radioGroup = (RadioGroup) findViewById(R.id.radio_group);

		String selectedLocale = App.getInstance().getPreferencesManager().contains(PreferencesManager.LANGUAGE) ? App.getInstance().getPreferencesManager().getString(PreferencesManager.LANGUAGE, "da") : Locale.getDefault().getLanguage();
		if (selectedLocale.equals("en")) {
			((RadioButton) radioGroup.findViewById(R.id.radio_btn_en)).setChecked(true);
		} else if (selectedLocale.equals("da")) {
			((RadioButton) radioGroup.findViewById(R.id.radio_btn_dk)).setChecked(true);
		}

		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_change_language);
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
            }
        }

	public void onDoneClick(View view) {
		Configuration config = getBaseContext().getResources().getConfiguration();
		Locale locale = null;
		switch (radioGroup.getCheckedRadioButtonId()) {
		case R.id.radio_btn_dk:
			locale = new Locale("da");
			break;
		case R.id.radio_btn_en:
			locale = new Locale("en");
			break;
		default:
			AppUtils.showToast(getApplicationContext(), R.string.txt_please_select_your_language);
			return;
		}
		Locale.setDefault(locale);
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		App.getInstance().getPreferencesManager().setString(PreferencesManager.LANGUAGE, locale.toString());
		finish();		
		startActivity(new Intent(this,SettingsScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
}
