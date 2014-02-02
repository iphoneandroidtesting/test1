package com.nmotion.android;

import com.nmotion.R;
import com.nmotion.android.utils.AppUtils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class FAQDetailsScreen extends Activity {
	public static final String ANSWER_PISITION_INT = "ansver_position";
	private TextView txtAnswer;
	private TextView txtQuestion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_faq_details_screen);
		findViewById(R.id.btn_menu_list).setVisibility(View.VISIBLE);
		txtQuestion = (TextView) findViewById(R.id.question);
		txtAnswer = (TextView) findViewById(R.id.answer);
		if (getIntent().hasExtra(ANSWER_PISITION_INT)) {
			int position = getIntent().getIntExtra(ANSWER_PISITION_INT, -1);
			String question = getResources().getStringArray(R.array.questions_array)[position];
			String answer = getResources().getStringArray(R.array.answers_array)[position];
			txtQuestion.setText(question);
			txtAnswer.setText(answer);
		} else {
			finish();
		}
	}
	
	@Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            if (App.getInstance().getPreferencesManager().isCrash()){
                AppUtils.showDialog(this, "Error", getString(R.string.crash_message)).setOnDismissListener(new OnDismissListener() {                    
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();                        
                    }
                });
            }
        }

	public void onListClick(View view) {
		finish();
	}
}
