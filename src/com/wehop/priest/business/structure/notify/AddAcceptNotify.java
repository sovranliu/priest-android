package com.wehop.priest.business.structure.notify;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.wehop.priest.business.user.User;

/**
 * 好友添加反馈通知
 */
public class AddAcceptNotify extends Notify {
	private static final long serialVersionUID = 1L;

	/**
	 * 通知类型
	 */
	public final static int TYPE_ADDACCEPT = 5;

	/**
	 * 反馈者手机号码
	 */
	public String targetPhone = null;
	/**
	 * 反馈者昵称
	 */
	public String targetNickname = null;
	/**
	 * 反馈者身份类型
	 */
	public String targetCategory = null;
	/**
	 * 请求者欲添加的关系
	 */
	public String relation;


	/**
	 * 获取通知类型
	 * 
	 * @return 通知类型
	 */
	@Override
	public int type() {
		return TYPE_ADDACCEPT;
	}

    /**
     * 解析
     * 
     * @parse data 数据
     * @return 是否解析成功
     */
    public boolean parse(JSONVisitor data) {
    	if(!super.parse(data)) {
    		return false;
    	}
    	data = data.getVisitor("info");
    	targetPhone = data.getString("targetPhone");
    	if(null == targetPhone) {
    		targetPhone = "";
    	}
    	targetNickname = data.getString("targetNickname");
    	if(null == targetNickname) {
    		targetNickname = "";
    	}
    	targetCategory = data.getString("targetCategory");
    	if(null == targetCategory) {
    		targetCategory = User.CATEGORY_DOCTOR;
    	}
    	relation = data.getString("relation");
    	if(null == relation) {
    		relation = "";
    	}
    	return true;
    }
}
