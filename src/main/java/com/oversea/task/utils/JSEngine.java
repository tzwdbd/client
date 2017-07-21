package com.oversea.task.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.taofen8.task.utils
 * @date 15/7/15 20:06
 */
public class JSEngine {

    private static JSEngine JSEngine = new JSEngine();

    private ScriptEngine engine;

    private static final String FILE_TYPE = "javascript";

    private JSEngine() {
        engine = new ScriptEngineManager().getEngineByName(FILE_TYPE);
    }

    public static JSEngine getInstance() {
        return JSEngine;
    }

    public ScriptEngine getScriptEngine(){
        return engine;
    }

    /**
     * 获取什么值得买的跳转后的链接
     *
     * @param contant web网页内容
     * @return
     */
    public String smzdmTransformAmazonLink(String contant) {
        try {
            StringBuilder jsMethodBuilder = new StringBuilder();
            jsMethodBuilder.append("function getAmazonUrl(){return ");
            Matcher matcher = Pattern.compile("<script>([\\s\\S]*?)</script>").matcher(contant);
            if (!matcher.find()) {
                return null;
            }
            String _orgJsMethod = matcher.group(1).trim();
            jsMethodBuilder.append(_orgJsMethod.substring(_orgJsMethod.indexOf("eval") + 5, _orgJsMethod.length() - 1).trim());
            jsMethodBuilder.append("}");
            engine.eval(jsMethodBuilder.toString());
            Invocable invoke = (Invocable) engine;
            String _resultMethod = (String) invoke.invokeFunction("getAmazonUrl");
            matcher = Pattern.compile("smzdmhref='([\\s\\S]*?)'").matcher(_resultMethod);
            if (matcher.find()) {
                String link = matcher.group(1);
                return amzonConvertUrl(link);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取什么值得买的跳转后的链接
     *
     * @param contant web网页内容
     * @return
     */
    public String smzdmTransform6pmLink(String contant) {
        try {
            StringBuilder jsMethodBuilder = new StringBuilder();
            jsMethodBuilder.append("function get6pmUrl(){return ");
            Matcher matcher = Pattern.compile("<script>([\\s\\S]*?)</script>").matcher(contant);
            if (!matcher.find()) {
                return null;
            }
            String _orgJsMethod = matcher.group(1).trim();
            jsMethodBuilder.append(_orgJsMethod.substring(_orgJsMethod.indexOf("eval") + 5, _orgJsMethod.length() - 1).trim());
            jsMethodBuilder.append("}");
            engine.eval(jsMethodBuilder.toString());
            Invocable invoke = (Invocable) engine;
            String _resultMethod = (String) invoke.invokeFunction("get6pmUrl");
            matcher = Pattern.compile("smzdmhref='([\\s\\S]*?)'").matcher(_resultMethod);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取asincode
     * URL 有两种形式,需要做转化
     * <1></> http://www.amazon.com/dp/B00ST4XV6E/?&t=joyo01y-20&m=ATVPDKIKX0DER&tag=joyo01y-20
     * <2></> http://www.amazon.com/gp/product/B00ST4XV6E/?&t=joyo01y-20&m=ATVPDKIKX0DER&tag=joyo01y-20
     * <p/>
     * 第二种需要转换成第一种url
     */
    public static String amzonConvertUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        //URL 转换
        if (url.contains("/gp/product/")) {
            url = url.replace("/gp/product/", "/dp/");
        } if (url.contains("/gp/product//")) {
            url = url.replace("/gp/product//", "/dp/");
        } else if (url.contains("/gp/offer-listing/")) {
            url = url.replace("/gp/offer-listing/", "/dp/");
        }
        if(url.contains("/dp/")){
            Matcher matcher = Pattern.compile("(/dp/[\\s\\S]*?/)").matcher(url);
            if (matcher.find()) {
                url = "http://www.amazon.com" + matcher.group(1);
            }
        }
        //去除最后的/
        while(url.endsWith("/")){
            url = url.substring(0,url.length()-1);
        }
        return url;
    }

 //   public static void main(String[] args) {
//        String con = "<!DOCTYPE html>\n" +
//                "<html>\n" +
//                "    <head>\n" +
//                "        <title>正在跳转至购买页面</title>\n" +
//                "        <meta charset=\"utf-8\" />\n" +
//                "        <meta http-equiv=\"pragma\" content=\"no-cache\" />\n" +
//                "        <noscript><meta http-equiv=\"refresh\" content=\"0; url=/\"></noscript>\n" +
//                "        <script>\n" +
//                "        var from_url = document.referrer.toLowerCase();\n" +
//                "        if (from_url.indexOf('www.baidu.com')>0 || from_url.indexOf('www.google.')>0 || from_url.indexOf('.bing.com')>0\n" +
//                "                 || from_url.indexOf('www.sogou.com') >0|| from_url.indexOf('www.soso.com')>0 || from_url.indexOf('www.so.com') >0\n" +
//                "                 || from_url.indexOf('.yahoo.com')>0 || from_url.indexOf('www.jike.com')>0) {\n" +
//                "                window.location.replace(\"http://www.smzdm.com\");\n" +
//                "        }\n" +
//                "        eval(function(p,a,c,k,e,d){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--){d[e(c)]=k[c]||e(c)}k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c])}}return p}('6 p(A){5 7,O=z Z(\"(^| )\"+A+\"=([^;]*)(;|$)\");8(7=D.11.S(O)){f X(7[2])}y{f\\'\\'}}6 c(d,K,J,c){f d.L(0,K-1)+c+d.L(J,d.12)}5 h=v.9.j;5 3=p(\"3\");8(3!=\\'\\'){3=Y(\"(\"+3+\")\");e=3.e;b=3.b;8(h.13(e+\"/\"+b)<0){B=c(h,W,10,e+\"/\"+b);v.9.14=B}}(6(){(6(i,s,o,g,r,a,m){i[\\'15\\']=r;i[r]=i[r]||6(){(i[r].q=i[r].q||[]).P(R)},i[r].l=1*z V();a=s.U(o),m=s.T(o)[0];a.Q=1;a.1t=g;m.1o.1q(a,m)})(v,D,\\'16\\',\\'//G.1r-E.n/E.1u\\',\\'4\\');5 C=p(\\'1p\\');5 7=C.1m(\\'|\\');8(7[1]){4(\\'w\\',\\'x-F-1\\',{\\'1n\\':7[1]})}y{4(\\'w\\',\\'x-F-1\\')}5 k=1c;6 u(){8(k)f;k=1d;9.j=N}4(\\'1b\\',\\'1a\\',9.j);4(\\'M\\',\\'17\\');N=\\'18://G.19.n/1e/1f?t=I-H&1k=I-H\\';4(\\'M\\',\\'1l\\',\\'直达链接\\',\\'1j\\',\\'1i.n\\',{\\'1g\\':u});1h(u,1s)})()',62,93,'|||zdm_track_info|ga|var|function|arr|if|location||channel|changeStr|allstr|source|return||this_url||href|redirected|||com||getCookie|||||redirect|window|create|UA|else|new|name|go_url|cookie_user|document|analytics|27058866|www|20|joyo01y|end|start|substring|send|smzdmhref|reg|push|async|arguments|match|getElementsByTagName|createElement|Date|26|unescape|eval|RegExp|30|cookie|length|indexOf|replace|GoogleAnalyticsObject|script|pageview|http|amazon|page|set|false|true|dp|B000YDDF6O|hitCallback|setTimeout|haitao_95D34DEB215FAD9B_amazon|web|tag|event|split|userId|parentNode|user|insertBefore|google|1000|src|js'.split('|'),0,{}))\n" +
//                "        </script>\n" +
//                "    </head>\n" +
//                "</html";
//        System.out.println(SMZDMJSEngine.getInstance().smzdmTransformAmazonLink(con));
//        System.out.println(SMZDMJSEngine.getInstance().smzdmTransformAmazonLink(con));
//        System.out.println(SMZDMJSEngine.getInstance().smzdmTransformAmazonLink(con));
 //   }
}
