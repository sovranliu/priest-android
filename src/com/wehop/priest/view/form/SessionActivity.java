package com.wehop.priest.view.form;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONBoolean;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;
import com.wehop.priest.business.Logic;
import com.wehop.priest.framework.Storage;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

    // private final static String URL = "http://www.baidu.com";

    @ResourceView(id = R.id.title)
    public TextView mSessionTitleView = null;

    @ResourceView(id = R.id.session_list)
    public ListView mListView = null;

    protected ArrayList<HashMap<String, Object>> mSessionList = new ArrayList<HashMap<String, Object>>();

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

    /**
     * 准备
     */
    public void prepare() {

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), mSessionList, R.layout.session_list_item,
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
                Map data = mSessionList.get(position);
                Intent intent = new Intent(SessionActivity.this.getActivity(), ChatActivity.class);
                // intent.putExtra(GLOBAL_ID, (String) data.get(GLOBAL_ID));
                // intent.putExtra(USER_ID, (String)data.get(USER_ID));
                // intent.putExtra(USER_NAME, (String)data.get(USER_NAME));
                // intent.putExtra(PHOTO, (String)data.get(PHOTO));
                // intent.putExtra(USER_IM_NAME, "efg");
                intent.putExtra(GROUP_ID, (String) data.get(GROUP_ID));
                intent.putExtra(USER_NAME, (String) data.get(USER_NAME));
                intent.putExtra(USER_IM_NAME, (String) data.get(USER_IM_NAME));
                startActivity(intent);
            }
        });

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
                Log.i("gxl", "JSONVisitor, sessions: user = " + Logic.user.imUsername + " , content = " + content);
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

                int recordCount = data.getInteger("recordCount");
                int pageCount = data.getInteger("pageCount");
                int pageSize = data.getInteger("pageSize");
                int page = data.getInteger("page");
                int nextStart = data.getInteger("nextStart");
                boolean sortAsc = data.getBoolean("sortAsc");
                String sortField = data.getString("sortField");

                ICollection<JSONVisitor> records = data.getVisitors("records");

                mSessionList.clear();
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

                    mSessionList.add(map);

                    if (userPortrait != null) {
                        String imageURL = userPortrait;
                        Log.i("gxl", "try to download: " + imageURL);
                        // load photo
                        Host.doImage("image",
                                new ImageResponse(Storage.getImageName(imageURL), mSessionList.size() - 1) {
                            @Override
                            public void onFinished(Bitmap content) {
                                Log.i("gxl", "download succeed ...");
                                HashMap<String, Object> map = mSessionList.get((Integer) tag);
                                map.put(PHOTO, content);
                                // refresh
                                mHandler.removeMessages(REFRESH_LIST);
                                mHandler.sendEmptyMessage(REFRESH_LIST);
                            }
                        }, imageURL);
                    }
                }
                mHandler.sendEmptyMessage(LOAD_SUCCESS);
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

}
