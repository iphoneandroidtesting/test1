//package com.nmotion.android.adapters;
//
//import java.util.ArrayList;
//
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.nmotion.R;
//import com.nmotion.android.core.Cache;
//import com.nmotion.android.models.Group;
//import com.nmotion.android.models.Option;
//import com.nmotion.android.models.OptionValue;
//
//public class OptionListScreenAdapter extends BaseAdapter {
//	private LayoutInflater mInflater;
//	private ArrayList<Option> options;
//	ArrayList<Group> groups;
//	boolean groupMode;
//	public static final int[] OPTION_FIELD_LENGTH_LIMITERS = { 25, 15 };
//	ArrayList<ArrayList<OptionValue>> optionValuesArrayList;
//	ArrayList<ArrayList<Option>> optionsForAllGroups;
//	double sumPrice;
//	
//	public OptionListScreenAdapter(ArrayList<ArrayList<Option>> optionsForAllGroups, ArrayList<Option> options, ArrayList<Group> groups, ArrayList<ArrayList<OptionValue>> optionValuesArrayList, LayoutInflater mInflater, boolean groupMode) {
//		this.options = options;
//		this.groups = groups;
//		this.groupMode = groupMode;
//		this.mInflater = mInflater;
//		this.optionValuesArrayList = optionValuesArrayList;
//		this.optionsForAllGroups=optionsForAllGroups;
//	}
//
//	@Override
//	public int getCount() {
//		if (groupMode)
//			return groups.size();
//		else
//			return options.size();
//	}
//
//	@Override
//	public Option getItem(int position) {
//		return options.get(position);
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder holder = null;
//		holder = new ViewHolder();
//		convertView = mInflater.inflate(R.layout.option_screen_list_element_layout, null);
//		holder.optionName = (TextView) convertView.findViewById(R.id.option_screen_option_name);
//		holder.selected = (ImageView) convertView.findViewById(R.id.imageView1);
//		holder.plusMinus = (ImageView) convertView.findViewById(R.id.ImageView01);
//		holder.selectedOptionValueName = (TextView) convertView.findViewById(R.id.option_value_list_screen_option_value_selection_image_view);
//		holder.optionPrice = (TextView) convertView.findViewById(R.id.option_screen_option_price);
//		convertView.setTag(holder);
//		holder.selected.setVisibility(View.GONE);
//		if (groupMode) {
//			
//			Group currentGroup = groups.get(position);
//			holder.optionName.setText(currentGroup.name);
//			//holder.optionPrice.setText(currentGroup.defaultOptionValueName);
//			boolean checkboxesFlag=false;
//			if (optionsForAllGroups!=null && optionsForAllGroups.size()>0 &&optionsForAllGroups.get(position)!=null && optionsForAllGroups.get(position).size()==1){
//				holder.optionPrice.setVisibility(View.VISIBLE);
//				convertView.findViewById(R.id.linearLayout1).setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.TOP));
//				holder.optionPrice.setText(optionsForAllGroups.get(position).get(0).selectedOptionValueName);
//			}else{
//				String text="";
//				//float textPrice=0;
//				for (int i = 0; i < optionsForAllGroups.get(position).size(); i++) {					
//					if(optionsForAllGroups.get(position).get(i).optionType==0 && optionsForAllGroups.get(position).get(i).selectedOptionValueName.toLowerCase().equals("yes")){
//						text+=optionsForAllGroups.get(position).get(i).name;
//						if (i!=optionsForAllGroups.get(position).size())
//							text+="; ";
//						checkboxesFlag=true;
//						//textPrice+=optionsForAllGroups.get(position).get(i).);
//					}
//				} 
//				if (!checkboxesFlag){
//					holder.optionPrice.setVisibility(View.GONE);
//					convertView.findViewById(R.id.linearLayout1).setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER));
//				}
//				else {
//					
//					checkboxesFlag = false;
//					if (sumPrice>0)
//						text="$"+sumPrice+"; "+text;
//				if (text.length()>0){
//					convertView.findViewById(R.id.linearLayout1).setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.TOP));
//					holder.optionPrice.setText(text);
//				}
//				
//			}
//			
//				
//		//	}else
//
//				
//			}
//			holder.selected.setVisibility(View.GONE);
//		} else {
//			holder.selected.setVisibility(View.VISIBLE);
//			Option currentOption = options.get(position);
//			double selectedOptionValuePrice = 0;
//			for (int i = 0; i < optionValuesArrayList.get(position).size(); i++) {
//				if (optionValuesArrayList.get(position).get(i).id == currentOption.selectedOptionValueId) {
//					selectedOptionValuePrice = optionValuesArrayList.get(position).get(i).price;
//					sumPrice = selectedOptionValuePrice;
//					break;
//				}
//			}
//			// if (convertView == null) {
//
//			/*
//			 * } else { holder = (ViewHolder) convertView.getTag(); }
//			 */
//			if (currentOption.name.length() > OPTION_FIELD_LENGTH_LIMITERS[0]) {
//				holder.optionName.setText(currentOption.name.substring(0, OPTION_FIELD_LENGTH_LIMITERS[0]) + "...");
//			} else
//				holder.optionName.setText(currentOption.name);
//			holder.selectedOptionValueName.setText("");
//			holder.selected.setBackgroundResource(R.drawable.btn_check_buttonless_off);
//
//			if (currentOption.selectedOptionValueId != -1 && currentOption.selectedOptionValueName != null) {
//				if (isYes(currentOption.selectedOptionValueName, position)) {
//					holder.selected.setBackgroundResource(R.drawable.btn_check_buttonless_on);
//					holder.plusMinus.setVisibility(View.VISIBLE);
//					holder.plusMinus.setImageResource(R.drawable.minus);
//				} else if (isNo(currentOption.selectedOptionValueName, position)) {
//					holder.selected.setBackgroundResource(R.drawable.btn_check_buttonless_off);
//					holder.plusMinus.setVisibility(View.VISIBLE);
//					holder.plusMinus.setImageResource(R.drawable.plus);
//					holder.selectedOptionValueName.setText("");
//				} else {
//					holder.selected.setBackgroundResource(R.drawable.btn_check_buttonless_on);
//					if (currentOption.selectedOptionValueName.length() > OPTION_FIELD_LENGTH_LIMITERS[1]) {
//						holder.selectedOptionValueName.setText(currentOption.selectedOptionValueName.substring(0, OPTION_FIELD_LENGTH_LIMITERS[1]) + "...");
//					} else
//						holder.selectedOptionValueName.setText(currentOption.selectedOptionValueName);
//				}
//				holder.optionPrice.setVisibility(View.VISIBLE);
//				convertView.findViewById(R.id.linearLayout1).setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.TOP));
//				holder.optionPrice.setText("$" + String.format("%.2f", selectedOptionValuePrice));
//			} else {
//				holder.selected.setBackgroundResource(R.drawable.btn_check_buttonless_off);
//				holder.selectedOptionValueName.setText("");
//			}
//			if (selectedOptionValuePrice == 0){
//				holder.optionPrice.setVisibility(View.GONE);
//				convertView.findViewById(R.id.linearLayout1).setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER));
//			}				
//		}
//		if (Cache.isEditMode){
//			holder.plusMinus.setVisibility(View.GONE);
//			holder.selected.setBackgroundResource(R.drawable.btn_check_buttonless_on);
//		}
//		//if (holder.optionPrice.getVisibility()==View.GONE || holder.optionPrice.getText().length()<1)
//			
//		return convertView;
//	}
//
//	public static class ViewHolder {
//		public TextView optionName, selectedOptionValueName, optionPrice;
//		public ImageView selected, plusMinus;
//	}
//
//	
//	private boolean isYesNo(int position) {
//		/*
//		 * System.out.println("YESNO+======================");
//		 * System.out.println(optionValuesArrayList.get(position).size());
//		 * System.out.println(getItem(0).selectedOptionValueName);
//		 */
//		if (/* getCount() == 2 */optionValuesArrayList.get(position).size() == 2
//				&& (getItem(position).selectedOptionValueName.toLowerCase().equals("no") || getItem(position).selectedOptionValueName.toLowerCase().equals("yes")))
//			return true;
//		else
//			return false;
//	}
//
//	private boolean isYes(String name, int position) {
//		if (isYesNo(position) && name.toLowerCase().equals("yes"))
//			return true;
//		else
//			return false;
//	}
//
//	private boolean isNo(String name, int position) {
//		if (isYesNo(position) && name.toLowerCase().equals("no"))
//			return true;
//		else
//			return false;
//	}
//}
