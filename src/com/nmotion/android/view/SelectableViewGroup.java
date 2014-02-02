package com.nmotion.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SelectableViewGroup extends LinearLayout {

	private boolean isMultipleSelectEnabled;
	private OnItemSelectChangeListener onItemSelectChangeListener;
	private boolean isCheckedEmptyEnable;

	public SelectableViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SelectableViewGroup(Context context) {
		super(context);
	}

	public void addItem(View view) {
		addView(view);
		final int position = getChildCount() - 1;
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean setSelected = !v.isSelected();
				if (!isCheckedEmptyEnable && !setSelected && getCheckedItemCount() == 1) {
					setSelected = !setSelected;
				}
				v.setSelected(setSelected);
				if (!isMultipleSelectEnabled) {
					int childCount = getChildCount();
					for (int i = 0; i < childCount; ++i) {
						View childView = getChildAt(i);
						if (childView != v) {
							childView.setSelected(false);
						}
					}
				}
				if (onItemSelectChangeListener != null) {
					onItemSelectChangeListener.onItemSelected(v, position, setSelected);
				}
			}
		});
	}

	private int getCheckedItemCount() {
		int checkedCount = 0;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View childView = getChildAt(i);
			if (childView.isSelected()) {
				checkedCount++;
			}
		}
		return checkedCount;
	}

	public void performItemClick(int position) {
		getChildAt(position).performClick();
	}

	public void setOnItemSelectChangeListener(OnItemSelectChangeListener onSelectedChangeListener) {
		this.onItemSelectChangeListener = onSelectedChangeListener;
	}

	public void setMultipleSelectEnabled(boolean isMultipleSelectEnabled) {
		this.isMultipleSelectEnabled = isMultipleSelectEnabled;
	}

	public void setCheckedEmptyEnable(boolean isCheckedEmptyEnable) {
		this.isCheckedEmptyEnable = isCheckedEmptyEnable;
	}

	public interface OnItemSelectChangeListener {
		public void onItemSelected(View view, int position, boolean isSelected);
	}
}