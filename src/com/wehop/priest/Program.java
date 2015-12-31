package com.wehop.priest;

import com.easemob.chat.EMChat;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.Logic;
import com.wehop.priest.framework.Environment;

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
}
