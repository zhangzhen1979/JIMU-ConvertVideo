package com.thinkdifferent.convertvideo.entity.input;

import java.io.File;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 11:03
 */
public abstract class Input {
    /**
     * 传入的文件对象
     */
    protected File inputFile;
    /**
     * 输入字符串转换成对应类
     * @param inputPath 文件路径
     * @param strExt 文件扩展名
     * @return  input对象
     */
    public abstract Input of(String inputPath, String strExt);

    /**
     * 获取文件
     * @return file
     */
    public abstract File getInputFile();

    protected void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * 判断传入的文件是否存在
     * @return  bln
     */
    public abstract boolean exist();
}
