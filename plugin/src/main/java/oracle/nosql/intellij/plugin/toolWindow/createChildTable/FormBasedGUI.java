/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createChildTable;

import oracle.nosql.model.schema.Table;

import javax.swing.*;

public class FormBasedGUI {
    private JPanel rootPanel;
    private JPanel tableNamePanel;
    private JTextField tableNameTextField;
    private JPanel mainPkPanel;
    private JButton addPrimaryKeyColumnButton;
    private JPanel mainColKeyPanel;
    private JButton addColumnButton;
    private JPanel ttlPanel;
    private JTextField ttlTextField;
    private JRadioButton daysRadioButton;
    private JRadioButton hoursRadioButton;
    private JLabel Name;
    private JLabel parentTableLabel;
    private int pkPanelCount = 0;

    public FormBasedGUI(Table table) {
        parentTableLabel.setText(table.getName() + ".");
        tableNameTextField.setName("TableName-TextField");
        tableNameTextField.setToolTipText("Table Name");
        ttlTextField.setName("TTL-TextField");
        ttlTextField.setToolTipText("TTL Value (Optional)");

        daysRadioButton.setName("Days-RadioButton");
        hoursRadioButton.setName("Hours-RadioButton");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(daysRadioButton);
        buttonGroup.add(hoursRadioButton);

        daysRadioButton.setSelected(true);
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public JComponent getTableNamePanel() {
        return tableNamePanel;
    }

    public JComponent getMainPkPanel() {
        return mainPkPanel;
    }

    public JComponent getMainColKeyPanel() {
        return mainColKeyPanel;
    }

    public JComponent getTtlPanel() {
        return ttlPanel;
    }

    public JButton getAddPrimaryKeyColumnButton() {
        return addPrimaryKeyColumnButton;
    }

    public JButton getAddColumnButton() {
        return addColumnButton;
    }

    public int getPkPanelCount() {
        return pkPanelCount;
    }

    public void increasePkPanelCount() {
        pkPanelCount++;
    }

    public void decreasePkPanelCount() {
        pkPanelCount--;
    }
}
