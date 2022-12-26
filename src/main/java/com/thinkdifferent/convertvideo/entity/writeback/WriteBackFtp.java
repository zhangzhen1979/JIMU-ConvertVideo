package com.thinkdifferent.convertvideo.entity.writeback;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ftp.FtpMode;
import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 10:18
 */
@Log4j2
public class WriteBackFtp extends WriteBack {
    /**
     * 是否是被动模式
     */
    private boolean passive;
    /**
     * ftp服务的访问地址
     */
    private String host;
    /**
     * ftp服务的访问端口
     */
    private Integer port;
    /**
     * ftp服务的用户名
     */
    private String username;
    /**
     * ftp服务的密码
     */
    private String password;
    /**
     * 文件所在路径
     */
    private String filePath;

    @Override
    public WriteBack of(Map<String, Object> writeBack) {
        WriteBackFtp writeBackFtp = new WriteBackFtp();
        writeBackFtp.setPassive(MapUtil.getBool(writeBack, "passive", false));
        String host = MapUtil.getStr(writeBack, "host");
        if (StringUtils.startsWith(host, "ftp://")) {
            host = host.substring(6);
        }
        writeBackFtp.setHost(host);
        writeBackFtp.setPort(MapUtil.getInt(writeBack, "port", 21));
        writeBackFtp.setUsername(MapUtil.getStr(writeBack, "username"));
        writeBackFtp.setPassword(MapUtil.getStr(writeBack, "password"));
        writeBackFtp.setFilePath(MapUtil.getStr(writeBack, "filepath"));
        return writeBackFtp;
    }

    /**
     * 转换结果回写
     *
     * @param fileOut 转换后的文件
     */
    @Override
    public WriteBackResult writeBack(File fileOut) {
        Ftp ftp = null;
        try {
            if (passive) {
                // 服务器需要代理访问，才能对外访问
                FtpConfig ftpConfig = new FtpConfig(host, port, username, password, CharsetUtil.CHARSET_UTF_8);
                ftp = new Ftp(ftpConfig, FtpMode.Passive);
            } else {
                // 服务器不需要代理访问
                ftp = new Ftp(host, port, username, password);
            }

            return new WriteBackResult(ftp.upload(filePath, fileOut));
        } catch (Exception e) {
            log.error(e);
            return new WriteBackResult(false, e.getMessage());
        } finally {
            IOUtils.closeQuietly(ftp);
        }
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
