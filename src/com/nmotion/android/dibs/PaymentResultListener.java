package com.nmotion.android.dibs;

import java.util.Map;

public abstract interface PaymentResultListener {
	public abstract void paymentAccepted(Map<String, String> paramMap);

	public abstract void paymentCancelled(Map<String, String> paramMap);

	public abstract void paymentWindowLoaded();

	public abstract void cancelUrlLoaded();

	public abstract void failedLoadingPaymentWindow();
}