package com.wehop.priest.business.structure.notify;

import java.io.Serializable;
import java.text.ParseException;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.time.DateTime;

/**
 * 通知数据
 */
public class Notify implements Serializable {
	private static final long serialVersionUID = -1;

	/**
	 * 未知通知类型
	 */
	public final static int TYPE_UNKNOWN = 0;


	/**
	 * 获取通知类型
	 * 
	 * @return 通知类型
	 */
	public int type() {
		return TYPE_UNKNOWN;
	}

	/**
	 * 通知ID
	 */
	public int id;
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
     * 消息投递时间
     */
    public DateTime time;


    /**
     * 解析
     * 
     * @parse data 数据
     * @return 是否解析成功
     */
    public boolean parse(JSONVisitor data) {
    	id = data.getInteger("id", 0);
    	if(0 == id) {
    		return false;
    	}
    	title = data.getString("title");
    	if(null == title) {
    		return false;
    	}
    	description = data.getString("description");
    	if(null == description) {
    		description = "";
    	}
    	hasRead = data.getBoolean("hasRead", true);
    	if(null != data.getString("time")) {
    		try {
    			time = DateTime.parse(data.getString("time"));
    		}
        	catch (ParseException e) { }
    	}
    	if(null == time) {
    		time = DateTime.now();
    	}
    	return true;
    }
}
