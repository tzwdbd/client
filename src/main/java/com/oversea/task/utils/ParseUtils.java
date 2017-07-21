package com.oversea.task.utils;

import com.oversea.task.exception.DataParseException;
import org.apache.log4j.Logger;

/**
 * 解析工具类
 * Created by lemon on 2014/6/12.
 */
public class ParseUtils {
	
	public static Logger logger = Logger.getLogger(ParseUtils.class);
    /**
     * 从K-V字符串中获取值
     *
     * @param kvString  源数据
     * @param key       Key
     * @param separator 分隔符,默认是英文分号
     * @return 返回对应的值
     */
    public static String getValueFromKVString(String kvString, String key, String... separator) {
        if(kvString == null || key == null)
            throw new DataParseException("kvString or key is null.");

        String sep;
        if(separator == null || separator.length == 0)
            sep = ";";
        else
            sep = separator[0];

        String[] temp = kvString.trim().split(sep);
        if (temp.length <= 1){
        	logger.warn("数据格式定义中不包含编码方式：" + kvString);
        	return null;
        }
            
        for (String s : temp) {
            if (s == null)
                continue;
            String[] temp2 = s.split("=");
            if (temp2.length <= 1)
                continue;
            if (temp2[0] == null)
                continue;
            if (key.equals(temp2[0].trim())) {
                return temp2[1];
            }
        }
        throw new DataParseException("数据解析出错：" + kvString);
    }

    /**
     * 格式化JSON文本
     *
     * @param jsonStr JSON文本
     * @param prefix  前缀
     * @param suffix  后缀
     * @return 格式化后的JSON文本
     */
    public static String formatJsonString(String jsonStr, String prefix, String suffix) {
        if (jsonStr == null)
            throw new RuntimeException("jsonStr is null.");

        if (prefix != null && !"".equals(prefix)) {
            if (jsonStr.startsWith(prefix))
                jsonStr = jsonStr.substring(prefix.length());
        }

        if (suffix != null && !"".equals(suffix)) {
            if (jsonStr.endsWith(suffix))
                jsonStr = jsonStr.substring(0, jsonStr.length() - suffix.length());
        }
        return jsonStr.trim();
    }
}
