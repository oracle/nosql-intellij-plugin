/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView.updateRow;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class DDLUpdateGUI {
    private JPanel ddlMainPanel;
    private JTextArea textArea1;

    public DDLUpdateGUI(String jString) {
        textArea1.setText(jString);
        textArea1.setToolTipText("Edit values in the JSON formatted row string");
    }

    public JComponent getDdlMainPanel() {
        return ddlMainPanel;
    }
}
