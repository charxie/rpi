package org.concord.iot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class RainbowHatBoardView extends JPanel {

    private RainbowHat rainbowHat;

    private Image image;

    private Rectangle buttonA = new Rectangle(69, 266, 78, 30);
    private Rectangle buttonB = new Rectangle(152, 266, 78, 30);
    private Rectangle buttonC = new Rectangle(236, 266, 78, 30);

    private Rectangle led0;
    private Rectangle led1;
    private Rectangle led2;
    private Rectangle led3;
    private Rectangle led4;
    private Rectangle led5;
    private Rectangle led6;

    private int xOffset;
    private int yOffset;

    RainbowHatBoardView(RainbowHat rainbowHat) {

        super();

        this.rainbowHat = rainbowHat;

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
        xOffset = (w - wi) / 2;
        yOffset = (h - hi) / 2;

        g2.drawImage(image, xOffset, yOffset, this);

        g2.dispose();

    }

    private void onMouseClicked(MouseEvent e) {

    }

    private void onMousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (buttonA.contains(x - xOffset, y - yOffset)) {
            rainbowHat.pressButtonA(true);
        } else if (buttonB.contains(x - xOffset, y - yOffset)) {
            rainbowHat.pressButtonB(true);
        } else if (buttonC.contains(x - xOffset, y - yOffset)) {
            rainbowHat.pressButtonC(true);
        }
    }

    private void onMouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (buttonA.contains(x - xOffset, y - yOffset)) {
            rainbowHat.pressButtonA(false);
        } else if (buttonB.contains(x - xOffset, y - yOffset)) {
            rainbowHat.pressButtonB(false);
        } else if (buttonC.contains(x - xOffset, y - yOffset)) {
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

        boolean overButtonA = buttonA.contains(x - xOffset, y - yOffset);
        boolean overButtonB = buttonB.contains(x - xOffset, y - yOffset);
        boolean overButtonC = buttonC.contains(x - xOffset, y - yOffset);

        if (overButtonA || overButtonB || overButtonC) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void onMouseDragged(MouseEvent e) {

    }

}
