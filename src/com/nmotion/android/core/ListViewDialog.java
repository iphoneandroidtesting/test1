/*package com.nmotion.android.core;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nmotion.R;

public class ListViewDialog extends Dialog implements OnClickListener {
	FrameLayout cardButton1, cardButton3, cardButton2;
	Context context;
	Toast messageToast;

	public ListViewDialog(Context context) {
		super(context);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list_view_dialog);
//		ListView list = (ListView) findViewById(R.id.listView1);
//		list.setAdapter(ArrayAdapter.createFromResource(context, R.array.tips_array_absolute_numbers, android.R.layout.simple_spinner_item));
//		list.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				Cache.currentTip = Float.parseFloat(arg0.getContext().getResources().getStringArray(R.array.tips_array_absolute_numbers)[arg2].substring(1));
//				dismiss();
//			}
//		});
	}

	@Override
	public void onClick(View v) {

	}

}
*/