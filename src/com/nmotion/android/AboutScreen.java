package com.nmotion.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.utils.ActionHelper;

public class AboutScreen extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_about);
		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_about);
		((TextView) findViewById(R.id.terms_conditions_text_view)).setText(Html.fromHtml("<u>" + getString(R.string.txt_terms_and_conditions) + "</u>"));
	}

	public void onFAQClick(View view) {
		Intent intent = new Intent(this, FAQListScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void onPhoneClick(View view) {
		String number = getString(R.string.txt_nmtn_phone);
		ActionHelper.actionDial(this, number);
	}

	public void onTermsClick(View view) {
		Intent intent = new Intent(this, TermsConditionsScreen.class);
		startActivity(intent);
	}

}
