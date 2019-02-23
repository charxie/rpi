package org.concord.iot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Charles Xie
 */

class RainbowHatGui {

    static void createAndShowGui(final RainbowHat rainbowHat) {

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
        fileMenu.add(aboutMenuItem);

        JPanel contentPane = new JPanel(new BorderLayout(5, 5));
        frame.setContentPane(contentPane);
        contentPane.add(rainbowHat.boardView, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        contentPane.add(actionPanel, BorderLayout.NORTH);

        actionPanel.add(new JLabel("Upload: "));
        JCheckBox uploadTemperatureCheckBox = new JCheckBox("Temperature");
        actionPanel.add(uploadTemperatureCheckBox);
        JCheckBox uploadPressureCheckBox = new JCheckBox("Pressure");
        actionPanel.add(uploadPressureCheckBox);

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
