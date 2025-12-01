/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.addColumn;

import javax.swing.*;

public class DDLColumnGUI {
    private JPanel rootPanel;
    private JTextArea textArea1;
    private JPanel ddlMainPanel;

    public DDLColumnGUI() {
        textArea1.setToolTipText("Write/Paste Create Table query");
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public JComponent getDdlMainPanel() {
        return ddlMainPanel;
    }
}
