package com.wehop.priest.view.form;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.R;

import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 浏览器页
 */
@ResourceView(id = R.layout.activity_message)
public class MessageActivity extends ActivityEx {

    private class Message {
        int msgType = 0;// 0 text, 1 image, 2 audio, 3 video
        String textMsg = null;
        String filePath = null;
        Message(String content, int type) {
            msgType = type;
            if(type == 0) {
                textMsg = content;
            } else {
                filePath = content;
            }
        }
    }
    // public static final String NAME = "name";
    
    public static final String PHOTO = "photo";
    
    public static final String MESSAGE = "message";
    
    public static final String DATE = "date";

    @ResourceView(id = R.id.return_button)
    public ImageView mReturnButton = null;

    @ResourceView(id = R.id.message_list)
    public ListView mListView = null;

    @ResourceView(id = R.id.more_options_button)
    public ImageButton mMoreOptionsButton = null;

    @ResourceView(id = R.id.send_message_button)
    public ImageButton mSendMessageButton = null;

    @ResourceView(id = R.id.more_options_holder)
    public View mMoreOptionsHolder = null;

    @ResourceView(id = R.id.send_image)
    public ImageButton mSendImageButton = null;

    @ResourceView(id = R.id.send_audio)
    public ImageButton mSendAudioButton = null;

    @ResourceView(id = R.id.send_video)
    public ImageButton mSendVideoButton = null;

    private List<MessageData> mMessageList = new ArrayList<MessageData>();

    private String contactGlobalId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mReturnButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MessageActivity.this.finish();
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

        mSendImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        mSendAudioButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        mSendVideoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
        
        mSendMessageButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
            }
        });

        MessageAdapter adapter = new MessageAdapter(this, mMessageList);
        mListView.setAdapter(adapter);
        // loadData();
        fakeData();
        adapter.notifyDataSetChanged();
    }

    private void loadData() {
        Intent intent = this.getIntent();
        contactGlobalId = intent.getStringExtra(SessionActivity.GLOBAL_ID);

    }

    private void fakeData() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        MessageData data = new MessageData();
        data.setMySelf(false);
        data.setName("小王");
        data.setMessage("昨天的药已经吃完了，什么时候再去拿一些~");
        data.setDate(formatter.format(new Date()));
        data.setPhoto(null);

        MessageData data1 = new MessageData();
        data1.setMySelf(true);
        data1.setName("李医生");
        data1.setMessage("好的，过来拿吧，记得要交费！");
        data1.setDate(formatter.format(new Date()));
        data1.setPhoto(null);

        mMessageList.clear();
        mMessageList.add(data);
        mMessageList.add(data1);
    }

    private void sendMessage(Message message) {
        
    }
    
    private class MessageData {
        private boolean isMySelf;

        private String name;

        // URI of photo
        private Uri photo;

        private String message;

        private String date;

        public boolean getIsMySelf() {
            return isMySelf;
        }

        public void setMySelf(boolean isMySelf) {
            this.isMySelf = isMySelf;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Uri getPhoto() {
            return photo;
        }

        public void setPhoto(Uri photo) {
            this.photo = photo;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
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
            TextView dataView = null;
            if (data.getIsMySelf()) {
                receivedGroup.setVisibility(View.GONE);
                sentGroup.setVisibility(View.VISIBLE);
                photoView = (ImageView) convertView.findViewById(R.id.sent_contact_photo);
                messageView = (TextView) convertView.findViewById(R.id.sent_message);
                dataView = (TextView) convertView.findViewById(R.id.sent_time);
            } else {
                receivedGroup.setVisibility(View.VISIBLE);
                sentGroup.setVisibility(View.GONE);
                photoView = (ImageView) convertView.findViewById(R.id.received_contact_photo);
                messageView = (TextView) convertView.findViewById(R.id.received_message);
                dataView = (TextView) convertView.findViewById(R.id.received_time);
            }

            if (data.getPhoto() == null) {
                photoView.setImageResource(R.drawable.photo_default);
            } else {
                photoView.setImageURI(data.getPhoto());
            }
            messageView.setText(data.getMessage());
            dataView.setText(data.getDate());
            return convertView;
        }

    }
}
