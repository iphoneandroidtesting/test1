package com.nmotion.android.models;

public class CreditCard {

	public String title;
	public String number;

	public String card_id;
	public String user;
	//public String userId;

	public CreditCard() {
	}

	public CreditCard(String title, String number, String id) {
		this.title = title;
		this.number = number;
		this.card_id = id;
	}

	public CreditCard(String title, String number, String cardId, String userId) {
		this.title = title;
		this.number = number;
		this.card_id = cardId;
		//this.userId = userId;
	}

}
