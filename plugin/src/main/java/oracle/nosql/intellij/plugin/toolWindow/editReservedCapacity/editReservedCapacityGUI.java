/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.editReservedCapacity;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class editReservedCapacityGUI {
    private JPanel rootPanel;
    private JPanel editCapacityPanel;
    private JTextField readCapacityTextField;
    private  JTextField writeCapacityTextField;
    private JTextField diskStorageTextField;
    private JButton applyButton;
    private JRadioButton provisionedCapacityButton;
    private JRadioButton onDemandCapacityButton;
    private JPanel capacityModePanel;
    private JLabel readCapacityLabel;
    private JLabel writeCapacityLabel;
    private JLabel diskStorageLabel;
    private JLabel readRange;
    private JLabel writeRange;
    private JLabel storageRange;
    private JFrame frame;
    private TableLimits tableLimits;
    private static int initialReadUnits,initialWriteUnits,diskStorage;

    public editReservedCapacityGUI(Project project, Table table){
        provisionedCapacityButton.setSelected(true);
        onDemandCapacityButton.setSelected(false);

        provisionedCapacityButton.addActionListener(e->{
            onDemandCapacityButton.setSelected(false);
            if(provisionedCapacityButton.isSelected()){
                readCapacityTextField.setText(String.valueOf(initialReadUnits));
                readCapacityLabel.setText("Read capacity (ReadUnits)");
                readRange.setText("Range: 1 to 40,000");
                writeCapacityTextField.setText(String.valueOf(initialWriteUnits));
                writeCapacityLabel.setText("Write capacity (WriteUnits)");
                writeRange.setText("Range: 1 to 20,000");
                diskStorageLabel.setText("Disk storage (GB)");
                storageRange.setText("Range: 1 to 5,000");
                readRange.setVisible(true);
                writeRange.setVisible(true);
            }
        });

        onDemandCapacityButton.addActionListener(e->{
            provisionedCapacityButton.setSelected(false);
            if(onDemandCapacityButton.isSelected()){
                readCapacityLabel.setText("Read capacity (ReadUnits) (Read-only)");
                readCapacityTextField.setText("Read-only");
                writeCapacityLabel.setText("Write capacity (WriteUnits) (Read-only)");
                writeCapacityTextField.setText("Read-only");
                diskStorageLabel.setText("Disk storage (GB)");
                storageRange.setText("Range: 1 to 5,000");
                readRange.setVisible(false);
                writeRange.setVisible(false);
            }
        });

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching table limits", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                IConnection con;
                try {
                    con = DBProject.getInstance(project).getConnection();
                    try {
                        TableLimits tableLimits = con.getTableLimits(table);
                        initialReadUnits = tableLimits.getReadUnits();
                        initialWriteUnits = tableLimits.getWriteUnits();
                        diskStorage = tableLimits.getStorageGB();
                        readCapacityTextField.setText(String.valueOf(initialReadUnits));
                        writeCapacityTextField.setText(String.valueOf(initialWriteUnits));
                        diskStorageTextField.setText(String.valueOf(diskStorage));
                    } catch (Exception ex) {
                        throw new Exception("Cannot fetch table limits :" + ex.getMessage());
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to connect to the cloud connection : " + ex.getMessage());
                }
            }
            @Override
            public void onFinished() {
                createFrame();
            }
        });

        applyButton.addActionListener(e->{
            tableLimits=new TableLimits(1,1,1);
            if(provisionedCapacityButton.isSelected()) {
                String ru = readCapacityTextField.getText();
                String wu = writeCapacityTextField.getText();
                String storage = diskStorageTextField.getText();
                try {
                    if (validate(ru, wu, storage)) {
                        int readUnits = Integer.parseInt(ru);
                        int writeUnits = Integer.parseInt(wu);
                        int storageUnits = Integer.parseInt(storage);
                        tableLimits = new TableLimits(readUnits, writeUnits, storageUnits);
                    }
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(null,ex.getMessage());
                    return;
                }
            }
            else if(onDemandCapacityButton.isSelected()){
                String storage = diskStorageTextField.getText();
                if(storage!=null && !storage.isEmpty()){
                    int diskstorage = Integer.parseInt(storage);
                    tableLimits = new TableLimits(diskstorage);
                }
                else{
                    JOptionPane.showMessageDialog(null,"Invalid storage capacity!");
                    return;
                }
            }
            frame.dispose();
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Changing capacity", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IConnection con;
                    try {
                        con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                        try {
                            boolean status = con.setTableLimits(table,tableLimits);
                        } catch (Exception ex) {
                            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error changing table limits  : "+ ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
                    } catch (Exception ex) {
                        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                        Notifications.Bus.notify(notification, project);
                    }
                }

                @Override
                public void onFinished() {
                    Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Table limits changed successfully!", NotificationType.INFORMATION);
                    Notifications.Bus.notify(notification, project);
                }
            });

        });

    }
    private void createFrame() {
        frame = new JFrame("Edit Reserve Capacity");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private boolean validate(String ru, String wu,String sto) throws Exception{
        int readUnits;
        int writeUnits;
        int storage;
        if(ru!=null && !ru.isEmpty()){
            readUnits = Integer.parseInt(ru);
        }
        else {
            throw new Exception("Invalid read capacity!");
        }

        if(wu!=null && !wu.isEmpty()){
            writeUnits = Integer.parseInt(wu);
        }
        else
            throw new Exception("Invalid write capacity!");

        if(sto!=null && !sto.isEmpty()){
            storage = Integer.parseInt(sto);
        }
        else
            throw new Exception("Invalid storage capacity!");

        if (!(readUnits >= 1 && readUnits <= 40000))  throw new Exception("Invalid read capacity!");
        if (!(writeUnits >= 1 && writeUnits <= 20000))  throw new Exception("Invalid write capacity!");
        if(!(storage>=1 && storage<=5000))  throw new Exception("Invalid storage capacity!");
        return true;
    }

}
