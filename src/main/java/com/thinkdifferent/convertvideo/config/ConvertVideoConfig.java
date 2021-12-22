package com.thinkdifferent.convertvideo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class ConvertVideoConfig {

    @Value(value = "${convert.video.outPutPath}")
    public static String outPutPath;

    @Value(value = "${convert.video.ffmpegPath}")
    public static String ffmpegPath;

}
