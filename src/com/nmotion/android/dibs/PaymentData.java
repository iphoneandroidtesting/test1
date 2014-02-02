package com.nmotion.android.dibs;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nmotion.android.App;

public class PaymentData {
	protected Map<String, String> params;

	protected String callbackUrl;
	protected String acceptreturnurl;
	protected String cancelreturnurl;

	public static final String merchantKey = "merchant";
	public static final String fNameKey = "billingFirstName";
	public static final String lNameKey = "billingLastName";
	public static final String timeKey = "debitingTime";
	public static final String addressKey = "debitingAddress";
	public static final String amountKey = "amount";
	public static final String currencyKey = "currency";
	public static final String orderIdKey = "orderId";
	public static final String payTypeKey = "paytype";
	public static final String callbackUrlKey = "callbackUrl";
	public static final String acceptreturnurlKey = "acceptReturnUrl";
	public static final String cancelreturnurlKey = "cancelreturnurl";
	public static final String languageKey = "language";
	public static final String testKey = "test";

	public PaymentData(String merchantId, String currencyCode, String orderId, long amount, ArrayList<String> payTypes) {
		params = new HashMap<String, String>();
		params.put(fNameKey, App.getInstance().getNetworkService().getCurrentUser()
		        .firstName);
		params.put(lNameKey, App.getInstance().getNetworkService().getCurrentUser().lastName);
		params.put(timeKey, new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));		
		params.put(addressKey, "Nmotion ApS, Tinghøjvej 40, 2860 Søborg - CVR. 34803706");
		params.put(merchantKey, merchantId);
		params.put(currencyKey, currencyCode);
		params.put(orderIdKey, orderId);
		params.put(amountKey, String.valueOf(amount));
		if (payTypes != null) {
			setPayTypes(payTypes);
		}
	}

	public void setPayTypes(ArrayList<String> payTypes) {
		StringBuilder payTypesString = new StringBuilder();
		Iterator<String> iterator = payTypes.iterator();
		while (iterator.hasNext()) {
			payTypesString.append(iterator.next());
			if (iterator.hasNext()) {
				payTypesString.append(",");
			}
		}
		params.put(payTypeKey, payTypesString.toString());
	}

	public void setTest() {
		params.put(testKey, "1");
	}

	public void setCallbackUrl(URL url) {
		callbackUrl = url.toString();
		params.put(callbackUrlKey, url.toString());
	}

	public void setAcceptCallbackUrl(URL url) {
		acceptreturnurl = url.toString();
		params.put(acceptreturnurlKey, url.toString());
	}

	public void setCancelUrl(URL url) {
		cancelreturnurl = url.toString();
		params.put(cancelreturnurlKey, url.toString());
	}

	public void setLanguage(String language) {
		params.put(languageKey, language);
	}
}
