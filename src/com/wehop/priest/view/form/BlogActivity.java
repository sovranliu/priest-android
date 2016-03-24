package com.wehop.priest.view.form;

import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.storage.Storage;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.slfuture.pretty.general.view.form.BrowserActivity;
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
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.startsWith("mailto:") || url.startsWith("geo:") ||url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	                startActivity(intent);
	                browser.pauseTimers();
	                browser.resumeTimers();
	                return false;
	            }
				else if(url.startsWith("new://")) {
					Intent intent = new Intent(BlogActivity.this.getActivity(), BrowserActivity.class);
					intent.putExtra("url", url.substring("new://".length()));
					Bundle bundle = BlogActivity.this.getActivity().getIntent().getBundleExtra("handler");
					if(null != bundle) {
						intent.putExtra("handler", bundle);
					}
					startActivity(intent);
	                browser.pauseTimers();
	                browser.resumeTimers();
	                return false;
				}
	            return super.shouldOverrideUrlLoading(view, url);
			}
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				browser.loadUrl("about:blank");
			}
			@Override 
	        public void onPageFinished(WebView view, String url) { 
				super.onPageFinished(view, url);
				view.loadUrl("javascript: var allLinks = document.getElementsByTagName('a'); if (allLinks) {var i;for (i=0; i<allLinks.length; i++) {var link = allLinks[i];var target = link.getAttribute('target'); if (target && target == '_blank') {link.setAttribute('target','_self');link.href = 'new://'+link.href;}}}"); 
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
    			browser.loadUrl(Networking.fetchURL("BlogPage", Me.instance.token));
    		}
    		else {
    			browser.loadUrl(Networking.fetchURL("BlogPage", ""));
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
