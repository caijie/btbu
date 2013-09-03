package cn.btbu.netlogin;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class NoticeActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(android.R.drawable.btn_default_small);
		TextView tv_message=new TextView(getApplicationContext());
		tv_message.setTextColor(Color.BLACK);
		tv_message.setText(R.string.notice);
		setContentView(tv_message);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return super.onTouchEvent(event);
		
	}
}
