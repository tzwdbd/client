package com.haitao;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.haitao
 * @Description:
 * @date 16/6/2
 */
public class DownloadImgTest {

    public static HttpClient getHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String proxyHost = "47.88.84.111";
        int proxyPort = 3128;
        String userName = "haihu";
        String password = "haihu";
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(proxyHost, proxyPort),
                new UsernamePasswordCredentials(userName, password));
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
        return httpClient;
    }

    public static byte[] downloadSmallFile(String url) {

//        HttpHost targetHost = new HttpHost("47.88.84.111", 3128, "http");
//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        credsProvider.setCredentials(
//                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//                new UsernamePasswordCredentials("haihu", "haihu!234560"));
//
//        AuthCache authCache = new BasicAuthCache();
//        BasicScheme basicAuth = new BasicScheme();
//        authCache.put(targetHost, basicAuth);
//
//        HttpClientContext context = HttpClientContext.create();
//        context.setCredentialsProvider(credsProvider);
//        context.setAuthCache(authCache);

        //RequestConfig config = RequestConfig.custom().build();

        HttpClient client = getHttpClient();
        //HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).setDefaultRequestConfig(config).build();

        HttpGet getCode = new HttpGet(url);
        int len = 0;
        try {
            byte[] bytes = new byte[102400];
            HttpResponse response = client.execute(getCode);
            InputStream is = response.getEntity().getContent();
            System.out.println(response.getStatusLine().getStatusCode());
            FileOutputStream fos = new FileOutputStream("/Users/fengjian/Documents/test.jpg");
            while ((len = is.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }
            fos.flush();
            IOUtils.write(bytes, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        byte[] bytes = downloadSmallFile("http://g.nordstromimage.com/ImageGallery/store/product/Large/3/_11573983.jpg");

//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        credsProvider.setCredentials(
//                new AuthScope("47.88.84.111", 3128),
//                new UsernamePasswordCredentials("haihu", "haihu"));
//        CloseableHttpClient httpclient = HttpClients.custom().build();
//        try {
//            HttpHost proxy = new HttpHost("47.88.84.111", 3128);
//            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
//            HttpGet httpget = new HttpGet("http://weibo.com/ttarticle/p/show?id=2309403982381420774022");
//            httpget.setConfig(config);
//
//            CloseableHttpResponse response = httpclient.execute(httpget);
//            try {
//                System.out.println("----------------------------------------");
//                System.out.println(response.getStatusLine());
//                System.out.println(EntityUtils.toString(response.getEntity()));
//            } finally {
//                response.close();
//            }
//        } finally {
//            httpclient.close();
//        }

    }
}
