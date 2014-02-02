package com.nmotion.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nmotion.R;
import com.nmotion.android.core.FieldChecker;
import com.nmotion.android.core.ResponseResultDialog;
import com.nmotion.android.core.TermsConditionsDialog;
import com.nmotion.android.database.AppDataSource;
import com.nmotion.android.models.CreditCard;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Utils;

public class RegistrationScreen extends Activity {
	private EditText[] editTexts = new EditText[5];
	private TextView txt_terms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_registration);

		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_registration);

		editTexts[0] = (EditText) findViewById(R.id.registration_screen_email_edit_text);
		editTexts[1] = (EditText) findViewById(R.id.registration_screen_password_edit_text);
		editTexts[2] = (EditText) findViewById(R.id.registration_screen_confirm_password_edit_text);
		editTexts[3] = (EditText) findViewById(R.id.registration_screen_first_name_edit_text);
		editTexts[4] = (EditText) findViewById(R.id.registration_screen_last_name_edit_text);

		findViewById(R.id.register_screen_register_button).setOnClickListener(registerButtonClickListener);
		txt_terms = (TextView) findViewById(R.id.terms_conditions_text_view_2);
		txt_terms.setText(Html.fromHtml("<u>" + getString(R.string.txt_terms_and_conditions) + "</u>"));
		txt_terms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), TermsConditionsScreen.class);
				startActivity(intent);
			}
		});
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

	private OnClickListener registerButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (FieldChecker.emptyFieldCheck(new String[] { editTexts[0].getText().toString(), editTexts[1].getText().toString()/*.toLowerCase()*/, editTexts[2].getText().toString()/*.toLowerCase()*/,
					editTexts[3].getText().toString(), editTexts[4].getText().toString() }, RegistrationScreen.this)
					&& FieldChecker.emailFieldCheck(editTexts[0].getText().toString(), v.getContext())
					&& FieldChecker.passwordFieldCheck(editTexts[1].getText().toString(), v.getContext())
					&& FieldChecker.isPasswordConfirmed(editTexts[1].getText().toString()/*.toLowerCase()*/, editTexts[2].getText().toString()/*.toLowerCase()*/, v.getContext())
					/*&& FieldChecker.nameFieldCheck(editTexts[3].getText().toString(), v.getContext()) && FieldChecker.snameFieldCheck(editTexts[4].getText().toString(), v.getContext())*/) {
				if (((CheckBox) findViewById(R.id.check_terms_conditions)).isChecked()) {
					if (Utils.isNetworkAvailable(getApplicationContext())) {
						new RegisterTask().execute();
					} else {
						Utils.showToast(getApplicationContext(), getString(R.string.network_unavailable_message));
					}
				} else {
					new TermsConditionsDialog(RegistrationScreen.this).show();
				}
			}
		}
	};

	private class RegisterTask extends AsyncTask<Void, Void, Void> {
		String response;
		int responseCode;
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = AppUtils.showProgressDialog(RegistrationScreen.this, R.string.txt_registation_user, false);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				App.getInstance().getNetworkService()
						.register(editTexts[0].getText().toString(), editTexts[1].getText().toString()/*.toLowerCase()*/, editTexts[3].getText().toString(), editTexts[4].getText().toString(), RegistrationScreen.this);

				response = getString(R.string.txt_registered_successfully);
			} catch (NetworkException e) {
				response = e.getMessage();
				responseCode = e.responseCode;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (responseCode == 0) {
				Toast.makeText(getApplicationContext(), R.string.txt_user_was_successfully_registered, Toast.LENGTH_LONG).show();

				AppDataSource dataSource = new AppDataSource(getApplicationContext());
				CreditCard creditCard = App.getInstance().getCache().getLastUsedCard();
				if (creditCard != null) {
					creditCard.user = editTexts[3].getText().toString();
					dataSource.open();
					dataSource.addCard(creditCard);
					dataSource.close();
				}
				Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			} else {
				ResponseResultDialog customizeDialog = new ResponseResultDialog(RegistrationScreen.this, response, responseCode);
				customizeDialog.show();
			}
		}
	}

}
