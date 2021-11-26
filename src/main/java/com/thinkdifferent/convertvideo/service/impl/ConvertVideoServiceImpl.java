package com.thinkdifferent.convertvideo.service.impl;

import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.utils.ConvertVideoUtils;
import com.thinkdifferent.convertvideo.utils.FtpUtil;
import net.sf.json.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
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

                byte[] byteFile = getFile(strInputPath, mapInputHeaders);
                fileInput = byte2File(byteFile, strOutPutPath+strInputFileName);

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
                        jsonReturn = writeBack2Api(strMp4FilePathName, strWriteBackURL, mapWriteBackHeaders);
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

                        sendGet(strCallBackURL);
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


    /**
     * 从url中以get方式获取文件。可以传入Headers
     * @param strURL 文件访问URL
     * @param mapHeaders Headers参数
     * @return
     * @throws IOException
     */
    private static byte[] getFile(String strURL, Map<String, String> mapHeaders) throws IOException {
        logger.info("Ready Get Request Url[{}]", strURL);
        HttpGet httpGet = new HttpGet(strURL);
        httpGet.setHeaders(getHeaders(mapHeaders));
        HttpResponse httpResponse = HttpClients.createDefault().execute(httpGet);
        if (null == httpResponse || httpResponse.getStatusLine() == null) {
            logger.info("Post Request For Url[{}] is not ok. Response is null", strURL);
            return null;
        } else if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            logger.info("Post Request For Url[{}] is not ok. Response Status Code is {}", strURL,
                    httpResponse.getStatusLine().getStatusCode());
            return null;
        }
        return EntityUtils.toByteArray(httpResponse.getEntity());
    }

    /**
     * 将传入的Map参数转换为Header对象数组
     * @param mapHeaders Header参数
     * @return Header对象数组
     */
    private static Header[] getHeaders(Map<String, String> mapHeaders){
        Header[] headers = new Header[mapHeaders.size()];

        int i = 0;
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()) {
            logger.info("Header -- key = " + entry.getKey() + ", value = " + entry.getValue());
            headers[i] = new BasicHeader(entry.getKey(), entry.getValue());
            i++;
        }

        return headers;
    }

    /**
     * 将byte数组写入到文件
     * @param byteArray 文件的byte数组
     * @param strTargetPath 目标文件夹和文件名
     * @return File文件对象
     */
    public static File byte2File(byte[] byteArray, String strTargetPath) {
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        File file = new File(strTargetPath);
        String strPath = strTargetPath.substring(0, strTargetPath.lastIndexOf("/"));
        if (!file.exists()) {
            new File(strPath).mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            int intLen = 0;
            byte[] byteBuf = new byte[1024];
            while ((intLen = inputStream.read(byteBuf)) != -1) {
                fos.write(byteBuf, 0, intLen);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }

    /**
     * 调用API接口，将文件上传
     * @param strFilePathName 文件路径和文件名
     * @param strUrl API接口的URL
     * @param mapHeader Header参数
     * @return 接口返回的JSON
     * @throws Exception
     */
    public static JSONObject writeBack2Api(String strFilePathName, String strUrl, Map<String, Object> mapHeader)
            throws Exception {

        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error");
        jsonReturn.put("message", "Call Back Error. URL = " + strUrl);

        // 换行符
        final String strNewLine = "\r\n";
        final String strBoundaryPrefix = "--";
        // 定义数据分隔线
        String strBOUNDARY = "========7d4a6d158c9";
        // 服务器的域名
        URL url = new URL(strUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        // 设置为POST情
        httpURLConnection.setRequestMethod("POST");
        // 发送POST请求必须设置如下两行
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setUseCaches(false);
        // 设置请求头参数
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + strBOUNDARY);
        try (
                OutputStream outputStream = httpURLConnection.getOutputStream();
                DataOutputStream out = new DataOutputStream(outputStream);
        ) {
            //传递参数
            if (mapHeader != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Object> entry : mapHeader.entrySet()) {
                    stringBuilder.append(strBoundaryPrefix)
                            .append(strBOUNDARY)
                            .append(strNewLine)
                            .append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey())
                            .append("\"").append(strNewLine).append(strNewLine)
                            .append(String.valueOf(entry.getValue()))
                            .append(strNewLine);
                }
                out.write(stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
            }

            // 上传文件
            {
                File file = new File(strFilePathName);
                StringBuilder sb = new StringBuilder();
                sb.append(strBoundaryPrefix);
                sb.append(strBOUNDARY);
                sb.append(strNewLine);
                sb.append("Content-Disposition: form-data;name=\"file\";filename=\"").append(strFilePathName)
                        .append("\"").append(strNewLine);
                sb.append("Content-Type:application/octet-stream");
                sb.append(strNewLine);
                sb.append(strNewLine);
                out.write(sb.toString().getBytes());

                try (
                        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                ) {
                    byte[] byteBufferOut = new byte[1024];
                    int intBytes = 0;
                    while ((intBytes = dataInputStream.read(byteBufferOut)) != -1) {
                        out.write(byteBufferOut, 0, intBytes);
                    }
                    out.write(strNewLine.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
            byte[] byteEndData = (strNewLine + strBoundaryPrefix + strBOUNDARY + strBoundaryPrefix + strNewLine)
                    .getBytes();
            // 写上结尾标识
            out.write(byteEndData);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //定义BufferedReader输入流来读取URL的响应
        try (
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            String strLine = null;
            StringBuffer sb = null;
            while ((strLine = reader.readLine()) != null) {
                System.out.println(strLine);
                sb.append(strLine);
            }

            jsonReturn = JSONObject.fromObject(sb);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonReturn;
    }


    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    /**
     * 发送HttpGet请求
     * @param strURL API的URL地址
     * @return 响应的字符串内容
     */
    public static String sendGet(String strURL) {


        HttpGet httpGet = new HttpGet(strURL);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String strResult = null;
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                strResult = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }


}
