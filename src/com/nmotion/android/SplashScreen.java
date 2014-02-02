package com.nmotion.android;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.facebook.Facebook;
import com.nmotion.android.facebook.SessionStore;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;
import com.nmotion.android.utils.Logger;

public class SplashScreen extends Activity {
	final int SPLASH_SCREEN_DELAY_1 = 1000;
	final int SPLASH_SCREEN_DELAY_2 = 2000;

	class LoginTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO: must be more correct place
			//App.getInstance().getCache().clear();

			String lastUserLogin = App.getInstance().getPreferencesManager().getString(PreferencesManager.LAST_USER_LOGIN, null);
			String lastUserPass = App.getInstance().getPreferencesManager().getString(PreferencesManager.LAST_USER_PASSWORD, null);

			try {
				App.getInstance().getNetworkService().requestAppConfig(SplashScreen.this);
			} catch (NetworkException e1) {
				Logger.warning(e1.toString());
			}
			boolean isViaFacebook = App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.USER_VIA_FACEBOOK, false);
			try {
				if (lastUserPass != null || isViaFacebook) {
					Thread.sleep(SPLASH_SCREEN_DELAY_1);
					try {
						if (isViaFacebook) {
							Facebook facebook = new Facebook(Config.APP_ID);
							SessionStore.restore(facebook, getApplicationContext());
							App.getInstance().getNetworkService().login(null, null, facebook.getAccessToken(), SplashScreen.this);
							App.getInstance().getPreferencesManager().setBoolean(PreferencesManager.IS_CHEKIN_FACEBOOK, true);
						} else {
							App.getInstance().getNetworkService().login(lastUserLogin, lastUserPass, null, SplashScreen.this);
						}
						/*try{
						    App.getInstance().getNetworkService().linkUser(SplashScreen.this);
		                                }catch(NetworkException e){
		                                    e.printStackTrace();
		                                }*/						
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				} else {
					Thread.sleep(SPLASH_SCREEN_DELAY_2);
				}

			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			startActivity(new Intent(getApplicationContext(), RestaurantsListScreen.class));
			finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getInstance().getPreferencesManager().clearCrashFlag();		
		/*Locale locale = new Locale(App.getInstance().getPreferencesManager().getString(PreferencesManager.LANGUAGE, "dk"));
		Locale.setDefault(locale);
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());*/		
		if (App.getInstance().getPreferencesManager().contains(PreferencesManager.LANGUAGE)){
		    Configuration config = getBaseContext().getResources().getConfiguration();
		    Locale locale = new Locale (App.getInstance().getPreferencesManager().getString(PreferencesManager.LANGUAGE, "en"));
    		    Locale.setDefault(locale);
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}
		setContentView(R.layout.main);
		new LoginTask().execute();
	}
}