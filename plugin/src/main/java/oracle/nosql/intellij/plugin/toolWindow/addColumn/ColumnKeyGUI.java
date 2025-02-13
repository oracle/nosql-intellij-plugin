/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.addColumn;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class ColumnKeyGUI {
    private JPanel columnKeyPanel;
    private JTextField colNameTextField;
    private JComboBox<String> colTypeComboBox;
    private JTextField precisionTextField;
    private JTextField defaultValTextField;
    private JCheckBox valueIsNotNullCheckBox;
    private JButton removeButton;
    private JLabel precisionLabel;
    private JLabel defValueLabel;
    private JTextField binarySize;
    private JLabel sizeLabel;

    public ColumnKeyGUI() {
        colNameTextField.setColumns(20);
        colNameTextField.setName("ColName-TextField");
        colNameTextField.setToolTipText("Column Name");
        precisionTextField.setColumns(5);
        defaultValTextField.setColumns(10);
        precisionTextField.setName("Precision-TextField");
        precisionTextField.setToolTipText("Precision Value (0-9)");
        binarySize.setName("Size-TextField");
        defaultValTextField.setName("DefaultValue-TextField");
        defaultValTextField.setToolTipText("Required if not null is set, Optional otherwise");
        removeButton.setForeground(JBColor.RED);
        removeButton.setToolTipText("Remove Column");
        precisionLabel.setVisible(false);
        precisionTextField.setVisible(false);
        binarySize.setVisible(false);
        sizeLabel.setVisible(false);

        String[] dataTypesString = {"Integer", "String", "Boolean", "Double", "Float", "Long", "Number", "Binary", "Json", "Timestamp"};
        colTypeComboBox.setModel(new DefaultComboBoxModel<>(dataTypesString));
        colTypeComboBox.addItemListener(e -> {
            if (e.getItem().equals("Timestamp")) {
                precisionTextField.setVisible(true);
                precisionLabel.setVisible(true);
            } else {
                precisionLabel.setVisible(false);
                precisionTextField.setVisible(false);
            }
            if (e.getItem().equals("Json")) {
                defValueLabel.setVisible(false);
                defaultValTextField.setVisible(false);
                valueIsNotNullCheckBox.setVisible(false);
            } else {
                defValueLabel.setVisible(true);
                defaultValTextField.setVisible(true);
                valueIsNotNullCheckBox.setVisible(true);
            }
            if (e.getItem().equals("Binary")) {
                defValueLabel.setVisible(false);
                defaultValTextField.setVisible(false);
                valueIsNotNullCheckBox.setVisible(false);
                binarySize.setVisible(true);
                sizeLabel.setVisible(true);
            } else {
                defValueLabel.setVisible(true);
                defaultValTextField.setVisible(true);
                valueIsNotNullCheckBox.setVisible(true);
                binarySize.setVisible(false);
                sizeLabel.setVisible(false);
            }
        });
    }

    public JComponent getColumnKeyPanel() {
        return columnKeyPanel;
    }

    public void setRemoveButton(FormColumnGUI formBasedGUI) {
        removeButton.addActionListener(e -> {
            formBasedGUI.decreasePkPanelCount();
            formBasedGUI.getMainPkPanel().remove(columnKeyPanel);
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
        return columnKeyPanel;
    }

}
