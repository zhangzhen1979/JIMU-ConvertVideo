package com.thinkdifferent.convertvideo.service.impl;

import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.utils.ConvertVideoUtils;
import com.thinkdifferent.convertvideo.utils.FtpUtil;
import com.thinkdifferent.convertvideo.utils.GetFileUtil;
import com.thinkdifferent.convertvideo.utils.WriteBackUtil;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConvertVideoServiceImpl implements ConvertVideoService {

    private static Logger logger = LoggerFactory.getLogger(ConvertVideoUtils.class);

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

                // 从指定的URL中将文件读取为Byte数组，并写入目标文件
                Map mapInputHeaders = new HashMap<>();
                if(parameters.get("inputHeaders") != null){
                    mapInputHeaders = (Map)parameters.get("inputHeaders");
                }

                byte[] byteFile = GetFileUtil.getFile(strInputPath, mapInputHeaders);
                fileInput = GetFileUtil.byte2File(byteFile, strOutPutPath+strInputFileName);

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
                logger.info("视频文件[" + strInputPathParam + "]转换成功");

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
                        String strFtpHost = jsonWriteBack.getString("host");
                        int intFtpPort = jsonWriteBack.getInt("port");
                        String strFtpUserName = jsonWriteBack.getString("username");
                        String strFtpPassWord = jsonWriteBack.getString("password");
                        String strFtpBasePath = jsonWriteBack.getString("basepath");
                        String strFtpFilePath = jsonWriteBack.getString("filepath");

                        FileInputStream in=new FileInputStream(fileMp4);
                        boolean blnFptSuccess = FtpUtil.uploadFile(strFtpHost, intFtpPort, strFtpUserName, strFtpPassWord,
                                strFtpBasePath, strFtpFilePath, strMp4FileName + ".mp4", in);

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
                        strCallBackURL = strCallBackURL + "?file=" + strMp4FileName + "&flag=" + strFlag;

                        WriteBackUtil.sendGet(strCallBackURL);
                    }

                }else{
                    jsonReturn.put("flag", "success");
                    jsonReturn.put("message", "Convert Video to MP4 success.");
                }

            }else{
                logger.info("视频文件[" + strInputPathParam + "]转换失败");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return jsonReturn;

    }


}
