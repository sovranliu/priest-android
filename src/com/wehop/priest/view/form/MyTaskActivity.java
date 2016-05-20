package com.wehop.priest.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;

/**
 * 我的任务页
 */
@ResourceView(id = R.layout.activity_mytask)
public class MyTaskActivity extends ActivityEx {
	@ResourceView(id = R.id.mytask_image_close)
	public ImageView imgClose;
	@ResourceView(id = R.id.mytask_label_refresh)
	public TextView labRefresh;
	@ResourceView(id = R.id.mytask_list)
	public ListView listTask;

	/**
	 * 当前页面ID
	 */
	private int pageId = 1;
	/**
	 * 任务列表
	 */
	protected ArrayList<HashMap<String, Object>> taskList = new ArrayList<HashMap<String, Object>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	// 
    	imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyTaskActivity.this.finish();
			}
		});
    	labRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pageId = 1;
				load();
			}
		});
    	prepareList();
    	load();
    }
    

	/**
	 * 处理成员
	 */
	private void prepareList() {
		SimpleAdapter adapter = new SimpleAdapter(this, taskList, R.layout.listitem_task,
				new String[]{"name", "time", "reason"}, 
		        new int[]{R.id.listitem_task_label_name, R.id.listitem_task_label_time, R.id.listitem_task_label_reason});
		adapter.setViewBinder(new ViewBinder() {
			@SuppressWarnings("deprecation")
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
		listTask.setAdapter(adapter);
		listTask.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
				HashMap<String, Object> newsMap = taskList.get(index);
				Networking.doCommand("LoadTask", new JSONResponse(MyTaskActivity.this) {
					@Override
					public void onFinished(JSONVisitor content) {
						if(null == content) {
							return;
						}
						if(-1 == content.getInteger("code")) {
							Toast.makeText(MyTaskActivity.this, "该任务已经被其他人领走", Toast.LENGTH_LONG).show();
							return;
						}
						if(content.getInteger("code") <= 0) {
							return;
						}
						content = content.getVisitor("data");
						Intent intent = new Intent(MyTaskActivity.this, ClientActivity.class);
						intent.putExtra("id", content.getInteger("id", 0));
						intent.putExtra("userId", content.getInteger("id", 0));
						intent.putExtra("name", content.getString("name"));
						intent.putExtra("gender", content.getInteger("gender", 0));
						intent.putExtra("birthday", content.getString("birthday"));
						MyTaskActivity.this.startActivity(intent);
					}
				}, Me.instance.token, newsMap.get("id"));
            }
		});
		listTask.setOnScrollListener(new OnScrollListener() {    
	        boolean isLastRow = false;
	        
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {      
					load();
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
     * 刷新列表
     */
    public void load() {
    	Networking.doCommand("TaskList", new JSONResponse(MyTaskActivity.this, pageId) {
			@Override
			public void onFinished(JSONVisitor content) {
				if(null == content || content.getInteger("code") <= 0) {
					return;
				}
				int requestPageId = (Integer) tag;
				if(requestPageId != pageId) {
					return;
				}
				for(JSONVisitor item : content.getVisitors("data")) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("id", item.getInteger("id", 0));
					map.put("userId", item.getInteger("userId", 0));
					map.put("name", item.getString("name"));
					map.put("time", item.getString("time"));
					map.put("reason", item.getString("reason"));
					taskList.add(map);
				}
				SimpleAdapter adapter = (SimpleAdapter) listTask.getAdapter();
				adapter.notifyDataSetChanged();
			}
    	}, Me.instance.token, pageId);
    }
}
