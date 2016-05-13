package com.wehop.priest.view.form;

import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 纯文本页面
 */
@ResourceView(id = R.layout.activity_suggest)
public class SuggestActivity extends ActivityEx {
	@ResourceView(id = R.id.suggest_image_close)
	public ImageView imgClose;
	@ResourceView(id = R.id.suggest_label_commit)
	public TextView labSubmit;
	@ResourceView(id = R.id.suggest_text_title)
	public EditText labTitle;
	@ResourceView(id = R.id.suggest_text_content)
	public EditText labContent;

	/**
	 * 评价ID
	 */
	private String id;

	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = this.getIntent().getStringExtra("id");
		imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SuggestActivity.this.finish();
			}
		});
		labSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Networking.doCommand("Suggest", new JSONResponse(SuggestActivity.this) {
					@Override
					public void onFinished(JSONVisitor content) {
						if(null == content || content.getInteger("code", 0) <= 0) {
							return;
						}
						SuggestActivity.this.finish();
					}
				}, Me.instance.token, id, labTitle.getText(), labContent.getText());
			}
		});
	}
}
