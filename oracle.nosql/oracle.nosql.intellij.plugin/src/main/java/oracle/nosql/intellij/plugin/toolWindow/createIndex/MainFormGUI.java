/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createIndex;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class MainFormGUI extends JDialog {
    private JPanel rootPanel;
    private JPanel subPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JPanel comboBoxPanel;
    private JComboBox<String> comboBox1;
    public static JFrame frame;
    private String FORM_BASED_INDEXING = "Form Based Index Creation (For Simple DDL input)";
    private String DDL_BASED_INDEXING = "Create Index as DDL Statement (For Advanced DDL input)";
    private boolean isJsonCollection;
    DDLBasedGUI ddlBasedGUI;
    FormBasedGUI formBasedGUI;

    public MainFormGUI(Project project, Table table) {
        // Initialize FormBasedGUI and DDLBasedGUI
        formBasedGUI = new FormBasedGUI(project, table);
        ddlBasedGUI = new DDLBasedGUI(project, table);

        IConnection connection;
        try {
            connection = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
            String schemaJson = connection.showSchema(table);
            JSONObject jsonObject = new JSONObject(schemaJson);
            isJsonCollection = jsonObject.has("jsonCollection");
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

        assert comboBox1 != null;
        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
            if (name.equals(FORM_BASED_INDEXING)) {
                ddlBasedGUI.getRootPanel().setVisible(false);
                formBasedGUI.getRootPanel().setVisible(true);
            } else if (name.equals(DDL_BASED_INDEXING)) {
                formBasedGUI.getRootPanel().setVisible(false);
                ddlBasedGUI.getRootPanel().setVisible(true);
            }
        });

        if(isJsonCollection)
            JOptionPane.showMessageDialog(
                    null,
                    "Simple Input is disabled for JSON Collection Tables. Please use Advanced JSON Input section to insert the data.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );

        createFrame();
        closeButton.addActionListener(e -> frame.dispose());
    }

    private void createFrame() {
        frame = new JFrame("Create Index");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public JComponent createPanel() {
        String[] createIndexModes = {FORM_BASED_INDEXING, DDL_BASED_INDEXING};
        comboBox1.setModel(new DefaultComboBoxModel<>(createIndexModes){
            @Override
            public void setSelectedItem(Object item) { // sets the default model to Advanced Input for jc tables
                if (isJsonCollection) {
                    if(!FORM_BASED_INDEXING.equals(item))
                        super.setSelectedItem(item);
                }
                else{
                    super.setSelectedItem(item);
                }
            }
        });
        for (String createIndexMode : createIndexModes) {
            subPanel.add(createIndexMode, getCreateIndexModeSpecificUI(createIndexMode));
        }

        if (isJsonCollection) {
            comboBox1.setSelectedItem(DDL_BASED_INDEXING);
        } else {
            comboBox1.setSelectedItem(FORM_BASED_INDEXING);
        }

        comboBox1.addItemListener(e -> {
            String name = (String) e.getItem();
            CardLayout cl = (CardLayout) subPanel.getLayout();
            cl.show(subPanel, name);
        });
        rootPanel.setPreferredSize(new Dimension(900, 500));

        // custom renderer to visually disable simple DDL input option
        if(isJsonCollection) {
            comboBox1.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (FORM_BASED_INDEXING.equals(value)) {
                        comp.setForeground(Color.GRAY);
                        comp.setEnabled(false);
                    }
                    return comp;
                }
            });
        }
        return rootPanel;
    }

    public JPanel getCreateIndexModeSpecificUI(String uiType) {
        JPanel panel = new JPanel(new GridBagLayout());
        if (uiType.equals(DDL_BASED_INDEXING)) {
            panel.add(ddlBasedGUI.getRootPanel());
        } else if (uiType.equals(FORM_BASED_INDEXING)) {
            panel.add(formBasedGUI.getRootPanel());
        }
        return panel;
    }
}
