/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
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
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import static oracle.nosql.intellij.plugin.toolWindow.createIndex.MainFormGUI.frame;

public class FormBasedGUI {
    private static final String NOTIFICATION_GROUP_ID = "Oracle NOSQL";
    private static final String NOTIFICATION_TITLE = "Oracle NoSql explorer";
    private JPanel createIndexPanel;
    private JTextField indexNameTextfield;
    private JButton addIndexButton;
    private JPanel subPanel;
    private String indexName;
    FormIndexGUI formIndexGUI;
    ArrayList<String> ar;
    AddIndexColumnGUI addIndexColumnGUI;

    public FormBasedGUI(Project project, Table table) {

        formIndexGUI = new FormIndexGUI();
        addIndexColumnGUI = new AddIndexColumnGUI(table);
        addIndexButton.setForeground(JBColor.GREEN);
        ar = new ArrayList<>();

        GridBagConstraints gridBagConstraintsPkPanel = new GridBagConstraints();
        // Initially one Primary Key Column field is added
        AddIndexColumnGUI primaryKeyColumnGUI1 = new AddIndexColumnGUI(table);
        gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
        formIndexGUI.getMainPkPanel().add(primaryKeyColumnGUI1.getRootPanel(), gridBagConstraintsPkPanel);
        primaryKeyColumnGUI1.setRemoveButton(formIndexGUI);
        primaryKeyColumnGUI1.disableRemoveButton();
        formIndexGUI.increasePkPanelCount();

        formIndexGUI.getAddColumnButton().addActionListener(e -> {
            // Set Remove Button enabled for more than one primary key
            if (formIndexGUI.getPkPanelCount() == 1) {
                for (Component c : formIndexGUI.getMainPkPanel().getComponents()) {
                    if (c instanceof JPanel) {
                        for (Component c1 : ((JPanel) c).getComponents()) {
                            if (c1 instanceof JButton) {
                                c1.setEnabled(true);
                            }
                        }
                    }
                }
            }

            AddIndexColumnGUI primaryKeyColumnGUI = new AddIndexColumnGUI(table);
            JPanel panel1 = (JPanel) primaryKeyColumnGUI.getRootPanel();
            primaryKeyColumnGUI.setRemoveButton(formIndexGUI);
            gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
            formIndexGUI.getMainPkPanel().add(panel1, gridBagConstraintsPkPanel);
            formIndexGUI.increasePkPanelCount();
            formIndexGUI.getMainPkPanel().updateUI();
        });
        subPanel.add(formIndexGUI.getRootPanel());
        addIndexButton.addActionListener(e -> {
            if (validate() && formString()) {
                frame.dispose();
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Creating index", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                            try {
                                String[] str = new String[ar.size()];
                                for (int i = 0; i < ar.size(); i++) {
                                    str[i] = ar.get(i);
                                }

                                indexName = indexNameTextfield.getText().trim();
                                Schema schema = table.getSchema();
                                con.createIndex(table, indexName, str);
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

    public boolean formString() {
        for (Component component : formIndexGUI.getMainPkPanel().getComponents()) {
            if (component instanceof JPanel) {
                String colName = "";
                String jsonPathToIndex = "";
                String typeOfJsonIndex = "";
                for (Component c : ((JPanel) component).getComponents()) {
                    if (c instanceof JComboBox) {
                        if (c.getName().equals("PKColumnName")) {
                            colName = Objects.requireNonNull(((JComboBox<?>) c).getSelectedItem()).toString();
                        } else if (c.isVisible() && c.getName().equals("Type")) {
                            typeOfJsonIndex = Objects.requireNonNull(((JComboBox<?>) c).getSelectedItem()).toString();
                        }
                    } else if (c.isVisible() && c instanceof JTextField) {
                        jsonPathToIndex = ((JTextField) c).getText();
                        if (jsonPathToIndex.isEmpty()) {
                            JOptionPane.showMessageDialog(createIndexPanel, "Json index path field cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        }
                    }
                }
                if (addIndexColumnGUI.getJsonColumnSet().contains(colName)) {
                    String result = colName + "." + jsonPathToIndex + " as " + typeOfJsonIndex;
                    ar.add(result);
                } else ar.add(colName);
            }
        }
        return true;
    }

    public boolean validate() {
        if (indexNameTextfield.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(createIndexPanel, "Index name field cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
            return false;
        } else if (indexNameTextfield.getText().trim().length() > 64) {
            JOptionPane.showMessageDialog(createIndexPanel, "Index name length should be less than 64 characters", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
            return false;
        } else if (!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$", indexNameTextfield.getText())) {
            JOptionPane.showMessageDialog(createIndexPanel, "Index name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
            return false;
        }

        return true;
    }

    public JPanel getRootPanel() {
        return createIndexPanel;
    }

}
