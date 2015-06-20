package com.scnuly.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class FileUtils {
	private String SDPATH;
	static final String APPDIR = "/FileTransfer";
	
	public String getSDPath() {
		return SDPATH;
	}
	
	public FileUtils() {
		SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
		File dir = new File(SDPATH + APPDIR);
		dir.mkdir();
	}
	
	public String isSDExist() {
		String state = Environment.getExternalStorageState();
		System.out.println(state);
		return state;
	}
	
	public File createSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		file.createNewFile();
		return file;
	}
	
	public File createSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}
	
	public boolean deleteFile(String name) {
		File file = new File(SDPATH + name);
		return file.delete();
	}
	
	public boolean copyFile(String srcFile, String dstFile) {
		boolean ret = false;
		InputStream input = null;
		OutputStream output = null;
		File filesrc = null;
		File filedst = null;
		
		try {
			int count = 0;
			filesrc = new File(SDPATH + srcFile);
			filedst = new File(SDPATH + dstFile);
			input = new FileInputStream(filesrc);
			output = new FileOutputStream(filedst);
			byte buffer[] = new byte[1024 * 4];
			while((count = input.read(buffer)) != -1) {
				output.write(buffer, 0, count);
			}
			output.flush();
			input.close();
			output.close();
			ret = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				input.close();
				output.close();	
				ret = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ret = false;
			}
		}
		
		return ret;
	}
	
	public boolean isFileExist(String fileName) {
		File file = new File(SDPATH + fileName);
		return file.exists();
	}
	
	public void listSDDir() {
		File dir = new File(SDPATH);
		if(dir.listFiles()!=null)
		{
			File[] files = dir.listFiles();
		
			System.out.println("tiger " + files.length);
			
			for(int i=0; i < files.length; i++) {
				System.out.println("tiger "+ files[i].getAbsolutePath());
			}
		}
	}
	
	public File inputToFile(String fileName, InputStream input) {
		File file = null;
		OutputStream output = null;
		
		try{
			int cnt = 0;
			file = createSDFile(APPDIR + fileName);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];
			System.out.println("tiger inputtofile");
			while((cnt = input.read(buffer)) != -1)
				output.write(buffer, 0, cnt);
			output.flush();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	public OutputStream fileToOuput(String fileName) {
		File file = new File(SDPATH + fileName);
		
		try {
			return (new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}