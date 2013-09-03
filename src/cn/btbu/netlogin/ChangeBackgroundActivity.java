package cn.btbu.netlogin;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import cn.btbu.netlogin.view.MyImageView;

public class ChangeBackgroundActivity extends Activity implements OnClickListener {
	
	
	private final static int[] ivshowId=new int[]{R.drawable.moren,R.drawable.background1,R.drawable.background2,R.drawable.background3};
	private MyImageView[] imageViews=new MyImageView[4];
	private Map<Integer,Drawable> map=new HashMap<Integer, Drawable>();
	private int currentLight;
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.changebackground);
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		currentLight = sp.getInt("backgroundId", R.drawable.background1);
		
		Drawable drawable = getResources().getDrawable(currentLight);
		//drawable=getLightDrawable(drawable);
		map.put(currentLight, drawable);
		
		
		
		for(int i=0;i<ivshowId.length;i++) {
			if(currentLight==ivshowId[i]) {
				currentLight=i;
				break;
			}
		}
		
		init();
		initDark(ivshowId[currentLight]);
		setImageViewSrc();
	}
	
	private void setImageViewSrc() {
		imageViews[0].setImageDrawable(map.get(ivshowId[0]));
		imageViews[1].setImageDrawable(map.get(ivshowId[1]));
		imageViews[2].setImageDrawable(map.get(ivshowId[2]));
		imageViews[3].setImageDrawable(map.get(ivshowId[3]));
	}

	private void initDark(int backgroundId) {
		for(int id:ivshowId) {
			if(id!=backgroundId) {
				Drawable drawable = getDarkDrawable(id);
				map.put(id, drawable);
			}
		}
	}

	private void init() {
		imageViews[0] = (MyImageView) findViewById(R.id.iv_show1);
		imageViews[1] = (MyImageView) findViewById(R.id.iv_show2);
		imageViews[2] = (MyImageView) findViewById(R.id.iv_show3);
		imageViews[3] = (MyImageView) findViewById(R.id.iv_show4);
		
		for(int i=0;i<imageViews.length;i++) {
			imageViews[i].setOnClickListener(this);
		}
	}
	
	private Drawable getDarkDrawable(int id){
		Drawable drawable = getResources().getDrawable(id);
		return changeView(true,drawable);
	}
	
	private Drawable getDarkDrawable(Drawable drawable){
		return changeView(true,drawable);
	}
	private Drawable getLightDrawable(Drawable drawable){
		return changeView(false,drawable);
	}

	private Drawable changeView(boolean isDark,Drawable drawable){
		ColorMatrix cm = new ColorMatrix();
		float contrast = 0.5f;
		if(!isDark) {
			contrast=1.0f;
		}
		cm.set(new float[] {
		contrast, 0, 0, 0, 0,
		0, contrast, 0, 0, 0,
		0, 0, contrast, 0, 0,
		0, 0, 0, 1, 0 });
		drawable.setColorFilter(new ColorMatrixColorFilter(cm));
		return drawable;
	}
	
	private void changeCurrentDrawable(int nowId){
		Drawable darkDrawable = getDarkDrawable(map.get(ivshowId[currentLight]));
		imageViews[currentLight].setImageDrawable(darkDrawable);
		imageViews[currentLight].invalidate();
		currentLight = nowId;
		Drawable lightDrawable=getLightDrawable(map.get(ivshowId[currentLight]));
		imageViews[currentLight].setImageDrawable(lightDrawable);
		imageViews[currentLight].invalidate();
		
		Intent intent = new Intent("cn.btbu.changebackground");
		intent.putExtra("backgroundId", ivshowId[currentLight]);
		sendBroadcast(intent);
	}
	
	@Override
	protected void onDestroy() {
		Editor edit = sp.edit();
		edit.putInt("backgroundId", ivshowId[currentLight]);
		edit.commit();
		super.onDestroy();
	}

	public void onClick(View v) {
		int nowId=0;
		switch (v.getId()) {
		case R.id.iv_show1:
			if(currentLight==0) {
				finish();
			}
			nowId=0;
			break;
		case R.id.iv_show2:
			if(currentLight==1) {
				finish();
			}
			nowId=1;
			break;
		case R.id.iv_show3:
			if(currentLight==2) {
				finish();
			}
			nowId=2;
			break;
		case R.id.iv_show4:
			if(currentLight==3) {
				finish();
			}
			nowId=3;
			break;
		}
		changeCurrentDrawable(nowId);
	}
}
