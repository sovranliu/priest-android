package com.wehop.priest.business.structure;

import java.io.Serializable;

import com.slfuture.carrie.base.json.JSONVisitor;

/**
 * 即时通信
 */
public class IM implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 即时通信种类
	 */
	public final static String TYPE_PHONE = "phone";
	public final static String TYPE_TV = "tv";
	
	/**
	 * 标题
	 */
	public String title;
	/**
	 * 种类
	 */
	public String type;
	/**
	 * IM标志符
	 */
	public String imId;


	/**
	 * 解析数据生成对象
	 * 
	 * @param visitor 数据
	 * @return 解析结果
	 */
	public boolean parse(JSONVisitor visitor) {
		type = visitor.getString("type");
		title = visitor.getString("title");
		if(null == title) {
			if(IM.TYPE_TV.equals(type)) {
				title = "电视";
			}
			else {
				title = "手机";
			}
		}
		imId = visitor.getString("imUsername");
		return true;
	}
}
