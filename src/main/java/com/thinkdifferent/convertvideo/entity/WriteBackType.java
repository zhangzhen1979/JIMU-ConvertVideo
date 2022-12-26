package com.thinkdifferent.convertvideo.entity;

import com.thinkdifferent.convertvideo.entity.writeback.*;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * 文件回写方式
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/13 15:37
 */
public enum WriteBackType {
    // 路径回写
    PATH(WriteBackPath.class),
    // 接口回写
    URL(WriteBackUrl.class),
    // FTP 回写
    FTP(WriteBackFtp.class),
    // Ecology 回写
    ECOLOGY(WriteBackEC.class),
    ;

    private final Class<? extends WriteBack> clazzWriteBack;

    WriteBackType(Class<? extends WriteBack> clazzWriteBack) {
        this.clazzWriteBack = clazzWriteBack;
    }

    /**
     * 转换回写对象
     *
     * @param parameters 传入数据
     * @return wb
     */
    public WriteBack convert(Map<String, Object> parameters) {
        Object writeBackParams = parameters.get("writeBack");
        if (Objects.isNull(writeBackParams)){
            return new WriteBackNone();
        }
        Assert.isTrue(writeBackParams instanceof Map, "回写信息格式错误");
        try {
            WriteBack writeBack = this.clazzWriteBack.newInstance().of((Map<String, Object>) writeBackParams);
            if (URL.equals(this)){
                // URL 请求添加请求头
                ((WriteBackUrl)writeBack).setWriteBackHeaders((Map)parameters.get("writeBackHeaders"));
            }
            return writeBack;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
