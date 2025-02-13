/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;

/**
 * Class that downloads the binary object type as .bin file from the
 * binary and fixed binary fields.
 *
 * @author kunalgup
 */

public class DownloadBinaryObject {
    private JTable jTable;
    private Project project;
    private Table table;
    private static byte[] bytes;

    public DownloadBinaryObject(JTable jTable, MouseEvent e, Project project, Table table) {
        this.jTable = jTable;
        this.project = project;
        this.table = table;
        Point clickedPoint = e.getPoint();
        int row = jTable.rowAtPoint(clickedPoint);
        int col = jTable.columnAtPoint(clickedPoint);
        String binaryColumnName = jTable.getColumnName(col);
        FieldGroup fieldGroup = table.getPrimaryKeys();
        List<Field> fieldList = fieldGroup.getFields();
        MapValue mapValue = new MapValue();
        try {
            //setting the mapValue with the primary keys
            for (int i = 0; i < jTable.getColumnCount(); i++) {
                for (int j = 0; j < fieldList.size(); j++) {
                    Field currField = fieldList.get(j);
                    String columnName = jTable.getColumnName(i);
                    if (currField.getName().equals(columnName)) {
                        mapValue.put(columnName, (String) jTable.getValueAt(row, i));
                    }
                }
            }
        } catch (Exception e2){
            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error downloading file: " + e2.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "In progress", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                IConnection con;
                try {
                    con = DBProject.getInstance(project).getConnection();
                    MapValue result;
                    try {
                        result = con.getData(mapValue, table);
                    }catch (NullPointerException e){
                        throw new NullPointerException();
                    } catch (Exception ex) {
                        throw new RuntimeException();
                    }
                    bytes = result.getBinary(binaryColumnName);
                }catch (NullPointerException e){
                    throw new NullPointerException();
                }
                catch (Exception ex) {
                    throw new RuntimeException();
                }
            }
        });
        JFileChooser jFileChooser = getjFileChooser(jTable, clickedPoint);
        int returnVal = jFileChooser.showSaveDialog(jFileChooser.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileSelected = jFileChooser.getSelectedFile();
            ProgressManager.getInstance().run(new Task.Backgroundable(null, "Downloading binary object", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try (FileOutputStream outputStream = new FileOutputStream(fileSelected.getAbsolutePath())) {
                        outputStream.write(bytes);
                        outputStream.close();
                        setNotification(fileSelected);
                    } catch (IOException e) {
                        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error downloading file: " + e.getMessage(), NotificationType.ERROR);
                        Notifications.Bus.notify(notification, project);
                    }
                }
            });
        }
    }

    /**
     * Method that displays a dialog to user to save the .bin file to the
     * desired location
     */
    @NotNull
    private JFileChooser getjFileChooser(JTable jTable, Point clickedPoint) {
        String home = System.getProperty("user.home");
        String fileSeparator = File.separator;
        String pathname = home + fileSeparator + "Downloads" + fileSeparator + "row-" + jTable.rowAtPoint(clickedPoint) + "-" + jTable.columnAtPoint(clickedPoint) + ".binaryData" + ".bin";
        JFileChooser jFileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File selectedFile = getSelectedFile();
                if (selectedFile.exists() && getDialogType() == SAVE_DIALOG) {
                    int option = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (option) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                        default:
                            throw new IllegalStateException("Unexpected value: " + option);
                    }
                }
                super.approveSelection();
            }
        };
        jFileChooser.setSelectedFile(new File(pathname));
        return jFileChooser;
    }

    /**
     * Method to display the notification in the IDE
     */
    private void setNotification(File fileSelected) {
        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Binary object downloaded successfully: \n", NotificationType.INFORMATION);
        notification.addAction(new NotificationAction(fileSelected.getName()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(fileSelected);
                    }
                } catch (IOException | SecurityException ex) {
                    Notification errorNotification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error downloading row: " + ex.getMessage(), NotificationType.ERROR);
                    Notifications.Bus.notify(errorNotification, project);
                }
            }
        });
        Notifications.Bus.notify(notification, project);
    }
}
