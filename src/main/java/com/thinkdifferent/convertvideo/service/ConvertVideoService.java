package com.thinkdifferent.convertvideo.service;

import com.thinkdifferent.convertvideo.entity.CallBackResult;
import net.sf.json.JSONObject;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface ConvertVideoService {
    @Async
    void asyncConvertVideo(Map<String, Object> parameters);

    /**
     * 将传入的JSON对象中记录的文件，转换为MP4，输出到指定的目录中；回调应用系统接口，将数据写回。
     *
     * @param parameters 输入的参数，JSON格式数据对象
     */
    CallBackResult convertVideo(Map<String, Object> parameters);

    /**
     * 检测传入的对象是否正确, 不正确抛出异常，可获取异常信息返回
     *
     * @param jsonInput 传入的参数
     */
    void checkParams(JSONObject jsonInput) throws Exception;
}
