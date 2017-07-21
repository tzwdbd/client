package com.oversea.task.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Title: NewUri.java
 * @Description: 此uri对象 , 主要是增加对 uri 上 参数的操作
 * @author yhb
 * @date 2013-7-9 下午4:50:41
 * @version V1.0
 */
public class NewUri {
	// 域名
	private String domain	= null;
	// 参数
	private Map<String,String> params = new LinkedHashMap<String,String>();
	/**
	 * 此对方 提供 对uri中,各参数的操作
	 * @param uri
	 */
	public NewUri(String uri){
		// 获取域名
		if(uri.indexOf("?") == -1){
			this.domain = uri;
		}else{
			this.domain = uri.substring(0, uri.indexOf("?"));
			
			String[] paramTemp = uri.substring(uri.indexOf("?")+1, uri.length()).split("&");
			
			for(String paramKV : paramTemp){
				String[] kv = paramKV.split("=");
				if(kv.length ==2){
					params.put(kv[0], kv[1]);
				}else{
					params.put(kv[0], null);
				}
			}
		}
	}
	/**
	 * 此操作将截取URL上 , 第一个?之前的内容
	 * @param url
	 * @return
	 */
	public static String getDomain(String url){
		if(url.indexOf("?") == -1){
			return url;
		}else{
			return url.substring(0, url.indexOf("?"));
		}
	}
	/**
	 * 获取参数
	 * @param name
	 * @return
	 */
	public String getParameter(String name){
		return params.get(name);
	}
	/**
	 * 设置参数
	 * @param name
	 * @param value
	 */
	public void setParameter(String name , String value){
		params.put(name, value);
	}
	/**
	 * 设置参数
	 * @param name
	 * @param value
	 */
	public void setParameter(String name , long value){
		params.put(name, String.valueOf(value));
	}
	/**
	 * 设置参数
	 * @param name
	 * @param value
	 */
	public void setParameter(String name , int value){
		params.put(name, String.valueOf(value));
	}
	/**
	 * 设置参数
	 * @param name
	 * @param value
	 */
	public void setParameter(String name , float value){
		params.put(name, String.valueOf(value));
	}
	/**
	 * 设置参数
	 * @param name
	 * @param value
	 */
	public void setParameter(String name , double value){
		params.put(name, String.valueOf(value));
	}
	
	public String getUri() {
		
		StringBuffer uriTemp = new StringBuffer(domain);
		
		uriTemp.append("?");
		
		for(String paramKey : params.keySet()){
			uriTemp.append(paramKey).append("=");
			if(params.get(paramKey) != null){
				uriTemp.append(params.get(paramKey));
			}
			uriTemp.append("&");
		}
		
		uriTemp.deleteCharAt(uriTemp.length()-1);
		
		return uriTemp.toString();
	}
	
	public InputStream openStream() throws MalformedURLException, IOException{
		return new URL(getUri()).openStream();
	}
	
	public String getDomain() {
		return domain;
	}
}
