package cn.btbu.netlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SearchResultActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv_message=new TextView(getApplicationContext());
		Intent intent = getIntent();
		String searchResult = intent.getStringExtra("searchString");
		tv_message.setText(searchResult);
		tv_message.setOnClickListener(new OnClickListener() {
			
		//	@Override
			public void onClick(View v) {
				finish();
			}
		});
		setContentView(tv_message);
	}
}
