package com.oversea.task.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SimpleHttpUtils
{
	public static String httpPost(String url, Map<String, String> param, String encoding) throws ClientProtocolException, IOException
	{
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000).build();
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);

		if (param != null && !param.isEmpty())
		{
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			Set<Entry<String, String>> et = param.entrySet();
			Iterator<Entry<String, String>> it = et.iterator();
			while (it.hasNext())
			{
				Entry<String, String> i = it.next();
				formparams.add(new BasicNameValuePair(i.getKey(), i.getValue()));
			}
			HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, encoding);
			post.setEntity(reqEntity);
		}

		post.setConfig(requestConfig);
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 200)
		{
			HttpEntity resEntity = response.getEntity();
			return EntityUtils.toString(resEntity, encoding);
		}
		else
		{
			return "";
		}
	}

	public static String httpPost(String url, Map<String, String> param) throws ClientProtocolException, IOException
	{
		return httpPost(url, param, "utf-8");
	}

	public static String httpGet(String url, String encoding) throws ClientProtocolException, IOException
	{
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(100000).setConnectTimeout(100000).setConnectionRequestTimeout(100000).build();
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(url);

		get.setConfig(requestConfig);
		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() == 200)
		{
			HttpEntity resEntity = response.getEntity();
			return EntityUtils.toString(resEntity, encoding);
		}
		else
		{
			return "";
		}
	}

	public static String httpGet(String url) throws ClientProtocolException, IOException
	{
		return httpGet(url, "utf-8");
	}
}
