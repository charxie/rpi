package org.concord.iot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

/**
 * @author Charles Xie
 */

public abstract class Symbol implements Icon {

    protected int xSymbol = 0, ySymbol = 0, wSymbol = 8, hSymbol = 8;
    protected Color color = Color.white;
    protected Stroke stroke = new BasicStroke(1);
    protected boolean paintBorder;
    protected boolean pressed;
    protected boolean disabled;
    protected int offsetX, offsetY;
    protected int marginX, marginY;

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setBorderPainted(boolean paintBorder) {
        this.paintBorder = paintBorder;
    }

    public boolean isBorderPainted() {
        return paintBorder;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setMarginX(int marginX) {
        this.marginX = marginX;
    }

    public int getMarginX() {
        return marginX;
    }

    public void setMarginY(int marginY) {
        this.marginY = marginY;
    }

    public int getMarginY() {
        return marginY;
    }

    public void setSymbolWidth(int wSymbol) {
        this.wSymbol = wSymbol;
    }

    public int getSymbolWidth() {
        return wSymbol;
    }

    public void setSymbolHeight(int hSymbol) {
        this.hSymbol = hSymbol;
    }

    public int getSymbolHeight() {
        return hSymbol;
    }

    public void setIconWidth(int width) {
        wSymbol = width - marginX * 2;
    }

    public int getIconWidth() {
        return wSymbol + marginX * 2;
    }

    public void setIconHeight(int height) {
        hSymbol = height - marginY * 2;
    }

    public int getIconHeight() {
        return hSymbol + marginY * 2;
    }

    public boolean contains(int rx, int ry) {
        return rx > xSymbol && rx < xSymbol + wSymbol && ry > ySymbol && ry < ySymbol + hSymbol;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        xSymbol = x + offsetX;
        ySymbol = y + offsetY;
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        if (paintBorder) {
            g.drawRoundRect(xSymbol, ySymbol, wSymbol, hSymbol, 10, 10);
        }
        g2.setStroke(stroke);
    }

    public Symbol getScaledInstance(float scale) {
        try {
            Symbol icon = getClass().newInstance();
            icon.setIconWidth((int) (scale * icon.getIconWidth()));
            icon.setIconHeight((int) (scale * icon.getIconHeight()));
            return icon;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Image createImage(Component c) {
        BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        try {
            paintIcon(c, g, 0, 0);
            return image;
        } finally {
            g.dispose();
        }
    }

    static class MonochromaticLedLight extends Symbol {

        private double radius;
        private int rays = 8;
        private double gap = 2;

        public MonochromaticLedLight(Color color, int w, int h, double radius, int rays) {
            setColor(color);
            setIconWidth(w);
            setIconHeight(h);
            setStroke(new BasicStroke(2));
            this.radius = radius;
            this.rays = rays;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            Graphics2D g2 = (Graphics2D) g;
            Ellipse2D.Float s = new Ellipse2D.Float(xSymbol, ySymbol, wSymbol, hSymbol);
            g2.fill(s);
            if (pressed) {
                int x1, y1, x2, y2;
                double angle, cos, sin;
                for (int i = 0; i < rays; i++) {
                    angle = i * Math.PI * 2 / rays;
                    cos = Math.cos(angle);
                    sin = Math.sin(angle);
                    x1 = (int) (s.getCenterX() + radius * cos);
                    y1 = (int) (s.getCenterY() + radius * sin);
                    x2 = (int) (s.getCenterX() + (wSymbol / 2 + gap) * cos);
                    y2 = (int) (s.getCenterY() + (hSymbol / 2 + gap) * sin);
                    g2.drawLine(x1, y1, x2, y2);
                }
            }
        }

    }

}