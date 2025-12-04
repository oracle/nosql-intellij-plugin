/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.JBColor;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

/**
 * context menu actions for Field group node in schema tree.
 * Drop Index action - drops index then refreshes schema tree
 *
 * @author shkose
 */
class FieldGroupNodeContextMenuActionGroup extends DefaultActionGroup {
    public FieldGroupNodeContextMenuActionGroup(FieldGroup field) {
        add(new DropIndexAction(field));
        add(new ViewIndexDdlAction((Index)field));
    }

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class DropIndexAction extends AnAction {
        private static final String DROP_INDEX = "Drop Index";
        private final FieldGroup field;

        public DropIndexAction(FieldGroup field) {
            super(DROP_INDEX);
            this.field = field;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String confirmMsg = "Are you sure you want to drop the index " + field.getName() + " ?";
            Object[] msg = {confirmMsg};

            int result = JOptionPane.showConfirmDialog(
                    null,
                    msg,
                    "DROP INDEX",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    AllIcons.General.BalloonWarning);

            if (result == JOptionPane.YES_OPTION) {
                ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Dropping Index " + field.getName(), false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(e.getProject())).getConnection();
                            try {
                                Schema schema = field.getTable().getSchema();
                                con.deleteIndex(field);
                                schema.recursiveRefresh();
                            } catch (Exception ex) {
                                Notification notification = new Notification(
                                        "Oracle NOSQL", "Oracle NoSQL Explorer",
                                        OracleNoSqlBundle
                                                .message(
                                                        "oracle.nosql.toolWindow.dropIndex.error") +
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
                DatabaseBrowserManager.getInstance(e.getProject())
                        .getToolWindowForm().getMyTreeModel().reload();
            }
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class ViewIndexDdlAction extends AnAction {
        private static final String INDEX_DDL = "View Index DDL";
        private final Index field;

        public ViewIndexDdlAction(Index field) {
            super(INDEX_DDL);
            this.field = field;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e){
            ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Fetching index ddl", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try{
                        String indexDdl = field.getCreateDDL();
                        //Frame which will display the index DDL
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frame.setSize(500, 300);
                            frame.setLayout(new BorderLayout());

                            JTextArea textArea = new JTextArea(indexDdl);
                            textArea.setWrapStyleWord(true);
                            textArea.setLineWrap(true);
                            textArea.setEditable(false);
                            JScrollPane scrollPane = new JScrollPane(textArea);

                            JPanel buttonPanel = getButtonPanel(frame, indexDdl);
                            frame.add(scrollPane, BorderLayout.CENTER);
                            frame.add(buttonPanel, BorderLayout.SOUTH);
                            frame.setVisible(true);
                            frame.setLocationRelativeTo(null);
                        });
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

        /**
         * Method which enables the "OK" and "COPY" button in the View Table DDL frame.
         */
        @NotNull
        private JPanel getButtonPanel(JFrame frame, String tableDdl) {
            JPanel buttonPanel = new JPanel();

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e1 -> frame.dispose());

            JButton copyButton = new JButton("Copy to Clipboard");
            copyButton.addActionListener(e2 -> {
                if (tableDdl != null) {
                    StringSelection stringSelection = new StringSelection(tableDdl);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
                Color originalColor = copyButton.getForeground();
                copyButton.setForeground(JBColor.green);
                Timer timer = new Timer(750, e3 -> copyButton.setForeground(originalColor));
                timer.setRepeats(false);
                timer.start();
            });
            buttonPanel.add(okButton);
            buttonPanel.add(copyButton);
            return buttonPanel;
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

}