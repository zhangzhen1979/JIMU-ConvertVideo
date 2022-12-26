package com.thinkdifferent.convertvideo.controller;

import com.thinkdifferent.convertvideo.config.RabbitMQConfig;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.service.RabbitMQService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@Api(tags = "根据传入的JSON生成MP4文件")
@Log4j2
@RestController
@RequestMapping(value = "/api")
public class ConvertVideoController {

    @Autowired
    private ConvertVideoService convertVideoService;

    @Autowired
    private RabbitMQService rabbitMQService;

    @ApiOperation("接收传入的JSON数据，将原文文件转换为Mp4文件")
    @RequestMapping(value = "/convert", method = RequestMethod.POST)
    public Callable<JSONObject> convert(@RequestBody JSONObject jsonInput) {
        return () -> {
            JSONObject jsonReturn = new JSONObject();
            jsonReturn.put("flag", "success");
            try {
                // 检测对象是否存在，不存在会抛出异常
                convertVideoService.checkParams(jsonInput);

                if (!RabbitMQConfig.producer) {
                    convertVideoService.asyncConvertVideo(jsonInput);
                    jsonReturn.put("message", "async convert Success");
                } else {
                    rabbitMQService.setData2MQ(jsonInput);
                    jsonReturn.put("message", "Set Data to MQ Success");
                }
            }catch (Exception e){
                jsonReturn.put("flag", "false");
                jsonReturn.put("message", e.getMessage());
                log.error("转换视频异常", e);
                log.error("输入数据", jsonInput.toString());

            }
            return jsonReturn;
        };
    }

}
