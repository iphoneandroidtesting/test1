package com.nmotion.android.database;

enum DBType {
	INT(" INTEGER,"), FLOAT(" FLOAT,"), TEXT(" TEXT,"), NUMERIC(" NUMERIC,"), BLOB(" BLOB,");

	private String name;

	DBType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
