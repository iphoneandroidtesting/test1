package com.nmotion.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;

import com.nmotion.R;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.view.TopPanelLayout;

public class BaseRestaurantScreen extends Activity {
    public static final String DATA_RESTAURANT_ID = "restaurant_id";
    private ViewStub container;
    protected int mRestaurantId;
    protected Restaurant mRestaurant;
    private boolean mIsShowSignInButton;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (App.getInstance().getPreferencesManager().isCrash()) {
            AppUtils.showDialog(this, "Error", getString(R.string.crash_message)).setOnDismissListener(
                    new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_restaurant_base);
        container = (ViewStub) findViewById(R.id.base_restaurant_screen_container);
        if (getIntent().getExtras() != null) {
            mRestaurantId = getIntent().getExtras().getInt(DATA_RESTAURANT_ID);
        } else {
            mRestaurantId = App.getInstance().getCache().getCurrentRestaurantId();
        }
        if (mRestaurantId > 0) {
            ((TopPanelLayout) findViewById(R.id.top_panel_layout)).updatePanelInfo(mRestaurantId, mIsShowSignInButton);
            mRestaurant = App.getInstance().getCache().getRestaurantById(mRestaurantId, true);
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (App.getInstance().getPreferencesManager().isCrash()) finish();
        if (mRestaurantId > 0) {
            ((TopPanelLayout) findViewById(R.id.top_panel_layout)).updatePanelInfo(mRestaurantId, mIsShowSignInButton);
            mRestaurant = App.getInstance().getCache().getRestaurantById(mRestaurantId, true);
        } else {
            finish();
        }
    }

    protected void enableSignInButton() {
        mIsShowSignInButton = true;
    }

    @Override
    public void setContentView(int layoutResID) {
        container.setLayoutResource(layoutResID);
        container.inflate();
        container = null;
    }

    public void onSignInClick(View view) {
        singIn();
    }

    private void singIn() {
        Intent intent = new Intent(getApplicationContext(), LogInScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onSignOutClick(View view) {
        if (App.getInstance().getCache().getOrders().size() == 0) {
            signOut();
        } else {
            AppUtils.showContinueCancelDialog(this, R.string.txt_your_basket_will_be_cleaned, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    signOut();
                    dialog.dismiss();
                }
            });
        }
    }

    private void signOut() {
        App.getInstance().getPreferencesManager().clear();
        App.getInstance().getNetworkService().logOut();
        App.getInstance().getCache().clear();

        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

}
