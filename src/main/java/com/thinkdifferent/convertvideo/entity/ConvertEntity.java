package com.thinkdifferent.convertvideo.entity;

import cn.hutool.core.map.MapUtil;
import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.entity.input.Input;
import com.thinkdifferent.convertvideo.entity.params.Params;
import com.thinkdifferent.convertvideo.entity.params.PicMark;
import com.thinkdifferent.convertvideo.entity.params.TextMark;
import com.thinkdifferent.convertvideo.entity.params.Thumbnail;
import com.thinkdifferent.convertvideo.entity.writeback.WriteBack;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 转换对象
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/13 13:49
 */
public class ConvertEntity {
    /**
     * 输入类型（path/url）
     */
    private InputType inputType;
    /**
     * 输入文件
     */
    private Input input;
    /**
     * url 请求头
     */
    private Map<String, String> inputHeaders;
    /**
     * 转换出来的MP4文件名（不包含扩展名）
     */
    private String outPutFileName;
    /**
     * 截屏得到的JPG文件名（不包含扩展名）
     */
    private String jpgFileName;
    /**
     * 转换参数
     */
    private Params params;
    /**
     * 文件回写方式（回写路径[path]/回写接口[api]/ftp回写[ftp]）
     */
    private WriteBackType writeBackType;
    /**
     * 回写接口或回写路径
     */
    private WriteBack writeBack;
    /**
     * 回调地址
     */
    private String callBackURL;
    /**
     * 回调请求头
     */
    private Map<String, String> callBackHeaders;

    /**
     * 图片水印相关默认值
     */
    @Value("${convert.video.ffmpeg.picMark.picFile:}")
    private String picFile;
    @Value("${convert.video.ffmpeg.picMark.overlay:}")
    private String picOverlay;
    @Value("${convert.video.ffmpeg.picMark.scale:}")
    private String picScale;

    /**
     * 文字水印相关默认值
     */
    @Value("${convert.video.ffmpeg.textMark.fontFile:}")
    private String fontFile;
    @Value("${convert.video.ffmpeg.textMark.text:}")
    private String text;
    @Value("${convert.video.ffmpeg.textMark.localX:}")
    private Integer localX;
    @Value("${convert.video.ffmpeg.textMark.localY:}")
    private Integer localY;
    @Value("${convert.video.ffmpeg.textMark.fontSize:}")
    private Integer fontSize;
    @Value("${convert.video.ffmpeg.textMark.fontColor:}")
    private String fontColor;

    /**
     * 输入对象转换为标准对象
     *
     * @param parameters 输入对象
     * @return ce
     */
    public ConvertEntity of(final Map<String, Object> parameters) {
        ConvertEntity entity = new ConvertEntity();
        // 输入类型（path/url/ftp）
        entity.setInputType(InputType.valueOf(String.valueOf(parameters.get("inputType")).toUpperCase()));
        // 输入文件（"D:/temp/001.avi" http://... ftp://root:12345@0.0.0.1/a/a.avi）
        entity.setInput(
                entity.getInputType().of(
                        String.valueOf(parameters.get("inputFile")),
                        String.valueOf(parameters.get("inputFileType"))
                )
        );
        // 转换出来的文件名（不包含扩展名）（"001-online"）
        if(parameters.containsKey("outPutFileName")) {
            entity.setOutPutFileName(MapUtil.getStr(parameters, "outPutFileName"));
        }
        if(parameters.containsKey("jpgFileName")) {
            entity.setJpgFileName(MapUtil.getStr(parameters, "jpgFileName"));
        }

        // 处理转换参数
        if(parameters.containsKey("params")){
            JSONObject joParams = JSONObject.fromObject(parameters.get("params"));

            Params params = new Params();

            if(joParams.containsKey("custom") && joParams.get("custom") != null){
                params.setCustom(joParams.optString("custom"));
            }else{
                // CPU进程数量
                if(joParams.containsKey("threads") && joParams.get("threads") != null){
                    // 有传入的参数，用传入的
                    params.setThreads(joParams.getInt("threads"));
                }else{
                    // 如果没有传入的，用配置文件设置的
                    params.setThreads(ConvertVideoConfig.ffmpegThreads);
                }

                // 视频编码格式
                if(joParams.containsKey("videoCode") && joParams.get("videoCode") != null){
                    // 有传入的参数，用传入的
                    params.setVideoCode(joParams.getString("videoCode"));
                }else{
                    // 如果没有传入的，用配置文件设置的
                    params.setVideoCode(ConvertVideoConfig.ffmpegVideoCode);
                }

                // 帧率
                if(joParams.containsKey("fps") && joParams.get("fps") != null){
                    // 有传入的参数，用传入的
                    params.setFps(joParams.getInt("fps"));
                }else{
                    // 如果没有传入的，用配置文件设置的
                    params.setFps(ConvertVideoConfig.ffmpegFps);
                }

                // 分辨率
                if(joParams.containsKey("resolution") && joParams.get("resolution") != null){
                    // 有传入的参数，用传入的
                    params.setResolution(joParams.getString("resolution"));
                }else{
                    // 如果没有传入的，用配置文件设置的
                    params.setResolution(ConvertVideoConfig.ffmpegResolution);
                }

                // 音频编码格式
                if(joParams.containsKey("audioCode") && joParams.get("audioCode") != null){
                    // 有传入的参数，用传入的
                    params.setAudioCode(joParams.getString("audioCode"));
                }else{
                    // 如果没有传入的，用配置文件设置的
                    params.setAudioCode(ConvertVideoConfig.ffmpegAudioCode);
                }

                // 图片水印
                if(joParams.containsKey("picMark") && joParams.get("picMark") != null){
                    PicMark picMark = new PicMark();
                    JSONObject joPicMark = joParams.getJSONObject("picMark");

                    // 图片位置
                    if(joPicMark.containsKey("picFile") && joPicMark.get("picFile") != null){
                        // 有传入的参数，用传入的
                        picMark.setPicFile(joPicMark.getString("picFile"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(picFile)){
                            picMark.setPicFile(picFile);
                        }
                    }

                    // 水印位置
                    if(joPicMark.containsKey("overlay") && joPicMark.get("overlay") != null){
                        // 有传入的参数，用传入的
                        picMark.setOverlay(joPicMark.optString("overlay"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(picFile) &&
                                !StringUtils.isEmpty(picOverlay)){
                            picMark.setOverlay(picOverlay);
                        }
                    }

                    // 水印缩放
                    if(joPicMark.containsKey("scale") && joPicMark.get("scale") != null){
                        // 有传入的参数，用传入的
                        picMark.setScale(joPicMark.optString("scale"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(picFile) &&
                                !StringUtils.isEmpty(picScale)){
                            picMark.setScale(picScale);
                        }
                    }

                    params.setPicMark(picMark);
                }

                // 文字水印
                if(joParams.containsKey("textMark") && joParams.get("textMark") != null){
                    TextMark textMark = new TextMark();
                    JSONObject joTextMark = joParams.getJSONObject("textMark");

                    // 字体文件
                    if(joTextMark.containsKey("fontFile") && joTextMark.get("fontFile") != null){
                        // 有传入的参数，用传入的
                        textMark.setFontFile(joTextMark.getString("fontFile"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(fontFile)){
                            textMark.setFontFile(fontFile);
                        }
                    }

                    // 水印文字内容
                    if(joTextMark.containsKey("text") && joTextMark.get("text") != null){
                        // 有传入的参数，用传入的
                        textMark.setText(joTextMark.getString("text"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(fontFile) &&
                                !StringUtils.isEmpty(fontFile)){
                            textMark.setText(text);
                        }
                    }

                    // 水印横坐标
                    if(joTextMark.containsKey("localX") && joTextMark.get("localX") != null){
                        // 有传入的参数，用传入的
                        textMark.setLocalX(joTextMark.getInt("localX"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(fontFile) &&
                                localX != null){
                            textMark.setLocalX(localX);
                        }
                    }

                    // 水印纵坐标
                    if(joTextMark.containsKey("localY") && joTextMark.get("localY") != null){
                        // 有传入的参数，用传入的
                        textMark.setLocalY(joTextMark.getInt("localY"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(fontFile) &&
                                localY != null){
                            textMark.setLocalY(localY);
                        }
                    }

                    // 文字大小
                    if(joTextMark.containsKey("fontSize") && joTextMark.get("fontSize") != null){
                        // 有传入的参数，用传入的
                        textMark.setFontSize(joTextMark.getInt("fontSize"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(fontFile) &&
                                fontSize != null){
                            textMark.setFontSize(fontSize);
                        }
                    }

                    // 文字颜色
                    if(joTextMark.containsKey("fontColor") && joTextMark.get("fontColor") != null){
                        // 有传入的参数，用传入的
                        textMark.setFontColor(joTextMark.getString("fontColor"));
                    }else{
                        // 如果没有传入的，用配置文件设置的
                        if(!StringUtils.isEmpty(fontFile) &&
                                !StringUtils.isEmpty(fontColor)){
                            textMark.setFontColor(fontColor);
                        }
                    }

                    params.setTextMark(textMark);
                }

                // 截图时间
                if(joParams.containsKey("time") && joParams.get("time") != null){
                    params.setTime(joParams.getString("time"));
                }

                // 缩略图
                if(joParams.containsKey("thumbnail") && joParams.get("thumbnail") != null){
                    params.setThumbnail(Thumbnail.convert((Map<String, Object>)joParams.get("thumbnail")));
                }

            }

            entity.setParams(params);
        }

        // 文件回写方式（回写路径[path]/回写接口[api]/ftp回写[ftp]）
        entity.setWriteBackType(WriteBackType.valueOf(MapUtil.getStr(parameters, "writeBackType", "path").toUpperCase()));
        // 回写信息配置
        entity.setWriteBack(entity.getWriteBackType().convert(parameters));
        // 回调
        entity.setCallBackURL(MapUtil.getStr(parameters, "callBackURL"));
        if (parameters.get("callBackHeaders") != null) {
            entity.setCallBackHeaders((Map<String, String>) parameters.get("callBackHeaders"));
        }
        return entity;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Map<String, String> getInputHeaders() {
        return inputHeaders;
    }

    public void setInputHeaders(Map<String, String> inputHeaders) {
        this.inputHeaders = inputHeaders;
    }

     public WriteBackType getWriteBackType() {
        return writeBackType;
    }

    public void setWriteBackType(WriteBackType writeBackType) {
        this.writeBackType = writeBackType;
    }

    public WriteBack getWriteBack() {
        return writeBack;
    }

    public void setWriteBack(WriteBack writeBack) {
        this.writeBack = writeBack;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public Map<String, String> getCallBackHeaders() {
        return callBackHeaders;
    }

    public void setCallBackHeaders(Map<String, String> callBackHeaders) {
        this.callBackHeaders = callBackHeaders;
    }

    public String getOutPutFileName() {
        return outPutFileName;
    }

    public void setOutPutFileName(String outPutFileName) {
        this.outPutFileName = outPutFileName;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getJpgFileName() {
        return jpgFileName;
    }

    public void setJpgFileName(String jpgFileName) {
        this.jpgFileName = jpgFileName;
    }
}
