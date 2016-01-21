package com.wehop.priest.view.form;

import java.io.File;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.etc.Controller;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pluto.etc.MulitPointTouchListener;
import com.slfuture.pluto.etc.ParameterRunnable;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.R;

/**
 * 图片查看界面
 */
@ResourceView(id=R.layout.activity_image)
public class ImageActivity extends ActivityEx {
	@ResourceView(id=R.id.image_image_image)
	public ImageView image;
	/**
	 * 对话框
	 */
	private AlertDialog alertDialog = null;


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		String path = this.getIntent().getStringExtra("path");
		String url = this.getIntent().getStringExtra("url");
		if(null != path) {
			Bitmap bitmap = GraphicsHelper.decodeFile(new File(path));
			if(null != bitmap) {
				showImage(bitmap);
			}
		}
		if(null != url) {
			showWaiting();
			Host.doImage("", new ImageResponse(url) {
				@Override
				public void onFinished(Bitmap content) {
					if(null != alertDialog) {
						alertDialog.cancel();
					}
					if(null == content) {
						Toast.makeText(ImageActivity.this, "下载失败", Toast.LENGTH_LONG).show();
						return;
					}
					showImage(content);
				}
			}, url);
		}
	}
	
	/**
	 * 展示图片
	 * 
	 * @param bitmap 图片内容
	 */
	private void showImage(Bitmap bitmap) {
		image.setOnTouchListener(null);
		image.setScaleType(ScaleType.CENTER_INSIDE);
		image.setImageBitmap((Bitmap) bitmap);
		Controller.doDelay(new ParameterRunnable(bitmap) {
			@Override
			public void run() {
				image.setOnTouchListener(new MulitPointTouchListener ());
				image.setScaleType(ScaleType.MATRIX);
			}
		}, 100);
	}

	/**
	 * 展示等待窗口
	 */
	private void showWaiting() {
		if(null == alertDialog) {
			alertDialog = new AlertDialog.Builder(ImageActivity.this).create();
		}
		alertDialog.show();
		Window window = alertDialog.getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setGravity(Gravity.CENTER);
		window.setAttributes(layoutParams);
		window.setContentView(R.layout.dialog_waiting);
		ViewGroup background = (ViewGroup) window.findViewById(R.id.waiting_layout_background);
		background.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		ImageView icon = (ImageView) window.findViewById(R.id.waiting_image_icon);
		final RotateAnimation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
        animation.setDuration(1000);
        animation.setRepeatCount(Animation.INFINITE);
        icon.startAnimation(animation);
	}
}
