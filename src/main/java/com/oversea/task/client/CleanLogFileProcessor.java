package com.oversea.task.client;

import com.oversea.task.utils.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.client
 * @Description: 清理超过10天的日志文件
 * @date 16/1/21 17:44
 */
public class CleanLogFileProcessor {

    private static final Pattern LOG_FILE_TIME_PATTERN = Pattern.compile("\\d{4}+[-]\\d{1,2}+[-]\\d{1,2}+");

    private Log log = LogFactory.getLog(getClass());

    private final static String SCREENSHOT = "screenshot";


    /**
     * 清理几天前的日志文件
     */
    private static final int DAYS = 45;

    /**
     * 进程
     */
    public void doProcessor() {
        try {
            String logPath = System.getProperty("log.path");
            log.info("清理过期文件开始:" + logPath);
            File logFolder = new File(logPath);
            deleteScreenShot(logFolder);
            deleteLogFile(logFolder);
            log.info("清理过期文件完毕");
        }
        //防止抛出异常影响主流程
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 删除截屏文件
     *
     * @param logFolder
     */
    public void deleteScreenShot(File logFolder) {
        File screenshotFolder = new File(logFolder.getParent() + File.separator + SCREENSHOT);
        if (screenshotFolder == null || screenshotFolder.list() == null) {
            return;
        }
        for (String screenshotFileName : screenshotFolder.list()) {
            try {
                File screenshotFile = new File(screenshotFolder.getAbsoluteFile() + File.separator + screenshotFileName);
                if (screenshotFile.isFile()) {
                    screenshotFileName = screenshotFile.getName();
//                    if (System.currentTimeMillis() - Long.parseLong(screenshotFileName.replace(".png", "")) > DAYS * 24 * 3600 * 1000L) {
                    if (System.currentTimeMillis() - screenshotFile.lastModified() > DAYS * 24 * 3600 * 1000L) {
                        if (screenshotFile.delete()) {
                            log.info(String.format("删除过期截屏文件[%s]", screenshotFile.getName()));
                        }
                    }
                }
            } catch (Exception e) {
                log.error(String.format("删除过期截屏文件[%s]报错...", screenshotFileName), e);
            }
        }
    }

    /**
     * 删除日志文件
     *
     * @param logFolder
     */
    public void deleteLogFile(File logFolder) {
        if (logFolder == null || logFolder.isFile()) {
            return;
        }
        for (String subFilePath : logFolder.list()) {
            try {
                File subLogFile = new File(logFolder.getAbsoluteFile() + File.separator + subFilePath);
                if (subLogFile.isFile()) {
//                    Matcher m = LOG_FILE_TIME_PATTERN.matcher(subLogFile.getName());
//                    if (m.find()) {
//                        String date = m.group();
//                        if (System.currentTimeMillis() - DateUtils.ymdString2Date(date).getTime() > DAYS * 24 * 3600 * 1000L) {
//                            if (subLogFile.delete()) {
//                                log.info(String.format("删除过期日志文件[%s]", subLogFile.getName()));
//                            }
//                        }
//                    }
                	if (System.currentTimeMillis() - subLogFile.lastModified() > DAYS * 24 * 3600 * 1000L) {
                        if (subLogFile.delete()) {
                            log.info(String.format("删除过期日志文件[%s]", subLogFile.getName()));
                        }
                    }
                } else {
                    deleteLogFile(subLogFile);
                }
            } catch (Exception e) {
                log.error(String.format("删除过期日志文件[%s]报错...", subFilePath), e);
            }
        }
    }
}
