package com.thinkdifferent.convertvideo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Objects;

/**
 * 定时任务配置
 *
 * @author ltian
 * @version 1.0
 * @date 2022/3/28 17:30
 */
@Slf4j
@Configuration
public class ScheduleConfig {

    /**
     * 定时移除未清理的文件，每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void cleanPdfImages() {
        String strInputTempPath = ConvertVideoConfig.inPutTempPath;
        String strOutputTempPath = ConvertVideoConfig.outPutPath;
        String[] strsTempPaths = {strInputTempPath, strOutputTempPath};

        // 当前时间 - 24 * 3
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DATE, -3);
        long day3Ago = calendar1.getTime().getTime();

        // 可删除的文件
        for(int i=0;i<strsTempPaths.length;i++){
            File[] files = new File(strsTempPaths[i]).listFiles(pathname -> canDelete(pathname, day3Ago));
            if (Objects.nonNull(files)) {
                for (File file : files) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        log.error("移除过期文件失败：" + file.getName(), e);
                    }
                }
            }
        }

    }

    /**
     * 判断文件夹是否可删除
     *
     * @param file    需要判断的文件
     * @param day3Ago 3天前的时间，判断文件夹创建时间是否小于此时间，小于删除
     * @return 判断文件夹创建时间是否小于3天前的时间
     */
    public static boolean canDelete(File file, long day3Ago) {
        try {
            long t = Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis();
            return day3Ago > t;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
