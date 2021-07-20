package com.CityLive;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CityLive extends Activity implements Runnable{
    /** Called when the activity is first created. */
	public static final String TAG = "PlaylistService";
	private static String URL_CITYLIVE = "http://somestreaming/live";
	 /** Called when the activity is first created. */
	private AudioManager _am;
	private int _volumeLevel = 0;
	private Button _streamButton;
	private SeekBar _volumeBar;
	private boolean _isPlaying =false;
	private ConnectivityManager connectivityManager;
	private TelephonyManager tm;
	private MediaPlayer _mediaPlayer;
	ProgressDialog _dialog;
	private boolean _setPlayerControlsResultComplete = true;
	/**Menu Id**/
	private static int EXIT_MENU =0;
	private static int CONTACT_MENU = 1;
	private static int LIVE_EVENT = 2;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        InitControl();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem){
		switch (menuitem.getItemId()) {
		case 0:
			stopStreamingAudio();
			this.finish();
			break;
		case 1:
			startActivity(new Intent(CityLive.this,CityLiveAbout.class));
			break;
		case 2:
			break;
		default:
			break;
		}
		return true;
	}  
	
    private void InitControl() {
		//The button of sync to the server
		_streamButton = (Button) findViewById(R.id.PlayButton);
		
		//The seekbar to adjust volume
		_volumeBar = (SeekBar) findViewById(R.id.volume);
		
		//Get wifi manager
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        //Check internet connectivity 
        if(connectivityManager.getActiveNetworkInfo()!=null && connectivityManager.getActiveNetworkInfo().isConnected())
        {
        	SetLoadingThread();
            startStreamingAudio();
        }else
        {
        	Toast.makeText(this, "No internet connection", 500).show();
        }
        
        //Set stream button
        _streamButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(_mediaPlayer!=null){
					if (_mediaPlayer.isPlaying()) {
						stopStreamingAudio();
					} else {
						SetLoadingThread();
						startStreamingAudio();
					}
				}else{
					SetLoadingThread();
					startStreamingAudio();
				}
				_isPlaying = !_isPlaying;
        }});
        
        //Set volume bar
      //The seekbar to adjust volume
    	_am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		_volumeBar = (SeekBar) findViewById(R.id.volume);
		_volumeBar.setMax(_am.getStreamMaxVolume(3));
		_volumeLevel = _volumeBar.getMax() /2;
		_volumeBar.setProgress(_volumeLevel);
		_volumeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar arg0, int arg1,
					boolean arg2) {
				// TODO Auto-generated method stub
				int index = _volumeBar.getProgress(); 
	            _am.setStreamVolume(3, index, 1); 
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}});
        
    	//Calling manager
    	tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    	//tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    	tm.listen(new PhoneStateListener(){
    		 @Override
             public void onCallStateChanged(int state, String incomingNumber)
             {
    			 if(state == TelephonyManager.CALL_STATE_RINGING){
    				 stopStreamingAudio();
    			 }else if(state==TelephonyManager.CALL_STATE_OFFHOOK){
    				 stopStreamingAudio();
    			 }else if(state == TelephonyManager.CALL_STATE_IDLE){
    				 if(_isPlaying){
    					 SetLoadingThread();
    					 startStreamingAudio();
						}
    			 }else{
    				 Log.d(TAG, "Unknown phone state=" + state);
    			 }
             }
    	}, PhoneStateListener.LISTEN_CALL_STATE);
    	
	}
	private void SetLoadingThread() {
		_dialog = ProgressDialog.show(CityLive.this, "", 
		        "Loading. Please wait...", true);
		
		 Thread thread = new Thread(this);
		 thread.start();
	}
	
	private boolean SetPlayerControls() {
		try {
			_mediaPlayer.prepare();
			//_mediaPlayer.seekTo(0);
			return true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			_dialog.dismiss();
			e.printStackTrace();
			return false;
		}
	}
	private void SetPlayerDataSource() {
		//Set data source
		try {
			_mediaPlayer = new MediaPlayer();
			_mediaPlayer.setDataSource(URL_CITYLIVE);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	private void startStreamingAudio() {
		if(connectivityManager.getActiveNetworkInfo()!=null && connectivityManager.getActiveNetworkInfo().isConnected()){		
			Toast.makeText(this, "Connect via "+connectivityManager.getActiveNetworkInfo().getTypeName()+" network", 1000).show();	
		}else{
			Toast.makeText(this, "No internet connection", 500).show();
		}	    	
    }
	private void stopStreamingAudio(){
		try{
			if ( _mediaPlayer != null) {
				
				_mediaPlayer.pause();//Pause
				_streamButton.setBackgroundResource(R.drawable.play_circle);//Change button picture
				Toast.makeText(this, "Disconnecting...", 500).show();//Notify to user
    		}
		}catch (Exception e) {
			// TODO: handle exception
			Log.e(getClass().getName(), "Error stopping to stream audio.", e);
			Toast.makeText(this, "Error stopping to stream audio.", 1000).show();
		}
	} 
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_BACK 
                && event.getRepeatCount() == 0) { 
        	stopStreamingAudio();
        	this.finish();
            return true; 
        } 
        return super.onKeyDown(keyCode, event); 
    } 

    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_BACK) { 
           // *** DO ACTION HERE *** 
           return true; 
        } 
        return super.onKeyUp(keyCode, event); 
    }

	public void run() {
		SetPlayerDataSource();			//Check data source
		_setPlayerControlsResultComplete = SetPlayerControls(); //Set player control
		_mediaPlayer.start(); 			//Start
		 handler.sendEmptyMessage(0);
	}
	
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
               _dialog.dismiss();
               	if(!_setPlayerControlsResultComplete)
               		Toast.makeText(CityLive.this, "Station closed/Station network unavailable", 1000).show();
       		//Change button picture
       			if(_setPlayerControlsResultComplete)
       				_streamButton.setBackgroundResource(R.drawable.pause_circle);	
               //Reset control setting result
       			_setPlayerControlsResultComplete = true;
        }
};
}