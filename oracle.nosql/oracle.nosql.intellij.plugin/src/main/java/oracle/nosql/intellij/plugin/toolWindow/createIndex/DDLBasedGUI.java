/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createIndex;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import org.jetbrains.annotations.NotNull;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static oracle.nosql.intellij.plugin.toolWindow.createIndex.MainFormGUI.frame;

public class DDLBasedGUI extends JDialog {
    private static final String NOTIFICATION_GROUP_ID = "Oracle NOSQL";
    private static final String NOTIFICATION_TITLE = "Oracle NoSql explorer";
    private JPanel rootPanel;
    private JPanel ddlMainPanel;
    private JTextArea textArea1;
    private JButton addIndexButton;

    public DDLBasedGUI(Project project, Table table) {
        textArea1.setToolTipText("Write/Paste Create Index query");
        addIndexButton.setForeground(JBColor.GREEN);
        addIndexButton.addActionListener(e -> {
            if (validateDdl()) {
                frame.dispose();
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Creating index", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                            try {
                                String createIdxDdl = getDDLStatement();
                                Schema schema = table.getSchema();
                                con.createIndexUsingDdl(createIdxDdl);
                                schema.recursiveRefresh();
                            } catch (Exception ex) {
                                Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, OracleNoSqlBundle.message("oracle.nosql.toolWindow.createIndex.error") + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, project);
                                return;
                            }
                            table.getSchema().recursiveRefresh();
                            DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, "Successfully created index!", NotificationType.INFORMATION);
                            Notifications.Bus.notify(notification, project);
                        } catch (Exception ex) {
                            Notification notification = new Notification(NOTIFICATION_GROUP_ID, NOTIFICATION_TITLE, OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
                    }
                });
                DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
            }
        });
    }

    private String getDDLStatement() {
        String ddlStatement = "";
        for (Component component : ddlMainPanel.getComponents()) {
            if (component instanceof JScrollPane) {
                for (Component c : ((JScrollPane) component).getViewport().getComponents()) {
                    if (c instanceof JTextArea) {
                        ddlStatement = ((JTextArea) c).getText().trim();
                    }

                }
            }
        }
        return ddlStatement;
    }

    private boolean validateDdl() {
        for (Component component : ddlMainPanel.getComponents()) {
            if (component instanceof JScrollPane) {
                for (Component c : ((JScrollPane) component).getViewport().getComponents()) {
                    if (c instanceof JTextArea && (((JTextArea) c).getText().trim().isEmpty())) {
                            JOptionPane.showMessageDialog(rootPanel, "Query input cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;

                    }
                }
            }
        }
        return true;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

}
