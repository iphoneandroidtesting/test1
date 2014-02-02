package com.nmotion.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;

public class ChangeYourLocationScreen extends BaseActivity {

	private TextView distance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_change_your_location);
		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_change_your_location);

		distance = (TextView) findViewById(R.id.txt_distance);

		SeekBar seekBar = (SeekBar) findViewById(R.id.seek_distance);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				App.getInstance().getPreferencesManager().setFloat(PreferencesManager.DISTANCE, seekBar.getProgress() / 10.0f);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				distance.setText(progress / 10.0 + " km");
			}
		});

		seekBar.setProgress((int) (App.getInstance().getPreferencesManager().getFloat(PreferencesManager.DISTANCE, Config.DISTANCE) * 10));
	}

	public void onViewRestaurantsClick(View view) {
		AppUtils.hideKeyBoard(this);
		Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

}
