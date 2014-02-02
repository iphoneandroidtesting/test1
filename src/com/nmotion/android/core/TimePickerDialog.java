//package com.nmotion.android.core;
//
//import java.util.Date;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.Window;
//import android.widget.TextView;
//import android.widget.TimePicker;
//import android.widget.TimePicker.OnTimeChangedListener;
//
//import com.nmotion.R;
//import com.nmotion.android.models.DeliveryTimeRange;
//
//public class TimePickerDialog extends Dialog implements OnClickListener {
//	// String result = "";
//	DeliveryTimeRange timeRange;
//	Integer startHour24,startMinute24,finishHour24,finishMinute24;
//	TextView errorMessage;
//	
//	public TimePickerDialog(Context context, final DeliveryTimeRange timeRange) {
//		super(context);
//		this.timeRange=timeRange;
//		convertTimeTo24();
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.time_picker_dialog);
//		findViewById(R.id.time_picker_dialog_button).setOnClickListener(this);
//		findViewById(R.id.time_picker_dialog_cancel_button).setOnClickListener(this);
//		errorMessage = ((TextView)findViewById(R.id.textView2));
//		int diff=new Date().getHours()*60+new Date().getMinutes()-timeRange.dataObtainingLocalMinutes;
//		int result = timeRange.currentRestaurantHour*60+timeRange.currentRestaurantMinute+diff;
//		errorMessage.setText("Sorry, restaurant works only from "+timeRange.startRangeHours+":"+timeRange.startRangeMinutes+" "+(timeRange.startRangePrefix.equals("am") ? "A.M." : (timeRange.startRangePrefix.equals("pm") ? "P.M." : timeRange.startRangePrefix))+" to "+timeRange.finishRangeHours+":"+timeRange.finishRangeMinutes+" "+(timeRange.finishRangePrefix.equals("am") ? "A.M" : (timeRange.finishRangePrefix.equals("pm") ? "P.M" : timeRange.finishRangePrefix))+". (Current restaurant time is "+(result/60==12 ? 12 : ((result/60)%12) )+":" +(result%60<10? "0"+result%60: result%60)+(result/60>=12 ? " P.M." : " A.M.")+")");
//		// result = ((TimePicker)
//		// findViewById(R.id.timePicker1)).getCurrentHour().toString() + ":" +
//		// ((TimePicker) findViewById(R.id.timePicker1)).getCurrentMinute();		
//		((TimePicker) findViewById(R.id.timePicker1)).setCurrentHour(Cache.currentAddress.deliveryHour24==-1 ? finishHour24 : Cache.currentAddress.deliveryHour24);
//		((TimePicker) findViewById(R.id.timePicker1)).setCurrentMinute(Cache.currentAddress.deliveryMinute24==-1 ? (finishMinute24%30==0 ? finishMinute24 : finishMinute24-finishMinute24%30): (Cache.currentAddress.deliveryMinute24%30==0 ? Cache.currentAddress.deliveryMinute24 : Cache.currentAddress.deliveryMinute24-Cache.currentAddress.deliveryMinute24%30));		
//		/*if (selectedTime!=null){
//			String hour="",minute="";
//			for (int i = 0; i < selectedTime.length(); i++) {
//				if (selectedTime.charAt(i)<='9' && selectedTime.charAt(i)>='0' && minute.length()==0)
//					hour+=selectedTime.charAt(i);
//				else if (minute.length()==0)
//					minute+=selectedTime.charAt(i+1);
//				else if (selectedTime.charAt(i)<='9' && selectedTime.charAt(i)>='0')
//					minute+=selectedTime.charAt(i);
//				
//			}
//		}*/
//		((TimePicker) findViewById(R.id.timePicker1)).setOnTimeChangedListener(new OnTimeChangedListener() {	
//			//boolean isError;
//			@Override
//			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//				//isError=false;
//				if (minute==31 || minute==29){
//					view.setCurrentMinute(0);
//					return;
//				}
//				else if (minute==1 || minute==59){
//					view.setCurrentMinute(30);
//					return;
//				}
//				//System.out.println(startHour24+" "+finishHour24);
//				if (hourOfDay>finishHour24){
//					errorMessage.setVisibility(View.VISIBLE);
//					view.setCurrentHour(finishHour24);
//					return;
//					//isError=true;					
//				}
//				if (hourOfDay==finishHour24 && minute>finishMinute24){
//					errorMessage.setVisibility(View.VISIBLE);
//					if (finishMinute24>=30){
//						view.setCurrentMinute(30);//finishMinute24);
//						//return;
//					}
//					else if(finishMinute24<30){						
//						view.setCurrentMinute(0);
//						//return;
//						}
//					return;
//					//isError=true;
//				}
//				if (hourOfDay<startHour24){
//					errorMessage.setVisibility(View.VISIBLE);
//					view.setCurrentHour(startHour24);					
//					return;
//					//isError=true;
//				}
//				if (hourOfDay==startHour24 && minute<startMinute24){
//					errorMessage.setVisibility(View.VISIBLE);
//					if (startMinute24<30){
//					view.setCurrentMinute(30);//startMinute24);
//					//return;
//					}
//					else if (startMinute24>30){
//						view.setCurrentHour(startHour24+1);
//						view.setCurrentMinute(0);
//						
//						//isError=true;
//						//return;
//					}
//					return;
//					//isError=true;
//				}
//				/*if (isError)
//					errorMessage.setVisibility(View.VISIBLE);
//				else
//					errorMessage.setVisibility(View.GONE);*/
//			}
//		});
//	}
//	
//	private void convertTimeTo24(){
//		startHour24=Integer.parseInt(timeRange.startRangeHours);
//		finishHour24=Integer.parseInt(timeRange.finishRangeHours);
//		System.out.println("pref="+timeRange.startRangePrefix);
//		if (timeRange.startRangePrefix.equals("pm"))
//			startHour24+=12;
//		if(timeRange.startRangeHours.equals("12"))
//			if(timeRange.startRangePrefix.equals("am"))
//				startHour24=0;
//			else
//				startHour24=12;
//		
//		if (timeRange.finishRangePrefix.equals("pm"))
//			finishHour24+=12;
//		if(timeRange.finishRangeHours.equals("12"))
//			if(timeRange.finishRangePrefix.equals("am"))
//				finishHour24=0;
//			else
//				finishHour24=12;
//		
//		startMinute24=Integer.parseInt(timeRange.startRangeMinutes);
//		finishMinute24=Integer.parseInt(timeRange.finishRangeMinutes);
//	}
//	
//	@Override
//	public void onClick(View v) {
//		if (v == findViewById(R.id.time_picker_dialog_button)) {
//			String buf = ( ( ((TimePicker) findViewById(R.id.timePicker1)).getCurrentHour()==12 ? 12 : ((TimePicker) findViewById(R.id.timePicker1)).getCurrentHour() % 12) ) + "";
//			if (buf.length() == 1)
//				buf = "0" + buf;
//			Cache.currentAddress.deliveryTime = buf + ":";// + ((TimePicker)
//															// findViewById(R.id.timePicker1)).getCurrentMinute();
//			buf = ((TimePicker) findViewById(R.id.timePicker1)).getCurrentMinute().toString();
//			if (buf.length() == 1)
//				buf = "0" + buf;
//			Cache.currentAddress.deliveryTime += buf;
//			if (((TimePicker) findViewById(R.id.timePicker1)).getCurrentHour() >= 12)
//				Cache.currentAddress.deliveryTime += " P.M.";
//			else
//				Cache.currentAddress.deliveryTime += " A.M.";
//			Cache.currentAddress.deliveryHour24=((TimePicker) findViewById(R.id.timePicker1)).getCurrentHour();
//			Cache.currentAddress.deliveryMinute24=((TimePicker) findViewById(R.id.timePicker1)).getCurrentMinute();
//		}
//		if (v == findViewById(R.id.time_picker_dialog_cancel_button)){
//			Cache.currentAddress.deliveryTime=null;
//			Cache.currentAddress.deliveryHour24=-1;
//			Cache.currentAddress.deliveryMinute24=-1;
//		}
//
//		dismiss();
//	}
//}
