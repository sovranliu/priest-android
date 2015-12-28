package com.wehop.priest.view.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;

import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
    public static final String CHAT_TYPE = "type"; // 0 single chat, 1 group chat
    
    public static final String GROUP_ID = "group_id";
    
    public static final String CLIENT_IM_NAME = "client_imName";

    public static final String USER_ID = "user_id";

    public static final String USER_IM_NAME = "user_imName";

    public static final String USER_NAME = "user_name";

    public static final String PHOTO = "photo";

    public static final String DATE = "date";

    public static final String LAST_MESSAGE = "last_message";

    public static final int LOAD_SUCCESS = 1;

    public static final int LOAD_FAILED = 2;

    public final static String URL = "http://www.baidu.com";

    @ResourceView(id = R.id.title)
    public TextView mSessionTitleView = null;

    @ResourceView(id = R.id.session_list)
    public ListView mListView = null;

    public String url = null;

    protected ArrayList<HashMap<String, Object>> mSessionList = new ArrayList<HashMap<String, Object>>();

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_SUCCESS:

                    break;

                case LOAD_FAILED:
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

    /**
     * 准备
     */
    public void prepare() {

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), mSessionList, R.layout.session_list_item,
                new String[] { PHOTO, USER_NAME, DATE, LAST_MESSAGE },
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
                Map data = mSessionList.get(position);
                Intent intent = new Intent(SessionActivity.this.getActivity(), ChatActivity.class);
                // intent.putExtra(GLOBAL_ID, (String) data.get(GLOBAL_ID));
                // intent.putExtra(USER_ID, (String)data.get(USER_ID));
                // intent.putExtra(USER_NAME, (String)data.get(USER_NAME));
                // intent.putExtra(PHOTO, (String)data.get(PHOTO));
                // intent.putExtra(USER_IM_NAME, "efg");
                intent.putExtra(GROUP_ID, (String)data.get(GROUP_ID));
                intent.putExtra(CLIENT_IM_NAME, (String)data.get(CLIENT_IM_NAME));
                startActivity(intent);
            }
        });

    }

    /**
     * 加载
     */
    public void load() {
        String myGlobalId = "";
        Host.doCommand("sessions", new CommonResponse<String>() {

            @Override
            public void onFinished(String content) {
                if (Response.CODE_SUCCESS != code()) {
                    Toast.makeText(SessionActivity.this.getActivity(), "访问网络失败", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject resultObject = JSONObject.convert(content);
                if (((JSONNumber) resultObject.get("code")).intValue() <= 0) {
                    Toast.makeText(SessionActivity.this.getActivity(), "get code is 0", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONArray result = (JSONArray) resultObject.get("data");
                mSessionList.clear();
                for (IJSON newJSON : result) {
                    JSONObject jsonObject = (JSONObject) newJSON;
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    int chatType = ((JSONNumber) (jsonObject.get("chatType"))).intValue();
                    String groupId = null;
                    String client = null;
                    if(chatType == 1) {
                        groupId = ((JSONString) (jsonObject.get("groupId"))).getValue();
                        client = ((JSONString) (jsonObject.get("client_user_imName"))).getValue();
                    } else {
                        continue;// single chat not support yet
                    }
                    String user_id = ((JSONString) (jsonObject.get("last_user_id"))).getValue();
                    String user_name = ((JSONString) (jsonObject.get("last_user_name"))).getValue();
                    String user_imName = ((JSONString) (jsonObject.get("last_user_imName"))).getValue();
                    Object photo = null;
                    if (jsonObject.get("last_user_photo") != null) {
                        photo = ((JSONString) (jsonObject.get("last_user_photo"))).getValue();
                    } else {
                        photo = R.drawable.photo_default;
                    }
                    String last_message = ((JSONString) (jsonObject.get("lastMessage"))).getValue();
                    String last_date = ((JSONString) (jsonObject.get("lastDate"))).getValue();

                    map.put(CHAT_TYPE, chatType);
                    map.put(GROUP_ID, groupId);
                    map.put(CLIENT_IM_NAME, client);
                    map.put(USER_ID, user_id);
                    map.put(USER_NAME, user_name);
                    map.put(USER_IM_NAME, user_imName);
                    map.put(PHOTO, photo);
                    map.put(LAST_MESSAGE, last_message);
                    map.put(DATE, last_date);
                    mSessionList.add(map);
                }
                ((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
            }
        }, myGlobalId);

    }

    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    

    public static void getUserFromServer(String imUsername) {
        // refresh data from app server & store to local db
    }

    // get user info by imName
    public static UserInfo getUserInfo(String imUsername) {
        // load from db
        return new UserInfo(imUsername);
    }
    
    //get group user info
    public static List<UserInfo> getUserList(String groupId) {
        //load user list from db
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

}
