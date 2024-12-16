/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.insertRow;

import javax.swing.*;

public class DDLInsertGUI {
    private JPanel ddlMainPanel;
    private JTextArea textArea1;

    public DDLInsertGUI(){
        textArea1.setToolTipText("Write/Paste JSON formatted row entry");
    }

    public JComponent getDdlMainPanel() { return ddlMainPanel;}
}
