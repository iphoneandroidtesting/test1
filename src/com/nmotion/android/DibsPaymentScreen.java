package com.nmotion.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nmotion.R;
import com.nmotion.android.adapters.CreditCardsAdapter;
import com.nmotion.android.database.AppDataSource;
import com.nmotion.android.dibs.DibsPaymentWindowView;
import com.nmotion.android.dibs.PaymentData;
import com.nmotion.android.dibs.PaymentResultListener;
import com.nmotion.android.dibs.PreAuthPurchasePaymentData;
import com.nmotion.android.dibs.PreAuthorizationPaymentData;
import com.nmotion.android.dibs.PurchasePaymentData;
import com.nmotion.android.dibs.TicketPurchasePaymentData;
import com.nmotion.android.models.CreditCard;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.User;
import com.nmotion.android.network.NetworkService;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;

public class DibsPaymentScreen extends Activity implements PaymentResultListener {
	private static final String TAG = DibsPaymentScreen.class.getSimpleName();
	private final String MERCHAND_ID = Config.DEBUG_MODE ? "90151377" : "90150157";//test
	//private final String MERCHAND_ID = "90150157";//prod
	public static boolean toUpdateOrderAfterCancel;
	private DibsPaymentWindowView dipsPaymentWindow;
	private AppDataSource dataSource;
        private OrderDetails orderDetails;
        private ProgressDialog progressDialog;
        
        private ArrayList<String> payTypes;     
        private ArrayList<CreditCard> creditCards;
        
	private long amount;
	private int currentAction;
	
	private String currencyCode = "DKK";
	private String cardTicketForUse = "";
	private String cardNoMask = "";
	private String cardTitle;
	private String yourOrderId;	

	private boolean isSaveCardEnabled;
	private boolean isCancelDisallowed;
	private boolean isLoggedIn;
	private boolean isCanceled;
        private boolean isAccepted;
        private boolean toRegisterCard;

	public static final String CREDIT_CARD_TITLE = "title";
	public static final String ACTION_REGISTER_NEW_CARD = "register_new_card";
	public static final String ACTION_SAVE_THEN_PAY = "save_then_pay";
	public static final String ACTION_PAY_AFTER_DISCRETE_SAVE = "pay_after_discrete_save";

	private static final int ACTION_PAY_NEW_WITH_SAVE = 10;
	private static final int ACTION_PAY_WITH_SAVED_CARD = 11;
	private static final int ACTION_PAY_NEW = 12;
	private static final int ACTION_SAVE_CARD = 13;
	private static final int ACTION_SAVE_PAY_CODE = 14;
	private static final int ACTION_PAY_AFTER_SAVE_CODE = 15;


	public void setCancelDisallowed(boolean isCancelDisallowed) {
            this.isCancelDisallowed = isCancelDisallowed;
        }

        @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dibs_payment_screen_layout);
		dataSource = new AppDataSource(this);
		payTypes = new ArrayList<String>();

		payTypes.add("VISA");
		payTypes.add("MC");
		payTypes.add("DK");
		payTypes.add("MTRO");
		payTypes.add("ELEC");
		payTypes.add("JCB");
		payTypes.add("AMEX");

		progressDialog = AppUtils.showProgressDialog(this, R.string.txt_loading, false);
		dipsPaymentWindow = (DibsPaymentWindowView) findViewById(R.id.dibs_payment);
		dipsPaymentWindow.setPaymentResultListener(this);
		isLoggedIn = App.getInstance().getNetworkService().isLoggedIn();
		if (ACTION_PAY_AFTER_DISCRETE_SAVE.equals(getIntent().getAction()))
                    currentAction = ACTION_PAY_AFTER_SAVE_CODE;
                if (ACTION_SAVE_THEN_PAY.equals(getIntent().getAction())) {     
                        currentAction = ACTION_SAVE_PAY_CODE;
                        dataSource.open();
                        creditCards = dataSource.getAllCreditCard(App.getInstance().getNetworkService().getCurrentUser().firstName);
                        orderDetails = App.getInstance().getCache().getOrderDetails();
                        amount = orderDetails.consolidatedOrderTotalInCents > 0 ? orderDetails.consolidatedOrderTotalInCents : orderDetails.orderTotalInCents;
                        yourOrderId = String.valueOf(orderDetails.orderId);
                        if (isLoggedIn && creditCards.size()>0){                                
                               showChooseCardDialog();
                        }else{
                            //yourOrderId = "registerCard";
                            cardTitle = getIntent().getStringExtra(CREDIT_CARD_TITLE);
                            //dipsPaymentWindow.loadPaymentWindow(constructPaymentData());
                            showSaveCardDialog();
                        }
                } else if (ACTION_REGISTER_NEW_CARD.equals(getIntent().getAction())) {
			currentAction = ACTION_SAVE_CARD;
			yourOrderId = "registerCard";
			toRegisterCard = true;
			cardTitle = getIntent().getStringExtra(CREDIT_CARD_TITLE);
			dipsPaymentWindow.loadPaymentWindow(constructPaymentData());
		} else {
			orderDetails = App.getInstance().getCache().getOrderDetails();
			amount = orderDetails.consolidatedOrderTotalInCents > 0 ? orderDetails.consolidatedOrderTotalInCents : orderDetails.orderTotalInCents;
			yourOrderId = String.valueOf(orderDetails.orderId);
			if (isLoggedIn) {
				AppDataSource dataSource = new AppDataSource(this);
				dataSource.open();
				creditCards = new ArrayList<CreditCard>();
				creditCards.addAll(dataSource.getAllCreditCard(App.getInstance().getNetworkService().getCurrentUser().firstName));
				if (creditCards.size() > 0) {
					showChooseCardDialog();
				} else {
					showSaveCardDialog();
				}
			} else {
			    if (currentAction == ACTION_PAY_AFTER_SAVE_CODE){
			        creditCards = new ArrayList<CreditCard>();
			        creditCards.add(App.getInstance().getCache().getLastUsedCard());
			        showChooseCardDialog();
			    }
			    else
				showSaveCardDialog();
			}
		}
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

	private PaymentData constructPaymentData() {
		if (TextUtils.isEmpty(cardTicketForUse)) {
			if (currentAction != ACTION_SAVE_CARD && currentAction != ACTION_SAVE_PAY_CODE) {
				if (isSaveCardEnabled) {
					currentAction = ACTION_PAY_NEW_WITH_SAVE;
				} else {
					currentAction = ACTION_PAY_NEW;
				}
			}
		} else {
			currentAction = ACTION_PAY_WITH_SAVED_CARD;
		}
		PaymentData paymentData = null;
		boolean isCalcfee = true;
		switch (currentAction) {
        		case ACTION_PAY_NEW:
        			paymentData = new PurchasePaymentData(MERCHAND_ID, currencyCode, yourOrderId, amount, payTypes);
        			((PurchasePaymentData) paymentData).setCalcfee(isCalcfee);
        			break;
        		case ACTION_PAY_NEW_WITH_SAVE:
        			paymentData = new PreAuthPurchasePaymentData(MERCHAND_ID, currencyCode, yourOrderId, amount, payTypes);
        			((PreAuthPurchasePaymentData) paymentData).setCalcfee(isCalcfee);
        			break;
        		case ACTION_PAY_WITH_SAVED_CARD:
        			paymentData = new TicketPurchasePaymentData(MERCHAND_ID, currencyCode, yourOrderId, cardTicketForUse, amount);
        			break;
        		case ACTION_SAVE_PAY_CODE:
        		case ACTION_SAVE_CARD:
        			paymentData = new PreAuthorizationPaymentData(MERCHAND_ID, currencyCode, yourOrderId, payTypes, toRegisterCard);
        			break;
        		default:
        			paymentData = null;
        			break;
		}
		// Set this flag to "true", if you want to be able to use test cards.
		// REMEMBER to reset this to false, in production !!!
		if (paymentData != null) {
		    if (Config.DEBUG_MODE)
			paymentData.setTest();
			try {
				paymentData.setAcceptCallbackUrl(new URL(Config.URL_CALLBACK));
				paymentData.setCancelUrl(new URL(Config.URL_CALLBACK));
				paymentData.setCallbackUrl(new URL(Config.URL_CALLBACK));
			} catch (MalformedURLException e) {
				Log.e(TAG, "Callback urls not seted");
			}

			paymentData.setLanguage("en_UK");
		}
		return paymentData;
	}

	private void showAfterPaymentDialog() {
		AlertDialog dialog = new Builder(this).create();

		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.txt_thank_after_pay_for_anonimous));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.txt_register), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startRegisterScreen();
				dialog.dismiss();
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.txt_continue), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startCategoriesScreen();
				dialog.dismiss();
			}
		});

		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
	}

	private void showSaveCardDialog() {
		AlertDialog.Builder builder = new Builder(this);

		builder.setCancelable(false);
		if (isLoggedIn) {
			builder.setMessage(R.string.txt_save_card_question);
		} else {
			builder.setMessage(R.string.txt_save_card_question_for_anonimous);
		}

		builder.setPositiveButton(getString(R.string.dialog_btn_ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			        //yourOrderId = "registerCard";s
			        toRegisterCard = true;
				isSaveCardEnabled = true;
				dipsPaymentWindow.loadPaymentWindow(constructPaymentData());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.dialog_btn_cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isSaveCardEnabled = false;
				if (currentAction == ACTION_SAVE_PAY_CODE)
				    currentAction = ACTION_PAY_NEW;
				dipsPaymentWindow.loadPaymentWindow(constructPaymentData());
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
	}

	private void showChooseCardDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.txt_choose_card_for_pay);
		CreditCardsAdapter creditCardsAdapter = new CreditCardsAdapter(this, creditCards);
		creditCardsAdapter.setCheckableEnable(true);
		builder.setSingleChoiceItems(creditCardsAdapter, 0, null);

		builder.setPositiveButton(getString(android.R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cardTicketForUse = creditCards.get(((AlertDialog) dialog).getListView().getCheckedItemPosition()).card_id;
				dipsPaymentWindow.loadPaymentWindow(constructPaymentData());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(android.R.string.cancel), new OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progressDialog.dismiss();
                        dipsPaymentWindow.cancelPayment(Config.URL_CALLBACK, constructPaymentData());
                        toUpdateOrderAfterCancel=true;
                    }
                });
		builder.setNeutralButton(getString(R.string.pay_with_new_card), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				if (currentAction == ACTION_SAVE_PAY_CODE){				    
                                    //yourOrderId = "registerCard";

                                    cardTitle = getIntent().getStringExtra(CREDIT_CARD_TITLE);
                                    //dipsPaymentWindow.loadPaymentWindow(constructPaymentData());
                                    showSaveCardDialog();
                                }
                                else if (currentAction==ACTION_PAY_AFTER_SAVE_CODE){
                                    if (!isLoggedIn)
                                        App.getInstance().getCache().storeLastUsedCard(null);
                                    //finish();
                                    dipsPaymentWindow.cancelPayment(Config.URL_CALLBACK, constructPaymentData());
                                }
                                else
                                    showSaveCardDialog();
			}

		});		
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
	}
	
	private void showSimpleThanksDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.txt_thank_after_pay);
		builder.setPositiveButton(getString(android.R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				startCategoriesScreen();
			}
		});
		AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(
                        R.drawable.gray_red_btn_background_selector);

	}

	private void startCategoriesScreen() {
		Intent intent = new Intent(getApplicationContext(), CategoriesScreen.class);
		intent.putExtra(CategoriesScreen.DATA_RESTAURANT_ID, App.getInstance().getCache().getCurrentRestaurantId());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void startRegisterScreen() {
		Intent intent = new Intent(getApplicationContext(), RegistrationScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	

	@Override
	public void onBackPressed() {
	    if (isCancelDisallowed)
	        return;
	    dipsPaymentWindow.cancelPayment();
	    toUpdateOrderAfterCancel=true;
	    if (isCanceled || isAccepted) {
	        super.onBackPressed();
	    }
	}

	@Override
	public void paymentAccepted(Map<String, String> paymentData) {
		// Log.d(TAG, "paymentAccepted: " + paymentData);
		isAccepted = true;
		cardNoMask = paymentData.get("cardNumberMasked");
		if (TextUtils.isEmpty(cardTitle) && !TextUtils.isEmpty(cardNoMask)) {
			cardTitle = cardNoMask.substring(cardNoMask.length() - 4);
		}
		CreditCard creditCard = null;
		cardTicketForUse = paymentData.get("ticket");
		if (currentAction == ACTION_SAVE_CARD || currentAction == ACTION_SAVE_PAY_CODE) {
			creditCard = new CreditCard(cardTitle, cardNoMask, cardTicketForUse, App.getInstance().getDeviceId());
			saveCreditCard(creditCard);
			finish();
			if (currentAction == ACTION_SAVE_PAY_CODE){
			    if (!isLoggedIn){
			        App.getInstance().getCache().storeLastUsedCard(new CreditCard(cardTitle, cardNoMask, cardTicketForUse, App.getInstance().getDeviceId()));
			    }
                            Intent intent = new Intent(getApplicationContext(), DibsPaymentScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.setAction(ACTION_PAY_AFTER_DISCRETE_SAVE);
                            startActivity(intent);
                        }
		} else {
			App.getInstance().getCache().deleteOrderedMeals();
			if (isSaveCardEnabled) {
				creditCard = new CreditCard(cardTitle, cardNoMask, cardTicketForUse, App.getInstance().getDeviceId());
			}
			if (isLoggedIn) {
				saveCreditCard(creditCard);
				showSimpleThanksDialog();
			} else {
				App.getInstance().getCache().storeLastUsedCard(creditCard);
				showAfterPaymentDialog();
			}
		}
	}

	private void saveCreditCard(CreditCard creditCard) {
		if (creditCard != null) {
			User user = null;
			user = App.getInstance().getNetworkService().getCurrentUser();
			creditCard.user = user.firstName;
			dataSource.open();
			dataSource.addCard(creditCard);
			dataSource.close();
		}
	}	

	private void hideDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void paymentCancelled(Map<String, String> paymentData) {
		hideDialog();
		finish();
	}

	@Override
	public void paymentWindowLoaded() {
		hideDialog();
	}

	@Override
	public void cancelUrlLoaded() {
            toUpdateOrderAfterCancel=true;
	    if (orderDetails!=null){
	        orderDetails.orderStatus=NetworkService.ORDER_STATUS_CANCELLED;
	        dataSource.setOrderDetails(orderDetails);
	    }
	    isCanceled = true;
	    hideDialog();
	    finish();
	}

	@Override
	public void failedLoadingPaymentWindow() {
		hideDialog();
		Toast.makeText(this, "Payment error. Maybe your order is too small. Minimum order price is 0.1 of your currency unit (10 euro cents for example)", Toast.LENGTH_LONG).show();
		finish();
	}
}
