package com.wehop.priest.view.form;

import android.content.Intent;
import android.os.Bundle;

import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.business.Me;

/**
 * 只有用户可以访问的界面
 */
public class OnlyUserActivity extends ActivityEx {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(null != Me.instance) {
			return;
		}
		finish();
		Intent intent = new Intent(OnlyUserActivity.this, LoginActivity.class);
		intent.putExtra("intent", this.getIntent());
		this.startActivityForResult(intent, 1234);
	}
}
