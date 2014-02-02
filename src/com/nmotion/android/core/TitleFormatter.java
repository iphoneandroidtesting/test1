//package com.nmotion.android.core;
//
//import android.view.Display;
//import android.widget.TextView;
//
//public class TitleFormatter {
//	public static void formatTitle(String input, Display display, TextView tv){
//		String output="";
//		input=input.toUpperCase();
//		for (int i = 0; i < input.length(); i++) {
//			if (input.charAt(i)==' '){
//				output+=(input.length()>17 ? "         " : "     ");
//			}else if(i!=input.length()-1)
//				output+=input.charAt(i)+" ";
//			else
//				output+=input.charAt(i);
//		}
//		if(display.getWidth()<241 || input.length()>17){
//			tv.setTextSize(16);
//		}
//		
//		tv.setText(output);	
//	}
//	public static void formatRestaurantName(String input, int limit, TextView tv){
//		String output="";
//		tv.setText(output);	
//	}
//	
//}
