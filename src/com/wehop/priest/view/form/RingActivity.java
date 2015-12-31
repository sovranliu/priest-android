package com.wehop.priest.view.form;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EMNoActiveCallException;
import com.wehop.priest.R;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * 响铃界面
 */
public class RingActivity extends Activity {
	/**
	 * 通信方式：audio，video
	 */
	private String type = null;
	/**
	 * 环信用户名
	 */
	private String userId = null;
	/**
	 * 环信用户名
	 */
	private String userName = null;
	/**
	 * 状态，是否需要拒绝
	 */
	private boolean status = true;
	/**
	 * 句柄
	 */
	private Handler handler = null;
	/**
	 * 语音
	 */
	private SoundPool soundPool;


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("TOWER", "RingActivity.onCreate() execute");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_ring);
		// 界面处理
		prepare();
	}
	
	@Override
    protected void onDestroy() {
		super.onDestroy();
		//
		soundPool.release();
		if(!status) {
			return;
		}
		try {
			EMChatManager.getInstance().rejectCall();
		}
		catch (EMNoActiveCallException e) {
			Log.e("TOWER", "rejectCall execute failed", e);
		}
    }
	
	/**
	 * 界面预处理
	 */
	public void prepare() {
		prepareData();
		prepareCaller();
		prepareHandUp();
		prepareAnswer();
	}
	
	/**
	 * 处理数据
	 */
	public void prepareData() {
		type = this.getIntent().getStringExtra("type");
		userName = this.getIntent().getStringExtra("from");
		userId = this.getIntent().getStringExtra("userId");
		userName = this.getIntent().getStringExtra("userName");
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		final int soundId = soundPool.load(this, R.raw.ring, 1);
		handler = (new Handler());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				soundPool.play(soundId, 1, 1, 0, -1, 1);
			}
		}, 20);
	}

	/**
	 * 处理呼入者名称
	 */
	public void prepareCaller() {
		TextView text = (TextView) this.findViewById(R.id.ring_label_caller);
		String title = userName + "的";
		if("audio".equals(type)) {
			title += "音频邀请";
			//
			EMChatManager.getInstance().addVoiceCallStateChangeListener(new EMCallStateChangeListener() {
			    @Override
			    public void onCallStateChanged(CallState callState, CallError error) {
			    	handler.post(new com.slfuture.pluto.etc.ParameterRunnable(callState) {
			            @Override
			            public void run() {
			            	switch ((CallState) parameter) {
					        case DISCONNNECTED:
					        	status = false;
					        	RingActivity.this.finish();
					            break;
					        }
			            }
			        });
			    }
			});
		}
		else {
			title += "视频邀请";
			//
			EMChatManager.getInstance().addVoiceCallStateChangeListener(new EMCallStateChangeListener() {
			    @Override
			    public void onCallStateChanged(CallState callState, CallError error) {
			    	handler.post(new com.slfuture.pluto.etc.ParameterRunnable(callState) {
			            @Override
			            public void run() {
			            	switch ((CallState) parameter) {
					        case DISCONNNECTED:
					        	status = false;
					        	RingActivity.this.finish();
					            break;
					        default:
					            break;
					        }
			            }
			        });
			    }
			});
		}
		text.setText(title);
	}

	/**
	 * 处理挂断按钮
	 */
	public void prepareHandUp() {
		Button button = (Button) this.findViewById(R.id.ring_button_handup);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RingActivity.this.finish();
			}
		});
	}

	/**
	 * 处理接听按钮
	 */
	public void prepareAnswer() {
		Button button = (Button) this.findViewById(R.id.ring_button_answer);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					EMChatManager.getInstance().answerCall();
				}
				catch (EMNoActiveCallException e) {
					Log.e("TOWER", "prepareAnswer execute failed", e);
				}
				catch (EMNetworkUnconnectedException e) {
					Log.e("TOWER", "prepareAnswer execute failed", e);
				}
				status = false;
				if("audio".equals(type)) {
					Intent voiceIntent = new Intent(RingActivity.this, VoiceActivity.class);
					voiceIntent.putExtra("userId", userId);
					voiceIntent.putExtra("userName", userName);
					voiceIntent.putExtra("mode", false);
	           		RingActivity.this.startActivity(voiceIntent);
				}
				else {
					Intent videoIntent = new Intent(RingActivity.this, VideoActivity.class);
					videoIntent.putExtra("userId", userId);
					videoIntent.putExtra("userName", userName);
					videoIntent.putExtra("mode", false);
	           		RingActivity.this.startActivity(videoIntent);
				}
				RingActivity.this.finish();
			}
		});
	}
}
