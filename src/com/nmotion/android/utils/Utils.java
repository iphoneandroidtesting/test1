package com.nmotion.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.nmotion.R;
import com.nmotion.android.network.NetworkService;

public class Utils {

	public static int getErrorString(int code) {
		switch (code) {
		case NetworkService.COMMON_ERROR:
			return R.string.COMMON_ERROR;

		}
		return R.string.COMMON_ERROR;
	}

	public static LayoutAnimationController getListAnimation() {
		AnimationSet set = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(150);
		set.addAnimation(animation);
		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(300);
		set.addAnimation(animation);
		return new LayoutAnimationController(set, 0.5f);
	}

	public static String convertStreamToString(InputStream stream) {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}
			} catch (IOException e) {
				Logger.debug("Error converting stream" + e);
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					Logger.debug("Error closing stream" + e);
				}
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Logger.warning("Couldn't get connectivity manager");
		} else {
			NetworkInfo wifiNetwork = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobileNetwork = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			return (wifiNetwork != null && wifiNetwork.isConnected()) || (mobileNetwork != null && mobileNetwork.isConnected());
		}
		return false;
	}

	public static final String md5(final String s) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				while (hex.length() < 2) {
					hex = "0" + hex;
				}
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
		}
		return "";
	}

	public static void printSummary(String... args) {
		for (int i = 0; i < args.length; i += 2) {
			System.out.println(args[i] + ": " + args[i + 1] + "\n");
		}
	}

	public static void showToast(Toast messageToast, String message) {
		messageToast.cancel();
		messageToast.setText(message);
		messageToast.show();
	}

	public static void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
