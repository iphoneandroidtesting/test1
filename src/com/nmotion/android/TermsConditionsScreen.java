package com.nmotion.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.utils.AppUtils;

public class TermsConditionsScreen extends Activity {
	private WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_terms_conditions);

		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_registration);

		if (!App.getInstance().getNetworkService().isLoggedIn()) {
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.VISIBLE);
		}
		webView = (WebView) findViewById(R.id.terms_web_view);
		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
		webView.loadUrl("file:///android_asset/" + getString(R.string.terms_page));
	}
	
	@Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            if (App.getInstance().getPreferencesManager().isCrash()){
                AppUtils.showDialog(this, "Error", getString(R.string.crash_message)).setOnDismissListener(new OnDismissListener() {                    
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();                        
                    }
                });
            }
        }

	public void onSignInClick(View view) {
		Intent intent = new Intent(getApplicationContext(), LogInScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
