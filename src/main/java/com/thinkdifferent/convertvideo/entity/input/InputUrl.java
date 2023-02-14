package com.thinkdifferent.convertvideo.entity.input;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.utils.SystemUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * web地址
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 11:03
 */
@Log4j2
public class InputUrl extends Input {
    /**
     * 需转换的输入文件在Web服务中的URL地址
     */
    private String url;
    private String fileType;

    @Override
    public Input of(String inputPath, String strExt) {
        InputUrl path = new InputUrl();
        path.setUrl(inputPath);
        path.setFileType(strExt);
        return path;
    }

    @Override
    public File getInputFile() {
        // 文件临时存储路径
        String strTempPath = SystemUtil.beautifulPath(ConvertVideoConfig.inPutTempPath);
        if (super.inputFile == null) {
            String strInputFileName = getFileNameFromHeader();
            // 移除重名文件
            FileUtil.del(strTempPath + strInputFileName);
            // 从指定的URL中将文件读取下载到目标路径
            long length = HttpUtil.downloadFile(url, strTempPath + strInputFileName);
            if (length>0L) {
                super.setInputFile(new File(strTempPath + strInputFileName));
            }else{
                log.warn("下载文件【{}】为空", this.url);
                FileUtil.del(strTempPath + strInputFileName);
                return null;
            }
        }
        return super.inputFile;
    }

    @SneakyThrows
    private String getFileNameFromHeader() {
        String fileName;
        URL url = new URL(this.url);
        URLConnection uc = url.openConnection();

        String uuid = "";
        if (this.url.contains("?") && this.url.contains("uuid=")) {
            // 取UUID
            String subUuid = this.url.substring(this.url.indexOf("uuid="));
            uuid = subUuid.substring(5, subUuid.contains("&") ? subUuid.indexOf("&") : subUuid.length());
        }

        String realFileName = "";
        // 优先从header获取， 获取失败截取http请求最后内容
        String headerField = uc.getHeaderField("Content-Disposition");
        if (StringUtils.isNotBlank(headerField)) {
            fileName = new String(headerField.getBytes(StandardCharsets.ISO_8859_1), "GBK");
            realFileName = URLDecoder.decode(fileName.substring(fileName.indexOf("filename=") + 9), "UTF-8");
        } else {
            realFileName = UUID.randomUUID().toString() + "." + this.fileType;
        }
        return StringUtils.isNotBlank(uuid) ? (uuid + realFileName.substring(realFileName.lastIndexOf("."))) : realFileName;
    }

    /**
     * 判断传入的文件是否存在
     * http存在标准为可以下载到
     *
     * @return bln
     */
    @Override
    public boolean exist() {
        File file = getInputFile();
        return Objects.nonNull(file) && file.exists();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

}
