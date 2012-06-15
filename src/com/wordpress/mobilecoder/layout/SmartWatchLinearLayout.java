package com.wordpress.mobilecoder.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.sdk.R;
import com.wordpress.mobilecoder.xbmcSmartExtension.SampleSensorControl;
import com.wordpress.mobilecoder.xbmcSmartExtension.SmartWatchCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SmartWatchLinearLayout extends LinearLayout{

	private static final String TAG = "SmartWatchLinearLayout";
	
	//For monitoring views
	private List<View> mViewsToMonitor;
	private View mCurrentView;
	private long mLastActionDownTime;
	
	//For handling FPS draw rate limiting
	private final int mMaxFPS = 24;
	private Timer mDrawTimer;
	private boolean mNeedDraw;
	
	//For drawing
	private int mWidth;
    private int mHeight;
	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private SampleSensorControl mControlExtension;
    private LinearLayout mLayout;
	
    /**
     * Construct a new layout with the given width and height
     * @param context Context
     * @param width Max width, most likely 128
     * @param height Max height, most likely 128
     */
	public SmartWatchLinearLayout(Context context, int width, int height) {
		super(context); 
		mViewsToMonitor = new ArrayList<View>();
		
		mWidth = width;
        mHeight = height;
        setLayoutParams(new LayoutParams(mWidth, mHeight));
	}
	
	/**
	 * Set the extra parameters required for drawing
	 * @param control The sensor control object for drawing to the screen
	 * @param layout The layout you've inflated
	 */
	public void setParameters(SampleSensorControl control, LinearLayout layout){
		mControlExtension = control;
		mLayout = layout;

		//Finish the layout on the inflated resource
        mLayout.measure(mWidth, mWidth);
        mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
        
        //Configure the bitmap and canvas for drawing
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, BITMAP_CONFIG);
        mBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        mCanvas = new Canvas(mBitmap);
        
        //Start the gated draw timer
        mDrawTimer  = new Timer();
        mDrawTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				if(mNeedDraw){
					mNeedDraw = false;
					updateSmartWatchScreen();
				}
				
			}
		}, 1000, (long)(1000 / mMaxFPS));
        
        mNeedDraw = true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		//Prevent events from getting consumed by anything other than us
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.i(TAG, "onTouchEvent: action: " + event.getAction() + " down time: " + event.getDownTime() + " event time: " + event.getEventTime());
		
		//Loop through the views that we're monitoring
		for(int child = 0; child < mViewsToMonitor.size(); child++){
			if( mViewsToMonitor.get(child) != null){
				View v = mViewsToMonitor.get(child);
				Rect bounds = new Rect();
			    v.getHitRect(bounds);
			    
			    //See if the tap event collided with the view
			    if (bounds.contains((int)event.getX(), (int)event.getY()) ){
			    	switch(event.getAction()){
				    	case MotionEvent.ACTION_DOWN:
				    		v.setPressed(true);
				    		mCurrentView = v;
				    		break;
				    		
				    	case MotionEvent.ACTION_UP:
				    		//Only click the button on release if it's the same button we tapped
				    		if(mCurrentView.equals(v)){
				    			v.setPressed(false);
					    		v.performClick();
				    		}
				    		else{
				    			mCurrentView.setPressed(false);
				    		}
				    		mCurrentView = null;
				    		break;
			    	}
		    	}
			}
		}
		
		//Don't allow any further propagation of this touch event
		return true;
	}
	
	/**
	 * Adds a view to the list of views to monitor for touch events
	 * @param view The view to add
	 */
	public void addViewToWatch(View view){
		mViewsToMonitor.add(view);
	}
	
	/**
	 * Removes a view from the list of views to monitor
	 * @param view The view to remove
	 */
	public void removeViewFromWatch(View view){
		while(mViewsToMonitor.contains(view)){
			mViewsToMonitor.remove(view);
		}
	}
	
	/**
	 * Converts the smartwatch ControlTouchEvent into an Android MotionEvent
	 * @param event The ControlTouchEvent generated in onTouch
	 */
	public void dispatchControlTouchEvent(ControlTouchEvent event){
		//Determine the action and convert to MotionEvent equivalent
    	int action = 0;
    	switch (event.getAction()){
	    	case Control.Intents.TOUCH_ACTION_PRESS:
	    		mLastActionDownTime = SystemClock.uptimeMillis();
	    		action = MotionEvent.ACTION_DOWN;
	    		break;
	    	case Control.Intents.TOUCH_ACTION_LONGPRESS:
	    		action = MotionEvent.ACTION_MOVE;
	    		break;
	    	case Control.Intents.TOUCH_ACTION_RELEASE:
	    		action = MotionEvent.ACTION_UP;
	    		break;
    	}
    	
    	//Construct a MotionEvent and dispatch it to the layout
    	MotionEvent m = MotionEvent.obtain(mLastActionDownTime, SystemClock.uptimeMillis(), action, (float)event.getX(), (float)event.getY(), 0 );
    	dispatchTouchEvent(m);
    	mNeedDraw = true;
	}
	
	/**
	 * Sends the current UI to the smartwatch
	 */
	public void updateSmartWatchScreen(){
		//Clear the canvas and then draw on it
		mCanvas.drawColor(Color.BLACK);
		mLayout.draw(mCanvas);
        
        //Signal the control extension to draw the bitmap
        ((SmartWatchCallback)mControlExtension).updateSmartWatchScreen(mBitmap);
	}
	
	/**
	 * Request the layout to draw the screen
	 */
	public void requestDraw(){
		mNeedDraw = true;
	}
}
