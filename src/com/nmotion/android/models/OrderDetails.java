package com.nmotion.android.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.nmotion.android.core.JSONKeys;

public class OrderDetails implements Serializable {

	@Override
	public String toString() {
		return "OrderDetails [orderId=" + orderId + ", orderTotalInCents=" + orderTotalInCents + ", orderTotal=" + orderTotal + ", restaurantId=" + restaurantId + ", tableNumber=" + tableNumber
				+ ", createdAt=" + createdAt + ", productTotal=" + productTotal + ", orderDiscount=" + orderDiscount + ", orderStatus=" + orderStatus + ", tips=" + tips + ", salesTax=" + salesTax
				+ ", consolidatedDiscount=" + consolidatedDiscount + ", consolidatedOrderTotal=" + consolidatedOrderTotal + ", consolidatedOrderTotalInCents=" + consolidatedOrderTotalInCents
				+ ", consolidatedProductTotal=" + consolidatedProductTotal + ", consolidatedSalesTax=" + consolidatedSalesTax + ", consolidatedTips=" + consolidatedTips + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int orderId;
	public long orderTotalInCents;
	public float orderTotal;
	public int restaurantId;
	public int tableNumber;
	public long createdAt;
	public long productTotal;
	public float orderDiscount;
	public int orderStatus;
	public float tips;
	public float salesTax;

	public float consolidatedDiscount;
	public float consolidatedOrderTotal;
	public long consolidatedOrderTotalInCents;
	public float consolidatedProductTotal;
	public float consolidatedSalesTax;
	public float consolidatedTips;

	public OrderDetails() {
	}

	public OrderDetails(JSONObject object) throws JSONException {

		JSONObject data = object.getJSONArray("entries").getJSONObject(0);

		restaurantId = data.getJSONObject(JSONKeys.restaurant).getInt(JSONKeys.id);		
		tableNumber = data.optInt(JSONKeys.tableNumber);
		orderId = data.getInt(JSONKeys.id);
		orderTotalInCents = data.getLong(JSONKeys.orderTotalInCents);
		orderTotal = (float) data.getDouble(JSONKeys.orderTotal);
		productTotal = data.getLong(JSONKeys.productTotal);
		orderDiscount = (float) data.getDouble(JSONKeys.discount);
		salesTax = (float) data.getDouble(JSONKeys.salesTax);
		tips = (float) data.getDouble(JSONKeys.tips);
		createdAt = data.getLong(JSONKeys.createdAt) * 1000;

		consolidatedDiscount = (float) data.optDouble(JSONKeys.consolidatedDiscount, -1);
		consolidatedOrderTotal = (float) data.optDouble(JSONKeys.consolidatedOrderTotal, -1);
		consolidatedOrderTotalInCents = data.optLong(JSONKeys.consolidatedOrderTotalInCents, -1);
		consolidatedProductTotal = (float) data.optDouble(JSONKeys.consolidatedProductTotal, -1);
		consolidatedSalesTax = (float) data.optDouble(JSONKeys.consolidatedSalesTax, -1);
		consolidatedTips = (float) data.optDouble(JSONKeys.consolidatedTips, -1);

		orderStatus = data.optJSONObject(JSONKeys.orderStatus).optInt(JSONKeys.id, -1);
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject orderJSON = new JSONObject();
		orderJSON.put(JSONKeys.id, orderId);
		orderJSON.put(JSONKeys.orderTotal, orderTotal);
		orderJSON.put(JSONKeys.orderTotalInCents, orderTotalInCents);
		orderJSON.put(JSONKeys.tableNumber, tableNumber);
		orderJSON.put(JSONKeys.createdAt, createdAt);
		orderJSON.put(JSONKeys.productTotal, productTotal);
		orderJSON.put(JSONKeys.tips, tips);
		orderJSON.put(JSONKeys.salesTax, salesTax);
		orderJSON.put(JSONKeys.consolidatedDiscount, consolidatedDiscount);
		orderJSON.put(JSONKeys.consolidatedOrderTotal, consolidatedOrderTotal);
		orderJSON.put(JSONKeys.consolidatedOrderTotalInCents, consolidatedOrderTotalInCents);
		orderJSON.put(JSONKeys.consolidatedProductTotal, consolidatedProductTotal);
		orderJSON.put(JSONKeys.consolidatedSalesTax, consolidatedSalesTax);
		orderJSON.put(JSONKeys.consolidatedTips, consolidatedTips);

		return orderJSON;
	}
}
