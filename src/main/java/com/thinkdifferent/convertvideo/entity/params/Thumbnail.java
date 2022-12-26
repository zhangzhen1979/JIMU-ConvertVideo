package com.thinkdifferent.convertvideo.entity.params;

import cn.hutool.core.map.MapUtil;

import java.util.Map;

public class Thumbnail {

    private int width;
    private int height;
    private double scale;
    private double quality;

    public static Thumbnail convert(Map<String, Object> parameters){
        try {
            Thumbnail thumbnail = new Thumbnail();
            thumbnail.setWidth(MapUtil.getInt(parameters, "width", 0));
            thumbnail.setHeight(MapUtil.getInt(parameters, "height", 0));
            thumbnail.setScale(MapUtil.getDouble(parameters, "scale", 0d));
            thumbnail.setQuality(MapUtil.getDouble(parameters, "quality", 0d));

            return thumbnail;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

}
