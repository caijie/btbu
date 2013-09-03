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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ChangeCountCategory_1Activity extends Activity implements
		OnClickListener {

	private EditText et_username;
	private EditText et_password;
	private RadioGroup radioGroup_cc;
	private Button bt_sure;
	private Button bt_cancel;
	private ProgressDialog progress;
	private String finalResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changcc_1);
		
		init();
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.sendrequest));
		progress.setCancelable(false);
		
		Intent intent = new Intent(this,NoticeActivity.class);
		startActivity(intent);
	}

	private void init() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		radioGroup_cc = (RadioGroup) findViewById(R.id.radioGroup_cc);
		bt_sure = (Button) findViewById(R.id.bt_sure);
		bt_cancel = (Button) findViewById(R.id.bt_cancel);

		bt_cancel.setOnClickListener(this);
		bt_sure.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_sure:
			String username = et_username.getText().toString();
			String password = et_password.getText().toString();
			changeCountCategory(username, password);
			break;
		case R.id.bt_cancel:
			finish();
			break;

		default:
			break;
		}
	}

	private void changeCountCategory(String username, String password) {
		int radioButtonId = radioGroup_cc.getCheckedRadioButtonId();
		String request = null;
		switch (radioButtonId) {
		case R.id.radio_cc1:
			request = Config.CC_1;
			break;
		case R.id.radio_cc2:
			request = Config.CC_2;
			break;
		case R.id.radio_cc3:
			request = Config.CC_3;
			break;
		case R.id.radio_cc4:
			request = Config.CC_4;
			break;
		}
		request = request.replace(Config.COUNTSIGN, username).replace(
				Config.PWDSIGN, password);
		new MyAsynTask().execute(request);
	}

	private final class MyAsynTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progress.dismiss();
			if (result) {
				Toast.makeText(getApplicationContext(), finalResult,Toast.LENGTH_LONG).show();
				if (finalResult.contains("³É¹¦")) {
					finish();
				}else {
					et_password.setText(null);
				}
			} else {
				Toast.makeText(getApplicationContext(),R.string.error_internet, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				URL url = new URL("http://self.btbu.edu.cn/cgi-bin/nacgi.cgi");
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(4000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				byte[] bytes = params[0].getBytes();

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
			return false;
		}

	}
}
