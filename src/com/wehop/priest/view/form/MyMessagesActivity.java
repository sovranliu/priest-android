package com.wehop.priest.view.form;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.structure.notify.AddRefuseNotify;
import com.wehop.priest.business.structure.notify.AddRequestNotify;
import com.wehop.priest.business.structure.notify.AddAcceptNotify;
import com.wehop.priest.business.structure.notify.BeRemovedNotify;
import com.wehop.priest.business.structure.notify.Notify;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.carrie.base.json.JSONVisitor;

import java.util.ArrayList;

/**
 * 我的消息页
 */
@ResourceView(id = R.layout.activity_mymessage)
public class MyMessagesActivity extends ActivityEx {
	@ResourceView(id = R.id.mymessage_button_close)
	public ImageButton btnClose;
	@ResourceView(id = R.id.mymessage_list)
	public ListView listMessages;

    private ArrayList<Notify> dataList = new ArrayList<Notify>();
    private MyMessageAdapter adapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//
        dataList = new ArrayList<Notify>();
        adapter = new MyMessageAdapter(this, dataList);
        listMessages.setAdapter(adapter);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMessagesActivity.this.finish();
            }
        });
        listMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notify notify = dataList.get(position);
                if(!notify.hasRead) {
                    Host.doCommand("readMessage", new CommonResponse<String>() {
                        @Override
                        public void onFinished(String content) { }
                    }, Me.instance.token, notify.id);
                }
                switch(notify.type()) {
                case AddRequestNotify.TYPE_ADDREQUEST:
                	Intent intent1 = new Intent(MyMessagesActivity.this, AddRequestActivity.class);
                	intent1.putExtra("message", notify);
                    MyMessagesActivity.this.startActivity(intent1);
                	break;
                case AddAcceptNotify.TYPE_ADDACCEPT:
                	Intent intent2 = new Intent(MyMessagesActivity.this, TextActivity.class);
                	intent2.putExtra("title", "通知");
                	intent2.putExtra("content", ((AddAcceptNotify) notify).targetNickname + "(" + ((AddAcceptNotify) notify).targetPhone + ")接受了您的添加为 " + ((AddAcceptNotify) notify).relation + " 的请求");
                	MyMessagesActivity.this.startActivity(intent2);
                	break;
                case AddRefuseNotify.TYPE_ADDREFUSE:
                	Intent intent3 = new Intent(MyMessagesActivity.this, TextActivity.class);
                	intent3.putExtra("title", "通知");
                	intent3.putExtra("content", ((AddAcceptNotify) notify).targetNickname + "(" + ((AddAcceptNotify) notify).targetPhone + ")拒绝了您的添加为 " + ((AddAcceptNotify) notify).relation + " 的请求");
                	MyMessagesActivity.this.startActivity(intent3);
                	break;
                case BeRemovedNotify.TYPE_BEREMOVE:
                	Intent intent9 = new Intent(MyMessagesActivity.this, TextActivity.class);
                	intent9.putExtra("title", "通知");
                	intent9.putExtra("content", ((BeRemovedNotify) notify).targetNickname + "(" + ((BeRemovedNotify) notify).targetPhone + ")把你从好友列表中删除");
                    MyMessagesActivity.this.startActivity(intent9);
                	break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	loadData();
    }

    private void loadData() {
        Host.doCommand("myMessage", new JSONResponse(MyMessagesActivity.this) {
			@Override
			public void onFinished(JSONVisitor content) {
				if(null == content || content.getInteger("code", 0) <= 0) {
					return;
				}
	            com.wehop.priest.business.Runtime.hasUnreadMessage = false;
	            dataList.clear();
	            for(JSONVisitor item : content.getVisitors("data")) {
	            	if(AddRequestNotify.TYPE_ADDREQUEST == item.getInteger("type", 0)) {
	            		AddRequestNotify notify = new AddRequestNotify();
	            		if(notify.parse(item)) {
		            		dataList.add(notify);
	            		}
	            	}
	            	else if(AddAcceptNotify.TYPE_ADDACCEPT == item.getInteger("type", 0)) {
	            		AddAcceptNotify notify = new AddAcceptNotify();
	            		if(notify.parse(item)) {
		            		dataList.add(notify);
	            		}
	            	}
	            	else if(AddRefuseNotify.TYPE_ADDREFUSE == item.getInteger("type", 0)) {
	            		AddRefuseNotify notify = new AddRefuseNotify();
	            		if(notify.parse(item)) {
		            		dataList.add(notify);
	            		}
	            	}
	            	else if(BeRemovedNotify.TYPE_BEREMOVE == item.getInteger("type", 0)) {
	            		BeRemovedNotify notify = new BeRemovedNotify();
	            		if(notify.parse(item)) {
		            		dataList.add(notify);
	            		}
	            	}
	            }
	            adapter.notifyDataSetChanged();
			}
        }, Me.instance.token);
    }

    class MyMessageAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<Notify> data;

        public MyMessageAdapter(Context context, ArrayList<Notify> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            if (data != null && data.size() > 0) {
                return data.size();
            }
            else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if (data != null && data.size() > position) {
                return data.get(position);
            }
            else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Notify model = data.get(position);
            ViewHolder viewHolder = null;
            if(null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.listitem_notify, null);
                viewHolder = new ViewHolder();
                viewHolder.imgIcon = (ImageView) convertView.findViewById(R.id.notify_icon_type);
                viewHolder.labTitle = (TextView) convertView.findViewById(R.id.notify_label_title);
                viewHolder.labTime = (TextView) convertView.findViewById(R.id.notify_label_time);
                viewHolder.labDescription = (TextView) convertView.findViewById(R.id.notify_label_description);
                viewHolder.imgRead = (ImageView) convertView.findViewById(R.id.notify_icon_read);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if(AddRequestNotify.TYPE_ADDREQUEST == model.type()) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_1);
            }
            else if(AddAcceptNotify.TYPE_ADDACCEPT == model.type()) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_2);
            }
			else if(BeRemovedNotify.TYPE_BEREMOVE == model.type()) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_3);
			}
            viewHolder.labTitle.setText(model.title);
            viewHolder.labTime.setText(model.time.toString());
            viewHolder.labDescription.setText(model.description);
            if(model.hasRead) {
                viewHolder.imgRead.setVisibility(View.INVISIBLE);
            }
            else {
                viewHolder.imgRead.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        class ViewHolder {
            public ImageView imgIcon;
            public TextView labTitle;
            public TextView labTime;
            public TextView labDescription;
            public ImageView imgRead;
        }
    }
}
