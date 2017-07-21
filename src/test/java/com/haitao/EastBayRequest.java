package com.haitao;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.oversea.task.utils.Constants;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.haitao
 * @Description:
 * @date 16/5/31
 */
public class EastBayRequest {

    public static void main(String[] args) throws IOException {
        String url = "http://www.eastbay.com/product/model:251879/sku:06772800/nike-air-max-2016-womens/orange/black/?cm=1";
        //String url = "http://www.eastbay.com/product/model:177154/sku:99031026/new-balance-990-womens/grey/pink/?cm=";
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(15000).setSocketTimeout(15000);
        RequestConfig requestConfig = builder.build();
        HttpGet get = new HttpGet(url);
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        get.setHeader("Host", "www.eastbay.com");
        get.setHeader("Connection", "keep-alive");
        get.setHeader("Pragma", "no-cache");
        get.setHeader("Cache-Control", "no-cache");
        get.setHeader("Upgrade-Insecure-Requests", "1");
        get.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.63 Safari/537.36");
        get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        get.setHeader("Accept-Encoding", "gzip, deflate, sdch");
        get.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
        get.setHeader("DNT", "1");
        HttpResponse response = client.execute(get);
        String text = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(text);
    }
}
