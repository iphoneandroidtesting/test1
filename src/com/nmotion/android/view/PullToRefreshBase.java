package com.nmotion.android.view;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.nmotion.R;

public abstract class PullToRefreshBase<T extends AdapterView<ListAdapter>> extends LinearLayout implements OnTouchListener {

	private final class SmoothScrollRunnable implements Runnable {

		static final int ANIMATION_DURATION_MS = 190;
		static final int ANIMATION_FPS = 1000 / 60;

		private final Interpolator interpolator;
		private final int scrollToY;
		private final int scrollFromY;
		private final Handler handler;

		private boolean continueRunning = true;
		private long startTime = -1;
		private int currentY = -1;

		public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
			this.handler = handler;
			this.scrollFromY = fromY;
			this.scrollToY = toY;
			this.interpolator = new AccelerateDecelerateInterpolator();
		}

		@Override
		public void run() {

			/**
			 * Only set startTime if this is the first time we're starting, else
			 * actually calculate the Y delta
			 */
			if (startTime == -1) {
				startTime = System.currentTimeMillis();
			} else {

				/**
				 * We do do all calculations in long to reduce software float
				 * calculations. We use 1000 as it gives us good accuracy and
				 * small rounding errors
				 */
				long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION_MS;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math.round((scrollFromY - scrollToY) * interpolator.getInterpolation(normalizedTime / 1000f));
				this.currentY = scrollFromY - deltaY;
				setHeaderScroll(currentY);
			}

			// If we're not at the target Y, keep going...
			if (continueRunning && scrollToY != currentY) {
				handler.postDelayed(this, ANIMATION_FPS);
			}
		}

		public void stop() {
			this.continueRunning = false;
			this.handler.removeCallbacks(this);
		}
	};

	static final int PULL_TO_REFRESH = 0;
	static final int RELEASE_TO_REFRESH = PULL_TO_REFRESH + 1;
	static final int REFRESHING = RELEASE_TO_REFRESH + 1;
	static final int EVENT_COUNT = 3;

	private int state = PULL_TO_REFRESH;
	private T adapterView;

	private EditText searchText;
	private int headerHeight;

	private final Handler handler = new Handler();

	private OnTouchListener onTouchListener;
	//private OnRefreshListener onRefreshListener;

	private SmoothScrollRunnable currentSmoothScrollRunnable;

	private float startY = -1;
	private final float[] lastYs = new float[EVENT_COUNT];

	public PullToRefreshBase(Context context) {
		this(context, null);
	}

	public PullToRefreshBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public final T getAdapterView() {
		return adapterView;
	}

	public final void setAdapterView(ListAdapter listAdapter) {
		adapterView.setAdapter(listAdapter);
	}

	@Override
	public void setOnTouchListener(OnTouchListener listener) {
		onTouchListener = listener;
	}

	public String getText() {
		return searchText.getText().toString();
	}

	public EditText getEditText() {
		return searchText;
	}

	@Override
	public boolean onTouch(View view, MotionEvent ev) {
		// if (state == REFRESHING) {
		// return false;
		// } else {
		return onAdapterViewTouch(view, ev);
		// }
	}

	protected abstract T createAdapterView(Context context, AttributeSet attrs);

	protected final void resetHeader() {
		state = PULL_TO_REFRESH;
		initializeYsHistory();
		startY = -1;
		smoothScrollTo(0);
	}

	private void init(Context context, AttributeSet attrs) {
		setOrientation(LinearLayout.VERTICAL);

		searchText = (EditText) LayoutInflater.from(context).inflate(R.layout.layout_restaurant_search_field, this, false);

		addView(searchText, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		measureView(searchText);
		headerHeight = searchText.getMeasuredHeight();

		adapterView = this.createAdapterView(context, attrs);
		adapterView.setOnTouchListener(this);
		addView(adapterView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);

		setPadding(getPaddingLeft(), -headerHeight, getPaddingRight(), getPaddingBottom());
	}

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	private boolean onAdapterViewTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			updateEventStates(event);

			if (isPullingDownToRefresh() && startY == -1) {
				if (startY == -1) {
					startY = event.getY();
				}
				return false;
			}

			if (startY != -1 && !adapterView.isPressed()) {
				pullDown(event, startY);
				return true;
			}

			break;
		case MotionEvent.ACTION_UP:
			initializeYsHistory();
			startY = -1;

			if (state == RELEASE_TO_REFRESH) {
				setRefreshing();
//				if (onRefreshListener != null) {
//					onRefreshListener.onRefresh();
//				}
			} else {
				resetHeader();
				// smoothScrollTo(0);
			}
			break;
		default:
		    break;
		}

		if (null != onTouchListener) {
			return onTouchListener.onTouch(view, event);
		}
		return false;
	}

	private void pullDown(MotionEvent event, float firstY) {
	    System.out.println("pull down method");
		float averageY = average(lastYs);

		int height = (int) (Math.max(averageY - firstY, 0));
		setHeaderScroll(height);

		if (state == PULL_TO_REFRESH && headerHeight < height) {
			state = RELEASE_TO_REFRESH;
		}
		if (state == RELEASE_TO_REFRESH && headerHeight >= height) {
			state = PULL_TO_REFRESH;
		}
	}

	private void setHeaderScroll(int y) {
		scrollTo(0, -y);
	}

	private int getHeaderScroll() {
		return -getScrollY();
	}

	private void setRefreshing() {
		state = REFRESHING;
		smoothScrollTo(headerHeight);
	}

	private float average(float[] ysArray) {
		float avg = 0;
		for (int i = 0; i < EVENT_COUNT; i++) {
			avg += ysArray[i];
		}
		return avg / EVENT_COUNT;
	}

	private void initializeYsHistory() {
		for (int i = 0; i < EVENT_COUNT; i++) {
			lastYs[i] = 0;
		}
	}

	private void updateEventStates(MotionEvent event) {
		for (int i = 0; i < EVENT_COUNT - 1; i++) {
			lastYs[i] = lastYs[i + 1];
		}

		float y = event.getY();
		int top = adapterView.getTop();
		lastYs[EVENT_COUNT - 1] = y + top;
	}	

	private boolean isPullingDownToRefresh() {
		return state != REFRESHING && isUserDraggingDownwards() && isFirstVisible();
	}

	private boolean isFirstVisible() {
		if (this.adapterView.getCount() == 0) {
			return true;
		} else if (adapterView.getFirstVisiblePosition() == 0) {
			return adapterView.getChildAt(0).getTop() >= adapterView.getTop();
		} else {
			return false;
		}
	}

	private boolean isUserDraggingDownwards() {
		return this.isUserDraggingDownwards(0, EVENT_COUNT - 1);
	}

	private boolean isUserDraggingDownwards(int from, int to) {
		return lastYs[from] != 0 && lastYs[to] != 0 && Math.abs(lastYs[from] - lastYs[to]) > 10 && lastYs[from] < lastYs[to];
	}

	private void smoothScrollTo(int y) {
		if (null != currentSmoothScrollRunnable) {
			currentSmoothScrollRunnable.stop();
		}

		this.currentSmoothScrollRunnable = new SmoothScrollRunnable(handler, getHeaderScroll(), y);
		handler.post(currentSmoothScrollRunnable);
	}

	public static interface OnRefreshListener {

		public void onRefresh();

	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		//TODO: remove this. Now need to correct recreate activity after rotate or from recent after process is died
		super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
	}
}
