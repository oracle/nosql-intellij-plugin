/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createIndex;

import com.intellij.ui.JBColor;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.schema.Field;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class AddIndexColumnGUI {
    private JComboBox<String> addIndexColNameCombo;
    private JButton removeButton;
    private JPanel addIdxColPanel;
    private JTextField jsonPathToIndexField;
    private JComboBox<String> typeOfJsonIndexField;
    private JLabel pathLabel;
    private JLabel typeLabel;
    private final Set<String> jsonColumnSet;

    AddIndexColumnGUI(Table table) {
        jsonColumnSet = new HashSet<>();   //To store the json type of column names
        List<String> list =new ArrayList<>();
        for (int i = 0; i < table.getFieldCount(); i++) {
            String name = table.getFields().get(i).getName();
            Field.Type type = table.getFields().get(i).getType();
            if(!(type.equals(Field.Type.MAP) || type.equals(Field.Type.RECORD) || type.equals(Field.Type.ARRAY)))
                list.add(name);
            if (table.getFields().get(i).getType().equals(Field.Type.JSON)) jsonColumnSet.add(name);
        }
        String[] st = new String[list.size()];
        st=list.toArray(st);
        addIndexColNameCombo.setModel(new DefaultComboBoxModel<>(st));
        addIndexColNameCombo.setName("PKColumnName");

        String[] typeOfJsonIndex = new String[]{"Integer", "String", "Long", "Boolean", "Double", "Float", "Number", "AnyAtomic"};
        typeOfJsonIndexField.setModel(new DefaultComboBoxModel<>(typeOfJsonIndex));
        typeOfJsonIndexField.setName("Type");

        jsonPathToIndexField.setName("Path");
        jsonPathToIndexField.setToolTipText("Json path to Index");

        removeButton.setForeground(JBColor.RED);
        jsonPathToIndexField.setVisible(false);
        pathLabel.setVisible(false);
        typeOfJsonIndexField.setVisible(false);
        typeLabel.setVisible(false);

        addIndexColNameCombo.addItemListener(e -> {
            if (jsonColumnSet.contains(e.getItem())) {
                jsonPathToIndexField.setVisible(true);
                pathLabel.setVisible(true);
                typeOfJsonIndexField.setVisible(true);
                typeLabel.setVisible(true);
            } else {
                jsonPathToIndexField.setVisible(false);
                pathLabel.setVisible(false);
                typeOfJsonIndexField.setVisible(false);
                typeLabel.setVisible(false);
            }
        });
    }

    public void setRemoveButton(FormIndexGUI formBasedGUI) {
        removeButton.addActionListener(e -> {
            formBasedGUI.decreasePkPanelCount();
            formBasedGUI.getMainPkPanel().remove(addIdxColPanel);
            if (formBasedGUI.getPkPanelCount() == 1) {
                for (Component c : formBasedGUI.getMainPkPanel().getComponents()) {
                    if (c instanceof JPanel) {
                        for (Component c1 : ((JPanel) c).getComponents()) {
                            if (c1 instanceof JButton) {
                                c1.setEnabled(false);
                            }
                        }

                    }
                }
            }
            formBasedGUI.getMainPkPanel().updateUI();
        });
    }

    public void disableRemoveButton() {
        removeButton.setEnabled(false);
    }

    public JComponent getRootPanel() {
        return addIdxColPanel;
    }

    public Set<String> getJsonColumnSet() {
        return jsonColumnSet;
    }

}
