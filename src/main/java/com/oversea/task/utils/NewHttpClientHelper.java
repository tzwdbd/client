package com.oversea.task.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import com.oversea.task.client.SpiderDocument;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.oversea.task.utils
 * @Description:
 * @date 16/5/26
 */
public class NewHttpClientHelper {

    private static final int DEFAULT_TIME_OUT = 15000;

    private static Logger log = Logger.getLogger(NewHttpClientHelper.class);

    public static String doGet(String url) {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(DEFAULT_TIME_OUT).setSocketTimeout(DEFAULT_TIME_OUT);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.DEFAULT).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpGet getCode = new HttpGet(url);
        try {
            HttpResponse response = client.execute(getCode);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static HttpResponse doGetResp(String url) {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(DEFAULT_TIME_OUT).setSocketTimeout(DEFAULT_TIME_OUT);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.DEFAULT).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpGet getCode = new HttpGet(url);
        try {
            return client.execute(getCode);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String doGet(String url, Map<String, String> heads, HttpClientContext context) {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(DEFAULT_TIME_OUT).setSocketTimeout(DEFAULT_TIME_OUT);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.STANDARD).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpGet getCode = new HttpGet(url);
        if (heads != null) {
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                getCode.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            HttpResponse response = client.execute(getCode, context);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String doGet(String url, Map<String, String> heads) {
        return doGet(url, heads, null);
    }

    public static String doPost(String url, Map<String, String> params, HttpClientContext context) {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(DEFAULT_TIME_OUT).setSocketTimeout(DEFAULT_TIME_OUT);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.DEFAULT).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpPost post = new HttpPost(url);
        try {
            List<NameValuePair> pairParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> enty : params.entrySet()) {
                pairParams.add(new BasicNameValuePair(enty.getKey(), enty.getValue()));
            }
            post.setEntity(new UrlEncodedFormEntity(pairParams));
            HttpResponse response = client.execute(post, context);
            return EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String doPost(String url, Map<String, String> params, List<NameValuePair> pairParams, HttpClientContext context) {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(DEFAULT_TIME_OUT).setSocketTimeout(DEFAULT_TIME_OUT);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.DEFAULT).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpPost post = new HttpPost(url);
        try {
            post.setEntity(new UrlEncodedFormEntity(pairParams));
            HttpResponse response = client.execute(post, context);
            //重定向
            if (response.getStatusLine().getStatusCode() == 301 || response.getStatusLine().getStatusCode() == 302) {
                String location = response.getFirstHeader("Location").getValue();
                return doGet(location, params, context);
            }
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String doPost(String url, Map<String, String> params) {
        return doPost(url, params, null);
    }

    public static byte[] downloadSmallFile(String url) {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(DEFAULT_TIME_OUT).setSocketTimeout(DEFAULT_TIME_OUT);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.DEFAULT).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        HttpGet getCode = new HttpGet(url);
        try {
            HttpResponse response = client.execute(getCode);
            return EntityUtils.toByteArray(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getDefaultHeaders(String userAgent, String url) {
        Map<String, String> heads = new HashMap<String, String>();
        heads.put("User-Agent", userAgent);
        heads.put("X-Requested-With", "XMLHttpRequest");
        heads.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        heads.put("Accept-Encoding", "gzip, deflate, sdch");
        heads.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
        heads.put("Cache-Control", "max-age=0");
        try {
            heads.put("Host", new URL(url).getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return heads;
    }

    /**
     * PC端取页面详情
     *
     * @param url
     * @return
     */
    public SpiderDocument doGetWebDoc(String url) {
        String html = doGet(url, getDefaultHeaders(UserAgentExcahnge.getWebRandomUserAgent(), url));
        return new SpiderDocument(Jsoup.parse(html));
    }

    /**
     * 手机段取页面详情
     *
     * @param url
     * @return
     */
    public SpiderDocument doGetWapDoc(String url) {
        String html = doGet(url, getDefaultHeaders(UserAgentExcahnge.getWapRandomUserAgent(), url));
        return new SpiderDocument(Jsoup.parse(html));
    }
}
