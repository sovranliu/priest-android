package com.wehop.priest.view.form;

import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.slfuture.pluto.etc.Version;
import com.slfuture.pluto.view.annotation.ResourceView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 关于界面
 */
@ResourceView(id = R.layout.activity_about)
public class AboutActivity extends Activity {
	@ResourceView(id = R.id.about_image_close)
	public ImageView imgClose;
	@ResourceView(id = R.id.about_label_version)
	public TextView labVersion;
	
	
	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 界面处理
		prepare();
	}

	/**
	 * 界面预处理
	 */
	public void prepare() {
		imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});
		labVersion.setText("当前版本：" + Version.fetchVersion(Program.application).toString());
	}
}
