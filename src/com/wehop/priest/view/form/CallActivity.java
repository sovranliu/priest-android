package com.wehop.priest.view.form;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.etc.Controller;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.im.view.form.AudioActivity;
import com.slfuture.pretty.im.view.form.VideoActivity;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.core.IMeListener;
import com.wehop.priest.business.user.User;
import com.wehop.priest.R;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 响铃界面
 */
@ResourceView(id=R.layout.activity_call)
public class CallActivity extends ActivityEx implements IMeListener {
	@ResourceView(id=R.id.call_image_photo)
	public ImageView imgPhoto;
	@ResourceView(id=R.id.call_label_name)
	public TextView labName;
	@ResourceView(id=R.id.call_label_description)
	public TextView labDescription;
	@ResourceView(id=R.id.call_image_handup)
	public ImageView imgHandup;
	@ResourceView(id=R.id.call_image_answer)
	public ImageView imgAnswer;


	/**
	 * 通话类型
	 * audio,video,visit
	 */
	private String dialType = null;
	/**
	 * 语音
	 */
	private SoundPool soundPool = null;
	/**
	 * 呼叫ID
	 */
	private int callId = 0;
	/**
	 * 呼叫者环信ID
	 */
	private String imUsername = null;
	

	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 界面处理
		prepare();
		//
		com.wehop.priest.business.Runtime.isCalling = true;
	}

	@Override
    protected void onDestroy() {
		super.onDestroy();
		//
		if(null != soundPool) {
			soundPool.release();
			soundPool = null;
		}
		com.wehop.priest.business.Runtime.isCalling = false;
    }

	@SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
    	super.onResume();
		try {
			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
			if(km.isKeyguardLocked()) {
				KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		        kl.disableKeyguard();
			}
	        // 获取电源管理器对象  
	        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        if(!pm.isScreenOn()) {
	            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
	            wl.acquire();
	            wl.release();
	        }
		}
		catch(Exception ex) { }
    }

	/**
	 * 界面预处理
	 */
	public void prepare() {
		prepareData();
		prepareHandUp();
	}

	/**
	 * 处理数据
	 */
	@SuppressWarnings("deprecation")
	public void prepareData() {
		callId = this.getIntent().getIntExtra("callId", 0);
		imUsername = this.getIntent().getStringExtra("imUsername");
		dialType = this.getIntent().getStringExtra("dialType");
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		final int soundId = soundPool.load(this, R.raw.ring, 1);
		Controller.doDelay(new Runnable() {
			@Override
			public void run() {
				soundPool.play(soundId, 1, 1, 0, -1, 1);
			}
		}, 1000);
		labName.setText(this.getIntent().getStringExtra("name") + " [" + this.getIntent().getStringExtra("phone") + "]");
		if("audio".equals(dialType)) {
			labDescription.setText("音频通话" + " (" + this.getIntent().getStringExtra("netstate") + ")");
		}
		else if("video".equals(dialType)) {
			labDescription.setText("视频通话" + " (" + this.getIntent().getStringExtra("netstate") + ")");
		}
		else if("visit".equals(dialType)) {
			labDescription.setText("呼叫上门" + " (" + this.getIntent().getStringExtra("netstate") + ")");
		}
		User user = new User();
		user.imId = imUsername;
		user.name = this.getIntent().getStringExtra("name");
		Me.instance.contacts.put(user.imId, user);
	}

	/**
	 * 处理挂断按钮
	 */
	public void prepareHandUp() {
		imgHandup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CallActivity.this.finish();
			}
		});
		imgAnswer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Networking.doCommand("Accept", new JSONResponse(CallActivity.this) {
					@Override
					public void onFinished(JSONVisitor content) {
						if(null == content) {
							Toast.makeText(CallActivity.this, "网络错误", Toast.LENGTH_LONG).show();
							CallActivity.this.finish();
							return;
						}
						if(content.getInteger("code", 0) == 1) {
							labDescription.setText("正在接通");
							Intent intent = null;
							if("audio".equals(dialType)) {
								intent = new Intent(CallActivity.this, AudioActivity.class);
							}
							else if("video".equals(dialType)) {
								intent = new Intent(CallActivity.this, VideoActivity.class);
							}
							else if("visit".equals(dialType)) {
								intent = new Intent(CallActivity.this, AudioActivity.class);
							}
							else {
								intent = new Intent(CallActivity.this, AudioActivity.class);
							}
							intent.putExtra("from", imUsername);
							intent.putExtra("isCaller", true);
							CallActivity.this.startActivity(intent);
							CallActivity.this.finish();
							return;
						}
						else if(content.getInteger("code", 0) == -1) {
							Toast.makeText(CallActivity.this, "呼叫已被其他人接通", Toast.LENGTH_LONG).show();
							labDescription.setText("呼叫已被其他人接通");
							CallActivity.this.finish();
							return;
						}
						else if(content.getInteger("code", 0) == -2) {
							Toast.makeText(CallActivity.this, "呼叫已挂断", Toast.LENGTH_LONG).show();
							labDescription.setText("呼叫已挂断");
							CallActivity.this.finish();
							return;
						}
						return;
					}
				}, Me.instance.token, callId);
			}
		});
	}

	@Override
	public void onConflict() {
		
	}

	@Override
	public void onCommand(String from, String action, Table<String, Object> data) {
		if("handupMessage".equals(action)) {
			Toast.makeText(CallActivity.this, "对方已经挂断", Toast.LENGTH_LONG).show();
			labDescription.setText("对方已经挂断");
			this.finish();
		}
	}
}
