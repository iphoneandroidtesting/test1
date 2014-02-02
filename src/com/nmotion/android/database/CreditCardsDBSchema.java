package com.nmotion.android.database;

import android.net.Uri;

public interface CreditCardsDBSchema {
	Uri BASE_CONTENT_URI = Uri.parse("content://" + AppSQLiteHelper.CONTENT_AUTHORITY);
	Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.CREDIT_CARDS_TABLE).build();

	public enum CreditCardsColumns {
		_ID("_id", DBType.INT),
		USER_ID("user_id", DBType.NUMERIC),
		USER_NAME("user_name", DBType.TEXT), 
		CARD_TITLE("card_title", DBType.TEXT), 
		CARD_NUMBER("card_number", DBType.NUMERIC),
		CARD_ID("card_id", DBType.NUMERIC);

		private String columnName;
		private DBType type;

		CreditCardsColumns(String columnName, DBType type) {
			this.columnName = columnName;
			this.type = type;
		}

		public String getName() {
			return columnName;
		}

		public DBType getType() {
			return type;
		}
		
		@Override
		public String toString() {
			return getName();
		}
	}
	
	public interface CreditCardsColumnInt {
		int _ID = 0;
		int USER_ID = 1;
		int USER_NAME = 2;
		int CARD_TITLE = 3;
		int CARD_NUMBER = 4;
		int CARD_ID = 5;
	}
}
