package org.concord.iot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Charles Xie
 */

class RainbowHatGui {

    private JCheckBox uploadTemperatureCheckBox;
    private JCheckBox uploadPressureCheckBox;

    RainbowHatGui() {

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

        final JFrame frame = new JFrame("Rainbow HAT Emulator on Raspberry Pi");
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
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Open");
        fileMenu.add(openMenuItem);

        fileMenu.add(new ScreenshotSaver(rainbowHat.boardView, false));

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            rainbowHat.destroy();
            System.exit(0);
        });
        fileMenu.add(exitMenuItem);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        JMenuItem aboutMenuItem = new JMenuItem("About");
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

        // button panel

        JPanel buttonPanel = new JPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Exit");
        button.addActionListener(e -> {
            rainbowHat.destroy();
            frame.dispose();
            System.exit(0);
        });
        buttonPanel.add(button);

        frame.pack();
        frame.setVisible(true);

    }

}