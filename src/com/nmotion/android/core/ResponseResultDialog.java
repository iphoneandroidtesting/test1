package com.nmotion.android.core;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.nmotion.R;

public class ResponseResultDialog extends Dialog implements OnClickListener {

	public ResponseResultDialog(Context context, String message, int code) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.registraion_result_dialog_layout);
		((TextView) findViewById(R.id.txt_message)).setText(message);
		findViewById(R.id.registration_result_dialog_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}
}
