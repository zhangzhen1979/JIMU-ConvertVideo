package com.thinkdifferent.convertvideo.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ftp.FtpMode;
import cn.hutool.http.HttpUtil;
import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.utils.ConvertVideoUtils;
import com.thinkdifferent.convertvideo.utils.WriteBackUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConvertVideoServiceImpl implements ConvertVideoService {

    private static Logger log = LoggerFactory.getLogger(ConvertVideoServiceImpl.class);

    /**
     * 将传入的JSON对象中记录的文件，转换为MP4，输出到指定的目录中；回调应用系统接口，将数据写回。
     * @param parameters 输入的参数，JSON格式数据对象
     */
    public JSONObject ConvertVideo(Map<String, Object> parameters){
        JSONObject jsonReturn =  new JSONObject();
        jsonReturn.put("flag", "error");
        jsonReturn.put("message", "Convert Video to MP4 Error.");

        try{

            /**
             * 输入参数的JSON示例
             *{
             * 	"inputType": "path",
             * 	"inputFile": "D:/cvtest/001.MOV",
             * 	"inputHeaders":
             *  {
             *     		"Authorization":"Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
             *   },
             * 	"mp4FileName": "001-online",
             * 	"writeBackType": "path",
             * 	"writeBack":
             *   {
             *     		"path":"D:/cvtest/"
             *   },
             * 	"writeBackHeaders":
             *   {
             *     		"Authorization":"Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
             *   },
             * 	"callBackURL": "http://1234.com/callback"
             * }
             */
            // 输入类型（path/url）
            String strInputType = String.valueOf(parameters.get("inputType"));
            // 输入文件（"D:/cvtest/001.MOV"）
            String strInputPath = String.valueOf(parameters.get("inputFile"));
            String strInputPathParam = strInputPath;
            // 默认输出路径
            String strOutPutPath = ConvertVideoConfig.outPutPath;
            strOutPutPath = strOutPutPath.replaceAll("\\\\", "/");
            if(!strOutPutPath.endsWith("/")){
                strOutPutPath = strOutPutPath + "/";
            }

            File fileInput = null;

            // 如果输入类型是url，则通过http协议读取文件，写入到默认输出路径中
            if("url".equalsIgnoreCase(strInputType)){
                String strInputFileName = strInputPath.substring(strInputPath.lastIndexOf("/") + 1, strInputPath.length());
                // 检查目标文件夹中是否有重名文件，如果有，先删除。
                fileInput = new File(strOutPutPath+strInputFileName);
                if(fileInput.exists()){
                    fileInput.delete();
                }

                // 从指定的URL中将文件读取下载到目标路径
                HttpUtil.downloadFile(strInputPath, strOutPutPath + strInputFileName);

                strInputPath = strOutPutPath+strInputFileName;
            }


            // ffmpeg程序所在路径和文件名
            String strFFmpegPath = ConvertVideoConfig.ffmpegPath;
            // 转换出来的mp4的文件名（不包含扩展名）（"001-online"）
            String strMp4FileName = String.valueOf(parameters.get("mp4FileName"));
            // 文件回写方式（回写路径[path]/回写接口[api]/ftp回写[ftp]）
            String strWriteBackType = "path";
            JSONObject jsonWriteBack = new JSONObject();
            if(parameters.get("writeBackType")!=null){
                strWriteBackType = String.valueOf(parameters.get("writeBackType"));

                // 回写接口或回写路径
                jsonWriteBack = JSONObject.fromObject(parameters.get("writeBack"));
                if("path".equalsIgnoreCase(strWriteBackType)){
                    strOutPutPath = jsonWriteBack.getString("path");
                }
            }

            ConvertVideoUtils convertVideoUtils = new ConvertVideoUtils(strInputPath, strOutPutPath, strFFmpegPath, strMp4FileName);
            boolean blnSuccess = convertVideoUtils.setVoidInfos();

            if(blnSuccess){
                log.info("视频文件[" + strInputPathParam + "]转换成功");

                String strMp4FilePathName = strOutPutPath + strMp4FileName + ".mp4";
                File fileMp4 = new File(strMp4FilePathName);

                if("url".equalsIgnoreCase(strInputType)) {
                    if (fileInput.exists()) {
                        fileInput.delete();
                    }
                }

                if(!"path".equalsIgnoreCase(strWriteBackType)){
                   // 回写文件
                    Map mapWriteBackHeaders = new HashMap<>();
                    if(parameters.get("writeBackHeaders") != null){
                        mapWriteBackHeaders = (Map)parameters.get("writeBackHeaders");
                    }

                    if("url".equalsIgnoreCase(strWriteBackType)){
                        String strWriteBackURL = jsonWriteBack.getString("url");
                        jsonReturn = WriteBackUtil.writeBack2Api(strMp4FilePathName, strWriteBackURL, mapWriteBackHeaders);
                    }else if("ftp".equalsIgnoreCase(strWriteBackType)){
                        // ftp回写
                        boolean blnPassive = jsonWriteBack.getBoolean("passive");
                        String strFtpHost = jsonWriteBack.getString("host");
                        int intFtpPort = jsonWriteBack.getInt("port");
                        String strFtpUserName = jsonWriteBack.getString("username");
                        String strFtpPassWord = jsonWriteBack.getString("password");
                        String strFtpFilePath = jsonWriteBack.getString("filepath");

                        boolean blnFptSuccess = false;
                        FileInputStream in=new FileInputStream(fileMp4);

                        Ftp ftp = null;
                        try {
                            if(blnPassive){
                                // 服务器需要代理访问，才能对外访问
                                FtpConfig ftpConfig = new FtpConfig(strFtpHost, intFtpPort,
                                        strFtpUserName, strFtpPassWord,
                                        CharsetUtil.CHARSET_UTF_8);
                                ftp = new Ftp(ftpConfig, FtpMode.Passive);
                            }else{
                                // 服务器不需要代理访问
                                ftp = new Ftp(strFtpHost, intFtpPort,
                                        strFtpUserName, strFtpPassWord);
                            }

                            blnFptSuccess =  ftp.upload(strFtpFilePath, fileMp4.getName(), in);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (ftp != null) {
                                    ftp.close();
                                }

                                if(in != null){
                                    in.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if(blnFptSuccess){
                            jsonReturn.put("flag", "success");
                            jsonReturn.put("message", "Upload MP4 file to FTP success.");
                        }else{
                            jsonReturn.put("flag", "error");
                            jsonReturn.put("message", "Upload MP4 file to FTP error.");
                        }

                    }

                    String strFlag = jsonReturn.getString("flag");
                    if("success".equalsIgnoreCase(strFlag)){
                        if(fileMp4.exists()){
                            fileMp4.delete();
                        }
                    }

                    // 回调对方系统提供的CallBack方法。
                    if(parameters.get("callBackURL")!=null){
                        String strCallBackURL = String.valueOf(parameters.get("callBackURL"));

                        Map mapCallBackHeaders = new HashMap<>();
                        if (parameters.get("callBackHeaders") != null) {
                            mapCallBackHeaders = (Map) parameters.get("callBackHeaders");
                        }

                        Map mapParams = new HashMap<>();
                        mapParams.put("file", strMp4FileName);
                        mapParams.put("flag", strFlag);

                        jsonReturn = callBack(strCallBackURL, mapCallBackHeaders, mapParams);
                    }

                }else{
                    jsonReturn.put("flag", "success");
                    jsonReturn.put("message", "Convert Video to MP4 success.");
                }

            }else{
                log.info("视频文件[" + strInputPathParam + "]转换失败");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return jsonReturn;

    }

    /**
     * 回调业务系统提供的接口
     * @param strWriteBackURL 回调接口URL
     * @param mapWriteBackHeaders 请求头参数
     * @param mapParams 参数
     * @return JSON格式的返回结果
     */
    private static JSONObject callBack(String strWriteBackURL, Map<String,String> mapWriteBackHeaders, Map<String, Object> mapParams){
        //发送get请求并接收响应数据
        String strResponse = HttpUtil.createGet(strWriteBackURL).
                addHeaders(mapWriteBackHeaders).form(mapParams)
                .execute().body();

        JSONObject jsonReturn = new JSONObject();
        if(strResponse != null){
            jsonReturn.put("flag", "success");
            jsonReturn.put("message", "Convert Office File Callback Success.\n" +
                    "Message is :\n" +
                    strResponse);
        }

        return jsonReturn;
    }


}
