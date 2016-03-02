package com.wehop.priest.view.form;

import com.wehop.priest.R;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.Me;

import com.slfuture.carrie.base.model.core.IEventable;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.general.utility.GeneralHelper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 登录页
 */
@ResourceView(id = R.layout.activity_login)
public class LoginActivity extends ActivityEx {
    @ResourceView(id = R.id.login_text_username)
    public EditText txtUsername;
    @ResourceView(id = R.id.login_text_password)
    public EditText txtPassword;
    @ResourceView(id = R.id.login_button_login)
    public Button btnLogin;
    
	/**
	 * 等待对话框
	 */
	private AlertDialog dialog = null;

    /**
     * 界面创建
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i("LoginActivity.onCreate() execute");
        super.onCreate(savedInstanceState);
        //
        prepare();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 界面启动预处理
     */
    public void prepare() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				final String phone = txtUsername.getText().toString();
				if(Text.isBlank(phone)) {
					return;
				}
				final String code = txtPassword.toString();
				if(Text.isBlank(code)) {
					return;
				}
				dialog = GeneralHelper.showWaiting(LoginActivity.this);
				Me.login(LoginActivity.this, phone, code, new IEventable<Boolean>() {
					@Override
					public void on(Boolean result) {
						if(null != dialog) {
							dialog.cancel();
						}
						dialog = null;
						if(!result) {
							return;
						}
						Intent intent = (Intent) LoginActivity.this.getIntent().getParcelableExtra("intent");
						if(null == intent) {
							intent = new Intent();
							intent.putExtra("RESULT", "SUCCESS");
							LoginActivity.this.setResult(1, intent);
						}
						else {
							LoginActivity.this.startActivity(intent);
						}
						LoginActivity.this.finish();
					}
				});
            }
        });
    }
}
