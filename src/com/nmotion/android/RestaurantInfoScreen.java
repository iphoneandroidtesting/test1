package com.nmotion.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.image.loader.ImageFetcher;
import com.nmotion.R;
import com.nmotion.android.models.User;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.ActionHelper;
import com.nmotion.android.utils.AppUtils;

public class RestaurantInfoScreen extends BaseRestaurantScreen {
        //private View checkInLayout;
        private TextView openCloseTxt, restaurantFeedback, restaurantName, restaurantAddress, restaurantDistance, restaurantPhone, restaurantWebsite, restaurantVideo, restaurantDescription; 
        private ImageView restaurantImage;
        private ImageFetcher imageResizer;
        private View videoUrlLayout, addressLayout, phoneLayout, urlLayout, feedbackUrlLayout, phoneAndUrlLayout;
        //private CheckBox checkIn;

	//private PreferencesManager preferencesManager;

	private Button btnMenu;

	//private boolean isMenuButtonEnabled;
	private boolean isRestaurantOpen;
	//private boolean isRestaurantTakeAway;
	private UpdateRestaurantTask restCheckTask;
	//private User currentUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		enableSignInButton();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_restaurant_info);
		//checkInLayout=findViewById(R.id.checkin_layout);
		addressLayout = findViewById(R.id.address_layout);
		videoUrlLayout = findViewById(R.id.video_url_layout);
		phoneLayout = findViewById(R.id.phone_layout);
		urlLayout = findViewById(R.id.url_layout);
		feedbackUrlLayout = findViewById(R.id.feedback_url_layout);
		phoneAndUrlLayout = findViewById(R.id.phone_and_url_layout);
		
		openCloseTxt = (TextView) findViewById(R.id.openclose_txt);
		restaurantName = (TextView) findViewById(R.id.txt_restaurant_name);
                restaurantAddress = (TextView) findViewById(R.id.txt_restaurant_address);
                restaurantDistance = (TextView) findViewById(R.id.txt_restaurant_distance);
                restaurantPhone = (TextView) findViewById(R.id.txt_restaurant_phone);
                restaurantWebsite = (TextView) findViewById(R.id.txt_restaurant_website);
                restaurantVideo = (TextView) findViewById(R.id.txt_restaurant_video);
                restaurantFeedback = (TextView) findViewById(R.id.txt_restaurant_feedback);
                restaurantDescription = (TextView) findViewById(R.id.txt_restaurant_description);
                restaurantImage = (ImageView) findViewById(R.id.img_restaurant_logo);
		btnMenu = (Button) findViewById(R.id.btn_restaurant_menu);
		/*checkIn = (CheckBox) findViewById(R.id.check_in);
		checkIn.setOnCheckedChangeListener(changeListener);*/
		imageResizer = new ImageFetcher(this, 0, 0);
		imageResizer.setLoadingImage(R.drawable.photo_def_big);		
		reinitUI();
	}
	
	private void reinitUI(){
	    //isRestaurantTakeAway = mRestaurant.isTakeAway;
            isRestaurantOpen = mRestaurant.isOpen;
            if (isRestaurantOpen){                
                    openCloseTxt.setText("Open");
                    openCloseTxt.setTextColor(Color.GREEN);
            } else{
                openCloseTxt.setText("Close");
                openCloseTxt.setTextColor(Color.RED);
            }
            //isMenuButtonEnabled = false;
	    if (mRestaurant != null) {
                imageResizer.loadImage(mRestaurant.image, restaurantImage);
                restaurantName.setText(mRestaurant.name);
                if (mRestaurant.address==null && mRestaurant.postalCode==null && mRestaurant.city==null)
                    addressLayout.setVisibility(View.GONE);
                else{
                    restaurantAddress.setText(TextUtils.concat(mRestaurant.address, ", ", mRestaurant.postalCode, ", ", mRestaurant.city));
                    restaurantDistance.setText(mRestaurant.getDistance(getString(R.string.txt_distance_string)));
                }
                if (mRestaurant.phone==null && mRestaurant.siteUrl==null)
                    phoneAndUrlLayout.setVisibility(View.GONE);
                else{
                    if (mRestaurant.phone==null)
                        phoneLayout.setVisibility(View.GONE);
                    else
                        restaurantPhone.setText(mRestaurant.phone);
                    if (mRestaurant.siteUrl==null)
                        urlLayout.setVisibility(View.GONE);
                    else
                        restaurantWebsite.setText(mRestaurant.siteUrl);
                }
                if (mRestaurant.videoUrl==null)
                    videoUrlLayout.setVisibility(View.GONE);
                else
                    restaurantVideo.setText(mRestaurant.videoUrl);
                if (mRestaurant.feedbackUrl!=null){
                    if (!mRestaurant.feedbackUrl.endsWith("/"))
                        mRestaurant.feedbackUrl=mRestaurant.feedbackUrl+"/";
                    restaurantFeedback.setText(mRestaurant.feedbackUrl.subSequence(
                            /*(mRestaurant.siteUrl.contains("http:") ? (mRestaurant.siteUrl.indexOf("http:")+6) : 0)*/0, 
                            mRestaurant.feedbackUrl.indexOf(
                                    '/',
                                    (mRestaurant.feedbackUrl.contains("http:") ? mRestaurant.feedbackUrl.indexOf("http:")+7 : (mRestaurant.feedbackUrl.contains("https:") ? mRestaurant.feedbackUrl.indexOf("https:")+8 : 0)))
                            )
                    );
                }else
                    feedbackUrlLayout.setVisibility(View.GONE);
                restaurantDescription.setText(mRestaurant.briefDescription);                    
            } else
                finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//currentUser = App.getInstance().getNetworkService().getCurrentUser();
		//preferencesManager = App.getInstance().getPreferencesManager();
		restCheckTask = new UpdateRestaurantTask();
		restCheckTask.execute();
	}
	
	@Override
	protected void onPause() {	
	    super.onPause();
	    restCheckTask.cancel(true);
	}

	private void setRestaurantStatus() {
		/*if (/*isRestaurantTakeAway*//*preferencesManager.isCheckedIn(currentUser.eMail, mRestaurantId) && preferencesManager.getCheckInMode()!=PreferencesManager.NO_CHECKIN_MODE && preferencesManager.getCheckInMode()!=PreferencesManager.TAKE_AWAY_CHECKIN_MODE) {
			/*checkInLayout.setVisibility(View.INVISIBLE);
			//isMenuButtonEnabled = isRestaurantOpen;
		} else {*/
		/*        checkInLayout.setVisibility(View.VISIBLE);
			checkIn.setEnabled(isRestaurantOpen);
			if (!isRestaurantOpen)
			    checkIn.setTextColor(getResources().getColor(R.color.ddark_gray));
			checkIn.setOnCheckedChangeListener(null);
			checkIn.setChecked(true);//!preferencesManager.isLastCheckinedRestaurantTakeaway() && preferencesManager.isCheckedIn(currentUser.eMail, mRestaurantId));
			checkIn.setOnCheckedChangeListener(changeListener);
			//isMenuButtonEnabled = isRestaurantOpen && checkIn.isChecked();
		}else
		    checkInLayout.setVisibility(View.INVISIBLE);*/
		//btnMenu.setEnabled(isRestaurantOpen/*isMenuButtonEnabled*/);
                if (!btnMenu.isEnabled())
                    btnMenu.setTextColor(getResources().getColor(R.color.light_gray));
                else
                    btnMenu.setTextColor(getResources().getColor(R.color.black));
	}

	/*private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			buttonView.setChecked(!isChecked);
			if (!isChecked) {
				onTableClick(null);
			} else {
				if (App.getInstance().getCache().getOrders().size() == 0) {
					new CheckOutTask().execute(mRestaurant.id);
				} else {
					AppUtils.showContinueCancelDialog(RestaurantInfoScreen.this, R.string.txt_your_basket_will_be_cleaned, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							new CheckOutTask().execute(mRestaurant.id);
							dialog.dismiss();
						}
					});
				}
			}
		}
	};*/

/*	public void onTableClick(View view) {
	    RestaurantCheckInDialog dialog = new RestaurantCheckInDialog(this, mRestaurant);
	    dialog.show();
	    dialog.setOnDismissListener(new OnDismissListener() {                
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (((RestaurantCheckInDialog)dialog).getStatus() == RESULT_OK) {                        
                                saveCheckin();
                                onMenuClick(null);                        
                    }
                }
            });
	}*/

	public void onMenuClick(View view) {
	    AlertDialog dialog = new AlertDialog.Builder(this).create();
	    dialog.setTitle("Warning");
	    dialog.setMessage(getString(R.string.restaurent_closed_message));
	    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
                }
            });
	    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new OnClickListener() {                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); 
                    startCategoriesScreen();
                }
            });
	    if (!isRestaurantOpen)
	        dialog.show();
	    else
	        startCategoriesScreen();
		/*if (isRestaurantTakeAway) {
			if (preferencesManager.isCheckedIn(currentUser.eMail, mRestaurantId)) {
				startCategoriesScreen();
			} else {
				if (preferencesManager.isUserAlreadyCheckedin(currentUser) && App.getInstance().getCache().getOrders().size() > 0) {
					AppUtils.showContinueCancelDialog(this, R.string.txt_your_basket_will_be_cleaned, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							new CheckInTakeAwayTask().execute((int) mRestaurant.id);
							dialog.dismiss();
						}
					});
				} else {
					new CheckInTakeAwayTask().execute((int) mRestaurant.id);
				}
			}
		} else {
			if (checkIn.isChecked()) {
				startCategoriesScreen();
			} else {
				onTableClick(null);
			}
		}*/
	}

	public void onPhoneClick(View view) {
		String phone = mRestaurant.phone;
		if (phone != null) {
			ActionHelper.actionDial(this, phone);
		}
	}

	public void onWebClick(View view) {
		String siteUrl = mRestaurant.siteUrl;
		if (siteUrl != null) {
			ActionHelper.actionBrowser(this, siteUrl);
		}
	}
	
	public void onFeedbackClick(View view) {
	        String siteUrl = mRestaurant.feedbackUrl;
	        if (siteUrl != null) {
	            ActionHelper.actionBrowser(this, siteUrl);
	        }
	}
	
	public void onVideoClick(View view) {
            String siteUrl = mRestaurant.videoUrl;
            if (siteUrl != null) {
                    ActionHelper.actionBrowser(this, siteUrl);
            }
    }

	private void startCategoriesScreen() {
	    User user = App.getInstance().getNetworkService().getCurrentUser();
            if (user.id==-1){
                Intent intent = new Intent(this, LogInScreen.class);
                intent.putExtra("orderMealMode", true);
                startActivity(intent);
                return;
            }
		Intent intent = new Intent(this, CategoriesScreen.class);
		intent.putExtra(BaseRestaurantScreen.DATA_RESTAURANT_ID, mRestaurant.id);
		startActivity(intent);
	}

/*	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 0) {
				saveCheckin();
				onMenuClick(null);
			}
		}
	}*/

	/*private void saveCheckin() {
		preferencesManager.checkIn(currentUser.eMail, mRestaurant.id, isRestaurantTakeAway);
		checkIn.setOnCheckedChangeListener(null);
		checkIn.setChecked(true);
		checkIn.setOnCheckedChangeListener(changeListener);
	}*/
	
	private class UpdateRestaurantTask extends AsyncTask<Void, Void, Void> {
            private ProgressDialog progressDialog;
            
            @Override
            protected void onPreExecute() {
                    progressDialog = AppUtils.showProgressDialog(RestaurantInfoScreen.this, "Checking restaurant status...", false);
                    progressDialog.setCancelable(true);
                    progressDialog.setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface arg0) {
                            RestaurantInfoScreen.this.finish();
                        }
                    });
                    super.onPreExecute();
            }
            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    App.getInstance().getAppDataSource().updatetRestaurant(App.getInstance().getNetworkService().getRestaurant(mRestaurantId, RestaurantInfoScreen.this));
                    mRestaurant = App.getInstance().getCache().getRestaurantById(mRestaurantId, true);
                } catch (NetworkException e) {
                    e.printStackTrace();
                }   
                return null;
            }	    
            
            @Override
            protected void onPostExecute(Void result) {
                reinitUI();
                setRestaurantStatus();                
                progressDialog.dismiss();
            }
	}

	/*private class CheckOutTask extends AsyncTask<Integer, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(RestaurantInfoScreen.this, R.string.txt_checkouting_from_restaurant, false);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Integer... arg0) {
			try {
				return App.getInstance().getNetworkService().checkOut(arg0[0], RestaurantInfoScreen.this);
			} catch (NetworkException e) {
				if (e.getHttpCode() == 304) {
					return true;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result) {
				preferencesManager.checkOut(currentUser.eMail, mRestaurant.id);
				if (!isRestaurantOpen)
	                            checkIn.setTextColor(getResources().getColor(R.color.ddark_gray));
				checkIn.setOnCheckedChangeListener(null);
				checkIn.setChecked(false);
				checkIn.setOnCheckedChangeListener(changeListener);
//				btnMenu.setEnabled(isRestaurantOpen && checkIn.isChecked());
				if (!btnMenu.isEnabled())
				    btnMenu.setTextColor(getResources().getColor(R.color.light_gray));
				else
				    btnMenu.setTextColor(getResources().getColor(R.color.black));
				AppUtils.showToast(RestaurantInfoScreen.this, R.string.txt_you_have_checked_out);
				checkInLayout.setVisibility(View.INVISIBLE);
			} else {
				AppUtils.showToast(RestaurantInfoScreen.this, R.string.txt_error);
			}
		}
	}*/

	/*private class CheckInTakeAwayTask extends AsyncTask<Integer, Void, SimpleResult> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(RestaurantInfoScreen.this, R.string.txt_loading, false);
			super.onPreExecute();
		}

		@Override
		protected SimpleResult doInBackground(Integer... arg0) {
			int restaurantId = arg0[0];
			int table = (int) (Math.random()*65535);//Random().nextInt(Integer.MAX_VALUE) + 1;
			int isForce = -1;
			int isTableEmpty = -1;
			try {
				App.getInstance().getNetworkService().checkIn(restaurantId, table, isForce, isTableEmpty, RestaurantInfoScreen.this);
			} catch (NetworkException e) {
				return new SimpleResult(e.getHttpCode(), e.getMessage());
			}
			return new SimpleResult(true);
		}

		@Override
		protected void onPostExecute(SimpleResult result) {
			progressDialog.dismiss();
			switch (result.getCode()) {
			case SimpleResult.STATUSE_DONE_OK:
				saveCheckin();
				startCategoriesScreen();
				break;
			case NetworkException.HTTP_CODE_PRECONDITION_FAILED:
				AppUtils.showToast(getApplicationContext(), result.getMessage());
				break;
			default:
				AppUtils.showToast(getApplicationContext(), R.string.txt_error_try_again);
				break;
			}
		}
	}*/
}
