package com.wordpress.mobilecoder.xbmc;

public interface XbmcCallbacks {
	public enum PlayStatus{Playing, Paused, Unknown};
	
	/**
	 * Play/pause has completed
	 * @param playStatus The current status
	 */
	public void onPlayPause(PlayStatus playStatus);
	
	/**
	 * Volume up has completed
	 * @param volume The current volume
	 */
	public void onVolumeUp(int volume);
	
	/**
	 * Volume down has completed
	 * @param volume The current volume
	 */
	public void onVolumeDown(int volume);
}
