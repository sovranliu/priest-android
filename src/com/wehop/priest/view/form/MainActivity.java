package com.wehop.priest.view.form;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.OnNotificationClickListener;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentActivityEx;
import com.wehop.priest.R;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.Logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

/**
 * 主界面
 */
@ResourceView(id = R.layout.activity_main)
public class MainActivity extends FragmentActivityEx {
	/**
	 * 选项卡个数
	 */
    public final static int TAB_COUNT = 3;
	
    /**
     * 拨号接收器
     */
    private BroadcastReceiver dialReceiver = null;
	/**
     * 选项卡对象
     */
    @ResourceView(id = R.id.main_tabhost)
    public TabHost tabhost = null;
    @ResourceView(id = R.id.main_tab_home)
    public RadioButton btnHome = null;
    @ResourceView(id = R.id.main_tab_task)
    public RadioButton btnTask = null;
    @ResourceView(id = R.id.main_tab_user)
    public RadioButton btnUser = null;

    /**
     * 界面创建
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("call MainActivity.onCreate()");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // 
        tabhost.setup();
        tabhost.addTab(tabhost.newTabSpec("main_tab_home").setIndicator("").setContent(R.id.main_fragment_home));
        tabhost.addTab(tabhost.newTabSpec("main_tab_task").setIndicator("").setContent(R.id.main_fragment_task));
        tabhost.addTab(tabhost.newTabSpec("main_tab_user").setIndicator("").setContent(R.id.main_fragment_user));
        RadioGroup group = (RadioGroup)findViewById(R.id.main_tab);
        group.check(R.id.main_tab_home);
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                TabHost tabhost = (TabHost)findViewById(R.id.main_tabhost);
                switch(checkedId) {
                    case R.id.main_tab_home:
                        tabhost.setCurrentTabByTag("main_tab_home");
                        break;
                    case R.id.main_tab_task:
                        tabhost.setCurrentTabByTag("main_tab_task");
                        break;
                    case R.id.main_tab_user:
                        tabhost.setCurrentTabByTag("main_tab_user");
                        break;
                }
            }
        });
        tabhost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
		        for(int i = 0; i < TAB_COUNT; i++) {
					if("main_tab_home".equals(tabId)) {
			        	final TextView txtTitle = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			        	txtTitle.setText("ceshi");
			        	txtTitle.setTextColor(Color.GREEN);
					}
		        }
			}
        });
        EMChatManager.getInstance().login(Logic.user.imUsername, Logic.user.imPassword, new EMCallBack() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.i("gxl", "----im login success --- ");
                EMChatManager.getInstance().loadAllConversations();
                EMGroupManager.getInstance().loadAllGroups();
                Logic.imLogin = true;
            }
            
            @Override
            public void onProgress(int arg0, String arg1) {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                Log.i("gxl", "----im login onError --- " + arg0 + " ,  " + arg1);
            }
        });
        dialReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String from = intent.getStringExtra("from");
                String type = intent.getStringExtra("type");
                Log.i("gxl", "intent = " + intent.getAction());
                Log.i("gxl", "from = " + from + ", type = " + type);
                Intent ringIntent = new Intent(MainActivity.this, RingActivity.class);
                ringIntent.putExtra("userId", from);
                ringIntent.putExtra("userName", from);
                ringIntent.putExtra("type", type);
                MainActivity.this.startActivity(ringIntent);
            }
        };
        registerReceiver(dialReceiver, new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction()));
        EMChat.getInstance().setAppInited();
        EMChatOptions option = new EMChatOptions();
        option.setNotificationEnable(true);
        option.setOnNotificationClickListener(new OnNotificationClickListener() {
            @Override
            public Intent onNotificationClick(EMMessage msg) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                return null;
            }
        });
        EMChatManager.getInstance().setChatOptions(option);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(dialReceiver);
        super.onDestroy();
    }
}
