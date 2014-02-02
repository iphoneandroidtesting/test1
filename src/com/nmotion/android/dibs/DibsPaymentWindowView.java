package com.nmotion.android.dibs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nmotion.android.App;
import com.nmotion.android.DibsPaymentScreen;
import com.nmotion.android.utils.Config;

public class DibsPaymentWindowView extends WebView {
	private static final String HEX_KEY = Config.DEBUG_MODE 
	        ? "572c744e33516b2962422c365841622a5e40652a6c29714d726a43616166592a3a352141324526447a4c55555946306d624d2529787b546f71486e3943705350" //test
	        : "4a64593a2a52565e73566665727e5224593a4c48436d572e367048553a297c527265402d4228397977716839697a45356241627a536772773234262b30654d4f"; //prod

	private static final String mainEntryUrl = "https://sat1.dibspayment.com/dibspaymentwindow/entrypoint/";
	private static final String autorizeTicketUri = "https://api.dibspayment.com/merchant/v1/JSON/Transaction/AuthorizeTicket";
	private static final String captureTransactUri = "https://api.dibspayment.com/merchant/v1/JSON/Transaction/CaptureTransaction";
	private static final String transactionIdKey = "transactionId";
	private static final String statusKey = "status";
	private static final String statusAccepted = "ACCEPTED";
	private static final String statusCancelled = "CANCELLED";
	
	private String acceptReturnUrl;
        private String cancelReturnUrl;
        private String callbackUrl;
        
        private boolean windowIsLoaded;
        
	private PaymentResultListener paymentResultListener;
	public HashMap<String, String> resultParamMap;
	private PaymentData paymentData;	

	public DibsPaymentWindowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DibsPaymentWindowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DibsPaymentWindowView(Context context) {
		super(context);
		init();
	}

	public void setPaymentResultListener(PaymentResultListener paymentResultListener) {
		this.paymentResultListener = paymentResultListener;
	}

	private void init() {
		setWebViewClient(new WebViewClient() {
			@Override
			public void onLoadResource(WebView view, String url) {
			    if (url.contains("https://sat1.dibspayment.com/dibspaymentwindow/ajax/cardpayment/authorizeCard"))
			        ((DibsPaymentScreen)getContext()).setCancelDisallowed(true);
			    super.onLoadResource(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {	
			    System.out.println("starting " + url);
			        if (callbackUrl.equals(url) && !statusCancelled.equals(paymentData.params.get(statusKey))) {
					loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementById(\"returnFormDiv\").innerHTML);");
				} else {
					super.onPageStarted(view, url, favicon);
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {		
			        if (url.endsWith("paymentconfirmation/"))
                                    ((DibsPaymentScreen)getContext()).setCancelDisallowed(false);
			        if (callbackUrl.equals(url) && statusCancelled.equals(paymentData.params.get(statusKey))) {
					paymentResultListener.cancelUrlLoaded();
				} else if (callbackUrl.equals(url) && statusAccepted.equals(paymentData.params.get(statusKey))) {
					paymentResultListener.paymentAccepted(paymentData.params);
				} else if (!windowIsLoaded) {
					paymentWindowLoaded();
				}
				super.onPageFinished(view, url);				
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}		

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				paymentResultListener.failedLoadingPaymentWindow();
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});

		getSettings().setJavaScriptEnabled(true);

		class MyJavaScriptInterface {
			@SuppressWarnings("unused")
			public void processHTML(String html) {
				// Log.d("ss", html);
				resultParamMap = new HashMap<String, String>();

				Pattern statusPattern = Pattern.compile("<input value=\"(.*?)\" name=\"status\" type=\"hidden\">");
				Pattern transactionPattern = Pattern.compile("<input value=\"(.*?)\" name=\"transaction\" type=\"hidden\">");
				Pattern ticketPattern = Pattern.compile("<input value=\"(.*?)\" name=\"ticket\" type=\"hidden\">");
				Pattern cardNumberMaskedPattern = Pattern.compile("<input value=\"(.*?)\" name=\"cardNumberMasked\" type=\"hidden\">");

				String status = getValue(statusPattern, html);
				resultParamMap.put(statusKey, status);
				resultParamMap.put("transaction", getValue(transactionPattern, html));
				resultParamMap.put("ticket", getValue(ticketPattern, html));
				resultParamMap.put("cardNumberMasked", getValue(cardNumberMaskedPattern, html));

				// Log.d("ss", "!! status " + resultParamMap.get("status"));
				// Log.d("ss", "!! transact " + resultParamMap.get("transact"));
				// Log.d("ss", "!! ticket " + resultParamMap.get("ticket"));
				// Log.d("ss", "!! cardNumberMasked " + resultParamMap.get("cardNumberMasked"));
				if (statusAccepted.equals(status)) {
					paymentResultListener.paymentAccepted(resultParamMap);
				} else if (statusCancelled.equals(status)) {
					paymentResultListener.cancelUrlLoaded();
				}
			}
		}
		
		addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
	}

	private String getValue(Pattern pattern, String html) {
		Matcher m = pattern.matcher(html);
		while (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public void loadPaymentWindow(PaymentData paymentData) {
		this.paymentData = paymentData;
		acceptReturnUrl = paymentData.acceptreturnurl;
		cancelReturnUrl = paymentData.cancelreturnurl;
		callbackUrl = paymentData.callbackUrl;
		if (acceptReturnUrl == null) {
			acceptReturnUrl = "";
		}
		if (cancelReturnUrl == null) {
			cancelReturnUrl = "";
		}
		if (paymentData instanceof TicketPurchasePaymentData) {
			new AutorizeAndCaptureWithTicket().execute();
		} else {
			postUrl(mainEntryUrl, EncodingUtils.getBytes(generateOperationUri(paymentData.params), "UTF-8"));
		}
	}

	public String generateOperationUri(Map<String, String> params) {
		String result = "";
		Object[] keys = params.keySet().toArray();
		for (int pos = 0; pos < keys.length; pos++) {
			String key = (String) keys[pos];
			result += String.format("%s=%s", key, URLEncoder.encode((String) params.get(key)));
			if (pos < (keys.length - 1)) {
				result += "&";
			}
		}
		return result;
	}

	class AutorizeAndCaptureWithTicket extends AsyncTask<Void, Void, String> {
		private String readResponse(InputStream is) throws IOException {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		}

		@Override
		protected String doInBackground(Void... args) {		   
                        ((DibsPaymentScreen)getContext()).setCancelDisallowed(true);
			JSONObject requestJSONAuth = createRequestJSONWithMAC(paymentData.params);
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("request", requestJSONAuth.toString()));
			System.out.println("dibs request json: "+requestJSONAuth.toString());
			try {
				final HttpPost postAuth = new HttpPost(autorizeTicketUri);
				postAuth.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

				HttpResponse responseAuth = App.getInstance().getNetworkService().getClient().execute(postAuth);
				String resultAuth = readResponse(responseAuth.getEntity().getContent());
				System.out.println("dibs response json: "+resultAuth);
				String transactionId = new JSONObject(resultAuth).getString(transactionIdKey);
				paymentData.params.put(transactionIdKey, transactionId);

				JSONObject requestJSONCapture = createRequestJSONWithMAC(paymentData.params);

				params.clear();
				params.add(new BasicNameValuePair("request", requestJSONCapture.toString()));

				final HttpPost postCapture = new HttpPost(captureTransactUri);
				postCapture.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

				HttpResponse responseCapture = App.getInstance().getNetworkService().getClient().execute(postAuth);
				String resultCapture = readResponse(responseCapture.getEntity().getContent());
				String status = new JSONObject(resultCapture).getString(statusKey);
				return status;
			} catch (Exception e) {
			        e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			paymentWindowLoaded();
			if ("ACCEPT".equals(result)) {
				acceptCallbackCall();
			} else if ("CANCEL".equals(result)) {
				cancelPayment();
			} else {
				paymentResultListener.failedLoadingPaymentWindow();
			}
			((DibsPaymentScreen)getContext()).setCancelDisallowed(false);
		}
	}

	public void cancelPayment() {
		paymentData.params.put(statusKey, statusCancelled);
		postUrl(cancelReturnUrl, EncodingUtils.getBytes(generateOperationUri(paymentData.params), "UTF-8"));
	}
	
	public void cancelPayment(String cancelReturnUrl, PaymentData data) {
    	        paymentData = data;
                acceptReturnUrl = paymentData.acceptreturnurl;
                cancelReturnUrl = paymentData.cancelreturnurl;
                callbackUrl = paymentData.callbackUrl;
                if (acceptReturnUrl == null) {
                        acceptReturnUrl = "";
                }
                if (cancelReturnUrl == null) {
                        cancelReturnUrl = "";
                }
                paymentData.params.put(statusKey, statusCancelled);

                System.out.println("cancel with params: "+ paymentData.params);
                postUrl(cancelReturnUrl, EncodingUtils.getBytes(generateOperationUri(paymentData.params), "UTF-8"));
        }

	private void acceptCallbackCall() {
		paymentData.params.put(statusKey, statusAccepted);
		postUrl(acceptReturnUrl, EncodingUtils.getBytes(generateOperationUri(paymentData.params), "UTF-8"));
	}

	private void paymentWindowLoaded() {
		windowIsLoaded = true;
		paymentResultListener.paymentWindowLoaded();
	}

	private JSONObject createRequestJSONWithMAC(Map<String, String> additionalHttpHeaders) {
		List<String> sortedKeys = new ArrayList<String>(additionalHttpHeaders.keySet());
		Collections.sort(sortedKeys);

		String result = "";
		Object[] keys = sortedKeys.toArray();
		for (int pos = 0; pos < keys.length; pos++) {
			String key = (String) keys[pos];
			result += String.format("%s=%s", key, (String) additionalHttpHeaders.get(key));
			if (pos < (keys.length - 1)) {
				result += "&";
			}
		}
		additionalHttpHeaders.put("MAC", buildHmacSignature(HEX_KEY, result));
		JSONObject request = new JSONObject(additionalHttpHeaders);
		return request;
	}

	public String hexStringToString(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return new String(data);
	}

	private String buildHmacSignature(String hexKey, String pStringToSign) {
		String key = hexStringToString(hexKey);
		String lSignature = "None";
		try {
			Mac lMac = Mac.getInstance("HmacSHA256");
			SecretKeySpec lSecret = new SecretKeySpec(key.getBytes(), "HmacSHA256");
			lMac.init(lSecret);

			byte[] lDigest = lMac.doFinal(pStringToSign.getBytes());
			BigInteger lHash = new BigInteger(1, lDigest);
			lSignature = lHash.toString(16);
			if ((lSignature.length() % 2) != 0) {
				lSignature = "0" + lSignature;
			}
		} catch (NoSuchAlgorithmException lEx) {
			throw new RuntimeException("Problems calculating HMAC", lEx);
		} catch (InvalidKeyException lEx) {
			throw new RuntimeException("Problems calculating HMAC", lEx);
		}
		return lSignature;
	}

}
