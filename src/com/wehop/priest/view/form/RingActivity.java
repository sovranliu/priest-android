package com.wehop.priest.view.form;

import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EMNoActiveCallException;
import com.wehop.priest.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	}
	
	/**
	 * 处理呼入者名称
	 */
	public void prepareCaller() {
		TextView text = (TextView) this.findViewById(R.id.ring_label_caller);
		String title = userName + "的";
		if("audio".equals(type)) {
			title += "音频邀请";
		}
		else {
			title += "视频邀请";
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
				try {
					EMChatManager.getInstance().rejectCall();
				}
				catch (EMNoActiveCallException e) {
					Log.e("TOWER", "prepareHandUp execute failed", e);
				}
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
				Intent intent;
		        if("audio".equals(type)) {
		            intent = new Intent(RingActivity.this, VoiceActivity.class);
		        }
		        else {
		            intent = new Intent(RingActivity.this, VideoActivity.class);
		        }

		        intent.putExtra("userId", userId);
		        intent.putExtra("userName", userName);
		        intent.putExtra("mode", false);
           		RingActivity.this.startActivity(intent);
				RingActivity.this.finish();
			}
		});
	}
}
