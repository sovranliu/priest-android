package com.wehop.priest.business.user;

import java.io.Serializable;

/**
 * 当前登陆用户
 */
public class Me extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 用户名
	 */
	public String username;
	/**
	 * 登录密码
	 */
	public String password;
	/**
     * IM用户名
     */
    public String imUsername;
    /**
     * IM登录密码
     */
    public String imPassword;

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


	public Me() { }
	/**
	 * 构造函数
	 * 
	 * @param id 用户ID
	 * @param username 用户名
	 * @param imUsername im用户名
	 * @param name 姓名
	 * @param photo 头像URL
	 * @param token 口令
	 */
	public Me(String id, String username, String password, String imUsername, String name, String photo, String token) {
		super(id);
		this.username = username;
		this.password = password;
		this.imUsername = imUsername;
		this.name = name;
		this.photo = photo;
		this.token = token;
		
		this.imPassword = this.username;
	}
}
