package com.nmotion.android;

import java.lang.Thread.UncaughtExceptionHandler;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.nmotion.android.core.Cache;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.database.AppDataSource;
import com.nmotion.android.network.NetworkService;

@ReportsCrashes(formKey = "dEphTXJGM3RtZmk1VVNZMURUbmZKOXc6MQ")
public final class App extends Application {
	private static com.nmotion.android.App instance;
	private PreferencesManager preferenceManager;
	private NetworkService networkService;
	private Cache cache;

	private AppDataSource appDataSource;
	private String fullDeviceId;

	public void onCreate() {
		super.onCreate();		
		ACRA.init(this);
		instance = this;
		initalize();
	}

	public static App getInstance() {
		return instance;		
	}

	void initalize() {
		String deviceId = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
		String serial = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		this.fullDeviceId = deviceId + (serial == null ? "" : serial);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {                    
                    @Override
                    public void uncaughtException(Thread thread, Throwable ex) {
                        ex.printStackTrace();
                        App.getInstance().getPreferencesManager().setCrashFlag();                        
                        App.getInstance().getNetworkService().logOut();                        
                        System.exit(0);
                    }
                });
		preferenceManager = new PreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));
		networkService = new NetworkService(this);
		cache = new Cache();
	}

	public String getDeviceId() {
		return fullDeviceId;
	}

	public PreferencesManager getPreferencesManager() {
		return preferenceManager;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public Cache getCache() {
		return cache;
	}

	public AppDataSource getAppDataSource() {
		if (appDataSource == null) {
			appDataSource = new AppDataSource(getApplicationContext());
		}
		return appDataSource;
	}
}
