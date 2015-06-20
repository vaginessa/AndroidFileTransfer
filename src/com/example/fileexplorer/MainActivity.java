package com.example.fileexplorer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.http.conn.util.InetAddressUtils;

import com.scnuly.utils.FileUtils;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private Button buttonOpen = null;
	private Button buttonSent = null;
	private Button buttonPaint = null;
	private Button buttonMouse = null;
	private EditText editText = null;
	private ProgressBar fileProgress = null;
	private TextView ipAddr = null;
	private TextView info = null;
	private String localIp = null;
	private String serverIp = null;
	private WifiManager wifimanager = null;
	
	static final int NETWORK_TOKEN_FILE_TRANSFER    = 0xfffe0001;
	static final int NETWORK_TOKEN_PAINT_MODE       = 0xfffd0002;
	static final int NETWORK_TOKEN_MOUSE_CONTROL    = 0xfffc0003;
    
	static final int HANDLER_UPDATE_UI = 1;  
	static final int HANDLER_UPDATE_PROGRESSBAR = 2; 
	static final int HANDLER_UPDATE_PROGRESSSTATE = 3;
	static final int HANDLER_UPDATE_CONNECT = 4;
	static final int HANDLER_UPDATE_WIDGET = 5;
	
	static final int ENDPOINT  = 7879;
	
	Handler threadMsg = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case HANDLER_UPDATE_UI:
				buttonSent.setEnabled(true);
				break;
			case HANDLER_UPDATE_CONNECT:
				String str = (String)msg.obj;
				ipAddr.setText(str);
				break;
			case HANDLER_UPDATE_PROGRESSBAR:
				fileProgress.setMax(msg.arg1);
				break;
			case HANDLER_UPDATE_PROGRESSSTATE:
				fileProgress.setProgress(msg.arg1);
				break;
			case HANDLER_UPDATE_WIDGET:
				boolean v;
				if(msg.arg1==1) v = true;
				else v= false;
				buttonOpen.setEnabled(v);
				buttonPaint.setEnabled(v);
				buttonMouse.setEnabled(v);
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonOpen = (Button)findViewById(R.id.buttonOpen);
		buttonSent = (Button)findViewById(R.id.buttonSent);
		buttonPaint = (Button)findViewById(R.id.buttonPaint);
		buttonMouse = (Button)findViewById(R.id.buttonControlMouse);
		editText = (EditText)findViewById(R.id.editTextfile);
		fileProgress = (ProgressBar)findViewById(R.id.progressBarFile);
		ipAddr = (TextView)findViewById(R.id.textIPaddr);
		info = (TextView)findViewById(R.id.textInfo);
		
		Log.d("tiger", "MainActivity create");
		editText.setText("/storage/emulated/0");
		buttonOpen.setEnabled(false);
		buttonSent.setEnabled(false);
		buttonPaint.setEnabled(false);
		buttonMouse.setEnabled(false);
		
		wifimanager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		if(!wifimanager.isWifiEnabled()) {
			ipAddr.setText("WIFI未开启，请先打开WiFi");
			return;
		}
		
		WifiInfo wifiinfo = wifimanager.getConnectionInfo();
		//localIp = "192.168.1.9";
		localIp = intToIp(wifiinfo.getIpAddress());
		if(localIp==null || localIp.equals("127.0.0.1")) {
			ipAddr.setText("WIFI未连接");
		}
		else {
			ipAddr.setText("本机IP：" + localIp + " 服务器未连接");
		}

		new socketConnectServerThread(2).start();
		new socketConnectServerThread(87).start();
		new socketConnectServerThread(172).start();
		
		buttonOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, FilelistActivity.class);
				intent.putExtra("DIR_PATH", editText.getText().toString());
				MainActivity.this.startActivityForResult(intent, 1);
			}
		});
		
		buttonSent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub		
				buttonSent.setEnabled(false);
				fileProgress.setProgress(0);
				new fileTransferThread().start();
			}			
		});
		
		buttonPaint.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, PaintActivity.class);
				intent.putExtra("PAINT_MODE", 1);
				intent.putExtra("SERVER_IP", serverIp);
				MainActivity.this.startActivity(intent);
			}
		});

		buttonMouse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, PaintActivity.class);
				intent.putExtra("PAINT_MODE", 0);
				intent.putExtra("SERVER_IP", serverIp);
				MainActivity.this.startActivity(intent);
			}
		});
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		info.setText("x="+event.getX()+"y="+event.getY());
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		//super.onActivityResult(arg0, arg1, arg2);
		String result = arg2.getExtras().getString("result");
		editText.setText(result);
		buttonSent.setEnabled(true);
	}
	
	static public String intToIp(int i) {
		return ((i & 0xFF) + "." + ((i>>8) & 0xFF) + "." +
				((i>>16) & 0xFF) + "." + ((i>>24) & 0xFF));
	}
	
	static public String getIpHead(String ip) {
		byte[] bufSrc = ip.getBytes();
		int i=0, n=0;

		for(i=0; i<bufSrc.length; i++){
			if(n==3)
				break;
			if(bufSrc[i]=='.')
				n++;
		}
		
		byte[] bufDst = new byte[i];
		System.arraycopy(bufSrc, 0, bufDst, 0, i);
		return (new String(bufDst));
	}
	
	static public String getLocalHostIp() {
		String ipaddress = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			
			while(en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				//System.out.println("tiger " + nif.getName());
				while(inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					//System.out.println("tiger " + ip.getHostAddress());
					if(!ip.isLinkLocalAddress() && 
							InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
						return ipaddress = ip.getHostAddress();
					}
				}
			}
			
		} catch (SocketException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ipaddress;
	}
	
	static class filePackage {
		private byte[] buf = null;
		
		public filePackage(int token, String fileName, long nFileLength) {
			int i=0;
			buf = new byte[512];
			for(i=0; i<512; i++)
				buf[i] = 0;
			//copy to token
			buf[0] = (byte)(token & 0xff);
			buf[1] = (byte)(token >>8 & 0xff);
			buf[2] = (byte)(token >>16 & 0xff);
			buf[3] = (byte)(token >>24 & 0xff);
			//copy file length
			buf[4] = (byte)(nFileLength & 0xff);
			buf[5] = (byte)(nFileLength >>8 & 0xff);
			buf[6] = (byte)(nFileLength >>16 & 0xff);
			buf[7] = (byte)(nFileLength >>24 & 0xff);
			//copy file name
			System.arraycopy(fileName.getBytes(), 0, buf, 8, fileName.getBytes().length);
		}
		
		public byte[] getBytes() {
			return buf;
		}
	}
	
	class socketConnectServerThread extends Thread {
		private int startip = 0;
		private int endip = 0;
		public socketConnectServerThread(int n) {
			startip = n;
			if(startip<2) startip=2;
			endip = startip + 83;
			if(endip>254) endip=254;
		}
		
		public void run() {
			Socket socket = null;
			Message msg = null;
			String ipHead = getIpHead(localIp);

			for(int n=startip; n<=endip; n++){
				Log.d("tiger", ipHead + n);
				if(serverIp!=null)
					return;
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(ipHead + n, ENDPOINT), 100);
					socket.close();
					Log.d("tiger", "connected");
					
					serverIp = ipHead + n;
					msg = new Message();
					msg.what = HANDLER_UPDATE_CONNECT;
					msg.obj = "已连接服务器IP: " + socket.getInetAddress().getHostAddress();
					threadMsg.sendMessage(msg);
					
					msg = new Message();
					msg.what = HANDLER_UPDATE_WIDGET;
					msg.arg1 = 1;
					threadMsg.sendMessage(msg);
					
					break;
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					Log.d("tiger", "sent over1");
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("tiger", "connect failed");
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	
	class fileTransferThread extends Thread {

		public void run() {
			// TODO Auto-generated method stub
			InputStream fileInput, input = null;
			OutputStream output = null;
			DataInputStream inputData = null;
			Socket socket = null;
			Message msg = null;
			String ipHead = getIpHead(localIp);

			try {
				if(serverIp!=null) {
					socket = new Socket();
					socket.connect(new InetSocketAddress(serverIp, ENDPOINT), 100);
					Log.d("tiger", "start transfer");
				}
				else {
					Log.d("tiger", "server is not connected");
					return;
				}
				
				File file = new File(editText.getText().toString());
				fileInput = new FileInputStream(file);
				output = socket.getOutputStream();
				input = socket.getInputStream();
				inputData = new DataInputStream(input);
				
				//file name
				filePackage pack = new filePackage(NETWORK_TOKEN_FILE_TRANSFER, file.getName(), file.length());
				
				msg = new Message();
				msg.what = HANDLER_UPDATE_PROGRESSBAR;
				msg.arg1 = (int)file.length();
				threadMsg.sendMessage(msg);
				
				output.write(pack.getBytes());
				int flag = inputData.readInt();
				if(flag==0)
					Log.d("tiger", "receive good");
				
				byte buffer [] = new byte[512];
				Log.d("tiger", "start file sent");
				int cnt = 0, cntSent = 0;
				
				while((cnt = fileInput.read(buffer)) != -1) {
					output.write(buffer, 0 , cnt);
					cntSent += cnt;
					msg = new Message();
					msg.what = HANDLER_UPDATE_PROGRESSSTATE;
					msg.arg1 = cntSent;
					threadMsg.sendMessage(msg);
				}
				output.flush();
				
				msg = new Message();
				msg.what = HANDLER_UPDATE_UI;
				threadMsg.sendMessage(msg);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.d("tiger", "sent over1");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("tiger", "connect failed");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Log.d("tiger", "sent over3");
				try {
					if(input!=null)
						input.close();
					
					if(output!=null)
						output.close();
					
					if(socket!=null)
						socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}












