package com.nmotion.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nmotion.R;
import com.nmotion.android.models.MenuCategory;

public class MenuCategoriesScreenAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<MenuCategory> objects;

	public MenuCategoriesScreenAdapter(Context context, List<MenuCategory> objects) {
		this.mInflater = LayoutInflater.from(context);
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		MenuCategory currentMenu = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_menu_categories, null);
			holder.name = (TextView) convertView.findViewById(R.id.txt_menu_category_name);
			holder.description = (TextView) convertView.findViewById(R.id.txt_menu_category_description);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(currentMenu.name);
		if (currentMenu.description != null) {
			holder.description.setVisibility(View.VISIBLE);
			holder.description.setText(currentMenu.description);
		}
		return convertView;
	}

	public static class ViewHolder {
		public TextView name, description;
	}

	@Override
	public int getCount() {
		return objects.size();
	}

	@Override
	public MenuCategory getItem(int position) {
		return objects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return objects.get(position).id;
	}
}
