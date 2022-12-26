package com.thinkdifferent.convertvideo.entity.writeback;

import com.thinkdifferent.convertvideo.entity.WriteBackResult;

import java.io.File;
import java.util.Map;

/**
 * 不需要回写
 * @author ltian
 * @version 1.0
 * @date 2022/7/26 10:36
 */
public class WriteBackNone extends WriteBack {

    public WriteBackNone() {
    }

    /**
     * 对象转换
     *
     * @param writeBack 传入的完整参数map
     * @return wb
     */
    @Override
    public WriteBack of(Map<String, Object> writeBack) {
        return new WriteBackNone();
    }

    /**
     * 转换结果回写
     *
     * @param fileOut 转换后的文件
     */
    @Override
    public WriteBackResult writeBack(File fileOut) {
        return new WriteBackResult(true);
    }
}
