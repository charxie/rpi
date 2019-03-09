package org.concord.iot;

import org.concord.iot.tools.SpringUtilities;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author Charles Xie
 */

class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public SettingsDialog(final Frame parent, final IoTWorkbench workbench) {

        super(parent, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Settings");

        getContentPane().setLayout(new BorderLayout());
        final JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        getContentPane().add(panel, BorderLayout.CENTER);

        final JTextField userNameField = new JTextField(workbench.user.getName());
        final JTextField numberOfLedsField = new JTextField(workbench.getNumberOfRgbLeds() + "", 12);

        final ActionListener okListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String userName = userNameField.getText();
                if (userName.trim().length() >= 4) {
                    workbench.user.setName(userNameField.getName());
                } else {
                    JOptionPane.showMessageDialog(SettingsDialog.this, "User name is not allowed to have less than four characters: " + userName, "Error", JOptionPane.ERROR_MESSAGE);
                }
                String s = numberOfLedsField.getText();
                if (s != null && !s.trim().equals("")) {
                    int n = 0;
                    try {
                        n = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(SettingsDialog.this, s + " cannot be parsed!", "Format Error", JOptionPane.ERROR_MESSAGE);
                    }
                    if (n > 0) {
                        workbench.setNumberOfRgbLeds(n);
                        workbench.boardView.repaint();
                    }
                }
                dispose();
            }
        };

        // User name
        panel.add(new JLabel("User Name: "));
        panel.add(userNameField);

        // RGB LED number
        panel.add(new JLabel("Number of APA RGB LEDs: "));
        panel.add(numberOfLedsField);

        SpringUtilities.makeCompactGrid(panel, 2, 2, 8, 8, 8, 8);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(okListener);
        okButton.setActionCommand("OK");
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dispose();
            }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPanel.add(cancelButton);

        pack();
        setLocationRelativeTo(parent);

    }

}