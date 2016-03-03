package com.wehop.priest.view.form;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.general.view.form.BrowserActivity;
import com.wehop.priest.R;
import com.wehop.priest.business.Profile;

/**
 * 搜索页
 */
@ResourceView(id = R.layout.activity_search)
public class SearchActivity extends ActivityEx {
	/**
	 * 控件
	 */
	@ResourceView(id = R.id.search_button_close)
	public ImageButton btnClose;
	@ResourceView(id = R.id.search_text_keword)
	public EditText txtKeyword;
	@ResourceView(id = R.id.search_button_confirm)
	public ImageButton btnConfirm;
	@ResourceView(id = R.id.search_list_hot)
	public ListView listHot;
	/**
	 * 数据
	 */
	protected LinkedList<HashMap<String, Object>> hotList = new LinkedList<HashMap<String, Object>>();



	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 
		prepare();
		load();
	}

	/**
	 * 准备
	 */
	public void prepare() {
		prepareClose();
		prepareConfirm();
		prepareHot();
	}

	/**
	 * 准备关闭按钮
	 */
	public void prepareClose() {
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchActivity.this.finish();
			}
		});
		txtKeyword.getBackground().setAlpha(200);
	}

	/**
	 * 准备确认按钮
	 */
	public void prepareConfirm() {
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String keyword = txtKeyword.getText().toString();
				if(keyword.equals("")) {
					return;
				}
				doSearch(keyword);
			}
		});
	}

	/**
	 * 准备热词列表
	 */
	public void prepareHot() {
		SimpleAdapter adapter = new SimpleAdapter(this, 
			hotList, 
			R.layout.listitem_search,
			new String[]{"text"}, 
	        new int[]{R.id.listitem_search_text});
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
		listHot.setAdapter(adapter);
		listHot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
				if(hotList.size() == index + 1) {
					Profile.instance().keywords.clear();
					try {
						Profile.instance().save();
					}
					catch (IOException e) { }
					load();
					Toast.makeText(SearchActivity.this, "清空搜索记录完毕", Toast.LENGTH_LONG).show();
					return;
				}
				HashMap<String, Object> map = hotList.get(index);
				doSearch((String) map.get("text"));
            }
		});
	}

	/**
	 * 加载数据
	 */
	public void load() {
		Host.doCommand("HotKeyword", new JSONResponse(SearchActivity.this) {
			@Override
			public void onFinished(JSONVisitor content) {
				if(null == content) {
					return;
				}
				hotList.clear();
				for(JSONVisitor item : content.getVisitors("data")) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("text", item.toString());
					hotList.add(map);
				}
				for(String keyword : Profile.instance().keywords) {
					boolean sentry = false;
					for(JSONVisitor item : content.getVisitors("data")) {
						if(item.toString().equals(keyword)) {
							sentry = true;
							break;
						}
					}
					if(!sentry) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("text", keyword);
						hotList.add(map);
					}
				}
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("text", "清空搜索记录");
				hotList.add(map);
				SimpleAdapter adapter = (SimpleAdapter) listHot.getAdapter();
				adapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * 开始搜索
	 * 
	 * @param keyword 关键词
	 */
	public void doSearch(String keyword) {
		if(!Text.isBlank(keyword)) {
			if(!Profile.instance().keywords.contains(keyword)) {
				Profile.instance().keywords.add(keyword);
				try {
					Profile.instance().save();
				}
				catch (IOException e) { }
			}
		}
		Intent intent = new Intent(SearchActivity.this, BrowserActivity.class);
		intent.putExtra("url", Host.fetchURL("search", keyword));
		SearchActivity.this.startActivity(intent);
		SearchActivity.this.finish();
	}
}
