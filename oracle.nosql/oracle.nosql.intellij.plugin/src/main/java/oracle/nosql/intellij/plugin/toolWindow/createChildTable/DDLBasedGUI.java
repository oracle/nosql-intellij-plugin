/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createChildTable;

import javax.swing.*;

public class DDLBasedGUI {
    private JPanel rootPanel;
    private JPanel ddlMainPanel;
    private JTextArea textArea1;

    public DDLBasedGUI() {
        textArea1.setToolTipText("Write/Paste Create Child Table query");
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public JComponent getDdlMainPanel() {
        return ddlMainPanel;
    }
}
