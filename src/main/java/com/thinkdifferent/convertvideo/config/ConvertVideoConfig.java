package com.thinkdifferent.convertvideo.config;

import com.thinkdifferent.convertvideo.utils.SystemUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class ConvertVideoConfig {

    public static String inPutTempPath;
    @Value("${convert.video.inPutTempPath}")
    public void setInPutTempPath(String inPutTempPath) {
        inPutTempPath = SystemUtil.beautifulPath(inPutTempPath);
        if(inPutTempPath.endsWith("intemp/")){
            inPutTempPath = inPutTempPath + "intemp/";
        }
        ConvertVideoConfig.inPutTempPath = inPutTempPath;
    }

    public static String outPutPath;
    @Value(value = "${convert.video.outPutPath}")
    public void setOutPutPath(String outPutPath) {
        outPutPath = SystemUtil.beautifulPath(outPutPath);
        if(outPutPath.endsWith("outtemp/")){
            outPutPath = outPutPath + "outtemp/";
        }
        ConvertVideoConfig.outPutPath = outPutPath;
    }

    public static String ffmpegFile;
    @Value(value = "${convert.video.ffmpeg.file}")
    public void setFfmpegFile(String ffmpegFile) {
        ConvertVideoConfig.ffmpegFile = ffmpegFile;
    }

    public static int ffmpegThreads;
    @Value(value = "${convert.video.ffmpeg.threads}")
    public void setFfmpegThreads(int ffmpegThreads) {
        ConvertVideoConfig.ffmpegThreads = ffmpegThreads;
    }

    public static String ffmpegVideoCode;
    @Value(value = "${convert.video.ffmpeg.videoCode}")
    public void setFfmpegVideoCode(String ffmpegVideoCode) {
        if(StringUtils.isEmpty(ffmpegVideoCode)){
            ffmpegVideoCode = "libx264";
        }
        ConvertVideoConfig.ffmpegVideoCode = ffmpegVideoCode;
    }

    public static int ffmpegFps;
    @Value(value = "${convert.video.ffmpeg.fps}")
    public void setFfmpegFps(int ffmpegFps) {
        ConvertVideoConfig.ffmpegFps = ffmpegFps;
    }

    public static String ffmpegResolution;
    @Value(value = "${convert.video.ffmpeg.resolution}")
    public void setFfmpegResolution(String ffmpegResolution) {
        ConvertVideoConfig.ffmpegResolution = ffmpegResolution;
    }

    public static String ffmpegAudioCode;
    @Value(value = "${convert.video.ffmpeg.audioCode}")
    public void setFfmpegAudioCode(String ffmpegAudioCode) {
        if(StringUtils.isEmpty(ffmpegAudioCode)){
            ffmpegAudioCode = "aac";
        }
        ConvertVideoConfig.ffmpegAudioCode = ffmpegAudioCode;
    }

//    public static String ffmpegPicFile;
//    @Value(value = "${convert.video.ffmpeg.picMark.picFile}")
//    public void setFfmpegPicFile(String ffmpegPicFile) {
//        ConvertVideoConfig.ffmpegPicFile = ffmpegPicFile;
//    }
//
//    public static String ffmpegPicParam;
//    @Value(value = "${convert.video.ffmpeg.picMark.param}")
//    public void setFfmpegPicParam(String ffmpegPicParam) {
//        ConvertVideoConfig.ffmpegPicParam = ffmpegPicParam;
//    }
//
//    public static String ffmpegFontFile;
//    @Value(value = "${convert.video.ffmpeg.textMark.fontFile}")
//    public void setFfmpegFontFile(String ffmpegFontFile) {
//        ConvertVideoConfig.ffmpegFontFile = ffmpegFontFile;
//    }
//
//    public static String ffmpegText;
//    @Value(value = "${convert.video.ffmpeg.textMark.text}")
//    public void setFfmpegText(String ffmpegText) {
//        ConvertVideoConfig.ffmpegText = ffmpegText;
//    }
//
//    public static int ffmpegLocalX;
//    @Value(value = "${convert.video.ffmpeg.textMark.localX}")
//    public void setFfmpegLocalX(int ffmpegLocalX) {
//        ConvertVideoConfig.ffmpegLocalX = ffmpegLocalX;
//    }
//
//    public static int ffmpegLocalY;
//    @Value(value = "${convert.video.ffmpeg.textMark.localY}")
//    public void setFfmpegLocalY(int ffmpegLocalY) {
//        ConvertVideoConfig.ffmpegLocalY = ffmpegLocalY;
//    }
//
//    public static int ffmpegFontSize;
//    @Value(value = "${convert.video.ffmpeg.textMark.fontSize}")
//    public void setFfmpegFontSize(int ffmpegFontSize) {
//        ConvertVideoConfig.ffmpegFontSize = ffmpegFontSize;
//    }
//
//    public static String ffmpegFontColor;
//    @Value(value = "${convert.video.ffmpeg.textMark.fontColor}")
//    public void setFfmpegFontColor(String ffmpegFontColor) {
//        ConvertVideoConfig.ffmpegFontColor = ffmpegFontColor;
//    }


}
