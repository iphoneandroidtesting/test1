package com.nmotion.android.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationService implements LocationListener {

	private LocationManager locationManager;
	private Context context;

	private double userLong = 0;
	private double userLat = 0;
	private boolean isEnabledLocationService = false;

	private LocationReadyListener listener;

	public LocationService(Context context) {
		this.context = context;
	}

	public void getLocation(float maxAccuracy, LocationReadyListener listener) {
		this.listener = listener;
		startLocationService();
	}

	@Override
	public void onLocationChanged(Location location) {
		userLong = location.getLongitude();
		userLat = location.getLatitude();

		if (listener != null) {
			listener.onLocationReady(userLat, userLong);
			stopLocationServices();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private void startLocationService() {
		if (!isEnabledLocationService) {
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
				}
			}

			if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
				if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
				} else {
					if (listener != null) {
						listener.onLocationNotAvailable();
						stopLocationServices();
					}
				}
			} else {
				if (listener != null) {
					listener.onLocationNotAvailable();
					stopLocationServices();
				}
			}

			isEnabledLocationService = true;
		}
	}

	public void stopLocationServices() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
		isEnabledLocationService = false;
	}

	public boolean isGPSEnabled() {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			locationManager.getProvider(LocationManager.GPS_PROVIDER);
			return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			return false;
		}
	}

}
