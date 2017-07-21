package com.oversea.task.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.enums.Platform;

public class Utils {

    /**
     * 首字母变大写
     *
     * @param fldName
     * @return
     */
    public static String firstCharUpper(String fldName) {
        String first = fldName.substring(0, 1).toUpperCase();
        String rest = fldName.substring(1, fldName.length());
        return first + rest;
    }

    /**
     * 寻找sku
     *
     * @param value
     * @return
     */
    public static List<String> getSku(String value) {
        Gson gson = new Gson();
        List<String> strs = new ArrayList<String>();
        ArrayList list = gson.fromJson(value, ArrayList.class);
        for (Object s : list) {
            List al = (List) s;
            for (Object ss : al) {
                strs.add(ss.toString());
            }
        }
        return strs;
    }

    /**
     * 字符串判空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() <= 0) {
            return true;
        }
        return false;
    }

    /**
     * 休眠 毫秒
     *
     * @param time
     */
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static final char UNDERLINE = '_';

    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static AutoBuyStatus switchStatus(AutoBuyStatus in) {
        AutoBuyStatus out = AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_TIME_OUT;
        switch (in) {
            case AUTO_LOGIN_SETUP_FAIL:
            case AUTO_CLIENT_NETWORK_TIMEOUT:
            case AUTO_LOGIN_EXP_UNKNOWN:
                out = AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_TIME_OUT;
                break;
            case AUTO_LOGIN_EXP_MEET_VALIDATE_CODE:
            case AUTO_LOGIN_EXP_MEET_PHONE_NUM:
            case AUTO_LOGIN_EXP_MEET_ZIPCODE:
            case AUTO_LOGIN_EXP_MEET_VISA:
                out = AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH;
                break;
            case AUTO_LOGIN_SUCCESS:
                out = AutoBuyStatus.AUTO_SCRIBE_LOGIN_SUCCESS;
                break;
            default:
                break;
        }
        return out;
    }

    public static String httpPost(String urlStr, Map<String, String> params) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        URLConnection conn = null;
        try {
            conn = url.openConnection();
        } catch (IOException e) {

        }

        HttpURLConnection httpConn = (HttpURLConnection) conn;
        try {
            httpConn.setConnectTimeout(10000);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            httpConn.setRequestMethod("POST");
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setChunkedStreamingMode(5);
            httpConn.connect();

            DataOutputStream out = new DataOutputStream(httpConn.getOutputStream());
            StringBuffer content = new StringBuffer();
            Set<Entry<String, String>> set = params.entrySet();
            for (Entry<String, String> me : set) {
                content.append(me.getKey() + "=" + URLEncoder.encode(me.getValue(), "utf-8")).append("&");
            }

            out.writeBytes(content.toString());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = readResponseData(httpConn);
        return new String(data);
    }

    private static byte[] readResponseData(HttpURLConnection httpConn) {
        ByteArrayOutputStream baos = null;
        InputStream in = null;
        byte[] data = null;
        try {
            byte[] buffer = new byte[1024];
            in = httpConn.getInputStream();
            baos = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            data = baos.toByteArray();

        } catch (IOException e) {
        } finally {
            try {
                if (null != in) in.close();
                if (null != baos) baos.close();
                httpConn.disconnect();
            } catch (IOException e) {
            }
        }
        return data;
    }

    public static void deleteFiles(File f) {
        if (f.exists()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files)
                    if (file.isDirectory()) {
                        deleteFiles(file);
                        file.delete();
                    } else if (file.isFile()) {
                        file.delete();
                    }
            }
            f.delete();
        }
    }

    public static <T> T newInstance(String clazzName) {
        try {
            Class clazz = Class.forName(clazzName);
            Constructor con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            return (T) con.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
