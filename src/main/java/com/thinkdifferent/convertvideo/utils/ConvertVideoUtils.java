package com.thinkdifferent.convertvideo.utils;

import cn.hutool.system.OsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConvertVideoUtils {

    private static Logger logger = LoggerFactory.getLogger(ConvertVideoUtils.class);

    private String strInputPath;

    private String strOutputPath;

    private String strFFmpegPath;

    private String strFileName;

    public ConvertVideoUtils(String strInputPath, String strOutputPath, String strFFmpegPath, String strFileName) {
        this.strInputPath = strInputPath;
        this.strOutputPath = strOutputPath;
        this.strFFmpegPath = strFFmpegPath;
        this.strFileName = strFileName;
    }

    public Boolean setVoidInfos() {
        if (!checkfile(strInputPath)) {
            logger.info(strInputPath + " is not file");
            return false;
        }
        if (process(strInputPath, strFFmpegPath, strOutputPath, strFileName)) {
            logger.info("ok");
            return true;
        }
        return false;
    }


    public static boolean process(String strInputPath, String strFFmpegPath, String strOutputPath, String strFileName) {
        int intType = checkContentType(strInputPath);
        boolean blnStatus = false;
        logger.info("直接转成mp4格式");
        blnStatus = processMp4(strInputPath, strFFmpegPath, strOutputPath, strFileName);// 直接转成mp4格式
        return blnStatus;
    }


    private static int checkContentType(String strInputPath) {
        String strType = strInputPath.substring(strInputPath.lastIndexOf(".") + 1, strInputPath.length())
                .toLowerCase();
        // ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
        if (strType.equals("avi")) {
            return 0;
        } else if (strType.equals("mpg")) {
            return 0;
        } else if (strType.equals("wmv")) {
            return 0;
        } else if (strType.equals("3gp")) {
            return 0;
        } else if (strType.equals("mov")) {
            return 0;
        } else if (strType.equals("mp4")) {
            return 0;
        } else if (strType.equals("asf")) {
            return 0;
        } else if (strType.equals("asx")) {
            return 0;
        } else if (strType.equals("flv")) {
            return 0;
        }
        // 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
        // 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
        else if (strType.equals("wmv9")) {
            return 1;
        } else if (strType.equals("rm")) {
            return 1;
        } else if (strType.equals("rmvb")) {
            return 1;
        }
        return 9;
    }

    private static boolean checkfile(String strInputPath) {
        File file = new File(strInputPath);
        if (!file.isFile()) {
            return false;
        }
        return true;
    }

    private static boolean processMp4(String strOldFilePath, String strFFmpegPath, String strOutputPath, String strFileName) {
        if (!checkfile(strOldFilePath)) {
            logger.info(strOldFilePath + " is not file");
            return false;
        }
        List<String> listCommand = new ArrayList<>();
        listCommand.add(strFFmpegPath);
        listCommand.add("-y");
        listCommand.add("-i");
        listCommand.add(strOldFilePath);
        listCommand.add("-c:v");
        listCommand.add("libx264");
        listCommand.add("-mbd");
        listCommand.add("0");
        listCommand.add("-c:a");
        listCommand.add("aac");
        listCommand.add("-strict");
        listCommand.add("-2");
        listCommand.add("-pix_fmt");
        listCommand.add("yuv420p");
        listCommand.add("-movflags");
        listCommand.add("faststart");
        listCommand.add(strOutputPath + strFileName + ".mp4");
        try {
            if (new OsInfo().isWindows()  ) {
                Process videoProcess = new ProcessBuilder(listCommand).redirectErrorStream(true).start();
                new PrintStream(videoProcess.getErrorStream()).start();
                new PrintStream(videoProcess.getInputStream()).start();
                videoProcess.waitFor();
            } else {
                logger.info("linux开始");
                StringBuilder strbTest = new StringBuilder();
                for (String s : listCommand) strbTest.append(s).append(" ");
                logger.info(strbTest.toString());
                // 执行命令
                Process p = Runtime.getRuntime().exec(strbTest.toString());
                // 取得命令结果的输出流
                InputStream inputStream = p.getInputStream();
                // 用一个读输出流类去读
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                // 用缓冲器读行
                BufferedReader br = new BufferedReader(inputStreamReader);
                String strLine = null;
                // 直到读完为止
                while ((strLine = br.readLine()) != null) {
                    logger.info("视频转换:{}", strLine);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args) {
        String strInputPath = "D:/cvtest/001.MOV";
        String strOutputPath = "D:/cvtest/";
        String strFFmpegPath = "C:/Program Files (x86)/FormatFactory/ffmpeg.exe";
        String strFileName = "001-online";
        ConvertVideoUtils convertVideoUtils = new ConvertVideoUtils(strInputPath, strOutputPath, strFFmpegPath, strFileName);
        boolean blnSuccess = convertVideoUtils.setVoidInfos();

        if(blnSuccess){
            logger.info("视频转换成功");
        }else{
            logger.info("视频转换失败");
        }
    }


}
