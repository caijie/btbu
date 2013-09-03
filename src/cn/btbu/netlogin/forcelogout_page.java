package cn.btbu.netlogin;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.htmlparser.jericho.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
//import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class forcelogout_page extends Activity implements
OnClickListener {
	
	private EditText et_username;
	private EditText et_password;
	private Button bt_sure;
	private Button bt_cancel;
	private ProgressDialog progress;
	private String finalResult;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forcelogout);
		
		init();
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.sendrequest));
		progress.setCancelable(false);
		
	}
	private void init() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		
		bt_sure = (Button) findViewById(R.id.bt_sure);
		bt_cancel = (Button) findViewById(R.id.bt_cancel);

		bt_cancel.setOnClickListener(this);
		bt_sure.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_sure:
			String request = null;
			new MyTask().execute(request);
			break;
		case R.id.bt_cancel:
			finish();
			break;

		default:
			break;
		}
}

	public final class MyTask extends AsyncTask<String, Void, Boolean> {//多线程异步

		@Override
		protected void onPreExecute() {//主要是实例化和准备工作
			super.onPreExecute();
			progress.show();
		}

		@Override
		public Boolean doInBackground(String... params) {//onPreExecute()之后执行，主要在后台执行复杂的操作
			try {
				String username = et_username.getText().toString();
				String password = et_password.getText().toString();

				URL url = new URL("http://self.btbu.edu.cn/cgi-bin/nacgi.cgi");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(4000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				byte[] bytes = null;
				bytes = ("textfield="+username+"&textfield2="+password+"&Submit=%CC%E1%BD%BB&nacgicmd=9&radio=1&jsidx=1").getBytes();
				
				conn.setRequestProperty("Content-Length",
						String.valueOf(bytes.length));
				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");

				OutputStream outputStream = conn.getOutputStream();
				outputStream.write(bytes);
				
				int responseCode = conn.getResponseCode();
				String result = null;
				if (responseCode == 200) {
					InputStream is = conn.getInputStream();
					Source source = new Source(is);
					List<Element> elements = source.getAllElements("td");
					for (Element element : elements) {
						String value = element.getAttributeValue("class");
						if (value != null && value.equals("STYLE11")) {
							result = element.getTextExtractor().toString();
							finalResult = result;
							return true;
						}
					}
				}
		} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		public void onPostExecute(Boolean result) {//doInBackground()之后执行，返回结果
			super.onPostExecute(result);
			progress.dismiss();			
			if (result) {
				Toast.makeText(getApplicationContext(), finalResult,Toast.LENGTH_LONG).show();
				if (finalResult.contains("成功")) {
					
					String message1 = "Force_off_Net";
					String message2 = message1.concat(et_username.getText().toString());
					String message = message2.concat(et_password.getText().toString());
					Intent intent = new Intent(message);
					sendBroadcast(intent);
					
					finish();
				}else {
					et_password.setText(null);
				}
			} else {
				Toast.makeText(getApplicationContext(),R.string.error_internet, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
