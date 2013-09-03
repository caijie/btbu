package cn.btbu.netlogin;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
/**
 * �������ӷ��񣬵����������ÿ��10�������������һ����־��������ʺ�����
 * @author zcj
 */
public class NetloginService extends Service {
	
	private String username;
	private Timer timer;
	private TimerTask task;
	
//	private int counter=0;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		MyApplication application=(MyApplication) getApplication();
		username=application.username;
		timer=new Timer();
		task=new MyTimeTast();
		timer.schedule(task, 10000,10000);		
	}
	
	public void onStart(Intent intent, int startId) {  
        super.onStart(intent, startId); 
    } 
	
	@Override
	public void onDestroy() {		
		timer.cancel();
		task=null;
		super.onDestroy();
		
		MyApplication application=(MyApplication) getApplication();
		username=application.username;
		timer=new Timer();
		task=new MyTimeTast();
		timer.schedule(task, 10000,10000);	
	}
	
	 public IBinder onBind(Intent intent) {//��
			// TODO Auto-generated method stub
			return null;
		}
	
	 public void onLowMemory() {  //�ڴ治��ʱע��service
	     super.onLowMemory();  
	     onDestroy();// ע����service  
	     }  
	 
	 public void onRebind(Intent intent) {  //���³��԰�
	        super.onRebind(intent);  
	    }  
	 	 
	 public int onStartCommand(Intent intent, int flags, int startId) {  //ÿ��startService����ִ�и÷��������ķ���ִ�к���Զ�ִ��onStart()����
	          
	        return super.onStartCommand(intent, flags, startId);  
	    }  
	 
	 public boolean onUnbind(Intent intent) {  //�Ͽ���
	        return super.onUnbind(intent);  
	    }  
	 
	private class MyTimeTast extends TimerTask{

		@Override
		public void run() {
			try{
				
				URL url = new URL("https://weblogin.btbu.edu.cn/cgi-bin/netlogincgi.cgi?msajaxfix=".concat(String.valueOf(System.currentTimeMillis())));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				byte[] bytes = ("cinfo=%u67E5%u8BE2&einfo=Account%20Info&chgpwd=%u4FEE%u6539%u5BC6%u7801&logout=%u65AD%u5F00&login=%u767B%u5F55&netlogincmd=5&proxyip=127.0.0.1&newpassword=&password=&account="
						+username).getBytes();
				conn.setRequestProperty("Content-Length", String
						.valueOf(bytes.length));
				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				
				OutputStream outputStream = conn.getOutputStream();
				outputStream.write(bytes);
				
				int responseCode = conn.getResponseCode();
				if(responseCode==200) {
					InputStream is = conn.getInputStream();
					byte[] buff = new byte[64];
					is.read(buff);
					String result = new String(buff,"GBK");
					Log.e(result,"����֮ǰ");
					
//					if(forcelogout_page.et_username.getText().toString()==MainActivity.et_username.getText().toString())
					
					if(!result.contains("S205")){
						Intent intent = new Intent("Auto_off_Net");
						sendBroadcast(intent);
						Log.e(result,"�ǲ��ǵ�����");
						onDestroy();
					}
				}
			}catch (Exception e) {
//				if(counter++==3) {
					Intent intent = new Intent("Auto_off_Net");
					sendBroadcast(intent);
					onDestroy();
//				}
				e.printStackTrace();
			}
		}
	}
	
}
