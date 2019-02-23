package org.concord.iot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;

class RainbowHatBoardView extends JPanel {

    private RainbowHat rainbowHat;

    private Image image;
    private int xImageOffset;
    private int yImageOffset;

    private Rectangle buttonA;
    private Rectangle buttonB;
    private Rectangle buttonC;
    private Ellipse2D.Double[] leds = new Ellipse2D.Double[RainbowHatState.NUMBER_OF_RGB_LEDS];

    RainbowHatBoardView(RainbowHat rainbowHat) {

        super();
        setBackground(Color.WHITE);

        this.rainbowHat = rainbowHat;

        buttonA = new Rectangle(69, 266, 78, 30);
        buttonB = new Rectangle(152, 266, 78, 30);
        buttonC = new Rectangle(236, 266, 78, 30);
        int radius = 10;
        leds[0] = new Ellipse2D.Double(294 - radius, 112 - radius, 2 * radius, 2 * radius);
        leds[1] = new Ellipse2D.Double(263 - radius, 95 - radius, 2 * radius, 2 * radius);
        leds[2] = new Ellipse2D.Double(228 - radius, 87 - radius, 2 * radius, 2 * radius);
        leds[3] = new Ellipse2D.Double(190 - radius, 83 - radius, 2 * radius, 2 * radius);
        leds[4] = new Ellipse2D.Double(155 - radius, 87 - radius, 2 * radius, 2 * radius);
        leds[5] = new Ellipse2D.Double(120 - radius, 95 - radius, 2 * radius, 2 * radius);
        leds[6] = new Ellipse2D.Double(87 - radius, 112 - radius, 2 * radius, 2 * radius);

        setPreferredSize(new Dimension(500, 400));
        image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/rainbow-hat.png"));

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

        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();
        int wi = image.getWidth(this);
        int hi = image.getHeight(this);
        xImageOffset = (w - wi) / 2;
        yImageOffset = (h - hi) / 2;

        g2.drawImage(image, xImageOffset, yImageOffset, this);

        g2.dispose();

    }

    private void onMouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        for (int i = 0; i < leds.length; i++) {
            if (leds[i].contains(x - xImageOffset, y - yImageOffset)) {
                rainbowHat.chooseLedColor(SwingUtilities.getWindowAncestor(this), i);
                break;
            }
        }
    }

    private void onMousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (buttonA.contains(x - xImageOffset, y - yImageOffset)) {
            rainbowHat.pressButtonA(true);
        } else if (buttonB.contains(x - xImageOffset, y - yImageOffset)) {
            rainbowHat.pressButtonB(true);
        } else if (buttonC.contains(x - xImageOffset, y - yImageOffset)) {
            rainbowHat.pressButtonC(true);
        }
    }

    private void onMouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (buttonA.contains(x - xImageOffset, y - yImageOffset)) {
            rainbowHat.pressButtonA(false);
        } else if (buttonB.contains(x - xImageOffset, y - yImageOffset)) {
            rainbowHat.pressButtonB(false);
        } else if (buttonC.contains(x - xImageOffset, y - yImageOffset)) {
            rainbowHat.pressButtonC(false);
        }
    }

    private void onMouseEntered(MouseEvent e) {
    }

    private void onMouseExited(MouseEvent e) {
    }

    private void onMouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        boolean overButtonA = buttonA.contains(x - xImageOffset, y - yImageOffset);
        boolean overButtonB = buttonB.contains(x - xImageOffset, y - yImageOffset);
        boolean overButtonC = buttonC.contains(x - xImageOffset, y - yImageOffset);

        boolean overLed = false;
        for (int i = 0; i < leds.length; i++) {
            if (leds[i].contains(x - xImageOffset, y - yImageOffset)) {
                overLed = true;
                break;
            }
        }

        if (overButtonA || overButtonB || overButtonC || overLed) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void onMouseDragged(MouseEvent e) {

    }

}
