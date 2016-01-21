package com.wehop.priest.view.form;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.Logic;
import com.wehop.priest.framework.Storage;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 会话页
 */
@ResourceView(id = R.layout.activity_session)
public class SessionActivity extends FragmentEx {

    public static final String GROUP_ID = "group_id";

    public static final String USER_NAME = "user_name";

    public static final String USER_IM_NAME = "user_imName";

    // public static final String USER_ID = "user_id";

    private static final String NAME = "name";

    private static final String PHOTO = "photo";

    private static final String DATE = "date";

    private static final String TOPIC = "last_message";

    private static final int LOAD_SUCCESS = 1;

    private static final int LOAD_FAILED = 2;

    private static final int REFRESH_LIST = 3;

    
    @ResourceView(id = R.id.search)
    public EditText txtSearch = null;
    
    @ResourceView(id = R.id.title)
    public TextView mSessionTitleView = null;

    @ResourceView(id = R.id.session_list)
    public ListView mListView = null;
    private BroadcastReceiver chatReceiver = null;

    /**
     * 当前展示会话列表
     */
    protected ArrayList<HashMap<String, Object>> currentSessionList = new ArrayList<HashMap<String, Object>>();
    /**
     * 会话列表
     */
    protected ArrayList<HashMap<String, Object>> sessionList = new ArrayList<HashMap<String, Object>>();


    /**
     * 未读消息映射
     */
    protected Table<String, Integer> messageMap = new Table<String, Integer>();
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i("gxl", "handle message : " + msg.what);
            switch (msg.what) {
            case LOAD_SUCCESS:
            case REFRESH_LIST:
                ((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
                break;
            case LOAD_FAILED:
                Toast.makeText(SessionActivity.this.getActivity(), "访问网络失败", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        prepare();
        load();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	refreshByMessageCome();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.getActivity().unregisterReceiver(chatReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 准备
     */
    public void prepare() {
    	txtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				refreshByFilter();
			}
    	});

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), currentSessionList, R.layout.session_list_item,
                new String[] { PHOTO, NAME, DATE, TOPIC },
                new int[] { R.id.remote_photo, R.id.remote_name, R.id.last_time, R.id.last_message });
        listItemAdapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Bitmap) {
                    ImageView imageView = (ImageView) view;
                    Bitmap bitmap = (Bitmap) data;
                    imageView.setImageBitmap(bitmap);
                    return true;
                }
                return false;
            }
        });

        mListView.setAdapter(listItemAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Map data = currentSessionList.get(position);
                Intent intent = new Intent(SessionActivity.this.getActivity(), GroupChatActivity.class);
                // Intent intent = new Intent(SessionActivity.this.getActivity(), ChatActivity.class);
                // intent.putExtra(GLOBAL_ID, (String) data.get(GLOBAL_ID));
                // intent.putExtra(USER_ID, (String)data.get(USER_ID));
                // intent.putExtra(USER_NAME, (String)data.get(USER_NAME));
                // intent.putExtra(PHOTO, (String)data.get(PHOTO));
                // intent.putExtra(USER_IM_NAME, "efg");
//                
//                intent.putExtra(GROUP_ID, (String) data.get(GROUP_ID));
//                messageMap.delete((String) data.get(USER_IM_NAME));
//                intent.putExtra(USER_NAME, (String) data.get(USER_NAME));
//                intent.putExtra(USER_IM_NAME, (String) data.get(USER_IM_NAME));

                intent.putExtra("localId", Logic.user.imUsername);
                intent.putExtra("groupId", (String) data.get(GROUP_ID));
                messageMap.delete((String) data.get(USER_IM_NAME));
                intent.putExtra("remoteName", (String) data.get(USER_NAME));
                intent.putExtra("remoteId", (String) data.get(USER_IM_NAME));
                intent.putExtra("localPhoto", Logic.user.photo);
                startActivity(intent);
            }
        });
        chatReceiver = new BroadcastReceiver() {
           	@Override
           	public void onReceive(Context context, Intent intent) {
           		String from = intent.getStringExtra("from");
           		Integer count = messageMap.get(from);
           		if(null == count) {
           			count = 1;
           		}
           		else {
           			count++;
           		}
           		messageMap.put(from, count);
           		boolean sentry = false;
           		for(HashMap<String, Object> session : sessionList) {
           			if(from.equals(session.get(USER_IM_NAME))) {
           				sentry = true;
           				break;
           			}
           		}
           		if(!sentry) {
           			load();
           		}
           		refreshByMessageCome();
    	        abortBroadcast();
           	}
        };
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
    	intentFilter.setPriority(2);
    	SessionActivity.this.getActivity().registerReceiver(chatReceiver, intentFilter);
    }

    /**
     * 加载
     */
    public void load() {
        Log.i("gxl", "user id = " + Logic.user.id);
        Log.i("gxl", "user username = " + Logic.user.username);
        Log.i("gxl", "user token = " + Logic.user.token);
        Object[] params = new Object[4];
        params[0] = 1; // page
        params[1] = 20; //page size
        params[2] = 1; //status
        params[3] = Logic.user.token; // token

        Host.doCommand("sessions", new JSONResponse(SessionActivity.this.getActivity()) {
            @Override
            public void onFinished(JSONVisitor content) {
                // TODO Auto-generated method stub
                Logger.d("JSONVisitor, sessions: user = " + Logic.user.imUsername + " , content = " + content);
                if (content == null) {
                    Toast.makeText(SessionActivity.this.getActivity(), "网络错误", Toast.LENGTH_LONG).show();
                    // login and take new token
                    requestNewToken();
                    return;
                }
                if (content.getInteger("code", -1) < 0) {
                    Toast.makeText(SessionActivity.this.getActivity(), content.getString("msg"), Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                JSONVisitor data = content.getVisitor("data");
                if(null == data) {
                	return;
                }
                int recordCount = data.getInteger("recordCount");
                int pageCount = data.getInteger("pageCount");
                int pageSize = data.getInteger("pageSize");
                int page = data.getInteger("page");
                int nextStart = data.getInteger("nextStart");
                boolean sortAsc = data.getBoolean("sortAsc");
                String sortField = data.getString("sortField");

                ICollection<JSONVisitor> records = data.getVisitors("records");

                sessionList.clear();
                for (JSONVisitor record : records) {
                    HashMap<String, Object> map = new HashMap<String, Object>();

                    int id = record.getInteger("id");
                    String groupId = record.getString("groupId");
                    String user_token = record.getString("user");
                    String userName = record.getString("userName");
                    String userPortrait = record.getString("userPortrait");
                    String userImName = record.getString("userImUsername");
                    String doctor_token = record.getString("doctor");
                    String doctorName = record.getString("doctorName");
                    String topic = record.getString("topic");
                    String lastSpeaker = record.getString("lastSpeaker");
                    boolean isLastSpeakerDoctor = record.getBoolean("isLastSpeakerDoctor");
                    int status = record.getInteger("status");
                    long time = record.getLong("time");

                    map.put("id", id);
                    map.put(GROUP_ID, groupId);
                    map.put("userToken", user_token);
                    map.put(USER_NAME, userName);
                    map.put("doctorToken", doctor_token);
                    map.put("doctorName", doctorName);
                    map.put("lastSpeaker", lastSpeaker);
                    map.put("isLastSpeakerDoctor", isLastSpeakerDoctor);
                    map.put("status", status);
                    map.put("time", time);

                    map.put(PHOTO, userPortrait);
                    map.put(USER_IM_NAME, userImName);
                    // for display
                    if (userPortrait == null) {
                        map.put(PHOTO, R.drawable.photo_default);
                    }
                    if (isLastSpeakerDoctor) {
                        map.put(NAME, doctorName);
                    } else {
                        map.put(NAME, userName);
                    }
                    map.put(DATE, formatter.format(new Date(time)));
                    map.put(TOPIC, topic);

                    sessionList.add(map);

                    if (userPortrait != null) {
                        String imageURL = userPortrait;
                        Log.i("gxl", "try to download: " + imageURL);
                        // load photo
                        Host.doImage("image", new ImageResponse(Storage.getImageName(imageURL), sessionList.size() - 1) {
                            @Override
                            public void onFinished(Bitmap content) {
                                Log.i("gxl", "download succeed ...");
                                HashMap<String, Object> map = sessionList.get((Integer) tag);
                                map.put(PHOTO, content);
                                // refresh
                                mHandler.removeMessages(REFRESH_LIST);
                                mHandler.sendEmptyMessage(REFRESH_LIST);
                            }
                        }, imageURL);
                    }
                }
                refreshByFilter();
            }

        }, params);
    }

    private void requestNewToken() {
        Host.doCommand("login", new JSONResponse(SessionActivity.this.getActivity()) {
            @Override
            public void onFinished(JSONVisitor content) {
                Log.i("gxl", "request new token content = " + content);
                if (content.getInteger("code", -1) < 0) {
                    Toast.makeText(SessionActivity.this.getActivity(), "用户登录未成功", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONVisitor dataVisitor = content.getVisitor("data");
                Logic.user.token = dataVisitor.getString("token");
                Logic.save();
                // reload the data
                load();
            }
        }, Logic.user.username, Logic.user.password);
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static void getUserFromServer(String imUsername) {
        // refresh data from app server & store to local db
    }

    // get user info by imName
    public static UserInfo getUserInfo(String imUsername) {
        // load from db
        return new UserInfo(imUsername);
    }

    // get group user info
    public static List<UserInfo> getUserList(String groupId) {
        // load user list from db
        List<UserInfo> list = new ArrayList<UserInfo>();
        return list;
    }

    public static class UserInfo {
        /**
         * IM用户名
         */
        public String imUsername;
        /**
         * 用户名
         */
        public String userId;
        /**
         * 姓名
         */
        public String name;
        /**
         * 头像URL
         */
        public String photo;

        public UserInfo(String imName) {
            this.imUsername = imName;
        }

    }

    /**
     * 因为新消息到来而刷新列表
     */
    public void refreshByMessageCome() {
    	for(HashMap<String, Object> sessionItem : sessionList) {
    		String groupId = (String) sessionItem.get(GROUP_ID);
    		EMConversation conversation = EMChatManager.getInstance().getConversation(groupId);
            int msgCount = conversation.getUnreadMsgCount();
    		if(msgCount <= 0) {
        		sessionItem.put(PHOTO, BitmapFactory.decodeResource(this.getResources(), R.drawable.photo_default));
    		}
    		else {
        		sessionItem.put(PHOTO, BitmapFactory.decodeResource(this.getResources(), R.drawable.photo_hasmessage));
    		}
    	}
    	((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    /**
     * 因为过滤而刷新列表
     */
    public void refreshByFilter() {
    	currentSessionList.clear();
    	for(HashMap<String, Object> sessionItem : sessionList) {
    		String userName = (String) sessionItem.get(USER_NAME);
    		if(null == userName) {
    			continue;
    		}
    		if("".equals(txtSearch.getText().toString()) || userName.contains(txtSearch.getText().toString())) {
    			currentSessionList.add(sessionItem);
    		}
    	}
    	refreshByMessageCome();
    }
}
