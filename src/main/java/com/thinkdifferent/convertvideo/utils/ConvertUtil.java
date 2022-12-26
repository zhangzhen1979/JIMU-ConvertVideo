package com.thinkdifferent.convertvideo.utils;

import org.apache.commons.lang.StringUtils;

/**
 * 项目工具类
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 9:51
 */
public class ConvertUtil {
    private ConvertUtil() {
    }

    /**
     * 检测对象是否有值且不为"null"字符串
     *
     * @param str 传入的字符串
     * @return  bln
     */
    public static boolean notBlank(String str) {
        return !("null".equalsIgnoreCase(str)) || StringUtils.isNotBlank(str);
    }

    /**
     * 路径美化
     *
     * @param path 原始路径
     * @return 美化后的路径
     */
    public static String beautifulPath(String path) {
        path = _beautifulPath(path);
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return beautifulFilePath(path);
    }

    /**
     * 文件路径标准化
     *
     * @param filePath 文件路径
     * @return 标准化结果
     */
    public static String beautifulFilePath(String filePath) {
        return _beautifulPath(filePath);
    }

    private static String _beautifulPath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        if (path.contains("\\") && !path.contains("\\\\")) {
            path = path.replace("\\", "/");
        }else{
            path = path.replace("\\\\", "/");
        }
        return path;
    }
}
