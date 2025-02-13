/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.intellij.plugin.recordView.DataBaseVirtualFile;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;


import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.intellij.plugin.recordView.DataBaseVirtualFile;
import oracle.nosql.intellij.plugin.toolWindow.createTable.MainFormGUI;
//import oracle.nosql.intellij.plugin.toolWindow.addColumn.addColumnGUI;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * context menu actions for Field node in schema tree.
 *  Drop Column action - drops column then refreshes schema tree
 *
 *  @author shkose
 */
class FieldNodeContextMenuActionGroup extends DefaultActionGroup {
    public FieldNodeContextMenuActionGroup(Field field) {
        add(new DropColumnAction(field));
    }

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class DropColumnAction extends AnAction {
        private static final String DROP_COLUMN = "Drop Column";
        private final Field field;
        public DropColumnAction(Field field) {
            super(DROP_COLUMN);
            this.field = field;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(!field.isPrimaryKey() && !field.isIndexKey()){
            String confirmMsg = "Are you sure you want to drop the column " + field.getName() + " ?";
            Object[] msg = {confirmMsg};

            int result = JOptionPane.showConfirmDialog(
                    null,
                    msg,
                    "DROP COLUMN",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    AllIcons.General.BalloonWarning);

            if (result == JOptionPane.YES_OPTION) {
                ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Dropping Column " + field.getName(), false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(e.getProject())).getConnection();
                            try {
                                Schema schema = field.getTable().getSchema();
                                con.deleteField(field);
                                schema.recursiveRefresh();
                            } catch (Exception ex) {
                                Notification notification = new Notification(
                                        "Oracle NOSQL", "Oracle NoSQL Explorer",
                                        OracleNoSqlBundle
                                                .message(
                                                        "oracle.nosql.toolWindow.dropColumn.error") +
                                                ex.getMessage(),
                                        NotificationType.ERROR);
                                Notifications.Bus.notify(notification, e.getProject());
                                return;
                            }
                            DatabaseBrowserManager.getInstance(e.getProject())
                                    .getToolWindowForm().getMyTreeModel().reload();
                        } catch (Exception ex) {
                            Notification notification = new Notification(
                                    "Oracle NOSQL", "Oracle NoSql explorer",
                                    OracleNoSqlBundle
                                            .message(
                                                    "oracle.nosql.toolWindow.connection.get.error") +
                                            ex.getMessage(),
                                    NotificationType.ERROR);
                            Notifications.Bus.notify(notification, e.getProject());
                        }
                    }
                });
            }
        }
            else if (field.isPrimaryKey()){
                JOptionPane.showMessageDialog(null, "Primary key Column: "+field.getName()+" cannot be deleted.", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
            }
            else {
                JOptionPane.showMessageDialog(null, "Index key field: "+field.getName()+" cannot be deleted.", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
            }
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

}