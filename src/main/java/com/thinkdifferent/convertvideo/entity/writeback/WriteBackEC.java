package com.thinkdifferent.convertvideo.entity.writeback;

import cn.hutool.core.map.MapUtil;
import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import com.thinkdifferent.convertvideo.utils.SystemUtil;
import com.thinkdifferent.convertvideo.utils.EcologyUploadUtil;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONObject;

import java.io.File;
import java.util.Map;

/**
 * @author 张镇
 * @version 1.0
 * @date 2022/4/23 12:30
 */
@Log4j2
public class WriteBackEC extends WriteBack {
    /**
     * Ecloogy服务的url
     */
    private String address;
    /**
     * 文件上传接口的API
     */
    private String apiUrl;
    /**
     * 文档目录ID
     */
    private String categoryID;
    /**
     * ecology系统发放的授权许可证(appid)
     */
    private String appId;


    @Override
    public WriteBack of(Map<String, Object> writeBack) {
        WriteBackEC writeBackEC = new WriteBackEC();
        writeBackEC.setAddress(MapUtil.getStr(writeBack, "address"));
        writeBackEC.setApiUrl(MapUtil.getStr(writeBack, "api"));
        writeBackEC.setCategoryID(MapUtil.getStr(writeBack, "category"));
        writeBackEC.setAppId(MapUtil.getStr(writeBack, "appId"));

        return writeBackEC;
    }

    /**
     * 转换结果回写
     *
     * @param fileOut 转换后的文件
     */
    @Override
    public WriteBackResult writeBack(File fileOut) {
        try {
            String filePath = SystemUtil.beautifulFilePath(fileOut.getCanonicalPath());
            String fileViewName = filePath.substring(filePath.lastIndexOf("/"));

            return writeBack2EC(appId, address, apiUrl, categoryID, filePath, fileViewName);
        } catch (Exception e) {
            log.error(e);
            return new WriteBackResult(false, e.getMessage());
        }
    }


    /**
     * 调用Ecology文档模块的文件上传接口，将本地文件上传到系统中
     *
     * @param address      Ecology服务的URL（http://127.0.0.1)
     * @param api          文件上传api的URL（/api/doc/upload/uploadFile2Doc）
     * @param category     Ecology系统中存储此文件的“文档目录ID”（123）
     * @param localFile    文件本地路径
     * @param fileViewName 文件的显示名
     * @return JSON格式消息
     */
    private static WriteBackResult writeBack2EC(String appId, String address, String api, String category,
                                                String localFile, String fileViewName) {
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }
        if (!api.startsWith("/")) {
            api = "/" + api;
        }

        JSONObject jsonUpload = EcologyUploadUtil.uploadFile2Ecology(appId, address, api, localFile, category, fileViewName);
        int intFileID = jsonUpload.getJSONObject("data").getInt("fileid");

        return new WriteBackResult(intFileID != -1,
                "Upload " + localFile + " to Ecology " + (intFileID != -1 ? "success." : "error."),
                String.valueOf(intFileID));
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
