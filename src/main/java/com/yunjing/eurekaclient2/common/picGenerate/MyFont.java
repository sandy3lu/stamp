package com.yunjing.eurekaclient2.common.picGenerate;

/**
 * Copyright (C), 2015-2019, 北京云京科技有限公司
 * FileName: MyFont
 *
 * @Author: FZF
 * Date:     2019/1/21 11:29
 * Description: 为印章图片提供字体支持
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */


import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyFont {
    private Font definedFont = null;

    private String fontUrl  = null;

    public MyFont(String fontUrl){
        this.fontUrl = fontUrl;
    }

    public Font getDefinedFont(float fs) {
        if (!new File(fontUrl).exists()) {
            throw new OperationException("17", fontUrl + "下字体文件不存在");
        } else {
            InputStream is = null;
            BufferedInputStream bis = null;
            try {
                is = new FileInputStream(new File(fontUrl));
                bis = new BufferedInputStream(is);
                definedFont = Font.createFont(Font.TRUETYPE_FONT, is);
                //设置字体大小，float型
                definedFont = definedFont.deriveFont(fs);
            } catch (FontFormatException | IOException e) {
                throw new OperationException("17", fontUrl + "下字体文件不存在");
            } finally {
                try {
                    if (null != bis) {
                        bis.close();
                    }
                    if (null != is) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return definedFont;
        }
    }
}
