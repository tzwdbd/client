package com.oversea.task.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.oversea.task.service.order.AutoBuy;
import com.oversea.task.util.StringUtil;


public class ClassUtil {
	
	private static List<Class<AutoBuy>> classList = ClassUtil.getAllClassByInterface(AutoBuy.class);
	
	
	public static AutoBuy getAutoBuy(String siteName) {
		if(classList != null && StringUtil.isNotEmpty(siteName)){
			if(siteName.contains(".")){
				siteName = siteName.replace(".", "");
			}
			if(siteName.contains(" ")){
				siteName = siteName.replaceAll(" ", "");
			}
			Class<AutoBuy> clazz = null;
			List<Class<AutoBuy>> matchList = new ArrayList<Class<AutoBuy>>();
			for(Class<AutoBuy> claz : classList){
				if(claz != null){
					String name = claz.getSimpleName().toLowerCase();
					if(name.contains(siteName.toLowerCase())){
						matchList.add(claz);
					}
				}
			}
			int size = matchList.size() ;
			if(size == 0){
				return null;
			}else if(size == 1){
				clazz = matchList.get(0);
			}else{
				//如果有多个,取类名字长度最短的
				for(Class<AutoBuy> claz : matchList){
					if(clazz == null){
						clazz = claz;
					}else{
						int len0 = clazz.getSimpleName().length();
						int len1 = claz.getSimpleName().length();
						clazz = (len0 <= len1 ? clazz:claz);
					}
				}
			}
			if(clazz != null){
	        	try {
	        		Class<?> c = Class.forName(clazz.getName());
					return (AutoBuy)c.newInstance();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					return null;
				}
			}
		}
		
		return null;
	}

    /**
     * 取得某个接口下所有实现这个接口的类
     */
    public static <T> List<Class<T>> getAllClassByInterface(Class<T> c) {
        List<Class<T>> returnClassList = null;

        // 获取当前的包名
        String packageName = c.getPackage().getName();
        // 获取当前包下以及子包下所以的类
        List<Class<?>> allClass = getClasses(packageName, true);
        if (allClass != null) {
            returnClassList = new ArrayList<Class<T>>();
            for (Class<?> classes : allClass) {
                // 判断是否是同一个接口
                if (c.isAssignableFrom(classes)) {
                    // 本身不加入进去
                    if (!c.equals(classes)) {
                        returnClassList.add((Class<T>) classes);
                    }
                }
            }
        }

        return returnClassList;
    }


    /* 
     * 取得某一类所在包的所有类名 不含迭代 
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        //将packageName分解  
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param packageName
     * @param recursive   是否迭代
     * @return
     */
    public static List<Class<?>> getClasses(String packageName, boolean recursive) {

        //第一个class类的集合  
        List<Class<?>> classes = new ArrayList<Class<?>>();
        //获取包的名字 并进行替换  
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things  
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去  
            while (dirs.hasMoreElements()) {
                //获取下一个元素  
                URL url = dirs.nextElement();
                //得到协议的名称  
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上  
                if ("file".equals(protocol)) {
                    //获取包的物理路径  
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中  
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    //如果是jar包文件   
                    //定义一个JarFile  
                    JarFile jar;
                    try {
                        //获取jar  
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        //从此jar包 得到一个枚举类  
                        Enumeration<JarEntry> entries = jar.entries();
                        //同样的进行循环迭代  
                        while (entries.hasMoreElements()) {
                            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件  
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            //如果是以/开头的  
                            if (name.charAt(0) == '/') {
                                //获取后面的字符串  
                                name = name.substring(1);
                            }
                            //如果前半部分和定义的包名相同  
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                //如果以"/"结尾 是一个包  
                                if (idx != -1) {
                                    //获取包名 把"/"替换成"."  
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //如果可以迭代下去 并且是一个包  
                                if ((idx != -1) || recursive) {
                                    //如果是一个.class文件 而且不是目录  
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        //去掉后面的".class" 获取真正的类名  
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            //添加到classes  
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(),
                        recursive,
                        classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 类下所有接口
     *
     * @param packageName
     * @param recursive
     * @return
     */
    public static List<Class<?>> getInterface(String packageName, boolean recursive) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        List<Class<?>> clzzList = getClasses(packageName, recursive);
        for (Class<?> clazz : clzzList) {
            //只保存接口
            if (clazz.isInterface()) {
                result.add(clazz);
            }
        }
        return result;
    }

    /**
     * 取得默认类名
     *
     * @param clazz
     * @return
     */
    public static String getDefaultBeanName(Class<?> clazz) {
        String beanName = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1, clazz.getSimpleName().length()).replace("Impl", "");
        return beanName;
    }

    public static boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> a) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            if (c.isAnnotationPresent(a))
                return true;
            if (isAnnotationPresentOnInterfaces(c, a))
                return true;
        }
        return false;
    }

    private static boolean isAnnotationPresentOnInterfaces(Class<?> clazz, Class<? extends Annotation> a) {
        for (Class<?> i : clazz.getInterfaces()) {
            if (i.isAnnotationPresent(a))
                return true;
            if (isAnnotationPresentOnInterfaces(i, a))
                return true;
        }

        return false;
    }

    public static Class<?> getInterfaceByAnnotation(Class<?> clazz, Class<? extends Annotation> aClazz) {
        Class<?>[] interfaceList = clazz.getInterfaces();
        if (interfaceList != null) {
            for (Class<?> interclazz : interfaceList) {
                Annotation ann = interclazz.getAnnotation(aClazz);
                if (ann != null) {
                    return interclazz;
                }
            }
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> aClazz) {
        //Check class hierarchy
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            T anno = c.getAnnotation(aClazz);
            if (anno != null) {
                return anno;
            }
        }

        //Check interfaces (breadth first)
        Queue<Class<?>> q = new LinkedList<Class<?>>();
        q.add(clazz);
        while (!q.isEmpty()) {
            Class<?> c = q.remove();
            if (c != null) {
                if (c.isInterface()) {
                    T anno = c.getAnnotation(aClazz);
                    if (anno != null) {
                        return anno;
                    }
                } else {
                    q.add(c.getSuperclass());
                }
                q.addAll(Arrays.asList(c.getInterfaces()));
            }
        }

        return null;
    }

    public static <T> T newInstance(Class<T> t) {
        try {
            Class clazz = Class.forName(t.getName());
            Constructor con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            return (T) con.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
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
