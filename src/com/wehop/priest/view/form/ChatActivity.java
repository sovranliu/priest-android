package com.wehop.priest.view.form;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.MessageBody;
import com.easemob.chat.TextMessageBody;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.wehop.priest.base.Logger;
import com.wehop.priest.view.form.SessionActivity.UserInfo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.wehop.priest.utils.FileUtils;

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
    private String mUser_imName = null;
    private String mUsername = null;

    private EMChatManager mChatManager = null;
    private EMGroupManager mGroupManager = null;
    private EMConversation mConversation = null;

    public static final int NEW_MESSAGE_COMING = 1;
    public static final int REFRESH_MESSAGE_LIST = 2;
    
    /**
     */
    protected BroadcastReceiver chatReceiver = null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        init();
        initData();
        //
        Program.register(this);
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        unregisterReceiver(chatReceiver);
        //
        Program.unregister(this);
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                }
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mSendVoiceButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                voiceCall();
            }
        });

        mSendVideoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
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
        mUser_imName = intent.getStringExtra(SessionActivity.USER_IM_NAME);
        mUsername = intent.getStringExtra(SessionActivity.USER_NAME);

        mGroupManager = EMGroupManager.getInstance();
        mChatManager = EMChatManager.getInstance();

        mMe = SessionActivity.getUserInfo(mChatManager.getCurrentUser());
        Log.i("gxl", "-------my IM name = " + mMe.imUsername);
        Log.i("gxl", "-------user im name = " + mUser_imName);
        Log.i("gxl", "-------user name " + mUsername);


    	chatReceiver = new BroadcastReceiver() {
           	@Override
           	public void onReceive(Context context, Intent intent) {
           		EMMessage emMessage = EMChatManager.getInstance().getMessage(intent.getStringExtra("msgid"));
    	        switch(emMessage.getType()) {
    	        case TXT:
    	        case IMAGE:
    	        	EMMessage message = emMessage;
                    Message msg = Message.obtain(mHander, NEW_MESSAGE_COMING);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("msg", message);
                    msg.setData(bundle);
                    msg.sendToTarget();
    	        	break;
    	        case VOICE:
    	        	break;
    	        case VIDEO:
    	        	break;
    	        default:
    	        	break;
    	        }
    	        abortBroadcast();
           	}
        };
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
    	intentFilter.setPriority(5);
    	registerReceiver(chatReceiver, intentFilter);
        // EMChat.getInstance().setAppInited();
    }
    
    Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEW_MESSAGE_COMING:
                    
                    EMMessage message = (EMMessage)msg.getData().getParcelable("msg");
                    onNewMessage(message);
                    break;
                case REFRESH_MESSAGE_LIST:
                    refreshList();
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
            switch (message.getType()) {
	            case TXT:
	            	data.setText(((TextMessageBody) message.getBody()).getMessage());
	                break;
	            case IMAGE:
	                ImageMessageBody imgBody= ((ImageMessageBody) message.getBody());
	                data.setThumbnailName(imgBody.getFileName());
	                data.setThumbnailUrl(imgBody.getThumbnailUrl());
	                break;
	            default:
	                break;
	        }
            String date = formatter.format(new Date(message.getMsgTime()));
            EMMessage.Direct direct = message.direct;

            data.setType(message.getType());
            data.setImName(message.getFrom());
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

    private void onNewMessage(EMMessage message) {
        MessageData data = new MessageData();
        MessageBody body = message.getBody();
        EMMessage.Direct direct = message.direct;
        switch (message.getType()) {
            case TXT:
            	data.setText(((TextMessageBody) message.getBody()).getMessage());
                break;
            case IMAGE:
                ImageMessageBody imgBody= ((ImageMessageBody) message.getBody());
                data.setThumbnailName(imgBody.getFileName());
                data.setThumbnailUrl(imgBody.getThumbnailUrl());
                break;
            default:
                break;
        }
        String date = formatter.format(new Date(message.getMsgTime()));
        data.setType(message.getType());
        data.setImName(message.getFrom());
        data.setDirection(direct);
        data.setDate(formatter.format(new Date(message.getMsgTime())));
        mMessageList.add(data);
        ((MessageAdapter) mListView.getAdapter()).notifyDataSetChanged();
        if (mMessageList.size() > 0) {
            mListView.setSelection(mMessageList.size() - 1);
        }
    }

    private void refreshList() {
        ((MessageAdapter) mListView.getAdapter()).notifyDataSetChanged();
        if (mMessageList.size() > 0) {
            mListView.setSelection(mMessageList.size() - 1);
        }
    }

    private void downloadImage(final String localFilePath, final String remoteFilePath, final Map<String, String> headers) {
        mChatManager.downloadFile(remoteFilePath, localFilePath, headers, new EMCallBack() {
            public void onSuccess() {
                Log.i("gxl", "download complete!  localPath = " + localFilePath);
                mHander.sendEmptyMessage(REFRESH_MESSAGE_LIST);
            }

            public void onError(int error, String msg) {
                Log.e("gxl", "offline file transfer error:" + msg);
                File file = new File(localFilePath);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }

            public void onProgress(final int progress, String status) {
                Log.d("gxl", "Progress: " + progress);
            }

        });
    }

    
    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                String path = FileUtils.getPath(this, uri);
                cursor.close();
                sendImageMessage(path);
            }
        } else if (requestCode == 2) {

        } else if (requestCode == 3) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void send(EMMessage message) {
    	InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mInputView.getWindowToken(), 0) ;
        
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

        onNewMessage(message);
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
        Intent intent = new Intent(ChatActivity.this, VoiceActivity.class);
        intent.putExtra("userImName", mUser_imName);
        intent.putExtra("userName", mUsername);
        intent.putExtra("mode", true);
        ChatActivity.this.startActivity(intent);
    }

    private void videoCall() {
        Intent intent = new Intent(ChatActivity.this, VideoActivity.class);
        intent.putExtra("userImName", mUser_imName);
        intent.putExtra("userName", mUsername);
        intent.putExtra("mode", true);
        ChatActivity.this.startActivity(intent);

    }

    private class MessageData {
        private EMMessage.Type type;

        private EMMessage.Direct direction;
        // 文字内容
        private String text;
        // 缩略图文件名
        private String thumbnailName;
        public String getThumbnailName() {
			return thumbnailName;
		}

		public void setThumbnailName(String thumbnailName) {
			this.thumbnailName = thumbnailName;
		}

		public String getThumbnailUrl() {
			return thumbnailUrl;
		}

		public void setThumbnailUrl(String thumbnailUrl) {
			this.thumbnailUrl = thumbnailUrl;
		}

		// 缩略图URL
        private String thumbnailUrl;

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

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
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
            }
            else {
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
                messageView.setText(data.getText());
            }
            else {
                messageView.setVisibility(View.GONE);
                fileView.setVisibility(View.VISIBLE);
            }
            dataView.setText(data.getDate());
            return convertView;
        }
    }

    /**
     * 绘制接收或发送的图片信息
     */
    public void drawImage(ImageView view, Bitmap image, String fileName, String fileURL) {
    	if(null != image) {
    		view.setImageBitmap(image);
    		return;
    	}
    	Host.doImage("image", new ImageResponse(fileName, view) {
			@Override
			public void onFinished(Bitmap content) {
				ImageView target = (ImageView) tag;
				target.setImageBitmap(content);
			}
    	}, fileURL);
    }
}
