package com.thinkdifferent.convertvideo.utils;

import cn.hutool.system.OsInfo;
import com.thinkdifferent.convertvideo.config.ConvertVideoConfig;
import com.thinkdifferent.convertvideo.entity.ConvertEntity;
import com.thinkdifferent.convertvideo.entity.params.Thumbnail;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class ConvertVideoUtils {

    private String strInputPath;
    private ConvertEntity convertEntity;
    private String strExt;

    public ConvertVideoUtils(String strInputPath, ConvertEntity convertEntity) {
        this.strInputPath = strInputPath;
        this.convertEntity = convertEntity;
    }

    public String getExt(){
        return this.strExt;
    }

    public Boolean setVoidInfos() {
        if (!checkfile(strInputPath)) {
            log.error(strInputPath + " is not file");
            return false;
        }
        if (process(strInputPath, convertEntity)) {
            log.info("ok");
            return true;
        }
        log.error(strInputPath + " process error");
        return false;
    }


    public boolean process(String strInputPath, ConvertEntity convertEntity) {
        int intType = checkContentType(strInputPath);
        boolean blnStatus = false;
        if (intType == 0) {
            log.info("直接转成mp4格式/截图jpg");
            blnStatus = processConvert(strInputPath, convertEntity);
        } else {
            log.error("暂不支持的格式" + strInputPath);
        }
        return blnStatus;
    }


    private static int checkContentType(String strInputPath) {
        String strType = strInputPath.substring(strInputPath.lastIndexOf(".") + 1).toLowerCase();
        // ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
        switch (strType) {
            case "avi":
            case "mpg":
            case "wmv":
            case "3gp":
            case "mov":
            case "mp4":
            case "asf":
            case "asx":
            case "flv":
                return 0;

            // 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
            // 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
            case "wmv9":
            case "rm":
            case "rmvb":
                return 1;
            default:
                return 9;
        }
    }

    private static boolean checkfile(String strInputPath) {
        File file = new File(strInputPath);
        return file.isFile();
    }

    private boolean processConvert(String strOldFilePath, ConvertEntity convertEntity) {
        if (!checkfile(strOldFilePath)) {
            log.info(strOldFilePath + " is not file");
            return false;
        }
        strOldFilePath = strOldFilePath.replaceAll("\\\\", "/");
        String strInputFileType = FileTypeUtil.getFileType(new File(strOldFilePath)).toLowerCase();

        LineMaker lineMaker = new LineMaker();

        List<String> listCommand = new ArrayList<>();
        listCommand.add(ConvertVideoConfig.ffmpegFile);


        String strFileName;

        String strOutputPath =  convertEntity.getWriteBack().getOutputPath();
        strOutputPath = strOutputPath.replaceAll("\\\\", "/");
        if(!strOutputPath.endsWith("/")){
            strOutputPath = strOutputPath + "/";
        }



        // 设置音频扩展名
        String[] strAudioExt = {"3gp", "3gpp", "amr", "aac", "ape", "aif", "au",
                "mid", "wma", "ra", "rm", "rmx", "vqf", "ogg", "wav"};
        ArrayList<String> listAudioExt = new ArrayList<>(strAudioExt.length);
        Collections.addAll(listAudioExt, strAudioExt);

        if(listAudioExt.contains(strInputFileType)){
            // 如果输入的是音频文件，则转换生成mp3
            this.strExt = "mp3";
            strFileName = strOutputPath + convertEntity.getOutPutFileName() + ".mp3";
            lineMaker.toMp3ByParam(listCommand, strOldFilePath, strFileName);
        }else{
            // 否则是视频文件，生成MP4或对视频截图生成JPG
            // 判断是转MP4还是截图JPG
            if(!StringUtils.isEmpty(convertEntity.getJpgFileName())){
                // 截屏生成JPG
                this.strExt = "jpg";
                strFileName = strOutputPath + convertEntity.getJpgFileName() + ".jpg";
                lineMaker.toJpg(listCommand, convertEntity, strOldFilePath, strFileName);

            }else{
                // 转换MP4/MP3
                this.strExt = "mp4";
                strFileName = strOutputPath + convertEntity.getOutPutFileName() + ".mp4";

                if(convertEntity.getParams() != null) {
                    lineMaker.lineByCustom(listCommand, convertEntity, strOldFilePath,  strFileName);
                }else{
                    lineMaker.toMp4ByParam(listCommand, convertEntity, strOldFilePath,  strFileName);
                }
            }
        }

        if(listCommand != null){
            try {
                if (new OsInfo().isWindows()) {
                    Process videoProcess = new ProcessBuilder(listCommand).redirectErrorStream(true).start();
                    new PrintStream(videoProcess.getErrorStream()).start();
                    new PrintStream(videoProcess.getInputStream()).start();
                    videoProcess.waitFor();
                } else {
                    log.info("linux开始");
                    StringBuilder strbTest = new StringBuilder();
                    for (String s : listCommand) strbTest.append(s).append(" ");
                    log.info(strbTest.toString());
                    // 执行命令
                    Process p = Runtime.getRuntime().exec(strbTest.toString());
                    // 取得命令结果的输出流
                    InputStream inputStream = p.getInputStream();
                    // 用一个读输出流类去读
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    // 用缓冲器读行
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    String strLine;
                    // 直到读完为止
                    while ((strLine = br.readLine()) != null) {
                        log.info("视频转换:{}", strLine);
                    }
                }

                // 如果进行截图（输出jpg），且配置了“缩略图”参数，则进行缩略图操作
                if(convertEntity.getParams() != null &&
                        convertEntity.getParams().getThumbnail() != null &&
                        "jpg".equalsIgnoreCase(this.strExt)){
                    Thumbnail thumbnail = convertEntity.getParams().getThumbnail();
                    if(thumbnail.getWidth() >0 || thumbnail.getHeight() >0){
                        // 如果输入了边长，则按边长生成
                        int intImageWidth = 0;
                        int intImageHeight = 0;
                        try (FileInputStream fis = new FileInputStream(new File(strOutputPath + strFileName))) {
                            BufferedImage buffSourceImg = ImageIO.read(fis);
                            BufferedImage buffImg = new BufferedImage(buffSourceImg.getWidth(), buffSourceImg.getHeight(), BufferedImage.TYPE_INT_RGB);
                            // 获取图片的大小
                            intImageWidth = buffImg.getWidth();
                            intImageHeight = buffImg.getHeight();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(thumbnail.getWidth() >0 && thumbnail.getHeight() == 0){
                            // 如果只输入了宽，则按比例计算高
                            thumbnail.setHeight(thumbnail.getWidth() * intImageHeight / intImageWidth);
                        }else if(thumbnail.getWidth() == 0 && thumbnail.getHeight() >0){
                            // 如果只输入了高，则按比例计算宽
                            thumbnail.setWidth(thumbnail.getHeight() * intImageWidth / intImageHeight);
                        }

                        fixedSizeImage(
                                strOutputPath + strFileName,
                                convertEntity.getWriteBack().getOutputPath() + convertEntity.getJpgFileName() + "_thum.jpg",
                                thumbnail.getWidth(),
                                thumbnail.getHeight()
                        );

                    }else if(thumbnail.getScale() >= 0d){
                        // 如果输入了比例，则按比例生成
                        thumbnail(
                                strOutputPath + strFileName,
                                convertEntity.getWriteBack().getOutputPath() + convertEntity.getJpgFileName() + "_thum.jpg",
                                thumbnail.getScale(),
                                thumbnail.getQuality()
                        );

                    }

                    File fileThum = new File(convertEntity.getWriteBack().getOutputPath() + convertEntity.getJpgFileName() + "_thum.jpg");
                    if(fileThum.exists()){
                        File fileOut = new File(convertEntity.getWriteBack().getOutputPath() + convertEntity.getJpgFileName() + ".jpg");
                        if(fileOut.exists()){
                            fileOut.delete();
                        }
                        fileThum.renameTo(fileOut);
                    }

                }

                return true;
            } catch (Exception e) {
                log.error(e);
                return false;
            }

        }else{
            log.error("命令行生成失败！");
            return false;
        }
    }

    /**
     * 图片处理相关方法
     */

    /**
     * 使用给定的图片生成指定大小的图片（原格式）
     * @param strInputFilePath   输入文件的绝对路径和文件名
     * @param strOutputFilePath  输出文件的绝对路径和文件名
     * @param intWidth           输出文件的宽度
     * @param intHeight          输出文件的高度
     */
    public static File fixedSizeImage(String strInputFilePath, String strOutputFilePath,
                                      int intWidth, int intHeight){
        try {
            Thumbnails.of(strInputFilePath).
                    size(intWidth,intHeight).
                    toFile(strOutputFilePath);
            return new File(strOutputFilePath);
        } catch (IOException e) {
            log.error("原因: " + e.getMessage());
        }
        return null;
    }

    /**
     * 按比例缩放图片
     * @param strInputFilePath   输入文件的绝对路径和文件名
     * @param strOutputFilePath  输出文件的绝对路径和文件名
     * @param dblScale           输出文件的缩放百分比。1为100%,0.8为80%，以此类推。
     * @param dblQuality         输出文件的压缩比（质量）。1为100%,0.8为80%，以此类推。
     */
    public static File thumbnail(String strInputFilePath, String strOutputFilePath,
                                 double dblScale, double dblQuality){
        try {
            Thumbnails.of(strInputFilePath).
                    //scalingMode(ScalingMode.BICUBIC).
                            scale(dblScale). // 图片缩放80%, 不能和size()一起使用
                    outputQuality(dblQuality). // 图片质量压缩80%
                    toFile(strOutputFilePath);
            return new File(strOutputFilePath);
        } catch (IOException e) {
            log.error("原因: " + e.getMessage());
        }
        return null;
    }


}
