package com.thinkdifferent.convertvideo.entity.writeback;

import cn.hutool.core.map.MapUtil;
import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import com.thinkdifferent.convertvideo.utils.SystemUtil;

import java.io.File;
import java.util.Map;

/**
 * 回写到本地路径
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 10:14
 */
public class WriteBackPath extends WriteBack {
    /**
     * 回写路径
     */
    private String path;

    @Override
    public WriteBack of(Map<String, Object> writeBack) {
        WriteBackPath writeBackPath = new WriteBackPath();
        writeBackPath.setPath(MapUtil.getStr(writeBack, "path"));
        return writeBackPath;
    }

    @Override
    public String getOutputPath() {
        // 本地路径使用配置的输出路径
        return SystemUtil.beautifulPath(path);
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
