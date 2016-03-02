package com.wehop.priest.view.form;

import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;

import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 浏览器页
 */
@ResourceView(id = R.layout.activity_web)
public class WebActivity extends ActivityEx {
	/**
	 * 回退
	 */
	@ResourceView(id = R.id.web_image_return)
	public ImageView imgReturn;
	/**
	 * 标题
	 */
	@ResourceView(id = R.id.web_text_caption)
	public TextView labTitle;
	/**
	 * 关闭
	 */
	@ResourceView(id = R.id.web_label_close)
	public TextView labClose;
	/**
	 * 浏览器对象
	 */
	@ResourceView(id = R.id.web_browser)
	public WebView browser;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		browser = (WebView) this.findViewById(R.id.web_browser);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.requestFocus();
		browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		browser.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.startsWith("mailto:") || url.startsWith("geo:") ||url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	                startActivity(intent);
	                browser.pauseTimers();
	                return false;
	            }
				else if(url.startsWith("local://")) {
					Toast.makeText(WebActivity.this, "暂不支持", Toast.LENGTH_LONG).show();
	                browser.pauseTimers();
	                return false;
				}
				browser.loadUrl(url);
	            return true;
			}
		});
		browser.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if(100 == newProgress) {
					view.setVisibility(View.VISIBLE);
				}
			} 
            @Override  
            public void onReceivedTitle(WebView view, String title) {  
                super.onReceivedTitle(view, title);  
                TextView textView = (TextView) WebActivity.this.findViewById(R.id.web_text_caption);
				textView.setText(title);
            }
		});
		browser.setDownloadListener(new DownloadListener() {
			@Override  
	        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {  
	            Uri uri = Uri.parse(url);  
	            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
	            startActivity(intent);  
	        }
		});
		imgReturn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				browser.goBack();
			}
		});
		labClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebActivity.this.finish();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		String url = this.getIntent().getStringExtra("url");
		if(null == url) {
			return;
		}
		browser.loadUrl("about:blank");
		browser.loadUrl(url);
	}

	@Override
	public void onStop() {
		browser.loadUrl("about:blank");
		super.onStop();
	}
}
