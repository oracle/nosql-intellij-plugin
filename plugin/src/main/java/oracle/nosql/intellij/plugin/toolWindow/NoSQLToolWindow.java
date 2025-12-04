/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.SchemaBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;
import java.util.Objects;

/**
 * Toolwindow for browsing schema.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class NoSQLToolWindow extends SimpleToolWindowPanel {
    private static final String NON_LINKED_CARD_NAME = "NON_LINKED";
    private static final String CONTENT_CARD_NAME = "CONTENT";

    private final CardLayout myLayout = new CardLayout();
    private final JPanel myContent = new JPanel(myLayout);

    private final Project project;
    private final String myPlace;

    private DatabaseBrowserTree browserTree;
    private BrowserTreeModel myTreeModel;

    public NoSQLToolWindow(Project project) {
        super(true, true);
        this.project = project;
        this.myPlace = ActionPlaces.MAIN_TOOLBAR;
        setContent(myContent);
        initContent();
        refresh();
    }

    private void initContent() {
        //add toolbar
        setToolbar(createToolbarPanel());

        //add Jpanel for Tree
        final JComponent payloadControl = new JPanel(new GridBagLayout());

        myTreeModel = new BrowserTreeModel(null);
        browserTree = new DatabaseBrowserTree(myTreeModel, project);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = constraints.weighty = 1;

        payloadControl.add(browserTree, constraints);
        payloadControl.setBackground(browserTree.getBackground());
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(payloadControl);
        myContent.add(scrollPane, CONTENT_CARD_NAME);

        final JComponent errorControl = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.error"), SwingConstants.CENTER);
        errorControl.add(label, constraints);
        myContent.add(errorControl, NON_LINKED_CARD_NAME);

    }

    @SuppressWarnings("unused")
    public JComponent buildContent() {
        JPanel result = new JPanel(new GridBagLayout());
        Datamodel store = getDataModel();
        if (store == null) return null;

        myTreeModel = new BrowserTreeModel(store);
        browserTree = new DatabaseBrowserTree(myTreeModel, project);
        //myTreeModel.rebuild();

        return result;
    }

    private Datamodel getDataModel() {
        Datamodel store;
        IConnection con;
        try {
            if (Objects.requireNonNull(MultipleConnectionsDataProviderService.getInstance(project).getState()).dict.isEmpty()) {
                String error = "No added connections!";
                Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", error, NotificationType.INFORMATION);
                Notifications.Bus.notify(notification, project);
                return null;
            }
            con = DBProject.getInstance(project).getConnection();
        } catch (Exception ex) {
            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
            return null;
        }
        try {
            String conString = con.getConnectionString();
            SchemaBuilder builder = con.getSchemaBuilder();
            String prefKey = "/" + DBProject.getInstance(project).getConnectionProfile().getType().getName() + "/" + "TENANT_ID";
            String schemaName = ConnectionDataProviderService.getInstance(project).getValue(prefKey);
            if (schemaName == null) schemaName = "CloudTenant";
            ConnectionDataProviderService.State state = ConnectionDataProviderService.getInstance(project).getState();
            schemaName = getNewSchemaName(state);
            store = builder.build(conString, schemaName);
        } catch (Exception ex) {
            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSQL Explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.schema.get.error") + ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
            return null;
        }
        return store;
    }

    private String getNewSchemaName(ConnectionDataProviderService.State state) {
        Map<String, ConnectionDataProviderService.State> dict = Objects.requireNonNull(MultipleConnectionsDataProviderService.getInstance(project).getState()).dict;
        for (Map.Entry<String, ConnectionDataProviderService.State> e : dict.entrySet()) {
            String uid = e.getKey();
            if (state.dict.equals(e.getValue().dict)) {
                Map<String, String> nameToUidMap = MultipleConnectionsDataProviderService.getInstance(project).getNameToUidMap();
                for (Map.Entry<String, String> e1 : nameToUidMap.entrySet()) {
                    if (uid.equals(e1.getValue())) {
                        String name = e1.getKey();
                        return name + " : " + uid;
                    }
                }
            }
        }
        return "null";
    }

    private JPanel createToolbarPanel() {
        final ActionManager actionManager = ActionManager.getInstance();
        final ActionGroup actionGroup = (ActionGroup) actionManager.getAction("oracle.nosql.ActionGroup.Browser.Controls");
        ActionToolbar actionToolbar = actionManager.createActionToolbar(myPlace, actionGroup, true);

        JPanel toolbarControl = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        toolbarControl.add(actionToolbar.getComponent(), constraints);
        actionToolbar.setTargetComponent(toolbarControl);
        return toolbarControl;
    }

    public void refresh() {
        // Clear the tree immediately (must be on EDT)
        ApplicationManager.getApplication().invokeLater(() -> {
            myTreeModel.setRoot(null);
        });
        ProgressManager.getInstance().run(new Task.Backgroundable(project,
            "Refreshing NOSQL Schema", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // Background work only: fetch the model
                Datamodel store = getDataModel();
                // Switch back to EDT for *all* UI updates
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (store == null) {
                        myLayout.show(myContent, NON_LINKED_CARD_NAME);
                    } else {
                        myTreeModel.setRoot(store);
                        myLayout.show(myContent, CONTENT_CARD_NAME);
                    }
                    // Ensure UI refresh
                    myContent.revalidate();
                    myContent.repaint();
                });
            }
        });
    }


    public BrowserTreeModel getMyTreeModel() {
        return myTreeModel;
    }
}
