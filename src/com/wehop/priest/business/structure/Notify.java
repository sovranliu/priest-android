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
	// 好友删除通知
	public final static int TYPE_4 = 4;
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
	 * 添加人
	 */
	public int name;
	/**
	 * 添加关系
	 */
	public int relation;
	/**
	 * 消息标题
	 */
	public String title;
	/**
	 * 消息标题
	 */
	public String description;
	/**
	 * 是否已读
	 */
	public boolean hasRead;
	/**
	 * 消息类型
	 */
    public int type;
	/**
	 * 消息来源类型：doctor, patient
	 */
    public String source;
    /**
     * 消息时间
     */
    public String time;
}
