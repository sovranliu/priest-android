package com.wehop.priest.view.form;

import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.storage.Storage;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 圈子界面
 */
@ResourceView(id = R.layout.activity_blog)
public class BlogActivity extends FragmentEx {
	@ResourceView(id = R.id.blog_browser)
	public WebView browser;
	/**
	 * 上传消息
	 */
	public ValueCallback<Uri> uploadMessage;

	/**
	 * 当前用户手机号码
	 */
	private String phone = "";


    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		browser.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
        });
		browser.setWebChromeClient(new WebChromeClient() {
			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
				this.openFileChooser(uploadMsg);
			}
			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
				this.openFileChooser(uploadMsg);
			}
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				uploadMessage = uploadMsg;
				Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
				chooserIntent.setType("image/*");
				startActivityForResult(chooserIntent, 0);
			}
		});
    }

	/**
	 * 界面创建
	 */
	@Override
    public void onResume() {
    	super.onResume();
    	boolean sentry = false;
    	if(null == phone) {
    		if(null == Me.instance) {
    			sentry = true;
    		}
    	}
    	else {
    		if(null != Me.instance) {
        		sentry = phone.equals(Me.instance.phone);
    		}
    	}
    	if(!sentry) {
    		if(null != Me.instance) {
    			browser.loadUrl(Host.fetchURL("BlogPage", Me.instance.token));
    		}
    		else {
    			browser.loadUrl("about:blank");
    		}
    		if(null == Me.instance) {
        		phone = null;
    		}
    		else {
        		phone = Me.instance.phone;
    		}
    	}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (0 == requestCode) {
			if(null == uploadMessage){
				return;
			}
			Uri uri = intent == null || resultCode != -1 ? null : intent.getData();
			if(null == uri) {
				uploadMessage.onReceiveValue(null);
				uploadMessage = null;
				return;
			}
			String path = Storage.getPathFromURI(BlogActivity.this.getActivity(), uri);
			uploadMessage.onReceiveValue(Uri.parse("file://" + path));
			uploadMessage = null;
		}
	}
}
