package com.oversea.task.update;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oversea.task.util.StringUtil;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.client
 * @Description: 升级jar
 * @date 15/12/5 15:28
 */
public class UpgradeClientJar {

    private Log log = LogFactory.getLog(getClass());

//    private static final String startShellCmd = "startShell.bat";
    private static final String startShellCommand = "cmd /c start java -jar client-shell.jar";

    private UpgradeClientJar() {
    }

    private static UpgradeClientJar jar = new UpgradeClientJar();

    public static UpgradeClientJar newInstance() {
        return jar;
    }

    /**
     * 是否有任务正在执行
     *
     * @return
     *

  

    /**
     * 启动shell
     *
     * @return
     */
    public boolean startUpgradeShell(String clientDownloadUrl) {
        String osn = System.getProperty("os.name").toLowerCase();
        if (osn.contains("win")) {
            try {
                log.error("1.开始启动shell程序SUCCESS");
                String cmd = startShellCommand;
                if(StringUtil.isNotEmpty(clientDownloadUrl)){
                	cmd += " ";
                	cmd += clientDownloadUrl;
                }
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            log.error("2.启动shell程序SUCCESS");
        } else {
            log.error("2.启动shell程序失败 OS.NAME ERROR:" + osn);
        }
        return true;
    }

    /**
     * 杀死自己进程
     */
    public void killSelfProcessor() {
        log.error("关闭本程序 官人不要啊,娘子下次再来~~");
        System.exit(0);
    }
}
