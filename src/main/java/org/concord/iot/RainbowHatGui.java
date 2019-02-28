package org.concord.iot;

import org.concord.iot.listeners.GraphEvent;
import org.concord.iot.listeners.GraphListener;
import org.concord.iot.listeners.ThreadPoolEvent;
import org.concord.iot.listeners.ThreadPoolListener;
import org.concord.iot.tools.ScreenshotSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.prefs.Preferences;

/**
 * @author Charles Xie
 */

class RainbowHatGui implements GraphListener, ThreadPoolListener {

    private RainbowHat rainbowHat;

    private JLabel threadPoolLabel;
    private JCheckBox showGraphCheckBox;
    private JCheckBox uploadTemperatureCheckBox;
    private JCheckBox uploadPressureCheckBox;

    private Task blinkRedLedTask;
    private Task blinkGreenLedTask;
    private Task blinkBlueLedTask;
    private Task jumpLedTask;
    private Task randomColorApaTask;
    private Task blinkApaTask;
    private Task scrollApaTask;

    RainbowHatGui() {

    }

    private void createTasks() {

        blinkRedLedTask = new Task("Blinking Red LED", rainbowHat);
        blinkRedLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setRedLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setRedLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkRedLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkGreenLedTask = new Task("Blinking Green LED", rainbowHat);
        blinkGreenLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setGreenLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setGreenLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkGreenLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkBlueLedTask = new Task("Blinking Blue LED", rainbowHat);
        blinkBlueLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setBlueLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setBlueLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkBlueLedTask.isStopped()) {
                    break;
                }
            }
        });

        jumpLedTask = new Task("Jumping LEDs", rainbowHat);
        jumpLedTask.setRunnable(() -> {
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
                if (jumpLedTask.isStopped()) {
                    break;
                }
            }
        });

        randomColorApaTask = new Task("Random Colors", rainbowHat);
        randomColorApaTask.setRunnable(() -> {
            byte[][] data = new byte[rainbowHat.getNumberOfRgbLeds()][4];
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
                if (randomColorApaTask.isStopped()) {
                    break;
                }
            }
        });

        blinkApaTask = new Task("Blink All", rainbowHat);
        blinkApaTask.setRunnable(() -> {
            while (true) {
                rainbowHat.apa102.blinkAll(Color.MAGENTA, 1000);
                if (blinkApaTask.isStopped()) {
                    break;
                }
            }
        });

        scrollApaTask = new Task("Moving Rainbows", rainbowHat);
        scrollApaTask.setRunnable(() -> {
            while (true) {
                rainbowHat.apa102.scrollRainbow(10, 4);
                rainbowHat.apa102.shift(0.01f);
                if (scrollApaTask.isStopped()) {
                    break;
                }
            }
        });

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

    private JMenuItem createCheckBoxMenuItem(Task t) {
        JMenuItem mi = new JCheckBoxMenuItem(t.getName());
        mi.addItemListener(e -> {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            t.setStopped(!selected);
            if (selected) {
                t.run();
            }
        });
        return mi;
    }

    void createAndShowGui(final RainbowHat rainbowHat) {

        this.rainbowHat = rainbowHat;
        rainbowHat.addThreadPoolListener(this);
        createTasks();

        final JFrame frame = new JFrame("IoT Workbench");
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

        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        settingsMenuItem.setToolTipText("Settings");
        settingsMenuItem.addActionListener(e -> {
            new SettingsDialog(frame, rainbowHat).setVisible(true);
        });
        fileMenu.add(settingsMenuItem);

        fileMenu.addSeparator();

        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setMnemonic(KeyEvent.VK_Q);
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK, true));
        quitMenuItem.addActionListener(e -> {
            rainbowHat.destroy();
            System.exit(0);
        });
        fileMenu.add(quitMenuItem);

        JMenu emulatorsMenu = new JMenu("Emulators");
        emulatorsMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(emulatorsMenu);

        ButtonGroup emulatorButtonGroup = new ButtonGroup();
        JMenuItem mi = new JRadioButtonMenuItem("Rainbow HAT", true);
        emulatorsMenu.add(mi);
        emulatorButtonGroup.add(mi);

        JMenu examplesMenu = new JMenu("Examples");
        examplesMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(examplesMenu);

        JMenu subMenu = new JMenu("Monochromatic LED Lights");
        examplesMenu.add(subMenu);
        subMenu.add(createCheckBoxMenuItem(blinkRedLedTask));
        subMenu.add(createCheckBoxMenuItem(blinkGreenLedTask));
        subMenu.add(createCheckBoxMenuItem(blinkBlueLedTask));
        subMenu.add(createCheckBoxMenuItem(jumpLedTask));

        subMenu = new JMenu("Trichromatic LED Lights");
        examplesMenu.add(subMenu);
        mi = new JMenuItem("Default Rainbow");
        mi.addActionListener(e -> {
            rainbowHat.setDefaultRainbow();
        });
        subMenu.add(mi);
        mi = new JMenuItem("Turn Off All Lights");
        mi.addActionListener(e -> {
            rainbowHat.apa102.setColorForAll(Color.BLACK);
        });
        subMenu.add(mi);
        subMenu.add(createCheckBoxMenuItem(blinkApaTask));
        subMenu.add(createCheckBoxMenuItem(randomColorApaTask));
        subMenu.add(createCheckBoxMenuItem(scrollApaTask));

        mi = new JMenuItem("Repeat Buzzer");
        mi.addActionListener(e -> {
            rainbowHat.buzzer.blink(1000, 10000);
        });
        examplesMenu.add(mi);

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

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Preferences pref = Preferences.userNodeForPackage(RainbowHat.class);
        Dimension boardViewSize = rainbowHat.boardView.getPreferredSize();
        frame.setSize(Math.min(pref.getInt("window_size_width", Math.max(900, boardViewSize.width)), screenSize.width), Math.min(pref.getInt("window_size_height", 600), screenSize.height));
        frame.setLocation(pref.getInt("window_location_x", (int) (screenSize.width - boardViewSize.width) / 2), pref.getInt("window_location_y", (int) (screenSize.height - boardViewSize.height) / 2));

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(final ComponentEvent e) {
                if (frame.getExtendedState() == 0) {
                    pref.putInt("window_location_x", e.getComponent().getLocation().x);
                    pref.putInt("window_location_y", e.getComponent().getLocation().y);
                }
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                if (frame.getExtendedState() == 0) {
                    pref.putInt("window_size_width", e.getComponent().getSize().width);
                    pref.putInt("window_size_height", e.getComponent().getSize().height);
                }
            }
        });

        frame.pack();
        frame.setVisible(true);

    }

    private void showAbout(JFrame frame) {
        String s = "<html><h3>" + RainbowHat.BRAND_NAME + " (V" + RainbowHat.VERSION_NUMBER + ")</h3>";
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