package com.yunjing.eurekaclient2.common.picGenerate;

import java.awt.*;
import java.awt.image.BufferedImage;


public class SealImgGen {

    public  byte[] genOvalSeal(String header, String footer, String center){
        {
            /**
             * 印章配置文件
             */
            SealConfiguration configuration = new SealConfiguration();

            /**
             * 主文字
             */
            SealFont mainFont = new SealFont();
            mainFont.setBold(true);
            mainFont.setMarginSize(10);
            mainFont.setFontText(header);
            mainFont.setFontSize(25);
            mainFont.setFontSpace(12.0);

            /**
             * 副文字
             */
            SealFont viceFont = new SealFont();
            viceFont.setBold(true);
            viceFont.setMarginSize(5);
            viceFont.setFontText(footer);
            viceFont.setFontSize(22);
            viceFont.setFontSpace(12.0);

            /**
             * 中心文字
             */
            SealFont centerFont = new SealFont();
            centerFont.setBold(true);
            centerFont.setFontText(center);
            centerFont.setFontSize(25);


            /**
             * 添加主文字
             */
            configuration.setMainFont(mainFont);
            /**
             * 添加副文字
             */
            configuration.setViceFont(viceFont);
            /**
             * 添加中心文字
             */
            configuration.setCenterFont(centerFont);


            /**
             * 图片大小
             */
            configuration.setImageSize(300);
            /**
             * 背景颜色
             */
            configuration.setBackgroudColor(Color.RED);
            /**
             * 边线粗细、半径
             */
            configuration.setBorderCircle(new SealCircle(3, 140, 100));
            /**
             * 内边线粗细、半径
             */
            configuration.setBorderInnerCircle(new SealCircle(1, 135, 95));
            /**
             * 内环线粗细、半径
             */
            configuration.setInnerCircle(new SealCircle(2, 85, 45));

            //1.生成公章
            try {
                BufferedImage bi =  SealUtil.buildSeal(configuration);
                return SealUtil.buildBytes(bi);
            } catch (Exception e) {
                e.printStackTrace();
                throw new OperationException("6004", e.getMessage());
            }


        }

    }
}
