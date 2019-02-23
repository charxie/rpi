package org.concord.iot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;

/**
 * @author Charles Xie
 *
 */

class RainbowHatBoardView extends JPanel {

    private RainbowHat rainbowHat;

    private Image image;
    private int xImageOffset;
    private int yImageOffset;
    private Font labelFont = new Font(null, Font.PLAIN, 10);
    private Stroke thinStroke = new BasicStroke(1);
    private Color buttonPressedColor = new Color(205, 205, 205, 128);
    private DecimalFormat decimalFormat = new DecimalFormat("#.00");
    private String label;
    private Point mouseMovedPoint = new Point(-1, -1);

    private Rectangle buttonA;
    private Rectangle buttonB;
    private Rectangle buttonC;
    private Rectangle redLed;
    private Rectangle greenLed;
    private Rectangle blueLed;
    private Ellipse2D.Double[] leds = new Ellipse2D.Double[RainbowHatState.NUMBER_OF_RGB_LEDS];
    private Rectangle temperatureSensor;
    private Rectangle barometricPressureSensor;
    private boolean buttonAPressed;
    private boolean buttonBPressed;
    private boolean buttonCPressed;

    private Symbol.LedLight redLedSymbol;
    private Symbol.LedLight greenLedSymbol;
    private Symbol.LedLight blueLedSymbol;
    private Symbol.LedLight[] ledLightSymbols = new Symbol.LedLight[RainbowHatState.NUMBER_OF_RGB_LEDS];

    RainbowHatBoardView(RainbowHat rainbowHat) {

        super();
        setBackground(Color.WHITE);

        this.rainbowHat = rainbowHat;

        buttonA = new Rectangle(72, 270, 72, 22);
        buttonB = new Rectangle(155, 270, 72, 22);
        buttonC = new Rectangle(238, 270, 72, 22);
        redLed = new Rectangle(100, 258, 18, 8);
        greenLed = new Rectangle(182, 258, 18, 8);
        blueLed = new Rectangle(264, 258, 18, 8);
        int radius = 10;
        leds[0] = new Ellipse2D.Double(293 - radius, 115 - radius, 2 * radius, 2 * radius);
        leds[1] = new Ellipse2D.Double(260 - radius, 98 - radius, 2 * radius, 2 * radius);
        leds[2] = new Ellipse2D.Double(226 - radius, 89 - radius, 2 * radius, 2 * radius);
        leds[3] = new Ellipse2D.Double(189 - radius, 86 - radius, 2 * radius, 2 * radius);
        leds[4] = new Ellipse2D.Double(153 - radius, 89 - radius, 2 * radius, 2 * radius);
        leds[5] = new Ellipse2D.Double(118 - radius, 98 - radius, 2 * radius, 2 * radius);
        leds[6] = new Ellipse2D.Double(85 - radius, 115 - radius, 2 * radius, 2 * radius);
        temperatureSensor = new Rectangle(186, 133, 10, 10);
        barometricPressureSensor = new Rectangle(228, 141, 8, 8);

        setPreferredSize(new Dimension(500, 400));
        image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/rainbow-hat.png"));

        redLedSymbol = new Symbol.LedLight(Color.RED, 8, 8, 16, 8);
        greenLedSymbol = new Symbol.LedLight(Color.GREEN, 8, 8, 16, 8);
        blueLedSymbol = new Symbol.LedLight(Color.BLUE, 8, 8, 16, 8);
        for (int i = 0; i < ledLightSymbols.length; i++) {
            ledLightSymbols[i] = new Symbol.LedLight(Color.BLACK, 16, 16, 12, 12);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onMouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                onMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMouseExited(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
        });

    }

    @Override
    public void paint(Graphics g) {

        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(getBackground());
        g2.clearRect(0, 0, w, h);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int wi = image.getWidth(this);
        int hi = image.getHeight(this);
        xImageOffset = (w - wi) / 2;
        yImageOffset = (h - hi) / 2;

        g2.drawImage(image, xImageOffset, yImageOffset, this);

        g2.translate(xImageOffset, yImageOffset);

        int a = (redLed.width - redLedSymbol.wSymbol) / 2;
        int b = (redLed.height - redLedSymbol.hSymbol) / 2;
        redLedSymbol.paintIcon(this, g, redLed.x + a, redLed.y + b);
        greenLedSymbol.paintIcon(this, g, greenLed.x + a, greenLed.y + b);
        blueLedSymbol.paintIcon(this, g, blueLed.x + a, blueLed.y + b);
        for (int i = 0; i < ledLightSymbols.length; i++) {
            ledLightSymbols[i].paintIcon(this, g, (int) (leds[i].x + a), (int) (leds[i].y + b));
        }

        if (buttonAPressed) {
            g2.setColor(buttonPressedColor);
            g2.fill(buttonA);
        }
        if (buttonBPressed) {
            g2.setColor(buttonPressedColor);
            g2.fill(buttonB);
        }
        if (buttonCPressed) {
            g2.setColor(buttonPressedColor);
            g2.fill(buttonC);
        }

        g2.translate(-xImageOffset, -yImageOffset);

        if (label != null) {
            drawString(g2, mouseMovedPoint, label);
        }

        g2.dispose();

    }

    private void drawString(Graphics2D g, Point p, String s) {
        g.setFont(labelFont);
        FontMetrics fm = g.getFontMetrics();
        int stringWidth = fm.stringWidth(s);
        g.setStroke(thinStroke);
        int x = p.x;
        boolean nearRightBorder = x > getWidth() - 50;
        x += nearRightBorder ? -30 : 20;
        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(x - 5, p.y - 14, stringWidth + 10, 20, 8, 8);
        g.drawLine(nearRightBorder ? x + stringWidth + 5 : x - 5, p.y - 5, p.x, p.y);
        g.fillOval(p.x - 2, p.y - 2, 4, 4);
        g.setColor(Color.WHITE);
        drawString(g, s, x, p.y);
    }

    private void drawString(Graphics2D g, String text, int x, int y) {
        for (String line : text.split("\n")) {
            g.drawString(line, x, y);
            y += g.getFontMetrics().getHeight();
        }
    }

    private void onMouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int x2 = x - xImageOffset;
        int y2 = y - yImageOffset;
        boolean found = false;
        for (int i = 0; i < leds.length; i++) {
            if (leds[i].contains(x2, y2)) {
                rainbowHat.chooseLedColor(SwingUtilities.getWindowAncestor(this), i);
                found = true;
                break;
            }
        }
        if (!found) {
            setLatchingSwitch(x2, y2);
        }
        repaint();
    }

    private void setLatchingSwitch(int x, int y) {
        if (redLed.contains(x, y)) {
            rainbowHat.setRedLedState(!rainbowHat.getRedLedState());
            redLedSymbol.setPressed(rainbowHat.getRedLedState());
        } else if (greenLed.contains(x, y)) {
            rainbowHat.setGreenLedState(!rainbowHat.getGreenLedState());
            greenLedSymbol.setPressed(rainbowHat.getGreenLedState());
        } else if (blueLed.contains(x, y)) {
            rainbowHat.setBlueLedState(!rainbowHat.getBlueLedState());
            blueLedSymbol.setPressed(rainbowHat.getBlueLedState());
        }
    }

    private void onMousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        setMomentarySwitch(x - xImageOffset, y - yImageOffset, true);
        repaint();
    }

    private void onMouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        setMomentarySwitch(x - xImageOffset, y - yImageOffset, false);
        repaint();
    }

    private void setMomentarySwitch(int x, int y, boolean on) {
        if (buttonA.contains(x, y)) {
            rainbowHat.touchA(on);
            buttonAPressed = on;
        } else if (buttonB.contains(x, y)) {
            rainbowHat.touchB(on);
            buttonBPressed = on;
        } else if (buttonC.contains(x, y)) {
            rainbowHat.touchC(on);
            buttonCPressed = on;
        }
    }

    private void onMouseEntered(MouseEvent e) {
        repaint();
    }

    private void onMouseExited(MouseEvent e) {
        mouseMovedPoint.setLocation(-1, -1);
        repaint();
    }

    private void onMouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        mouseMovedPoint.setLocation(x, y);
        int x2 = x - xImageOffset;
        int y2 = y - yImageOffset;
        label = null;

        if (buttonA.contains(x2, y2)) {
            label = "Touch button A (momentary)";
        }
        if (label == null && buttonB.contains(x2, y2)) {
            label = "Touch button B (momentary)";
        }
        if (label == null && buttonC.contains(x2, y2)) {
            label = "Touch button C (momentary)";
        }

        if (label == null && redLed.contains(x2, y2)) {
            label = "Red LED light";
        }
        if (label == null && greenLed.contains(x2, y2)) {
            label = "Green LED light";
        }
        if (label == null && blueLed.contains(x2, y2)) {
            label = "Blue LED light";
        }

        if (label == null && temperatureSensor.contains(x2, y2)) {
            label = "Temperature sensor (" + decimalFormat.format(rainbowHat.getTemperature()) + "\u00B0C)";
        }
        if (label == null && barometricPressureSensor.contains(x2, y2)) {
            label = "Barometric pressure sensor (" + decimalFormat.format(rainbowHat.getBarometricPressure()) + "hPa)";
        }

        if (label == null) {
            for (int i = 0; i < leds.length; i++) {
                if (leds[i].contains(x2, y2)) {
                    label = "Trichromatic LED light " + i;
                    break;
                }
            }
        }

        if (label != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        repaint();

    }

    private void onMouseDragged(MouseEvent e) {
        repaint();
    }

    public void setRedLedPressed(boolean on) {
        redLedSymbol.setPressed(on);
        repaint();
    }

    public void setGreenLedPressed(boolean on) {
        greenLedSymbol.setPressed(on);
        repaint();
    }

    public void setBlueLedPressed(boolean on) {
        blueLedSymbol.setPressed(on);
        repaint();
    }

    public void setLedColor(int i, Color c) {
        ledLightSymbols[i].setColor(c);
        ledLightSymbols[i].setPressed(!c.equals(Color.BLACK));
        repaint();
    }

}