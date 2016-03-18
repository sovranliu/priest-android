package com.wehop.priest.business.user;

import com.slfuture.carrie.base.json.JSONVisitor;

/**
 * 医生信息
 */
public class Doctor extends User {
	private static final long serialVersionUID = 1L;

	/**
	 * 头衔
	 */
	public String title;
	/**
	 * 关系名
	 */
	public String relation;


	/**
	 * 构建医生
	 * 
	 * @param visitor
	 */
	public boolean parse(JSONVisitor visitor) {
		if(!super.parse(visitor)) {
			return false;
		}
		title = visitor.getString("title");
		relation = visitor.getString("relation");
		if(null == relation) {
			relation = "";
		}
		return true;
	}
}
