package com.wordpress.mobilecoder.xbmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.wordpress.mobilecoder.xbmc.XbmcCallbacks.PlayStatus;

import android.os.AsyncTask;
import android.util.Log;

public class XbmcRemote {

	private static final String TAG = "XbmcRemote";
	
	private XbmcCallbacks mListener;
	private String mUserName;
	private String mPassword;
	private String mHost;
	private int mPort;
	private boolean mUseSSL;

	private static enum RequestType {Ping, PausePlay, VolumeUp, VolumeDown};
	
	public XbmcRemote(String user, String password, String host, String port,
			boolean useSSL) {
		mUserName = user;
		mPassword = password;
		mHost = host;
		mPort = Integer.parseInt(port);
		mUseSSL = useSSL;
	}
	
	/**
	 * Sets a listener for xbmc callbacks
	 * @param listener
	 */
	public void setListener(XbmcCallbacks listener){
		mListener = listener;
	}

	/**
	 * This does not handle multiple requests in the same execute
	 */
	private class NetworkRequest extends AsyncTask<RequestType, Void, Void>{
		@Override
		protected Void doInBackground(RequestType... params) {
			boolean success = false;
			switch (params[0]){
				case Ping:
					break;
				
				case PausePlay:
					mListener.onPlayPause(doPlayPause());
					break;
					
				case VolumeUp:
					mListener.onVolumeUp(doVolumeUp());
					break;
					
				case VolumeDown:
					mListener.onVolumeDown(doVolumeDown());
					break;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
		}
	}
	
	private PlayStatus doPlayPause(){
		JSONObject json = doPost("{\"jsonrpc\":\"2.0\",\"method\":\"Player.PlayPause\", \"params\": {\"playerid\":1}, \"id\":1}");
				
		if(json != null){
			try{
				JSONObject resultArray = json.getJSONObject("result");
				int speed = resultArray.getInt("speed");
				Log.i(TAG, "GOt speed: " + speed);
				if(speed == 0)
					return PlayStatus.Paused;
				else
					return PlayStatus.Playing;
			} catch (JSONException ex){
				Log.i(TAG, "Failure parsing JSON");
				return PlayStatus.Unknown;
			}
		}
		
		return PlayStatus.Unknown;
	}
	
	private int doVolumeUp(){
		//TODO
		return 0;
	}
	
	private int doVolumeDown(){
		//TODO
		return 0;
	}
	
	private JSONObject doPost(final String JSON){

		HttpClient httpClient = new DefaultHttpClient();
		
		try {
			HttpPost request = null;
			if (mUseSSL)
				request = new HttpPost("https://" + mHost + ":" + mPort + "/jsonrpc");
			else
				request = new HttpPost("http://" + mHost + ":" + mPort + "/jsonrpc");

			//Add authentication header
			StringEntity params = new StringEntity( JSON );
			request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(mUserName, mPassword), "UTF-8", false));

			//Set parameters and start the request
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);

			//Parse result into JSON
			if(response != null && response.getStatusLine().getStatusCode() == 200){
				
				JSONObject json = null;
				try {
					//Read result
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					String result = reader.readLine();
					
					//Convert to JSON
					JSONTokener tokener = new JSONTokener(result);
					json = new JSONObject(tokener);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					return null;
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
					return null;
				} catch (IOException e1) {
					e1.printStackTrace();
					return null;
				} catch (JSONException ex){
					ex.printStackTrace();
					return null;
				}
				
				return json;
			}
			
			return null;
		} catch (Exception ex) {
			Log.e(TAG, "Recevied exception during http post: " + ex);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}
	
	public void volumeUp(){
		
	}
	
	public void volumeDown(){
		
	}
	
	public void playPause(){
		new NetworkRequest().execute(RequestType.PausePlay);
	}
	
	
}
