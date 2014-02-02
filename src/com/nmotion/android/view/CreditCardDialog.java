package com.nmotion.android.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.core.FieldChecker;
import com.nmotion.android.models.CreditCard;

public class CreditCardDialog extends Dialog implements OnDismissListener, OnClickListener {

	private static CreditCardDialog editCreditCardDialog;
	private EditCreditCardDialogListener cardDialogListener;
	private CreditCard creditCard;
	private EditText edtCardTitle;
	private TextView txtCardNumber;
	private Button doneBtn;
	private Context activity;

	public static void show(Activity activity, CreditCard creditCard, EditCreditCardDialogListener cardDialogListener) {
		editCreditCardDialog = new CreditCardDialog(activity, creditCard, cardDialogListener, true);
		editCreditCardDialog.show();
	}

	protected CreditCardDialog(Activity activity, CreditCard creditCard, EditCreditCardDialogListener cardDialogListener, boolean isFull) {
		super(activity, android.R.style.Theme_Light_NoTitleBar);
		setContentView(R.layout.dialog_edit_credit_card);

		doneBtn = (Button) findViewById(R.id.btn_done);
		doneBtn.setOnClickListener(this);
		edtCardTitle = (EditText) findViewById(R.id.edt_card_title);
		txtCardNumber = (TextView) findViewById(R.id.txt_card_number);
		setOnDismissListener(this);
		this.cardDialogListener = cardDialogListener;
		this.activity = activity;
		if (creditCard != null) {
			this.creditCard = creditCard;
			edtCardTitle.setText(creditCard.title);
			txtCardNumber.setText(creditCard.number);
		} else {
			this.creditCard = new CreditCard();
			txtCardNumber.setVisibility(View.GONE);
			((TextView) findViewById(R.id.txt_card_number_title)).setVisibility(View.GONE);
			doneBtn.setText(R.string.txt_save);
		}
	}

	@Override
	public void onBackPressed() {
		cardDialogListener = null;
		super.onBackPressed();
	}

	public interface EditCreditCardDialogListener {
		public void onEditDone(CreditCard creditCard);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (cardDialogListener != null) {
			cardDialogListener.onEditDone(creditCard);
		}
	}

	@Override
	public void onClick(View v) {
		if (FieldChecker.creditCardTitleCheck(edtCardTitle.getText().toString(), activity)) {
			creditCard.title = edtCardTitle.getText().toString();
			dismiss();
		}
	}
}
