package com.wehop.priest.business.user;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.text.Text;

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
	 * 获取合适称呼
	 * 
	 * @return 合适称呼
	 */
	@Override
	public String nickname() {
		if(!Text.isBlank(relation)) {
			return relation;
		}
		return super.nickname();
	}

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
