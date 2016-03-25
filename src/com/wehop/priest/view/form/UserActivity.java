package com.wehop.priest.view.form;

import java.util.HashMap;
import java.util.LinkedList;

import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.slfuture.pretty.general.view.form.BrowserActivity;
import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 用户界面
 */
@ResourceView(id = R.layout.activity_user)
public class UserActivity extends FragmentEx {
	@ResourceView(id = R.id.user_button_photo)
	public ImageButton btnPhoto;

	/**
	 * 用户面板列表
	 */
	private LinkedList<HashMap<String, Object>> itemList = new LinkedList<HashMap<String, Object>>();


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prepare();
	}

	@Override
	public void onResume() {
		super.onResume();
		TextView txtCaption = (TextView) this.getActivity().findViewById(R.id.user_label_caption);
		if(null == Me.instance) {
			txtCaption.setText("点击登录");
		}
		else {
			txtCaption.setText(Me.instance.name);
		}
		if(null == Me.instance) {
			btnPhoto.setImageResource(R.drawable.user_photo_null);
		}
		else if(!Text.isBlank(Me.instance.photo)) {
            Networking.doImage("image", new ImageResponse(Me.instance.photo) {
				@Override
				public void onFinished(Bitmap content) {
					if(null == content) {
						return;
					}
					btnPhoto.setImageBitmap(GraphicsHelper.makeImageRing(GraphicsHelper.makeCycleImage(content, 200, 200), Color.WHITE, 4));
				}
            }, Me.instance.photo);
		}
		else {
			btnPhoto.setImageResource(R.drawable.user_photo_default);
		}
	}

	/**
	 * 界面预处理
	 */
	public void prepare() {
		btnPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == Me.instance) {
					Intent intent = new Intent(UserActivity.this.getActivity(), LoginActivity.class);
					UserActivity.this.startActivity(intent);
				}
				else {
					Intent intent = new Intent(UserActivity.this.getActivity(), UserInfoActivity.class);
					UserActivity.this.startActivity(intent);
				}
			}
		});
		dealList();
	}

	/**
	 * 处理咨询
	 */
	private void dealList() {
		HashMap<String, Object> map = null;
		//
		map = new HashMap<String, Object>();
		map.put("icon", BitmapFactory.decodeResource(Program.application.getResources(), R.drawable.icon_user_history));
		map.put("caption", "业绩回顾");
		itemList.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", BitmapFactory.decodeResource(Program.application.getResources(), R.drawable.icon_user_help));
		map.put("caption", "客服中心");
		itemList.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", BitmapFactory.decodeResource(Program.application.getResources(), R.drawable.icon_user_config));
		map.put("caption", "系统设置");
		itemList.add(map);
		//
		ListView listview = (ListView) this.getActivity().findViewById(R.id.user_list);
		if(listview.getHeaderViewsCount() > 0) {
			return;
		}
		SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), itemList, R.layout.listitem_user,
			new String[]{"icon", "caption"}, 
	        new int[]{R.id.listitem_user_image_icon, R.id.listitem_user_label_caption});
		listItemAdapter.setViewBinder(new ViewBinder() {
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
		listview.setAdapter(listItemAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
				if(0 == index) {
					if(null == Me.instance) {
						Intent intent = new Intent(UserActivity.this.getActivity(), LoginActivity.class);
						UserActivity.this.startActivity(intent);
						Toast.makeText(UserActivity.this.getActivity(), "请先登录账号", Toast.LENGTH_LONG).show();
						return;
					}
					Intent intent = new Intent(UserActivity.this.getActivity(), BrowserActivity.class);
					intent.putExtra("url", Networking.fetchURL("HistoryPage", Me.instance.token));
					UserActivity.this.startActivity(intent);
					return;
				}
				else if(1 == index) {
					String token = "";
					if(null != Me.instance) {
						token = Me.instance.token;
					}
					Intent intent = new Intent(UserActivity.this.getActivity(), BrowserActivity.class);
					intent.putExtra("url", Networking.fetchURL("CustomerPage", token));
					UserActivity.this.startActivity(intent);
					return;
				}
				else if(2 == index) {
					Intent intent = new Intent(UserActivity.this.getActivity(), ConfigActivity.class);
					UserActivity.this.startActivity(intent);
                    return;
				}
            }
		});
	}

	/**
	 * 根据原图和变长绘制圆形图片
	 * 
	 * @param source
	 * @param min
	 * @return
	 */
	public Bitmap transferCircleImage(Bitmap source, int min) {
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);
		/**
		 * 产生一个同样大小的画布
		 */
		Canvas canvas = new Canvas(target);
		/**
		 * 首先绘制圆形
		 */
		canvas.drawCircle(min / 2, min / 2, min / 2, paint);
		/**
		 * 使用SRC_IN
		 */
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		/**
		 * 绘制图片
		 */
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}
}
