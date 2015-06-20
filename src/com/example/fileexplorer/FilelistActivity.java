package com.example.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.scnuly.utils.FileUtils;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FilelistActivity extends ActionBarActivity {

	private ListView fileListView = null;
	private File fileList[] = null;
	SimpleAdapter mSimpleAdaptor = null;
	ArrayList<HashMap<String, Object>> listItem = null;
	
	private boolean updateListData(String dirPath) {
		
		if(dirPath!=null)
			System.out.println("tiger " + dirPath);
		else
			return false;
		
		File dir = new File(dirPath);
		
		if(dir.isFile())
			return true;
		
		fileList = dir.listFiles();
		
		listItem.removeAll(listItem);
		
		for(int i=0; i<fileList.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if(fileList[i].isFile()) {
				map.put("ItemImage", R.drawable.file2);
			}
			else if(fileList[i].isDirectory()) {
				map.put("ItemImage", R.drawable.folder2);
			}
			map.put("ItemTitle", fileList[i].getName());
			map.put("ItemText", fileList[i].getAbsolutePath());
			System.out.println("tiger " + fileList[i].getAbsolutePath());
			listItem.add(map);
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filelist);
		
		Intent intent=getIntent();
		String dirPath = intent.getStringExtra("DIR_PATH");
		
		
		if(dirPath!=null)
			System.out.println("tiger " + dirPath);
		else
			return;
		
		File dir = new File(dirPath);
		
		if(dir.isFile())
			return;
		
		fileListView = (ListView)findViewById(R.id.FileListView);
		
		listItem = new ArrayList<HashMap<String, Object>>();
		
		fileList = dir.listFiles();
		
		for(int i=0; i<fileList.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if(fileList[i].isFile()) {
				map.put("ItemImage", R.drawable.file2);
			}
			else if(fileList[i].isDirectory()) {
				map.put("ItemImage", R.drawable.folder2);
			}
			map.put("ItemTitle", fileList[i].getName());
			map.put("ItemText", fileList[i].getAbsolutePath());
			listItem.add(map);
		}
		
		mSimpleAdaptor = new SimpleAdapter(this, listItem, R.layout.file_list,
				new String[]{"ItemImage", "ItemTitle", "ItemText"},
				new int[]{R.id.ItemFileImage, R.id.ItemFileTitle, R.id.ItemFileText});
		
		fileListView.setAdapter(mSimpleAdaptor);
		
		fileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//setTitle("你点击了第"+arg2+"行");
				boolean quit = updateListData(fileList[arg2].getAbsolutePath());
				
				if(quit==true){
					Intent intent = new Intent();
					intent.putExtra("result", fileList[arg2].getAbsolutePath());
					FilelistActivity.this.setResult(RESULT_OK, intent);
					FilelistActivity.this.finish();
				}
				else {
					mSimpleAdaptor.notifyDataSetChanged();
				}				
			}
						
		});
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
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		Intent intent = new Intent();
		intent.putExtra("result", "/storage/emulated/0");
		FilelistActivity.this.setResult(RESULT_OK, intent);
		FilelistActivity.this.finish();
	}
}
