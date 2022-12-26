package com.thinkdifferent.convertvideo.utils;

import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import com.thinkdifferent.convertvideo.entity.input.InputFtp;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

/**
 * ftp工具类
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/21 14:57
 */
@Log4j2
public class FtpUtil {
    /**
     * 默认的FTP模式
     */
    private static final FtpMode DEFAULT_FTP_MODE = FtpMode.Passive;

    private FtpUtil() {
    }

    /**
     * ftp 文件下载
     *
     * @param strFtpFilePath 例： ftp://192.168.1.1:21/path/file1
     * @param downloadFile   输出文件
     */
    public static void downloadFile(String strFtpFilePath, File downloadFile) {
        Assert.hasText(strFtpFilePath, "路径不能为空");
        Assert.isTrue(strFtpFilePath.startsWith("ftp://"), "FTP文件格式错误");
        // 解析路径，获取host\端口\路径\文件名
        String subFtpFilePath = strFtpFilePath.substring(6);
        // 用户、密码
        String strUserName, password;
        if (subFtpFilePath.contains("@")) {
            strUserName = subFtpFilePath.substring(0, subFtpFilePath.indexOf(":"));
            password = subFtpFilePath.substring(subFtpFilePath.indexOf(":") + 1, subFtpFilePath.indexOf("@"));
            // 获取完用户密码后
            subFtpFilePath = subFtpFilePath.substring(subFtpFilePath.indexOf("@") + 1);
        } else {
            strUserName = null;
            password = null;
        }

        // 文件路径开始下标
        int index = subFtpFilePath.contains(":") ? subFtpFilePath.indexOf(":") : subFtpFilePath.indexOf("/");
        String host = subFtpFilePath.substring(0, index);
        int port = subFtpFilePath.contains(":") ? Integer.parseInt(subFtpFilePath.substring(index, subFtpFilePath.indexOf("/"))) : 21;
        String path = subFtpFilePath.substring(subFtpFilePath.indexOf("/"));

        // 服务器不需要代理访问
        Ftp ftp = new Ftp(host, port, strUserName, password);
        // 切换目录
        ftp.cd(path);
        ftp.download(path, downloadFile);
        IOUtils.closeQuietly(ftp);
    }

    /**
     * 下载FTP文件
     *
     * @param inputFtp     ftp配置
     * @param downloadFile 保存的文件
     */
    public static void downloadFile(InputFtp inputFtp, File downloadFile) {
        // 服务器不需要代理访问
        FtpMode ftpMode = FtpMode.Passive;
        Ftp ftp = new Ftp(inputFtp.getHost(), inputFtp.getPort(), inputFtp.getUsername(), inputFtp.getPassword(),
                null, null, null, ftpMode);
        // 切换目录
        ftp.cd(inputFtp.getFilePath());
        ftp.download(inputFtp.getFilePath(), downloadFile);
        IOUtils.closeQuietly(ftp);
    }

    /**
     * 判断FTP下文件是否存在
     *
     * @param inputFtp ftp对象
     * @return bln
     */
    public static boolean exist(InputFtp inputFtp) {
        try (Ftp ftp = new Ftp(inputFtp.getHost(), inputFtp.getPort(), inputFtp.getUsername(), inputFtp.getPassword(),
                null, null, null, DEFAULT_FTP_MODE)) {
            // 切换目录
            return ftp.exist(inputFtp.getFilePath());
        } catch (IOException e) {
            log.error("判断FTP文件【" + inputFtp.getFilePath() + "】是否存在异常", e);
            throw new RuntimeException("判断FTP文件【" + inputFtp.getFilePath() + "】是否存在异常");
        }
    }
}
