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
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.intellij.plugin.recordView.DataBaseVirtualFile;
import oracle.nosql.intellij.plugin.toolWindow.addColumn.AddColumnGUI;
import oracle.nosql.intellij.plugin.toolWindow.addReplica.AddReplicaGUI;
import oracle.nosql.intellij.plugin.toolWindow.createChildTable.CreateChildGUI;
import oracle.nosql.intellij.plugin.toolWindow.createIndex.MainFormGUI;
import oracle.nosql.intellij.plugin.toolWindow.dropReplicas.DropReplicasGUI;
import oracle.nosql.intellij.plugin.toolWindow.editReservedCapacity.editReservedCapacityGUI;
import oracle.nosql.intellij.plugin.toolWindow.insertRow.InsertRowGUI;
import oracle.nosql.intellij.plugin.toolWindow.viewReplicas.ViewReplicaGUI;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * context menu actions for Table node in schema tree.
 * Browse action - opens record view
 * Refresh Table action - refreshes table DDL data
 * Drop Table action - drops table then refreshes schema tree
 * Create Index action - create index in the table
 * Add Column action -alters table schema to add column in a table
 * Insert Row action - inserts a row in table
 * View table DDL - fetches the current table DDL
 * Create Child Table - create a child table
 * Freeze/Unfreeze - Freezes or Unfreezes the table schema
 * View Replica - Gets the replicas of the table
 * Add Replica - help adding replica to other cloud regions
 * Drop Replica - help dropping the table replicas in other regions
 *
 * @author amsundar,kunalgup
 */
class TableNodeContextMenuActionGroup extends DefaultActionGroup {
    public TableNodeContextMenuActionGroup(Table table, Project project) {
        add(new AddColumnAction(table));
        add(new BrowseTableAction(table));
        add(new CreateChildTableAction(table));
        add(new CreateIndexAction(table));
        add(new DropTableAction(table));
        add(new InsertRowAction(table));
        add(new RefreshTableAction(table));
        add(new ViewTableDdlAction(table));

        if (cloudCheck(project)) {
            add(new editReserveCapacityAction(table));
            add(new FreezeOrUnfreezeSchemaAction(table));
            DefaultActionGroup replicaActions = new DefaultActionGroup("Regional Replicas", true);
            replicaActions.add(new ViewReplicasAction(table));
            replicaActions.add(new AddReplicaAction(table));
            replicaActions.add(new DropReplicasAction(table));
            // Adding the subgroup to the main action group
            add(replicaActions);
        }
    }

    private boolean cloudCheck(Project project) {
        ConnectionDataProviderService conServ = ConnectionDataProviderService.getInstance(project);
        if (conServ != null && conServ.getState().dict.get(ConnectionDataProviderService.KEY_PROFILE_TYPE).equals("Cloud"))
            return true;
        return false;
    }

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class BrowseTableAction extends AnAction {
        private static final String BROWSE_TABLE = "Browse Table";
        private final Table table;

        public BrowseTableAction(Table table) {
            super(BROWSE_TABLE);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            FileEditorManager mgr = FileEditorManager.getInstance(Objects.requireNonNull(e.getProject()));
            DataBaseVirtualFile vFile = new DataBaseVirtualFile(table);//,PlainTextFileType.INSTANCE, "file content");
            mgr.openFile(vFile, true);
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class RefreshTableAction extends AnAction {
        private static final String REFRESH_TABLE = "Refresh Table";
        private final Table table;

        public RefreshTableAction(Table table) {
            super(REFRESH_TABLE);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {

            ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Refreshing table " + table.getName(), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    table.recursiveRefresh();
                    DatabaseBrowserManager.getInstance(Objects.requireNonNull(e.getProject())).getToolWindowForm().getMyTreeModel().reload();
                }
            });

        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    @SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
    private static class DropTableAction extends AnAction {
        private static final String DROP_TABLE = "Drop Table";
        private final Table table;

        public DropTableAction(Table table) {
            super(DROP_TABLE);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String confirmMsg = "Are you sure you want to drop the table " + table.getName() + " ?";
            Object[] msg = {confirmMsg};

            int result = JOptionPane.showConfirmDialog(
                    null,
                    msg,
                    "DROP TABLE",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    AllIcons.General.BalloonWarning);

            if (result == JOptionPane.YES_OPTION) {
                ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Dropping table", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(e.getProject())).getConnection();
                            try {
                                Schema schema = table.getSchema();
                                con.dropTable(table);
                                schema.recursiveRefresh();
                            } catch (Exception ex) {
                                Notification notification = new Notification(
                                        "Oracle NOSQL", "Oracle NoSQL Explorer",
                                        OracleNoSqlBundle
                                                .message(
                                                        "oracle.nosql.toolWindow.dropTable.error") +
                                                ex.getMessage(),
                                        NotificationType.ERROR);
                                Notifications.Bus.notify(notification, e.getProject());
                                return;
                            }
                            DatabaseBrowserManager.getInstance(e.getProject())
                                    .getToolWindowForm().getMyTreeModel().reload();
                        } catch (Exception ex) {
                            Notification notification = new Notification(
                                    "Oracle NOSQL", "Oracle NoSQL Explorer",
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
            return true;
        }
    }

    private static class CreateIndexAction extends AnAction {
        private static final String CREATE_INDEX = "Create Index";
        private final Table table;

        public CreateIndexAction(Table table) {
            super(CREATE_INDEX);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new MainFormGUI(e.getProject(), table));
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class AddColumnAction extends AnAction {
        private static final String ADD_COLUMN = "Add Column";
        private final Table table;

        public AddColumnAction(Table table) {
            super(ADD_COLUMN);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new AddColumnGUI(e.getProject(), table));
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class InsertRowAction extends AnAction {
        private static final String INSERT_ROW = "Insert Row";
        private final Table table;

        public InsertRowAction(Table table) {
            super(INSERT_ROW);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new InsertRowGUI(e.getProject(), table));
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class ViewTableDdlAction extends AnAction {
        private static final String SHOW_TABLE_DDL = "View Table DDL";
        private final Table table;

        public ViewTableDdlAction(Table table) {
            super(SHOW_TABLE_DDL);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Fetching table DDL", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IConnection con;
                    try {
                        con = DBProject.getInstance(Objects.requireNonNull(e.getProject())).getConnection();
                        String tableDdl = con.showTableDdl(table);
                        //Frame which will display the table DDL
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frame.setSize(500, 300);
                            frame.setLayout(new BorderLayout());

                            JTextArea textArea = new JTextArea(tableDdl);
                            textArea.setWrapStyleWord(true);
                            textArea.setLineWrap(true);
                            textArea.setEditable(false);
                            JScrollPane scrollPane = new JScrollPane(textArea);

                            JPanel buttonPanel = getButtonPanel(frame, tableDdl);
                            frame.add(scrollPane, BorderLayout.CENTER);
                            frame.add(buttonPanel, BorderLayout.SOUTH);
                            frame.setVisible(true);
                            frame.setLocationRelativeTo(null);
                        });

                    } catch (Exception ex) {
                        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSQL Explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
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

    private static class CreateChildTableAction extends AnAction {
        private static final String CREATE_CHILD_TABLE = "Create Child Table";
        private final Table table;

        public CreateChildTableAction(Table table) {
            super(CREATE_CHILD_TABLE);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new CreateChildGUI(e.getProject(), table));
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class ViewReplicasAction extends AnAction {
        private static final String VIEW_REPLICAS = "View Replicas";
        private final Table table;

        public ViewReplicasAction(Table table) {
            super(VIEW_REPLICAS);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new ViewReplicaGUI(table, e.getProject()));
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class AddReplicaAction extends AnAction {
        private static final String ADD_REPLICA = "Add Replica";
        private final Table table;
        private List<String> replicasNames;

        public AddReplicaAction(Table table) {
            super(ADD_REPLICA);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching Replicas", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IConnection con;
                    try {
                        con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                        try {
                            replicasNames = con.getReplicas(table);
                        } catch (Exception ex) {
                            Notification notification = new Notification(
                                    "Oracle NOSQL", "Oracle NoSql explorer",
                                    "Error fetching table replicas : " +
                                            ex.getMessage(),
                                    NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
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
                }

                @Override
                public void onFinished() {
                    SwingUtilities.invokeLater(() -> new AddReplicaGUI(table, replicasNames, e.getProject()));
                }
            });
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class DropReplicasAction extends AnAction {
        private static final String DROP_REPLICA = "Drop Replicas";
        private final Table table;
        private List<String> replicasNames;

        public DropReplicasAction(Table table) {
            super(DROP_REPLICA);
            this.table = table;
            replicasNames = new ArrayList<>();
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching Replicas", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IConnection con;
                    try {
                        con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                        try {
                            replicasNames = con.getReplicas(table);
                        } catch (Exception ex) {
                            Notification notification = new Notification(
                                    "Oracle NOSQL", "Oracle NoSql explorer",
                                    "Error fetching table replicas : " +
                                            ex.getMessage(),
                                    NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
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
                }

                @Override
                public void onFinished() {
                    if (!replicasNames.isEmpty())
                        SwingUtilities.invokeLater(() -> new DropReplicasGUI(table, replicasNames, e.getProject()));
                    else {
                        Notification notification = new Notification(
                                "Oracle NOSQL", "Oracle NoSql explorer",
                                "No replicas found for table : " + table.getName(),
                                NotificationType.ERROR);
                        Notifications.Bus.notify(notification, project);
                    }
                }
            });

        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class FreezeOrUnfreezeSchemaAction extends AnAction {
        private static final String FREEZE_UNFREEZE = "Freeze/Unfreeze";
        private final Table table;
        private String status;
        private boolean isFrozen;

        public FreezeOrUnfreezeSchemaAction(Table table) {
            super(FREEZE_UNFREEZE);
            this.table = table;
            status = null;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching table state", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IConnection con;
                    try {
                        con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();

                        try {
                            isFrozen = con.isFreezed(table.getName());
                        } catch (Exception ex) {
                            Notification notification = new Notification(
                                    "Oracle NOSQL", "Oracle NoSql explorer",
                                    "Cannot fetch table state : " +
                                            ex.getMessage(),
                                    NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
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
                }

                @Override
                public void onFinished() {
                    if (isFrozen)
                        unfreeze();
                    else
                        freeze();
                }

                private void unfreeze() {
                    String confirmMsg = "Are you sure you want to unfreeze " + table.getName() + " schema?";
                    Object[] msg = {confirmMsg};

                    int result = JOptionPane.showConfirmDialog(
                            null,
                            msg,
                            "Unfreeze Schema",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            AllIcons.General.BalloonWarning);

                    if (result == JOptionPane.YES_OPTION) {
                        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Unfreezing schema", false) {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                IConnection con;
                                try {
                                    con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();

                                    try {
                                        boolean result = con.unfreezeSchema(table.getName());
                                        if (result)
                                            status = "pass";
                                        else
                                            status = "fail";
                                    } catch (Exception ex) {
                                        Notification notification = new Notification(
                                                "Oracle NOSQL", "Oracle NoSql explorer",
                                                "Cannot unfreeze schema : " +
                                                        ex.getMessage(),
                                                NotificationType.ERROR);
                                        Notifications.Bus.notify(notification, project);
                                    }
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
                            }

                            @Override
                            public void onFinished() {
                                if (status != null && status.equals("pass")) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            "Successfully Unfreezed schema of table : " + table.getName(),
                                            NotificationType.INFORMATION);
                                    Notifications.Bus.notify(notification, project);
                                }
                            }
                        });
                    }
                }

                private void freeze() {
                    String confirmMsg = "Are you sure you want to freeze " + table.getName() + " schema?";
                    Object[] msg = {confirmMsg};

                    int result = JOptionPane.showConfirmDialog(
                            null,
                            msg,
                            "Freeze Schema",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            AllIcons.General.BalloonWarning);

                    if (result == JOptionPane.YES_OPTION) {
                        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Freezing schema", false) {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                IConnection con;
                                try {
                                    con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();

                                    try {
                                        boolean result = con.freezeSchema(table.getName());
                                        if (result)
                                            status = "pass";
                                        else
                                            status = "fail";
                                    } catch (Exception ex) {
                                        Notification notification = new Notification(
                                                "Oracle NOSQL", "Oracle NoSql explorer",
                                                "Cannot freeze schema : " +
                                                        ex.getMessage(),
                                                NotificationType.ERROR);
                                        Notifications.Bus.notify(notification, project);
                                    }
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
                            }

                            @Override
                            public void onFinished() {
                                if (status != null && status.equals("pass")) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            "Successfully Freezed schema of table : " + table.getName(),
                                            NotificationType.INFORMATION);
                                    Notifications.Bus.notify(notification, project);
                                }
                            }
                        });
                    }
                }
            });

        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }
    private static class editReserveCapacityAction extends AnAction {
        private static final String EDIT_CAPACITY = "Edit Reserved Capacity";
        private final Table table;

        public editReserveCapacityAction(Table table) {
            super(EDIT_CAPACITY);
            this.table = table;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new editReservedCapacityGUI(e.getProject(),table));
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

}
