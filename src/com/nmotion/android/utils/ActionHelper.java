package com.nmotion.android.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ActionHelper {

	public static void actionDial(Context context, String number) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + number));
		context.startActivity(intent);
	}

	public static void actionBrowser(Context context, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(intent);
	}

}
