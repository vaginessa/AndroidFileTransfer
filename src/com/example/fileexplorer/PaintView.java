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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View {

	private Resources myResources;
	
	private Paint myPaint;
	private Paint mBitmapPaint;
	
	private Path myPath;
	
	private Bitmap myBitmap;
	private Canvas myCanvas;
	
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;
	
	private int mWidth;
	private int mHeight;
	private int paintMode;
	
	private OutputStream output = null;
	private Socket socket = null;
	private String serverIp = null;
	byte[] buf;

	public PaintView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public PaintView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initialize();
	}
	
	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initialize();
	}
	
	public void startConnect(int mode, String ip) {
		paintMode = mode;
		System.out.println("liangyi view" + paintMode);
		serverIp = ip;
		new PaintViewClientThread().start();
	}
	
	public void closeConnect() {
		buf[3] = (byte)0xff;
		try {
			if(output!=null) {
				output.write(buf);
				output.flush();
				output.close();
			}
			if(socket!=null) {
				output.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initialize() {		
		myResources = getResources();
		
		myPaint = new Paint();
		myPaint.setAntiAlias(true);
		myPaint.setDither(true);
		myPaint.setColor(myResources.getColor(R.color.purple_dark));
		myPaint.setStyle(Paint.Style.STROKE);
		myPaint.setStrokeJoin(Paint.Join.ROUND);
		myPaint.setStrokeCap(Paint.Cap.ROUND);
		myPaint.setStrokeWidth(12);
		
		String paintMode = (String)getTag(R.id.paint_mode);
		System.out.println("liangyi view" + paintMode);
		
		myPath = new Path();
		buf = new byte[4];
		
		//new PaintViewClientThread().start();
	}
	
	class PaintViewClientThread extends Thread {
		
		public void run() {			
			try {
				if(serverIp!=null) {
					socket = new Socket();
					socket.connect(new InetSocketAddress(serverIp, MainActivity.ENDPOINT), 100);
					Log.d("tiger", "start transfer");
				}
				else {
					Log.d("tiger", "server is not connected");
					return;
				}				
				output = socket.getOutputStream();
				
				//file name
				MainActivity.filePackage pack;
				if(paintMode==1) {
					pack = new filePackage(MainActivity.NETWORK_TOKEN_PAINT_MODE, "NULL", 0);
				}
				else {
					pack = new filePackage(MainActivity.NETWORK_TOKEN_MOUSE_CONTROL, "NULL", 0);
				}
				output.write(pack.getBytes());
				output.flush();
				System.out.println("tiger sent done");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				System.out.println("tiger sent over1");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("tiger connect failed");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				System.out.println("tiger sent over3");
			}
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		myBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		myCanvas = new Canvas(myBitmap);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		
		//System.out.println("liangyi event"+ x + " " + y + " " + event.getAction());
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			buf[0] = (byte)((int)x & 0xff);
			buf[1] = (byte)((int)x >>8 & 0xff);
			buf[2] = (byte)((int)y & 0xff);
			buf[3] = (byte)((int)y >>8 & 0xff);
			buf[3] |= 0x80;

			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			touch_start(x, y);
			//System.out.println("liangyi down"+ x + " " + y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			buf[0] = (byte)((int)x & 0xff);
			buf[1] = (byte)((int)x >>8 & 0xff);
			buf[2] = (byte)((int)y & 0xff);
			buf[3] = (byte)((int)y >>8 & 0xff);
			buf[3] &= 0x7f;
			touch_move(x, y);
			//System.out.println("liangyi move"+ x + " " + y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			//System.out.println("liangyi up"+ x + " " + y);
			invalidate();
			break;			
		}
		try {
			if(output!=null) {
				output.write(buf);
				output.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return super.onTouchEvent(event);
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		if(paintMode==1) {			
			canvas.drawBitmap(myBitmap, 0, 0, mBitmapPaint);	
			canvas.drawPath(myPath, myPaint);
		}
	}
	
	public void clear() {
		myBitmap.eraseColor(myResources.getColor(R.color.write));
		myPath.reset();
		buf[3] = (byte)0xfe;
		try {
			if(output!=null) {
				output.write(buf);
				output.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		invalidate();
	}
	
	private void touch_start(float x, float y) {
		myPath.reset();
		myPath.moveTo(x, y);
		mX = x;
		mY = y;
	}
	
	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			myPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
			mX = x;
			mY = y;
		}
	}
	
	private void touch_up() {
		myPath.lineTo(mX, mY);
		myCanvas.drawPath(myPath, myPaint);
		
		myPath.reset();
	}
}