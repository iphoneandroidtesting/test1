package com.nmotion.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nmotion.R;
import com.nmotion.android.core.FieldChecker;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.core.ResponseResultDialog;
import com.nmotion.android.core.SecCodeDialog;
import com.nmotion.android.facebook.DialogError;
import com.nmotion.android.facebook.Facebook;
import com.nmotion.android.facebook.Facebook.DialogListener;
import com.nmotion.android.facebook.FacebookError;
import com.nmotion.android.facebook.SessionStore;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;
import com.nmotion.android.utils.Utils;

public class LogInScreen extends BaseActivity {

    private Facebook facebook;

    private EditText edtPass;

    private EditText edtEmail;
    private boolean orderMealMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_log_in_screen);

        ((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_sign_in);

        (findViewById(R.id.login_screen_ok_button)).setOnClickListener(loginButtonClickListener);
        (findViewById(R.id.login_screen_forgot_password_button)).setOnClickListener(forgotPasswordButtonClickListener);
        (findViewById(R.id.login_screen_register_button)).setOnClickListener(registerButtonClickListener);

        edtEmail = (EditText) findViewById(R.id.login_screen_email_edit_text);
        edtPass = (EditText) findViewById(R.id.login_screen_password_edit_text);
        
        orderMealMode = getIntent().getBooleanExtra("orderMealMode", false);;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }

    public void onLoginViaFacebookClick(View view) {
        facebook = new Facebook(Config.APP_ID);
        if (!SessionStore.restore(facebook, getApplicationContext())) {
            facebook.authorize(this, new String[] { "email", "publish_stream" }, new DialogListener() {

                @Override
                public void onFacebookError(FacebookError e) {
                    App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.USER_VIA_FACEBOOK, false);
                    SessionStore.clear(getApplicationContext());
                }

                @Override
                public void onError(DialogError e) {
                    App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.USER_VIA_FACEBOOK, false);
                    SessionStore.clear(getApplicationContext());
                }

                @Override
                public void onComplete(Bundle values) {
                    App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.USER_VIA_FACEBOOK, true);
                    SessionStore.save(facebook, getApplicationContext());
                    new LoginTask().execute();
                }

                @Override
                public void onCancel() {
                    App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.USER_VIA_FACEBOOK, false);
                    SessionStore.clear(getApplicationContext());
                }
            });
        } else {
            App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.USER_VIA_FACEBOOK, true);
            new LoginTask().execute(edtEmail.getText().toString(), edtPass.getText().toString());
        }
    }

    private OnClickListener loginButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AppUtils.hideKeyBoard(LogInScreen.this);
            if (FieldChecker.emptyFieldCheck(
                    new String[] { edtEmail.getText().toString(), edtPass.getText().toString() }, LogInScreen.this)
                    && FieldChecker.emailFieldCheck(edtEmail.getText().toString(), v.getContext())
                    && FieldChecker.passwordFieldCheck(edtPass.getText().toString(), v.getContext())) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    new LoginTask().execute(edtEmail.getText().toString(), edtPass.getText().toString());
                } else
                    Utils.showToast(getApplicationContext(), getString(R.string.network_unavailable_message));
            }
        }
    };

    private OnClickListener registerButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), RegistrationScreen.class);
            startActivity(intent);
        }
    };

    private OnClickListener forgotPasswordButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SecCodeDialog customizeDialog = new SecCodeDialog(LogInScreen.this);
            customizeDialog.setForgotListener(new ForgotListener() {

                @Override
                public void onSendClick(String email) {
                    if (FieldChecker.emailFieldCheck(email, LogInScreen.this)) {
                        new ForgotTask().execute(email);
                    }
                    edtPass.setText("");
                }
            });
            customizeDialog.show();
        }
    };

    public interface ForgotListener {
        public void onSendClick(String email);
    }

    private class LoginTask extends AsyncTask<String, Void, Void> {
        private String response;
        private int responseCode;

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = AppUtils.showProgressDialog(LogInScreen.this, R.string.txt_signing_in, false);
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = null;
            String pass = null;
            if (params.length >= 2) {
                email = params[0];
                pass = params[1];
            }

            boolean isFacebookAuth = App.getInstance().getPreferencesManager()
                    .getBoolean(PreferencesManager.USER_VIA_FACEBOOK, false);
            try {
                if (isFacebookAuth && facebook != null) {
                    App.getInstance().getNetworkService()
                            .login(null, null, facebook.getAccessToken(), LogInScreen.this);
                    App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.IS_CHEKIN_FACEBOOK, true);
                } else {
                    App.getInstance().getNetworkService().login(email, Utils.md5(pass/*
                                                                                      * .
                                                                                      * toLowerCase
                                                                                      * (
                                                                                      * )
                                                                                      */), null, LogInScreen.this);
                }
                App.getInstance().getPreferencesManager().setString(PreferencesManager.LAST_USER_LOGIN, email);
                if (((CheckBox) findViewById(R.id.checkBox1)).isChecked()) {
                    App.getInstance().getPreferencesManager()
                            .setString(PreferencesManager.LAST_USER_PASSWORD, Utils.md5(pass/*
                                                                                             * .
                                                                                             * toLowerCase
                                                                                             * (
                                                                                             * )
                                                                                             */));
                } else {
                    App.getInstance().getPreferencesManager().setString(PreferencesManager.LAST_USER_PASSWORD, null);
                }
                // App.getInstance().getCache().clear();
            } catch (NetworkException e) {
                response = getString(R.string.txt_login_error);
                responseCode = e.responseCode;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            if (responseCode != 0) {
                ResponseResultDialog customizeDialog = new ResponseResultDialog(LogInScreen.this, response,
                        responseCode);
                customizeDialog.show();
            } else {
                finish();
                if (!orderMealMode){
                    Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }
    }

    private class ForgotTask extends AsyncTask<String, Void, Void> {
        private String response;
        private int responseCode;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            progressDialog = AppUtils.showProgressDialog(LogInScreen.this, R.string.txt_restore_password, false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                App.getInstance().getNetworkService().resetPassword(params[0], LogInScreen.this);
                response = getString(R.string.txt_password_successfully_reset);
            } catch (NetworkException e) {
                response = e.getMessage();
                responseCode = e.responseCode;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            if (responseCode != 0) {
                ResponseResultDialog customizeDialog = new ResponseResultDialog(LogInScreen.this, response,
                        responseCode);
                customizeDialog.show();
            } else {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        }
    }
}
