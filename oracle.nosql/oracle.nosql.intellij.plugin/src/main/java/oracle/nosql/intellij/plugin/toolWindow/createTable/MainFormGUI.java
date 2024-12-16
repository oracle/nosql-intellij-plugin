/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createTable;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.NoSqlIcons;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Main class for Create table ui. Calls FormBasedGUI and DDLBasedUI.
 *
 */
public class MainFormGUI {
    private JComboBox<String> comboBox1;
    private JPanel rootPanel;
    private JPanel subPanel;
    private JPanel buttonPanel;
    private JButton createButton;
    private JButton closeButton;
    private JPanel comboBoxPanel;
    private JButton showDDLButton;
    private static JFrame frame;

    FormBasedGUI formBasedGUI;
    DDLBasedGUI ddlBasedGUI;
    HashMap<String,String> resultSet ; // contains parameters passed to create table method in CloudConnection.java

    public MainFormGUI(Project project) {
        assert comboBox1 != null;
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
            if(name.equals("Form based table schema entry (For Simple DDL input)")){
                // To prevent frame size from increasing
                formBasedGUI.getRootPanel().setVisible(true);
                ddlBasedGUI.getRootPanel().setVisible(false);
                showDDLButton.setVisible(true);
            }else {
                formBasedGUI.getRootPanel().setVisible(false);
                ddlBasedGUI.getRootPanel().setVisible(true);
                showDDLButton.setVisible(false);
            }
        });

        // Initialize FormBasedGUI and DDLBasedGUI
        formBasedGUI = new FormBasedGUI();
        ddlBasedGUI = new DDLBasedGUI();

        // Get the latest profile type.
        String prefKeyForProfileType = "/profile_type";
        String profileType = ConnectionDataProviderService
                .getInstance(Objects.requireNonNull(project)).
                        getValue(prefKeyForProfileType);

        // Enable Reserved Capacity Panel for Cloud & Cloudsim
        if(profileType != null && (profileType.equals("Cloud") || profileType.equals("Cloudsim"))){
            formBasedGUI.getCloudUnitsPanel().setEnabled(true);
            formBasedGUI.getCloudUnitsPanel().setVisible(true);
            ddlBasedGUI.getCloudUnitsPanel().setVisible(true);
            ddlBasedGUI.getCloudUnitsPanel().setEnabled(true);
        }

        createButton.addActionListener(e -> {
            String comboItem = (String) comboBox1.getSelectedItem();

            assert comboItem != null;
            if(validate(comboItem)){

                    if(comboItem.equals("Form based table schema entry (For Simple DDL input)")){
                        resultSet = formDDLString();
                    }
                    else if(comboItem.equals("Supply Table Schema as DDL Statement (For Advanced DDL input)")){
                        resultSet = new HashMap<>();
                        resultSet.put("query",getDDLStatement());
                        resultSet.put("TableName","sample_table");
                    }
                    frame.dispose();
                    ProgressManager.getInstance().run(new Task.Backgroundable(project,"Creating table",false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            IConnection con;
                            try {
                                con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                                try {

                                    if(profileType != null && (profileType.equals("Cloud") || profileType.equals("Cloudsim"))){
                                        con.createTable(
                                                resultSet.get("TableName"),
                                                resultSet.get("query"),
                                                Integer.parseInt(resultSet.get("readKB")),
                                                Integer.parseInt(resultSet.get("writeKB")),
                                                Integer.parseInt(resultSet.get("storageKB"))
                                        );
                                    } else {
                                        con.createTable(
                                                resultSet.get("TableName"),
                                                resultSet.get("query"),
                                                1,
                                                1,
                                                1
                                        );
                                    }
                                } catch (Exception ex) {
                                    Notification notification = new Notification(
                                            "Oracle NOSQL", "Oracle NoSql explorer",
                                            OracleNoSqlBundle
                                                    .message(
                                                            "oracle.nosql.toolWindow.createTable.error") +
                                                    ex.getMessage(),
                                            NotificationType.ERROR);
                                    Notifications.Bus.notify(notification, project);
                                    return;
                                }
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
        });

        closeButton.addActionListener(e -> frame.dispose());

        showDDLButton.addActionListener(e -> {
            if(validate((String) Objects.requireNonNull(comboBox1.getSelectedItem()))){
                    JOptionPane.showMessageDialog(null, formDDLString().get("query"),"DDL Statement",JOptionPane.INFORMATION_MESSAGE,NoSqlIcons.ORACLE_LOGO);
            }
        });

        // Creates a frame
        createFrame();
    }

    private void createFrame() {
        frame = new JFrame("Create Table");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this.createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public JComponent createPanel(){
        String[] createTableModes = {"Form based table schema entry (For Simple DDL input)","Supply Table Schema as DDL Statement (For Advanced DDL input)"};
        comboBox1.setModel(new DefaultComboBoxModel<>(createTableModes));
        for (String createTableMode : createTableModes) {
            subPanel.add(createTableMode, getCreateTableModeSpecificUI(createTableMode));
        }
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel,name);
        });
        rootPanel.setPreferredSize(new Dimension(900,500));
        return rootPanel;
    }


    public JPanel getCreateTableModeSpecificUI(String uiType){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraintsPkPanel = new GridBagConstraints();
        GridBagConstraints gridBagConstraintsColumnPanel = new GridBagConstraints();
        if(uiType.equals("Supply Table Schema as DDL Statement (For Advanced DDL input)")){
            panel.add(ddlBasedGUI.getRootPanel());
        }
        else if (uiType.equals("Form based table schema entry (For Simple DDL input)")){
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
                if(formBasedGUI.getPkPanelCount() == 1){
                    for(Component c : formBasedGUI.getMainPkPanel().getComponents()){
                        if(c instanceof JPanel){
                            for (Component c1 : ((JPanel) c).getComponents()){
                                if(c1 instanceof JButton){
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
            formBasedGUI.getMainColKeyPanel().add(columnGUI.getColumnKeyPanel(),gridBagConstraintsColumnPanel);
            columnGUI.setRemoveButton((JPanel) formBasedGUI.getMainColKeyPanel());

            // Action Listener for Add Column Button
            formBasedGUI.getAddColumnButton().addActionListener(e -> {
                ColumnGUI columnGUI1 = new ColumnGUI();
                columnGUI1.setRemoveButton((JPanel) formBasedGUI.getMainColKeyPanel());
                gridBagConstraintsColumnPanel.gridy = gridBagConstraintsColumnPanel.gridy + 10;
                formBasedGUI.getMainColKeyPanel().add(columnGUI1.getColumnKeyPanel(),gridBagConstraintsColumnPanel);
                formBasedGUI.getMainColKeyPanel().updateUI();
            });

            panel.add(formBasedGUI.getRootPanel());
        }

        return panel;
    }

    /**
     * Forms DDL Statement (query) from user input
     *
     * @return HashMap with keys - TableName, query, readKB, writeKB, storageGB
     */
    public HashMap<String, String> formDDLString() {
        HashMap<String,String> createTableParametersMap = new HashMap<>();
        ArrayList<String> fieldDefinitions = new ArrayList<>();
        StringBuilder primaryKeyString = new StringBuilder("Primary Key( ");
        ArrayList<String> shardKeys = new ArrayList<>();
        ArrayList<String> restPrimaryKeys = new ArrayList<>();
        String primaryKeyColName = "";
        String tableName = "";
        int ttl = 0;
        String ttlUnit = "";
        boolean isShardKey = false;
        for (Component c: formBasedGUI.getTableNamePanel().getComponents()){
            if(c instanceof JTextField){
                if(c.getName().equals("TableName-TextField")){
                    tableName = ((JTextField) c).getText().trim();
                    createTableParametersMap.put("TableName",tableName);
                }
            }
        }
        for(Component component: formBasedGUI.getMainPkPanel().getComponents()){

            if(component instanceof JPanel ){
                StringBuilder fieldDefinition = new StringBuilder(new StringBuilder());
                for (Component c : ((JPanel) component).getComponents()){
                    if(c instanceof JTextField){
                        if(c.getName().equals("PKColumnName")){
                            String text = ((JTextField) c).getText().trim();
                            fieldDefinition.append(text).append(" ");
                            primaryKeyColName = text;
                        }
                    }
                    if(c instanceof JComboBox){
                        String item = (String) ((JComboBox<?>) c).getSelectedItem();
                        assert item != null;
                        if(item.equals("Timestamp")){
                            for (Component component1 : ((JPanel) component).getComponents() ){
                                if(component1 instanceof JTextField){
                                    if(component1.isVisible() && component1.getName().equals("PKPrecision")){
                                        fieldDefinition.append("Timestamp(").append(((JTextField) component1).getText().trim()).append(") ");
                                    }
                                }
                            }
                        }else {
                            fieldDefinition.append(item).append(" ");
                        }

                    }
                    if(c instanceof JCheckBox){
                        isShardKey = ((JCheckBox) c).isSelected();
                    }
                }
                if(isShardKey){
                    shardKeys.add(primaryKeyColName);
                }else {
                    restPrimaryKeys.add(primaryKeyColName);
                }
                fieldDefinitions.add(fieldDefinition.toString());
            }

        }

        if(shardKeys.size() > 0){
            primaryKeyString.append("Shard( ");
            for (String shardKey: shardKeys){
                primaryKeyString.append(shardKey).append(", ");
            }
            primaryKeyString = new StringBuilder(primaryKeyString.substring(0, primaryKeyString.length() - 2));
            primaryKeyString.append(" )");
        }

        if(primaryKeyString.toString().endsWith(")")){
            primaryKeyString.append(", ");
        }

        for (String key : restPrimaryKeys){
            primaryKeyString.append(key).append(", ");
        }
        primaryKeyString = new StringBuilder(primaryKeyString.substring(0, primaryKeyString.length() - 2));
        primaryKeyString.append(" )");

        for (Component component: formBasedGUI.getMainColKeyPanel().getComponents()){

            if(component instanceof JPanel ){
                StringBuilder fieldDefinition = new StringBuilder();
                for (Component c : ((JPanel) component).getComponents()){
                    if(c instanceof JTextField){
                        if(c.getName().equals("ColName-TextField")){
                            String text = ((JTextField) c).getText().trim();
                            fieldDefinition.append(text).append(" ");
                        }
                        if(c.getName().equals("DefaultValue-TextField")){
                            String defVal = ((JTextField) c).getText().trim();
                            if(!defVal.isEmpty()){
                                for(Component component1 : ((JPanel) component).getComponents()){
                                    if(component1 instanceof JComboBox){
                                        if(((JComboBox<?>) component1).getSelectedItem() == "String" || ((JComboBox<?>) component1).getSelectedItem() == "Timestamp"){
                                            fieldDefinition.append("DEFAULT \"").append(defVal).append("\" ");
                                        }
                                        else {
                                            fieldDefinition.append("DEFAULT ").append(defVal).append(" ");
                                        }
                                        break;
                                    }
                                }

                            }

                        }
                    }
                    if(c instanceof JComboBox){
                        String item = (String) ((JComboBox<?>) c).getSelectedItem();
                        assert item != null;
                        if(item.equals("Timestamp")){
                            for (Component component1 : ((JPanel) component).getComponents() ){
                                if(component1.isVisible() && component1 instanceof JTextField){
                                    if(component1.getName().equals("Precision-TextField")){
                                        fieldDefinition.append("Timestamp(").append(((JTextField) component1).getText().trim()).append(") ");
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
                            fieldDefinition.append(item).append(" ");
                        }
                    }
                    if(c instanceof JCheckBox){
                        boolean item = ((JCheckBox) c).isSelected();
                        if(item){
                            fieldDefinition.append("NOT NULL ");
                        }
                    }
                }
                fieldDefinitions.add(fieldDefinition.toString());
            }
        }
        if(formBasedGUI.getCloudUnitsPanel().isEnabled()){
            for (Component c:formBasedGUI.getCloudUnitsPanel().getComponents()){
                if(c instanceof JTextField){
                    if(c.getName().equals("RU-TextField")){
                        createTableParametersMap.put("readKB",((JTextField) c).getText());
                    }
                    if(c.getName().equals("WU-TextField")){
                        createTableParametersMap.put("writeKB",((JTextField) c).getText());
                    }
                    if(c.getName().equals("SU-TextField")){
                        createTableParametersMap.put("storageKB",((JTextField) c).getText());
                    }
                }
            }
        }

        for(Component c: formBasedGUI.getTtlPanel().getComponents()){
            if(c instanceof JTextField){
                if(c.getName().equals("TTL-TextField")){
                    String timeToLive = ((JTextField) c).getText().trim();
                    if(!timeToLive.isEmpty()){
                        ttl = Integer.parseInt(timeToLive);
                    }
                }
            }
            if(c instanceof JRadioButton){
                if(c.getName().equals("Days-RadioButton")){
                    if(((JRadioButton) c).isSelected()){
                        ttlUnit = "days";
                    }
                }
                else if(c.getName().equals("Hours-RadioButton")){
                    if(((JRadioButton) c).isSelected()){
                        ttlUnit = "hours";
                    }
                }
            }
        }

        StringBuilder ddlStatement = new StringBuilder("CREATE TABLE ").append(tableName).append(" (\n");
        for (String fieldDefinition : fieldDefinitions){
            ddlStatement.append(fieldDefinition).append(", ").append('\n');
        }
        ddlStatement.append(primaryKeyString).append(" )");
        if(ttl > 0){
            ddlStatement.append(" USING TTL ").append(ttl).append(" ").append(ttlUnit);
        }
        createTableParametersMap.put("query", ddlStatement.toString());
        return createTableParametersMap;
    }


    /**
     * For DDLBasedGUI input
     * @return ddlStatement from user input
     */
    public String getDDLStatement(){
        String ddlStatement = "";

        if(ddlBasedGUI.getCloudUnitsPanel().isEnabled()){
            for (Component c:ddlBasedGUI.getCloudUnitsPanel().getComponents()){
                if(c instanceof JTextField){
                    if(c.getName().equals("RU-TextField")){
                        resultSet.put("readKB",((JTextField) c).getText());
                    }
                    if(c.getName().equals("WU-TextField")){
                        resultSet.put("writeKB",((JTextField) c).getText());
                    }
                    if(c.getName().equals("SU-TextField")){
                        resultSet.put("storageKB",((JTextField) c).getText());
                    }
                }
            }
        }

        for (Component component : ddlBasedGUI.getDdlMainPanel().getComponents()){
            if(component instanceof JScrollPane){
                for (Component c: ((JScrollPane) component).getViewport().getComponents()){
                    if(c instanceof JTextArea){
                        ddlStatement = ((JTextArea) c).getText().trim();
                    }

                }
            }

        }
        return ddlStatement;
    }


    /**
     * Validates user input and displays appropriate error msg
     *
     * @param uiType Table Creation Mode
     * @return true - if all the validation checks are passed, false - if any validation check fails
     */
    public boolean validate(@NotNull String uiType) {
        if(uiType.equals("Form based table schema entry (For Simple DDL input)")){
            int shardKeyCheck = 1;
            for (Component c: formBasedGUI.getTableNamePanel().getComponents()){
                if(c instanceof JTextField){
                    if(c.getName().equals("TableName-TextField")){
                        if(((JTextField) c).getText().trim().isEmpty()){
                            JOptionPane.showMessageDialog(null,"Table Name cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        }
                        else if (((JTextField) c).getText().trim().length() > 256){
                            JOptionPane.showMessageDialog(null,"Table Name length should be less than 256 characters","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        }
                        else if(!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$",((JTextField) c).getText())){
                            JOptionPane.showMessageDialog(null,"Table Name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                            return false;
                        }
                    }
                }
            }
            for(Component component: formBasedGUI.getMainPkPanel().getComponents()){

                if(component instanceof JPanel ){
                    for (Component c : ((JPanel) component).getComponents()){
                        if(c instanceof JTextField){
                            if(c.getName().equals("PKColumnName")){
                                if(((JTextField) c).getText().trim().isEmpty()){
                                    JOptionPane.showMessageDialog(frame,"Primary Key Column Name cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                                else if(((JTextField) c).getText().trim().length() > 64){
                                    JOptionPane.showMessageDialog(null,"Primary Key Column Name length should be less than 64 characters","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                                else if(!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$",((JTextField) c).getText())){
                                    JOptionPane.showMessageDialog(null,"Primary Key Name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }
                            if(c.getName().equals("PKPrecision") && c.isVisible()){
                                if(((JTextField) c).getText().trim().isEmpty()){
                                    JOptionPane.showMessageDialog(null,"Precision Value cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                                else if(!Pattern.matches("^[0-9]$",((JTextField) c).getText())){
                                    JOptionPane.showMessageDialog(null,"Precision Value should be an integer between 0-9","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }

                        }
                        if(c instanceof JCheckBox){
                            if(((JCheckBox) c).isSelected()){
                                if(shardKeyCheck == 0){
                                    JOptionPane.showMessageDialog(null,"Shard keys must appear first and form a contiguous group","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }else {
                                shardKeyCheck = 0;
                            }
                        }
                    }
                }

            }

            for (Component component: formBasedGUI.getMainColKeyPanel().getComponents()){

                if(component instanceof JPanel ){
                    for (Component c : ((JPanel) component).getComponents()){
                        if(c instanceof JTextField){
                            if(c.getName().equals("ColName-TextField")){
                                if(((JTextField) c).getText().trim().isEmpty()){
                                    JOptionPane.showMessageDialog(frame,"Column Name cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                                else if(((JTextField) c).getText().trim().length() > 64){
                                    JOptionPane.showMessageDialog(frame,"Column Name length should be less than 64 characters","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                                else if(!Pattern.matches("^[A-Za-z][A-Za-z0-9_$:.]*$",((JTextField) c).getText())){
                                    JOptionPane.showMessageDialog(frame,"Column Name should begin with letter (A-Z, a-z), and is restricted to alphanumeric characters (A-Z, a-z, 0–9), plus underscore (_) and a period (.) character","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }

                            }
                            if(c.getName().equals("Precision-TextField") && c.isVisible()){
                                if(((JTextField) c).getText().trim().isEmpty()){
                                    JOptionPane.showMessageDialog(null,"Precision Value cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                                else if(!Pattern.matches("^[0-9]$",((JTextField) c).getText())){
                                    JOptionPane.showMessageDialog(null,"Precision Value should be an integer between 0-9","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                    return false;
                                }
                            }
                            if(c.getName().equals("DefaultValue-TextField")){
                                for(Component component1 : ((JPanel) component).getComponents()){
                                    if(component1 instanceof JCheckBox){
                                        if(((JCheckBox) component1).isSelected() && ((JTextField) c).getText().trim().isEmpty()){
                                            JOptionPane.showMessageDialog(frame,"Default Value is required if Not Null is set","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                            return false;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
            if(formBasedGUI.getCloudUnitsPanel().isEnabled()){
                for (Component c:formBasedGUI.getCloudUnitsPanel().getComponents()){
                    if(c instanceof JTextField){
                        if(c.getName().equals("RU-TextField")){
                            if(((JTextField) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(null,"Read Units cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(!Pattern.matches("^[0-9]*$",((JTextField) c).getText().trim())){
                                JOptionPane.showMessageDialog(null,"Read Units should be integer in range 1-40,000","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(Integer.parseInt(((JTextField) c).getText().trim()) > 40000 || Integer.parseInt(((JTextField) c).getText().trim()) < 1){
                                JOptionPane.showMessageDialog(null,"Read Units should be integer in range 1-40,000","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }

                        }
                        if(c.getName().equals("WU-TextField")){
                            if(((JTextField) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(null,"Write Units cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(!Pattern.matches("^[0-9]*$",((JTextField) c).getText().trim())){
                                JOptionPane.showMessageDialog(null,"Write Units should be integer in range 1-20,000","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(Integer.parseInt(((JTextField) c).getText().trim()) > 20000 || Integer.parseInt(((JTextField) c).getText().trim()) < 1){
                                JOptionPane.showMessageDialog(null,"Read Units should be integer in range 1-20,000","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }

                        }
                        if(c.getName().equals("SU-TextField")){
                            if(((JTextField) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(null,"Disk Storage cannot be empty","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(!Pattern.matches("^[0-9]*$",((JTextField) c).getText().trim())){
                                JOptionPane.showMessageDialog(null,"Disk Storage should be integer in range 1-5000","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(Integer.parseInt(((JTextField) c).getText().trim()) > 5000 || Integer.parseInt(((JTextField) c).getText().trim()) < 1){
                                JOptionPane.showMessageDialog(null,"Disk Storage should be integer in range 1-20,000","Error",JOptionPane.PLAIN_MESSAGE, AllIcons.General.ErrorDialog);
                                return false;
                            }

                        }
                    }
                }
            }

            for(Component c: formBasedGUI.getTtlPanel().getComponents()){
                if(c instanceof JTextField){
                    if(c.getName().equals("TTL-TextField")){
                        String ttlValue = ((JTextField) c).getText().trim();
                        if(!ttlValue.isEmpty() && !Pattern.matches("^[0-9]*$",ttlValue)){
                            JOptionPane.showMessageDialog(null,"TTL Value should be an integer","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                        }
                    }
                }
            }


        }else if(uiType.equals("Supply Table Schema as DDL Statement (For Advanced DDL input)")){

            if(ddlBasedGUI.getCloudUnitsPanel().isEnabled()){
                for (Component c:ddlBasedGUI.getCloudUnitsPanel().getComponents()){
                    if(c instanceof JTextField){
                        if(c.getName().equals("RU-TextField")){
                            if(((JTextField) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(frame,"Read Units cannot be empty","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(!Pattern.matches("^[0-9]*$",((JTextField) c).getText().trim())){
                                JOptionPane.showMessageDialog(frame,"Read Units should be integer in range 1-40,000","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(Integer.parseInt(((JTextField) c).getText().trim()) > 40000 || Integer.parseInt(((JTextField) c).getText().trim()) < 1){
                                JOptionPane.showMessageDialog(frame,"Read Units should be integer in range 1-40,000","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }

                        }
                        if(c.getName().equals("WU-TextField")){
                            if(((JTextField) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(frame,"Write Units cannot be empty","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(!Pattern.matches("^[0-9]*$",((JTextField) c).getText().trim())){
                                JOptionPane.showMessageDialog(frame,"Write Units should be integer in range 1-20,000","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(Integer.parseInt(((JTextField) c).getText().trim()) > 20000 || Integer.parseInt(((JTextField) c).getText().trim()) < 1){
                                JOptionPane.showMessageDialog(frame,"Read Units should be integer in range 1-20,000","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }

                        }
                        if(c.getName().equals("SU-TextField")){
                            if(((JTextField) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(frame,"Disk Storage cannot be empty","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(!Pattern.matches("^[0-9]*$",((JTextField) c).getText().trim())){
                                JOptionPane.showMessageDialog(frame,"Disk Storage should be integer in range 1-5000","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }
                            else if(Integer.parseInt(((JTextField) c).getText().trim()) > 5000 || Integer.parseInt(((JTextField) c).getText().trim()) < 1){
                                JOptionPane.showMessageDialog(frame,"Read Units should be integer in range 1-5000","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
                                return false;
                            }

                        }
                    }
                }
            }

            for (Component component : ddlBasedGUI.getDdlMainPanel().getComponents()){
                if(component instanceof JScrollPane){
                    for (Component c : ((JScrollPane) component).getViewport().getComponents()){
                        if(c instanceof JTextArea){
                            if(((JTextArea) c).getText().trim().isEmpty()){
                                JOptionPane.showMessageDialog(frame,"Query input cannot be empty","Error",JOptionPane.PLAIN_MESSAGE,AllIcons.General.ErrorDialog);
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
