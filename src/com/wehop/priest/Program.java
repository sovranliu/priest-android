package com.wehop.priest;

import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.config.Configuration;
import com.slfuture.pretty.im.Module;
import com.wehop.priest.base.Logger;

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
		Logger.i("Program.onCreate() execute");
		super.onCreate();
		application = this;
		// 初始化配置系统
		Configuration.initialize(application);
		// 初始化IM组件
		Module.context = this;
		Module.initialize();
		// 初始化网络
		Networking.initialize(application);
    }

	/**
	 * 销毁回调
	 */
	@Override
	public void onTerminate() {
		Logger.i("Program.onTerminate() execute");
		super.onTerminate();
		// 关闭IM组件
		Module.terminate();
		// 关闭配置系统
		Configuration.terminate();
		// 关闭网络
		Networking.terminate();
	}
}
