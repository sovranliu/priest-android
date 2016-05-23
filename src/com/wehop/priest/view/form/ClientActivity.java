package com.wehop.priest.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.model.core.ITargetEventable;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.general.view.form.BrowserActivity;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

/**
 * 客户界面
 */
@ResourceView(id=R.layout.activity_client)
public class ClientActivity extends ActivityEx {
	@ResourceView(id=R.id.client_layout_user)
	public View viewUser;
	@ResourceView(id=R.id.client_image_close)
	public ImageView imgClose;
	@ResourceView(id=R.id.client_image_photo)
	public ImageView imgPhoto;
	@ResourceView(id=R.id.client_label_name)
	public TextView labName;
	@ResourceView(id=R.id.client_image_gender)
	public ImageView imgGender;
	@ResourceView(id=R.id.client_label_birthday)
	public TextView labBirthday;
	@ResourceView(id=R.id.client_layout_tab1)
	public View viewTab1;
	@ResourceView(id=R.id.client_layout_tab2)
	public View viewTab2;
	@ResourceView(id=R.id.client_layout_tab3)
	public View viewTab3;
	@ResourceView(id=R.id.client_list1)
	public ListView list1;
	@ResourceView(id=R.id.client_list2)
	public ListView list2;
	@ResourceView(id=R.id.client_list3)
	public ListView list3;
	
	/**
	 * 客户ID
	 */
	private int clientId = 0;
	private int userId = 0;
	/**
	 * 体检报告列表
	 */
	private ArrayList<HashMap<String, Object>> examinationList = new ArrayList<HashMap<String, Object>>();
	private int examinationPageId = 1;
	/**
	 * 医生建议列表
	 */
	private ArrayList<HashMap<String, Object>> suggestList = new ArrayList<HashMap<String, Object>>();
	private int suggestLastId = 0;
	/**
	 * 硬件列表
	 */
	private ArrayList<HashMap<String, Object>> hardwareList = new ArrayList<HashMap<String, Object>>();


	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 界面处理
		prepare();
	}

	private void prepare() {
		viewUser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ClientActivity.this, SuggestActivity.class);
				intent.putExtra("id", userId);
				ClientActivity.this.startActivity(intent);
			}
		});
		imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClientActivity.this.finish();
			}
		});
		clientId = this.getIntent().getIntExtra("id", 0);
		userId = this.getIntent().getIntExtra("userId", 0);
		labName.setText(this.getIntent().getStringExtra("name"));
		switch(this.getIntent().getIntExtra("gender", 0)) {
		case 1:
			imgGender.setImageResource(R.drawable.icon_gender_male);
			break;
		case 2:
			imgGender.setImageResource(R.drawable.icon_gender_female);
			break;
		case 0:
		default:
			imgGender.setImageResource(R.drawable.icon_gender_unknown);
			break;
		}
		labBirthday.setText(this.getIntent().getStringExtra("birthday"));
		//
		viewTab1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				list1.setVisibility(View.VISIBLE);
				list2.setVisibility(View.GONE);
				list3.setVisibility(View.GONE);
			}
		});
		SimpleAdapter listItemAdapter = new SimpleAdapter(ClientActivity.this, examinationList, R.layout.listitem_examination,
				new String[]{"id", "icon", "title", "description", "time"}, 
		        new int[]{R.id.listitem_examination_label_id, R.id.listitem_examination_image_icon, R.id.listitem_examination_label_title, R.id.listitem_examination_label_description, R.id.listitem_examination_label_time});
		listItemAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageBitmap((Bitmap) data);
                    return true;
                }
                else if(view instanceof ImageView && data instanceof String) {
                	Networking.<ImageView>doImage("", (ImageView) view, new ITargetEventable<ImageView, Bitmap>() {
						@Override
						public void on(ImageView target, Bitmap event) {
							target.setImageBitmap(event);
						}
                	}, (String) data);
                    return true;
                }
                return false;
			}});
		list1.setAdapter(listItemAdapter);
		list1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, final int index, long arg3) {
				Intent intent = new Intent(ClientActivity.this, BrowserActivity.class);
				intent.putExtra("url", (String) examinationList.get(index).get("url"));
				ClientActivity.this.startActivity(intent);
			}
		});
		list1.setOnScrollListener(new OnScrollListener() {
	        boolean isLastRow = false;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {      
					loadExamination();
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
		loadExamination();
		//
		viewTab2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				list1.setVisibility(View.GONE);
				list2.setVisibility(View.VISIBLE);
				list3.setVisibility(View.GONE);
			}
		});
		listItemAdapter = new SimpleAdapter(ClientActivity.this, suggestList, R.layout.listitem_suggest,
			new String[]{"title", "time", "content"}, 
	        new int[]{R.id.listitem_suggest_label_title, R.id.listitem_suggest_label_time, R.id.listitem_suggest_label_content});
		listItemAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageBitmap((Bitmap) data);
                    return true;
                }
                else if(view instanceof TextView && data instanceof Boolean) {
                	TextView textView = (TextView) view;
                	if((Boolean) data) {
                		textView.setVisibility(View.VISIBLE);
                	}
                	else {
                		textView.setVisibility(View.GONE);
                	}
                    return true;
                }
                return false;
			}});
		list2.setAdapter(listItemAdapter);
		list2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, final int index, long arg3) {
			}
		});
		list2.setOnScrollListener(new OnScrollListener() {    
	        boolean isLastRow = false;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {      
					loadSuggest();
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
		loadSuggest();
		//
		viewTab3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				list1.setVisibility(View.GONE);
				list2.setVisibility(View.GONE);
				list3.setVisibility(View.VISIBLE);
			}
		});
		listItemAdapter = new SimpleAdapter(ClientActivity.this, hardwareList, R.layout.listitem_hardware,
				new String[]{"icon", "title"}, 
		        new int[]{R.id.hardware_image_icon, R.id.hardware_label_title});
		listItemAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
			}});
		list3.setAdapter(listItemAdapter);
		list3.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, final int index, long arg3) {
			}
		});
		loadHardware();
	}

	/**
	 * 加载体检数据
	 */
	private void loadExamination() {
		Networking.doCommand("ExaminationList", new JSONResponse(ClientActivity.this) {
			@Override
			public void onFinished(JSONVisitor content) {
				if(null == content || content.getInteger("code") <= 0) {
					return;
				}
				examinationList.clear();
				for(JSONVisitor item : content.getVisitors("data")) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("id", "报告编号：" + item.getInteger("id", 0));
					map.put("status", item.getBoolean("hasRead"));
					map.put("icon", item.getString("icon"));
					map.put("title", "项目：" + item.getString("title"));
					map.put("description", "简介：" + item.getString("description"));
					map.put("time", DateTime.parse(item.getLong("addTime")).toString());
					map.put("url", item.getString("url"));
					examinationList.add(map);
				}
				SimpleAdapter adapter = (SimpleAdapter) list1.getAdapter();
				adapter.notifyDataSetChanged();
			}
		}, Me.instance.token, userId, examinationPageId);
	}

	/**
	 * 加载医生建议数据
	 */
	private void loadSuggest() {
		Networking.doCommand("SuggestList", new JSONResponse(ClientActivity.this, suggestLastId) {
			@Override
			public void onFinished(JSONVisitor content) {
				if(null == content || content.getInteger("code") <= 0) {
					return;
				}
				if((Integer) tag != suggestLastId) {
					return;
				}
				for(JSONVisitor item : content.getVisitors("data")) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					if(0 == suggestLastId || suggestLastId > item.getInteger("id", 0)) {
						suggestLastId = item.getInteger("id", 0);
					}
					map.put("id", item.getInteger("id", 0));
					map.put("title", item.getString("title"));
					map.put("tip", item.getBoolean("hasRead"));
					map.put("time", item.getString("time"));
					map.put("content", item.getString("content"));
					suggestList.add(map);
				}
				SimpleAdapter adapter = (SimpleAdapter) list2.getAdapter();
				adapter.notifyDataSetChanged();
			}
		}, Me.instance.token, userId, suggestLastId);
	}
	
	/**
	 * 加载硬件列表
	 */
	private void loadHardware() {
		hardwareList.clear();
		HashMap<String, Object> map = null;
		//
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.icon_sb);
		map.put("title", "智能手环");
		hardwareList.add(map);
		//
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.icon_bp);
		map.put("title", "血压计");
		hardwareList.add(map);
		//
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.icon_ws);
		map.put("title", "体脂秤");
		hardwareList.add(map);
		SimpleAdapter adapter = (SimpleAdapter) list3.getAdapter();
		adapter.notifyDataSetChanged();
	}
}
