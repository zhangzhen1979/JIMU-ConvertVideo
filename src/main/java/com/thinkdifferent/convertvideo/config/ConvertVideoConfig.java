package com.thinkdifferent.convertvideo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class ConvertVideoConfig {

    public static String outPutPath;
    @Value(value = "${convert.video.outPutPath}")
    public void setOutPutPath(String outPutPath) {
        ConvertVideoConfig.outPutPath = outPutPath;
    }

    public static String ffmpegPath;
    @Value(value = "${convert.video.ffmpegPath}")
    public void setFfmpegPath(String ffmpegPath) {
        ConvertVideoConfig.ffmpegPath = ffmpegPath;
    }

}
