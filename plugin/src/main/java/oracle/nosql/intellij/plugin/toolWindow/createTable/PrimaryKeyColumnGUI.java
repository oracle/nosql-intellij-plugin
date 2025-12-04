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
import java.awt.Color;
import java.awt.Component;

/**
 * Represents the Primary Key Panels inside mainPkPanel in FormBasedGUI
 *
 */
public class PrimaryKeyColumnGUI {
    private JPanel pkPanel;
    private JTextField colNameTextField;
    private JComboBox<String> colTypeComboBox;
    private JCheckBox setAsShardKeyCheckBox;
    private JButton removeButton;
    private JTextField precisionTextField;
    private JLabel precisionLabel;

    public PrimaryKeyColumnGUI(){
        precisionLabel.setVisible(false);
        precisionTextField.setVisible(false);

        // Data types supported for primary key columns
        String[] dataTypesString = {"Integer","String","Boolean","Double","Float","Long","Number","Timestamp"};
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
        });
        colNameTextField.setColumns(20);
        colNameTextField.setName("PKColumnName");
        colNameTextField.setToolTipText("Primary Key Column Name");
        precisionTextField.setName("PKPrecision");
        precisionTextField.setToolTipText("Precision Value (0-9)");
        removeButton.setForeground(JBColor.RED);
        removeButton.setToolTipText("Remove primary key column");
    }


    /**
     *
     * @return primary key panel which is added to MainPkPanel in FormBasedGUI
     */
    public JComponent getPkPanel(){
        return pkPanel;
    }

    /**
     *
     * @param formBasedGUI - object of FormBasedGUI
     */
    public void setRemoveButton(FormBasedGUI formBasedGUI){
        removeButton.addActionListener(e -> {
            formBasedGUI.decreasePkPanelCount();
            formBasedGUI.getMainPkPanel().remove(pkPanel);
            if(formBasedGUI.getPkPanelCount() == 1){
                for(Component c : formBasedGUI.getMainPkPanel().getComponents()){
                    if(c instanceof JPanel){
                        for (Component c1 : ((JPanel) c).getComponents()){
                            if(c1 instanceof JButton){
                                c1.setEnabled(false);
                            }
                        }

                    }
                }
            }
            formBasedGUI.getMainPkPanel().updateUI();
        });
    }

    /**
     * disables remove button if there is only one pkPanel.
     */
    public void disableRemoveButton(){
        removeButton.setEnabled(false);
    }


}
