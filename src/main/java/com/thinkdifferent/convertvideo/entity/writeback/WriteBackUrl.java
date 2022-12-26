package com.thinkdifferent.convertvideo.entity.writeback;

import cn.hutool.core.map.MapUtil;
import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import com.thinkdifferent.convertvideo.utils.WriteBackUtil;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.Map;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 10:15
 */
@Log4j2
public class WriteBackUrl extends WriteBack {
    /**
     * 文件上传接口的API地址
     */
    private String url;
    /**
     * 请求头
     */
    private Map<String, String> writeBackHeaders;

    @Override
    public WriteBack of(Map<String, Object> writeBack) {
        WriteBackUrl writeBackUrl = new WriteBackUrl();
        writeBackUrl.setUrl(MapUtil.getStr(writeBack, "url"));
        return writeBackUrl;
    }

    /**
     * 转换结果回写
     *
     * @param fileOut 转换后的文件
     */
    @Override
    public WriteBackResult writeBack(File fileOut) {
        try {
            return WriteBackUtil.writeBack2Api(fileOut.getCanonicalPath(), url, writeBackHeaders);
        } catch (Exception e) {
            log.error(e);
            return new WriteBackResult(false, e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getWriteBackHeaders() {
        return writeBackHeaders;
    }

    public void setWriteBackHeaders(Map<String, String> writeBackHeaders) {
        this.writeBackHeaders = writeBackHeaders;
    }
}
