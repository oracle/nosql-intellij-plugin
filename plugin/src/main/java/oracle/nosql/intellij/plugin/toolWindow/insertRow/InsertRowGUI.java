/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.insertRow;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.JsonUtils;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Field.Type;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

public class InsertRowGUI {
    private JPanel rootPanel;
    private JPanel comboBoxPanel;
    private JPanel buttonPanel;
    private JPanel subPanel;
    private JButton insertRowButton;
    private JButton closeButton;
    private JComboBox<String> comboBox1;
    private boolean isJsonCollection;
    private static JFrame frame;
    FormInsertGUI formInsertGUI;
    DDLInsertGUI ddlInsertGUI;

    public InsertRowGUI(Project project, Table table) {
        assert comboBox1 != null;
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
            if (name.equals("Form-based row fields entry (For Simple DDL input)")) {

                formInsertGUI.getMainPkPanel().setVisible(true);
                ddlInsertGUI.getDdlMainPanel().setVisible(false);

            } else {
                formInsertGUI.getMainPkPanel().setVisible(false);
                ddlInsertGUI.getDdlMainPanel().setVisible(true);
            }
        });


        IConnection connection;
        try {
            connection = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
            try {
                String schema = connection.showSchema(table);
                JSONObject schemaJson = new JSONObject(schema);
                isJsonCollection = schemaJson.has("jsonCollection");
                formInsertGUI = new FormInsertGUI(table, schema);
            } catch (Exception ex) {
                Notification notification = new Notification(
                        "Oracle NOSQL", "Oracle NoSql explorer",
                        OracleNoSqlBundle
                                .message(
                                        "oracle.nosql.toolWindow.updateRow.error") +
                                ex.getMessage(),
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification, project);
                return;
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
        //     formInsertGUI = new FormInsertGUI(table);
        ddlInsertGUI = new DDLInsertGUI();

        insertRowButton.addActionListener(e -> {
            String comboItem = (String) comboBox1.getSelectedItem();
            assert comboItem != null;
            if (validate(comboItem, table)) {
                if (comboItem.equals("Form-based row fields entry (For Simple DDL input)")) {
                    frame.dispose();
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Inserting Row", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {
                                    Schema schema = table.getSchema();
                                    String schemaJson = con.showSchema(table);
                                    String st = formQueryInsert(table, schemaJson);
                                    con.insertFromJson(table, st, false);
                                    schema.recursiveRefresh();
                                } catch (Exception ex) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            OracleNoSqlBundle
                                                    .message(
                                                            "oracle.nosql.toolWindow.insertRow.error") +
                                                    ex.getMessage(),
                                            NotificationType.ERROR);
                                    Notifications.Bus.notify(notification, project);
                                    return;
                                }
                                table.recursiveRefresh();
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
                } else if (comboItem.equals("Supply row contents as a JSON object (For Advanced DDL input)")) {

                    frame.dispose();
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Inserting Row", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {
                                    Schema schema = table.getSchema();
                                    String ddlStatement = formddlRows();
                                    con.insertFromJson(table, ddlStatement, false);
                                    schema.recursiveRefresh();
                                } catch (Exception ex) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            OracleNoSqlBundle
                                                    .message(
                                                            "oracle.nosql.toolWindow.insertRow.error") +
                                                    ex.getMessage(),
                                            NotificationType.ERROR);
                                    Notifications.Bus.notify(notification, project);
                                    return;
                                }
                                table.recursiveRefresh();
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
                }
            }
        });

        closeButton.addActionListener(e -> frame.dispose());

        if(isJsonCollection)
            JOptionPane.showMessageDialog(
                    null,
                    "Simple Input is disabled for JSON Collection Tables. Please use Advanced JSON Input option to insert the data.", // The message to display
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        createFrame();

    }

    private void createFrame() {
        frame = new JFrame("Insert Row");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //  frame.setPreferredSize(new Dimension(700,260));
        frame.setContentPane(this.createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public JComponent createPanel() {
        String[] createTableModes = {"Form-based row fields entry (For Simple DDL input)", "Supply row contents as a JSON object (For Advanced DDL input)"};
        comboBox1.setModel(new DefaultComboBoxModel<>(createTableModes) {
            @Override
            public void setSelectedItem(Object item) { // sets the default model to Advanced Input for jc tables
                if (isJsonCollection) {
                    if(!"Form-based row fields entry (For Simple DDL input)".equals(item))
                        super.setSelectedItem(item);
                }
                else{
                    super.setSelectedItem(item);
                }
            }
        });
        for (String createTableMode : createTableModes) {
            subPanel.add(createTableMode, getCreateTableModeSpecificUI(createTableMode));
        }

        if (isJsonCollection) {
            comboBox1.setSelectedItem("Supply row contents as a JSON object (For Advanced DDL input)");
        }

        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
        });
        rootPanel.setPreferredSize(new Dimension(700, 260));

        // custom renderer to visually disable simple DDL input option
        if(isJsonCollection) {
            comboBox1.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if ("Form-based row fields entry (For Simple DDL input)".equals(value)) {
                        comp.setForeground(Color.GRAY);
                        comp.setEnabled(false);
                    }
                    return comp;
                }
            });
        }
        return rootPanel;
    }

    public JPanel getCreateTableModeSpecificUI(String uiType) {
        JPanel panel = new JPanel(new GridBagLayout());

        if (uiType.equals("Supply row contents as a JSON object (For Advanced DDL input)")) {
            panel.add(ddlInsertGUI.getDdlMainPanel());
        } else if (uiType.equals("Form-based row fields entry (For Simple DDL input)")) {
            if(!isJsonCollection)
                panel.add(formInsertGUI.getMainPkPanel());
        }

        return panel;
    }

    public String formQueryInsert(Table table, String schem) {
        StringBuilder fieldDefinition = new StringBuilder();
        MapValue jsonMap = JsonUtils.createValueFromJson(schem, null).asMap();

        fieldDefinition.append("{");
        int j = -1;
        for (Component component : formInsertGUI.getMainPkPanel().getComponents()) {
            if (component instanceof JPanel) {
                j++;
                for (Component c : ((JPanel) component).getComponents()) {
                    if (c instanceof JLabel) {
                        if (c.getName() != null && c.getName().equals("orLabel"))
                            continue;
                        boolean flag1 = false;
                        boolean flag2 = false;
                        boolean flag3 = false;
                        String text = ((JLabel) c).getText().trim();
                        String text1 = "\"" + text + "\"";
                        for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                            if ("fields".equals(entry.getKey())) {
                                ArrayValue arr = entry.getValue().asArray();
                                for (FieldValue entr : arr) {
                                    MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entr), null).asMap();
                                    for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                        if ("name".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals(text1)) {
                                            flag1 = true;
                                            for (Entry<String, FieldValue> en : jsonMp.entrySet()) {
                                                if ("as uuid".equals(en.getKey()) && String.valueOf(en.getValue()).equals("true")) {
                                                    for (Entry<String, FieldValue> e : jsonMp.entrySet()) {
                                                        if ("generated".equals(e.getKey()) && String.valueOf(e.getValue()).equals("true")) {
                                                            flag2 = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (flag1)
                                        break;
                                }
                            } else if ("identity".equals(entry.getKey())) {
                                MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entry.getValue()), null).asMap();
                                for (Entry<String, FieldValue> entr : jsonMp.entrySet()) {
                                    if ("name".equals(entr.getKey()) && String.valueOf(entr.getValue()).equals(text1)) {
                                        for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                            if ("always".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals("true")) {
                                                //    c.setEnabled(false);
                                                flag3 = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!flag2 && !flag3) {
                            fieldDefinition.append("\"").append(text).append("\":");
                        }
                    }
                    if (c instanceof JTextField) {
                        boolean flag1 = false;
                        boolean flag2 = false;
                        boolean flag3 = false;
                        String text = ((JTextField) c).getText().trim();
                        String text1 = "\"" + table.getFieldAt(j).getName() + "\"";
                        if (!text.isEmpty()) {
                            if (table.getFieldAt(j).getType().equals(Type.STRING) || table.getFieldAt(j).getType().equals(Type.TIMESTAMP)
                            || table.getFieldAt(j).getType().equals(Type.BINARY) || table.getFieldAt(j).getType().equals(Type.FIXED_BINARY)) {
                                fieldDefinition.append("\"").append(text).append("\"");
                            }  else {
                                for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                                    if ("identity".equals(entry.getKey())) {
                                        MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entry.getValue()), null).asMap();
                                        for (Entry<String, FieldValue> entr : jsonMp.entrySet()) {
                                            if ("name".equals(entr.getKey()) && String.valueOf(entr.getValue()).equals(text1)) {
                                                for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                                    if ("always".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals("true")) {
                                                        //    c.setEnabled(false);
                                                        flag3 = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!flag3) {
                                    fieldDefinition.append(text);
                                }
                            }
                        } else {
                            for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                                if ("fields".equals(entry.getKey())) {
                                    ArrayValue arr = entry.getValue().asArray();
                                    for (FieldValue entr : arr) {
                                        MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entr), null).asMap();
                                        for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                            if ("name".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals(text1)) {
                                                flag1 = true;
                                                for (Entry<String, FieldValue> en : jsonMp.entrySet()) {
                                                    if ("as uuid".equals(en.getKey()) && String.valueOf(en.getValue()).equals("true")) {
                                                        for (Entry<String, FieldValue> e : jsonMp.entrySet()) {
                                                            if ("generated".equals(e.getKey()) && String.valueOf(e.getValue()).equals("true")) {
                                                                flag2 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (flag1)
                                            break;
                                    }
                                } else if ("identity".equals(entry.getKey())) {
                                    MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entry.getValue()), null).asMap();
                                    for (Entry<String, FieldValue> entr : jsonMp.entrySet()) {
                                        if ("name".equals(entr.getKey()) && String.valueOf(entr.getValue()).equals(text1)) {
                                            for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                                if ("always".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals("true")) {
                                                    // c.setEnabled(false);
                                                    formInsertGUI.setSchemaJSON(true);
                                                    flag3 = true;
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            if (!flag2 && !flag3) {
                                fieldDefinition.append("null");
                            }
                        }
                        if (!flag2 && !flag3)
                            fieldDefinition.append(", ");
                    }
                }
            }
        }
        String str = fieldDefinition.toString().trim();
        StringBuilder st = new StringBuilder(str.substring(0, str.length() - 1));
        return st.append("}").toString().trim();
    }

    public String formddlRows() {
        String ddlStatement = "";
        for (Component component : ddlInsertGUI.getDdlMainPanel().getComponents()) {
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

    public boolean validate(@NotNull String uiType, Table table) {
        if (uiType.equals("Form-based row fields entry (For Simple DDL input)")) {
            int j = -1;
            for (Component component : formInsertGUI.getMainPkPanel().getComponents()) {
                if (component instanceof JPanel) {
                    j++;
                    for (Component c : ((JPanel) component).getComponents()) {
                        if (c instanceof JTextField) {
                            if (((JTextField) c).getText().trim().isEmpty()) {
                                if (c.getName().equals("NullableKeyTextField")) {
                                    JOptionPane.showMessageDialog(frame, "Not Null Column field: " + table.getFieldAt(j).getName() + " cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            } else {
                                switch (table.getFieldAt(j).getType()) {
                                    case INTEGER:
                                        if (!Pattern.matches("^[0-9]*$", ((JTextField) c).getText().trim())) {
                                            JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Integer", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                    case DOUBLE:
                                    case FLOAT:
                                    case LONG:
                                    case NUMBER:
                                        try {
                                            Double.parseDouble(((JTextField) c).getText().trim());
                                        } catch (NumberFormatException ex) {
                                            JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Number", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                        break;
                                    case BINARY:
                                    case FIXED_BINARY:
                                        String text = ((JTextField) c).getText().trim();
                                        String regex="^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
                                        if(!Pattern.matches(regex,text)) {
                                            JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Base64 String", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                        break;
                                    case BOOLEAN:
                                        if (!(((JTextField) c).getText().trim().equalsIgnoreCase("true") || ((JTextField) c).getText().trim().equalsIgnoreCase("false"))) {
                                            JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Boolean", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        } else if (uiType.equals("Supply row contents as a JSON object (For Advanced DDL input)")) {
            for (Component component : ddlInsertGUI.getDdlMainPanel().getComponents()) {
                if (component instanceof JScrollPane) {
                    for (Component c : ((JScrollPane) component).getViewport().getComponents()) {
                        if (c instanceof JTextArea) {
                            if (((JTextArea) c).getText().trim().isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "Json input cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
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

