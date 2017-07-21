package com.oversea.task.client;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author wangqiang
 */
public class SpiderDocument {

    // 主页面的文档
    private Document document;

    public SpiderDocument(Document document) {
        this.document = document;
    }


    /**
     * 从文档中按照cssQuery进行查询。
     *
     * @param cssQuery
     * @return
     */
    public Elements selectEles(String cssQuery) {
        return document.select(cssQuery);
    }

    /**
     * 根据Id获取元素
     *
     * @param id
     * @return
     */
    public Element selectEleById(String id) {
        return document.getElementById(id);
    }

    /**
     * 从文档中查询出一个特定的值
     *
     * @param cssQuery
     * @return
     */
    public Element selectOne(String cssQuery) {
        Elements eles = this.selectEles(cssQuery);
        if (eles == null || eles.isEmpty()) {
            return null;
        } else if (eles.size() > 1) {
            return eles.get(0);
        } else {
            return eles.get(0);
        }
    }

    public String selectHtmlValue(String cssQuery) {
        Element ele = this.selectOne(cssQuery);
        if (ele == null) {
            return null;
        } else {
            return ele.html();
        }
    }

    public String selectHtmlText(String cssQuery) {
        Element ele = this.selectOne(cssQuery);
        if (ele == null) {
            return null;
        } else {
            return ele.text();
        }
    }

    public String selectTextValue(String cssQuery) {
        Element ele = this.selectOne(cssQuery);
        if (ele == null) {
            return null;
        } else {
            return ele.text();
        }
    }

    public Elements getElementsByClass(String className) {
        return document.getElementsByClass(className);
    }

    public String selectAttrValue(String cssQuery, String attribute) {
        Element ele = this.selectOne(cssQuery);
        if (ele == null) {
            return null;
        } else {
            return ele.attr(attribute);
        }
    }

    /**
     * @param text
     * @return
     */
    public boolean contains(String text) {
        return this.document.html().contains(text);
    }

    @Override
    public String toString() {
        return this.document.toString();
    }

    public Document get() {
        return this.document;
    }
}
