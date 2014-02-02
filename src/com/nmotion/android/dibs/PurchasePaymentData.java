package com.nmotion.android.dibs;

import java.util.ArrayList;

public class PurchasePaymentData extends PaymentData {

	public PurchasePaymentData(String merchantId, String currencyCode, String orderId, long amount, ArrayList<String> payTypes) {
		super(merchantId, currencyCode, orderId, amount, payTypes);
	}

	public void setCalcfee(boolean isCalcfee) {
		params.put("addFee", isCalcfee?"1":"0");
	}
	
}
