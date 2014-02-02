package com.nmotion.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityEvent;

import com.nmotion.R;

public class SlidingMenu extends ViewGroup {

	private static final int TAP_THRESHOLD = 6;
	private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
	private static final float MAXIMUM_MINOR_VELOCITY = 100.0f;
	private static final float MAXIMUM_MAJOR_VELOCITY = 100.0f;
	private static final float MAXIMUM_ACCELERATION = 600.0f;
	private static final int VELOCITY_UNITS = 100;
	private static final int MSG_ANIMATE = 1000;
	private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

	private static final int MENU_CLOSED = -10001;
	private static final int MENU_OPEN = -10002;

	private View mMenu;
	private View mContent;

	private Rect mFrame = new Rect();
	private final Rect mInvalidate = new Rect();
	private boolean mTracking;
	private boolean mLocked;

	private VelocityTracker mVelocityTracker;

	private boolean mContentActive;
	private int mRightOffset;
	private int mLeftOffset;

	private OnMenuOpenListener mOnMenuOpenListener;
	private OnMenuCloseListener mOnMenuCloseListener;
	private OnMenuScrollListener mOnMenuScrollListener;

	private final Handler mSlidingHandler = new SlidingHandler();
	private float mAnimatedAcceleration;
	private float mAnimatedVelocity;
	private float mAnimationPosition;
	private long mAnimationLastTime;
	private long mCurrentAnimationTime;
	private int mTouchDelta;
	private boolean mAnimating;
	private boolean mAllowSingleTap;
	private boolean mAnimateOnClick;

	private final int mTapThreshold;
	private final int mMaximumTapVelocity;
	private final int mMaximumMinorVelocity;
	private final int mMaximumMajorVelocity;
	private final int mMaximumAcceleration;
	private final int mVelocityUnits;

	private int mHandleZoneWidth;
	private Rect mHandle = new Rect();

	private ViewStub menuPage;
	private ViewStub contentPage;
	private int mPaddingRight;

	/**
	 * Callback invoked when the menu is opened.
	 */
	public static interface OnMenuOpenListener {

		/**
		 * Invoked when the menu becomes fully open.
		 */
		public void onMenuOpened();
	}

	/**
	 * Callback invoked when the menu is closed.
	 */
	public static interface OnMenuCloseListener {

		/**
		 * Invoked when the menu becomes fully closed.
		 */
		public void onMenuClosed();
	}

	/**
	 * Callback invoked when the menu is scrolled.
	 */
	public static interface OnMenuScrollListener {

		/**
		 * Invoked when the user starts dragging/flinging the menu's handle.
		 */
		public void onScrollStarted();

		/**
		 * Invoked when the user stops dragging/flinging the menu's handle.
		 */
		public void onScrollEnded();
	}

	/**
	 * Creates a new SlidingMenu from a specified set of attributes defined in XML.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param attrs
	 *            The attributes defined in XML.
	 */
	public SlidingMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Creates a new SlidingMenu from a specified set of attributes defined in XML.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param attrs
	 *            The attributes defined in XML.
	 * @param defStyle
	 *            The style to apply to this widget.
	 */
	public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		readAttrs(attrs);

		// mHandleZoneWidth = 150;

		mRightOffset = mPaddingRight;
		mLeftOffset = 0;
		mAllowSingleTap = true;
		mAnimateOnClick = true;

		mContentActive = true;

		contentPage = new ViewStub(getContext());
		menuPage = new ViewStub(getContext());
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(contentPage, layoutParams);
		addView(menuPage, layoutParams);

		mMenu = menuPage;
		mContent = contentPage;

		final float density = getResources().getDisplayMetrics().density;
		mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
		mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
		mMaximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
		mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
		mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);
		mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

		setAlwaysDrawnWithCacheEnabled(false);
	}

	private void readAttrs(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SlidingMenu);

		mHandleZoneWidth = a.getDimensionPixelOffset(R.styleable.SlidingMenu_handleSize, 0);

		mPaddingRight = a.getDimensionPixelOffset(R.styleable.SlidingMenu_paddingRight, 150);
		a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException("SlidingMenu cannot have UNSPECIFIED dimensions");
		}

		final View menu = mMenu;
		final View content = mContent;

		int menuWidth = widthSpecSize - mRightOffset;
		menu.measure(MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.AT_MOST), heightMeasureSpec);
		if (menu.getMeasuredWidth() > 0)
			menu.measure(MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
		int contentWidth = widthSpecSize;
		content.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), heightMeasureSpec);

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		final long drawingTime = getDrawingTime();

		if (mTracking || mAnimating) {
			// int offsetMenu = getMenuOffset(mContent.getLeft());
			// Bitmap cache = Bitmap.createBitmap(mMenu.getDrawingCache(), 0, 0, mContent.getLeft(), mMenu.getMeasuredHeight());
			final Bitmap cache = mMenu.getDrawingCache();

			if (cache != null) {
				canvas.save();
				canvas.clipRect(0, 0, mContent.getLeft(), mContent.getMeasuredHeight());
				// canvas.drawBitmap(cache, mContent.getLeft() - mMenu.getMeasuredWidth() + offsetMenu, 0, null);
				canvas.drawBitmap(cache, 0, 0, null);
				canvas.restore();

				onDrawMenuOverlay(canvas, (mMenu.getMeasuredWidth() - mContent.getLeft()) / (float) mMenu.getMeasuredWidth());
			} else {
				canvas.save();
				canvas.translate(mContent.getLeft(), 0);
				drawChild(canvas, mMenu, drawingTime);
				canvas.restore();
			}
		} else {
			drawChild(canvas, mMenu, drawingTime);
		}
		drawChild(canvas, mContent, drawingTime);
	}

	private static final int MAXIMUM_MENU_ALPHA_OVERLAY = 170;

	private void onDrawMenuOverlay(Canvas canvas, float opennessRatio) {
		final Paint menuOverlayPaint = mMenuOverlayPaint;
		final int alpha = (int) (MAXIMUM_MENU_ALPHA_OVERLAY * opennessRatio);
		if (alpha > 0) {
			menuOverlayPaint.setColor(Color.argb(alpha, 0, 0, 0));
			canvas.drawRect(0, 0, mContent.getLeft(), getHeight(), mMenuOverlayPaint);
		}
	}

	private static final float PARALLAX_SPEED_RATIO = 0.25f;
	private Paint mMenuOverlayPaint = new Paint();

	@SuppressWarnings("unused")
	private int getMenuOffset(int contentLeft) {
		final int menuWidth = mMenu.getMeasuredWidth();
		int offset = 0;
		if (menuWidth != 0) {
			final float opennessRatio = ((menuWidth - contentLeft) / (float) menuWidth);
			offset = (int) (opennessRatio * menuWidth * PARALLAX_SPEED_RATIO);
		}
		return offset;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mTracking) {
			return;
		}

		final int width = r - l;
		final int height = b - t;

		final View menu = mMenu;

		int menuWidth = menu.getMeasuredWidth();
		int menuLeft;
		if (menuWidth > 0) {
			menuLeft = mContentActive ? mLeftOffset - menuWidth : width - mRightOffset - menuWidth;
		} else {
			menuLeft = mLeftOffset;
		}
		int mnLeft = menuLeft;
		int mnRight = mnLeft + menuWidth;

		menu.layout(mnLeft, 0, mnRight, height);

		final View content = mContent;

		int cLeft = menuLeft + menuWidth;
		int cRight = cLeft + content.getMeasuredWidth();

		content.layout(cLeft, 0, cRight, height);

		updateHandleRect();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mLocked) {
			return false;
		}

		final int action = event.getAction();

		float x = event.getX();
		float y = event.getY();

		final Rect frame = mHandle;
		if (!mTracking && !frame.contains((int) x, (int) y)) {
			return false;
		}

		if (action == MotionEvent.ACTION_DOWN) {
			mTracking = true;

			// Must be called before prepareTracking()
			prepareMenu();

			// Must be called after prepareContent()
			if (mOnMenuScrollListener != null) {
				mOnMenuScrollListener.onScrollStarted();
			}

			final int left = mContent.getLeft();
			mTouchDelta = (int) x - left;

			prepareTracking();

			mVelocityTracker.addMovement(event);
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mLocked) {
			return true;
		}
		if (mTracking) {
			mVelocityTracker.addMovement(event);
			final int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				moveContent((int) (event.getX()) - mTouchDelta);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(mVelocityUnits);

				float yVelocity = velocityTracker.getYVelocity();
				float xVelocity = velocityTracker.getXVelocity();
				boolean negative;

				negative = xVelocity < 0;
				if (yVelocity < 0) {
					yVelocity = -yVelocity;
				}
				if (yVelocity > mMaximumMinorVelocity) {
					yVelocity = mMaximumMinorVelocity;
				}

				float velocity = (float) Math.hypot(xVelocity, yVelocity);
				if (negative) {
					velocity = -velocity;
				}

				final int left = mContent.getLeft();

				if (Math.abs(velocity) < mMaximumTapVelocity) {
					if ((mContentActive && left > mTapThreshold + mLeftOffset) || (!mContentActive && left > mRightOffset + getRight() - getLeft() - mMenu.getMeasuredWidth() - mTapThreshold)) {

						if (mAllowSingleTap) {

							if (mContentActive) {
								animateOpen(left);
							} else {
								animateClose(left);
							}
						} else {
							performFling(left, velocity, false);
						}

					} else {
						performFling(left, velocity, false);
					}
				} else {
					performFling(left, velocity, false);
				}
			}
			default:
			    break;
			}
		}

		return mTracking || mAnimating || super.onTouchEvent(event);
	}

	public final void injectContentById(int layoutId) throws NullPointerException {
		contentPage.setLayoutResource(layoutId);
		mContent = contentPage.inflate();
		contentPage = null;
	}

	public final void injectMenuById(int layoutMenuId) throws NullPointerException {
		menuPage.setLayoutResource(layoutMenuId);
		mMenu = menuPage.inflate();
		menuPage = null;
	}

	public final void injectMenuView(View menuView) throws NullPointerException {
		menuPage = null;
		mMenu = menuView;
	}

	private void updateHandleRect() {
		int offset = mHandleZoneWidth > 0 ? mHandleZoneWidth : 0;
		if (!isMenuClosed()) {
			offset = mHandleZoneWidth > 0 ? mHandleZoneWidth : mPaddingRight;
		}
		if (mMenu.getMeasuredWidth() > 0) {
			mHandle.set(mMenu.getRight(), 0, mMenu.getRight() + offset, getHeight());
		}
	}

	private void animateOpen(int position) {
		prepareTracking();
		performFling(position, mMaximumAcceleration, true);
	}

	private void animateClose(int position) {
		prepareTracking();
		performFling(position, -mMaximumAcceleration, true);
	}

	private void performFling(int position, float velocity, boolean always) {
		mAnimationPosition = position;
		mAnimatedVelocity = velocity;

		if (mContentActive) {
			if (always || (velocity > mMaximumMajorVelocity || (position > mLeftOffset + mMenu.getMeasuredWidth() && velocity > -mMaximumMajorVelocity))) {
				// We are expanded, but they didn't move sufficiently to cause
				// us to retract. Animate back to the expanded position.
				mAnimatedAcceleration = mMaximumAcceleration;
				if (velocity < 0) {
					mAnimatedVelocity = 0;
				}
			} else {
				// We are expanded and are now going to animate away.
				mAnimatedAcceleration = -mMaximumAcceleration;
				if (velocity > 0) {
					mAnimatedVelocity = 0;
				}
			}
		} else {
			if (!always && (velocity > mMaximumMajorVelocity || (position > getWidth() / 2 && velocity > -mMaximumMajorVelocity))) {
				// We are collapsed, and they moved enough to allow us to expand.
				mAnimatedAcceleration = mMaximumAcceleration;
				if (velocity < 0) {
					mAnimatedVelocity = 0;
				}
			} else {
				// We are collapsed, but they didn't move sufficiently to cause
				// us to retract. Animate back to the collapsed position.
				mAnimatedAcceleration = -mMaximumAcceleration;
				if (velocity > 0) {
					mAnimatedVelocity = 0;
				}
			}
		}

		long now = SystemClock.uptimeMillis();
		mAnimationLastTime = now;
		mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
		mAnimating = true;
		mSlidingHandler.removeMessages(MSG_ANIMATE);
		mSlidingHandler.sendMessageAtTime(mSlidingHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
		stopTracking();
	}

	private void prepareTracking() {
		mTracking = true;
		mVelocityTracker = VelocityTracker.obtain();
		boolean openingMenu = !mContentActive;
		if (openingMenu) {
			mAnimatedAcceleration = 1;// mMaximumAcceleration/4;
			mAnimatedVelocity = 250;// mMaximumMajorVelocity/4;
			mAnimationPosition = mContent.getLeft();
			moveContent((int) mAnimationPosition);
			mAnimating = true;
			mSlidingHandler.removeMessages(MSG_ANIMATE);
			long now = SystemClock.uptimeMillis();
			mAnimationLastTime = now;
			mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
			mAnimating = true;
		} else {
			if (mAnimating) {
				mAnimating = false;
				mSlidingHandler.removeMessages(MSG_ANIMATE);
			}
		}
	}

	private void moveContent(int position) {
		final View content = mContent;
		if (position == MENU_OPEN) {
			int offsetValue = getWidth() - mRightOffset;
			content.offsetLeftAndRight(offsetValue);

			requestLayout();
			invalidate();
			updateHandleRect();
		} else if (position == MENU_CLOSED) {
			int offsetValue = mLeftOffset - content.getMeasuredWidth();
			content.offsetLeftAndRight(offsetValue);

			requestLayout();
			invalidate();
			updateHandleRect();
		} else if (position > mLeftOffset && position < getWidth() - mRightOffset) {
			final int left = content.getLeft();

			int deltaX = position - left;
			content.offsetLeftAndRight(deltaX);

			final Rect frame = mFrame;
			final Rect region = mInvalidate;

			mContent.getHitRect(frame);
			region.set(frame);

			// region.union(frame.right - deltaX, frame.top, frame.left - deltaX, frame.bottom);
			region.union(frame.left - deltaX - mMenu.getWidth(), 0, frame.left - deltaX, getHeight());

			invalidate(region);
		}
	}

	private void prepareMenu() {
		if (mAnimating) {
			return;
		}

		// Something changed in the content, we need to honor the layout request
		// before creating the cached bitmap
		final View menu = mMenu;
		// Try only once... we should really loop but it's not a big deal
		// if the draw was cancelled, it will only be temporary anyway
		menu.getViewTreeObserver().dispatchOnPreDraw();
		menu.buildDrawingCache();
	}

	private void stopTracking() {
		mTracking = false;
		if (mOnMenuScrollListener != null) {
			mOnMenuScrollListener.onScrollEnded();
		}

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void doAnimation() {
		if (mAnimating) {
			incrementAnimation();

			if (mAnimationPosition >= getWidth() - mRightOffset) {
				mAnimating = false;
				openMenu();
			} else if (mAnimationPosition < mLeftOffset) {
				mAnimating = false;
				closeMenu();
			} else {
				moveContent((int) mAnimationPosition);
				mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
				mSlidingHandler.sendMessageAtTime(mSlidingHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
			}
		}
	}

	private void incrementAnimation() {
		long now = SystemClock.uptimeMillis();
		float t = (now - mAnimationLastTime) / 1000.0f; // ms -> s
		final float position = mAnimationPosition;
		final float v = mAnimatedVelocity; // px/s
		final float a = mAnimatedAcceleration; // px/s/s
		mAnimationPosition = position + (v * t) + (0.5f * a * t * t); // px
		mAnimatedVelocity = v + (a * t); // px/s
		mAnimationLastTime = now; // ms
	}

	/**
	 * Toggles the menu open and close. Takes effect immediately.
	 * 
	 * @see #open()
	 * @see #close()
	 * @see #animateMenuOpen()
	 * @see #animateMenuClose()
	 * @see #animateToggle()
	 */
	public void toggle() {
		if (!mContentActive) {
			closeMenu();
		} else {
			openMenu();
		}
		invalidate();
		requestLayout();
	}

	/**
	 * Toggles the menu open and close with an animation.
	 * 
	 * @see #open()
	 * @see #close()
	 * @see #animateMenuOpen()
	 * @see #animateMenuClose()
	 * @see #toggle()
	 */
	public void animateToggle() {
		if (mContentActive) {
			animateMenuOpen();
		} else {
			animateMenuClose();
		}
	}

	/**
	 * Opens the menu immediately.
	 * 
	 * @see #toggle()
	 * @see #close()
	 * @see #animateMenuClose()
	 */
	public void open() {
		openMenu();
		invalidate();
		requestLayout();

		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	/**
	 * Closes the menu immediately.
	 * 
	 * @see #toggle()
	 * @see #open()
	 * @see #animateMenuOpen()
	 */
	public void close() {
		closeMenu();
		invalidate();
		requestLayout();
	}

	/**
	 * Open the menu with an animation.
	 * 
	 * @see #close()
	 * @see #open()
	 * @see #animateMenuClose()
	 * @see #animateToggle()
	 * @see #toggle()
	 */
	public void animateMenuOpen() {
		prepareMenu();
		final OnMenuScrollListener scrollListener = mOnMenuScrollListener;
		if (scrollListener != null) {
			scrollListener.onScrollStarted();
		}
		animateOpen(mContent.getLeft());

		if (scrollListener != null) {
			scrollListener.onScrollEnded();
		}
	}

	/**
	 * Close the menu with an animation.
	 * 
	 * @see #close()
	 * @see #open()
	 * @see #animateMenuOpen()
	 * @see #animateToggle()
	 * @see #toggle()
	 */
	public void animateMenuClose() {
		prepareMenu();
		final OnMenuScrollListener scrollListener = mOnMenuScrollListener;
		if (scrollListener != null) {
			scrollListener.onScrollStarted();
		}
		animateClose(mContent.getLeft());

		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

		if (scrollListener != null) {
			scrollListener.onScrollEnded();
		}
	}

	private void closeMenu() {
		moveContent(MENU_CLOSED);

		if (mContentActive) {
			return;
		}

		mContentActive = true;
		if (mOnMenuCloseListener != null) {
			mOnMenuCloseListener.onMenuClosed();
		}
	}

	private void openMenu() {
		moveContent(MENU_OPEN);

		if (!mContentActive) {
			return;
		}

		mContentActive = false;

		if (mOnMenuOpenListener != null) {
			mOnMenuOpenListener.onMenuOpened();
		}
	}

	/**
	 * Sets the listener that receives a notification when the menu becomes open.
	 * 
	 * @param onMenuOpenListener
	 *            The listener to be notified when the menu is opened.
	 */
	public void setOnMenuOpenListener(OnMenuOpenListener onMenuOpenListener) {
		mOnMenuOpenListener = onMenuOpenListener;
	}

	/**
	 * Sets the listener that receives a notification when the menu becomes close.
	 * 
	 * @param onMenuCloseListener
	 *            The listener to be notified when the menu is closed.
	 */
	public void setOnMenuCloseListener(OnMenuCloseListener onMenuCloseListener) {
		mOnMenuCloseListener = onMenuCloseListener;
	}

	/**
	 * Sets the listener that receives a notification when the menu starts or ends a scroll. A fling is considered as a scroll. A fling will also trigger a menu opened or menu closed event.
	 * 
	 * @param onMenuScrollListener
	 *            The listener to be notified when scrolling starts or stops.
	 */
	public void setOnMenuScrollListener(OnMenuScrollListener onMenuScrollListener) {
		mOnMenuScrollListener = onMenuScrollListener;
	}

	/**
	 * Returns the handle of the menu.
	 * 
	 * @return The View reprenseting the handle of the menu, identified by the "handle" id in XML.
	 */
	public View getMenu() {
		return mMenu;
	}

	/**
	 * Returns the content of the menu.
	 * 
	 * @return The View reprenseting the content of the menu, identified by the "content" id in XML.
	 */
	public View getContent() {
		return mContent;
	}

	/**
	 * Unlocks the SlidingMenu so that touch events are processed.
	 * 
	 * @see #lock()
	 */
	public void unlock() {
		mLocked = false;
	}

	/**
	 * Locks the SlidingMenu so that touch events are ignores.
	 * 
	 * @see #unlock()
	 */
	public void lock() {
		mLocked = true;
	}

	/**
	 * Indicates whether the menu is currently fully opened.
	 * 
	 * @return True if the menu is opened, false otherwise.
	 */
	public boolean isMenuClosed() {
		return mContentActive;
	}

	/**
	 * Indicates whether the menu is scrolling or flinging.
	 * 
	 * @return True if the menu is scroller or flinging, false otherwise.
	 */
	public boolean isMoving() {
		return mTracking || mAnimating;
	}

	@SuppressWarnings("unused")
	private class MenuToggler implements OnClickListener {

		public void onClick(View v) {
			if (mLocked) {
				return;
			}
			// mAllowSingleTap isn't relevant here; you're *always*
			// allowed to open/close the menu by clicking with the
			// trackball.

			if (mAnimateOnClick) {
				animateToggle();
			} else {
				toggle();
			}
		}
	}

	private class SlidingHandler extends Handler {

		public void handleMessage(Message m) {
			switch (m.what) {
			case MSG_ANIMATE:
				doAnimation();
				break;
			default: 
			    break;
			}
		}
	}
}
