package com.nmotion.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.nmotion.android.adapters.RestaurantsListScreenAdapter;

public class PullToRefreshListView extends PullToRefreshBase<ListView> {

	public PullToRefreshListView(Context context) {
		super(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected final ListView createAdapterView(Context context, AttributeSet attrs) {
		return new ListView(context, attrs);
	}
	
	public void setAdapter(RestaurantsListScreenAdapter mAdapter) {
		super.setAdapterView(mAdapter);
	}

}
