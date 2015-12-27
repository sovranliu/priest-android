package com.wehop.priest.view.form;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import com.easemob.EMCallBack;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.MessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.chat.core.s;
import com.easemob.chat.EMChatConfig.EMEnvMode;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.R;
import com.wehop.priest.base.Logger;
import com.wehop.priest.framework.Storage;
import com.wehop.priest.view.form.SessionActivity.UserInfo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller.Session;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethod;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 浏览器页
 */
@ResourceView(id = R.layout.activity_message)
public class ChatActivity extends ActivityEx {

    @ResourceView(id = R.id.return_button)
    public ImageView mReturnButton = null;

    @ResourceView(id = R.id.message_list)
    public ListView mListView = null;

    @ResourceView(id = R.id.more_options_button)
    public ImageButton mMoreOptionsButton = null;

    @ResourceView(id = R.id.send_message_button)
    public ImageButton mSendMessageButton = null;

    @ResourceView(id = R.id.input_frame)
    public EditText mInputView = null;

    @ResourceView(id = R.id.more_options_holder)
    public View mMoreOptionsHolder = null;

    @ResourceView(id = R.id.send_image)
    public ImageButton mSendImageButton = null;

    @ResourceView(id = R.id.send_voice)
    public ImageButton mSendVoiceButton = null;

    @ResourceView(id = R.id.send_video)
    public ImageButton mSendVideoButton = null;

    private List<MessageData> mMessageList = new ArrayList<MessageData>();

    private UserInfo mMe = null;
    private List<UserInfo> mUserList = new ArrayList<SessionActivity.UserInfo>();

    private String mGroupId = null;
    // Group中唯一的client用户的 IM 名
    private String mClientUser_imName = null;

    private EMChatManager mChatManager = null;
    private EMGroupManager mGroupManager = null;
    private EMConversation mConversation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        init();
        initData();
    }

    private void init() {
        mReturnButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ChatActivity.this.finish();
            }
        });

        mMoreOptionsButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMoreOptionsHolder.getVisibility() == View.GONE) {
                    mMoreOptionsHolder.setVisibility(View.VISIBLE);
                } else {
                    mMoreOptionsHolder.setVisibility(View.GONE);
                }
            }
        });

        mSendMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendTextMessage(mInputView.getText().toString());
                mInputView.setText("");

            }
        });

        mSendImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);

            }
        });

        mSendVoiceButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = new Intent();
                // intent.setType("audio/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(intent, 2);
                voiceCall();
            }
        });

        mSendVideoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = new Intent();
                // intent.setType("video/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(intent, 3);
                videoCall();
            }
        });

        MessageAdapter adapter = new MessageAdapter(this, mMessageList);
        mListView.setAdapter(adapter);

        initEmchat();
    }

    private void initEmchat() {
        // mChatManager.deleteConversation(mUsername);
        Intent intent = this.getIntent();
        mGroupId = intent.getStringExtra(SessionActivity.GROUP_ID);
        mClientUser_imName = intent.getStringExtra(SessionActivity.CLIENT_IM_NAME);

        mGroupManager = EMGroupManager.getInstance();
        mChatManager = EMChatManager.getInstance();

        mMe = SessionActivity.getUserInfo(mChatManager.getCurrentUser());
        Log.i("gxl", "-------current user = " + mMe.imUsername);
        Log.i("gxl", "-------mClient im name = " + mClientUser_imName);

        mChatManager.registerEventListener(new EMEventListener() {

            @Override
            public void onEvent(EMNotifierEvent event) {
                Log.i("gxl", "-------event = " + event);
                switch (event.getEvent()) {
                    case EventNewMessage:
                        EMMessage message = (EMMessage) event.getData();
                        // EMConversation conversation =
                        // mChatManager.getConversation(mUsername);
                        // conversation.addMessage(message);
                        // mChatManager.saveMessage(message);
                        // mChatManager.loadAllConversations();
//                        refreshData(message);
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("msg", message);
                        msg.setData(bundle);
                        mHander.sendMessage(msg);
                        //
                        break;
                    case EventDeliveryAck:
                        break;
                    case EventNewCMDMessage:
                        //
                        break;
                    case EventReadAck:

                        break;
                    case EventOfflineMessage:
                        List<EMMessage> messages = (List<EMMessage>) event.getData();
                        //
                        break;
                    case EventConversationListChanged:
                        break;
                    default:
                        break;
                }
            }
        });

        // EMChat.getInstance().setAppInited();
    }
    
    Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    
                    EMMessage message = (EMMessage)msg.getData().getParcelable("msg");
                    onMessageAdd(message);
                    break;

                default:
                    break;
            }
        }
        
    };

    private void initData() {
        // EMGroup group = mGroupManager.getGroup(mGroupId);
        mConversation = mChatManager.getConversation(mGroupId);
        List<EMMessage> messages = mConversation.getAllMessages();
        Log.i("gxl", "---- messages.size = " + messages.size() + ", ,msg count = " + mConversation.getMsgCount());
        mMessageList.clear();
        for (EMMessage message : messages) {
            MessageData data = new MessageData();
            MessageBody body = message.getBody();
            String msg = null;
            switch (message.getType()) {
                case TXT:
                    msg = ((TextMessageBody) message.getBody()).getMessage();
                    break;
                case IMAGE:
                    msg = ((ImageMessageBody) message.getBody()).getLocalUrl();
                    break;
                default:
                    break;
            }
            String date = formatter.format(new Date(message.getMsgTime()));
            EMMessage.Direct direct = message.direct;

            data.setType(message.getType());
            data.setImName(message.getFrom());
            data.setContent(msg);
            data.setDirection(direct);
            data.setDate(formatter.format(new Date(message.getMsgTime())));
            mMessageList.add(data);
        }

        ((MessageAdapter) mListView.getAdapter()).notifyDataSetChanged();
        // ((MessageAdapter) mListView.getAdapter()).notifyDataSetInvalidated();
        if (mMessageList.size() > 0) {
            mListView.setSelection(mMessageList.size() - 1);
        }
    }
    public static String getImagePath(String remoteUrl)
    {
        String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
        String path =PathUtil.getInstance().getImagePath()+"/"+ imageName;
        Log.d("gxl", "image path:" + path);
        return path;
        
    }

    private void onMessageAdd(EMMessage message) {
        MessageData data = new MessageData();
        MessageBody body = message.getBody();
        String msg = null;
        switch (message.getType()) {
            case TXT:
                msg = ((TextMessageBody) message.getBody()).getMessage();
                break;
            case IMAGE:
                ImageMessageBody imgBody= ((ImageMessageBody) message.getBody());
                
                String path = imgBody.getLocalUrl();
                msg = path;
                break;
            default:
                break;
        }
        Log.i("gxl", "From = " + message.getFrom() + ", To = " + message.getTo() + ", userName = " + message.getUserName());
        String date = formatter.format(new Date(message.getMsgTime()));
        EMMessage.Direct direct = message.direct;

        data.setType(message.getType());
        data.setImName(message.getFrom());
        data.setContent(msg);
        data.setDirection(direct);
        data.setDate(formatter.format(new Date(message.getMsgTime())));
        mMessageList.add(data);

        ((MessageAdapter) mListView.getAdapter()).notifyDataSetChanged();
        if (mMessageList.size() > 0) {
            mListView.setSelection(mMessageList.size() - 1);
        }
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                String path = cursor.getString(1); // 图片文件路径
                cursor.close();
                sendImageMessage(path);
            }
        } else if (requestCode == 2) {

        } else if (requestCode == 3) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void send(EMMessage message) {
        mChatManager.sendMessage(message, new EMCallBack() {

            @Override
            public void onSuccess() {
                Log.i("gxl", "send onSuccess");
            }

            @Override
            public void onProgress(int arg0, String arg1) {
                Log.i("gxl", "send onProgress");
            }

            @Override
            public void onError(int arg0, String arg1) {
                Log.i("gxl", "send onError  : " + arg0 + ", " + arg1);
            }
        });

        onMessageAdd(message);
    }

    private void sendTextMessage(String text) {
        if (text == null || text.equals("")) {
            return;
        }
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
        TextMessageBody body = new TextMessageBody(text);
        message.addBody(body);
        message.setChatType(ChatType.GroupChat);
        message.setFrom(mMe.imUsername);
        message.setReceipt(mGroupId);
        mConversation.addMessage(message);
        send(message);
    }

    private void sendImageMessage(String path) {
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
        File file = new File(path);
        if (!file.exists()) {
            Logger.i("file [" + path + "] is not existing.");
        }
        ImageMessageBody body = new ImageMessageBody(new File(path));
        message.addBody(body);
        message.setChatType(ChatType.GroupChat);
        message.setFrom(mMe.imUsername);
        message.setReceipt(mGroupId);
        mConversation.addMessage(message);
        send(message);
    }

    private void voiceCall() {
    }

    private void videoCall() {
    }

    private class MessageData {
        private EMMessage.Type type;

        private EMMessage.Direct direction;
        // Text message or File Path
        private String content;

        private String date;

        private String imName;

        public EMMessage.Type getType() {
            return type;
        }

        public void setType(EMMessage.Type type) {
            this.type = type;
        }

        public EMMessage.Direct getDirection() {
            return direction;
        }

        public void setDirection(EMMessage.Direct direct) {
            this.direction = direct;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getImName() {
            return imName;
        }

        public void setImName(String imName) {
            this.imName = imName;
        }
    }

    private class MessageAdapter extends BaseAdapter {

        private Context mContext;

        private LayoutInflater mInflater;

        private List<MessageData> mData;

        public MessageAdapter(Context context, List<MessageData> data) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mData = data;
        }

        @Override
        public int getCount() {
            if (mData != null) {
                return mData.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mData != null) {
                return mData.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.message_list_item, null);
            }

            MessageData data = mData.get(position);

            View receivedGroup = convertView.findViewById(R.id.received_message_item);
            View sentGroup = convertView.findViewById(R.id.sent_message_item);
            ImageView photoView = null;
            TextView messageView = null;
            ImageView fileView = null;
            TextView dataView = null;
            if (data.getDirection().equals(EMMessage.Direct.SEND)) {
                receivedGroup.setVisibility(View.GONE);
                sentGroup.setVisibility(View.VISIBLE);
                photoView = (ImageView) convertView.findViewById(R.id.sent_contact_photo);
                messageView = (TextView) convertView.findViewById(R.id.sent_message);
                fileView = (ImageView) convertView.findViewById(R.id.sent_file);
                dataView = (TextView) convertView.findViewById(R.id.sent_time);

                photoView.setImageResource(R.drawable.photo_default);
                // if (mUser_photo == null) {
                // photoView.setImageResource(R.drawable.photo_default);
                // } else {
                // photoView.setImageURI(Uri.fromFile(new File(mMy_photo)));
                // }
            } else {
                receivedGroup.setVisibility(View.VISIBLE);
                sentGroup.setVisibility(View.GONE);
                photoView = (ImageView) convertView.findViewById(R.id.received_contact_photo);
                messageView = (TextView) convertView.findViewById(R.id.received_message);
                fileView = (ImageView) convertView.findViewById(R.id.received_file);
                dataView = (TextView) convertView.findViewById(R.id.received_time);

                photoView.setImageResource(R.drawable.photo_default);
                // if (mUser_photo == null) {
                // photoView.setImageResource(R.drawable.photo_default);
                // } else {
                // photoView.setImageURI(Uri.fromFile(new File(mUser_photo)));
                // }
            }

            if (data.getType() == EMMessage.Type.TXT) {
                messageView.setVisibility(View.VISIBLE);
                fileView.setVisibility(View.GONE);
                messageView.setText(data.getContent());
            } else {
                messageView.setVisibility(View.GONE);
                fileView.setVisibility(View.VISIBLE);
                if (data.getType() == EMMessage.Type.IMAGE) {
                    File file = new File(data.getContent());
                    Log.i("gxl", "file path = " + data.getContent());
                    if (file.exists()) {
                        Log.i("gxl", "file exists");
                    } else {
                        Log.i("gxl", "file not exist");
                    }
                    Bitmap bitmap = BitmapFactory.decodeFile(data.getContent());
                    fileView.setImageBitmap(bitmap);
                } else {
                    messageView.setText(data.getContent());
                }
            }
            dataView.setText(data.getDate());
            return convertView;
        }

    }
}
