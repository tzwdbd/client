package com.oversea.task.cookie;

import java.util.concurrent.ConcurrentHashMap;

import com.oversea.task.enums.Platform;
import com.oversea.task.utils.Utils;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.oversea.task.cookie
 * @Description: Cookie保存容器
 * @date 16/5/26
 */
public class CookieStore {


    /**
     * 获取扫描的目录
     */
    private final static String SCAN_FOLDER = CookieStore.class.getName().replace(CookieStore.class.getSimpleName(), "mall.");

    private final static String CLASS_SUFFIX = "ComCaptchaOcr";

    private static ConcurrentHashMap<String, WebSiteCookie> cookieSet = new ConcurrentHashMap<String, WebSiteCookie>();

    public static WebSiteCookie getWebSiteCookie(String mallName) {
        return getWebSiteCookie(mallName, null);
    }

    public static WebSiteCookie getWebSiteCookie(String mallName, Integer maxRequest) {
        WebSiteCookie cookie = cookieSet.get(mallName);
        if (cookie == null) {
            synchronized (CookieStore.class) {
                cookie = cookieSet.get(mallName);
                if (cookie == null) {
                    cookie = new WebSiteCookie();
                    cookie.setMall(mallName);
                    cookie.setMaxRequestNum(maxRequest);
                    String className = SCAN_FOLDER + Utils.firstCharUpper(getPlatForm(mallName).getValue()) + CLASS_SUFFIX;
                    CaptchaOcr ocr = Utils.newInstance(className);
                    if (ocr != null) {
                        cookie.setOcr(ocr);
                    }
                    cookieSet.put(mallName, cookie);
                }
            }
        }
        return cookie;
    }

    public static Platform getPlatForm(String mallName) {
        if (mallName.contains(".")) {
            mallName = mallName.split("\\.")[0];
        }
        return Platform.create(mallName);
    }
}
