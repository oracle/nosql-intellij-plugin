/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.icons.AllIcons;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.intellij.plugin.recordView.updateRow.UpdateRowGUI;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TableRowNodeContextMenuActionGroup extends DefaultActionGroup {
    protected static Set<String> binSet;
    private static final String NOTIFICATION_GROUP_ID = "Oracle NOSQL";
    private static final String NOTIFICATION_TITLE = "Oracle NoSql explorer";
    private static boolean isJsonCollection;

    public TableRowNodeContextMenuActionGroup(Table table, JTable jTable, Project project) {
        IConnection connection;
        try { // checks if the table is json collection table
            connection = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
            String schema = connection.showSchema(table);
            JSONObject jsonObject = new JSONObject(schema);
            isJsonCollection = jsonObject.has("jsonCollection");
        } catch (Exception ex) {
            Notification notification = new Notification(
                    "Oracle NOSQL", "Oracle NoSql explorer",
                    OracleNoSqlBundle
                            .message(
                                    "oracle.nosql.toolWindow.connection.get.error") +
                            ex.getMessage(),
                    NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
        }

        binSet = new HashSet<>();
        try {
            for (int i = 0; i < table.getFieldCount(); i++) {
                String name = table.getFields().get(i).getName();
                Field.Type type = table.getFields().get(i).getType();
                if (type.equals(Field.Type.BINARY) || type.equals(Field.Type.FIXED_BINARY)) {
                    binSet.add(name);
                }
            }
        } catch (NullPointerException ex) {
            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, "Error Performing Operation : " + ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
            return;
        }
        add(new UpdateRowAction(table, jTable, project));
        add(new DeleteRowAction(table, jTable));
        add(new DownloadJsonAction(table, jTable, project));
    }

    /**
     * Class that enables user to delete a row
     */
    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class DeleteRowAction extends AnAction {
        private static final String DELETE_ROW = "Delete Row";
        private String jString;
        private Table table;
        private JTable jTable;

        public DeleteRowAction(Table table, JTable jTable) {
            super(DELETE_ROW);
            this.table = table;
            this.jTable = jTable;
            final int r = jTable.getSelectedRow();
            jString = getPrimaryKeys(table,jTable,r);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String confirmMsg = "Are you sure you want to delete this row ?";
            Object[] msg = {confirmMsg};
            int result = JOptionPane.showConfirmDialog(null, msg, "DELETE ROW", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, AllIcons.General.BalloonWarning);
            if (result == JOptionPane.YES_OPTION) {
                ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Deleting Row ", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(e.getProject())).getConnection();
                            try {
                                Schema schema = table.getSchema();
                                con.deleteRow(table, jString);
                                schema.recursiveRefresh();
                            } catch (Exception ex) {
                                Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, OracleNoSqlBundle.message("oracle.nosql.toolWindow.deleteRow.error") + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, e.getProject());
                                return;
                            }
                            table.recursiveRefresh();
                        } catch (Exception ex) {
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, e.getProject());
                        }
                    }
                });
            }
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

    /**
     * Class that enables user to update the fields of a row.
     * Primary Key cannot be updated.
     */

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class UpdateRowAction extends AnAction {
        private static final String UPDATE_ROW = "Update Row";
        private static final String ERROR_UPDATING_ROW = "Error updating row:";
        private Table table;
        private JTable jTable;
        private String jString;
        private String primaryKeys;
        public DataBaseVirtualFile file;
        private static byte[] bytes;

        public UpdateRowAction(Table table, JTable jTable, Project project) {
            super(UPDATE_ROW);
            this.table = table;
            this.jTable = jTable;
            this.file = new DataBaseVirtualFile(table);
            final int r = jTable.getSelectedRow();
            primaryKeys = getPrimaryKeys(table,jTable,r);
            StringBuilder jsonString = new StringBuilder();
            jsonString.append("{");
            boolean isRowDataEmpty = false;
            for (int i = 0; i < jTable.getColumnCount(); i++) {
                if (jTable.getValueAt(r, i) != "") {
                    if (binSet.contains(jTable.getColumnName(i))) {
                        String binaryColumnName = jTable.getColumnName(i);
                        MapValue mapValue = new MapValue();
                        //setting the mapValue with the primary keys
                        try {
                            FieldGroup fieldGroup = table.getPrimaryKeys();
                            List<Field> fieldList = fieldGroup.getFields();
                            for (int k = 0; k < jTable.getColumnCount(); k++) {
                                for (int l = 0; l < fieldList.size(); l++) {
                                    Field currField = fieldList.get(l);
                                    String columnName = jTable.getColumnName(k);
                                    if (currField.getName().equals(columnName)) {
                                        mapValue.put(columnName, (String) jTable.getValueAt(r, k));
                                    }
                                }
                            }
                        } catch (NullPointerException ex) {
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_UPDATING_ROW + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                            return;
                        }

                        IConnection con;
                        try {
                            con = DBProject.getInstance(project).getConnection();
                            MapValue result;
                            try {
                                result = con.getData(mapValue, table);
                            } catch (Exception ex) {
                                Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_UPDATING_ROW + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, project);
                                return;
                            }
                            bytes = result.getBinary(binaryColumnName);
                        } catch (Exception ex) {
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_UPDATING_ROW + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                            return;
                        }

                        if (bytes != null) {
                            byte[] encoded = Base64.getEncoder().encode(bytes);
                            jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append("\"").append(new String(encoded)).append("\"").append(",");
                        } else {
                            jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append("null").append(",");
                        }
                    }
                    else if(TableRowNodeContextMenuActionGroup.isJsonCollection && jTable.getColumnName(i).equals("Rowdata")){
                        String rowdata = (String) jTable.getValueAt(r, i);
                        if(rowdata.equals("{}"))
                            isRowDataEmpty = true;
                        jsonString.append(rowdata.substring(1, rowdata.length()));
                    }
                    else
                        jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append(jTable.getValueAt(r, i)).append(",");
                } else {
                    jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append("null").append(",");
                }
            }
            if(isRowDataEmpty)
                jString = jsonString.toString().trim().substring(0, jsonString.length() - 2) + "}";
            else
                jString = jsonString.toString().trim().substring(0, jsonString.length() - 1) + "}";
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new UpdateRowGUI(e.getProject(), table, jString, primaryKeys));
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

    /**
     * Class that enables user to download a row in JSON
     */

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class DownloadJsonAction extends AnAction {
        private static final String DOWNLOAD_ROW = "Download JSON";
        private static final String ERROR_DOWNLOADING_ROW = "Error downloading row:";
        private Table table;
        private JTable jTable;
        private String jString;
        private JsonNode json;
        private File file;
        private static byte[] bytes;
        private Project project;

        public DownloadJsonAction(Table table, JTable jTable, Project project) {
            super(DOWNLOAD_ROW);
            this.table = table;
            this.jTable = jTable;
            this.project = project;

            final int r = jTable.getSelectedRow();
            StringBuilder jsonString = new StringBuilder();
            jsonString.append("{");
            for (int i = 0; i < jTable.getColumnCount(); i++) {
                if (jTable.getValueAt(r, i) != "") {
                    Object temp = jTable.getValueAt(r, i);
                    if (binSet.contains(jTable.getColumnName(i))) {
                        String binaryColumnName = jTable.getColumnName(i);
                        MapValue mapValue = new MapValue();
                        //setting the mapValue with the primary keys
                        try {
                            FieldGroup fieldGroup = table.getPrimaryKeys();
                            List<Field> fieldList = fieldGroup.getFields();
                            for (int k = 0; k < jTable.getColumnCount(); k++) {
                                for (int l = 0; l < fieldList.size(); l++) {
                                    Field currField = fieldList.get(l);
                                    String columnName = jTable.getColumnName(k);
                                    if (currField.getName().equals(columnName)) {
                                        mapValue.put(columnName, (String) jTable.getValueAt(r, k));
                                    }
                                }
                            }
                        } catch (Exception e){
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_DOWNLOADING_ROW + e.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                            return;
                        }

                        IConnection con;
                        try {
                            con = DBProject.getInstance(project).getConnection();
                            MapValue result;
                            try {
                                result = con.getData(mapValue, table);
                            } catch (Exception ex) {
                                Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_DOWNLOADING_ROW + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, project);
                                return;
                            }
                            bytes = result.getBinary(binaryColumnName);
                        } catch (Exception ex) {
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_DOWNLOADING_ROW + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                            return;
                        }

                        if (bytes != null) {
                            byte[] encoded = Base64.getEncoder().encode(bytes);
                            jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append("\"").append(new String(encoded)).append("\"").append(",");
                        } else {
                            jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append("null").append(",");
                        }
                    }
                    else if(TableRowNodeContextMenuActionGroup.isJsonCollection && jTable.getColumnName(i).equals("Rowdata")){
                        String rowdata = (String) jTable.getValueAt(r, i);
                        jsonString.append(rowdata.substring(1, rowdata.length()));
                    }
                    else
                        jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append(temp).append(",");
                } else {
                    jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append("null").append(",");
                }
            }

            jString = jsonString.toString().trim().substring(0, jsonString.length() - 1) + "}";
        }

        /**
         * Method that displays a dialog to user to save the JSON file to the
         * desired location
         */
        @NotNull
        private JFileChooser getjFileChooser() {
            String home = System.getProperty("user.home");
            String fileSeparator = File.separator;
            String pathname = home + fileSeparator + "Downloads" + fileSeparator + "row" + jTable.getSelectedRow() + ".json";
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

        @Override
        public void actionPerformed(@NotNull AnActionEvent e1) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                json = mapper.readTree(jString);
            } catch (JsonProcessingException e) {
                Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_DOWNLOADING_ROW + e.getMessage(), NotificationType.ERROR);
                Notifications.Bus.notify(notification, e1.getProject());
                return;
            }
            JFileChooser jFileChooser = getjFileChooser();
            int returnVal = jFileChooser.showSaveDialog(jFileChooser.getParent());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = jFileChooser.getSelectedFile();
                ProgressManager.getInstance().run(new Task.Backgroundable(null, "Downloading row", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            mapper.writeValue(file, json);
                            setNotification(e1);
                        } catch (IOException e) {
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_DOWNLOADING_ROW + e.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, e1.getProject());
                        }
                    }
                });
            }
        }

        /**
         * Method to display the notification in the IDE
         */
        private void setNotification(@NotNull AnActionEvent e1) {
            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, "Row downloaded successfully: \n", NotificationType.INFORMATION);
            notification.addAction(new NotificationAction(file.getName()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(file);
                        }
                    } catch (IOException | SecurityException ex) {
                        Notification errorNotification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, ERROR_DOWNLOADING_ROW + ex.getMessage(), NotificationType.ERROR);
                        Notifications.Bus.notify(errorNotification, e1.getProject());
                    }
                }
            });
            Notifications.Bus.notify(notification, e1.getProject());
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

    // returns primary keys and their values from table
    private static String getPrimaryKeys(Table table, JTable jTable, int row){
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");

        FieldGroup pkGroup = table.getPrimaryKeys();
        List<Field> pkList = pkGroup.getFields();
        for (int i = 0; i < jTable.getColumnCount(); i++) {
            for (int j = 0; j < pkList.size(); j++) {
                Field currPkField = pkList.get(j);
                String columnName = jTable.getColumnName(i);
                if (currPkField.getName().equals(columnName)) {
                    jsonString.append("\"").append(jTable.getColumnName(i)).append("\":").append(jTable.getValueAt(row, i)).append(" , ");
                }
            }
        }
        return jsonString.toString().trim().substring(0, jsonString.length() - 2) + "}";
    }
}