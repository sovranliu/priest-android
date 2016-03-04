package com.wehop.priest.business.structure.notify;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.wehop.priest.business.user.User;

/**
 * 好友添加请求通知
 */
public class AddRequestNotify extends Notify {
	private static final long serialVersionUID = 1L;

	/**
	 * 通知类型
	 */
	public final static int TYPE_ADDREQUEST = 1;

	/**
	 * 申请ID
	 */
	public String requestId = null;
	/**
	 * 申请者手机号码
	 */
	public String applicantPhone = null;
	/**
	 * 申请者昵称
	 */
	public String applicantNickname = null;
	/**
	 * 申请者身份类型
	 */
	public String applicantCategory = null;
	/**
	 * 添加的关系
	 */
	public String relation = null;


	/**
	 * 获取通知类型
	 * 
	 * @return 通知类型
	 */
	@Override
	public int type() {
		return TYPE_ADDREQUEST;
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
    	requestId = data.getString("requestId");
    	if(null == requestId) {
    		return false;
    	}
    	applicantPhone = data.getString("applicantPhone");
    	if(null == applicantPhone) {
    		applicantPhone = "";
    	}
    	applicantNickname = data.getString("applicantNickname");
    	if(null == applicantNickname) {
    		applicantNickname = "";
    	}
    	applicantCategory = data.getString("applicantCategory");
    	if(null == applicantNickname) {
    		applicantCategory = User.CATEGORY_DOCTOR;
    	}
    	relation = data.getString("relation");
    	if(null == relation) {
    		relation = "";
    	}
    	return true;
    }
}
