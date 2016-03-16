package com.wehop.priest.view.form;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.user.User;

/**
 * 添加朋友
 */
@ResourceView(id = R.layout.activity_addfriend)
public class AddFriendActivity extends OnlyUserActivity {
	@ResourceView(id = R.id.addfriend_button_close)
	public ImageButton btnClose;
	@ResourceView(id = R.id.addfriend_label_confirm)
	public TextView labConfirm;
	@ResourceView(id = R.id.addfriend_text_phone)
	public EditText txtPhone;
	@ResourceView(id = R.id.addfriend_text_relation)
	public EditText txtRelation;

	/**
	 * 带编辑的成员ID
	 */
	private String userId = "";

	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		userId = this.getIntent().getStringExtra("userId");
		if(Text.isBlank(userId)) {
			userId = "";
			if(null != this.getIntent().getStringExtra("phone")) {
				txtPhone.setText(this.getIntent().getStringExtra("phone"));
				txtRelation.requestFocus();
			}
		}
		else {
			User user = Me.instance.fetchUserById(userId);
			if(null == user) {
				AddFriendActivity.this.finish();
				return;
			}
			txtPhone.setText("●●●●●●●●●●●");
			txtPhone.setEnabled(false);
			if(null != user.relation) {
				txtRelation.setText(user.relation);
			}
		}
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddFriendActivity.this.finish();
			}
		});
		labConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int mode = 1;
				if(Text.isBlank(userId)) {
					mode = 0;
					Pattern pattern = Pattern.compile("^[1][358][0-9]{9}$");
					Matcher matcher = pattern.matcher(txtPhone.getText().toString());
					if(!matcher.matches()) {
						Toast.makeText(AddFriendActivity.this, "手机号码格式不正确", Toast.LENGTH_LONG).show();
						return;
					}
				}
				Host.doCommand("add", new JSONResponse(AddFriendActivity.this) {
					@Override
					public void onFinished(JSONVisitor content) {
						if(null != content && content.getInteger("code") > 0) {
							AddFriendActivity.this.finish();
						}
					}
				}, Me.instance.token, userId, mode, txtRelation.getText().toString(), txtPhone.getText().toString());
			}
		});
	}
}
