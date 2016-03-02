package com.wehop.priest.view.form;

import com.qcast.tower.R;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * 意见页面
 */
@ResourceView(id = R.layout.activity_config)
public class ConfigActivity extends ActivityEx {
	@ResourceView(id = R.id.config_button_close)
	public ImageButton btnClose;
	@ResourceView(id = R.id.config_layout_clear)
	public View viewClear;
	@ResourceView(id = R.id.config_layout_password)
	public View viewPassword;
	@ResourceView(id = R.id.config_layout_about)
	public View viewAbout;


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ConfigActivity.this.finish();
			}
		});
		viewClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ConfigActivity.this, "缓存清理完毕", Toast.LENGTH_LONG).show();
			}
		});
		viewPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ConfigActivity.this, PasswordActivity.class);
				intent.putExtra("mode", PasswordActivity.MODE_MODIFY);
				ConfigActivity.this.startActivity(intent);
			}
		});
		viewAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ConfigActivity.this, AboutActivity.class);
				ConfigActivity.this.startActivity(intent);
			}
		});
	}
}
