package com.wehop.priest.business.user;

import java.io.Serializable;

/**
 * 用户类
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 用户ID
	 */
	public String id;


	/**
	 * 构造函数
	 * 
     * @param id 用户ID
	 */
	public User() { }
	public User(String id) {
		this.id = id;
	}
}
