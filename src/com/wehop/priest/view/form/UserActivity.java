package com.wehop.priest.view.form;

import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;
import com.wehop.priest.business.Logic;
import com.wehop.priest.framework.Storage;
import com.wehop.priest.framework.Utility;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 首页
 */
@ResourceView(id = R.layout.activity_user)
public class UserActivity extends FragmentEx {
	/**
	 * 关于我们URL
	 */
	public final static String URL_ABOUT = "http://cdn.oss.wehop-resources.wehop.cn/sales/app/sites/v-1/about_us.html";
	/**
	 * 热线电话
	 */
	public final static String TELEPHONE_US = "13816202676";


	@ResourceView(id = R.id.user_image_photo)
	public ImageView imgPhoto;
	@ResourceView(id = R.id.user_label_name)
	public TextView labName;
	@ResourceView(id = R.id.user_layout_about)
	public View layAbout;
	@ResourceView(id = R.id.user_layout_telephone)
	public View layTelephone;
	@ResourceView(id = R.id.user_button_exit)
	public Button btnExit;


	@Override
	public void onStart() {
		super.onStart();
		prepare();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/**
	 * 界面预处理
	 */
	public void prepare() {
		if(null != Logic.user) {
			if(null != Logic.user.photo) {
				Host.doImage("image", new ImageResponse(Logic.user.photo, null) {
					@Override
					public void onFinished(Bitmap content) {
						if(null == content) {
							return;
						}
						imgPhoto.setImageBitmap(Utility.makeImageRing(Utility.makeCycleImage(content, 200, 200), 4));
					}
				}, Logic.user.photo);
			}
			labName.setText(Logic.user.name);
		}
		layAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserActivity.this.getActivity(), WebActivity.class);
				intent.putExtra("url", URL_ABOUT);
				startActivity(intent);
			}
		});
		layTelephone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + TELEPHONE_US));
	            startActivity(intent);
			}
		});
		btnExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Logic.user = null;
				Storage.clear();
				// 切入登录页
				UserActivity.this.getActivity().startActivity(new Intent(UserActivity.this.getActivity(), LoginActivity.class));
				UserActivity.this.getActivity().finish();
			}
		});
	}
}
