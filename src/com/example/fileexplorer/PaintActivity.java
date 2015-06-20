package com.example.fileexplorer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.example.fileexplorer.MainActivity.filePackage;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class PaintActivity extends ActionBarActivity {
	Button clearBtn;
	PaintView paintView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paint_view);

		clearBtn = (Button)findViewById(R.id.btn_clear);
		paintView = (PaintView)findViewById(R.id.View_paint);
		
		Intent intent=getIntent();
		int paintMode = intent.getIntExtra("PAINT_MODE", 0);
		String serverIp = intent.getStringExtra("SERVER_IP");
		System.out.println("liangyi" + paintMode);
		
		if(paintMode==1)
			paintView.startConnect(1, serverIp);
		else
			paintView.startConnect(0, serverIp);
		
		
		clearBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(paintView!=null)
					paintView.clear();
				else
					System.out.println("liangyi paintview is null");
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		paintView.closeConnect();
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
}