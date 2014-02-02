package com.nmotion.android;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.nmotion.R;
import com.nmotion.android.adapters.CreditCardsAdapter;
import com.nmotion.android.database.AppDataSource;
import com.nmotion.android.models.CreditCard;
import com.nmotion.android.models.User;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.view.CreditCardDialog;
import com.nmotion.android.view.CreditCardDialog.EditCreditCardDialogListener;

public class CreditCardsInfoScreen extends ListActivity implements OnItemClickListener {

	private AppDataSource dataSource;
	private ArrayList<CreditCard> creditCards = new ArrayList<CreditCard>();
	private CreditCardsAdapter adapter;
	private ViewAnimator ccEditAnimator;
	private int selectedPosition = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
                setContentView(R.layout.layout_credit_cards_info);
		((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_edit_credit_cards_info);
		findViewById(R.id.btn_menu_sign_in).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_menu_edit).setVisibility(View.VISIBLE);

		ccEditAnimator = (ViewAnimator) findViewById(R.id.cc_edit_animator);

		adapter = new CreditCardsAdapter(this, creditCards);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		dataSource = new AppDataSource(getApplicationContext());
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

	@Override
	protected void onResume() {
		updateUi();
		super.onResume();
	}

	public void onEditClick(View view) {
		if (adapter.isCheckable()) {
			adapter.setCheckableEnable(false);
			((TextView) findViewById(R.id.btn_menu_edit)).setText(R.string.txt_edit);
		} else {
			adapter.setCheckableEnable(true);
			((TextView) findViewById(R.id.btn_menu_edit)).setText(R.string.txt_cancel);
		}
		ccEditAnimator.showNext();
	}

	public void onAddNewCardClick(View view) {
		CreditCardDialog.show(this, null, new EditCreditCardDialogListener() {

			@Override
			public void onEditDone(CreditCard creditCard) {
				openDibsForRegisterCard(creditCard.title);
			}
		});

	}

	public void onDeleteCardClick(View view) {
		User currentUser = App.getInstance().getNetworkService().getCurrentUser();
		if (currentUser != null && selectedPosition != -1) {
			dataSource.open();
			dataSource.deleteCard(creditCards.get(selectedPosition));
			dataSource.close();
			creditCards.remove(selectedPosition);
			updateUi();
		}
	}

	public void onEditCardClick(View view) {
		showEditCardDialog();
	}

	private void showEditCardDialog() {

		if (selectedPosition != -1 && creditCards.get(selectedPosition) != null) {
			CreditCardDialog.show(this, creditCards.get(selectedPosition), new EditCreditCardDialogListener() {

				@Override
				public void onEditDone(CreditCard creditCard) {
					updateSelectedCreditCard(creditCard);
					updateUi();
				}
			});
		}
	}

	private void updateSelectedCreditCard(CreditCard creditCard) {
		User currentUser = App.getInstance().getNetworkService().getCurrentUser();
		if (currentUser != null) {
			dataSource.open();
			dataSource.updateCard(creditCard);
			dataSource.close();
		}
	}

	private void openDibsForRegisterCard(String title) {
		Intent intent = new Intent(this, DibsPaymentScreen.class);
		intent.setAction(DibsPaymentScreen.ACTION_REGISTER_NEW_CARD);
		intent.putExtra(DibsPaymentScreen.CREDIT_CARD_TITLE, title);
		startActivity(intent);
	}

	private void updateUi() {
		User currentUser = App.getInstance().getNetworkService().getCurrentUser();
		if (currentUser != null) {
			dataSource.open();
			creditCards.clear();
			creditCards.addAll(dataSource.getAllCreditCard(currentUser.firstName));
			adapter.notifyDataSetChanged();
			dataSource.close();
			selectedPosition = -1;
			getListView().clearChoices();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (adapter.isCheckable()) {
			getListView().setItemChecked(position, true);
			selectedPosition = position;
		}
	}

}
