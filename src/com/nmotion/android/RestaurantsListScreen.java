package com.nmotion.android;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.adapters.RestaurantsListScreenAdapter;
import com.nmotion.android.location.LocationReadyListener;
import com.nmotion.android.location.LocationService;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;
import com.nmotion.android.view.PullToRefreshListView;

public class RestaurantsListScreen extends BaseActivity {
	private PullToRefreshListView listView;
	private RestaurantsListScreenAdapter restaurantsAdapterAll;
	private LocationService locationService;
	private ProgressDialog findingLocation;
	private DownloadRestaurantListTask downloadRestaurantListTask;
	double latitude;
	double longitude;
	private String findString;
	private ArrayList<Restaurant> restaurants;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_restaurants_list);
		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_restaurants_nearby);
		findViewById(R.id.btn_menu_map).setVisibility(View.VISIBLE);
		Bundle bundle = getIntent().getExtras();

		if (bundle != null && bundle.containsKey("data_search")) {
			findString = bundle.getString("data_search");
		}
		restaurants = App.getInstance().getCache().getRestaurants();

		restaurantsAdapterAll = new RestaurantsListScreenAdapter(getApplicationContext(), restaurants);

		listView = (PullToRefreshListView) findViewById(R.id.list_restaurant);
		updateLocation();

		listView.setAdapter(restaurantsAdapterAll);

		final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
		listView.setOnTouchListener(gestureListener);

		listView.getEditText().addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int length = s.length();
				if (length == 0) {
					download(null);
				} else if (length >= Config.SEARCH_SYMBOLS_LENGTH) {
					download(s.toString());
				}
			}
		});
	}

	private void updateLocation() {
		if (Config.USE_FAKE_LOCATION) {
			this.latitude = Config.FAKE_LAT;
			this.longitude = Config.FAKE_LON;
			download(findString);
		} else {
			findingLocation = AppUtils.showProgressDialog(this, R.string.txt_finding_location, false);
			locationService = new LocationService(this);

			locationService.getLocation(com.nmotion.android.utils.Config.MAX_ACCURACY, new LocationReadyListener() {

				@Override
				public void onLocationReady(double latitude, double longitude) {
    				        try{
    				            findingLocation.dismiss();
    				        }catch(IllegalArgumentException e){}
					RestaurantsListScreen.this.latitude = latitude;
					RestaurantsListScreen.this.longitude = longitude;
					download(findString);
				}

				@Override
				public void onLocationNotAvailable() {
					findingLocation.dismiss();
					setListAdapter();
					showWarningDialog();
				}
			});
		}
	}

	private void showWarningDialog() {
		AlertDialog dialog = new Builder(this).create();

		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.txt_location_not_available));
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.txt_turn_on), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				download(findString);
				dialog.dismiss();
			}
		});
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		setIntent(null);
		// restaurantsAdapterAll.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause() {	
	    super.onPause();
	    if (downloadRestaurantListTask != null) {
                downloadRestaurantListTask.cancel(true);
            }
	}

	public void onTryAgainClick(View view) {
		findString = null;
		updateLocation();
	}

	private void download(String search) {
		if (downloadRestaurantListTask != null) {
			downloadRestaurantListTask.cancel(true);
		}
		downloadRestaurantListTask = new DownloadRestaurantListTask();
		downloadRestaurantListTask.execute(String.valueOf(latitude), String.valueOf(longitude), search);
	}

	private class DownloadRestaurantListTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			listView.setEnabled(false);
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(String... arg0) {
			try {
				ArrayList<Restaurant> restaurants = App.getInstance().getNetworkService().getRestaurants(arg0[0], arg0[1], arg0[2], RestaurantsListScreen.this);
				if (!isCancelled())
					App.getInstance().getCache().setRestaurants(restaurants);
			} catch (NetworkException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			listView.setEnabled(true);
			setListAdapter();
			findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
		}
	}

	private void setListAdapter() {
		restaurantsAdapterAll.notifyDataSetChanged();
		if (restaurants.isEmpty()) {
			findViewById(R.id.list_restaurant_empty).setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			findViewById(R.id.list_restaurant_empty).setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int pos = listView.getAdapterView().pointToPosition((int) e.getX(), (int) e.getY());
			onItemClick(pos);
			return false;
		}
	}

	private void onItemClick(int pos) {
		if (pos >= 0 && pos < restaurants.size()) {
			Intent intent = new Intent(getApplicationContext(), RestaurantInfoScreen.class);
			intent.putExtra(RestaurantInfoScreen.DATA_RESTAURANT_ID, restaurants.get(pos).id);
			startActivity(intent);
		}
	}
	
	@Override
	public void onBackPressed() {
	if (listView.getEditText().getText().length()>0){
	    findString = null;
            listView.getEditText().setText("");
	}
	else
	    super.onBackPressed();
	}
}
