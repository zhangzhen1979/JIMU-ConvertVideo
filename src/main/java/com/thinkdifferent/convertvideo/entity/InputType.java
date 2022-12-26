package com.thinkdifferent.convertvideo.entity;


import com.thinkdifferent.convertvideo.entity.input.Input;
import com.thinkdifferent.convertvideo.entity.input.InputFtp;
import com.thinkdifferent.convertvideo.entity.input.InputPath;
import com.thinkdifferent.convertvideo.entity.input.InputUrl;

/**
 * 输入类型
 *
 * @author ltian
 * @version 1.0
 * @date 2022/4/13 15:37
 */
public enum InputType {
    PATH(InputPath.class),
    URL(InputUrl.class),
    FTP(InputFtp.class);
    private Class<? extends Input> inputClass;

    InputType(Class<? extends Input> inputClass) {
        this.inputClass = inputClass;
    }

    /**
     * 将输入格式转换为标准格式
     *
     * @param strInputPath 输入字符串
     * @return input对象
     */
    public Input of(String strInputPath, String strExt) {
        try {
            return this.inputClass.newInstance().of(strInputPath, strExt);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
