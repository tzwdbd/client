package com.oversea.task.utils;

import com.oversea.task.domain.HttProxypHost;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.utils
 * @Description: 用户获取httphost类
 * 警告****注意不要后面再向PROXY_TL补代理,多线程下会出错*****
 * @date 16/3/18 17:41
 */
public class HttpHostHelper {

    private static ThreadLocal<List<HttProxypHost>> PROXY_TL = new ThreadLocal<List<HttProxypHost>>();

    private static ThreadLocal<AtomicInteger> PROXY_IND = new ThreadLocal<AtomicInteger>();

    /**
     * 初始化导入所有的代理
     *
     * @param hosts
     */
    public static void init(List<HttProxypHost> hosts) {
        PROXY_TL.set(hosts);
        PROXY_IND.set(new AtomicInteger(0));
    }


    /**
     * 获取下一个代理,返回null就表示已经没有了
     *
     * @return
     */
    public static HttProxypHost next() {
        int index = PROXY_IND.get().getAndIncrement();
        return (PROXY_TL.get().size() > index) ? PROXY_TL.get().get(index) : null;
    }

    /**
     * 反馈状态
     *
     * @param host
     * @param status
     */
    public static void feedback(HttProxypHost host, int status) {
        host.setStatus(status);
    }
}


