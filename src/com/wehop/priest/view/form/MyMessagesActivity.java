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
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.structure.Notify;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.general.view.form.BrowserActivity;
import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONBoolean;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.time.Time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

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
                Notify notifyModel = dataList.get(position);
                if(!notifyModel.hasRead) {
                    Host.doCommand("readMessage", new CommonResponse<String>() {
                        @Override
                        public void onFinished(String content) { }
                    }, Me.instance.token, notifyModel.id);
                }
                switch(notifyModel.type) {
                case Notify.TYPE_1:
                	Intent intent1 = new Intent(MyMessagesActivity.this, AddRequestActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("message", notifyModel);
                    intent1.putExtras(bundle);
                    intent1.putExtra("messageId", notifyModel.id);
                    MyMessagesActivity.this.startActivity(intent1);
                	break;
                case Notify.TYPE_2:
                	Intent intent2 = new Intent(MyMessagesActivity.this, TextActivity.class);
                	intent2.putExtra("title", "通知");
                	intent2.putExtra("content", "对方接受了您的添加请求");
                    MyMessagesActivity.this.startActivity(intent2);
                	break;
                case Notify.TYPE_3:
                	Intent intent3 = new Intent(MyMessagesActivity.this, TextActivity.class);
                	intent3.putExtra("title", "通知");
                	intent3.putExtra("content", "对方拒绝了您的添加请求");
                    MyMessagesActivity.this.startActivity(intent3);
                	break;
                case Notify.TYPE_4:
                	Intent intent9 = new Intent(MyMessagesActivity.this, TextActivity.class);
                	intent9.putExtra("title", "通知");
                	intent9.putExtra("content", "对方删除了您");
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
        Host.doCommand("myMessage", new CommonResponse<String>() {
            @Override
            public void onFinished(String content) {
                if (Response.CODE_SUCCESS != code()) {
                    Toast.makeText(MyMessagesActivity.this, "网络问题", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject resultObject = JSONObject.convert(content);
                if (((JSONNumber) resultObject.get("code")).intValue() <= 0) {
                    Toast.makeText(MyMessagesActivity.this, ((JSONString) resultObject.get("msg")).getValue(), Toast.LENGTH_LONG).show();
                    return;
                }
                com.qcast.tower.business.Runtime.hasUnreadMessage = false;
                dataList.clear();
                JSONArray result = (JSONArray) resultObject.get("data");
                if (null == result) {
                	dataList.clear();
                	dataList.addAll(fetchUnreadMessages());
                    adapter.notifyDataSetChanged();
                    return;
                }
                SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
                for(IJSON item : result) {
                    JSONObject newJSONObject = (JSONObject) item;
                    Notify myMessageModel = new Notify();
                    myMessageModel.id = ((JSONNumber) newJSONObject.get("id")).intValue();
                    myMessageModel.title = ((JSONString) newJSONObject.get("title")).getValue();
                    if(null == newJSONObject.get("description")) {
                        myMessageModel.description = "";
                    }
                    else {
                        myMessageModel.description = ((JSONString) newJSONObject.get("description")).getValue();
                    }
                    myMessageModel.time = format.format(((JSONNumber) newJSONObject.get("time")).longValue());
                    myMessageModel.type = ((JSONNumber) newJSONObject.get("type")).intValue();
                    myMessageModel.hasRead = ((JSONBoolean) newJSONObject.get("hasRead")).getValue();
                    switch(myMessageModel.type) {
                    case Notify.TYPE_1:
                        JSONObject infomation1 = (JSONObject) newJSONObject.get("info");
                        if(null == infomation1) {
                        	break;
                        }
                        myMessageModel.requestId = ((JSONString) infomation1.get("requestId")).getValue();
                        myMessageModel.name = ((JSONString) infomation1.get("name")).getValue();
                        myMessageModel.phone = ((JSONString) infomation1.get("phone")).getValue();
                        myMessageModel.relation = ((JSONString) infomation1.get("relation")).getValue();
                    	break;
                    case Notify.TYPE_5:
                        JSONObject infomation2 = (JSONObject) newJSONObject.get("info");
                        if(null == infomation2) {
                        	break;
                        }
                        myMessageModel.name = "";
                        if(null != infomation2.get("name")) {
                        	myMessageModel.name = ((JSONString) infomation2.get("name")).getValue();
                        }
                        myMessageModel.phone = "";
                        if(null != infomation2.get("phone")) {
                            myMessageModel.phone = ((JSONString) infomation2.get("phone")).getValue();
                        }
                        myMessageModel.relation = "";
                        if(null != infomation2.get("relation")) {
                            myMessageModel.relation = ((JSONString) infomation2.get("relation")).getValue();
                        }
                    	break;
                    case Notify.TYPE_6:
                        JSONObject infomation3 = (JSONObject) newJSONObject.get("info");
                        if(null == infomation3) {
                        	break;
                        }
                        myMessageModel.name = "";
                        if(null != infomation3.get("name")) {
                        	myMessageModel.name = ((JSONString) infomation3.get("name")).getValue();
                        }
                        myMessageModel.phone = "";
                        if(null != infomation3.get("phone")) {
                            myMessageModel.phone = ((JSONString) infomation3.get("phone")).getValue();
                        }
                        myMessageModel.relation = "";
                        if(null != infomation3.get("relation")) {
                            myMessageModel.relation = ((JSONString) infomation3.get("relation")).getValue();
                        }
                    	break;
                    case Notify.TYPE_3:
                        JSONObject infomation4 = (JSONObject) newJSONObject.get("info");
                        if(null == infomation4) {
                        	break;
                        }
                        myMessageModel.url = "";
                        if(null != infomation4.get("url")) {
                        	myMessageModel.url = ((JSONString) infomation4.get("url")).getValue();
                        }
                    	break;
                    case Notify.TYPE_7:
                    	break;
                    case Notify.TYPE_8:
                    	break;
                    case Notify.TYPE_9:
                    	break;
                    }
                    dataList.add(myMessageModel);
                }
                dataList.addAll(fetchUnreadMessages());
                adapter.notifyDataSetChanged();
            }
        }, Me.instance.token);
    }

    /**
     * 获取所有未读消息
     */
    private ArrayList<Notify> fetchUnreadMessages() {
    	ArrayList<Notify> result = new ArrayList<Notify>();
    	Hashtable<String, EMConversation> conversationMap = EMChatManager.getInstance().getAllConversations();
    	for(Entry<String, EMConversation> conversationEntry : conversationMap.entrySet()) {
    		String conversationId = conversationEntry.getKey();
    		int msgCount = conversationEntry.getValue().getUnreadMsgCount();
        	if(msgCount <= 0) {
        		continue;
        	}
        	if(conversationEntry.getValue().isGroup()) {
        		Notify model = fetchDoctorConversation(conversationId);
        		if(null != model) {
        			result.add(model);
        		}
        	}
        	else {
        		Notify model = fetchFriendConversation(conversationId);
        		if(null != model) {
        			result.add(model);
        		}
        	}
    	}
    	return result;
    }

    private Notify fetchFriendConversation(String imUsername) {
    	Notify result = new Notify();
    	result.type = Notify.TYPE_8;
    	result.imId = imUsername;
    	result.title = "好友消息";
    	result.time = Time.now().toString();
    	result.description = Me.instance.fetchFriendByIM(imUsername).nickname;
    	return result;
    }

    private Notify fetchDoctorConversation(String doctorIMUsername) {
    	Notify result = new Notify();
    	return result;
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
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_notify, null);
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
            if(Notify.TYPE_1 == model.type) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_1);
            }
            else if(Notify.TYPE_2 == model.type) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_2);
            }
			else if(Notify.TYPE_3 == model.type) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_3);
			}
			else if(Notify.TYPE_4 == model.type) {
            	viewHolder.imgIcon.setImageResource(R.drawable.icon_notify_4);
			}
            viewHolder.labTitle.setText(model.title);
            viewHolder.labTime.setText(model.time);
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
