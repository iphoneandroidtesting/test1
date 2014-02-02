package com.nmotion.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.core.FieldChecker;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.core.ResponseResultDialog;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Utils;

public class MyAccountEditScreen extends Activity {
	private UpdateUserInfoTask taskUpdate;
	TextView emailTxt, pswdTxt, pswdConfirmTxt, fNameTxt, sNameTxt;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_account_edit);

		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_edit_my_info);
		findViewById(R.id.btn_menu_sign_out).setVisibility(View.VISIBLE);

		emailTxt = (TextView) findViewById(R.id.edit_email);
		fNameTxt = (TextView) findViewById(R.id.edit_first_name);
		sNameTxt = (TextView) findViewById(R.id.edit_last_name);
		pswdTxt = (TextView) findViewById(R.id.edit_password);
		pswdConfirmTxt = (TextView) findViewById(R.id.edit_confirm_password);
		fNameTxt.setText(App.getInstance().getNetworkService().getCurrentUser().firstName);
		sNameTxt.setText(App.getInstance().getNetworkService().getCurrentUser().lastName);
		emailTxt.setText(App.getInstance().getNetworkService().getCurrentUser().eMail);
		// ((TextView)
		// findViewById(R.id.edit_password)).setText(App.getInstance().getPreferencesManager().getString(PreferencesManager.USER_PASSWORD,
		// ""));
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

	public void onSignOutClick(View view) {
		App.getInstance().getPreferencesManager().clear();
		App.getInstance().getNetworkService().logOut();

		Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void onEditProfileClick(View view) {
		String firstName = fNameTxt.getText().toString();
		String lastName = sNameTxt.getText().toString();
		String email = emailTxt.getText().toString();
		String password = pswdTxt.getText().toString();
		String passwordConfirm = pswdConfirmTxt.getText().toString();

		if (FieldChecker.emailFieldCheck(email, getApplicationContext())) {
			if (FieldChecker.passwordFieldCheck(password, getApplicationContext())) {
				if (FieldChecker.isPasswordConfirmed(password, passwordConfirm, getApplicationContext())) {
					update(String.valueOf(App.getInstance().getNetworkService().getCurrentUser().id), firstName, lastName, email, password);
				}
			}
		}
	}

	private void update(String id, String firstName, String lastName, String email, String password) {
		if (Utils.isNetworkAvailable(getApplicationContext())) {
			if (taskUpdate != null)
				taskUpdate.cancel(true);
			taskUpdate = new UpdateUserInfoTask();
			taskUpdate.execute(id, firstName, lastName, email, password);
		} else
			Utils.showToast(getApplicationContext(), getString(R.string.network_unavailable_message));
	}

	private class UpdateUserInfoTask extends AsyncTask<String, Void, Void> {
		private int errorCode;
		private String errorMessage;

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(MyAccountEditScreen.this, R.string.txt_updating_user_info, false);
			super.onPreExecute();
		}
		

		@Override
		protected Void doInBackground(String... arg0) {
			String id = arg0[0];
			String firstName = arg0[1];
			String lastName = arg0[2];
			String email = arg0[3];
			String password = arg0[4];
			try {
				App.getInstance().getNetworkService().updateUserInfo(id, firstName, lastName, email, password.length()==0 ? null : password, MyAccountEditScreen.this);
				App.getInstance().getNetworkService().login(email, password.length()==0 ? App.getInstance().getPreferencesManager().getString(PreferencesManager.USER_PASSWORD, null) : Utils.md5(password), null, MyAccountEditScreen.this);				
			} catch (NetworkException e) {
				errorMessage = e.error;
				errorCode = e.responseCode;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (errorCode != 0) {
				new ResponseResultDialog(MyAccountEditScreen.this, errorMessage, errorCode).show();
			} else {
				fNameTxt.setText(App.getInstance().getNetworkService().getCurrentUser().firstName);
				sNameTxt.setText(App.getInstance().getNetworkService().getCurrentUser().lastName);
				emailTxt.setText(App.getInstance().getNetworkService().getCurrentUser().eMail);
				pswdTxt.setText("");
				pswdConfirmTxt.setText("");
				Utils.showToast(getApplicationContext(), "Successful");
			}
		}
	};
}
