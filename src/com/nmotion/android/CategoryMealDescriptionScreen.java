package com.nmotion.android;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.image.loader.ImageFetcher;
import com.image.loader.ImageResizer;
import com.nmotion.R;
import com.nmotion.android.models.Meal;
import com.nmotion.android.models.OrderedMeal;
import com.nmotion.android.models.User;
import com.nmotion.android.view.RestaurantCheckInDialog;

public class CategoryMealDescriptionScreen extends BaseRestaurantScreen {

    public static final String DATA_MENU_CATEGORY_ID = "menu_category_id";
    public static final String DATA_MEAL_ID = "meal_id";
    public static final int LOOP_COUNT = 1000;
    //private EditText comment;
    //private SlidingDrawer slidingDrawer;
    private int quantity = 1;
    private TextView screenName;//, mealQuantity, mealName, restaurantName, mealDescription, mealPrice, mealPriceDiscount;
    //private Meal meal;
    //private ImageResizer imageResizer;
    //ImageView mealImage;
    private ViewPager pager;
    private MyPagerAdapter pagerAdapter;
    private ArrayList<Meal> meals;
    int menuCategoryId, selectedMealId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_category_meal_description_pager);
        
        menuCategoryId = getIntent().getExtras().getInt(DATA_MENU_CATEGORY_ID);
        selectedMealId = getIntent().getExtras().getInt(DATA_MEAL_ID);
        meals = App.getInstance().getCache().getMenuCategoryMeals(menuCategoryId);
        
        pager = (ViewPager) findViewById(R.id.mealPager);
        pagerAdapter = new MyPagerAdapter(meals);
        pager.setAdapter(pagerAdapter);
        for (int i=0; i<meals.size(); i++) {
            if (meals.get(i).id==selectedMealId){
                pager.setCurrentItem(i+meals.size()*LOOP_COUNT/2);
                break;
            }
        }
    }
    
    
    private void proceedNext(String comment, Meal meal){
        OrderedMeal orderedMeal = new OrderedMeal(App.getInstance().getCache().getOrders().size(), mRestaurant, meal,
                quantity);
        orderedMeal.comment = comment;
        App.getInstance().getCache().addOrder(orderedMeal);
        if (meal.isExtraAvailible() || meal.isOptionAvailible()) {
            Intent intent = new Intent(CategoryMealDescriptionScreen.this, MealsOptionsScreen.class);
            intent.putExtra(MealsOptionsScreen.DATA_ORDER_ID, App.getInstance().getCache().getOrders().size() - 1);
            startActivity(intent);
        } else {
            startActivity(new Intent(CategoryMealDescriptionScreen.this, OrderCartScreen.class));
        }    
    }
     

    public void onRestaurantClick(View view) {
        Intent intent = new Intent(getApplicationContext(), RestaurantInfoScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private class MyPagerAdapter extends PagerAdapter {
        private ImageResizer imageResizer = new ImageFetcher(getApplicationContext(), 0, 0);;
        private ArrayList<Meal> meals = new ArrayList<Meal>();

        private class ViewHolder {
            private View plusBtn, minusBtn, orderBtn;
            private EditText comment;
            private SlidingDrawer slidingDrawer;
            private TextView mealQuantity, mealName, restaurantName, mealDescription, mealPrice,
                    mealPriceDiscount;
            private ImageView mealImage;
        }

        public MyPagerAdapter(ArrayList<Meal> meals) {
            this.meals.addAll(meals);            
        }

        @Override
        public int getCount() {
            return meals.size()*LOOP_COUNT;
        }
        
        

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            position = position % meals.size();
            LayoutInflater inflater = LayoutInflater.from(CategoryMealDescriptionScreen.this);
            final RelativeLayout page = (RelativeLayout) inflater.inflate(R.layout.layout_category_meal_description, null);
            final ViewHolder holder = new ViewHolder();
            final Meal meal = meals.get(position);
            holder.plusBtn = page.findViewById(R.id.btn_restaurant_meal_quantity_plus);
            holder.minusBtn = page.findViewById(R.id.btn_restaurant_meal_quantity_minus);
            holder.orderBtn = page.findViewById(R.id.btn_restaurant_meal_order);
            
            holder.plusBtn.setOnClickListener(new OnClickListener() {                
                @Override
                public void onClick(View v) {
                    quantity++;
                    holder.mealQuantity.setText(String.valueOf(quantity));
                }
            });
            holder.minusBtn.setOnClickListener(new OnClickListener() {                
                @Override
                public void onClick(View v) {
                    quantity--;
                    if (quantity == 0) {
                        quantity = 1;
                    }
                    holder.mealQuantity.setText(String.valueOf(quantity));      
                }
            });
            
            holder.orderBtn.setOnClickListener(new OnClickListener() {                
                @Override
                public void onClick(View v) {
                    User user = App.getInstance().getNetworkService().getCurrentUser();
                    if (!mRestaurant.isOpen){
                        Toast.makeText(v.getContext(), getString(R.string.restaurent_closed_warning), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (user.id==-1){
                        Intent intent = new Intent(CategoryMealDescriptionScreen.this, LogInScreen.class);
                        intent.putExtra("orderMealMode", true);
                        startActivity(intent);
                        return;
                    }
                    if (App.getInstance().getPreferencesManager().isCheckedIn(user.eMail, mRestaurantId) /*|| mRestaurant.isTakeAway*/){
                        proceedNext(holder.comment.getText().toString(), meal);
                        return;
                    }
                    RestaurantCheckInDialog dialog = new RestaurantCheckInDialog(v.getContext(), mRestaurant);
                    dialog.show();
                    dialog.setOnDismissListener(new OnDismissListener() {                
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (((RestaurantCheckInDialog)dialog).getStatus() == RESULT_OK) {                       
                                        App.getInstance().getPreferencesManager().checkIn(App.getInstance().getNetworkService().getCurrentUser().eMail, mRestaurant.id, /*mRestaurant.isTakeAway*/((RestaurantCheckInDialog) dialog).getCheckInMode());
                                        proceedNext(holder.comment.getText().toString(), meal);              
                            }
                        }
                    });
                    
                }
            });
            
            holder.mealImage = (ImageView) page.findViewById(R.id.img_meal);
            screenName = ((TextView) findViewById(R.id.txt_screen_name));
            screenName.setText(R.string.txt_meal_details);
            holder.comment = (EditText) page.findViewById(R.id.txt_comment);
            findViewById(R.id.btn_menu_restaurant).setVisibility(View.VISIBLE);
            quantity=1;
            
            if (meal != null) {
                holder.restaurantName = (TextView) page.findViewById(R.id.txt_restaurant_name);
                holder.restaurantName.setText(mRestaurant.name);

                holder.mealName = (TextView) page.findViewById(R.id.txt_restaurant_meal_name);
                holder.mealName.setText(meal.name);

                holder.mealDescription = (TextView) page.findViewById(R.id.txt_restaurant_meal_description);
                holder.mealDescription.setText(meal.description);

                holder.mealPrice = (TextView) page.findViewById(R.id.txt_restaurant_meal_price);
                holder.mealPriceDiscount = (TextView) page.findViewById(R.id.txt_restaurant_meal_price_discount);
                DecimalFormat decimalFormat = new DecimalFormat(getString(R.string.txt_price_string));

                holder.mealPrice.setText(decimalFormat.format(meal.priceIncludingTax));
                holder.mealPriceDiscount.setText(decimalFormat.format(meal.discountPriceIncludingTax));

                if (meal.discountPriceIncludingTax != -1 && meal.discountPriceIncludingTax != 0) {
                    holder.mealPriceDiscount.setVisibility(View.VISIBLE);
                    holder.mealPrice.setPaintFlags(holder.mealPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mealPriceDiscount.setText(decimalFormat.format(meal.discountPriceIncludingTax));
                }

                holder.mealQuantity = (TextView) page.findViewById(R.id.txt_restaurant_meal_quantity);
                holder.mealQuantity.setText(String.valueOf(quantity));
                
                imageResizer.loadImage(meal.image==null ? meal.imageThumb : meal.image, holder.mealImage);

                holder.slidingDrawer = (SlidingDrawer) page.findViewById(R.id.slidingDrawer);
                holder.slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
                    @Override
                    public void onDrawerOpened() {
                        ((ImageView) page.findViewById(R.id.img_arrow)).setImageResource(R.drawable.bullet_down_small);
                    }
                });
                holder.slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
                    @Override
                    public void onDrawerClosed() {
                        ((ImageView) page.findViewById(R.id.img_arrow)).setImageResource(R.drawable.bullet_up_small);
                    }
                });
            }
            collection.addView(page);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void finishUpdate(ViewGroup arg0) {}

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup arg0) {}
    }
    
    
}
