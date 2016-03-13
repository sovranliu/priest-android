package com.wehop.priest.view.form;

import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

/**
 * 圈子界面
 */
@ResourceView(id = R.layout.activity_blog)
public class BlogActivity extends FragmentEx {
	@ResourceView(id = R.id.blog_browser)
	public WebView browser;

	/**
	 * 当前用户手机号码
	 */
	private String phone = "";


    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
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
    		if(null == Me.instance) {
        		phone = null;
    		}
    		else {
        		phone = Me.instance.phone;
    		}
    	}
	}
}
