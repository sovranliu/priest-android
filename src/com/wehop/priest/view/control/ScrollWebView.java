package com.wehop.priest.view.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * 支持滚动的浏览器
 */
public class ScrollWebView extends WebView {
	public ScrollWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollWebView(Context context) {
		super(context);
	}

	@Override
    public boolean onTouchEvent(MotionEvent event){
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    } 
}
