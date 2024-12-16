/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createChildTable;

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
import oracle.nosql.intellij.plugin.common.NoSqlIcons;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.intellij.plugin.toolWindow.createTable.ColumnGUI;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.schema.Schema;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CreateChildGUI {
    private static final String FORM_BASED = "Form based table schema entry (For Simple DDL input)";
    private static final String DDL_BASED = "Supply Table Schema as DDL Statement (For Advanced DDL input)";
    private JPanel rootPanel;
    private JComboBox<String> comboBox1;
    private JPanel subPanel;
    private JPanel comboBoxPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton createButton;
    private JButton showDDLButton;
    private boolean isJsonCollection;
    FormBasedGUI formBasedGUI;
    DDLBasedGUI ddlBasedGUI;
    private static JFrame frame;
    Map<String, String> resultSet; // contains parameters passed to create table method in CloudConnection.java

    public CreateChildGUI(Project project, Table table) {
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
            if (name.equals(FORM_BASED)) {
                // To prevent frame size from increasing
                formBasedGUI.getRootPanel().setVisible(true);
                ddlBasedGUI.getRootPanel().setVisible(false);
                showDDLButton.setVisible(true);
            } else {
                formBasedGUI.getRootPanel().setVisible(false);
                ddlBasedGUI.getRootPanel().setVisible(true);
                showDDLButton.setVisible(false);
            }
        });

        // Initialize FormBasedGUI and DDLBasedGUI
        formBasedGUI = new FormBasedGUI(table);
        ddlBasedGUI = new DDLBasedGUI();
        createButton.addActionListener(e -> {
            String comboItem = (String) comboBox1.getSelectedItem();

            assert comboItem != null;
            if (validate(comboItem)) {

                if (comboItem.equals(FORM_BASED)) {
                    resultSet = formDDLString(table);
                } else if (comboItem.equals(DDL_BASED)) {
                    resultSet = new HashMap<>();
                    resultSet.put("query", getDDLStatement());
                    resultSet.put("TableName", "sample_table");
                }
                frame.dispose();
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Creating child table", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                            try {
                                con.createChildTable(resultSet.get("TableName"), resultSet.get("query"));
                            } catch (Exception ex) {
                                Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.createTable.error") + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, project);
                                return;
                            }
                            DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                        } catch (Exception ex) {
                            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
                    }
                });
                DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
            }
        });

        closeButton.addActionListener(e -> frame.dispose());

        showDDLButton.addActionListener(e -> {
            if (validate((String) Objects.requireNonNull(comboBox1.getSelectedItem()))) {
                JOptionPane.showMessageDialog(null, formDDLString(table).get("query"), "DDL Statement", JOptionPane.INFORMATION_MESSAGE, NoSqlIcons.ORACLE_LOGO);
            }
        });

        // Creates a frame
        if(isJsonCollection){
            JOptionPane.showMessageDialog(
                    null,                    // Parent component (null for no parent)
                    "Creating Child Table option is not supported by Json Collection Tables.", // The message to display
                    "Warning",               // Title of the dialog
                    JOptionPane.WARNING_MESSAGE // Message type (WARNING_MESSAGE for warnings)
            );
        }
        else {
            createFrame();
        }

    }

    private void createFrame() {
        frame = new JFrame("Create Table");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this.createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public JComponent createPanel() {
        String[] createTableModes = {FORM_BASED, DDL_BASED};
        comboBox1.setModel(new DefaultComboBoxModel<>(createTableModes));
        for (String createTableMode : createTableModes) {
            subPanel.add(createTableMode, getCreateTableModeSpecificUI(createTableMode));
        }
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
        });
        rootPanel.setPreferredSize(new Dimension(900, 500));
        return rootPanel;
    }

    public JPanel getCreateTableModeSpecificUI(String uiType) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraintsPkPanel = new GridBagConstraints();
        GridBagConstraints gridBagConstraintsColumnPanel = new GridBagConstraints();
        if (uiType.equals(DDL_BASED)) {
            panel.add(ddlBasedGUI.getRootPanel());
        } else if (uiType.equals(FORM_BASED)) {
            // Initially one Primary Key Column field is added
            PrimaryKeyColumnGUI primaryKeyColumnGUI1 = new PrimaryKeyColumnGUI();
            gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
            formBasedGUI.getMainPkPanel().add(primaryKeyColumnGUI1.getPkPanel(), gridBagConstraintsPkPanel);
            primaryKeyColumnGUI1.setRemoveButton(formBasedGUI);
            primaryKeyColumnGUI1.disableRemoveButton();
            formBasedGUI.increasePkPanelCount();

            // Action Listener for Add primary key column button
            formBasedGUI.getAddPrimaryKeyColumnButton().addActionListener(e -> {
                // Set Remove Button enabled for more than one primary key
                if (formBasedGUI.getPkPanelCount() == 1) {
                    for (Component c : formBasedGUI.getMainPkPanel().getComponents()) {
                        if (c instanceof JPanel) {
                            for (Component c1 : ((JPanel) c).getComponents()) {
                                if (c1 instanceof JButton) {
                                    c1.setEnabled(true);
                                }
                            }

                        }
                    }
                }

                PrimaryKeyColumnGUI primaryKeyColumnGUI = new PrimaryKeyColumnGUI();
                JPanel panel1 = (JPanel) primaryKeyColumnGUI.getPkPanel();
                primaryKeyColumnGUI.setRemoveButton(formBasedGUI);
                gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
                formBasedGUI.getMainPkPanel().add(panel1, gridBagConstraintsPkPanel);
                formBasedGUI.increasePkPanelCount();
                formBasedGUI.getMainPkPanel().updateUI();
            });

            // Initially one Column Field is added
            ColumnGUI columnGUI = new ColumnGUI();
            gridBagConstraintsColumnPanel.gridy = gridBagConstraintsColumnPanel.gridy + 10;
            formBasedGUI.getMainColKeyPanel().add(columnGUI.getColumnKeyPanel(), gridBagConstraintsColumnPanel);
            columnGUI.setRemoveButton((JPanel) formBasedGUI.getMainColKeyPanel());

            // Action Listener for Add Column Button
            formBasedGUI.getAddColumnButton().addActionListener(e -> {
                ColumnGUI columnGUI1 = new ColumnGUI();
                columnGUI1.setRemoveButton((JPanel) formBasedGUI.getMainColKeyPanel());
                gridBagConstraintsColumnPanel.gridy = gridBagConstraintsColumnPanel.gridy + 10;
                formBasedGUI.getMainColKeyPanel().add(columnGUI1.getColumnKeyPanel(), gridBagConstraintsColumnPanel);
                formBasedGUI.getMainColKeyPanel().updateUI();
            });

            panel.add(formBasedGUI.getRootPanel());
        }

        return panel;
    }

    public String getDDLStatement() {
        String ddlStatement = "";

        for (Component component : ddlBasedGUI.getDdlMainPanel().getComponents()) {
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

    public Map<String, String> formDDLString(Table table) {
        HashMap<String, String> createTableParametersMap = new HashMap<>();
        ArrayList<String> fieldDefinitions = new ArrayList<>();
        StringBuilder primaryKeyString = new StringBuilder("Primary Key( ");
        ArrayList<String> restPrimaryKeys = new ArrayList<>();
        String primaryKeyColName = "";
        String tableName = "";
        int ttl = 0;
        String ttlUnit = "";
        for (Component c : formBasedGUI.getTableNamePanel().getComponents()) {
            if (c instanceof JTextField) {
                if (c.getName().equals("TableName-TextField")) {
                    tableName = table.getName() + "." + ((JTextField) c).getText().trim();
                    createTableParametersMap.put("TableName", tableName);
                }
            }
        }
        for (Component component : formBasedGUI.getMainPkPanel().getComponents()) {

            if (component instanceof JPanel) {
                StringBuilder fieldDefinition = new StringBuilder(new StringBuilder());
                for (Component c : ((JPanel) component).getComponents()) {
                    if (c instanceof JTextField) {
                        if (c.getName().equals("PKColumnName")) {
                            String text = ((JTextField) c).getText().trim();
                            fieldDefinition.append(text).append(" ");
                            primaryKeyColName = text;
                        }
                    }
                    if (c instanceof JComboBox) {
                        String item = (String) ((JComboBox<?>) c).getSelectedItem();
                        assert item != null;
                        if (item.equals("Timestamp")) {
                            for (Component component1 : ((JPanel) component).getComponents()) {
                                if (component1 instanceof JTextField) {
                                    if (component1.isVisible() && component1.getName().equals("PKPrecision")) {
                                        fieldDefinition.append("Timestamp(").append(((JTextField) component1).getText().trim()).append(") ");
                                    }
                                }
                            }
                        } else {
                            fieldDefinition.append(item).append(" ");
                        }

                    }

                }

                restPrimaryKeys.add(primaryKeyColName);
                fieldDefinitions.add(fieldDefinition.toString());
            }

        }

        if (primaryKeyString.toString().endsWith(")")) {
            primaryKeyString.append(", ");
        }

        for (String key : restPrimaryKeys) {
            primaryKeyString.append(key).append(", ");
        }
        primaryKeyString = new StringBuilder(primaryKeyString.substring(0, primaryKeyString.length() - 2));
        primaryKeyString.append(" )");

        for (Component component : formBasedGUI.getMainColKeyPanel().getComponents()) {

            if (component instanceof JPanel) {
                StringBuilder fieldDefinition = new StringBuilder();
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
                                            fieldDefinition.append("DEFAULT \"").append(defVal).append("\" ");
                                        } else {
                                            fieldDefinition.append("DEFAULT ").append(defVal).append(" ");
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
                                        fieldDefinition.append("Timestamp(").append(((JTextField) component1).getText().trim()).append(") ");
                                    }
                                }
                            }
                        } else if (item.equals("Binary")) {
                            for (Component component1 : ((JPanel) component).getComponents()) {
                                if (component1.isVisible() && component1 instanceof JTextField) {
                                    if (component1.getName().equals("Size-TextField")) {
                                        String size = ((JTextField) component1).getText().trim();
                                        if (size.isEmpty()) {
                                            fieldDefinition.append("Binary");
                                        } else
                                            fieldDefinition.append("Binary(").append(((JTextField) component1).getText().trim()).append(")");
                                    }
                                }
                            }
                        } else {
                            fieldDefinition.append(item).append(" ");
                        }
                    }
                    if (c instanceof JCheckBox) {
                        boolean item = ((JCheckBox) c).isSelected();
                        if (item) {
                            fieldDefinition.append("NOT NULL ");
                        }
                    }
                }
                fieldDefinitions.add(fieldDefinition.toString());
            }
        }

        for (Component c : formBasedGUI.getTtlPanel().getComponents()) {
            if (c instanceof JTextField) {
                if (c.getName().equals("TTL-TextField")) {
                    String timeToLive = ((JTextField) c).getText().trim();
                    if (!timeToLive.isEmpty()) {
                        ttl = Integer.parseInt(timeToLive);
                    }
                }
            }
            if (c instanceof JRadioButton) {
                if (c.getName().equals("Days-RadioButton")) {
                    if (((JRadioButton) c).isSelected()) {
                        ttlUnit = "days";
                    }
                } else if (c.getName().equals("Hours-RadioButton")) {
                    if (((JRadioButton) c).isSelected()) {
                        ttlUnit = "hours";
                    }
                }
            }
        }

        StringBuilder ddlStatement = new StringBuilder("CREATE TABLE ").append(tableName).append(" (\n");
        for (String fieldDefinition : fieldDefinitions) {
            ddlStatement.append(fieldDefinition).append(", ").append('\n');
        }
        ddlStatement.append(primaryKeyString).append(" )");
        if (ttl > 0) {
            ddlStatement.append(" USING TTL ").append(ttl).append(" ").append(ttlUnit);
        }
        createTableParametersMap.put("query", ddlStatement.toString());
        return createTableParametersMap;
    }

    public boolean validate(@NotNull String uiType) {
        if (uiType.equals(FORM_BASED)) {
            for (Component c : formBasedGUI.getTableNamePanel().getComponents()) {
                if (c instanceof JTextField) {
                    if (c.getName().equals("TableName-TextField")) {
                        if (((JTextField) c).getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Table Name cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        } else if (((JTextField) c).getText().trim().length() > 256) {
                            JOptionPane.showMessageDialog(null, "Table Name length should be less than 256 characters", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        } else if (!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$", ((JTextField) c).getText())) {
                            JOptionPane.showMessageDialog(null, "Table Name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        }
                    }
                }
            }
            for (Component component : formBasedGUI.getMainPkPanel().getComponents()) {

                if (component instanceof JPanel) {
                    for (Component c : ((JPanel) component).getComponents()) {
                        if (c instanceof JTextField) {
                            if (c.getName().equals("PKColumnName")) {
                                if (((JTextField) c).getText().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(frame, "Primary Key Column Name cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                } else if (((JTextField) c).getText().trim().length() > 64) {
                                    JOptionPane.showMessageDialog(null, "Primary Key Column Name length should be less than 64 characters", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                } else if (!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$", ((JTextField) c).getText())) {
                                    JOptionPane.showMessageDialog(null, "Primary Key Name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }
                            if (c.getName().equals("PKPrecision") && c.isVisible()) {
                                if (((JTextField) c).getText().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Precision Value cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                } else if (!Pattern.matches("^[0-9]$", ((JTextField) c).getText())) {
                                    JOptionPane.showMessageDialog(null, "Precision Value should be an integer between 0-9", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }

                        }
                    }
                }

            }

            for (Component component : formBasedGUI.getMainColKeyPanel().getComponents()) {

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

            for (Component c : formBasedGUI.getTtlPanel().getComponents()) {
                if (c instanceof JTextField) {
                    if (c.getName().equals("TTL-TextField")) {
                        String ttlValue = ((JTextField) c).getText().trim();
                        if (!ttlValue.isEmpty() && !Pattern.matches("^[0-9]*$", ttlValue)) {
                            JOptionPane.showMessageDialog(null, "TTL Value should be an integer", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                        }
                    }
                }
            }


        } else if (uiType.equals(DDL_BASED)) {

            for (Component component : ddlBasedGUI.getDdlMainPanel().getComponents()) {
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
