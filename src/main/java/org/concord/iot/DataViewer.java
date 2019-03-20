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
                showData("Light", "Visible", workbench.getVisibleLuxDataStore(), "Infrared", workbench.getInfraredLuxDataStore());
                break;
            case 4:
                showData("Distance", "Lidar", workbench.getLidarDistanceDataStore(), "Ultrsonic", workbench.getUltrasonicDistanceDataStore());
                break;
            case 5:
                showData("Acceleration", "Ax", workbench.getAxDataStore(), "Ay", workbench.getAyDataStore(), "Az", workbench.getAzDataStore());
                break;
        }
    }

    private void showData(String name, String s1, List<SensorDataPoint> data1, String s2, List<SensorDataPoint> data2, String s3, List<SensorDataPoint> data3) {
        int n = data1.size();
        if (n < 1) {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(workbench.boardView), "No data has been collected.", "No data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] header = new String[]{"Time", s1, s2, s3};
        Object[][] column = new Object[n][4];
        for (int i = 0; i < n; i++) {
            SensorDataPoint d = data1.get(i);
            column[i][0] = d.getTime();
            column[i][1] = d.getValue();
            column[i][2] = data2.get(i).getValue();
            column[i][3] = data3.get(i).getValue();
        }
        showDataWindow(name, column, header);
    }

    private void showData(String name, String s1, List<SensorDataPoint> data1, String s2, List<SensorDataPoint> data2) {
        int n1 = data1.size();
        int n2 = data2.size();
        if (n1 < 1 && n2 < 1) {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(workbench.boardView), "No data has been collected.", "No data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int n = Math.max(n1, n2);
        String[] header = new String[]{"Time", s1, s2};
        Object[][] column = new Object[n][3];
        for (int i = 0; i < n; i++) {
            if (!data1.isEmpty() && data2.isEmpty()) {
                column[i][0] = data1.get(i).getTime();
                column[i][1] = data1.get(i).getValue();
                column[i][2] = "-";
            } else if (!data2.isEmpty() && data1.isEmpty()) {
                column[i][0] = data2.get(i).getTime();
                column[i][1] = "-";
                column[i][2] = data2.get(i).getValue();
            } else {
                column[i][0] = data1.get(i).getTime();
                column[i][1] = data1.get(i).getValue();
                column[i][2] = data2.get(i).getValue();
            }
        }
        showDataWindow(name, column, header);
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