package com.thinkdifferent.convertvideo.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/3/25 14:47
 */
@Log4j2
public class FileTypeUtil {
    /**
     * 所有的文件类型
     */
    private static final Map<String, String> ALL_FILE_TYPE = new HashMap<>();

    private FileTypeUtil() {
    }

    static {
        initFileType();
    }

    /**
     * 初始化所有文件类型
     */
    private static void initFileType() {
        ALL_FILE_TYPE.put("avi", "41564920"); //AVI (avi)
        ALL_FILE_TYPE.put("mpg", "000001BA");  //MPG (mpg)
        ALL_FILE_TYPE.put("mov", "00000F");
        ALL_FILE_TYPE.put("mov", "000077");
        ALL_FILE_TYPE.put("mov", "6D6F6F76");  //Quicktime
        ALL_FILE_TYPE.put("mov", "6D646174");  //QuickTimeMovie
        ALL_FILE_TYPE.put("asf", "3026B2758E66CF11"); //ASF (asf)
        ALL_FILE_TYPE.put("wav", "57415645");  //Wave (wav)
        ALL_FILE_TYPE.put("ram", "2E7261FD");  //Real Audio (ram)
        ALL_FILE_TYPE.put("rm", "2E524D46");  //Real Media (rm)
        ALL_FILE_TYPE.put("mp4", "000000206674797069736F6D");  //MP4 (mp4)

        ALL_FILE_TYPE.put("mid", "4D546864");  //MIDI (mid)
        ALL_FILE_TYPE.put("mp3", "494433");  //MPEG-1AudioLayer3(MP3)audiofile
        ALL_FILE_TYPE.put("mp3", "FFFB50");
    }

    /**
     * 获取文件类型
     *
     * @param file 文件对象
     * @return 文件类型
     */
    public static String getFileType(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return FileTypeUtil.getFileType(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 输入流获取文件类型
     *
     * @param is 输入流
     * @return 文件类型
     */
    public static String getFileType(InputStream is) {
        String filetype = null;
        byte[] b = new byte[50];
        try {
            is.read(b);
            filetype = getFileTypeByStream(b);
        } catch (IOException e) {
            log.error("getFileType", e);
        }
        return filetype;
    }

    /**
     * 获取文件类型，
     *
     * @return 文件类型； null: 未匹配到对应的文件类型
     */
    private static String getFileTypeByStream(byte[] b) {
        String filetypeHex = String.valueOf(getFileHexString(b));

        return ALL_FILE_TYPE.entrySet().stream()
                .filter(entry -> filetypeHex.toUpperCase().startsWith(entry.getValue()))
                .findFirst().map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 解析读取的is的字符
     */
    private static String getFileHexString(byte[] b) {
        StringBuilder stringBuilder = new StringBuilder();
        if (b == null || b.length <= 0) {
            return null;
        }
        for (byte value : b) {
            int v = value & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
