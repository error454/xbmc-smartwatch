/*
 Copyright (c) 2011, Sony Ericsson Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
 of its contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.wordpress.mobilecoder.xbmcSmartExtension;

import java.util.Timer;
import java.util.TimerTask;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorManager;
import com.sonyericsson.extras.liveware.sdk.R;
import com.wordpress.mobilecoder.layout.SmartWatchLinearLayout;
import com.wordpress.mobilecoder.xbmc.XbmcCallbacks;
import com.wordpress.mobilecoder.xbmc.XbmcRemote;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * The sample sensor control handles the accelerometer sensor on an accessory.
 * This class exists in one instance for every supported host application that
 * we have registered to
 */
public class SampleSensorControl extends ControlExtension implements SmartWatchCallback{

	private static final String TAG = "SensorControl";

	public static final int WIDTH = 128;
    public static final int HEIGHT = 128;
    
    private Context mContext;
    private SmartWatchLinearLayout mSmartView;
    private LinearLayout mLayout;
    private SampleSensorControl mThis;
    
    private XbmcRemote mXbmc;
    
    private Timer mScreenDimTimer;
    private static final long TIMER_DELAY = 10000;
    
    private Button mPausePlay;
    
    /**
     * Sets the screen state to on and starts a timer to dim the screen after
     * TIMER_DELAY milliseconds
     */
    private void startScreenDimTimer(){
    	setScreenState(Control.Intents.SCREEN_STATE_ON);
    	mScreenDimTimer = new Timer();
        mScreenDimTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				setScreenState(Control.Intents.SCREEN_STATE_DIM);
			}
		}, TIMER_DELAY);
    }
    
    /**
     * Configure click handlers and add them to the smartview
     */
    private void setupButtons(){
//    	Button up = (Button)mLayout.findViewById(R.id.imageViewUp);
//        up.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Log.i("TEST", "up button clicked");
//				mXbmc.volumeUp();
//			}
//		});
//        
//        Button down = (Button)mLayout.findViewById(R.id.imageViewDown);
//        down.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Log.i("TEST", "left button clicked");
//				mXbmc.volumeDown();
//			}
//		});
        
		mPausePlay = (Button)mSmartView.findViewById(R.id.imageViewPausePlay);
		mPausePlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mXbmc.playPause();
			}
		});
        
//        mSmartView.addViewToWatch(up);
//        mSmartView.addViewToWatch(down);
        mSmartView.addViewToWatch(mPausePlay);
    }
    
    /**
     * Create sample sensor control.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     */
    SampleSensorControl(final String hostAppPackageName, final Context context) {
        super(context, hostAppPackageName);
        mThis = this;
        mContext = context;
//        AccessorySensorManager manager = new AccessorySensorManager(context, hostAppPackageName);
        
        mSmartView = new SmartWatchLinearLayout(mContext, WIDTH, HEIGHT);
        mLayout = (LinearLayout)LinearLayout.inflate(context, R.layout.pauseplay, mSmartView);
        mSmartView.setParameters(mThis, mLayout);
        
        setupButtons();
        startScreenDimTimer();
        
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String user = pref.getString(context.getString(R.string.preference_key_user), "");
        String pass = pref.getString(context.getString(R.string.preference_key_password), "");
        String host = pref.getString(context.getString(R.string.preference_key_host), "");
        String port = pref.getString(context.getString(R.string.preference_key_port), "");
        boolean ssl = pref.getBoolean(context.getString(R.string.preference_key_ssl), false);
        
        //Create a new instance of the xbmc remote
        mXbmc = new XbmcRemote(user, pass, host, port, ssl);
        
        //Create a listener for callbacks
        XbmcCallbacks listener = new XbmcCallbacks() {
			
			@Override
			public void onVolumeUp(int volume) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onVolumeDown(int volume) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPlayPause(PlayStatus playStatus) {
				if(playStatus.equals(PlayStatus.Playing))
					mPausePlay.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pausebig));
				else if(playStatus.equals(PlayStatus.Paused))
					mPausePlay.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.playbig));
				
				mSmartView.requestDraw();
			}
		};
        
		mXbmc.setListener(listener);
    }

	@Override
    public void onResume() {
        Log.d(SampleExtensionService.LOG_TAG, "Starting control");
        setScreenState(Control.Intents.SCREEN_STATE_ON);
    }

	@Override
	public void onTouch(ControlTouchEvent event) {
		super.onTouch(event);
		
		//Reset the screen dim timer
		if(mScreenDimTimer != null){
			mScreenDimTimer.cancel();
			mScreenDimTimer = null;
			startScreenDimTimer();
		}
		
		//Forward this touch event on to the smartview
		mSmartView.dispatchControlTouchEvent(event);
	}
    
	@Override
	public void updateSmartWatchScreen(Bitmap b) {
		showBitmap(b);
	}
}
