package org.concord.iot;

import org.concord.iot.tools.ScreenshotSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

/**
 * @author Charles Xie
 */

class RainbowHatGui implements GraphListener, ThreadPoolListener {

    private RainbowHat rainbowHat;

    private JLabel threadPoolLabel;
    private JCheckBox showGraphCheckBox;
    private JCheckBox uploadTemperatureCheckBox;
    private JCheckBox uploadPressureCheckBox;

    private volatile boolean stopBlinkRedLed;
    private volatile boolean stopBlinkGreenLed;
    private volatile boolean stopBlinkBlueLed;
    private volatile boolean stopLedJump;
    private volatile boolean stopRgbLedAnimation;

    RainbowHatGui() {

    }

    void setShowGraphCheckBox(boolean checked) {
        if (showGraphCheckBox != null) {
            Util.setSelectedSilently(showGraphCheckBox, checked);
        }
    }

    void setUploadTemperatureCheckBox(boolean checked) {
        if (uploadTemperatureCheckBox != null) {
            Util.setSelectedSilently(uploadTemperatureCheckBox, checked);
        }
    }

    void setUploadPressureCheckBox(boolean checked) {
        if (uploadPressureCheckBox != null) {
            Util.setSelectedSilently(uploadPressureCheckBox, checked);
        }
    }

    void createAndShowGui(final RainbowHat rainbowHat) {

        this.rainbowHat = rainbowHat;
        rainbowHat.addThreadPoolListener(this);

        final JFrame frame = new JFrame("Rainbow HAT Emulator on Raspberry Pi");
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(RainbowHat.class.getResource("images/frame.png")));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                rainbowHat.destroy();
                System.exit(0);
            }
        });

        // menu bar

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setToolTipText("Open a design");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK, true));
        fileMenu.add(openMenuItem);

        fileMenu.add(new ScreenshotSaver(rainbowHat.boardView, false));

        fileMenu.addSeparator();

        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setMnemonic(KeyEvent.VK_Q);
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK, true));
        quitMenuItem.addActionListener(e -> {
            rainbowHat.destroy();
            System.exit(0);
        });
        fileMenu.add(quitMenuItem);

        JMenu examplesMenu = new JMenu("Examples");
        examplesMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(examplesMenu);

        JMenuItem miExample = new JCheckBoxMenuItem("Blinking Red LED");
        miExample.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                stopBlinkRedLed = false;
                rainbowHat.submitTask(() -> {
                    while (true) {
                        try {
                            rainbowHat.setRedLedState(true, true);
                            Thread.sleep(500);
                            rainbowHat.setRedLedState(false, true);
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                        if (stopBlinkRedLed) {
                            break;
                        }
                    }
                });
            } else {
                stopBlinkRedLed = true;
            }
        });
        examplesMenu.add(miExample);

        miExample = new JCheckBoxMenuItem("Blinking Green LED");
        miExample.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                stopBlinkGreenLed = false;
                rainbowHat.submitTask(() -> {
                    while (true) {
                        try {
                            rainbowHat.setGreenLedState(true, true);
                            Thread.sleep(500);
                            rainbowHat.setGreenLedState(false, true);
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                        if (stopBlinkGreenLed) {
                            break;
                        }
                    }
                });
            } else {
                stopBlinkGreenLed = true;
            }
        });
        examplesMenu.add(miExample);

        miExample = new JCheckBoxMenuItem("Blinking Blue LED");
        miExample.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                stopBlinkBlueLed = false;
                rainbowHat.submitTask(() -> {
                    while (true) {
                        try {
                            rainbowHat.setBlueLedState(true, true);
                            Thread.sleep(500);
                            rainbowHat.setBlueLedState(false, true);
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                        if (stopBlinkBlueLed) {
                            break;
                        }
                    }
                });
            } else {
                stopBlinkBlueLed = true;
            }
        });
        examplesMenu.add(miExample);

        miExample = new JCheckBoxMenuItem("LED Jump");
        miExample.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                stopLedJump = false;
                rainbowHat.submitTask(() -> {
                    while (true) {
                        try {
                            rainbowHat.setRedLedState(true, true);
                            Thread.sleep(500);
                            rainbowHat.setRedLedState(false, true);
                            rainbowHat.setGreenLedState(true, true);
                            Thread.sleep(500);
                            rainbowHat.setGreenLedState(false, true);
                            rainbowHat.setBlueLedState(true, true);
                            Thread.sleep(500);
                            rainbowHat.setBlueLedState(false, true);
                        } catch (InterruptedException ex) {
                        }
                        if (stopLedJump) {
                            break;
                        }
                    }
                });
            } else {
                stopLedJump = true;
            }
        });
        examplesMenu.add(miExample);
        examplesMenu.addSeparator();

        miExample = new JMenuItem("Bainbow LEDs");
        miExample.addActionListener(e -> {
            rainbowHat.submitTask(() -> {
                rainbowHat.setDefaultRainbow();
            });
        });
        examplesMenu.add(miExample);

        miExample = new JMenuItem("Blinking RGB LEDs");
        miExample.addActionListener(e -> {
            rainbowHat.submitTask(() -> {
                rainbowHat.apa102.blinkAll(Color.MAGENTA, 5, 1000);
            });
        });
        examplesMenu.add(miExample);

        miExample = new JCheckBoxMenuItem("Animate RGB LEDs");
        miExample.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                stopRgbLedAnimation = false;
                rainbowHat.submitTask(() -> {
                    byte[][] data = new byte[RainbowHatState.NUMBER_OF_RGB_LEDS][4];
                    while (true) {
                        for (int i = 0; i < data.length; i++) {
                            data[i][0] = (byte) (255 * Math.random());
                            data[i][1] = (byte) (255 * Math.random());
                            data[i][2] = (byte) (255 * Math.random());
                        }
                        rainbowHat.apa102.setData(data);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                        if (stopRgbLedAnimation) {
                            break;
                        }
                    }
                });
            } else {
                stopRgbLedAnimation = true;
            }
        });
        examplesMenu.add(miExample);
        examplesMenu.addSeparator();

        miExample = new JMenuItem("Repeat Buzzer");
        miExample.addActionListener(e -> {
            rainbowHat.buzzer.blink(1000, 10000);
        });
        examplesMenu.add(miExample);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setToolTipText("About this software");
        aboutMenuItem.addActionListener(e -> {
            showAbout(frame);
        });
        helpMenu.add(aboutMenuItem);

        JPanel contentPane = new JPanel(new BorderLayout(5, 5));
        frame.setContentPane(contentPane);
        contentPane.add(rainbowHat.boardView, BorderLayout.CENTER);

        // tool bar

        JPanel toolPanel = new JPanel();
        contentPane.add(toolPanel, BorderLayout.NORTH);

        toolPanel.add(new JLabel("Upload: "));
        uploadTemperatureCheckBox = new JCheckBox("Temperature", rainbowHat.getAllowTemperatureTransmission());
        uploadTemperatureCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rainbowHat.setAllowTemperatureTransmission(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        toolPanel.add(uploadTemperatureCheckBox);

        uploadPressureCheckBox = new JCheckBox("Pressure", rainbowHat.getAllowBarometricPressureTransmission());
        uploadPressureCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rainbowHat.setAllowBarometricPressureTransmission(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        toolPanel.add(uploadPressureCheckBox);

        showGraphCheckBox = new JCheckBox("Graph", rainbowHat.boardView.getShowGraph());
        showGraphCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rainbowHat.boardView.setShowGraph(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        toolPanel.add(showGraphCheckBox);

        // status panel

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        threadPoolLabel = new JLabel("<html><font size=2>Thread pool: size = " + rainbowHat.getThreadPoolSize() + ", active = " + rainbowHat.getActiveThreadCount() + "</font></html>");
        statusPanel.add(threadPoolLabel);

        frame.pack();
        frame.setVisible(true);

    }

    private void showAbout(JFrame frame) {
        String s = "<html><h3>Rainbow HAT Emulator</h3>";
        s += "<h4><i>Learning to create the Internet of Things</i></h4>";
        s += "Charles Xie, &copy; 2019-" + Calendar.getInstance().get(Calendar.YEAR);
        s += "<hr>";
        s += "<h4>Credit:</h4>";
        s += "<font size=2>This program is created by Dr. Charles Xie. Funding";
        s += "<br>";
        s += "was provided by the National Science Foundation<br>";
        s += "under grant 1721054 that was awarded to Dr.Xie.";
        s += "<h4>License:</h4>";
        s += "<font size=2>This software is provided to you as it is under";
        s += "<br>";
        s += "the MIT License.";
        s += "</html>";
        JOptionPane.showMessageDialog(frame, new JLabel(s), "About the software", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(RainbowHat.class.getResource("images/frame.png")));
    }

    @Override
    public void graphClosed(GraphEvent e) {
        Util.setSelectedSilently(showGraphCheckBox, false);
    }

    @Override
    public void graphOpened(GraphEvent e) {
        Util.setSelectedSilently(showGraphCheckBox, true);
    }

    @Override
    public void updated(ThreadPoolEvent e) {
        threadPoolLabel.setText("<html><font size=2>Thread pool: size = " + rainbowHat.getThreadPoolSize() + ", active = " + rainbowHat.getActiveThreadCount() + "</font></html>");
    }

}