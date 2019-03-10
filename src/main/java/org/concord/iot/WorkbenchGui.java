package org.concord.iot;

import org.concord.iot.listeners.GraphEvent;
import org.concord.iot.listeners.GraphListener;
import org.concord.iot.listeners.ThreadPoolEvent;
import org.concord.iot.listeners.ThreadPoolListener;
import org.concord.iot.tools.ScreenshotSaver;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.prefs.Preferences;

/**
 * @author Charles Xie
 */

class WorkbenchGui implements GraphListener, ThreadPoolListener {

    private IoTWorkbench workbench;
    private JLabel threadPoolLabel;

    WorkbenchGui() {

    }

    private JMenuItem createMenuItem(Task t, boolean radio) {
        JMenuItem mi = radio ? new JRadioButtonMenuItem((t.getName())) : new JCheckBoxMenuItem(t.getName());
        mi.addItemListener(e -> {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            t.setStopped(!selected);
            if (selected) {
                t.run();
            }
        });
        return mi;
    }

    void createAndShowGui(final IoTWorkbench workbench) {

        this.workbench = workbench;
        workbench.addThreadPoolListener(this);

        final JFrame frame = new JFrame(IoTWorkbench.BRAND_NAME + " (" + IoTWorkbench.VERSION_NUMBER + ")");
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(IoTWorkbench.class.getResource("images/frame.png")));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                workbench.destroy();
                System.exit(0);
            }
        });

        // menu bar

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // file menu

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setToolTipText("Open a design");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK, true));
        fileMenu.add(openMenuItem);

        fileMenu.add(new ScreenshotSaver(workbench.boardView, false));

        fileMenu.addSeparator();

        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        settingsMenuItem.setToolTipText("Settings");
        settingsMenuItem.addActionListener(e -> {
            new SettingsDialog(frame, workbench).setVisible(true);
        });
        fileMenu.add(settingsMenuItem);

        fileMenu.addSeparator();

        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setMnemonic(KeyEvent.VK_Q);
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK, true));
        quitMenuItem.addActionListener(e -> {
            workbench.destroy();
            System.exit(0);
        });
        fileMenu.add(quitMenuItem);

        // board menu

        JMenu boardsMenu = new JMenu("Boards");
        boardsMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(boardsMenu);
        final JMenuItem miRainbowHAT = new JRadioButtonMenuItem("Rainbow HAT");
        final JMenuItem miSensorHub = new JRadioButtonMenuItem("Sensor Hub");
        boardsMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                switch (workbench.getBoardType()) {
                    case IoTWorkbench.RAINBOW_HAT:
                        Util.setSelectedSilently(miRainbowHAT, true);
                        break;
                    case IoTWorkbench.SENSOR_HUB:
                        Util.setSelectedSilently(miSensorHub, true);
                        break;
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        ButtonGroup boardsButtonGroup = new ButtonGroup();
        miRainbowHAT.addItemListener(e -> {
            workbench.setBoardType(IoTWorkbench.RAINBOW_HAT);
            workbench.boardView.repaint();
            final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
            pref.putInt("board_type", IoTWorkbench.RAINBOW_HAT);
        });
        boardsMenu.add(miRainbowHAT);
        boardsButtonGroup.add(miRainbowHAT);

        miSensorHub.addItemListener(e -> {
            workbench.setBoardType(IoTWorkbench.SENSOR_HUB);
            workbench.boardView.repaint();
            final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
            pref.putInt("board_type", IoTWorkbench.SENSOR_HUB);
        });
        boardsMenu.add(miSensorHub);
        boardsButtonGroup.add(miSensorHub);

        JMenu sensorsMenu = new JMenu("Sensors");
        sensorsMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(sensorsMenu);
        final JMenuItem graphMenuItem = new JCheckBoxMenuItem("Show Graph");
        final JMenuItem temperatureSensorMenuItem = new JCheckBoxMenuItem("Allow Transmission of Temperature Data");
        final JMenuItem barometricPressureSensorMenuItem = new JCheckBoxMenuItem("Allow Transmission of Barometric Pressure Data");
        final JMenuItem relativeHumiditySensorMenuItem = new JCheckBoxMenuItem("Allow Transmission of Relative Humidity Data");
        final JMenuItem visibleLightSensorMenuItem = new JCheckBoxMenuItem("Allow Transmission of Visible Light Data");
        final JMenuItem infraredLightSensorMenuItem = new JCheckBoxMenuItem("Allow Transmission of Infrared Light Data");
        final JMenuItem distanceSensorMenuItem = new JCheckBoxMenuItem("Allow Transmission of Distance Data");
        sensorsMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                Util.setSelectedSilently(graphMenuItem, workbench.boardView.getShowGraph());
                Util.setSelectedSilently(temperatureSensorMenuItem, workbench.getAllowTemperatureTransmission());
                Util.setSelectedSilently(barometricPressureSensorMenuItem, workbench.getAllowBarometricPressureTransmission());
                Util.setSelectedSilently(relativeHumiditySensorMenuItem, workbench.getAllowRelativeHumidityTransmission());
                Util.setSelectedSilently(visibleLightSensorMenuItem, workbench.getAllowVisibleLuxTransmission());
                Util.setSelectedSilently(infraredLightSensorMenuItem, workbench.getAllowInfraredLuxTransmission());
                Util.setSelectedSilently(distanceSensorMenuItem, workbench.getAllowDistanceTransmission());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        JMenuItem clearDataStoresMenuItem = new JMenuItem("Clear Data Stores");
        clearDataStoresMenuItem.addActionListener(e -> {
            workbench.clearDataStores();
            workbench.boardView.repaint();
        });
        sensorsMenu.add(clearDataStoresMenuItem);

        graphMenuItem.setToolTipText("Show graph");
        graphMenuItem.addItemListener(e -> {
            workbench.boardView.setShowGraph(e.getStateChange() == ItemEvent.SELECTED);
            final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
            pref.putBoolean("show_graph", workbench.boardView.getShowGraph());
        });
        sensorsMenu.add(graphMenuItem);
        sensorsMenu.addSeparator();

        temperatureSensorMenuItem.setToolTipText("Permit the temperature data to be uploaded");
        temperatureSensorMenuItem.addItemListener(e -> {
            workbench.setAllowTemperatureTransmission(e.getStateChange() == ItemEvent.SELECTED);
        });
        sensorsMenu.add(temperatureSensorMenuItem);

        barometricPressureSensorMenuItem.setToolTipText("Permit the barometric pressure data to be uploaded");
        barometricPressureSensorMenuItem.addItemListener(e -> {
            workbench.setAllowBarometricPressureTransmission(e.getStateChange() == ItemEvent.SELECTED);
        });
        sensorsMenu.add(barometricPressureSensorMenuItem);

        relativeHumiditySensorMenuItem.setToolTipText("Permit the barometric pressure data to be uploaded");
        relativeHumiditySensorMenuItem.addItemListener(e -> {
            workbench.setAllowRelativeHumidityTransmission(e.getStateChange() == ItemEvent.SELECTED);
        });
        sensorsMenu.add(relativeHumiditySensorMenuItem);

        visibleLightSensorMenuItem.setToolTipText("Permit the visible light data to be uploaded");
        visibleLightSensorMenuItem.addItemListener(e -> {
            workbench.setAllowVisibleLuxTransmission(e.getStateChange() == ItemEvent.SELECTED);
        });
        sensorsMenu.add(visibleLightSensorMenuItem);

        infraredLightSensorMenuItem.setToolTipText("Permit the infrared light data to be uploaded");
        infraredLightSensorMenuItem.addItemListener(e -> {
            workbench.setAllowInfraredLuxTransmission(e.getStateChange() == ItemEvent.SELECTED);
        });
        sensorsMenu.add(infraredLightSensorMenuItem);

        distanceSensorMenuItem.setToolTipText("Permit the distance data to be uploaded");
        distanceSensorMenuItem.addItemListener(e -> {
            workbench.setAllowDistanceTransmission(e.getStateChange() == ItemEvent.SELECTED);
        });
        sensorsMenu.add(distanceSensorMenuItem);

        JMenu examplesMenu = new JMenu("Examples");
        examplesMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(examplesMenu);

        JMenu subMenu = new JMenu("Monochromatic LED Lights");
        examplesMenu.add(subMenu);
        subMenu.add(createMenuItem(workbench.taskFactory.blinkRedLedTask, false));
        subMenu.add(createMenuItem(workbench.taskFactory.blinkGreenLedTask, false));
        subMenu.add(createMenuItem(workbench.taskFactory.blinkBlueLedTask, false));
        subMenu.add(createMenuItem(workbench.taskFactory.jumpLedTask, false));

        subMenu = new JMenu("Trichromatic LED Lights");
        examplesMenu.add(subMenu);

        ButtonGroup bg = new ButtonGroup();
        JMenuItem mi = new JRadioButtonMenuItem("Default Rainbow");
        mi.addActionListener(e -> {
            synchronized (workbench.taskFactory.getLock()) {
                workbench.taskFactory.stopAllApaTasks();
                workbench.setDefaultRainbow();
            }
        });
        subMenu.add(mi);
        bg.add(mi);

        mi = new JRadioButtonMenuItem("Turn Off All LED Lights");
        mi.addActionListener(e -> {
            synchronized (workbench.taskFactory.getLock()) {
                workbench.taskFactory.stopAllApaTasks();
                workbench.apa102.setColorForAll(Color.BLACK);
                if (workbench.boardView != null) {
                    workbench.boardView.setColorForAllLeds(Color.BLACK);
                }
            }
        });
        subMenu.add(mi);
        bg.add(mi);

        mi = createMenuItem(workbench.taskFactory.blinkApaTask, true);
        subMenu.add(mi);
        bg.add(mi);

        mi = createMenuItem(workbench.taskFactory.movingRainbowApaTask, true);
        subMenu.add(mi);
        bg.add(mi);

        mi = createMenuItem(workbench.taskFactory.randomColorsApaTask, true);
        subMenu.add(mi);
        bg.add(mi);

        mi = createMenuItem(workbench.taskFactory.bouncingDotApaTask, true);
        subMenu.add(mi);
        bg.add(mi);

        mi = createMenuItem(workbench.taskFactory.movingTrainsApaTask, true);
        subMenu.add(mi);
        bg.add(mi);

        mi = createMenuItem(workbench.taskFactory.rippleEffectApaTask, true);
        subMenu.add(mi);
        bg.add(mi);

        mi = new JMenuItem("Repeat Buzzer");
        mi.addActionListener(e -> {
            workbench.buzzer.blink(1000, 10000);
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
        contentPane.add(workbench.boardView, BorderLayout.CENTER);

        // tool bar

        //JPanel toolPanel = new JPanel();
        //contentPane.add(toolPanel, BorderLayout.NORTH);

        // status panel

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        threadPoolLabel = new JLabel("<html><font size=2>Thread pool: size = " + workbench.getThreadPoolSize() + ", active = " + workbench.getActiveThreadCount() + "</font></html>");
        statusPanel.add(threadPoolLabel);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        Dimension boardViewSize = workbench.boardView.getPreferredSize();
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
        String s = "<html><h3>" + IoTWorkbench.BRAND_NAME + " (V" + IoTWorkbench.VERSION_NUMBER + ")</h3>";
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
        JOptionPane.showMessageDialog(frame, new JLabel(s), "About the software", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(IoTWorkbench.class.getResource("images/frame.png")));
    }

    @Override
    public void graphClosed(GraphEvent e) {
        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        pref.putBoolean("show_graph", workbench.boardView.getShowGraph());
    }

    @Override
    public void graphOpened(GraphEvent e) {
        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        pref.putBoolean("show_graph", workbench.boardView.getShowGraph());
    }

    @Override
    public void updated(ThreadPoolEvent e) {
        threadPoolLabel.setText("<html><font size=2>Thread pool: size = " + workbench.getThreadPoolSize() + ", active = " + workbench.getActiveThreadCount() + "</font></html>");
    }

}