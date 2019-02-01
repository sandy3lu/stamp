package com.yunjing.eurekaclient2.common.picGenerate;


import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author fengzhifei
 */
public class ImgComponent {
    private int canvasWidth;
    private int canvasHeight;
    private float xRadius;
    private BufferedImage bufferedImage;
    private Graphics2D g2d;


    public ImgComponent(int canvasWidth, int canvasHeight) {
        assert canvasWidth > 0 && canvasHeight > 0;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        bufferedImage = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_ARGB);
        this.g2d = bufferedImage.createGraphics();
        // reduce sawtooth
        this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void setBackground(Color color) {
        this.g2d.setBackground(color);
    }

    void drawCircle() {
        final int circleBrushStroke = 10;

        this.xRadius = (this.canvasWidth - circleBrushStroke) >> 1;
        float yRadius = (this.canvasHeight - circleBrushStroke) >> 1;

        g2d.setPaint(Color.red);
        //set brush stroke
        g2d.setStroke(new BasicStroke(circleBrushStroke));
        Shape circle = new Arc2D.Double(circleBrushStroke >> 1, circleBrushStroke >> 1, this.xRadius * 2, yRadius * 2, 0, 360, Arc2D.OPEN);
        g2d.draw(circle);
    }

    void setHeader(String content, Font font) {
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(content, context);

        int contentLen = content.length();
        double msgWidth = bounds.getWidth();
        double interval = msgWidth / contentLen;
        double newRadius = this.xRadius - bounds.getHeight();
        double radianPerInterval = 2 * Math.asin(interval / (2 * newRadius));

        double firstAngle;
        if (contentLen % 2 == 1) {
            firstAngle = (contentLen - 1) * radianPerInterval / 2.0 + radianPerInterval / 2.0 + Math.PI / 2;
        } else {
            firstAngle = contentLen / 2.0 * radianPerInterval + Math.PI / 2;
        }

        for (int i = 0; i < contentLen; i++) {
            double aa = firstAngle - i * radianPerInterval;
            double ax = newRadius * Math.sin(Math.PI / 2 - aa);
            double ay = newRadius * Math.cos(aa - Math.PI / 2);
            AffineTransform transform = AffineTransform.getRotateInstance(Math.PI / 2 - aa);
            Font f2 = font.deriveFont(transform);
            g2d.setFont(f2);
            g2d.drawString(content.substring(i, i + 1), (float) (this.canvasWidth / 2 + ax), (float) (this.canvasHeight / 2 - ay));
        }
    }

    void setStar(MyFont myFont) {
        int font = this.canvasWidth / 3;
        this.g2d.setFont(myFont.getDefinedFont(font));
        this.g2d.drawString("★", this.canvasWidth / 2 - font / 2, this.canvasHeight / 2 + font / 3);
    }

    void setCenter(String content, Font font) {
        FontRenderContext context = this.g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(content, context);
        this.g2d.setFont(font);
        this.g2d.drawString(content, (float) (this.canvasWidth / 2 - bounds.getCenterX()), (float) this.canvasHeight / 2);
    }

    void setFooter(String content, Font font) {
        FontRenderContext context = this.g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(content, context);
        this.g2d.setFont(font);
        this.g2d.drawString(content, (float) (this.canvasWidth / 2 - bounds.getCenterX()),
            (float) (this.canvasHeight - this.canvasHeight / 5));
    }

    public BufferedImage finish() {
        this.g2d.dispose();
        this.g2d = null;
        return bufferedImage;
    }
}

