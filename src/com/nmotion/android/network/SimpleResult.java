package com.nmotion.android.network;

public class SimpleResult {

	public static final int STATUSE_DONE_OK = 11001;
	public static final int STATUSE_DONE_WRONG = 11002;

	int code;
	String message;

	public SimpleResult(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * 
	 * @param result
	 *            if true code = STATUSE_DONE_OK / false code = STATUSE_DONE_WRONG
	 */
	public SimpleResult(boolean result) {
		code = result ? STATUSE_DONE_OK : STATUSE_DONE_WRONG;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
