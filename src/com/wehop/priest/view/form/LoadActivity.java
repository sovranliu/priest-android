package com.wehop.priest.view.form;

import java.io.File;
import java.io.IOException;

import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.Profile;
import com.wehop.priest.framework.Storage;

import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.etc.Controller;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.model.core.IEventable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * 引导界面
 */
@ResourceView(id = R.layout.activity_load)
public class LoadActivity extends ActivityEx {
	@ResourceView(id = R.id.load_image_ad)
	public ImageView imgAd;


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 延时加载
		Controller.doDelay(new Runnable() {
			@Override
			public void run() {
            	// 页面切换
				Me.autoLogin(LoadActivity.this, new IEventable<Boolean>() {
					@Override
					public void on(Boolean result) {
						startActivity(new Intent(LoadActivity.this, MainActivity.class));
		            	LoadActivity.this.finish();
					}
				});
			}
		}, 2000);
		// 加载启动广告
		showPoster();
		// 引导启动广告
		loadPoster();
	}

	/**
	 * 展示启动广告
	 */
	private void showPoster() {
		String poster = Profile.instance().poster;
		if(null != poster) {
			try {
				imgAd.setImageBitmap(Storage.getImage(poster));
			}
			catch(Exception ex) {}
		}
	}

	/**
	 * 引导启动广告
	 */
	private void loadPoster() {
		Host.doCommand("LoadingImage", new JSONResponse(LoadActivity.this) {
			@Override
			public void onFinished(JSONVisitor content) {
				if(null == content) {
					return;
				}
				if(content.getInteger("code") <= 0) {
					return;
				}
				String url = content.getString("data");
				if(null == url) {
					return;
				}
				String fileName = Host.parseFileNameWithURL(url);
				File file = new File(Storage.imagePath(fileName));
				if(file.exists()) {
					return;
				}
				Host.doImage("image", new ImageResponse(file, fileName) {
					@Override
					public void onFinished(Bitmap content) {
						try {
							Profile.instance().poster = (String) tag;
							Profile.instance().save();
						}
						catch (IOException e) { }
					}
		        }, url);
			}
		});
	}
}
