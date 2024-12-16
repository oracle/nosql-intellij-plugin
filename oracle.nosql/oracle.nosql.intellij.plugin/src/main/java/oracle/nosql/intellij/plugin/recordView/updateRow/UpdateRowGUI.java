/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView.updateRow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
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
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Field.Type;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public class UpdateRowGUI {
    private JPanel rootPanel;
    private JPanel comboBoxPanel;
    private JPanel buttonPanel;
    private JPanel subPanel;
    private JButton updateRowButton;
    private JButton closeButton;
    private JComboBox<String> comboBox1;
    private static JFrame frame;
    private boolean isJsonCollection;
    FormUpdateGUI formUpdateGUI;
    DDLUpdateGUI ddlUpdateGUI;
    private Map<String, Set<String>> mrCounters;
    private String primaryKeys;
    private Set<String> jsonFields;
    private static Map<String,String>mrCounterType;
    private static boolean mrCounterUpdateError;
    private static Project project;
    private String schema = null;
    private String jString;

    public UpdateRowGUI(Project project, Table table, String jString, String primaryKeys) {
        assert comboBox1 != null;
        this.primaryKeys = primaryKeys;
        mrCounters = new HashMap<>();
        mrCounterType = new HashMap<>();
        jsonFields = new HashSet<>();
        this.project = project;
        this.jString = jString;
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
            if (name.equals("Form-based row fields entry (For Simple DDL input)")) {
                formUpdateGUI.getMainPkPanel().setVisible(true);
                ddlUpdateGUI.getDdlMainPanel().setVisible(false);
            } else {
                formUpdateGUI.getMainPkPanel().setVisible(false);
                ddlUpdateGUI.getDdlMainPanel().setVisible(true);
            }
        });

        IConnection connection;
        try {
            connection = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
            schema = connection.showSchema(table);
            JSONObject schemaJson = new JSONObject(schema);
            isJsonCollection = schemaJson.has("jsonCollection");
            
            // Extract MR Counters from the schema 
            JSONArray columns = (JSONArray) schemaJson.get("fields");
            if(schemaJson.has("mrcounters")){
                JSONObject mrCounterFields = (JSONObject) schemaJson.get("mrcounters");
                for(String columnName : mrCounterFields.keySet()) {
                    mrCounters.put(columnName, new HashSet<>(Arrays.asList(columnName)));
                    mrCounterType.put(columnName, (String) mrCounterFields.get(columnName));
                }
            }

            for(int i = 0; i < columns.length(); i++){
                JSONObject columnMetaData = columns.getJSONObject(i);
                String columnName = (String) columnMetaData.get("name");
                if(columnMetaData.has("mrcounter")){
                    mrCounters.put(columnName,new HashSet<>(Arrays.asList(columnName)));
                    mrCounterType.put(columnName,(String) columnMetaData.get("type"));
                }
                else if(columnMetaData.has("mrcounters")){
                    JSONObject mrCounterFields = (JSONObject) columnMetaData.get("mrcounters");
                    Set<String> list = new HashSet<>();
                    for(String keys : mrCounterFields.keySet()){
                        String path = columnName + "." + keys;
                        list.add(path);
                        mrCounterType.put(path, (String) mrCounterFields.get(keys));
                    }
                    mrCounters.put(columnName,list);
                }
                if(columnMetaData.get("type").equals("JSON"))
                    jsonFields.add((String) columnMetaData.get("name"));
            }

            formUpdateGUI = new FormUpdateGUI(table, jString, schema);
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

        ddlUpdateGUI = new DDLUpdateGUI(jString);

        updateRowButton.addActionListener(e -> {
            mrCounterUpdateError = false;
            String comboItem = (String) comboBox1.getSelectedItem();
            assert comboItem != null;
            if (validate(comboItem, table)) {
                if (comboItem.equals("Form-based row fields entry (For Simple DDL input)")) {
                    frame.dispose();
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating Row", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {
                                    Schema schema = table.getSchema();
                                    String schemaJson = con.showSchema(table);
                                    MapValue jsonMap = JsonUtils.createValueFromJson(schemaJson, null).asMap();
                                    boolean flag3 = false;
                                    for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                                        if ("identity".equals(entry.getKey())) {
                                            flag3 = true;
                                            break;
                                        }
                                    }
                                    if (flag3) {
                                        String st = formUpdateQuery(table, schemaJson);
                                        con.dmlQuery(st);
                                    } else {
                                        String test = formInsertQuery(table, schemaJson);
                                        if(!mrCounterUpdateError)
                                            con.dmlQuery(test);
                                    }
                                    schema.recursiveRefresh();
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
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating Row", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {

                                    Schema schema = table.getSchema();
                                    String schemaJson = con.showSchema(table);

                                    MapValue jsonMap = JsonUtils.createValueFromJson(schemaJson, null).asMap();
                                    boolean flag3 = false;
                                    for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                                        if ("identity".equals(entry.getKey())) {
                                            flag3 = true;
                                            break;
                                        }
                                    }
                                    if (flag3) {
                                        String st = jsonDDLUpdate(table, schemaJson, jString);
                                        if (!st.equals("generate_new_row"))
                                            con.dmlQuery(st);
                                        else
                                            con.insertFromJson(table, "{}", true);
                                    } else {
                                        String ddljString = formDDLUpdate();
                                        String st = formDDLUpdate(ddljString, table);
                                        if(!mrCounterUpdateError)
                                            con.dmlQuery(st);
                                    }
                                    schema.recursiveRefresh();
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

        createFrame();
    }

    private void createFrame() {
        frame = new JFrame("Update Row");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this.createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JComponent createPanel() {
        String[] createTableModes = {"Form-based row fields entry (For Simple DDL input)", "Supply row contents as a JSON object (For Advanced DDL input)"};
        comboBox1.setModel(new DefaultComboBoxModel<>(createTableModes){  // sets the default model to Advanced Input for jc tables
            @Override
            public void setSelectedItem(Object item) {
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

        if (isJsonCollection) {   //
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
            panel.add(ddlUpdateGUI.getDdlMainPanel());
        } else if (uiType.equals("Form-based row fields entry (For Simple DDL input)")) {
            panel.add(formUpdateGUI.getMainPkPanel());
        }

        return panel;
    }

    // Iterates the json field to extract the mr counter and then append the set statement for incrementing/decrementing the MR Counter
    private void iterateJson(String val, String columnName, String parent, StringBuilder setStatement){
        Gson gson = new Gson();
        JsonObject value = gson.fromJson(val, JsonObject.class);

        for (String keys : (value.keySet())) {
            Object keyValue = value.get(keys);
            if (keyValue instanceof JsonObject) {
                iterateJson(keyValue.toString(), columnName, parent + "." + keys, setStatement);
            } else if (!(keyValue instanceof JsonArray)) {
                String currentPath = parent + ".";
                if (mrCounters.get(columnName).contains(currentPath+keys)) {
                    setMrCounter(currentPath, setStatement, keyValue.toString(), keys);
                }
            }
        }
    }

    // validates and append the set statement for incrementing/decrementing the MR Counter
    private static void setMrCounter(String currentPath, StringBuilder setStatement, String value, String columnName){
        if(validateMRCounter(columnName, value, currentPath + columnName))
            setStatement.append("t.").append(currentPath).
                    append(columnName).append(" = t.").append(currentPath).
                    append(value.substring(1,value.length()-1)).
                    append(" , ");
        else
            mrCounterUpdateError = true;
    }

    public String formUpdateQuery(Table table, String schem) {
        StringBuilder fieldDefinition = new StringBuilder();
        fieldDefinition.append("UPDATE ").append(table.getName()).append(" ");
        StringBuilder setStatement = new StringBuilder();
        StringBuilder whereStatement = new StringBuilder();
        whereStatement.append("WHERE ");
        MapValue jsonMap = JsonUtils.createValueFromJson(schem, null).asMap();
        int j = -1;
        for (Component component : formUpdateGUI.getMainPkPanel().getComponents()) {
            if (component instanceof JPanel) {
                j++;
                boolean flag1 = false;
                boolean flag3 = false;
                for (Component c : ((JPanel) component).getComponents()) {
                    if (c instanceof JLabel) {
                        if(c.getName()!=null && c.getName().equals("orLabel"))
                            continue;
                        String text = ((JLabel) c).getText().trim();
                        String text1 = "\"" + text + "\"";
                        for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                            if ("primaryKey".equals(entry.getKey())) {
                                ArrayValue arr = entry.getValue().asArray();
                                for (FieldValue entr : arr) {
                                    if (String.valueOf(entr).equals(text1)) {
                                        whereStatement.append(text).append(" = ");
                                        flag1 = true;
                                        break;
                                    }
                                }
                            } else if ("identity".equals(entry.getKey())) {
                                MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entry.getValue()), null).asMap();
                                for (Entry<String, FieldValue> entr : jsonMp.entrySet()) {
                                    if ("name".equals(entr.getKey()) && String.valueOf(entr.getValue()).equals(text1)) {
                                        for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                            if ("always".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals("true")) {
                                                flag3 = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!flag1 && !flag3) {
                            setStatement.append("SET ").append(text).append(" = ");
                        }
                    }
                    if (c instanceof JTextField) {
                        String text = ((JTextField) c).getText().trim();
                        if (flag1) {
                            if (table.getFieldAt(j).getType().equals(Type.STRING) || table.getFieldAt(j).getType().equals(Type.TIMESTAMP)
                            || table.getFieldAt(j).getType().equals(Type.BINARY) || table.getFieldAt(j).getType().equals(Type.FIXED_BINARY)) {
                                whereStatement.append("\"").append(text).append("\"").append(" AND ");
                            } else
                                whereStatement.append(text).append(" AND ");
                        } else if (!flag3) {
                            if (!text.isEmpty()) {
                                if (table.getFieldAt(j).getType().equals(Type.STRING) || table.getFieldAt(j).getType().equals(Type.TIMESTAMP)
                                || table.getFieldAt(j).getType().equals(Type.BINARY) || table.getFieldAt(j).getType().equals(Type.FIXED_BINARY)) {
                                    setStatement.append("\"").append(text).append("\"").append(" , ");
                                }else
                                    setStatement.append(text).append(" , ");
                            } else {
                                setStatement.append("null").append(" , ");
                            }
                        }
                    }
                }
            }
        }
        String setStr = setStatement.toString();
        String whereStr = whereStatement.toString();
        fieldDefinition.append(setStr, 0, setStr.length() - 2).append(whereStr, 0, whereStr.length() - 5);
        return fieldDefinition.toString().trim();
    }

    public String formInsertQuery(Table table, String schem){
        StringBuilder fieldDefinition = new StringBuilder();
        fieldDefinition.append("UPDATE ").append(table.getName()).append(" AS t ");
        StringBuilder setStatement = new StringBuilder("SET ");
        StringBuilder setMRStatement = new StringBuilder("SET ");
        StringBuilder whereStatement = new StringBuilder();
        String columnName = null;
        whereStatement.append("WHERE ");
        MapValue jsonMap = JsonUtils.createValueFromJson(schem, null).asMap();
        int j = -1;
        for (Component component : formUpdateGUI.getMainPkPanel().getComponents()) {
            if (component instanceof JPanel) {
                j++;
                boolean flag1 = false;
                for (Component c : ((JPanel) component).getComponents()) {
                    if (c instanceof JLabel) {
                        if(c.getName()!=null && c.getName().equals("orLabel"))
                            continue;
                        String text = ((JLabel) c).getText().trim();
                        columnName = text;
                        String text1 = "\"" + text + "\"";
                        for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                            if ("primaryKey".equals(entry.getKey())) {
                                ArrayValue arr = entry.getValue().asArray();
                                for (FieldValue entr : arr) {
                                    if (String.valueOf(entr).equals(text1)) {
                                        whereStatement.append(text).append(" = ");
                                        flag1 = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!flag1 && !mrCounters.containsKey(text)) {
                            setStatement.append(text).append(" = ");
                        }

                    }
                    if (c instanceof JTextField) {
                        String text = ((JTextField) c).getText().trim();
                        String text1 = "\"" + text + "\"";
                        if (flag1) {
                            if (table.getFieldAt(j).getType().equals(Type.STRING) ||
                                    table.getFieldAt(j).getType().equals(Type.TIMESTAMP) ||
                                    table.getFieldAt(j).getType().equals(Type.BINARY) ||
                                    table.getFieldAt(j).getType().equals(Type.FIXED_BINARY)) {
                                whereStatement.append("\"").append(text).append("\"").append(" AND ");
                            } else
                                whereStatement.append(text).append(" AND ");
                        } else{
                            if (!text.isEmpty()) {
                                if (table.getFieldAt(j).getType().equals(Type.STRING) ||
                                        table.getFieldAt(j).getType().equals(Type.TIMESTAMP) ||
                                        table.getFieldAt(j).getType().equals(Type.BINARY) ||
                                        table.getFieldAt(j).getType().equals(Type.FIXED_BINARY)) {
                                    setStatement.append("\"").append(text).append("\"").append(" , ");
                                }else {
                                    if(mrCounters.containsKey(columnName)) {
                                        if (mrCounters.get(columnName).contains(columnName)) {
                                            setMrCounter("", setMRStatement, text1, columnName);
                                        }
                                        else {
                                            iterateJson(text, columnName, columnName, setMRStatement);
                                            setStatement.append(columnName).append(" = ");
                                            setStatement.append(text).append(" , ");
                                        }
                                    }
                                    else{
                                        setStatement.append(text).append(" , ");
                                    }
                                }
                            } else {
                                setStatement.append("null").append(" , ");
                            }
                        }
                    }
                }
            }
        }

        String setStr = setStatement.toString();
        String setMRStr = setMRStatement.toString();
        String whereStr = whereStatement.toString();

        if(!setMRStr.equals("SET ")) {
            if(!setStr.equals("SET "))
                fieldDefinition.append(setStr, 0, setStr.length());
            fieldDefinition.append(setMRStr, 0, setMRStr.length() - 2);
        }
        else if(!setStr.equals("SET ")){
            fieldDefinition.append(setStr, 0, setStr.length() - 2);
        }

        fieldDefinition.append(whereStr, 0, whereStr.length() - 5);
        return fieldDefinition.toString().trim();
    }


    public String formDDLUpdate(String ddljStatement, Table table){
        StringBuilder fieldDefinition = new StringBuilder();
        fieldDefinition.append("UPDATE ").append(table.getName()).append(" AS t ");
        StringBuilder setStatement = new StringBuilder("SET ");
        StringBuilder setMRStatement = new StringBuilder("SET ");
        StringBuilder whereStatement = new StringBuilder();
        whereStatement.append("WHERE ");

        Gson gson = new Gson();
        JsonObject ddlJson = gson.fromJson(ddljStatement, JsonObject.class);
        JsonObject primaryKeysJson = gson.fromJson(primaryKeys, JsonObject.class);
        Set<String> primaryKeysSet = new HashSet<>();

        for(String key : primaryKeysJson.keySet()){
            whereStatement.append(key).
                    append(" = ").
                    append(primaryKeysJson.get(key)).
                    append(" AND " );
            primaryKeysSet.add(key);
        }

        for(String columnName : ddlJson.keySet()){
            if(!primaryKeysSet.contains(columnName)) {
                Object columnValue = ddlJson.get(columnName);
                if (mrCounters.containsKey(columnName)) {
                    if (mrCounters.get(columnName).contains(columnName)) {
                        setMrCounter("", setMRStatement, columnValue.toString(), columnName);
                    } else {
                        iterateJson(ddlJson.get(columnName).toString(), columnName, columnName, setMRStatement);
                        setStatement.append(columnName).append(" = ").append(columnValue).append(" , ");
                    }
                }
                else {
                        setStatement.append(columnName).append(" = ").append(columnValue).append(" , ");
                }
            }
        }

        String setStr = setStatement.toString();
        String setMRStr = setMRStatement.toString();
        String whereStr = whereStatement.toString();

        removeFieldsQuery(fieldDefinition, ddljStatement);

        if(!setMRStr.equals("SET "))
            fieldDefinition.append(setMRStr);
        if(!setStr.equals("SET "))
            fieldDefinition.append(setStr);
        if(fieldDefinition.charAt(fieldDefinition.length()-2) == ',')
            fieldDefinition.deleteCharAt(fieldDefinition.length()-2);

        fieldDefinition.append(whereStr, 0, whereStr.length() - 5);
        return fieldDefinition.toString().trim();
    }

    private void removeFieldsQuery(StringBuilder fieldDefinition, String ddljStatement){
        StringBuilder removeStatement = new StringBuilder("REMOVE ");

        Gson gson = new Gson();
        JsonObject ddlJson = gson.fromJson(ddljStatement, JsonObject.class);
        JsonObject currentJsonRow = gson.fromJson(jString, JsonObject.class);

        for(String column : currentJsonRow.keySet()){
            boolean check = false;
            for(String newColumns : ddlJson.keySet()){
                if (column.equals(newColumns)) {
                    check = true;
                    break;
                }
            }
            if(!check){
                removeStatement.append(column).append(" , ");
            }
        }

        String setRemoveStr = removeStatement.toString();

        if(!setRemoveStr.equals("REMOVE "))
            fieldDefinition.append(setRemoveStr);
    }

    public String jsonDDLUpdate(Table table, String schem, String jString) {
        StringBuilder fieldDefinition = new StringBuilder();
        fieldDefinition.append("UPDATE ").append(table.getName()).append(" ");
        StringBuilder setStatement = new StringBuilder();
        StringBuilder whereStatement = new StringBuilder();
        whereStatement.append("WHERE ");
        MapValue jsonMap = JsonUtils.createValueFromJson(schem, null).asMap();
        MapValue jsonMapDDL = JsonUtils.createValueFromJson(jString, null).asMap();
        for (Entry<String, FieldValue> en : jsonMapDDL.entrySet()) {
            String text = en.getKey();
            String text1 = "\"" + text + "\"";
            boolean flag1 = false;
            boolean flag3 = false;
            for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                if ("primaryKey".equals(entry.getKey())) {
                    ArrayValue arr = entry.getValue().asArray();
                    for (FieldValue entr : arr) {
                        if (String.valueOf(entr).equals(text1)) {
                            whereStatement.append(text).append(" = ");
                            flag1 = true;
                            whereStatement.append(en.getValue()).append(" AND ");
                            break;
                        }
                    }
                } else if ("identity".equals(entry.getKey())) {
                    MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entry.getValue()), null).asMap();
                    for (Entry<String, FieldValue> entr : jsonMp.entrySet()) {
                        if ("name".equals(entr.getKey()) && String.valueOf(entr.getValue()).equals(text1)) {
                            for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                if ("always".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals("true")) {
                                    flag3 = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!flag1 && !flag3) {
                String ddlStatement = "";
                String val = "";
                for (Component component : ddlUpdateGUI.getDdlMainPanel().getComponents()) {
                    if (component instanceof JScrollPane) {
                        for (Component c : ((JScrollPane) component).getViewport().getComponents()) {
                            if (c instanceof JTextArea) {
                                ddlStatement = ((JTextArea) c).getText().trim();
                                val = ddlStatement.substring(1, ddlStatement.length() - 1);
                            }
                        }
                    }
                }
                if (!val.trim().equals("")) {
                    MapValue jsonM = JsonUtils.createValueFromJson(ddlStatement, null).asMap();
                    for (Entry<String, FieldValue> e : jsonM.entrySet()) {
                        if (text.equals(e.getKey())) {
                            if (!e.getValue().isEMPTY()) {
                                setStatement.append("SET ").append(text).append(" = ").append(e.getValue()).append(" , ");
                            } else {
                                setStatement.append("SET ").append(text).append(" = ").append("null").append(" , ");
                            }
                            break;
                        }
                    }
                } else {
                    return "generate_new_row";
                }
            }
        }
        String setStr = setStatement.toString();
        String whereStr = whereStatement.toString();
        fieldDefinition.append(setStr, 0, setStr.length() - 2).append(whereStr, 0, whereStr.length() - 5);
        return fieldDefinition.toString().trim();
    }

    public String formDDLUpdate() {
        String ddlStatement = "";
        for (Component component : ddlUpdateGUI.getDdlMainPanel().getComponents()) {
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
            for (Component component : formUpdateGUI.getMainPkPanel().getComponents()) {
                if (component instanceof JPanel) {
                    j++;
                    String columnName = null;
                    for (Component c : ((JPanel) component).getComponents()) {
                        if (c instanceof JLabel) {
                            columnName = ((JLabel) c).getText().trim();
                        }
                        if (c instanceof JTextField) {
                            if (((JTextField) c).getText().trim().isEmpty()) {
                                if (c.getName().equals("NullableKeyTextField")) {
                                    JOptionPane.showMessageDialog(frame, "Not Null Column field: " + table.getFieldAt(j).getName() + " cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            } else {
                                switch (table.getFieldAt(j).getType()) {
                                    case INTEGER:
                                        if (!(mrCounters.containsKey(columnName))&&
                                                !Pattern.matches("^[+-]?[0-9]*$", ((JTextField) c).getText().trim())) {
                                            JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Integer", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                    case DOUBLE:
                                    case FLOAT:
                                    case LONG:
                                    case NUMBER:
                                        if(!mrCounters.containsKey(columnName)) { // Do not validate as it is in the form of string in case of mr counters while updating
                                            try {
                                                Double.parseDouble(((JTextField) c).getText().trim());
                                            } catch (NumberFormatException ex) {
                                                JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Number", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                                return false;
                                            }
                                        }
                                        break;
                                    case BINARY:
                                    case FIXED_BINARY:
                                        String text = ((JTextField) c).getText().trim();
                                        String regex="^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
                                        if(!Pattern.matches(regex,text))
                                            JOptionPane.showMessageDialog(null, table.getFieldAt(j).getName() + " must be a value of type: Base64 String", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
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
            for (Component component : ddlUpdateGUI.getDdlMainPanel().getComponents()) {
                if (component instanceof JScrollPane) {
                    for (Component c : ((JScrollPane) component).getViewport().getComponents()) {
                        if (c instanceof JTextArea) {
                            String text = ((JTextArea) c).getText().trim();
                            if (text.isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "Json input cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else{  // checks if the primary key is not changed while updating
                                Gson gson = new Gson();
                                JsonObject primKeys = gson.fromJson(primaryKeys, JsonObject.class);
                                JsonObject rowJson = gson.fromJson(text, JsonObject.class);
                                boolean pkError = false;

                                for(String rowData : rowJson.keySet()){
                                    for(String key : primKeys.keySet()){
                                        if(rowData.equals(key)){
                                            if(!rowJson.get(rowData).equals((primKeys.get(key))))
                                                pkError = true;
                                        }
                                    }
                                }

                                if(pkError){
                                    JOptionPane.showMessageDialog(null, "Primary keys must not be changed!", "Error", JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }

                        }
                    }
                }
            }
        }
        return true;
    }

    // Function to validate the mr counter increment while updating. Format - ( <mrCounter> +/- <value> )
    private static boolean validateMRCounter(String columnName, String val, String currentPath){
        if(val.length() < 3){  // 3 as one for variable name, second for +/- sign and other for numerical value
            setMRCounterError(currentPath);
            return false;
        }

        String value = val.substring(1,val.length()-1).replaceAll("\\s", "");
        if(value.length() < columnName.length() + 2) { //  +2 as one character for +/- sign and other for numerical value
            setMRCounterError(currentPath);
            return false;
        }

        String extractedColumnName = value.substring(0,columnName.length());
        String extractedNumericalStr = value.substring(columnName.length() + 1);
        boolean validateNumericaldata = value.charAt(columnName.length()) == '+' || value.charAt(columnName.length()) == '-';
        String counterType = mrCounterType.get(currentPath);

        if(counterType.equals("INTEGER")){
            if(!Pattern.matches("^[+-]?[0-9.]*$", extractedNumericalStr))
                validateNumericaldata = false;
        }
        else {
            try {
                Double.parseDouble(extractedNumericalStr);
            } catch (NumberFormatException ex) {
                validateNumericaldata = false;
            }
        }

        if(!extractedColumnName.equals(columnName) || !validateNumericaldata){
            setMRCounterError(currentPath);
            return false;
        }
        return true;
    }

    private static void setMRCounterError(String counterName){
        String errorMessage = "MR Counter \"" + counterName + "\" can only be incremented or decremented!";
        setNotification("Error executing Update Row : " + errorMessage);
    }

    private static void setNotification(String message) {
        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", message, NotificationType.ERROR);
        Notifications.Bus.notify(notification, project);
    }
}

