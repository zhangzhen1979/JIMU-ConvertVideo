package com.thinkdifferent.convertvideo.entity.input;

import java.io.File;

/**
 * 本地文件路径输入
 * @author ltian
 * @version 1.0
 * @date 2022/4/22 11:03
 */
public class InputPath extends Input {
    /**
     * 本地文件路径
     */
    private String filePath;

    @Override
    public Input of(String inputPath, String strExt) {
        InputPath path = new InputPath();
        path.setFilePath(inputPath);
        return path;
    }

    @Override
    public File getInputFile() {
        if (super.inputFile == null) {
            super.setInputFile(new File(filePath));
        }
        return super.inputFile;
    }

    /**
     * 判断传入的文件是否存在
     *
     * @return bln
     */
    @Override
    public boolean exist() {
        return new File(filePath).exists();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
