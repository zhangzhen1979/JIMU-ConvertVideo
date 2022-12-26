package com.thinkdifferent.convertvideo.controller;

import com.thinkdifferent.convertvideo.config.SystemConstants;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/5/13 14:15
 */
@RestController
public class IndexController {

    @GetMapping
    public String index() {
        return "启动成功";
    }

    @ApiOperation("显示最近错误的数据，最大200条")
    @GetMapping("listError")
    public Collection<JSONObject> listError() {
        return SystemConstants.ERROR_CONVERT_DATA.values();
    }
}
