package com.wehop.priest.view.form;

import java.io.File;
import java.io.FileOutputStream;

import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.general.view.form.ImageActivity;
import com.slfuture.pretty.qcode.Module;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 首页
 */
@ResourceView(id = R.layout.activity_userinfo)
public class UserInfoActivity extends ActivityEx {
	@ResourceView(id = R.id.userinfo_image_close)
	public ImageView imgClose;
	@ResourceView(id = R.id.userinfo_label_logout)
	public TextView labLogout;
	@ResourceView(id = R.id.userinfo_image_photo)
	public ImageView imgPhoto;
	@ResourceView(id = R.id.userinfo_layout_phone)
	public View viewPhone;
	@ResourceView(id = R.id.userinfo_text_photo)
	public TextView labPhone;
	@ResourceView(id = R.id.userinfo_layout_nickname)
	public View viewNickname;
	@ResourceView(id = R.id.userinfo_text_nickname)
	public TextView labNickname;
	@ResourceView(id = R.id.userinfo_layout_title)
	public View viewTitle;
	@ResourceView(id = R.id.userinfo_label_title)
	public TextView labTitle;
	@ResourceView(id = R.id.userinfo_image_qcode)
	public ImageView imgQCode;


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prepare();
	}

	@Override
	public void onStart() {
		super.onStart();
		prepare();
	}

	/**
	 * 界面预处理
	 */
	public void prepare() {
		imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserInfoActivity.this.finish();
			}
		});
		labLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Me.instance.logout();
				UserInfoActivity.this.finish();
			}
		});
		final ImageView imgPhoto = (ImageView) this.findViewById(R.id.userinfo_image_photo);
		if(Text.isBlank(Me.instance.photo)) {
			imgPhoto.setImageBitmap(GraphicsHelper.makeImageRing(GraphicsHelper.makeCycleImage(BitmapFactory.decodeResource(this.getResources(), R.drawable.user_photo_default), 200, 200), Color.WHITE, 4));
		}
		else {
            Host.doImage("image", new ImageResponse(Me.instance.photo) {
				@Override
				public void onFinished(Bitmap content) {
					if(null == content) {
						return;
					}
					imgPhoto.setImageBitmap(GraphicsHelper.makeImageRing(GraphicsHelper.makeCycleImage(content, 200, 200), Color.WHITE, 4));
				}
            }, Me.instance.photo);
		}
		labPhone.setText(Me.instance.phone);
		labNickname.setText(Me.instance.nickname);
		labTitle.setText(Me.instance.title);
		imgQCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = com.wehop.priest.framework.Storage.IMAGE_ROOT + "qcode." + Me.instance.phone + ".png";
				if(!(new File(path)).exists()) {
					Bitmap bitmap = Module.createQRImage("add://" + Me.instance.phone, 500, 500);
					if(null == bitmap) {
						return;
					}
					FileOutputStream stream = null;
					try {
						stream = new FileOutputStream(new File(path));
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						stream.flush();
					}
					catch(Exception ex) {
						Log.e("TOWER", "保存二维码失败", ex);
					}
					finally {
						if(null != stream) {
							try {
								stream.close();
							}
							catch(Exception ex) { }
						}
						stream = null;
					}
				}
				Intent intent = new Intent(UserInfoActivity.this, ImageActivity.class);
				intent.putExtra("path", path);
				UserInfoActivity.this.startActivity(intent);
			}
		});
	}
}
