package com.wehop.priest.business.user;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.model.core.ITargetEventable;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pretty.im.Module;

/**
 * 用户类
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 性别
	 */
	public final static int GENDER_UNKNOWN = 0;
	public final static int GENDER_MALE = 1;
	public final static int GENDER_FEMALE = 2;

	/**
	 * 用户ID
	 */
	public String id;
	/**
	 * 昵称
	 */
	public String nickname;
	/**
	 * 头像
	 */
	public String photo;
	/**
	 * 通信ID
	 */
	public String imId;
	/**
	 * 关系名
	 */
	public String relation;


	/**
	 * 获取头像位图
	 * 
	 * @return 头像位图
	 */
	public Bitmap photo() {
		if(Text.isBlank(photo)) {
			return null;
		}
		File file = new File(Host.storage + Host.parseFileNameWithURL(photo));
		if(file.exists()) {
			return GraphicsHelper.decodeFile(file);
		}
		return null;
	}
	
	/**
	 * 获取头像位图
	 * 
	 * @return 头像位图
	 */
	public <T>void photo(T tatget, ITargetEventable<T, Bitmap> event) {
		if(Text.isBlank(photo)) {
			event.on(tatget, null);
			return;
		}
		Host.doImage("image", tatget, event, photo);
	}

	/**
	 * 解析数据生成用户对象
	 * 
	 * @param visitor 数据
	 * @return 解析结果
	 */
	public boolean parse(JSONVisitor visitor) {
		id = visitor.getString("userGlobalId");
		nickname = visitor.getString("nickname");
		photo = visitor.getString("photo");
		imId = visitor.getString("imUsername");
		return true;
	}

	/**
	 * 获取未读消息个数
	 */
	public int unreadMessageCount() {
		return Module.getUnreadMessageCount(imId);
	}
}
