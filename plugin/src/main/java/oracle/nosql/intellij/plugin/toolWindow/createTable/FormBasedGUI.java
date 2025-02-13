/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createTable;

import javax.swing.*;

/**
 * UI displayed when Form based table creation mode is chosen in MainFormGUI
 */
public class FormBasedGUI {
    private JPanel rootPanel;
    private JLabel Name;
    private JPanel tableNamePanel;
    private JTextField tableNameTextField;
    private JPanel mainPkPanel;
    private JButton addPrimaryKeyColumnButton;
    private JPanel mainColKeyPanel;
    private JButton addColumnButton;
    private JTextField ttlTextField;
    private JPanel cloudUnitsPanel;
    private JTextField readUnitTextField;
    private JTextField writeUnitTextField;
    private JTextField storageUnitTextField;
    private JPanel ttlPanel;
    private JRadioButton daysRadioButton;
    private JRadioButton hoursRadioButton;
    private int pkPanelCount = 0;

    public FormBasedGUI(){
        cloudUnitsPanel.setVisible(false);
        cloudUnitsPanel.setEnabled(false);
        tableNameTextField.setName("TableName-TextField");
        tableNameTextField.setToolTipText("Table Name");
        ttlTextField.setName("TTL-TextField");
        ttlTextField.setToolTipText("TTL Value (Optional)");
        readUnitTextField.setName("RU-TextField");
        writeUnitTextField.setName("WU-TextField");
        storageUnitTextField.setName("SU-TextField");

        readUnitTextField.setToolTipText("Range 1-40,000");
        writeUnitTextField.setToolTipText("Range 1-20,000");
        storageUnitTextField.setToolTipText("Range 1-5,000");

        daysRadioButton.setName("Days-RadioButton");
        hoursRadioButton.setName("Hours-RadioButton");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(daysRadioButton);
        buttonGroup.add(hoursRadioButton);

        daysRadioButton.setSelected(true);

    }


    public JComponent getRootPanel(){
        return rootPanel;
    }
    public JComponent getTableNamePanel(){ return tableNamePanel;}
    public JComponent getMainPkPanel(){
        return mainPkPanel;
    }
    public JComponent getMainColKeyPanel(){
        return mainColKeyPanel;
    }
    public JComponent getTtlPanel(){ return ttlPanel;}
    public JComponent getCloudUnitsPanel(){
        return cloudUnitsPanel;
    }
    public JButton getAddPrimaryKeyColumnButton(){
        return addPrimaryKeyColumnButton;
    }
    public JButton getAddColumnButton() {
        return addColumnButton;
    }
    public int getPkPanelCount(){return pkPanelCount;}
    public void increasePkPanelCount(){
        pkPanelCount++;
    }
    public void decreasePkPanelCount(){
        pkPanelCount--;
    }
}
