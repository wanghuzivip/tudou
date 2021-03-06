package com.tudou.common.resource;

import com.google.common.collect.Lists;
import com.tudou.common.util.A;
import com.tudou.common.util.LogUtil;
import com.tudou.common.util.U;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Loader {

    /**
     * 基于指定的类(会基于此类来获取类加载器), 在指定的包名下获取所有的枚举
     */
    public static Class[] getEnumArray(Class clazz, String classPackage) {
        List<Class> enumList = getClassList(clazz, classPackage, true);
        return enumList.toArray(new Class[enumList.size()]);
    }

    /**
     * 基于指定的类(会基于此类来获取类加载器), 在指定的包名下获取所有的实现了 Serializable 接口的类
     */
    public static List<Class> getSerializableClassList(Class clazz, String classPackage) {
        return getClassList(clazz, classPackage, false);
    }

    private static List<Class> getClassList(Class clazz, String classPackage, boolean wasEnum) {
        if (LogUtil.ROOT_LOG.isTraceEnabled()) {
            LogUtil.ROOT_LOG.trace("{} in ({})", clazz, clazz.getProtectionDomain().getCodeSource().getLocation());
        }
        List<Class> classList = Lists.newArrayList();
        String packageName = classPackage.replace(".", "/");
        URL url = clazz.getClassLoader().getResource(packageName);
        if (url != null) {
            if ("file".equals(url.getProtocol())) {
                File parent = new File(url.getPath());
                if (parent.isDirectory()) {
                    File[] files = parent.listFiles();
                    if (A.isNotEmpty(files)) {
                        for (File file : files) {
                            Class<?> aClass;
                            if (wasEnum) {
                                aClass = getEnum(file.getName(), classPackage);
                            } else {
                                aClass = getSerializableClass(file.getName(), classPackage);
                            }
                            if (aClass != null) {
                                classList.add(aClass);
                            }
                        }
                    }
                }
            } else if ("jar".equals(url.getProtocol())) {
                try (JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile()) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith(packageName) && name.endsWith(".class")) {
                            Class<?> aClass;
                            if (wasEnum) {
                                aClass = getEnum(name.substring(name.lastIndexOf("/") + 1), classPackage);
                            } else {
                                aClass = getSerializableClass(name.substring(name.lastIndexOf("/") + 1), classPackage);
                            }
                            if (aClass != null) {
                                classList.add(aClass);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (LogUtil.ROOT_LOG.isErrorEnabled()) {
                        LogUtil.ROOT_LOG.error("can't load jar file: " + e.getMessage());
                    }
                }
            }
        }
        return classList;
    }
    private static Class<?> getEnum(String name, String classPackage) {
        if (U.isNotBlank(name)) {
            String className = classPackage + "." + name.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz != null && clazz.isEnum()) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                if (LogUtil.ROOT_LOG.isErrorEnabled()) {
                    LogUtil.ROOT_LOG.error(String.format("can't load class file (%s): ", className) + e.getMessage());
                }
            }
        }
        return null;
    }
    private static Class<?> getSerializableClass(String name, String classPackage) {
        if (U.isNotBlank(name)) {
            String className = classPackage + "." + name.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz != null && Serializable.class.isAssignableFrom(clazz)) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                if (LogUtil.ROOT_LOG.isErrorEnabled()) {
                    LogUtil.ROOT_LOG.error(String.format("can't load class file (%s): ", className) + e.getMessage());
                }
            }
        }
        return null;
    }
}
