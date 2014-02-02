package com.nmotion.android.utils;

import com.nmotion.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class AppUtils {

	/**
	 * Method for hiding keyboard
	 */
	public static void hideKeyBoard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (activity.getWindow().getCurrentFocus() != null)
			imm.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
	}

	/**
	 * Method for showing dialog
	 */
	public static AlertDialog showDialog(Context context, String title, String message, OnClickListener listener) {
	    AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), listener!=null ? listener : new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                    }
            });
            try{
                dialog.show();
            }catch(Throwable e){            
            }
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                    R.drawable.gray_red_btn_background_selector);
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                    R.drawable.gray_red_btn_background_selector);
            return dialog;
	    
	}
	public static AlertDialog showDialog(Context context, String title, String message) {
	    return showDialog(context, title, message, null);
	}

	/**
	 * Method for showing dialog
	 */
	public static AlertDialog showDialog(Context context, int title, int message) {
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle(title);
		dialog.setMessage(context.getString(message));
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
		return dialog;
	}

	/**
	 * Method for showing progress dialog
	 */
	public static ProgressDialog showProgressDialog(Context context, String message, boolean isCancel) {
		ProgressDialog progDialog = new ProgressDialog(context);
		progDialog.setMessage(message);
		try {		    
		    progDialog.show();
		}catch(Throwable e){		    
		}		
		progDialog.setCanceledOnTouchOutside(isCancel);
		progDialog.setCancelable(isCancel);
		return progDialog;
	}

	/**
	 * Method for showing progress dialog
	 */
	public static ProgressDialog showProgressDialog(Context context, int message, boolean isCancel) {
		return showProgressDialog(context, context.getString(message), isCancel);
	}

	/**
	 * Method for showing toast
	 */
	public static void showToast(Context context, int id) {
		Toast.makeText(context, id, Toast.LENGTH_LONG).show();
	}

	/**
	 * Method for showing toast
	 */
	public static void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public static String getDeviceId(Context context) {
		String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		if (TextUtils.isEmpty(deviceId)) {
			deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		}
		return deviceId;
	}
	
	public static void showContinueCancelDialog(Context context, String message, OnClickListener positiveButtonListener) {
	    AlertDialog dialog = new Builder(context).create();

            dialog.setCancelable(false);
            dialog.setMessage(message);

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.txt_continue), positiveButtonListener);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.txt_cancel), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                    }
            });

            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                    R.drawable.gray_red_btn_background_selector);
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                    R.drawable.gray_red_btn_background_selector);
	}

	public static void showContinueCancelDialog(Context context, int messageId, OnClickListener positiveButtonListener) {
		showContinueCancelDialog(context, context.getString(messageId), positiveButtonListener);
	}
}
