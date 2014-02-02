package com.nmotion.android.dibs;

import java.util.ArrayList;

public class PreAuthorizationPaymentData extends PaymentData {
	public static final String createticketKey = "createticket";

	public PreAuthorizationPaymentData(String merchantId, String currencyCode, String orderId, ArrayList<String> payTypes, boolean toRegisterCard) {
		super(merchantId, currencyCode, orderId, 1, payTypes);
		params.put(createticketKey, "1");
		params.put("s_registerCard", toRegisterCard ? "1" : "0");
	}

}
