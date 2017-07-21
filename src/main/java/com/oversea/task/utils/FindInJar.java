package com.oversea.task.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.taofen8.task.client.orderspider
 * @date 15/8/28 23:03
 */
public class FindInJar {
    public String className;
    public ArrayList<String> jarFiles = new ArrayList<String>();

    public FindInJar() {
    }

    public FindInJar(String className) {
        this.className = className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> findClass(String dir, boolean recurse) {
        searchDir(dir, recurse);
        return this.jarFiles;
    }

    @SuppressWarnings({ "resource", "rawtypes" })
	protected void searchDir(String dir, boolean recurse) {
        try {
            File d = new File(dir);
            if (!d.isDirectory()) {
                return;
            }
            File[] files = d.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (recurse && files[i].isDirectory()) {
                    searchDir(files[i].getAbsolutePath(), true);
                } else {
                    String filename = files[i].getAbsolutePath();
                    if (filename.endsWith(".jar") || filename.endsWith(".zip")) {
						ZipFile zip = new ZipFile(filename);
                        Enumeration entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = (ZipEntry) entries.nextElement();
                            String thisClassName = getClassName(entry);
                            if (thisClassName.equals(this.className) || thisClassName.equals(this.className + ".class")) {
                                this.jarFiles.add(filename);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getFilenames() {
        return this.jarFiles;
    }

    protected String getClassName(ZipEntry entry) {
        StringBuffer className = new StringBuffer(entry.getName().replace('/', '.'));
        return className.toString();
    }

    public static void main(String args[]) {
        FindInJar findInJar = new FindInJar("org.apache.http.impl.client.HttpClientBuilder");
        List<?> jarFiles = findInJar.findClass("/Users/fengjian/Documents/sea-online/common/target/lib/", true);
        if (jarFiles.size() == 0) {
            System.out.println("Not Found");
        } else {
            for (int i = 0; i < jarFiles.size(); i++) {
                System.out.println(jarFiles.get(i));
            }
        }
    }
}
