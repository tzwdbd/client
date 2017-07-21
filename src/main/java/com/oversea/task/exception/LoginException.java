/**
 * tf8-task-client 淘粉吧版权所有 2014
 */
package com.oversea.task.exception;


/**
 * @author wangqiang
 *
 */
public class LoginException extends BaseCoException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6606081418964058069L;
	
	private LoginException(String msg){
		super(msg);
	}
	
	private LoginException(String msg, Throwable e){
		super(msg, e);
	}
	
	public static LoginException build() {
		return new LoginException(getMsg("alimama", null));
	}
	
	public static LoginException build(String targetUrl) {
		return new LoginException(getMsg(targetUrl, null));
	}
	
	public static LoginException build(String targetUrl, Throwable e){
		return new LoginException(getMsg(targetUrl, e), e);
	}
	
	protected static String getMsg(String targetUrl, Throwable e){
		if(e == null){
			return String.format("在访问页面【%s】时无法正确登录。", targetUrl);
		}else{
			return String.format("在访问页面【%s】时无法正确登录。出现异常%s", targetUrl, e.getMessage());
		}
	}

}
