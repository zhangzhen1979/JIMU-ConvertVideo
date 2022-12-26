package com.thinkdifferent.convertvideo.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.OsInfo;
import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.config.SystemConstants;
import com.thinkdifferent.convertvideo.entity.CallBackResult;
import com.thinkdifferent.convertvideo.entity.ConvertEntity;
import com.thinkdifferent.convertvideo.entity.WriteBackResult;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.service.RabbitMQService;
import com.thinkdifferent.convertvideo.utils.ConvertVideoUtils;
import com.thinkdifferent.convertvideo.utils.WriteBackUtil;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class ConvertVideoServiceImpl implements ConvertVideoService {

    @Autowired
    private RabbitMQService rabbitMQService;

    /**
     * 异步处理转换
     *
     * @param parameters 输入的参数，JSON格式数据对象
     */
    @Async
    @Override
    public void asyncConvertVideo(Map<String, Object> parameters) {
        CallBackResult callBackResult = convertVideo(parameters);
        if (callBackResult.isFlag()) {
            // 成功，清理失败记录
            SystemConstants.removeErrorData((JSONObject) parameters);
        } else {
            // 异常情况重试
            rabbitMQService.setRetryData2MQ((JSONObject) parameters);
        }
    }

    /**
     * V2 版本
     * 将传入的JSON对象中记录的文件，转换为MP4，输出到指定的目录中；回调应用系统接口，将数据写回。
     *
     * @param parameters 输入的参数，JSON格式数据对象
     */
    @Override
    public CallBackResult convertVideo(Map<String, Object> parameters) {
        try {
            // 参数转换
            ConvertEntity convertEntity = new ConvertEntity().of(parameters);
            // 下载文件
            File inputFile = convertEntity.getInput().getInputFile();
            // 将传入的文件转换为MP4文件，存放到输出路径中
            ConvertVideoUtils convertVideoUtils = new ConvertVideoUtils(inputFile.getCanonicalPath(),
                    convertEntity);
            // 转换结果
            boolean blnSuccess = convertVideoUtils.setVoidInfos();

            File fileOut = new File(convertEntity.getWriteBack().getOutputPath() + convertEntity.getOutPutFileName() + "." + convertVideoUtils.getExt());

            // 转换结果回写
            WriteBackResult writeBackResult = new WriteBackResult(true);
            // 校验文件有效性
            if (blnSuccess && checkMp4File(fileOut)) {
                // 回调对方系统
                writeBackResult = WriteBackUtil.writeBack(convertEntity.getWriteBack(), fileOut);
                if (writeBackResult.isFlag()) {
                    // 不需要回写 （转换失败） 回写成功   都意味着回写操作完成
                    if (StringUtils.isBlank(writeBackResult.getFile())) {
                        writeBackResult.setFile(fileOut.getName());
                    }
                }
            } else {
                // 转换错误，回调
                writeBackResult.setFlag(false).setMessage("转换视频文件/截图失败");
            }
            CallBackResult callBackResult = callBack(convertEntity.getCallBackURL(), convertEntity.getCallBackHeaders(), writeBackResult);

            // 清理文件
            if (!"path".equalsIgnoreCase(convertEntity.getWriteBackType().name())) {
                FileUtil.del(fileOut);
            }
            return callBackResult;
        } catch (Exception e) {
            log.error("文件[" + parameters.get("inputFile") + "]转换/截图失败", e);
            return new CallBackResult(false, e.getMessage());
        }
    }

    /**
     * 检测传入的对象是否正确, 不正确抛出异常，可获取异常信息返回
     *
     * @param jsonInput 传入的参数
     */
    @Override
    public void checkParams(JSONObject jsonInput) {
        // 参数转换
        ConvertEntity convertEntity = new ConvertEntity().of(jsonInput);
        // 判断文件是否存在
        if (!convertEntity.getInput().exist()) {
            throw new RuntimeException("文件不存在");
        }
    }

    /**
     * 回调业务系统提供的接口
     *
     * @param strCallBackURL      回调接口URL
     * @param mapWriteBackHeaders 请求头参数
     * @param writeBackResult     参数
     */
    private static CallBackResult callBack(String strCallBackURL, Map<String, String> mapWriteBackHeaders, WriteBackResult writeBackResult) {
        if (StringUtils.isBlank(strCallBackURL)) {
            return new CallBackResult(true, "Convert Video to MP4 success.");
        }
        //发送get请求并接收响应数据
        try (HttpResponse httpResponse = HttpUtil.createGet(strCallBackURL).
                addHeaders(mapWriteBackHeaders).form(writeBackResult.bean2Map())
                .execute()) {
            String body = httpResponse.body();
            log.info("回调请求地址:{}, 请求体:{},状态码：{}，结果：{}", strCallBackURL, writeBackResult, httpResponse.isOk(), body);
            if (httpResponse.isOk() && writeBackResult.isFlag()) {
                // 回调成功且转换成功，任务才会结束
                return new CallBackResult(true, "Convert Video to MP4 Callback Success.\n" +
                        "Message is :\n" +
                        body);
            } else {
                return new CallBackResult(false, "CallBack error, resp: " + body + ", writeBackResult=" + writeBackResult);
            }
        }
    }

    /**
     * 检测 mp4 文件的有效性
     *
     * @param mp4File 需要检测的文件
     * @return bln
     */
    public static boolean checkMp4File(File mp4File) {
        if (!mp4File.exists()) {
            return false;
        }
        try {
            List<String> listCommand = new ArrayList<>();
            listCommand.add(ConvertVideoConfig.ffmpegFile);
            listCommand.add("-i");
            listCommand.add(mp4File.getCanonicalPath());
            if (new OsInfo().isWindows()) {
                Process videoProcess = new ProcessBuilder(listCommand).redirectErrorStream(true).start();
                videoProcess.waitFor();

                return !(StringUtils.isNotBlank(IOUtils.toString(videoProcess.getErrorStream()))
                        || StringUtils.contains(IOUtils.toString(videoProcess.getInputStream()),
                        "Invalid data found when processing input"));
            } else {
                log.info("linux开始");
                StringBuilder strbTest = new StringBuilder();
                for (String s : listCommand) strbTest.append(s).append(" ");
                log.info(strbTest.toString());
                // 执行命令
                Process p = Runtime.getRuntime().exec(strbTest.toString());
                // 取得命令结果的输出流
                return !StringUtils.contains(IOUtils.toString(p.getInputStream()), "Invalid data found when processing input");
            }
        } catch (IOException | InterruptedException e) {
            log.error("校验mp4文件【" + mp4File.getName() + "】异常", e);
            return false;
        }
    }


}
