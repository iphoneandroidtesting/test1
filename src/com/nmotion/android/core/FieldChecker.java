package com.nmotion.android.core;

import java.util.Date;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.nmotion.android.LogInScreen;
import com.nmotion.android.RegistrationScreen;
import com.nmotion.android.utils.Utils;

public class FieldChecker {
	static TreeSet<Character> allowedEmailSymbolSet = new TreeSet<Character>();
	static Character[] symbols = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', '.', '@', '-', '_', '+' };

	private static void init() {
		for (int i = 0; i < symbols.length; i++) {
			allowedEmailSymbolSet.add(symbols[i]);
		}
	}

	public static boolean emptyFieldCheck(String[] texts, Activity context) {
		for (int i = 0; i < texts.length; i++) {
			if (texts[i].length() < 1) {
				if (context.getClass() == RegistrationScreen.class)
					switch (i) {
					case 0:
						Utils.showToast(context, "Please enter a valid email address");
						break;
					case 1:
						Utils.showToast(context, "Please, enter a preferred password");
						break;
					case 2:
						Utils.showToast(context, "Please, enter a password confirmation");
						break;
					case 3:
						Utils.showToast(context, "Please, enter your first name");
						break;
					case 4:
						Utils.showToast(context, "Please, enter your last name");
						break;
					case 5:
						Utils.showToast(context, "Please, check your phone number");
						break;
					default:
					        break;
					}
				else if (context.getClass() == LogInScreen.class)
					switch (i) {
					case 0:
						Utils.showToast(context, "Please, enter your email address.");
						break;
					case 1:
						Utils.showToast(context, "Please, enter your password.");
						break;
					default:
                                            break;
					}
				return false;
			}
		}
		return true;
	}

	public static boolean emailFieldCheck(String text, Context context) {
		text = text.toLowerCase();
		if (allowedEmailSymbolSet.isEmpty()) {
			init();
		}
		if (!text.contains("@") || !text.contains(".") || text.charAt(0) < 'a' || text.charAt(0) > 'z' || text.charAt(text.length() - 1) > 'z' || text.charAt(text.length() - 1) < 'a'
				|| text.toString().contains("@.") || text.toString().contains(".@")) {
			Utils.showToast(context, "Please enter a valid email address");
			return false;
		}
		int positionAt = 0;// , positionDot = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '@') {
				if (positionAt != 0) {
					Utils.showToast(context, "Please enter a valid email address");
					return false;
				}
				positionAt = i;
				if ((text.charAt(i + 1) < 'a' || text.charAt(i + 1) > 'z')) {
					Utils.showToast(context, "Please enter a valid email address");
					return false;
				}
			}
			if (text.charAt(i) == '.') {
				if (text.charAt(i + 1) < 'a' || text.charAt(i + 1) > 'z') {
					Utils.showToast(context, "Please enter a valid email address");
					return false;
				}
			}
			if (!allowedEmailSymbolSet.contains(text.charAt(i))) {
				Utils.showToast(context, "Please enter a valid email address");
				return false;
			}
		}
		return true;
	}

	public static boolean passwordFieldCheck(String text, Context context) {
		if (text.length()>0 && text.length() < 6) {
			Utils.showToast(context, "Password should be at least 6 symbols long.");
			return false;
		}
		return true;
	}

	public static boolean isPasswordConfirmed(String text, String text2, Context context) {
		if (!text.equals(text2)) {
			Utils.showToast(context, "Your confirmation password did not match, please try again.");
			return false;
		}
		return true;
	}

	public static boolean nameFieldCheck(String text, Context context) {
		text = text.toLowerCase();
		for (int i = 0; i < text.length(); i++) {
			if ((text.charAt(i) > 'z' || text.charAt(i) < 'a') && text.charAt(i) != ' ') {
				Utils.showToast(context, "Name fields must consist only of letters.");
				return false;
			}
		}
		return true;
	}
	
	public static boolean snameFieldCheck(String text, Context context) {
            text = text.toLowerCase();
            for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) > 'z' || text.charAt(i) < 'a') {
                            Utils.showToast(context, "Name fields must consist only of letters.");
                            return false;
                    }
            }
            return true;
    }

	public static boolean phoneFieldCheck(String text1, String text2, Context context) {
		if (text1.length() != 3 || text2.length() != 7) {
			Utils.showToast(context, "Please, check Your phone number.");
			return false;
		}
		return true;
	}

	public static boolean addressFieldCheck(String text, Context context) {
		return true;
	}

	public static boolean zipCodeFieldCheck(String text, Context context) {
		if (text.length() != 5) {
			Utils.showToast(context, "ZIP code should be 5 digits long.");
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) < '0' || text.charAt(i) > '9') {
				Utils.showToast(context, "ZIP code must contain only numbers.");
				return false;
			}
		}
		return true;
	}

	public static boolean creditCardTitleCheck(String text, Context context) {
		if (TextUtils.isEmpty(text)) {
			Utils.showToast(context, "Title can't be empty");
			return false;
		}
		return true;
	}

	public static boolean creditCardNumberFieldCheck(String text, Context context) {
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == ' ') {
				Utils.showToast(context, "You should not use spaces in credit card number field.");
				return false;
			}
		}
		if (text.length() < 13 || text.length() > 19) {
			Utils.showToast(context, "Credit card number field must contain 13-19 digits.");
			return false;
		}
		return true;
	}

	public static boolean expiryDateFieldCheck(String text, Context context) {
		if (text.length() != 5) {
			Utils.showToast(context, "Expire date field must be entered in format 'MM-YY', for example 03-15 for March, year 2015.");
			return false;
		}
		if (text.charAt(2) != '-') {
			Utils.showToast(context, "Expire date field must be entered in format 'MM-YY', for example 03-15 for March, year 2015.");
			return false;
		}
		if (Integer.parseInt(text.subSequence(0, 2).toString()) < 1 || Integer.parseInt(text.subSequence(0, 2).toString()) > 12) {
			Utils.showToast(context, "Month number must be between 01 and 12...");
			return false;
		}
		if (Integer.parseInt(text.subSequence(3, 5).toString()) > 25) {
			Utils.showToast(context, "Your card can not be valid for such long period... Enter real data, please.");
			return false;
		}
		if (Integer.parseInt(text.subSequence(3, 5).toString()) + 100/* - 1900 */< new Date().getYear()
				|| (Integer.parseInt(text.subSequence(3, 5).toString()) + 100 /*- 1900*/== new Date().getYear() && Integer.parseInt(text.subSequence(0, 2).toString()) - 1 < new Date().getMonth())) {
			Utils.showToast(context, "You can't use credit card, that has already expired...");
			return false;
		}
		return true;
	}

	public static boolean creditCardSecurityCodeFieldCheck(String text, Context context) {
		if (text.length() != 3 && text.length() != 4) {
			Utils.showToast(context, "Security code field must contain 3-4 digits.");
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) < '0' || text.charAt(i) > '9') {
				Utils.showToast(context, "Security code must contain only numbers.");
				return false;
			}
		}
		return true;
	}

}
