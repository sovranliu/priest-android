package com.wehop.priest;

import java.util.LinkedList;
import java.util.List;

import com.easemob.chat.EMChat;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.Logic;
import com.wehop.priest.framework.Environment;

import android.app.Activity;
import android.app.Application;

/**
 * 应用类
 */
public class Program extends Application {
	/**
	 * 程序ID
	 */
	public final static String ID = "priest";
	/**
	 * 程序引用
	 */
	public static Application application = null;
    /**
     * 系统Activity列表
     */
	public static List<Activity> systemActivities = new LinkedList<Activity>();  


	/**
	 * 构建回调
	 */
	@Override
    public void onCreate() {
		Logger.i("Program.onCreate() start");
		super.onCreate();
		
	    EMChat.getInstance().init(this);
	    EMChat.getInstance().setDebugMode(true);

		Program.application = this;
		// 初始化框架
		Environment.initialize();
		// 初始化逻辑
		Logic.initialize();
		Logger.i("Program.onCreate() end");
    }

	/**
	 * 终结回调
	 */
	@Override
	public void onTerminate() {
		Logger.i("Program.onTerminate() start");
		super.onTerminate();
		// 终结配置
		Logic.terminate();
		// 终结框架
		Environment.terminate();
		Program.application = null;
		Logger.i("Program.onTerminate() end");
	}

	/**
	 * 注册窗口
	 * 
	 * @param activity 窗口
	 */
	public static void register(Activity activity) {
		systemActivities.add(activity);
	}

	/**
	 * 解除注册窗口
	 * 
	 * @param activity 窗口
	 */
	public static void unregister(Activity activity) {
		for(int i = systemActivities.size() - 1; i >= 0; i--) {
			Activity item = systemActivities.get(i);
			if(item == activity) {
				systemActivities.remove(systemActivities);
				break;
			}
		}
	}
	
	/**
	 * 清理所有窗口
	 */
	public static void exit() {
		for(int i = systemActivities.size() - 1; i >= 0; i--) {
			Activity item = systemActivities.get(i);
			systemActivities.remove(i);
			item.finish();
		}
	}
}
