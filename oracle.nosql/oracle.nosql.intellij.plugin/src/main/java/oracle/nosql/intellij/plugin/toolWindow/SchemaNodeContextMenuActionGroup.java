/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.toolWindow.createTable.MainFormGUI;
import oracle.nosql.intellij.plugin.toolWindow.executeDDL.executeDdlGUI;
import oracle.nosql.model.schema.Schema;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.util.Objects;

/**
 * context menu actions for schema node in schema tree
 * Refresh Schema action - refreshes schema tree
 * Create Table action - opens Create Table dialog
 * Change Endpoint/Compartment - opens dialog to change endpoint and compartment of Cloud Connection
 */
public class SchemaNodeContextMenuActionGroup extends DefaultActionGroup {
    public SchemaNodeContextMenuActionGroup(Project project, Schema schema) {
        add(new RefreshSchemaAction(schema));
        add(new CreateTableAction());
        String prefKeyForProfileType = "/profile_type";

        // Get the latest profile type.
        String profileType = ConnectionDataProviderService.getInstance(Objects.requireNonNull(project)).getValue(prefKeyForProfileType);

        // This action is only for cloud connection.
        if (profileType != null && profileType.equals("Onprem")) {
            add(new ExecuteDdlAction());
        }
    }

    private static class RefreshSchemaAction extends AnAction {
        private static final String REFRESH_SCHEMA = "Refresh Schema";
        private final Schema schema;

        public RefreshSchemaAction(Schema schema) {
            super(REFRESH_SCHEMA);
            this.schema = schema;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Refreshing schema " + schema.getName(), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    schema.recursiveRefresh();
                    DatabaseBrowserManager.getInstance(Objects.requireNonNull(e.getProject())).getToolWindowForm().getMyTreeModel().reload();
                }
            });
        }

        @Override
        public boolean isDumbAware() {
            return true;
        }
    }

    private static class CreateTableAction extends AnAction {
        private static final String CREATE_TABLE = "Create Table";

        public CreateTableAction() {
            super(CREATE_TABLE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new MainFormGUI(e.getProject()));
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }

    private static class ExecuteDdlAction extends AnAction {
        private static final String EXECUTE_DDL = "Execute DDL";

        public ExecuteDdlAction() {
            super(EXECUTE_DDL);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SwingUtilities.invokeLater(() -> new executeDdlGUI(e.getProject()));
        }

        @Override
        public boolean isDumbAware() {
            return false;
        }
    }
}
