package com.nmotion.android.view;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.image.loader.ImageFetcher;
import com.nmotion.R;
import com.nmotion.android.RestaurantInfoScreen;
import com.nmotion.android.models.Restaurant;

public class RestaurantMapListDialog extends Dialog implements OnClickListener, OnItemClickListener{
    View backBtn;
    ListView listView;
    Context context;
    ArrayList<Restaurant> restaurants;
    
    public RestaurantMapListDialog(Context context, ArrayList<Restaurant> restaurants) {
        super(context);
        this.context=context;
        this.restaurants=restaurants;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_restaurant_list_map_dialog);
        
        backBtn = findViewById(R.id.button1);
        backBtn.setOnClickListener(this);
        
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(new BalloonAdapter());
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                dismiss();
                break;
            default:
                break;
        }
    }  
    
    class BalloonAdapter extends BaseAdapter {

        @Override
        public int getCount() {
                return restaurants.size();
        }

        @Override
        public Restaurant getItem(int position) {
                return restaurants.get(position);
        }

        @Override
        public long getItemId(int position) {
                return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                Restaurant item = getItem(position);
                ViewHolder holder = null;
                //if (convertView == null) {
                        holder = new ViewHolder();
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.balloon_restaurant_item, null);
                        holder.title = (TextView) convertView.findViewById(R.id.balloon_item_title);
                        holder.image = (ImageView) convertView.findViewById(R.id.balloon_item_image);
                        /*convertView.setTag(holder);
                } else {
                        holder = (ViewHolder) convertView.getTag();
                }*/
                holder.title.setText(item.name);
                ImageFetcher fetcher = new ImageFetcher(getContext(), -1);                
                fetcher.setLoadingImage(R.drawable.photo_def_small);
                if (item.image != null) {
                        fetcher.loadImage(item.image, holder.image);
                }
                return convertView;
        }

        private class ViewHolder {
                private TextView title;
                private ImageView image;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), RestaurantInfoScreen.class);
        intent.putExtra(RestaurantInfoScreen.DATA_RESTAURANT_ID, restaurants.get(position).id);
        getContext().startActivity(intent);
        dismiss();
    }
}
