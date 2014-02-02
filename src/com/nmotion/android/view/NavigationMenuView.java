package com.nmotion.android.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.AboutScreen;
import com.nmotion.android.App;
import com.nmotion.android.FAQListScreen;
import com.nmotion.android.LogInScreen;
import com.nmotion.android.MyAccountScreen;
import com.nmotion.android.OrderCartScreen;
import com.nmotion.android.RestaurantsListScreen;
import com.nmotion.android.SettingsScreen;
import com.nmotion.android.SplashScreen;
import com.nmotion.android.utils.AppUtils;

public class NavigationMenuView extends LinearLayout implements OnItemClickListener {

	private ListView navigationMenuList;
	private MenuAdapter menuAdapter;
	private ArrayList<NavigationMenuItem> mMenu;

	public NavigationMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NavigationMenuView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		inflate(getContext(), R.layout.layout_navigation_menu_view, this);
		navigationMenuList = (ListView) findViewById(R.id.navigation_menu_list);
		generateItems();
		menuAdapter = new MenuAdapter();
		navigationMenuList.setOnItemClickListener(this);
		navigationMenuList.setAdapter(menuAdapter);
	}

	/**
	 * calling every time when menu list created or {@link #update() update()} first create list with all items in NavigationMenuItem enum. Then check which item should be delete .
	 * 
	 * @return ArrayList with menu items
	 */
	private ArrayList<NavigationMenuItem> generateItems() {
		mMenu = new ArrayList<NavigationMenuItem>();
		for (NavigationMenuItem item : NavigationMenuItem.values()) {
			mMenu.add(item);
		}
		if (!isInEditMode()) {
			if (App.getInstance().getNetworkService().isLoggedIn()) {
				mMenu.remove(NavigationMenuItem.SIGN_IN);
			} else {
				mMenu.remove(NavigationMenuItem.SIGN_OUT);
			}

			/*if (App.getInstance().getPreferencesManager().getBoolean(PreferencesManager.USER_VIA_FACEBOOK, false)) {
				mMenu.remove(NavigationMenuItem.MY_ACCOUNT);
			}*/
		}
		return mMenu;
	}

	// use it for turning off eclipse formatting (see Window->Preferences->Formatter)
	//@formatter:off
	
	/**
	 * Represent navigation menu structure. If need empty line like SEPARATOR, add any object with titleStringId = -1.
	 * Here we can implement additional items logic in future.
	 */
	enum NavigationMenuItem {

		SIGN_IN(R.string.txt_sign_in), 
		SIGN_OUT(R.string.txt_sign_out),
		//SEPARATOR(-1),
		RESTAURANTS(R.string.txt_restaurant), 
		ORDER_CART(R.string.txt_order_cart), 
		MY_ACCOUNT(R.string.txt_my_account), 
		SETTINGS(R.string.txt_setting), 
		ABOUT(R.string.txt_about),
		FAQ(R.string.txt_faq);

		private int titleStringId;

		NavigationMenuItem(int titleStringId) {
			this.titleStringId = titleStringId;
		}

		public int getTitleStringId() {
			return titleStringId;
		}

	}
	//@formatter:on
	// use it for turning on eclipse formatting

	/**
	 * Adapter provide views for menu, if need not default view - check item type and inflate the necessary layout
	 */
	class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mMenu.size();
		}

		@Override
		public NavigationMenuItem getItem(int position) {
			return mMenu.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private View getItemView(NavigationMenuItem item) {
			View view = inflate(getContext(), R.layout.navigation_menu_item, null);
			TextView titleView = (TextView) view.findViewById(R.id.navigation_menu_title);
			ImageView icon = (ImageView) view.findViewById(R.id.navigation_menu_icon);
			int titleId = item.getTitleStringId();
			titleView.setText(titleId);
			switch (item) {
                            case SIGN_OUT:
                                icon.setImageResource(R.drawable.icons_black_5);
                                break;
                            case SIGN_IN:
                                icon.setImageResource(R.drawable.icons_black_5);
                                break;
                           case RESTAURANTS:
                                icon.setImageResource(R.drawable.icons_black_6);
                                break;
                           case MY_ACCOUNT:
                               icon.setImageResource(R.drawable.icons_black_9);
                               break;
                           case SETTINGS:
                               icon.setImageResource(R.drawable.icons_black_10);
                               break;
                           case ABOUT:
                               icon.setImageResource(R.drawable.icons_black_11);
                               break;
                           case FAQ:
                               icon.setImageResource(R.drawable.icons_black_12);
                               break;

                            default:
                                break;
                        }
			
			return view;
		}

		private View getSeparatorView() {
			View view = inflate(getContext(), R.layout.navigation_menu_item, null);
			return view;
		}

		private View getOrderCartView(NavigationMenuItem item) {
			View view = getItemView(item);
			int count = isInEditMode() ? 10 : App.getInstance().getCache().getOrders().size();
			TextView countTextView = (TextView) view.findViewById(R.id.ordered_meals_count);
			ImageView icon = (ImageView) view.findViewById(R.id.navigation_menu_icon);
			 icon.setImageResource(R.drawable.icons_black_7);
			if (count > 0) {
				countTextView.setVisibility(View.VISIBLE);
				countTextView.setText(String.valueOf(count));
			}
			return view;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NavigationMenuItem item = getItem(position);
			View view = null;
			switch (item) {
			case ORDER_CART:
				view = getOrderCartView(item);
				break;
			default:
				if (item.titleStringId != -1) {
					view = getItemView(item);
				} else {
					view = getSeparatorView();
				}
			}
			return view;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Intent intent = null;
		switch (menuAdapter.getItem(position)) {
		case SIGN_IN:
			onSignInClick();
			break;
		case SIGN_OUT:
			onSignOutClick();
			break;
		case RESTAURANTS:
			intent = new Intent(getContext(), RestaurantsListScreen.class);
			break;
		case ORDER_CART:
			intent = new Intent(getContext(), OrderCartScreen.class);
			break;
		case MY_ACCOUNT:
			intent = new Intent(getContext(), MyAccountScreen.class);
			break;
		case SETTINGS:
			intent = new Intent(getContext(), SettingsScreen.class);
			break;
		case ABOUT:
			intent = new Intent(getContext(), AboutScreen.class);
			break;
		case FAQ:
			intent = new Intent(getContext(), FAQListScreen.class);
			break;
		default:
                    break;
		}
		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			getContext().startActivity(intent);
		}
	}

	public void onSignInClick() {
		//if (App.getInstance().getCache().getOrders().size() == 0) {
			singIn();
		/*} else {
			AppUtils.showContinueCancelDialog(getContext(), R.string.txt_your_basket_will_be_cleaned, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					singIn();
					dialog.dismiss();
				}
			});
		}*/

	}

	private void singIn() {
		Intent intent = new Intent(getContext(), LogInScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		getContext().startActivity(intent);
	}

	public void onSignOutClick() {
		if (App.getInstance().getCache().getOrders().size() == 0) {
			signOut();
		} else {
			AppUtils.showContinueCancelDialog(getContext(), R.string.txt_your_basket_will_be_cleaned, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					signOut();
					dialog.dismiss();
				}
			});
		}
	}

	private void signOut() {
		App.getInstance().getPreferencesManager().clear();
		App.getInstance().getNetworkService().logOut();
		App.getInstance().getCache().clear();

		Intent intent = new Intent(getContext(), SplashScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		getContext().startActivity(intent);
	}

	/**
	 * Update navigation menu view. (example: login,logout, change order cart item count)
	 */
	public void update() {
		generateItems();
		menuAdapter.notifyDataSetChanged();
	}
}
