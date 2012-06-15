package com.wordpress.mobilecoder.xbmcSmartExtension;

import android.graphics.Bitmap;

/**
 * An interface to be a little more certain that the user has implemented things as planned 
 */
public interface SmartWatchCallback {
	public void updateSmartWatchScreen(Bitmap b);
}
