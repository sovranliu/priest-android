package com.wehop.priest.view.form;

import java.io.File;

import com.wehop.priest.R;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.Logic;
import com.wehop.priest.framework.DynamicConfig;
import com.wehop.priest.framework.Storage;
import com.slfuture.pluto.etc.Control;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * 引导页
 */
@ResourceView(id = R.layout.activity_load)
public class LoadActivity extends ActivityEx {
	@ResourceView(id = R.id.load_image_poster)
	public ImageView imagePoster;
	@ResourceView(id = R.id.load_image_logo)
	public ImageView imageLogo;

	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.i("LoadActivity.onCreate() execute");
		super.onCreate(savedInstanceState);
		//
		prepare();
		load();
	}

	/**
	 * 界面启动预处理
	 */
	public void prepare() {
		// 启动动画
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.gradually);
		imagePoster.startAnimation(animation);
	}

	/**
	 * 加载数据
	 */
	public void load() {
		File loadImage = DynamicConfig.fetchFile("load.png");
		if(null != loadImage) {
			imageLogo.setImageBitmap(Storage.getImage(loadImage));
		}
		Control.doDelay(new Runnable() {
			@Override
			public void run() {
		        if(null == Logic.user) {
		        	startActivity(new Intent(LoadActivity.this, LoginActivity.class));
		        }
		        else {
					startActivity(new Intent(LoadActivity.this, MainActivity.class));
		        }
            	LoadActivity.this.finish();
			}
		}, 2000);
	}
}
