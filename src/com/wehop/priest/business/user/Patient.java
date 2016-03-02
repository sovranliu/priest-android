package com.wehop.priest.business.user;

import java.text.ParseException;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.time.Date;

/**
 * 病人信息
 */
public class Patient extends User {
	private static final long serialVersionUID = 1L;

	/**
	 * 出生年月
	 */
	public Date birthday;
	/**
	 * 性别
	 */
	public int gender = GENDER_UNKNOWN;
	
	
	/**
	 * 解析数据生成用户对象
	 * 
	 * @param visitor 数据
	 * @return 解析结果
	 */
	public boolean parse(JSONVisitor visitor) {
		if(!super.parse(visitor)) {
			return false;
		}
		if(null != visitor.getString("birthday")) {
			try {
				birthday = Date.parse(visitor.getString("birthday"));
			}
			catch (ParseException e) { }
		}
		gender = visitor.getInteger("gender", 0);
		return true;
	}
}
