/**
 * 淘粉吧版权所有 2013
 */
package com.oversea.task.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * @author rambing
 *
 * 创建时间： 2014年7月30日
 */
public class UrlCodeUtils {
	
	public static String encode(String code, String charset){
		try {
			return URLEncoder.encode(code, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
