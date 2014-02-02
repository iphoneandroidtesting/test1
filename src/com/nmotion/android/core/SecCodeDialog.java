package com.nmotion.android.core;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;

import com.nmotion.R;
import com.nmotion.android.LogInScreen.ForgotListener;

public class SecCodeDialog extends Dialog implements OnClickListener {
	private ForgotListener forgotListener;

	public SecCodeDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sec_code_dialog_layout);
		findViewById(R.id.sec_code_dialog_cancel_button).setOnClickListener(this);
		findViewById(R.id.sec_code_dialog_ok_button).setOnClickListener(this);
	}

	public void setForgotListener(ForgotListener forgotListener) {
		this.forgotListener = forgotListener;
	}

	@Override
	public void onClick(View v) {
		if (forgotListener != null) {
			forgotListener.onSendClick(((EditText) findViewById(R.id.editText1)).getText().toString());
		}
		dismiss();
	}
}
