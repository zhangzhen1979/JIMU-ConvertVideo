package com.thinkdifferent.convertvideo.utils;

import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import com.thinkdifferent.convertvideo.entity.writeback.WriteBack;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class WriteBackUtil {

    /**
     * 回写
     *
     * @param writeBack 回写对象
     * @param fileOut   输出文件
     * @return jo
     */
    public static WriteBackResult writeBack(WriteBack writeBack, File fileOut) {
        if (Objects.isNull(writeBack)) {
            return new WriteBackResult(true);
        }
        return writeBack.writeBack(fileOut);
    }

    /**
     * 调用API接口，将文件上传
     *
     * @param strFilePathName 文件路径和文件名
     * @param strUrl          API接口的URL
     * @param mapHeader       Header参数
     * @return 接口返回的JSON
     * @throws Exception
     */
    public static WriteBackResult writeBack2Api(String strFilePathName, String strUrl, Map<String, String> mapHeader)
            throws Exception {
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
                for (Map.Entry<String, String> entry : mapHeader.entrySet()) {
                    stringBuilder.append(strBoundaryPrefix)
                            .append(strBOUNDARY)
                            .append(strNewLine)
                            .append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey())
                            .append("\"").append(strNewLine).append(strNewLine)
                            .append(entry.getValue())
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
                }
            }

            // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
            byte[] byteEndData = (strNewLine + strBoundaryPrefix + strBOUNDARY + strBoundaryPrefix + strNewLine)
                    .getBytes();
            // 写上结尾标识
            out.write(byteEndData);
            out.flush();

        }

        //定义BufferedReader输入流来读取URL的响应
        try (
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            String strLine;
            StringBuilder sb = new StringBuilder();
            while ((strLine = reader.readLine()) != null) {
                sb.append(strLine);
            }

            return new WriteBackResult(true, sb.toString());

        }

    }
}
