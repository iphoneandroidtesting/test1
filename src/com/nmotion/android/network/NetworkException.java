package com.nmotion.android.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkException extends Exception {

	public static final int STATUS_OK = 0;
	public static final int ERROR_NETWORK = -1;

	public static final int EXCEPTION_CODE_ORDER_BEING_PAID = 1005001;
	public static final int EXCEPTION_CODE_ORDER_ALREADY_HAS_BEEN_PAID = 1005002;
	public static final int EXCEPTION_CODE_MEAL_NOT_AVAILABLE_1 = 10050021;
	public static final int EXCEPTION_CODE_MEAL_NOT_AVAILABLE_2 = 10050022;
	public static final int EXCEPTION_CODE_MEAL_NOT_AVAILABLE_3 = 10050023;
	public static final int EXCEPTION_CODE_MEAL_NOT_AVAILABLE_4 = 10050024;
	public static final int EXCEPTION_CODE_SUBMIT_IF_TABLE_EMPTY = 1002;

	public static final int HTTP_CODE_NOT_MODIFIED = 304;
	public static final int HTTP_CODE_PRECONDITION_FAILED = 412;
	public static final int HTTP_CODE_UPDATE = 426;
	public static final int HTTP_CODE_OK = 200;

	private static final long serialVersionUID = -5412404771197473649L;

	private static final String successKey = "success";
	private static final String statusKey = "status";
	private static final String exceptionCodeKey = "exception_code";
	private static final String statusCodeKey = "status_code";
	private static final String statusTextKey = "status_text";
	private static final String currentContentKey = "current_content";
	private static final String messageKey = "message";

	public String error;
	public int responseCode = -1;
	private int exceptionCode;
	private int httpCode;
	private boolean success;
	private String status;
	private int statusCode;
	private Object statusText;
	private Object currentContent;
	private String message;

	public NetworkException() {
	}

	public NetworkException(int errorCode, String error) {
		this.error = error;
		this.responseCode = errorCode;
		this.exceptionCode = errorCode;
	}

	public NetworkException(int httpCode, JSONObject response) {
		this.httpCode = httpCode;
		parseResponse(response);
	}

	public static void isNetworkError(JSONObject response) throws NetworkException {
		if (response != null) {
			if (response.optInt("code", STATUS_OK) != STATUS_OK || response.optInt("status_code", STATUS_OK) != STATUS_OK) {
				throw new NetworkException(response);
			}
		} else {
			throw new NetworkException();
		}
	}

	public static void checkResponse(int httpCode, JSONObject response) throws NetworkException {
		NetworkException networkException = new NetworkException(httpCode, response);
		if (response == null || !networkException.success) {
			throw networkException;
		}
		networkException = null;
	}

	public NetworkException(JSONObject response) {
		responseCode = response.optInt("code", ERROR_NETWORK);
		error = response.optString(messageKey, null);
		exceptionCode = response.optInt(exceptionCodeKey, -1);
		statusCode = response.optInt(statusCodeKey, -1);
	}

	protected void parseResponse(JSONObject response) {
		success = response.optBoolean(successKey, false);
		status = response.optString(statusKey, null);
		exceptionCode = response.optInt(exceptionCodeKey, -1);
		statusCode = response.optInt(statusCodeKey, -1);
		statusText = response.optString(statusTextKey, null);
		currentContent = response.optString(currentContentKey, null);
		message = response.optString(messageKey, "Wrong server response");
		// TODO: UGLY need to check errors format on server, and delete
		handleOldFormat(response);
	}

	private void handleOldFormat(JSONObject response) {

		JSONArray errors;
		try {
			errors = response.getJSONArray("errors");

			try {
				JSONArray emailArray = errors.getJSONObject(0).getJSONArray("email");
				String groupMessage = "";
				for (int i = 0; i < emailArray.length(); i++) {
					groupMessage = groupMessage.concat(" ").concat(emailArray.getString(i));
				}
				message = groupMessage;
			} catch (JSONException e1) {
				try {
					message = errors.getString(0);
				} catch (JSONException e2) {
				}
			}
		} catch (JSONException e) {
		}
	}

	public int getResponseCode() {
		return responseCode;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getStatus() {
		return status;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Object getStatusText() {
		return statusText;
	}

	public Object getCurrentContent() {
		return currentContent;
	}

	public String getMessage() {
		return message;
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public boolean isNotModifiedError() {
		return httpCode == HTTP_CODE_NOT_MODIFIED;
	}
}
