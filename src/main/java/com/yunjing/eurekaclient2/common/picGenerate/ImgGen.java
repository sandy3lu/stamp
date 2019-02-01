package com.yunjing.eurekaclient2.common.picGenerate;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;


/**
 * Copyright (C), 2015-2019, 北京云京科技有限公司
 * FileName: ImgGen
 *
 * @Author: FZF
 * Date:     2019/1/9
 * Description:传参类型
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
public class ImgGen {

    private static final int COLOR_RANGE = 255;
    private static final String FORM = "UTF-8";
    private String fontPath = this.getClass().getResource("simsun.ttc").getFile();
    public static String centerValue = "*";

    public  byte[] genCircleSeal(String header, String footer, String center){

        try {
            header = new String(header.getBytes(), FORM);
            footer = new String(footer.getBytes(), FORM);
        } catch (UnsupportedEncodingException e) {
            throw new OperationException("6001", "印章字体编码格式不支持");
        }

        ImgComponent sealMaker = new ImgComponent(390, 390);
        sealMaker.drawCircle();

        MyFont myFont = new MyFont(fontPath);
        Font font = myFont.getDefinedFont(40);
        if (centerValue.equals(center)) {
            sealMaker.setStar(myFont);
        } else {
            sealMaker.setCenter(center, font);
        }
        sealMaker.setHeader(header, font);
        sealMaker.setFooter(footer, font);
        BufferedImage image = sealMaker.finish();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new OperationException("6002", "圆形印章图片生成后写入流时异常");
        }
        return out.toByteArray();
    }

    /**
     * genRectangleSeal：创建矩形章
     * param：
     * name：矩形章中显示的内容名称
     * return：矩形印章图片的二进制
     * */
    public  byte[] genRectangleSeal(String name){

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int width = 200;
        int height = 70;
        try {
            // 创建BufferedImage对象
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            // 获取Graphics2D
            Graphics2D g2d = image.createGraphics();
            g2d.setBackground(new Color(255, 255, 255));
            g2d.setColor(Color.red);
            g2d.clearRect(0, 0, width, height);

            MyFont myFont = new MyFont(fontPath);
            Font font = myFont.getDefinedFont(40);
            g2d.setFont(font);
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 计算文字居中点坐标
            FontMetrics fm1 = sun.font.FontDesignMetrics.getMetrics(font);
            int textWidth = fm1.stringWidth(name);
            int widthX = (width - textWidth) / 2;
            int textHeight = fm1.getHeight();
            int heightY = textHeight / 3 + height / 2;
            //绘制汉字，参数：内容，中心点坐标
            g2d.drawString(name, widthX, heightY);
            //画矩形
            final int rectangles = 6;
            g2d.setPaint(Color.red);
            g2d.setStroke(new BasicStroke(rectangles));
            g2d.drawRect(0, 0, width, height);
            // 释放对象
            g2d.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 保存文件到流中
            ImageIO.write(image, "png", out);
            InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            // 清除图片底色
            convert(inputStream, os, "png");
        }
        catch(Exception ex) {
            throw new OperationException("6003", "方形印章生成时异常");
        }
        return os.toByteArray();
    }

    /**
     * 清除图片底色
     *
     * @param resourceIs 源图片输入流
     * @param os         输出流
     * @param format     输出文件的格式名称
     */
    public  void convert(InputStream resourceIs, OutputStream os, String format) {
        try {
            BufferedImage image = ImageIO.read(resourceIs);
            ImageIcon imageIcon = new ImageIcon(image);
            BufferedImage bufferedImage = new BufferedImage(
                imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
            g2D.drawImage(imageIcon.getImage(), 0, 0,
                imageIcon.getImageObserver());
            int alpha;
            for (int j1 = bufferedImage.getMinY(); j1 < bufferedImage
                .getHeight(); j1++) {
                for (int j2 = bufferedImage.getMinX(); j2 < bufferedImage
                    .getWidth(); j2++) {
                    int rgb = bufferedImage.getRGB(j2, j1);
                    if (colorInRange(rgb)) {
                        alpha = 0;
                    } else {
                        alpha = 255;
                    }
                    rgb = (alpha << 24) | (rgb & 0x00ffffff);
                    bufferedImage.setRGB(j2, j1, rgb);
                }
            }
            g2D.drawImage(bufferedImage, 0, 0, imageIcon.getImageObserver());
            ImageIO.write(bufferedImage, format, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  boolean colorInRange(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return red >= COLOR_RANGE && green >= COLOR_RANGE && blue >= COLOR_RANGE;
    }
}
