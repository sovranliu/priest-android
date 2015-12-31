package com.wehop.priest.view.form;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EMServiceNotReadyException;
import com.wehop.priest.R;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 语音通话界面
 */
public class VoiceActivity extends Activity {
	/**
	 * 句柄
	 */
	private Handler handler = null;
	/**
	 * 环信用户名
	 */
	private String userImName = null;
	/**
	 * 用户名
	 */
	private String userName = null;
	/**
	 * 状态，true：主动拨号，false：被动接听
	 */
	private boolean mode = false;
	/**
	 * 静音状态
	 */
	private boolean muteStatus = false;
	/**
	 * 免提状态
	 */
	private boolean speakerStatus = false;


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("TOWER", "VoiceActivity.onCreate() execute");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_voice);
		// 界面处理
		prepare();
	}
	
	/**
	 * 界面预处理
	 */
	public void prepare() {
		prepareData();
		prepareMute();
		prepareSpeaker();
		prepareHandUp();
		prepareCaller();
	}
	
	/**
	 * 处理呼入者名称
	 */
	public void prepareCaller() {
		TextView text = (TextView) this.findViewById(R.id.voice_label_caller);
		text.setText(userName);
	}
	
	/**
	 * 处理数据
	 */
	public void prepareData() {
		handler = new Handler();
		//
		userImName = this.getIntent().getStringExtra("userId");
		userName = this.getIntent().getStringExtra("userName");
		Log.i("gxl", "voice call: user IM name = " + userImName);
		mode = this.getIntent().getBooleanExtra("mode", false);
		//
		EMChatManager.getInstance().addVoiceCallStateChangeListener(new EMCallStateChangeListener() {
		    @Override
		    public void onCallStateChanged(CallState callState, CallError error) {
		    	handler.post(new com.slfuture.pluto.etc.ParameterRunnable(callState) {
		            @Override
		            public void run() {
		            	TextView text = (TextView) VoiceActivity.this.findViewById(R.id.voice_text_title);
				    	switch ((CallState) parameter) {
				        case CONNECTING:
				        	text.setText(userName + "连接中");
				            break;
				        case CONNECTED:
				        	text.setText(userName + "响铃中");
				            break;
				        case ACCEPTED:
				        	text.setText(userName + "通话中");
				            break;
				        case DISCONNNECTED:
				            Log.e("gxl", "DISCONNNECTED");
				        	text.setText(userName + "已断开");
				        	VoiceActivity.this.finish();
				            break;
				        default:
				            break;
				        }
		            }
		        });
		    }
		});
		//
		if(mode) {
			try {
				EMChatManager.getInstance().makeVoiceCall(userImName);
			}
			catch (EMServiceNotReadyException e) {
				Log.e("gxl", "prepareData failed", e);
			}
		}
	}

	/**
	 * 处理挂断按钮
	 */
	public void prepareHandUp() {
		final Button button = (Button) this.findViewById(R.id.voice_button_handup);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				button.setEnabled(false);
				try {
					EMChatManager.getInstance().endCall();
				}
				catch (Exception e) { }
				VoiceActivity.this.finish();
			}
		});
	}
	
	/**
	 * 处理静音按钮
	 */
	public void prepareMute() {
		final ImageButton button = (ImageButton) this.findViewById(R.id.voice_button_mute);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AudioManager audioManager = (AudioManager) VoiceActivity.this.getSystemService(Context.AUDIO_SERVICE);
				if (muteStatus) {
					// 关闭静音
					button.setImageResource(R.drawable.icon_mute_normal);
					audioManager.setMicrophoneMute(false);
					muteStatus = false;
				}
				else {
					// 打开静音
					button.setImageResource(R.drawable.icon_mute_on);
					audioManager.setMicrophoneMute(true);
					muteStatus = true;
				}
			}
		});
	}
	
	/**
	 * 处理免提按钮
	 */
	public void prepareSpeaker() {
		final ImageButton button = (ImageButton) this.findViewById(R.id.voice_button_speaker);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AudioManager audioManager = (AudioManager) VoiceActivity.this.getSystemService(Context.AUDIO_SERVICE);
				if (speakerStatus) {
					// 关闭免提
					button.setImageResource(R.drawable.icon_speaker_normal);
					speakerStatus = false;
					//
					if(audioManager.isSpeakerphoneOn()) {
	                    audioManager.setSpeakerphoneOn(false);
					}
	                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
				}
				else {
					button.setImageResource(R.drawable.icon_speaker_on);
					speakerStatus = true;
					//
					if(!audioManager.isSpeakerphoneOn()) {
		                audioManager.setSpeakerphoneOn(true);
					}
		            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
				}
			}
		});
	}
}
