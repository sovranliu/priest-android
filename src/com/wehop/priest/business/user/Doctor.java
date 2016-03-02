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
	 * 科室
	 */
	public String department;
	/**
	 * 简历
	 */
	public String resume;
	/**
	 * 自我介绍
	 */
	public String description;


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
		department = visitor.getString("department");
		resume = visitor.getString("resume");
		description = visitor.getString("description");
		return true;
	}
}
