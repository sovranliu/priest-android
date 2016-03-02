package com.wehop.priest.business.core;

import com.slfuture.pluto.framework.annotation.ListenerInterface;

/**
 * 登录用户状态监听器
 */
@ListenerInterface
public interface IMeListener {
	/**
	 * 冲突掉线回调
	 */
	public void onConflict();

	/**
	 * 透传命令回调
	 * 
	 * @param from 消息投递者
	 * @param action 动作
	 * @param data 属性
	 */
	public void onCommand(String from, String action, com.slfuture.carrie.base.type.Table<String, Object> data);
}
