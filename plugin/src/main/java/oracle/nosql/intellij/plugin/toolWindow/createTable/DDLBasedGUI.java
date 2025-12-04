/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createTable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DDLBasedGUI {
    private JPanel rootPanel;
    private JPanel ddlMainPanel;
    private JPanel cloudUnitsPanel;
    private JTextField readUnitTextField;
    private JTextField writeUnitTextField;
    private JTextField storageUnitTextField;
    private JTextArea textArea1;

    public DDLBasedGUI(){
        cloudUnitsPanel.setVisible(false);
        cloudUnitsPanel.setEnabled(false);
        readUnitTextField.setName("RU-TextField");
        readUnitTextField.setToolTipText("Range 1-40,000");
        writeUnitTextField.setName("WU-TextField");
        writeUnitTextField.setToolTipText("Range 1-20,000");
        storageUnitTextField.setName("SU-TextField");
        storageUnitTextField.setToolTipText("Range 1-5000");
        textArea1.setToolTipText("Write/Paste Create Table query");
    }


    public JComponent getRootPanel(){
        return rootPanel;
    }

    public JComponent getCloudUnitsPanel(){
        return cloudUnitsPanel;
    }

    public JComponent getDdlMainPanel() { return ddlMainPanel;}
}
