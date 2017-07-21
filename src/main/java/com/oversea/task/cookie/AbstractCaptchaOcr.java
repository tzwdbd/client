package com.oversea.task.cookie;

import com.oversea.task.client.SpiderDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.oversea.task.cookie
 * @Description: 验证码抽象处理类
 * @date 16/5/26
 */
public abstract class AbstractCaptchaOcr implements CaptchaOcr {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private Log log = LogFactory.getLog(getClass());

    public synchronized void doOcr(final WebSiteCookie cookie, final String html, final String url) {
        final SpiderDocument d = new SpiderDocument(Jsoup.parse(html));
        //如果正在处理验证码线程中则直接返回
        if (cookie == null || cookie.isCaptcha()) {
            log.info("正在破解中,请等待破解完成直接使用...");
            return;
        }
        cookie.setCaptcha(true);
        cookie.getCookieStore().clear();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ocr(cookie, d, url)) {
                        log.info(String.format("破解%s验证码成功,设置Cookie", cookie.getMall()));
                        cookie.resetRequest();
                    } else {
                        log.info(String.format("破解%s验证码失败", cookie.getMall()));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    cookie.setCaptcha(false);
                    cookie.incRequest();
                }
            }
        });
    }

    public abstract boolean ocr(WebSiteCookie cookie, SpiderDocument d, String url) throws Exception;
}
