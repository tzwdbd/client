package com.oversea.task.utils;

import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

	/**
	 * 将URL附带的参数转成Map
	 * @param url
	 * @return
	 */
	public static Map<String, String> decodeURLParameter(String url) {
		if(null == url) {
			return null;
		}
		
		Map<String, String> paramMap = new HashMap<String, String>();
		
		String parameters = url.substring(url.indexOf("?") + 1);
		if(StringUtils.isEmpty(parameters)) {
			return paramMap;
		}
		
		String[] paramArray = parameters.split("&");
		for(String param : paramArray) {
			if(param.indexOf("?") > 0 && param.indexOf("=") > 0){
				paramMap.putAll(decodeURLParameter(param));
				break;
			}
			if(param.indexOf("=") > 0) {
				String key = param.substring(0, param.indexOf("="));
				String value = param.substring(param.indexOf("=") + 1);
				paramMap.put(key, value);
			}
		}
		
		return paramMap;
	}
	
	/**
	 * 将URL的参数清空然后重新添加
	 * @param url
     * @param paramMap
	 * @return
	 */
	public static String encodeURLParameter(String url, Map<String, String> paramMap) {
		if(null == url) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(url);
	
		if(null != paramMap && paramMap.size() > 0) {
			if(!url.contains("?")) {
				sb.append("?");
			}
			
			for(String key : paramMap.keySet()) {
				sb.append(key).append("=").append(paramMap.get(key)).append("&");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 返回没有参数的URL
	 * @param url
	 * @return
	 */
	public static String getURLWithoutParameter(String url) {
		if(null == url) {
			return null;
		}
		if(url.indexOf("?") > 0) {
			return url.substring(0, url.indexOf("?"));
		} else {
			return url;
		}
	}
}
