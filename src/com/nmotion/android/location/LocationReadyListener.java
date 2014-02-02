package com.nmotion.android.location;

public interface LocationReadyListener {
	public void onLocationReady(double latitude, double longitude);

	public void onLocationNotAvailable();
}
