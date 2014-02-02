package com.nmotion.android.dibs;

import java.util.ArrayList;

public class PreAuthPurchasePaymentData extends PaymentData {
	public static final String createticketandauthKey = "createticketandauth";

	public PreAuthPurchasePaymentData(String merchantId, String currencyCode, String orderId, long amount, ArrayList<String> payTypes) {
		super(merchantId, currencyCode, orderId, amount, payTypes);
		params.put(createticketandauthKey, "1");
	}

	public void setCalcfee(boolean isCalcfee) {
		params.put("addFee", isCalcfee ? "1" : "0");
	}

}
