package com.nmotion.android.view;

import java.util.Date;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.method.DateTimeKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nmotion.R;
import com.nmotion.android.App;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.models.User;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.network.SimpleResult;
import com.nmotion.android.utils.AppUtils;

public class RestaurantCheckInDialog extends Dialog implements OnClickListener {
    int status;
    public static final String DATA_RESTAURANT_ID = "restaurant_id";
    private EditText tableNumber, roomNumber;
    private String tableId;
    private User currentUser;
    View joinBtn, roomNumberLayout, tableNumberLayout;
    Restaurant mRestaurant;
    Context context;
    RadioGroup radioGroup;
    RadioButton radio1, radio2, radio3;
    int i;
    TimePicker timePicker;
    DatePicker datePicker;

    public RestaurantCheckInDialog(Context context, Restaurant mRestaurant) {
        super(context);
        this.context = context;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_table_checkin);
        joinBtn = findViewById(R.id.joinBtn);
        roomNumberLayout = findViewById(R.id.room_number_layout);
        tableNumberLayout = findViewById(R.id.table_number_layout);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        radio1 = (RadioButton) findViewById(R.id.radio0);
        radio2 = (RadioButton) findViewById(R.id.radio1);
        radio3 = (RadioButton) findViewById(R.id.radio2);
        tableNumber = (EditText) findViewById(R.id.txt_table_number);
        roomNumber = (EditText) findViewById(R.id.txt_room_number);
        
        /**
         * Following fragment of code calculates current date and adds 15 minutes to it,
         * to initialize Date and Time pickers.
         * */
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        // Adding 15 minutes to current time, predefined interval requested by client
        calendar.add(Calendar.MINUTE, 15);
        Date orderTime = calendar.getTime();
        
        timePicker = (TimePicker) findViewById(R.id.timePicker1);
        datePicker = (DatePicker) findViewById(R.id.datePicker1);
        
        
        /**
         * Since there is a possibility that some restaurants can work 24/7 or just in interval
         * that involves date change in the middle of work day e.g. from 12:00 to 01:00
         * we should consider this in calculation of default pick up time, thus we're using calendar object
         * to calculate exact date and time in 15 minutes of current time and date.
         */
        datePicker.updateDate(orderTime.getYear(), orderTime.getMonth(), orderTime.getDay());
        timePicker.setCurrentHour(orderTime.getHours());
        timePicker.setCurrentMinute(orderTime.getMinutes());
        
        
        if (!mRestaurant.isInHouse)
            radio1.setVisibility(View.GONE);
        else {
            roomNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
            tableNumberLayout.setBackgroundColor(Color.WHITE);
            roomNumber.setEnabled(false);
            tableNumber.setEnabled(true);
        }
        if (!mRestaurant.isRoomService)
            radio3.setVisibility(View.GONE);
        if (!mRestaurant.isTakeAway){
            radio2.setVisibility(View.GONE);
            timePicker.setVisibility(View.GONE);
            datePicker.setVisibility(View.GONE);
        }
        if (!mRestaurant.isTakeAway && !mRestaurant.isRoomService && !mRestaurant.isInHouse){
            roomNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
            tableNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
            roomNumber.setEnabled(false);
            tableNumber.setEnabled(false);
            Toast.makeText(getContext(), "Sorry, you cannot check-in to current restaurant at the moment", Toast.LENGTH_LONG).show();
        }
            
        for (i = 0; i < radioGroup.getChildCount(); i++) {
            if (((RadioButton)radioGroup.getChildAt(i)).getVisibility()==View.VISIBLE){
            ((RadioButton)radioGroup.getChildAt(0)).post(new Runnable() {                
                @Override
                public void run() {
                    ((RadioButton)radioGroup.getChildAt(i)).setChecked(true);
                }
            });
            break;
            }
        }
        
        ((RadioButton)radioGroup.getChildAt(0)).setChecked(true);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {            
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio0:
                        roomNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
                        tableNumberLayout.setBackgroundColor(Color.WHITE);
                        roomNumber.setEnabled(false);
                        tableNumber.setEnabled(true);
                        timePicker.setVisibility(View.GONE);
                        datePicker.setVisibility(View.GONE);
                        break;
                    case R.id.radio1:
                        roomNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
                        tableNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
                        roomNumber.setEnabled(false);
                        tableNumber.setEnabled(false);
                        timePicker.setVisibility(View.VISIBLE);
                        datePicker.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radio2:      
                        tableNumberLayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_gray));
                        roomNumberLayout.setBackgroundColor(Color.WHITE);
                        roomNumber.setEnabled(true);
                        tableNumber.setEnabled(false);
                        timePicker.setVisibility(View.GONE);
                        datePicker.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });
        joinBtn.setOnClickListener(this);
        /*TextView restaurantName = (TextView) findViewById(R.id.txt_restaurant_name);
        restaurantName.setText(mRestaurant.name);*/
        
        /*tableNumber.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(tableNumber, 0);
                
            }
        }, 500);*/
        currentUser = App.getInstance().getNetworkService().getCurrentUser();
        this.mRestaurant = mRestaurant;
    }
    
    public int getCheckInMode(){
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio0:
                return PreferencesManager.IN_HOUSE_CHECKIN_MODE;
            case R.id.radio1:
                return PreferencesManager.TAKE_AWAY_CHECKIN_MODE;
            case R.id.radio2:
                return PreferencesManager.ROOM_SERVICE_CHECKIN_MODE;
            default:
                return PreferencesManager.NO_CHECKIN_MODE;
        }
    }
    
    public boolean isAllowedPickupTime(Date pickupDate) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        Date minDate = calendar.getTime();
        return pickupDate.getTime() >= minDate.getTime();
    }
    
    public Date getCheckInDateTime() {
    	Date date = new Date();
        date.setYear(datePicker.getYear());
        date.setMonth(datePicker.getMonth());
        date.setDate(datePicker.getDayOfMonth());
        date.setHours(timePicker.getCurrentHour());
        date.setMinutes(timePicker.getCurrentMinute());
        return date;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.joinBtn:
                final String number = getCheckInMode()==PreferencesManager.IN_HOUSE_CHECKIN_MODE ? tableNumber.getText().toString() : (getCheckInMode()==PreferencesManager.ROOM_SERVICE_CHECKIN_MODE ? roomNumber.getText().toString() : "-1");
                if (TextUtils.isEmpty(number)) {
                    AppUtils.showToast(getContext(), R.string.txt_pls_enter_table_number);
                } else {
                    Date date = getCheckInDateTime();
                    if (number.equals("-1") && !isAllowedPickupTime(date)){
                        AlertDialog dialog = new AlertDialog.Builder(context).create();
                        dialog.setTitle("Warning");
                        dialog.setMessage("You have selected time that is less then in an 15 minutes or even is in the past. In such case the order will be left for tomorrow.\n\nDo you agree?");
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Change time", new DialogInterface.OnClickListener() {                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Agree", new DialogInterface.OnClickListener() {                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                tableId = number;
                                if (tableId.length() > 0 || getCheckInMode()==PreferencesManager.TAKE_AWAY_CHECKIN_MODE) {
                                    boolean isLastRestaurantIsTakeAway = App.getInstance().getPreferencesManager()
                                            .getCheckInMode()==PreferencesManager.TAKE_AWAY_CHECKIN_MODE;
                                    boolean isBasketEmpty = App.getInstance().getCache().getOrders().size() == 0;
                                    boolean isUserAlreadyChekedin = App.getInstance().getPreferencesManager()
                                            .isUserAlreadyCheckedin(currentUser);

                                    if (isUserAlreadyChekedin && isLastRestaurantIsTakeAway && isBasketEmpty
                                            || !isUserAlreadyChekedin) {
                                        new CheckInTask().execute(mRestaurant.id/*, tableId*/);
                                    } else {
                                        showCheckinWarningDialog(/*tableId, */isLastRestaurantIsTakeAway, isBasketEmpty);
                                    }
                                } else {
                                    AppUtils.showToast(getContext(), R.string.txt_table_number_more_then_null);
                                }
                            }
                        });
                        dialog.show();
                        break;
                    }
                    tableId = number;
                    if (tableId.length() > 0 || getCheckInMode()==PreferencesManager.TAKE_AWAY_CHECKIN_MODE) {
                        boolean isLastRestaurantIsTakeAway = App.getInstance().getPreferencesManager()
                                .getCheckInMode()==PreferencesManager.TAKE_AWAY_CHECKIN_MODE;
                        boolean isBasketEmpty = App.getInstance().getCache().getOrders().size() == 0;
                        boolean isUserAlreadyChekedin = App.getInstance().getPreferencesManager()
                                .isUserAlreadyCheckedin(currentUser);

                        if (isUserAlreadyChekedin && isLastRestaurantIsTakeAway && isBasketEmpty
                                || !isUserAlreadyChekedin) {
                            new CheckInTask().execute(mRestaurant.id);
                        } else {
                            showCheckinWarningDialog(isLastRestaurantIsTakeAway, isBasketEmpty);
                        }
                    } else {
                        AppUtils.showToast(getContext(), R.string.txt_table_number_more_then_null);
                    }
                }
                break;
            default:
                break;
        }
    }

    public int getStatus() {
        return status;
    }

    private void showCheckinWarningDialog(boolean isLastRestaurantIsTakeAway, boolean isBasketEmpty) {
        AlertDialog dialog = new Builder(getContext()).create();
        dialog.setCancelable(false);
        int messageId = R.string.txt_you_already_checkined;
        if (!isLastRestaurantIsTakeAway && !isBasketEmpty) {
            messageId = R.string.txt_you_already_checkined_and_basket_cleaned;
        } else if (isLastRestaurantIsTakeAway && !isBasketEmpty) {
            messageId = R.string.txt_your_basket_will_be_cleaned;
        }
        dialog.setMessage(getContext().getString(messageId));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.txt_continue), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new CheckInTask().execute((int) mRestaurant.id/*, tableId*/);
                dialog.dismiss();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.txt_cancel), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                R.drawable.gray_red_btn_background_selector);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                R.drawable.gray_red_btn_background_selector);
    }

    private class CheckInTask extends AsyncTask<Integer, Void, SimpleResult> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = AppUtils.showProgressDialog(getContext(), R.string.txt_checkining_to_restaurant, false);
            super.onPreExecute();
        }

        @Override
        protected SimpleResult doInBackground(Integer... arg0) {
            int restaurantId = arg0[0];
            String table = tableId;
            int isForce = -1;
            int isTableEmpty = -1;
            if (arg0.length > 1) {
                isForce = arg0[1];
                isTableEmpty = arg0[2];
            }
            try {
                App.getInstance().getNetworkService().checkIn(restaurantId, table, isForce, isTableEmpty, context, getCheckInMode(), getCheckInMode()==PreferencesManager.TAKE_AWAY_CHECKIN_MODE ? (int)getCheckInDateTime().getTime() : -1);
            } catch (NetworkException e) {
                if (e.getHttpCode() == NetworkException.HTTP_CODE_UPDATE)
                    return new SimpleResult(e.getHttpCode(), e.getMessage());
                else if (e.getExceptionCode() > 0) {
                    return new SimpleResult(e.getExceptionCode(), e.getMessage());
                } else if (e.getHttpCode() == 304) {
                    // not modified http answer
                    return new SimpleResult(true);
                } else {
                    return new SimpleResult(e.getHttpCode(), e.getMessage());
                }
            }
            return new SimpleResult(true);
        }

        @Override
        protected void onPostExecute(SimpleResult result) {
            progressDialog.dismiss();
            switch (result.getCode()) {
                case 409:
                    showDialogJoinTable();
                    break;
                case NetworkException.EXCEPTION_CODE_SUBMIT_IF_TABLE_EMPTY:
                    showDialogSubmitEmpty();
                    break;
                case 412:
                    AppUtils.showToast(getContext(), R.string.txt_restaurant_closed);
                    break;
                case SimpleResult.STATUSE_DONE_OK:
                    checkinDoneOk();
                    break;
                case NetworkException.HTTP_CODE_UPDATE:
                    break;
                default:
                    AppUtils.showToast(getContext(), R.string.txt_error);
                    break;
            }
        }
    }

    private void checkinDoneOk() {
        // AppUtils.hideKeyBoard((Activity)getContext());
        AppUtils.showToast(getContext(), getCheckInMode() == PreferencesManager.ROOM_SERVICE_CHECKIN_MODE ? R.string.txt_room_was_checked_in : (getCheckInMode() == PreferencesManager.TAKE_AWAY_CHECKIN_MODE ? R.string.txt_you_were_checked_in : R.string.txt_table_was_checked_in));
        status = Activity.RESULT_OK;
        dismiss();
    }

    private void checkinDoneCancel() {
        // AppUtils.hideKeyBoard((Activity)getContext());
        status = Activity.RESULT_CANCELED;
        dismiss();
    }

    private void showDialogJoinTable() {
        AlertDialog dialog = new Builder(getContext()).create();
        dialog.setCancelable(false);
        dialog.setMessage(String.format(getContext().getString(getCheckInMode()==PreferencesManager.ROOM_SERVICE_CHECKIN_MODE ? R.string.txt_join_to_the_room : R.string.txt_join_to_the_table), tableId));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(android.R.string.yes),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CheckInTask().execute(mRestaurant.id/*, tableId*/, 1, -1);
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(android.R.string.no),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkinDoneCancel();
                    }
                });
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                R.drawable.gray_red_btn_background_selector);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                R.drawable.gray_red_btn_background_selector);
    }

    private void showDialogSubmitEmpty() {
        AlertDialog dialog = new Builder(getContext()).create();
        dialog.setCancelable(false);
        dialog.setMessage(String.format(getContext().getString(getCheckInMode()==PreferencesManager.ROOM_SERVICE_CHECKIN_MODE ? R.string.txt_submit_thats_room_empty : R.string.txt_submit_thats_table_empty), tableId));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(android.R.string.yes),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CheckInTask().execute(mRestaurant.id/*, tableId*/, -1, 1);
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(android.R.string.no),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkinDoneCancel();
                    }
                });
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                R.drawable.gray_red_btn_background_selector);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                R.drawable.gray_red_btn_background_selector);
    }
}
