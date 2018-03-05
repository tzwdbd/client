package com.oversea.task.update;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haihu.rpc.client.RpcNettyClient;
import com.haihu.rpc.common.SpringObjectFactory;
import com.oversea.task.util.MD5Util;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class VersionCheckProcessor extends Thread implements InitializingBean{
	private Logger log = Logger.getLogger(getClass());
	private String version = "";
	private Gson gson = new Gson();
	
	@Resource
	private RpcNettyClient rpcNettyClient;
	
	@Value("${version.check}")
	private String versionCheck;
	
	private String clientDownloadUrl;
	
	static CloseableHttpClient httpclient = null;
	static{
		RequestConfig defaultRequestConfig = RequestConfig.custom()
		        .setConnectTimeout(10000)
		        .setSocketTimeout(10000)
		        .setConnectionRequestTimeout(10000)
		        .build();
		
		httpclient = HttpClients.custom()
		        .setDefaultRequestConfig(defaultRequestConfig)
		        .build();
	}
	
	public void init(){
		log.error("start version check init");
		this.start();
	}

	public String getVersion() {
		if(StringUtil.isEmpty(version)){
			try {
				File file = new File("taskClient.jar");
				version = MD5Util.calMD5(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		log.error("start version check");
		while(true){
			try {
				if(isUpdate()){
					updateJar();
					break;
				}
	        } catch (Throwable e) {
	        	log.error(e);
	        }
			Utils.sleep(9500);
		}
	}
	
	private void updateJar() {
        log.error("[INFO]:Client to begin the update");
        log.error("[INFO]:Check whether there are tasks being doing , if there is the wait!");
        while (true) {
            if (!rpcNettyClient.isBusy()) {
                rpcNettyClient.stop();
                log.error("[INFO]:Close the Client side NIO connection");
                UpgradeClientJar.newInstance().startUpgradeShell(clientDownloadUrl);
                log.error("[INFO]:Start Shell");
                UpgradeClientJar.newInstance().killSelfProcessor();
                log.error("[INFO]:Kill myself");
                break;
            }
            log.error("[ERROR]:wait 15 seconds");
            try {
                Thread.sleep(15000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
	
	private boolean isUpdate() {
		if(StringUtils.isEmpty(version)){
			try {
				File file = new File("taskClient.jar");
				version = MD5Util.calMD5(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String url = versionCheck+"?version="+version;
		String result = "";
		HttpGet httpRequst = new HttpGet(url);
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpclient.execute(httpRequst);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				result = EntityUtils.toString(httpEntity);// 取出应答字符串
				if(httpEntity!=null){
					EntityUtils.consumeQuietly(httpEntity);
				}
				result.replaceAll("\r", "");// 去掉返回结果中的"\r"字符，否则会在结果字符串后面显示一个小方格
			}
		} catch (Exception e) {
			log.error(e);
		}finally{
			if(httpResponse != null){
				try{
					httpResponse.close();
				}catch(Exception e){
					log.error(e);
				}
				httpResponse = null;
			}
			Utils.sleep(200);
		}
		if(!StringUtils.isEmpty(result)){
			try{
				Map<String, String> map = gson.fromJson(result, new TypeToken<Map<String, String>>(){}.getType());
				if("true".equals(map.get("isUpdate"))){
					clientDownloadUrl = map.get("clientDownloadUrl");
					return true;
				}
			}catch(Exception e){
				log.error(result,e);
			}
			
		}
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		RpcNettyClient rpcNettyClient = (RpcNettyClient)SpringObjectFactory.getInstance("rpcNettyClient");
        rpcNettyClient.setVersion(getVersion());
	}
}
