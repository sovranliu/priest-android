package com.wehop.priest.view.form;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.wehop.priest.utils.GraphicHelper;

/**
 * 群聊窗口
 */
@ResourceView(id = R.layout.activity_groupchat)
public class GroupChatActivity extends ActivityEx {
	/**
	 * 聊天消息
	 */
	public class Message {
		/**
		 * 消息发送者ID
		 */
		private String sender = null;
		/**
		 * 消息发送者头像
		 */
		private Bitmap photo = null;
		/**
		 * 消息文字内容
		 */
		private String text = null;
		/**
		 * 消息图片下载地址
		 */
		private String url = null;
		/**
		 * 消息图片内容
		 */
		private Bitmap image = null;
		/**
		 * 图片文件
		 */
		private File file = null;


		public String getSender() {
			return sender;
		}
		public void setSender(String sender) {
			this.sender = sender;
		}
		public Bitmap getPhoto() {
			return photo;
		}
		public void setPhoto(Bitmap photo) {
			this.photo = photo;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public Bitmap getImage() {
			return image;
		}
		public void setImage(Bitmap image) {
			this.image = image;
		}
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
	}


	/**
	 * 对话列表适配器
	 */
	public class MessageListAdapter extends BaseAdapter {
		/**
		 * 消息列表
		 */ 
		private ArrayList<Message> messages;
		/**
		 * 渲染器
		 */
		private LayoutInflater inflater;


		/**
		 * 构造函数
		 * 
		 * @param context 上下文
		 * @param messages 消息列表
		 */
		public MessageListAdapter(Context context, ArrayList<Message> messages) {
			inflater = LayoutInflater.from(context);
			this.messages = messages;
		}

		public int getCount() {
			return messages.size();
		}

		public Object getItem(int position) {
			return messages.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getViewTypeCount() {
			return 2;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Message message = messages.get(position);
			ImageView messagePhoto = null;
			TextView messageText = null;
			ImageView messageImage = null;
			if(message.sender.equals(localId)) {
				convertView = inflater.inflate(R.layout.div_chat_right, null);
				messagePhoto = (ImageView) convertView.findViewById(R.id.chat_right_image_photo);
				messageText = (TextView) convertView.findViewById(R.id.chat_right_label_message);
				messageImage = (ImageView) convertView.findViewById(R.id.chat_right_image_message);
			}
			else {
				convertView = inflater.inflate(R.layout.div_chat_left, null);
				messagePhoto = (ImageView) convertView.findViewById(R.id.chat_left_image_photo);
				messageText = (TextView) convertView.findViewById(R.id.chat_left_label_message);
				messageImage = (ImageView) convertView.findViewById(R.id.chat_left_image_message);
			}
			messagePhoto.setImageBitmap(message.photo);
			if(null == message.image) {
				messageText.setText(message.getText());
				messageText.setVisibility(View.VISIBLE);
				messageImage.setVisibility(View.GONE);
			}
			else {
				messageText.setVisibility(View.GONE);
				messageImage.setImageBitmap(message.image);
				messageImage.setVisibility(View.VISIBLE);
				messageImage.setTag(position);
				messageImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int p = (Integer) v.getTag();
						if(p >= messages.size()) {
							return;
						}
						Message m = messages.get(p);
						Intent intent = new Intent(GroupChatActivity.this, ImageActivity.class);
						if(null != m.file) {
							intent.putExtra("path", m.file.getAbsolutePath());
						}
						else {
							intent.putExtra("url", m.url);
						}
						GroupChatActivity.this.startActivity(intent);
					}
				});
			}
			return convertView;
		}
	}


	/**
	 * 消息接收器
	 */
	public class MessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        EMMessage emMessage = EMChatManager.getInstance().getMessage(intent.getStringExtra("msgid"));
	        String sender = intent.getStringExtra("from");
	        if(localId.equals(sender)) {
	        	return;
	        }
	        Message message = new Message();
	        message.sender = sender;
	        message.photo = remotePhoto;
	        switch(emMessage.getType()) {
	        case TXT:
	        	TextMessageBody textBody = (TextMessageBody) emMessage.getBody();
	        	message.text = textBody.getMessage();
	        	drawMessage(message);
	        	break;
	        case IMAGE:
	        	ImageMessageBody imageBody = (ImageMessageBody) emMessage.getBody();
	        	Host.doImage("image", new ImageResponse(imageBody.getFileName(), message) {
					@Override
					public void onFinished(Bitmap content) {
						Message message = (Message) tag;
						message.image = content;
						drawMessage(message);
					}
				}, imageBody.getThumbnailUrl());
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
	}


	/**
	 * 控件
	 */
	@ResourceView(id = R.id.groupchat_button_close)
	public ImageButton btnClose;
	@ResourceView(id = R.id.groupchat_label_title)
	public TextView labTitle;
	@ResourceView(id = R.id.groupchat_listview_messages)
	public ListView listMessages;
	@ResourceView(id = R.id.groupchat_button_more)
	public ImageView btnMore;
	@ResourceView(id = R.id.groupchat_text_message)
	public EditText txtMessage;
	@ResourceView(id = R.id.groupchat_button_send)
	public ImageView btnSend;
	/**
	 * 数据
	 */
	private String localId = null;
	private String groupId = null;
	private String remoteId = null;
	private String remoteName = null;
	private Bitmap localPhoto = null;
	private Bitmap remotePhoto = null;
	private ArrayList<Message> messages = new ArrayList<Message>();
	private MessageListAdapter adapter = null;
	private EMConversation conversation = null;
	private MessageBroadcastReceiver receiver = null;

	
	/**
	 * 窗口构建
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//
    	prepare();
        //
        Program.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != receiver) {
			GroupChatActivity.this.unregisterReceiver(receiver);
		}
		receiver = null;
        //
        Program.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EMConversation conversation = EMChatManager.getInstance().getConversation(groupId);
        int msgCount = conversation.getUnreadMsgCount();
		if(msgCount > 0) {
			loadHistory();
		}
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1:
				if(RESULT_OK != resultCode || null == data) {
					return;
				}
				String imagePath = getImageAbsolutePath(this, data.getData());
				File imageFile = new File(imagePath);
				Message message = new Message();
				message.sender = localId;
				message.photo = localPhoto;
				message.image = GraphicsHelper.decodeFile(new File(imagePath), 200, 200);
				message.file = imageFile;
				send(message);
				break;
		}
    }

	public String getImageAbsolutePath(Context context, Uri imageUri) {  
	    if (context == null || imageUri == null)  
	        return null;  
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {  
	        if (isExternalStorageDocument(imageUri)) {  
	            String docId = DocumentsContract.getDocumentId(imageUri);  
	            String[] split = docId.split(":");  
	            String type = split[0];  
	            if ("primary".equalsIgnoreCase(type)) {  
	                return Environment.getExternalStorageDirectory() + "/" + split[1];  
	            }  
	        } else if (isDownloadsDocument(imageUri)) {  
	            String id = DocumentsContract.getDocumentId(imageUri);  
	            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));  
	            return getDataColumn(context, contentUri, null, null);  
	        } else if (isMediaDocument(imageUri)) {  
	            String docId = DocumentsContract.getDocumentId(imageUri);  
	            String[] split = docId.split(":");  
	            String type = split[0];  
	            Uri contentUri = null;  
	            if ("image".equals(type)) {  
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("video".equals(type)) {  
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("audio".equals(type)) {  
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  
	            }  
	            String selection = MediaStore.Images.Media._ID + "=?";  
	            String[] selectionArgs = new String[] { split[1] };  
	            return getDataColumn(context, contentUri, selection, selectionArgs);  
	        }  
	    } // MediaStore (and general)  
	    else if ("content".equalsIgnoreCase(imageUri.getScheme())) {  
	        // Return the remote address  
	        if (isGooglePhotosUri(imageUri))  
	            return imageUri.getLastPathSegment();  
	        return getDataColumn(context, imageUri, null, null);  
	    }  
	    // File  
	    else if ("file".equalsIgnoreCase(imageUri.getScheme())) {  
	        return imageUri.getPath();  
	    }  
	    return null;  
	}
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {  
	    Cursor cursor = null;  
	    String column = MediaStore.Images.Media.DATA;  
	    String[] projection = { column };  
	    try {  
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);  
	        if (cursor != null && cursor.moveToFirst()) {  
	            int index = cursor.getColumnIndexOrThrow(column);  
	            return cursor.getString(index);  
	        }  
	    } finally {  
	        if (cursor != null)  
	            cursor.close();  
	    }  
	    return null;  
	}  

	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is ExternalStorageProvider. 
	 */  
	public static boolean isExternalStorageDocument(Uri uri) {  
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());  
	}  
	  
	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is DownloadsProvider. 
	 */  
	public static boolean isDownloadsDocument(Uri uri) {  
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());  
	}  
	  
	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is MediaProvider. 
	 */  
	public static boolean isMediaDocument(Uri uri) {  
	    return "com.android.providers.media.documents".equals(uri.getAuthority());  
	}  
	  
	/** 
	 * @param uri The Uri to check. 
	 * @return Whether the Uri authority is Google Photos. 
	 */  
	public static boolean isGooglePhotosUri(Uri uri) {  
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());  
	}

	/**
	 * 准备视图
	 */
	public void prepare() {
		prepareParameter();
		prepareClose();
		prepareTitle();
		prepareMessages();
		prepareMore();
		prepareSend();
		prepareIM();
	}

    /**
     * 准备数据
     */
    private void prepareParameter() {
    	localId = getIntent().getStringExtra("localId");
    	groupId = getIntent().getStringExtra("groupId");
    	remoteId = getIntent().getStringExtra("remoteId");
    	remoteName = getIntent().getStringExtra("remoteName");
    	if(null == remoteName) {
    		remoteName = "对方";
    	}
    	String url = getIntent().getStringExtra("localPhoto");
    	if(null == url) {
    		localPhoto = BitmapFactory.decodeResource(GroupChatActivity.this.getResources(), R.drawable.user_photo_default);
    	}
    	else {
    		Host.doImage("image", new ImageResponse(url, null) {
    			@Override
				public void onFinished(Bitmap content) {
    				if(null == content) {
    					localPhoto = GraphicHelper.makeImageRing(GraphicHelper.makeCycleImage(BitmapFactory.decodeResource(GroupChatActivity.this.getResources(), R.drawable.user_photo_default), 200, 200), 4);
    					return;
    				}
    				localPhoto = GraphicHelper.makeImageRing(GraphicHelper.makeCycleImage(content, 200, 200), 4);
    			}
    		}, url);
    	}
    	url = getIntent().getStringExtra("remotePhoto");
    	if(null == url) {
    		remotePhoto = BitmapFactory.decodeResource(GroupChatActivity.this.getResources(), R.drawable.user_photo_default);
    	}
    	else {
    		Host.doImage("image", new ImageResponse(url, null) {
    			@Override
				public void onFinished(Bitmap content) {
    				remotePhoto = GraphicHelper.makeImageRing(GraphicHelper.makeCycleImage(content, 200, 200), 4);
				}
    		}, url);
    	}
    }

    /**
     * 准备IM
     */
    public void prepareIM() {
    	receiver = new MessageBroadcastReceiver();
    	IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
    	intentFilter.setPriority(3);
    	registerReceiver(receiver, intentFilter);
    	if(null != groupId) {
        	conversation = EMChatManager.getInstance().getConversation(groupId);
    	}
    	else {
        	conversation = EMChatManager.getInstance().getConversation(remoteId);
    	}
		loadHistory();
    }

    /**
     * 准备关闭按钮
     */
    private void prepareClose() {
    	btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GroupChatActivity.this.finish();
			}
        });
    }
    
    /**
     * 准备关闭按钮
     */
    private void prepareTitle() {
    	labTitle.setText(remoteName);
    }

    /**
     * 准备消息列表
     */
    private void prepareMessages() {
    	adapter = new MessageListAdapter(this, messages);
    	listMessages.setAdapter(adapter);
    }

    /**
     * 准备更多按钮
     */
    private void prepareMore() {
    	btnMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog alertDialog = new AlertDialog.Builder(GroupChatActivity.this).create();
				alertDialog.show();
				Window window = alertDialog.getWindow();
				WindowManager.LayoutParams layoutParams = window.getAttributes();
				layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
				window.setGravity(Gravity.BOTTOM);
				window.setAttributes(layoutParams);
				window.setContentView(R.layout.dialog_chat_more);
				TextView labelCancel = (TextView) window.findViewById(R.id.chatmore_label_cancel);
				labelCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						alertDialog.cancel();
					}
				});
				TextView labelImage = (TextView) window.findViewById(R.id.chatmore_label_image);
				labelImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						GroupChatActivity.this.startActivityForResult(intent, 1);
						alertDialog.hide();
					}
				});
				TextView labelAudio = (TextView) window.findViewById(R.id.chatmore_label_audio);
				labelAudio.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(GroupChatActivity.this, VoiceActivity.class);
						intent.putExtra("userId", remoteId);
						intent.putExtra("userName", remoteName);
						intent.putExtra("mode", true);
						GroupChatActivity.this.startActivity(intent);
						alertDialog.hide();
					}
				});
				TextView labelVideo = (TextView) window.findViewById(R.id.chatmore_label_video);
				labelVideo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(GroupChatActivity.this, VideoActivity.class);
						intent.putExtra("userId", remoteId);
						intent.putExtra("userName", remoteName);
						intent.putExtra("mode", true);
						GroupChatActivity.this.startActivity(intent);
						alertDialog.hide();
					}
				});
			}
        });
    }

    /**
     * 准备发送按钮
     */
    private void prepareSend() {
    	btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				send();
			}
        });
    }

    /**
     * 发送消息
     */
	public void send() {
        String text = txtMessage.getText().toString();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(txtMessage.getWindowToken(), 0) ;
        if(text.equals("")) {
        	Toast.makeText(GroupChatActivity.this, "请填写要发送的文字", Toast.LENGTH_LONG).show();
        	return;
        }
        txtMessage.setText("");
        //
        Message message = new Message();
        message.sender = localId;
        message.photo = localPhoto;
        message.text = text;
        send(message);
    }

    /**
     * 发送消息
     * 
     * @param message 消息对象
     */
	public void send(Message message) {
		EMMessage emMessage = null;
		if(null == message.image) {
			emMessage = EMMessage.createSendMessage(EMMessage.Type.TXT);
			emMessage.addBody(new TextMessageBody(message.text));
		}
		else {
			emMessage = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
			emMessage.addBody(new ImageMessageBody(message.file));
		}
		if(null == groupId) {
			emMessage.setReceipt(remoteId);
		}
		else {
			emMessage.setChatType(ChatType.GroupChat);
			emMessage.setReceipt(groupId);
		}
        conversation.addMessage(emMessage);
        try {
			EMChatManager.getInstance().sendMessage(emMessage);
		}
        catch (EaseMobException e) {
        	Log.e("Tower", "call send(?) failed", e);
        }
        drawMessage(message);
	}

	/**
	 * 加载历史记录
	 */
	public void loadHistory() {
    	List<EMMessage> emMessages = conversation.getAllMessages();
    	messages.clear();
    	for(EMMessage emMessage : emMessages) {
    		switch(emMessage.getType()) {
	        case TXT:
	        	TextMessageBody textBody = (TextMessageBody) emMessage.getBody();
	        	Message txtMessage = new Message();
	        	txtMessage.sender = emMessage.getFrom();
	        	if(txtMessage.sender.endsWith(localId)) {
	        		txtMessage.photo = localPhoto;
	        	}
	        	else {
	        		txtMessage.photo = remotePhoto;
	        	}
	        	txtMessage.text = textBody.getMessage();
	        	drawMessage(txtMessage);
	        	break;
	        case IMAGE:
	        	ImageMessageBody imageBody = (ImageMessageBody) emMessage.getBody();
	        	if(null == imageBody.getFileName() || null == imageBody.getThumbnailUrl()) {
	        		return;
	        	}
	        	Message imgMessage = new Message();
	        	if(null != imageBody.getLocalUrl() && (new File(imageBody.getLocalUrl())).exists()) {
	        		imgMessage.sender = emMessage.getFrom();
		        	if(imgMessage.sender.endsWith(localId)) {
		        		imgMessage.photo = localPhoto;
		        	}
		        	else {
		        		imgMessage.photo = remotePhoto;
		        	}
		        	imgMessage.file = new File(imageBody.getLocalUrl());
		        	imgMessage.image = GraphicsHelper.decodeFile(new File(imageBody.getLocalUrl()), 200, 200);
		        	drawMessage(imgMessage);
		        	continue;
	        	}
	        	if(null == imageBody.getThumbnailUrl() || "null".equals(imageBody.getThumbnailUrl())) {
	        		continue;
	        	}
	        	imgMessage.sender = emMessage.getFrom();
	        	if(imgMessage.sender.endsWith(localId)) {
	        		imgMessage.photo = localPhoto;
	        	}
	        	else {
	        		imgMessage.photo = remotePhoto;
	        	}
	        	imgMessage.url = imageBody.getThumbnailUrl();
	        	Host.doImage("image", new ImageResponse(imageBody.getFileName(), imgMessage) {
					@Override
					public void onFinished(Bitmap content) {
						Message imgMessage = (Message) tag;
			        	imgMessage.image = content;
			        	drawMessage(imgMessage);
					}
				}, imageBody.getThumbnailUrl());
	        	break;
	        case VOICE:
	        	Message voiceMessage = new Message();
	        	voiceMessage.sender = emMessage.getFrom();
	        	if(voiceMessage.sender.endsWith(localId)) {
	        		voiceMessage.photo = localPhoto;
	        	}
	        	else {
	        		voiceMessage.photo = remotePhoto;
	        	}
	        	voiceMessage.text = "语音通话...";
	        	drawMessage(voiceMessage);
	        	break;
	        case VIDEO:
	        	Message videoMessage = new Message();
	        	videoMessage.sender = emMessage.getFrom();
	        	if(videoMessage.sender.endsWith(localId)) {
	        		videoMessage.photo = localPhoto;
	        	}
	        	else {
	        		videoMessage.photo = remotePhoto;
	        	}
	        	videoMessage.text = "视频通话...";
	        	drawMessage(videoMessage);
	        default:
	        	break;
	        }
    	}
    	EMConversation conversation = EMChatManager.getInstance().getConversation(groupId);
    	conversation.markAllMessagesAsRead();
	}

	/**
	 * 渲染消息
	 * 
	 * @param message 消息对象
	 */
	public void drawMessage(Message message) {
        messages.add(message);
        adapter.notifyDataSetChanged();
        listMessages.setSelection(listMessages.getCount() - 1);
	}
}
