package com.nmotion.android.dibs;

import java.net.URL;
import java.util.ArrayList;

public class TicketPurchasePaymentData extends PaymentData {
	public static final String ticketIdKey = "ticketId";

	public TicketPurchasePaymentData(String merchantId, String currencyCode, String orderId, String ticketId, long amount) {
		super(merchantId, currencyCode, orderId, amount, null);
		params.clear();
		params.put("merchantId", merchantId);
		params.put("amount", String.valueOf(amount));
		params.put("currency", currencyCode);
		params.put("orderId", orderId);
		params.put("ticketId", ticketId);
	}

	@Override
	public void setPayTypes(ArrayList<String> payTypes) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setLanguage(String language) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTest() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCallbackUrl(URL url) {
		super.setCallbackUrl(url);
		params.remove(callbackUrlKey);
	}

	@Override
	public void setAcceptCallbackUrl(URL url) {
		super.setAcceptCallbackUrl(url);
		params.remove(acceptreturnurlKey);
	}

	@Override
	public void setCancelUrl(URL url) {
		super.setCancelUrl(url);
		params.remove(cancelreturnurlKey);
	}
}
