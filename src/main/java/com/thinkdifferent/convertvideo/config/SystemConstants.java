package com.thinkdifferent.convertvideo.config;

import cn.hutool.core.map.FixedLinkedHashMap;
import cn.hutool.crypto.SecureUtil;
import net.sf.json.JSONObject;

/**
 * 系统常量
 *
 * @author ltian
 * @version 1.0
 * @date 2022/7/20 16:33
 */
public interface SystemConstants {

    /**
     * json 重试 key
     */
    String RETRY_KEY = "currentRetryKey";

    /**
     * 固定长度队列，记录错误数据
     * key： 无重试次数的md5值
     * value： 含重试次数的数据
     */
    FixedLinkedHashMap<String, JSONObject> ERROR_CONVERT_DATA = new FixedLinkedHashMap<>(200);

    /**
     * 添加错误数据
     * @param data  错误数据，含重试记录
     */
    static void addErrorData(JSONObject data) {
        JSONObject joKey = JSONObject.fromObject(data);
        joKey.put(RETRY_KEY, "0");
        ERROR_CONVERT_DATA.put(SecureUtil.md5(joKey.toString()), data);
    }

    /**
     * 移除错误数据
     * @param data  需移除错误数据，可能有重试次数记录
     */
    static void removeErrorData(JSONObject data) {
        JSONObject joKey = JSONObject.fromObject(data);
        joKey.put(RETRY_KEY, "0");
        ERROR_CONVERT_DATA.remove(SecureUtil.md5(joKey.toString()));
    }
}
