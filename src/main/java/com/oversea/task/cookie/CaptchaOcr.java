package com.oversea.task.cookie;

import com.oversea.task.client.SpiderDocument;
import org.apache.http.client.HttpClient;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.oversea.task.cookie
 * @Description:
 * @date 16/5/26
 */
public interface CaptchaOcr {

    /**
     * 破解验证码
     *
     * @param cookie
     * @param html
     * @param url
     */
    public void doOcr(final WebSiteCookie cookie, final String html, final String url);

    /**
     * 是否出现验证码
     *
     * @param html
     * @return
     */
    public boolean isCaptcha(String html);
}
