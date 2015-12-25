package com.wehop.priest.view.form;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.wehop.priest.R;
import com.wehop.priest.business.Logic;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

    public static final String SESSION_ID = "sessionId";

    public static final String GLOBAL_ID = "userGlobalId";

    public static final String NAME = "name";

    public static final String PHOTO = "photo";

    public static final String DATE = "date";

    public static final String MESSAGE_PREVIEW = "message_preview";

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
        // loadConversations();
    }

    /**
     * 准备
     */
    public void prepare() {

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), mSessionList, R.layout.session_list_item,
                new String[] { PHOTO, NAME, DATE, MESSAGE_PREVIEW },
                new int[] { R.id.contact_photo, R.id.contact_name, R.id.session_time, R.id.session_msg_view });
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
                intent.putExtra(GLOBAL_ID, "efg");
                startActivity(intent);
            }
        });

    }

    /**
     * 加载
     */
    public void load() {
        String myGlobalId = "";
        Host.doCommand("mySession", new CommonResponse<String>() {

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
                    String session_id = ((JSONString) (jsonObject.get("sessionId"))).getValue();
                    String user_global_id = ((JSONString) (jsonObject.get("userGlobalId"))).getValue();
                    String contact_name = ((JSONString) (jsonObject.get("name"))).getValue();
                    Object contact_photo = null;
                    if (jsonObject.get("photo") != null) {
                        contact_photo = ((JSONString) (jsonObject.get("photo"))).getValue();
                    } else {
                        contact_photo = R.drawable.photo_default;
                    }
                    String message = ((JSONString) (jsonObject.get("lastMessage"))).getValue();
                    String date = ((JSONString) (jsonObject.get("lastDate"))).getValue();

                    map.put(SESSION_ID, session_id);
                    map.put(GLOBAL_ID, user_global_id);
                    map.put(NAME, contact_name);
                    map.put(PHOTO, contact_photo);
                    map.put(MESSAGE_PREVIEW, message);
                    map.put(DATE, date);
                    mSessionList.add(map);
                }
                ((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
            }
        }, myGlobalId);

    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private void loadConversations() {
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();

        mSessionList.clear();
        for (EMConversation conversation : conversations.values()) {
            String imUsername = conversation.getUserName();
            User user = getUserInfo(imUsername);

            String name = user.name;
            String photo = user.photo;
            EMMessage message = conversation.getLastMessage();
            String lastMsg = ((TextMessageBody) message.getBody()).getMessage();
            long time = message.getMsgTime();

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(GLOBAL_ID, imUsername);
            map.put(NAME, name);
            map.put(PHOTO, photo);
            map.put(MESSAGE_PREVIEW, lastMsg);
            map.put(DATE, formatter.format(new Date(time)));
            mSessionList.add(map);
        }
        ((SimpleAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    // get user info
    private User getUserInfo(String imUsername) {
        return new User();
    }

    class User {
        /**
         * 用户名
         */
        public String username;
        /**
         * IM用户名
         */
        public String imUsername;

        /**
         * 姓名
         */
        public String name;
        /**
         * 头像URL
         */
        public String photo;

    }

}
