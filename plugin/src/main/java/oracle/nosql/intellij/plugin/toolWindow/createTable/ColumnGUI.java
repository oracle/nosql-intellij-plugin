/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createTable;

import com.intellij.ui.JBColor;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;

/**
 * Represents the Column Panels inside mainColKeyPanel in FormBasedGUI
 *
 */
public class ColumnGUI {
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

    public ColumnGUI(){
        colNameTextField.setColumns(20);
        colNameTextField.setName("ColName-TextField");
        colNameTextField.setToolTipText("Column Name");
        precisionTextField.setColumns(5);
        defaultValTextField.setColumns(10);
        precisionTextField.setName("Precision-TextField");
        precisionTextField.setToolTipText("Precision Value (0-9)");
        defaultValTextField.setName("DefaultValue-TextField");
        defaultValTextField.setToolTipText("Required if not null is set, Optional otherwise");
        removeButton.setForeground(JBColor.RED);
        removeButton.setToolTipText("Remove Column");
        binarySize.setName("Size-TextField");

        precisionLabel.setVisible(false);
        precisionTextField.setVisible(false);
        binarySize.setVisible(false);
        sizeLabel.setVisible(false);

        // Data Types supported for Column Keys
        String[] dataTypesString = {"Integer","String","Boolean","Double","Float","Long","Number","Binary","Json","Timestamp"};
        colTypeComboBox.setModel(new DefaultComboBoxModel<>(dataTypesString));
        colTypeComboBox.addItemListener(e -> {
            if(e.getItem().equals("Timestamp")){
                precisionTextField.setVisible(true);
                precisionLabel.setVisible(true);
            }
            else {
                precisionLabel.setVisible(false);
                precisionTextField.setVisible(false);
            }
            if(e.getItem().equals("Json")){
                defValueLabel.setVisible(false);
                defaultValTextField.setVisible(false);
                valueIsNotNullCheckBox.setVisible(false);
            }
            else {
                defValueLabel.setVisible(true);
                defaultValTextField.setVisible(true);
                valueIsNotNullCheckBox.setVisible(true);
            }
            if(e.getItem().equals("Binary")){
                defValueLabel.setVisible(false);
                defaultValTextField.setVisible(false);
                valueIsNotNullCheckBox.setVisible(false);
                binarySize.setVisible(true);
                sizeLabel.setVisible(true);
            }
            else {
                defValueLabel.setVisible(true);
                defaultValTextField.setVisible(true);
                valueIsNotNullCheckBox.setVisible(true);
                binarySize.setVisible(false);
                sizeLabel.setVisible(false);
            }
        });

    }

    /**
     *
     * @return - columnKeyPanel to be added to mainColKeyPanel in FormBasedGUI
     */
    public JComponent getColumnKeyPanel(){
        return columnKeyPanel;
    }

    /**
     *
     * @param MainColKeyPanel - mainColKeyPanel in FormBasedGUI
     */
    public void setRemoveButton(JPanel MainColKeyPanel){
        removeButton.addActionListener(e -> {
            MainColKeyPanel.remove(columnKeyPanel);
            MainColKeyPanel.updateUI();
            for(Component component: MainColKeyPanel.getComponents()){
                if(component instanceof JPanel ) {
                    ((JPanel) component).updateUI();
                }
            }
        });
    }

}
