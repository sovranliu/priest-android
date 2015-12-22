package com.wehop.priest.business.user;

import java.io.Serializable;

/**
 * 当前登陆用户
 */
public class Me extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 手机号码
	 */
	public String username;
	/**
	 * 登录密码
	 */
	public String password;
	/**
	 * 姓名
	 */
	public String name;
	/**
	 * 头像URL
	 */
	public String photo;
	/**
	 * 操作口令
	 */
	public String token;


	/**
	 * 构造函数
	 * 
     * @param id 用户ID
	 * @param phone 手机号码
	 * @param password 密码
	 * @param name 姓名
	 * @param photo 头像URL
	 * @param token 口令
	 */
	public Me() { }
	public Me(int id, String username, String password, String name, String photo, String token) {
		super(id);
		this.username = username;
		this.password = password;
		this.name = name;
		this.photo = photo;
		this.token = token;
	}
}
