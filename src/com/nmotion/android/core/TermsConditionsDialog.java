package com.nmotion.android.core;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.nmotion.R;

public class TermsConditionsDialog extends Dialog implements OnClickListener {

	public TermsConditionsDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_terms_conditions);
		findViewById(R.id.btn_ok).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}
}
