package com.nmotion.android.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.nmotion.android.database.SingleObjectsDAO.OrderDetailsDBSchema.SingleObjectsColumns;
import com.nmotion.android.models.OrderDetails;
import com.nmotion.android.models.User;

public class SingleObjectsDAO extends BasicDAO<Object> {

	public interface OrderDetailsDBSchema {

		public enum SingleObjectsColumns {
			_ID("_id", DBType.INT), OBJECT_NAME("name", DBType.TEXT), SERIALIZED_OBJECT("serialized_object", DBType.BLOB);

			private String columnName;
			private DBType type;

			SingleObjectsColumns(String columnName, DBType type) {
				this.columnName = columnName;
				this.type = type;
			}

			public String getName() {
				return columnName;
			}

			public DBType getType() {
				return type;
			}
		}
	}

	public SingleObjectsDAO(SQLiteDatabase db) {
		super(db, Tables.SINGLE_OBJECTS_TABLE);
	}

	@Override
	protected ContentValues createValues(Object item) {
		ContentValues values = new ContentValues();
		values.put(SingleObjectsColumns.OBJECT_NAME.getName(), item.getClass().getSimpleName());
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
			objectOutputStream.writeObject(item);
		} catch (IOException e) {
			e.printStackTrace();
		}
		values.put(SingleObjectsColumns.OBJECT_NAME.getName(), item.getClass().getName());
		values.put(SingleObjectsColumns.SERIALIZED_OBJECT.getName(), arrayOutputStream.toByteArray());
		return values;
	}

	@Override
	protected Object parseValues(ContentValues values) {

		// item.orderId =
		// values.getAsInteger(SingleObjectsColumns.OBJECT_NAME.getName());
		byte[] objectArray = values.getAsByteArray(SingleObjectsColumns.SERIALIZED_OBJECT.getName());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(objectArray);
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			return objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<Object> readItems() {
		return readItems(null, null, null, null, null);
	}

	@Override
	public void deleteItem(Object item) {
		String selection = SingleObjectsColumns.OBJECT_NAME.getName() + "=?";
		String[] selectionArgs = { String.valueOf(item.getClass().getName()) };
		deleteItems(selection, selectionArgs);
	}

	@Override
	public void deleteItems() {
		deleteItems(null, null);
	}

	public void updateItem(Object item) {
		String selection = SingleObjectsColumns.OBJECT_NAME.getName() + "=?";
		String[] selectionArgs = { item.getClass().getName() };
		updateItem(createValues(item), selection, selectionArgs);

	}

	public OrderDetails readOrderDetails() {
		String selection = SingleObjectsColumns.OBJECT_NAME.getName() + "=?";
		String[] selectionArgs = { String.valueOf(OrderDetails.class.getName()) };
		ArrayList<Object> list = (ArrayList<Object>) readItems(selection, selectionArgs, null, null, null);
		if (list.size() > 0) {
			return (OrderDetails) list.get(0);
		}
		return null;
	}

	public User readCurrentUser() {
		String selection = SingleObjectsColumns.OBJECT_NAME.getName() + "=?";
		String[] selectionArgs = { String.valueOf(User.class.getName()) };
		ArrayList<Object> list = (ArrayList<Object>) readItems(selection, selectionArgs, null, null, null);
		if (list.size() > 0) {
			return (User) list.get(0);
		}
		return null;
	}

}