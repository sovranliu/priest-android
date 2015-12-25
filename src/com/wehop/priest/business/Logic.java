package com.wehop.priest.business;

import java.io.File;
import java.io.IOException;

import com.slfuture.carrie.base.etc.Serial;
import com.wehop.priest.base.Logger;
import com.wehop.priest.business.user.Me;
import com.wehop.priest.framework.Storage;

/**
 * 当前运行时
 */
public class Logic {
	/**
	 * 当前登录用户
	 */
	public static Me user = null;

	public static boolean imLogin = false;

	/**
	 * 初始化
	 * 
	 * @return 执行结果
	 */
	public static boolean initialize() {
		File meFile = Storage.getDataFile("me.dat");
		if(meFile.exists()) {
			try {
				Logic.user = Serial.extract(meFile, Me.class);
			}
			catch (Exception ex) {
				Logger.e("extract user information failed", ex);
			}
		}
		return true;
	}

	/**
	 * 终结
	 */
	public static void terminate() {
		user = null;
	}

	/**
	 * 登录
	 * 
	 * @param id 用户ID
	 * @param username 用户名
	 * @param password 密码
	 * @param name 用户姓名
	 * @param photo 头像
	 * @param token 访问口令
	 */
	public static void login(int id, String username, String password, String name, String photo, String token) {
		user = new Me(id, username, password, name, photo, token);
		save();
	}

	/**
	 * 保存用户信息
	 */
	public static void save() {
		if(null == user) {
			Storage.getDataFile("me.dat").delete();
			return;
		}
		try {
			Serial.restore(user, Storage.getDataFile("me.dat"));
		}
		catch (IOException e) {
			Logger.e("restore user information failed", e);
		}
	}
}
