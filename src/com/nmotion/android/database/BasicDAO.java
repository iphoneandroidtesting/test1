package com.nmotion.android.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.utils.Logger;

public abstract class BasicDAO<T> {

	protected final SQLiteDatabase db;
	private final String TABLE_NAME;
	protected InsertHelper insertHelper;

	public BasicDAO(SQLiteDatabase db, String TABLE_NAME) {
		this.db = db;
		this.TABLE_NAME = TABLE_NAME;
		insertHelper = new InsertHelper(db, TABLE_NAME);
	}

	// Table info getters

	// ContentValues helpers

	protected abstract ContentValues createValues(T item);

	protected abstract T parseValues(ContentValues values);

	// Store, Read, Delete

	public void storeItem(T item) {
		db.insert(TABLE_NAME, null, createValues(item));
	}

	public void storeItems(List<T> items) {
		try {
			db.beginTransaction();
			for (T item : items) {
				insertHelper.insert(createValues(item));
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		insertHelper.close();
	}

	public abstract List<T> readItems();

	protected List<T> readItems(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		List<T> list = new ArrayList<T>();
		try{
		    final Cursor c = db.query(TABLE_NAME, null, selection, selectionArgs, groupBy, having, orderBy);
		
		if (c.moveToFirst()) {
			do {
				ContentValues values = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(c, values);
				try {
					list.add(parseValues(values));
				} catch (Exception e) {
					Logger.warning(e.toString());
				}
			} while (c.moveToNext());
		}
		c.close();
		}catch(IllegalStateException e){
                    e.printStackTrace();
                }
		return list;
	}

	protected int updateItem(ContentValues values, String whereClause, String[] whereArgs) {
		return db.update(TABLE_NAME, values, whereClause, whereArgs);
	}

	public abstract void deleteItem(T item);

	public void deleteItem(List<T> items) {
		for (T item : items)
			deleteItem(item);
	}

	public abstract void deleteItems();

	protected final void deleteItems(String whereClause, String[] whereArgs) {
		db.delete(TABLE_NAME, whereClause, whereArgs);
	}
}
