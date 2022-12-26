package com.thinkdifferent.convertvideo.task;

import com.thinkdifferent.convertvideo.config.SystemConstants;
import com.thinkdifferent.convertvideo.entity.CallBackResult;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.service.RabbitMQService;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONObject;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Log4j2
public class Task implements RabbitTemplate.ConfirmCallback {
    @Resource
    private RabbitMQService rabbitMQService;

    /**
     * 处理接收列表中的数据，异步多线程任务
     *
     * @param convertVideoService 创建PDF文件的Service对象
     * @param jsonInput           队列中待处理的JSON数据
     * @throws Exception
     */
    @Async("taskExecutor")
    public void doTask(ConvertVideoService convertVideoService, JSONObject jsonInput) {

        log.info("开始处理-转换MP4文件");
        long longStart = System.currentTimeMillis();

        log.info("MQ中存储的数据:" + jsonInput.toString());

        CallBackResult callBackResult = new CallBackResult(false);
        try {
            callBackResult = convertVideoService.convertVideo(jsonInput);
        } catch (Exception e) {
            log.error("转换MP4异常", e);
            callBackResult = new CallBackResult(false, e.getMessage());
        } finally {
            if (callBackResult.isFlag()) {
                // 成功，清理失败记录
                SystemConstants.removeErrorData(jsonInput);
            } else {
                // 异常情况重试
                rabbitMQService.setRetryData2MQ(jsonInput);
            }
        }
        long longEnd = System.currentTimeMillis();
        log.info("完成-转换MP4文件，耗时：" + (longEnd - longStart) + "毫秒");
    }


    /**
     * 回调反馈消费者消费信息
     *
     * @param correlationData
     * @param b
     * @param msg
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String msg) {
        log.info(" 回调id:" + correlationData);
        if (b) {
            log.info("消息成功消费");
        } else {
            log.info("消息消费失败:" + msg);
        }
    }


}
