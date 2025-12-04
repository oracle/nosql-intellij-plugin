/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.addColumn;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.regex.Pattern;

public class AddColumnGUI {
    private JPanel rootPanel;
    private JPanel buttonPanel;
    private JPanel comboBoxPanel;
    private JPanel subPanel;
    private JComboBox<String> comboBox1;
    private JButton addColumnButton;
    private JButton closeButton;
    private static JFrame frame;
    private boolean isJsonCollection;
    FormColumnGUI formColumnGUI;
    DDLColumnGUI ddlColumnGUI;

    public AddColumnGUI(Project project, Table table) {
        IConnection con;
        try{
            con = DBProject.getInstance(project).getConnection();
            String result = con.showSchema(table);
            JSONObject jsonObject = new JSONObject(result);
            isJsonCollection = jsonObject.has("jsonCollection");
        }
        catch (Exception ex) {
            throw new RuntimeException("Unable to get Schema : " + ex.getMessage());
        }

        assert comboBox1 != null;
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
            if (name.equals("Form-based column entry (For Simple DDL input)")) {
                formColumnGUI.getRootPanel().setVisible(true);
                ddlColumnGUI.getDdlMainPanel().setVisible(false);
            } else {
                formColumnGUI.getRootPanel().setVisible(false);
                ddlColumnGUI.getDdlMainPanel().setVisible(true);
            }
        });

        formColumnGUI = new FormColumnGUI();
        ddlColumnGUI = new DDLColumnGUI();

        addColumnButton.addActionListener(e -> {
            String comboItem = (String) comboBox1.getSelectedItem();
            assert comboItem != null;
            if (validate(comboItem)) {

                if (comboItem.equals("Form-based column entry (For Simple DDL input)")) {
                    frame.dispose();
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Adding Column(s)", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {
                                    Schema schema = table.getSchema();
                                    String str = flattenedColumn();
                                    con.addNewColumn(table, str);
                                    schema.recursiveRefresh();
                                } catch (Exception ex) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            OracleNoSqlBundle
                                                    .message(
                                                            "oracle.nosql.toolWindow.addColumn.error") +
                                                    ex.getMessage(),
                                            NotificationType.ERROR);
                                    Notifications.Bus.notify(notification, project);
                                    return;
                                }
                                table.recursiveRefresh();
                                DatabaseBrowserManager.getInstance(Objects.requireNonNull(project)).getToolWindowForm().refresh();
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
                    });
                    DatabaseBrowserManager.getInstance(Objects.requireNonNull(project)).getToolWindowForm().refresh();
                } else if (comboItem.equals("Supply new columns in a DDL statement (For Advanced DDL input)")) {

                    frame.dispose();
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Adding Column(s)", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {

                                    Schema schema = table.getSchema();
                                    String ddlStatement = formddlColumn();
                                    con.ddlQuery(ddlStatement);
                                    schema.recursiveRefresh();
                                } catch (Exception ex) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            OracleNoSqlBundle
                                                    .message(
                                                            "oracle.nosql.toolWindow.addColumn.error") +
                                                    ex.getMessage(),
                                            NotificationType.ERROR);
                                    Notifications.Bus.notify(notification, project);
                                    return;
                                }
                                table.recursiveRefresh();
                                DatabaseBrowserManager.getInstance(project)
                                        .getToolWindowForm().refresh();
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
                    });
                    DatabaseBrowserManager.getInstance(project)
                            .getToolWindowForm().refresh();
                }
            }
        });

        closeButton.addActionListener(e -> frame.dispose());

        if(isJsonCollection){
            JOptionPane.showMessageDialog(
                    null,                    // Parent component (null for no parent)
                    "Add Column option is not supported by Json Collection Tables.", // The message to display
                    "Warning",               // Title of the dialog
                    JOptionPane.WARNING_MESSAGE // Message type (WARNING_MESSAGE for warnings)
            );
        }
        else {
            createFrame();
        }
    }

    public String flattenedColumn() {
        StringBuilder fieldDefinition = new StringBuilder();
        for (Component component : formColumnGUI.getMainPkPanel().getComponents()) {
            if (component instanceof JPanel) {
                for (Component c : ((JPanel) component).getComponents()) {
                    if (c instanceof JTextField) {
                        if (c.getName().equals("ColName-TextField")) {
                            String text = ((JTextField) c).getText().trim();
                            fieldDefinition.append(text).append(" ");
                        }
                        if (c.getName().equals("DefaultValue-TextField")) {
                            String defVal = ((JTextField) c).getText().trim();
                            if (!defVal.isEmpty()) {
                                for (Component component1 : ((JPanel) component).getComponents()) {
                                    if (component1 instanceof JComboBox) {
                                        if (((JComboBox<?>) component1).getSelectedItem() == "String" || ((JComboBox<?>) component1).getSelectedItem() == "Timestamp") {
                                            fieldDefinition.append(" DEFAULT \"").append(defVal).append("\" ");
                                        } else {
                                            fieldDefinition.append(" DEFAULT ").append(defVal).append(" ");
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (c instanceof JComboBox) {
                        String item = (String) ((JComboBox<?>) c).getSelectedItem();
                        assert item != null;
                        if (item.equals("Timestamp")) {
                            for (Component component1 : ((JPanel) component).getComponents()) {
                                if (component1.isVisible() && component1 instanceof JTextField) {
                                    if (component1.getName().equals("Precision-TextField")) {
                                        fieldDefinition.append("Timestamp(").append(((JTextField) component1).getText().trim()).append(")");
                                    }
                                }
                            }
                        }
                        else if (item.equals("Binary")) {
                            for (Component component1 : ((JPanel) component).getComponents()) {
                                if (component1.isVisible() && component1 instanceof JTextField) {
                                    if (component1.getName().equals("Size-TextField")) {
                                        String size = ((JTextField) component1).getText().trim();
                                        if(size.isEmpty()){
                                            fieldDefinition.append("Binary");
                                        }
                                        else
                                            fieldDefinition.append("Binary(").append(((JTextField) component1).getText().trim()).append(")");
                                    }
                                }
                            }
                        } else {
                            fieldDefinition.append(item);
                        }

                    }
                    if (c instanceof JCheckBox) {
                        boolean item = ((JCheckBox) c).isSelected();
                        if (item) {
                            fieldDefinition.append("NOT NULL ");
                        }
                    }
                }
                fieldDefinition.append(", ADD ");
            }
        }
        String str = fieldDefinition.toString().trim();
        return str.substring(0, str.length() - 5);
    }

    public String formddlColumn() {
        String ddlStatement = "";
        for (Component component : ddlColumnGUI.getDdlMainPanel().getComponents()) {
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

    private void createFrame() {
        frame = new JFrame("Add Column");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this.createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JComponent createPanel() {
        String[] createTableModes = {"Form-based column entry (For Simple DDL input)", "Supply new columns in a DDL statement (For Advanced DDL input)"};
        comboBox1.setModel(new DefaultComboBoxModel<>(createTableModes));
        for (String createTableMode : createTableModes) {
            subPanel.add(createTableMode, getCreateTableModeSpecificUI(createTableMode));
        }
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
        });
        rootPanel.setPreferredSize(new Dimension(800, 260));
        return rootPanel;
    }

    public JPanel getCreateTableModeSpecificUI(String uiType) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraintsPkPanel = new GridBagConstraints();

        if (uiType.equals("Supply new columns in a DDL statement (For Advanced DDL input)")) {
            panel.add(ddlColumnGUI.getDdlMainPanel());
        } else if (uiType.equals("Form-based column entry (For Simple DDL input)")) {
            // Initially one Primary Key Column field is added
            ColumnKeyGUI primaryKeyColumnGUI1 = new ColumnKeyGUI();
            gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
            formColumnGUI.getMainPkPanel().add(primaryKeyColumnGUI1.getRootPanel(), gridBagConstraintsPkPanel);
            primaryKeyColumnGUI1.setRemoveButton(formColumnGUI);
            primaryKeyColumnGUI1.disableRemoveButton();
            formColumnGUI.increasePkPanelCount();

            // Action Listener for Add primary key column button
            formColumnGUI.getAddColumnButton().addActionListener(e -> {
                // Set Remove Button enabled for more than one primary key
                if (formColumnGUI.getPkPanelCount() == 1) {
                    for (Component c : formColumnGUI.getMainPkPanel().getComponents()) {
                        if (c instanceof JPanel) {
                            for (Component c1 : ((JPanel) c).getComponents()) {
                                if (c1 instanceof JButton) {
                                    c1.setEnabled(true);
                                }
                            }
                        }
                    }
                }
                ColumnKeyGUI primaryKeyColumnGUI = new ColumnKeyGUI();
                JPanel panel1 = (JPanel) primaryKeyColumnGUI.getRootPanel();
                primaryKeyColumnGUI.setRemoveButton(formColumnGUI);
                gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
                formColumnGUI.getMainPkPanel().add(panel1, gridBagConstraintsPkPanel);
                formColumnGUI.increasePkPanelCount();
                formColumnGUI.getMainPkPanel().updateUI();
            });
            panel.add(formColumnGUI.getRootPanel());
        }
        return panel;
    }

    public boolean validate(@NotNull String uiType) {
        if (uiType.equals("Form-based column entry (For Simple DDL input)")) {
            for (Component component : formColumnGUI.getMainPkPanel().getComponents()) {
                if (component instanceof JPanel) {
                    for (Component c : ((JPanel) component).getComponents()) {
                        if (c instanceof JTextField) {
                            if (c.getName().equals("ColName-TextField")) {
                                if (((JTextField) c).getText().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(frame, "Column Name cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                } else if (((JTextField) c).getText().trim().length() > 64) {
                                    JOptionPane.showMessageDialog(frame, "Column Name length should be less than 64 characters", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                } else if (!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$", ((JTextField) c).getText())) {
                                    JOptionPane.showMessageDialog(frame, "Column Name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }
                            if (c.getName().equals("Precision-TextField") && c.isVisible()) {
                                if (((JTextField) c).getText().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Precision Value cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                } else if (!Pattern.matches("^[0-9]$", ((JTextField) c).getText())) {
                                    JOptionPane.showMessageDialog(null, "Precision Value should be an integer between 0-9", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }
                            if (c.getName().equals("DefaultValue-TextField")) {
                                for (Component component1 : ((JPanel) component).getComponents()) {
                                    if (component1 instanceof JCheckBox) {
                                        if (((JCheckBox) component1).isSelected() && ((JTextField) c).getText().trim().isEmpty()) {
                                            JOptionPane.showMessageDialog(frame, "Default Value is required if Not Null is set", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (uiType.equals("Supply new columns in a DDL statement (For Advanced DDL input)")) {
            for (Component component : ddlColumnGUI.getDdlMainPanel().getComponents()) {
                if (component instanceof JScrollPane) {
                    for (Component c : ((JScrollPane) component).getViewport().getComponents()) {
                        if (c instanceof JTextArea) {
                            if (((JTextArea) c).getText().trim().isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "Query input cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

}
