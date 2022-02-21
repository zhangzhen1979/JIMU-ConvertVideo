package com.thinkdifferent.convertvideo.controller;

import com.thinkdifferent.convertvideo.config.RabbitMQConfig;
import com.thinkdifferent.convertvideo.service.ConvertVideoService;
import com.thinkdifferent.convertvideo.service.RabbitMQService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags="根据传入的JSON生成MP4文件")
@RestController
@RequestMapping(value = "/api")
public class ConvertVideo {

    @Autowired
    private ConvertVideoService convertVideoService;

    @Autowired
    private RabbitMQService rabbitMQService;

    @ApiOperation("接收传入的JSON数据，将文件转换为Mp4文件")
    @RequestMapping(value = "/convert", method = RequestMethod.POST)
    public Map<String, String> data2PDF(@RequestBody JSONObject jsonInput) {
        JSONObject jsonReturn = new JSONObject();

        if(!RabbitMQConfig.producer){
            jsonReturn = convertVideoService.ConvertVideo(jsonInput);
        }else{
            jsonReturn.put("flag", "success" );
            jsonReturn.put("message", "Set Data to MQ Success" );

            rabbitMQService.setData2MQ(jsonInput);
        }


        return jsonReturn;
    }


}
