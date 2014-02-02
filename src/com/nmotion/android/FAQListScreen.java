package com.nmotion.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nmotion.R;

public class FAQListScreen extends BaseActivity implements OnItemClickListener {
	private QuestionAdapter questionsAdapter;
	private String[] questions;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_faq_list_screen);

		listView = (ListView) findViewById(android.R.id.list);

		if (!App.getInstance().getNetworkService().isLoggedIn()) {
			findViewById(R.id.btn_menu_sign_in).setVisibility(View.VISIBLE);
		}
		questions = getResources().getStringArray(R.array.questions_array);
		questionsAdapter = new QuestionAdapter();

		listView.setAdapter(questionsAdapter);
		listView.setOnItemClickListener(this);
	}

	public void onSignInClick(View view) {
		Intent intent = new Intent(getApplicationContext(), LogInScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		openQuestionDetails(position);
	}

	private void openQuestionDetails(int answerPosition) {
		Intent intent = new Intent(this, FAQDetailsScreen.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(FAQDetailsScreen.ANSWER_PISITION_INT, answerPosition);
		startActivity(intent);
	}

	private class QuestionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return questions.length;
		}

		@Override
		public CharSequence getItem(int position) {
			return questions[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(FAQListScreen.this).inflate(R.layout.layout_question_item, null);
				holder = new ViewHolder();
				holder.question = (TextView) convertView.findViewById(R.id.question);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.question.setText(getItem(position));
			return convertView;
		}

		private class ViewHolder {
			TextView question;
		}
	}
}