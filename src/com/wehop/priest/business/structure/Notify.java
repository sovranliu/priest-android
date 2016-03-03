package com.wehop.priest.business.structure;

import java.io.Serializable;

/**
 * 通知数据
 */
public class Notify implements Serializable {
	private static final long serialVersionUID = -1;

	// 好友添加请求
	public final static int TYPE_1 = 1;
	// 好友接受通知
	public final static int TYPE_2 = 2;
	// 好友拒绝通知
	public final static int TYPE_3 = 3;
	/**
	 * 消息来源类型
	 */
	public final static String SOURCE_DOCTOR = "doctor";
	public final static String SOURCE_PATIENT = "patient";
	

	/**
	 * 通知ID
	 */
	public int id;
	/**
	 * 消息类型
	 */
    public int type;
	/**
	 * 消息来源类型：doctor, patient
	 */
    public String source;
}
