package com.wehop.priest.view.form;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMVideoCallHelper;
import com.easemob.chat.EMVideoCallHelper.EMVideoOrientation;
import com.easemob.exceptions.EMServiceNotReadyException;
import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.wehop.priest.utils.CameraHelper;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 视频通话界面
 */
public class VideoActivity extends Activity {
    /**
     * 本地SurfaceHolder callback
     * 
     */
    class LocalCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            cameraHelper.startCapture();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }
    
    /**
     * 对方SurfaceHolder callback
     */
    class OppositeCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        	EMVideoCallHelper.getInstance().onWindowResize(width, height, format);
   
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

    }
    
    /**
     * 
     */
    private CameraHelper cameraHelper = null;
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
		Log.i("TOWER", "VideoActivity.onCreate() execute");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_video);
		// 界面处理
		prepare();
        //
        Program.register(this);
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        EMVideoCallHelper.getInstance().setSurfaceView(null);
    	if(null != cameraHelper) {
			cameraHelper.stopCapture();
    	}
		cameraHelper = null;
        //
        Program.unregister(this);
    }

	/**
	 * 界面预处理
	 */
	public void prepare() {
		prepareData();
		prepareMute();
		prepareSpeaker();
		prepareHandUp();
		prepareVideo();
		if(mode) {
			call();
		}
	}
	
	/**
	 * 处理数据
	 */
	public void prepareData() {
		handler = new Handler();
		//
		userImName = this.getIntent().getStringExtra("userId");
        userName = this.getIntent().getStringExtra("userName");
        Log.i("gxl", "video call: user IM name = " + userImName);
		mode = this.getIntent().getBooleanExtra("mode", false);
		//
		EMChatManager.getInstance().addVoiceCallStateChangeListener(new EMCallStateChangeListener() {
		    @Override
		    public void onCallStateChanged(CallState callState, CallError error) {
		    	handler.post(new com.slfuture.pluto.etc.ParameterRunnable(callState) {
		            @Override
		            public void run() {
		            	TextView text = (TextView) VideoActivity.this.findViewById(R.id.video_text_title);
				    	switch ((CallState) parameter) {
				        case CONNECTING:
				        	text.setText(userName + "连接中");
				            break;
				        case CONNECTED:
				        	text.setText(userName + "响铃中");
				            break;
				        case ACCEPTED:
				        	text.setText(userName + "通话中");
				        	SurfaceView oppositeSurface = (SurfaceView) findViewById(R.id.video_surface_opposite);
				        	
				            SurfaceHolder oppositeSurfaceHolder = oppositeSurface.getHolder();
				            // oppositeSurfaceHolder.setFixedSize(oppositeSurface.getWidth(), oppositeSurface.getHeight());
				            View viewBackground = VideoActivity.this.findViewById(R.id.video_layout_background);
				            View viewHead = VideoActivity.this.findViewById(R.id.video_layout_head);
				            // oppositeSurfaceHolder.setFixedSize(viewBackground.getWidth(), viewBackground.getHeight() - viewHead.getHeight());
				            // oppositeSurfaceHolder.setFixedSize(500, 500);
//				            ViewGroup.LayoutParams lp = oppositeSurface.getLayoutParams();
//				            lp.width = viewBackground.getWidth();
//				            lp.height = viewBackground.getHeight() - viewHead.getHeight();
//				            oppositeSurface.setLayoutParams(lp);
				            break;
				        case DISCONNNECTED:
				        	text.setText(userName + "已断开");
				        	VideoActivity.this.finish();
				            break;
				        default:
				            break;
				        }
		            }
		        });
		    }
		});
	}
	
	/**
	 * 开始呼叫
	 */
	public void call() {
		try {
			EMChatManager.getInstance().makeVideoCall(userImName);
		}
		catch (EMServiceNotReadyException e) {
			Log.e("TOWER", "call failed", e);
		}
	}

	public void prepareVideo() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        // 显示本地图像的surfaceview
		SurfaceView localSurface = (SurfaceView) findViewById(R.id.video_surface_local);
        localSurface.setZOrderMediaOverlay(true);
        localSurface.setZOrderOnTop(true);
        localSurface.getHolder().addCallback(new LocalCallback());
        SurfaceHolder localSurfaceHolder = localSurface.getHolder();
        // 显示对方图像的surfaceview
        SurfaceView oppositeSurface = (SurfaceView) findViewById(R.id.video_surface_opposite);
        SurfaceHolder oppositeSurfaceHolder = oppositeSurface.getHolder();
        // 设置显示对方图像的surfaceview
        EMVideoCallHelper.getInstance().setSurfaceView(oppositeSurface);
        oppositeSurfaceHolder.addCallback(new OppositeCallback());
        EMVideoCallHelper.getInstance().setVideoOrientation(EMVideoOrientation.EMPortrait);
        cameraHelper = new CameraHelper(EMVideoCallHelper.getInstance(), localSurface.getHolder());
        cameraHelper.setStartFlag(true);
	}

	/**
	 * 处理挂断按钮
	 */
	public void prepareHandUp() {
		final Button button = (Button) this.findViewById(R.id.video_button_handup);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				button.setEnabled(false);
				try {
					EMChatManager.getInstance().endCall();
				}
				catch (Exception e) { }
				VideoActivity.this.finish();
			}
		});
	}
	
	/**
	 * 处理静音按钮
	 */
	public void prepareMute() {
		final ImageButton button = (ImageButton) this.findViewById(R.id.video_button_mute);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AudioManager audioManager = (AudioManager) VideoActivity.this.getSystemService(Context.AUDIO_SERVICE);
				if (muteStatus) {
					// 关闭静音
					button.setImageResource(R.drawable.icon_mute_normal);
					button.getBackground().setAlpha(0);
					audioManager.setMicrophoneMute(false);
					muteStatus = false;
				}
				else {
					// 打开静音
					button.setImageResource(R.drawable.icon_mute_on);
					button.getBackground().setAlpha(0);
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
		final ImageButton button = (ImageButton) this.findViewById(R.id.video_button_speaker);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AudioManager audioManager = (AudioManager) VideoActivity.this.getSystemService(Context.AUDIO_SERVICE);
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
