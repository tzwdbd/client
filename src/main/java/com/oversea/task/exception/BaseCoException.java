/**
 * tf8-task-client 淘粉吧版权所有 2014
 */
package com.oversea.task.exception;



/**
 * @author wangqiang
 *
 */
public class BaseCoException extends Exception{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4466866443513845050L;

	public BaseCoException(String msg) {
       super(msg);
    }
	
	public BaseCoException(String msg, Throwable e) {
		super(msg, e);
	}
	
	

}
