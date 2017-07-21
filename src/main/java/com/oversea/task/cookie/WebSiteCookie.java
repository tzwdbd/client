package com.oversea.task.cookie;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.oversea.task.cookie
 * @Description: 网站Cookie管理
 * @date 16/5/26
 */
public class WebSiteCookie implements Serializable {

    private static final long serialVersionUID = -2477389224218826232L;
    /**
     * 对应网站
     */
    private String mall;

    private volatile BasicCookieStore cookieStore = new BasicCookieStore();

    /**
     * 当前cookie的请求的次数
     */
    private volatile AtomicInteger requestNum = new AtomicInteger(0);

    /**
     * 需要验证码
     */
    private volatile boolean isCaptcha;

    /**
     * 多少次请求换Cookie
     */
    public int MAX_EVERY_COOKIE_REQUEST_NUM = 1000;

    /**
     * 验证码处理器
     */
    private CaptchaOcr ocr;

    public WebSiteCookie() {
        isCaptcha = false;
    }

    public void setCaptcha(boolean captcha) {
        isCaptcha = captcha;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public boolean isCaptcha() {
        return isCaptcha;
    }

    public HttpClientContext getContext() {
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(getCookieStore());
        return context;
    }

    public boolean incRequest() {
        if (requestNum.incrementAndGet() > MAX_EVERY_COOKIE_REQUEST_NUM) {
            cookieStore.clear();
            requestNum.set(0);
            return true;
        }
        return false;
    }

    protected void resetRequest() {
        requestNum.set(0);
    }

    public String getMall() {
        return mall;
    }

    public void setMall(String mall) {
        this.mall = mall;
    }

    public CaptchaOcr getOcr() {
        return ocr;
    }

    public void setOcr(CaptchaOcr ocr) {
        this.ocr = ocr;
    }

    public void setMaxRequestNum(Integer maxRequestNum) {
        if (maxRequestNum != null) {
            this.MAX_EVERY_COOKIE_REQUEST_NUM = maxRequestNum;
        } else {
            this.MAX_EVERY_COOKIE_REQUEST_NUM = 1000;
        }
    }
}
