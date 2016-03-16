package com.wehop.priest.business.structure.notify;

import com.slfuture.carrie.base.json.JSONVisitor;

/**
 * 好友删除反馈通知
 */
public class BeSelectedNotify extends Notify {
	private static final long serialVersionUID = 1L;

	/**
	 * 通知类型
	 */
	public final static int TYPE_BESELECTED = 10;

	/**
	 * 反馈者手机号码
	 */
	public String targetPhone = null;


	/**
	 * 获取通知类型
	 * 
	 * @return 通知类型
	 */
	@Override
	public int type() {
		return TYPE_BESELECTED;
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
    	return true;
    }
}
