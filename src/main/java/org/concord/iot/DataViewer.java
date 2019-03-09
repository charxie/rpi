package org.concord.iot;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * @author Charles Xie
 */

class DataViewer {

    private IoTWorkbench workbench;

    DataViewer(IoTWorkbench workbench) {
        this.workbench = workbench;
    }

    void showDataOfType(byte type) {
        switch (type) {
            case 0:
                showData("Temperature", workbench.getTemperatureDataStore());
                break;
            case 1:
                showData("Barometric Pressure", workbench.getBarometricPressureDataStore());
                break;
            case 2:
                showData("Relative Humidity", workbench.getRelativeHumidityDataStore());
                break;
            case 3:
                showData("Visible Light", workbench.getVisibleLuxDataStore());
                break;
            case 4:
                showData("Infrared Light", workbench.getInfraredLuxDataStore());
                break;
            case 5:
                showData("Distance", workbench.getDistanceDataStore());
                break;
        }
    }

    private void showData(String name, List<SensorDataPoint> data) {
        int n = data.size();
        if (n < 1) {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(workbench.boardView), "No data has been collected.", "No data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] header = new String[]{"Time", name};
        Object[][] column = new Object[n][2];
        for (int i = 0; i < n; i++) {
            SensorDataPoint d = data.get(i);
            column[i][0] = d.getTime();
            column[i][1] = d.getValue();
        }
        showDataWindow(name, column, header);
    }

    private void showDataWindow(String title, Object[][] column, String[] header) {
        final JDialog dataWindow = new JDialog(JOptionPane.getFrameForComponent(workbench.boardView), title, true);
        dataWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final JTable table = new JTable(column, header);
        table.setModel(new DefaultTableModel(column, header) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
        dataWindow.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel p = new JPanel();
        dataWindow.getContentPane().add(p, BorderLayout.SOUTH);
        JButton button = new JButton("Copy Data");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table.selectAll();
                ActionEvent ae = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "copy");
                if (ae != null) {
                    table.getActionMap().get(ae.getActionCommand()).actionPerformed(ae);
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(workbench.boardView), "The data is now ready for pasting.", "Copy Data", JOptionPane.INFORMATION_MESSAGE);
                    table.clearSelection();
                }
            }
        });
        button.setToolTipText("Copy data to the system clipboard");
        p.add(button);
        button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataWindow.dispose();
            }
        });
        p.add(button);
        dataWindow.pack();
        dataWindow.setLocationRelativeTo(workbench.boardView);
        dataWindow.setVisible(true);
    }

}