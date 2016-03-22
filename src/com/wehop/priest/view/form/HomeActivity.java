package com.wehop.priest.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.core.IMeListener;
import com.wehop.priest.framework.Storage;
import com.wehop.priest.view.control.ScrollWebView;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.pluto.etc.Controller;
import com.slfuture.pluto.etc.Version;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.slfuture.pretty.general.view.form.BrowserActivity;
import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.text.Text;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleAdapter.ViewBinder;

/**
 * 首页
 */
@ResourceView(id = R.layout.activity_home)
public class HomeActivity extends FragmentEx implements IMeListener {
	/**
	 * 当前资讯列表
	 */
	protected ArrayList<HashMap<String, Object>> newsList = new ArrayList<HashMap<String, Object>>();
    
	/**
	 * 顶部浏览器
	 */
	@ResourceView(id = R.id.home_browser)
	public ScrollWebView browser;
	/**
	 * 铃铛
	 */
	@ResourceView(id = R.id.home_button_notify)
	public ImageView btnBell;
	/**
	 * 搜索按钮
	 */
	@ResourceView(id = R.id.home_button_search)
	public Button btnSearch;
	/**
	 * 我的患者
	 */
	@ResourceView(id = R.id.home_image_left)
	public ImageView imgLeft;
	/**
	 * 我的日程
	 */
	@ResourceView(id = R.id.home_image_right)
	public ImageView imgRight;
	/**
	 * 新闻列表
	 */
	@ResourceView(id = R.id.home_list_news)
	public ListView listNews;
	/**
	 * 当前页面索引
	 */
	protected int page = 1;
	/**
	 * 当前用户手机号码
	 */
	private String phone = "";
	/**
	 * 动画
	 */
	private AnimationListener listener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			Log.d("tower", "onAnimationStart()");
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationEnd(Animation animation) {
			if(1 == shakeDirection) {
				Log.d("tower", "onAnimationEnd(Right)");
				btnBell.startAnimation(animLeft);
				shakeDirection = -1;
			}
			else if(-1 == shakeDirection) {
				Log.d("tower", "onAnimationEnd(Left)");
				btnBell.startAnimation(animRight);
				shakeDirection = 1;
			}
			else {
				btnBell.clearAnimation();
			}
		}
    };
	protected int shakeDirection = 1;
	protected RotateAnimation animRight = null; 
	protected RotateAnimation animLeft = null; 


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prepare();
		dealNews();
		dealSearch();
		//
		loadVersion();
		loadNews();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(com.wehop.priest.business.Runtime.hasUnreadMessage) {
			Controller.doDelay(new Runnable() {
				@Override
				public void run() {
					shakeBell();
				}
			}, 1000);
		}
		else {
			stopBell();
		}
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
    			browser.loadUrl(Networking.fetchURL("HomePage", Me.instance.token));
    		}
    		else {
    			browser.loadUrl(Networking.fetchURL("HomePage", ""));
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
	public void onPause() {
		super.onPause();
		stopBell();
	}

	/**
	 * 加载版本准备升级
	 */
	private void loadVersion() {
		Networking.doCommand("readVersion", new CommonResponse<String>() {
			@Override
			public void onFinished(String content) {
				if(Response.CODE_SUCCESS != code()) {
					return;
				}
				JSONObject resultObject = JSONObject.convert(content);
				if(((JSONNumber) resultObject.get("code")).intValue() <= 0) {
					return;
				}
				JSONObject result = (JSONObject) resultObject.get("data");
				String version = ((JSONString) (result.get("appVersion"))).getValue();
				String url = ((JSONString) (result.get("downloadUrl"))).getValue();
				Version current = Version.fetchVersion(Program.application);
				Version server = Version.build(version);
				if(null == server) {
					return;
				}
				if(current.compareTo(server) >= 0) {
					 return;
				}
				Intent intent = new Intent(HomeActivity.this.getActivity(), BrowserActivity.class);
				intent.putExtra("url", url);
				HomeActivity.this.startActivity(intent);
			}
		});
	}

	/**
	 * 加载资讯列表
	 */
	private void loadNews() {
		Networking.doCommand("news", new CommonResponse<String>(page) {
			@Override
			public void onFinished(String content) {
				if(Response.CODE_SUCCESS != code()) {
					Toast.makeText(HomeActivity.this.getActivity(), "加载资讯失败", Toast.LENGTH_LONG).show();
					return;
				}
				JSONObject resultObject = JSONObject.convert(content);
				if(((JSONNumber) resultObject.get("code")).intValue() <= 0) {
					Toast.makeText(HomeActivity.this.getActivity(), ((JSONString) resultObject.get("msg")).getValue(), Toast.LENGTH_LONG).show();
					return;
				}
				JSONArray result = (JSONArray) resultObject.get("data");
				int thisPage = (Integer) this.tag;
				if(page != thisPage) {
					return;
				}
				if(0 == result.size()) {
					return;
				}
				for(IJSON item : result) {
					JSONObject newJSONObject = (JSONObject) item;
					HashMap<String, Object> newsMap = new HashMap<String, Object>();
					String photoURL = ((JSONString) newJSONObject.get("imageUrl")).getValue();
					String photoName = Storage.getImageName(photoURL);
					newsMap.put("photo", photoName);
					newsMap.put("title", ((JSONString) newJSONObject.get("title")).getValue());
					newsMap.put("publisher", ((JSONString) newJSONObject.get("publisher")).getValue());
					newsMap.put("date", ((JSONString) newJSONObject.get("date")).getValue());
					newsMap.put("url", ((JSONString) newJSONObject.get("url")).getValue());
					newsList.add(newsMap);
					if(Text.isBlank(photoURL)) {
		            	continue;
		            }
		            // 加载图片
		            Networking.doImage("image", new ImageResponse(photoName, newsList.size() - 1) {
						@Override
						public void onFinished(Bitmap content) {
							HashMap<String, Object> map = newsList.get((Integer) tag);
							map.put("photo", content);
							ListView listview = (ListView) HomeActivity.this.getActivity().findViewById(R.id.home_list_news);
							SimpleAdapter adapter = (SimpleAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter();
							adapter.notifyDataSetChanged();
						}
		            }, photoURL);
				}
				ListView listview = (ListView) HomeActivity.this.getActivity().findViewById(R.id.home_list_news);
				SimpleAdapter adapter = (SimpleAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter();
				adapter.notifyDataSetChanged();
				page = thisPage + 1;
			}
		}, page);
	}

	/**
	 * 处理搜索按钮
	 */
	public void dealSearch() {
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this.getActivity(), SearchActivity.class);
				HomeActivity.this.getActivity().startActivity(intent);
			}
		});
	}
	
	/**
	 * 界面预设
	 */
	private void prepare() {
		if(listNews.getHeaderViewsCount() > 0) {
			return;
		}
		animRight = new RotateAnimation(-30, 30f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
		animRight.setDuration(1000);
        animRight.setAnimationListener(listener);
		animLeft = new RotateAnimation(30f, -30f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
		animLeft.setDuration(1000);
		animLeft.setAnimationListener(listener);
		View viewHead = LayoutInflater.from(this.getActivity()).inflate(R.layout.div_home_head, null);
		listNews.addHeaderView(viewHead);
		btnSearch.getBackground().setAlpha(200);
		btnBell.setImageAlpha(200);
		btnBell.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == Me.instance) {
					Intent intent = new Intent(HomeActivity.this.getActivity(), LoginActivity.class);
					HomeActivity.this.startActivity(intent);
					Toast.makeText(HomeActivity.this.getActivity(), "请先登录账号", Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this.getActivity(), MyMessagesActivity.class);
				HomeActivity.this.getActivity().startActivity(intent);
			}
		});
		browser = (ScrollWebView) viewHead.findViewById(R.id.home_browser);
		DisplayMetrics metrics = new DisplayMetrics();
		this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		LayoutParams lp = (LayoutParams) browser.getLayoutParams();
		lp.height = metrics.widthPixels * 17 / 20;
		browser.setLayoutParams(lp);
		imgLeft = (ImageView) viewHead.findViewById(R.id.home_image_left);
		imgRight = (ImageView) viewHead.findViewById(R.id.home_image_right);
		imgLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == Me.instance) {
					Intent intent = new Intent(HomeActivity.this.getActivity(), LoginActivity.class);
					HomeActivity.this.startActivity(intent);
					Toast.makeText(HomeActivity.this.getActivity(), "请先登录账号", Toast.LENGTH_LONG).show();
					return;
				}
				((MainActivity) HomeActivity.this.getActivity()).goPatient();
			}
		});
		imgRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == Me.instance) {
					Intent intent = new Intent(HomeActivity.this.getActivity(), LoginActivity.class);
					HomeActivity.this.startActivity(intent);
					Toast.makeText(HomeActivity.this.getActivity(), "请先登录账号", Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent(HomeActivity.this.getActivity(), BrowserActivity.class);
				intent.putExtra("url", Networking.fetchURL("CalendarPage", Me.instance.token));
				HomeActivity.this.getActivity().startActivity(intent);
			}
		});
		browser.getSettings().setJavaScriptEnabled(true);
		browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		browser.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Intent intent = new Intent(HomeActivity.this.getActivity(), BrowserActivity.class);
				intent.putExtra("url", url);
				startActivity(intent);
				browser.pauseTimers();
				browser.resumeTimers();
                return true;
			}
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				browser.loadUrl("about:blank");
			}
		});
		this.getActivity().findViewById(R.id.home_layout_header).bringToFront();
	}

	/**
	 * 处理资讯
	 */
	private void dealNews() {
		SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), newsList, R.layout.listitem_news,
			new String[]{"photo", "title", "publisher", "date"}, 
	        new int[]{R.id.news_image_photo, R.id.news_label_title, R.id.news_label_publisher, R.id.news_label_date});
		listItemAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap) {
                    ImageView imageView = (ImageView)view;
                    Bitmap bitmap = (Bitmap) data;
                    imageView.setImageDrawable(new BitmapDrawable(bitmap));
                    return true;
                }
                return false;
            }
        });
		listNews.setAdapter(listItemAdapter);
		listNews.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
				index = index - 1;
				HashMap<String, Object> newsMap = newsList.get(index);
				Intent intent = new Intent(HomeActivity.this.getActivity(), BrowserActivity.class);
				intent.putExtra("url", newsMap.get("url").toString());
				HomeActivity.this.startActivity(intent);
            }
		});
		listNews.setOnScrollListener(new OnScrollListener() {    
	        boolean isLastRow = false;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {      
					loadNews();
	                isLastRow = false;      
	            }
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 2) {      
	                isLastRow = true;      
	            }
			}
		});
	}

	/**
	 * 摇晃铃铛
	 */
	public void shakeBell() {
		btnBell.setImageResource(R.drawable.bell_active);
		//
		if(0 == shakeDirection) {
	        btnBell.startAnimation(animRight);
	        shakeDirection = 1;
		}
	}

	/**
	 * 停止摇晃铃铛
	 */
	public void stopBell() {
		btnBell.setImageResource(R.drawable.bell_normal);
		shakeDirection = 0;
	}

	@Override
	public void onConflict() {
		
	}

	@Override
	public void onCommand(String from, String action, com.slfuture.carrie.base.type.Table<String, Object> data) {
		if("systemMessage".equals(action)) {
			shakeBell();
		}
	}
}
