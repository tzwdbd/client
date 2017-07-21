package com.oversea.task.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by lemon on 2014/5/14.
 */
public class ProcessUtil {

    private static final String CMD_CHARSET = "GBK";

    public static String exec(String command){
        try {
            Process p = Runtime.getRuntime().exec(command);
            String error = getErrorMsg(p);
            return error == null ? getOutputMsg(p) : error;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取错误信息
     *
     * @param p Process
     * @return  message
     * @throws java.io.IOException
     */
    private static String getErrorMsg(Process p) throws IOException {
        String error = stream2text(p.getErrorStream());
        return "".equals(error) ? null : error.trim();
    }

    /**
     * 获取输出信息
     *
     * @param p Process
     * @return  message
     * @throws IOException
     */
    private static String getOutputMsg(Process p) throws IOException {
        String output = stream2text(p.getInputStream());
        return "".equals(output) ? null : output.trim();
    }

    /**
     * 处理流中的数据，返回文本
     *
     * @param in    InputStream
     * @return  message
     * @throws IOException
     */
    private static String stream2text(InputStream in) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(in, CMD_CHARSET));
            while ((line = br.readLine()) != null && sb.length() == 0) // 可能出现bug unavailable - no suspended threads
                sb.append(line);
            return sb.toString();
        } finally {
            if (br != null)
                br.close();
        }
    }
}
