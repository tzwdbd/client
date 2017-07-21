package com.oversea.task.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class NetUtil {
    public static String getExternalIp() {
        try {
            Document doc = Jsoup.parse(new URL("http://www.ip.cn/"), 1000000);
            Elements result = doc.select("#result .well p code");
            if (result != null && result.size() > 0) {
                return result.get(0).text();
            }
        } catch (IOException e) {

        }
        return "";
    }

    public static void main(String[] args) {
        getExternalIp();
    }
}
