package com.thinkdifferent.convertvideo.entity.writeback;

import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import com.thinkdifferent.convertvideo.utils.ConvertUtil;

import java.io.File;
import java.util.Map;

/**
 * 回写父类
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 10:14
 */
public abstract class WriteBack {
    /**
     * 对象转换
     *
     * @param writeBack 传入的完整参数map
     * @return wb
     */
    public abstract WriteBack of(Map<String, Object> writeBack);

    /**
     * @return 文件输出路径
     */
    public String getOutputPath() {
        return ConvertUtil.beautifulPath(ConvertVideoConfig.outPutPath);
    }

    /**
     * 转换结果回写
     *
     * @param fileOut        转换后的文件
     */
    public abstract WriteBackResult writeBack(File fileOut);
}
