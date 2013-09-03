package cn.btbu.netlogin;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * 主界面
 * @author zcj
 */
public class MainActivity extends Activity implements OnClickListener,
		OnTimeSetListener {

	private static final int NOTIFICATION_ID = 103;

	/**
	 * 上网登陆状态
	 *
	 */
	private enum Sata {
		NETLOGIN, SEARCH, MODIFY_UP, OFF_LINE  //登录状态、查询状态、修改状态、下线状态
	}

	private Sata Satamode;  //状态实例化
	private EditText et_password;  //密码框
	private EditText et_username; //账号框
	private Button bt_connect;  //开始连接按钮
	private Button bt_modify_pw; //修改密码按钮
	private Button bt_search;  //查询按钮

	private Timer timer;             //定时器   上网时间
	private TimerTask task;           //定时任务
	private Button bt_off_line;   //断线按钮
	private TextView tv_login_time;  //定时断网弹窗
	private LinearLayout ll_editmode;  //修改密码布局
	private LinearLayout ll_successmode;  //成功修改密码之后布局
	private String username;		//用户名
	private String password;		//密码
	private String searchString;	  //查询结果
	private String finalResult;   //
	private Timer autoOfflineTimer;  //自动下线时间
	private long startTime;    // 登陆开始时间
	private boolean flag;    //标志
	

	private enum Mode {
		Edit_login, Success_login, Modify_pw //编辑登录模式、成功登陆模式、修改密码模式
	}

	private Intent serviceIntent;   //主线程
	private Handler handler = new Handler() {  
		// 响应主线程上面的其他操作，分发数据
		//将其他操作绑定到当前线程和消息队列中

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Config.CHANGETIME:				
				tv_login_time.setText(getString(R.string.use_time).concat(
						msg.obj.toString()));
				break;
			case Config.SUCCESS_OFF_LINE:		//成功断开连接
				ChangeMode(Mode.Edit_login);
				startTime = 0;
				if (timer != null) {
					timer.cancel();
				}
				timer = null;
				task = null;
			case Config.FAIL_OFF_LINE:			//断开连接失败
				ChangeMode(Mode.Edit_login);
				Satamode=Sata.OFF_LINE;
				Toast.makeText(getApplicationContext(),
						R.string.fail_off_line_message, Toast.LENGTH_SHORT)
						.show();
			case Config.OFF_LINE_SIGN:			//断开连接标志
				if (Satamode == Sata.NETLOGIN) {
					Satamode = Sata.OFF_LINE;
					new ConnectionTask().execute(Sata.OFF_LINE);
					stopService(serviceIntent);
				}
			}
		}

	};
	private MyApplication application;
	private Button bt_sure; // 
	private Button bt_cancel;  //网络下线按钮
	private LinearLayout ll_mainpanel; //
	private LinearLayout ll_modify_pw;  //
	private EditText et_confimpassword; //
	private EditText et_newpassword;  //
	private String newPassword;  //
	private NotificationManager mNm;
	private Notification notification;
	private RemoteViews remoteViews;
	private Time time;
	private BroadcastReceiver receiver;
	private ProgressDialog progress;
	private MenuItem autoOfflineMenu;
	private boolean isAutoOffline;
	private SharedPreferences sp;
	private CheckBox cb_rememberpwd;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		int backgroundId = sp.getInt("backgroundId", R.drawable.background1);
		getWindow().setBackgroundDrawableResource(backgroundId);
		setNotification();
		setBroadcastReceiver();

		if (!checkNet()) { 			//检查WIFI是否打开
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(R.string.wifimessage);
			builder.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
							intent.setClassName("com.android.settings",
									"com.android.settings.wifi.WifiSettings");
							startActivity(intent);
						//	finish();
						
						}
					});

			builder.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
						//	finish();		//点击设置网络登陆退出键   退出程序
						}
					});
			builder.create().show();
		}
		
		application = (MyApplication) getApplication();
		timer = new Timer();
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.sendrequest));
		progress.setCancelable(false);
		init();
		String username = sp.getString("username", null);
		if (username != null) {
			et_username.setText(username);
		}
		String password=sp.getString("password", null);
		if(password!=null) {
			cb_rememberpwd.setChecked(true);
			et_password.setText(password);
		}	
	}

	private void setBroadcastReceiver() {  //设置通信传递和消息过滤注册
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				
				if (intent.getAction().equals("cn.btbu.changebackground")) {  //换肤
					int backgroundId = intent.getIntExtra("backgroundId",R.drawable.moren);
					getWindow().setBackgroundDrawable(getResources().getDrawable(backgroundId));
				}else {
			//		remoteViews.setImageViewResource(R.id.iv_sign,android.R.drawable.presence_busy);
			//		remoteViews.setTextViewText(R.id.tv_message,getString(R.string.offline));
			//		notification.icon = android.R.drawable.presence_busy;
			//		mNm.notify(NOTIFICATION_ID, notification);					
					do{
				//	handler.sendEmptyMessage(Config.FAIL_OFF_LINE);
				//	handler.sendEmptyMessage(Config.OFF_LINE_SIGN);
					new ConnectionTask().execute(Sata.NETLOGIN);//自动上线
					}while(Satamode == Sata.OFF_LINE);
				}
			}
		};

		IntentFilter filter1 = new IntentFilter("Auto_off_Net");
		IntentFilter filter2 = new IntentFilter("cn.btbu.changebackground");
		
/*		if((et_username.getText().toString()!=null)&&(et_password.getText().toString()!=null)){
			String message1 = "Force_off_Net";
			Log.e(message1,"chucuola");
			String message2 = message1.concat(et_username.getText().toString());
			String message = message2.concat(et_password.getText().toString());
			IntentFilter filter3 = new IntentFilter(message);
			registerReceiver(receiver, filter3);
		}else{
			
		}
	*/	
		registerReceiver(receiver, filter1);
		registerReceiver(receiver, filter2);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);
		autoOfflineMenu = menu.getItem(0);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_auto_off_line:
			if (isAutoOffline) {
				autoOfflineTimer.cancel();
				autoOfflineTimer = null;
				changeAutoOfflineMode();
				Toast.makeText(getApplicationContext(),
						R.string.off_autooffline, Toast.LENGTH_SHORT).show();
			} else {
				time = new Time();
				time.setToNow();
				new TimePickerDialog(this, this, time.hour, time.minute,
						DateFormat.is24HourFormat(this)).show();
			}
			break;
		case R.id.menu_about:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_change_background:
			Intent intent2 = new Intent(this, ChangeBackgroundActivity.class);
			startActivity(intent2);
			break;
		case R.id.menu_count_category:
			Intent intent3 = new Intent(this, ChangeCountCategory_1Activity.class);
			startActivity(intent3);
			break;
		case R.id.menu_forcelogout:
			Intent intent4 = new Intent(this, forcelogout_page.class);
			startActivity(intent4);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 设置通知
	 */
	private void setNotification() {
		mNm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(android.R.drawable.presence_busy, "",
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_NO_CLEAR;
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.notificationview);
		remoteViews.setTextViewText(R.id.tv_message,
				getString(R.string.offline));
		remoteViews.setTextColor(R.id.tv_message, Color.BLACK);
		notification.contentView = remoteViews;
		Intent intent = new Intent(MainActivity.this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				MainActivity.this, 1, intent, 0);
		notification.contentIntent = pendingIntent;
		mNm.notify(NOTIFICATION_ID, notification);
	}

	/**
	 * 检查wifi连接
	 * @return
	 */
	private boolean checkNet() {
		ConnectivityManager mCm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = mCm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return networkInfo.isConnected();
	}

	private void init() {

		ll_editmode = (LinearLayout) findViewById(R.id.ll_editmode);
		ll_successmode = (LinearLayout) findViewById(R.id.ll_successmode);

		et_password = (EditText) findViewById(R.id.et_password);
		et_username = (EditText) findViewById(R.id.et_username);

		bt_connect = (Button) findViewById(R.id.bt_connect);
		bt_modify_pw = (Button) findViewById(R.id.bt_modify_pw);
		bt_search = (Button) findViewById(R.id.bt_search);
		bt_off_line = (Button) findViewById(R.id.bt_off_line);

		tv_login_time = (TextView) findViewById(R.id.tv_login_time);

		bt_sure = (Button) findViewById(R.id.bt_sure);
		bt_cancel = (Button) findViewById(R.id.bt_cancel);
		ll_mainpanel = (LinearLayout) findViewById(R.id.ll_mainpanel);
		ll_modify_pw = (LinearLayout) findViewById(R.id.ll_modify_pw);
		et_confimpassword = (EditText) findViewById(R.id.et_confimpassword);
		et_newpassword = (EditText) findViewById(R.id.et_newpassword);
		cb_rememberpwd = (CheckBox) findViewById(R.id.cb_rememberpwd);

		bt_off_line.setOnClickListener(this);
		bt_connect.setOnClickListener(this);
		bt_modify_pw.setOnClickListener(this);
		bt_search.setOnClickListener(this);
		bt_sure.setOnClickListener(this);
		bt_cancel.setOnClickListener(this);
	}
	
	/**
	 * 模式改变时改变页面
	 * @param mode
	 */
	private void ChangeMode(Mode mode) {
		if (mode == Mode.Success_login) {
			ll_editmode.setVisibility(View.GONE);
			ll_successmode.setVisibility(View.VISIBLE);
			ll_mainpanel.setVisibility(View.VISIBLE);
			ll_modify_pw.setVisibility(View.GONE);
		} else if (mode == Mode.Edit_login) {
			ll_mainpanel.setVisibility(View.VISIBLE);
			ll_modify_pw.setVisibility(View.GONE);
			ll_editmode.setVisibility(View.VISIBLE);
			ll_successmode.setVisibility(View.GONE);
		} else {
			mode = Mode.Modify_pw;
			ll_mainpanel.setVisibility(View.GONE);
			ll_modify_pw.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (Satamode == Sata.NETLOGIN) {
			timer.cancel();
			task = null;
			timer = null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Satamode == Sata.NETLOGIN) {
			timer = new Timer();
			task = new MyTimeTast();
			timer.scheduleAtFixedRate(task, 1000, 1000);
		}
	}

	public void onClick(View v) {
		if (flag) {
			return;
		}
		password = et_password.getText().toString();
		username = et_username.getText().toString();

		switch (v.getId()) {
		case R.id.bt_connect:    //连接网络
			application.username = username;
            
			new ConnectionTask().execute(Sata.NETLOGIN);
			break;
		case R.id.bt_modify_pw:        //修改密码
			ChangeMode(Mode.Modify_pw);
			break;

		case R.id.bt_search:			//查询
			Satamode = Sata.SEARCH;
			new ConnectionTask().execute(Sata.SEARCH);
			break;

		case R.id.bt_off_line:			//断开连接
			Satamode = Sata.OFF_LINE;
			new ConnectionTask().execute(Sata.OFF_LINE);
			stopService(serviceIntent);

			break;
		case R.id.bt_cancel:			
			ChangeMode(Mode.Edit_login);
			break;
		case R.id.bt_sure:										
			Satamode = Sata.MODIFY_UP;
			modify_pw(username, password);
			break;
		}
	}
	
	

	/**
	 * 修改密码
	 */
	private void modify_pw(String username2, String password2) {
		newPassword = et_newpassword.getText().toString();
		String confimPassword = et_confimpassword.getText().toString();
		if (!newPassword.equals(confimPassword)) {
			et_newpassword.setText(null);
			et_confimpassword.setText(null);
			Toast.makeText(getApplicationContext(), R.string.passwordnotequal,
					Toast.LENGTH_SHORT).show();
		} else {
			new ConnectionTask().execute(Sata.MODIFY_UP);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
		timer = null;
		task = null;
		mNm.cancel(NOTIFICATION_ID);
		unregisterReceiver(receiver);
		if (Satamode == Sata.NETLOGIN) {
			Satamode = Sata.OFF_LINE;
			new ConnectionTask().execute(Sata.OFF_LINE);
			stopService(serviceIntent);
		}
	}
	
	/**
	 * 请求网络
	 * @author zcj
	 *
	 */
	private class ConnectionTask extends AsyncTask<Sata, Void, Integer> {

		@Override
		protected void onPreExecute() {
			if (!MainActivity.this.isFinishing()) {
				progress.show();
			}
		}

		@Override
		protected Integer doInBackground(Sata... params) {
			try {
				Sata param = params[0];
				URL url = new URL(
						"https://weblogin.btbu.edu.cn/cgi-bin/netlogincgi.cgi?msajaxfix="
								.concat(String.valueOf(System
										.currentTimeMillis())));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(4000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				byte[] bytes = null;
				if (param == Sata.NETLOGIN) {
					/**
					 * 登陆
					 */
					bytes = ("cinfo=%u67E5%u8BE2&einfo=Account%20Info&chgpwd=%u4FEE%u6539%u5BC6%u7801&logout=%u65AD%u5F00&login=%u767B%u5F55&netlogincmd=1&proxyip=127.0.0.1&newpassword=&password="
							+ password + "&account=" + username).getBytes();
				} else if (param == Sata.OFF_LINE) {
					/**
					 * 下线
					 */
					bytes = ("cinfo=%u67E5%u8BE2&einfo=Account%20Info&chgpwd=%u4FEE%u6539%u5BC6%u7801&logout=%u65AD%u5F00&login=%u767B%u5F55&netlogincmd=0&proxyip=127.0.0.1&newpassword=&password=&account=" + username)
							.getBytes();
				} else if (param == Sata.SEARCH) {
					/**
					 * 查询
					 */
					bytes = Config.SEARCH.replace(Config.COUNTSIGN, username)
							.replace(Config.PWDSIGN, password).getBytes();
				} else if (param == Sata.MODIFY_UP) {
					/**
					 * 修改密码
					 */
					bytes = Config.MODIFY_UP
							.replace(Config.COUNTSIGN, username)
							.replace(Config.PWDSIGN, password)
							.replace(Config.NEWPWDSIGN, newPassword).getBytes();
				}
				conn.setRequestProperty("Content-Length",
						String.valueOf(bytes.length));
				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				
				
				
				
				OutputStream outputStream = conn.getOutputStream();
				outputStream.write(bytes);

				int responseCode = conn.getResponseCode();
				if (responseCode == 200) {
					InputStream is = conn.getInputStream();
					byte[] buff = new byte[600];
					is.read(buff);
					String result = new String(buff, "GBK");
					finalResult = result;
					if (Satamode == Sata.SEARCH) {
						if (result.contains(Config.REPLACESTRING1)) {
							result = formatSearchString(result);
						}
					}
					result = result.substring(1, result.indexOf("."));
					return getSituation(result);
				}
			} catch (Exception e) {
				e.printStackTrace();
				onDestroy();
				return R.string.error_internet;
			} 
			return R.string.error_internet;
		}

		private String formatSearchString(String result) {
			result = result.replace(Config.REPLACESTRING1, "").replace(
					Config.REPLACESTRING2, "");
			searchString = result;
			return Config.SUCCESS_SEARCH_ADD_SIGN.concat(result);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == -1) {
				Satamode = Sata.NETLOGIN;
				ChangeMode(Mode.Success_login);
				remoteViews.setImageViewResource(R.id.iv_sign,android.R.drawable.presence_online);
				remoteViews.setTextViewText(R.id.tv_message,getString(R.string.online));
				notification.icon = android.R.drawable.presence_online;
				mNm.notify(NOTIFICATION_ID, notification);

				task = new MyTimeTast();
				timer = new Timer();
				timer.scheduleAtFixedRate(task, 1000, 1000);
				
				Editor edit = sp.edit();
				if(cb_rememberpwd.isChecked()) {
					edit.putString("password", password);
				}else {
					edit.remove("password");
				}
				edit.putString("username", username);
				edit.commit();
			} else if (result == -2) {
				Intent intent = new Intent(getApplicationContext(),
						SearchResultActivity.class);
				intent.putExtra("searchString", searchString.trim());
				startActivity(intent);
			} else if (result == 0) {
				Toast.makeText(getApplicationContext(),finalResult.substring(finalResult.indexOf(".") + 1).trim(), Toast.LENGTH_SHORT).show();
			} else if (result == -3) {
				et_password.setText(null);
				ChangeMode(Mode.Edit_login);
				Toast.makeText(getApplicationContext(),finalResult.substring(finalResult.indexOf(".") + 1).trim(), Toast.LENGTH_SHORT).show();
			} else if (result == R.string.success_off_line_message) {
				if (MainActivity.this.isFinishing()) {
					return;
				}
				remoteViews.setImageViewResource(R.id.iv_sign,
						android.R.drawable.presence_busy);
				remoteViews.setTextViewText(R.id.tv_message,
						getString(R.string.offline));
				notification.icon = android.R.drawable.presence_busy;
				mNm.notify(NOTIFICATION_ID, notification);
			} else {
				Toast.makeText(getApplicationContext(), result,
						Toast.LENGTH_SHORT).show();
			}
			progress.dismiss();
		}
	}
	
	private int getSituation(String result) {

		int message_id = 0;
		if (result.equals(getString(R.string.success_login_code))) {
			message_id = -1;
			serviceIntent = new Intent(this, NetloginService.class);
			startService(serviceIntent);   // 成功登陆	
			


		} else if (result.equals(getString(R.string.error_count_online_code))) {
			message_id = R.string.error_count_online_message; //此账号已在线
		} else if (result.equals(getString(R.string.error_fee_code))) {
			message_id = R.string.error_fee_message; // 费用不足
		} else if (result.equals(getString(R.string.error_up_code))) {
			message_id = R.string.error_up_message;  //账号或密码错误
		} else if (result.equals(getString(R.string.error_iporservice_code))) {
			message_id = R.string.error_iporservice_message;  //服务器忙或IP不正确
		} else if (result.equals(getString(R.string.success_off_line_code))) {
			message_id = R.string.success_off_line_message; //成功断开外网
			handler.sendEmptyMessage(Config.SUCCESS_OFF_LINE);
		} else if (result.equals(Config.SUCCESS_SEARCH)) {
			message_id = -2;
		} else if (result.equals(Config.SUCCESS_MODIFY)) {
			message_id = -3;
		}
		return message_id;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(R.string.choose_action);
			builder.setPositiveButton(R.string.quit,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			builder.setNeutralButton(R.string.minix,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
							intent.setAction("android.intent.action.MAIN");
							intent.addCategory("android.intent.category.HOME");
							startActivity(intent);
						}
					});
			builder.setNegativeButton(R.string.cancel, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MyTimeTast extends TimerTask {

		public MyTimeTast() {
			if (startTime == 0) {
				Time time = new Time();
				time.setToNow();
				startTime = time.toMillis(false);
			}
		}

		@Override
		public void run() {
			long time = (System.currentTimeMillis() - startTime) / 1000;
			String formatTime = formatTime(time);

			Message msg = Message.obtain();
			msg.obj = formatTime;
			msg.what = Config.CHANGETIME;
			handler.sendMessage(msg);
		}

	}

	private String formatTime(long time) {
		String hour = String.valueOf((int) (time / 3600));
		String minute = String.valueOf((int) (time % 3600) / 60);
		String second = String.valueOf((int) (time % 60));

		StringBuilder sb = new StringBuilder();
		if (hour.length() == 1) {
			sb.append("0");
		}
		sb.append(hour.concat(":"));
		if (minute.length() == 1) {
			sb.append("0");
		}
		sb.append(minute.concat(":"));
		if (second.length() == 1) {
			sb.append("0");
		}
		sb.append(second);
		return sb.toString();
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

		time.setToNow();

		if (minute < time.minute) {
			hourOfDay--;
			minute += 60;
		}
		if (hourOfDay < time.hour) {
			hourOfDay += 24;
		}

		minute -= time.minute;
		hourOfDay -= time.hour;
		Toast.makeText(getApplicationContext(),
				"距离自动断网还剩" + hourOfDay + "时" + minute + "分", Toast.LENGTH_SHORT)
				.show();
		if (autoOfflineTimer != null) {
			autoOfflineTimer.cancel();
		}
		autoOfflineTimer = new Timer();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.add(Calendar.MINUTE, minute);
		autoOfflineTimer.schedule(new AutoOffline(), calendar.getTime());
		changeAutoOfflineMode();
	}

	void changeAutoOfflineMode() {  //修改定时时间
		if (isAutoOffline) {
			autoOfflineMenu
					.setIcon(android.R.drawable.button_onoff_indicator_off);
			isAutoOffline = false;
		} else {
			autoOfflineMenu
					.setIcon(android.R.drawable.button_onoff_indicator_on);
			isAutoOffline = true;
		}
	}

	private final class AutoOffline extends TimerTask {  //定时时间到，自动断开网络

		@Override
		public void run() {
			handler.sendEmptyMessage(Config.OFF_LINE_SIGN);
		}
	}

}