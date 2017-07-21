package com.oversea.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientMain {

    private static final Object await = new Object();
    static Log log = LogFactory.getLog(ClientMain.class);

    public static void main(String[] args) throws Exception {
    	List<String> configList = new ArrayList<String>();
    	configList.add("classpath*:spring-client-bean.xml");
    	configList.add("classpath*:spring-task-client.xml");
    	log.error("client begin----");
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configList.toArray(new String[configList.size()]));
    	context.start();
    	
    }

    /**
     * 方法销毁回调
     */
    public static class ShutDownHook implements Runnable {
        @Override
        public void run() {
            try {
                synchronized (await) {
                    await.notifyAll();
                }
                //logManager.shutdownLogging();
                System.out.println("The application destory successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
