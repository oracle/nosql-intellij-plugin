/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.dropReplicas;

import javax.swing.*;

public class FormGUI {
    private JPanel rootPanel;
    private JPanel replicasPanel;
    private JPanel buttonPanel;
    private JButton addButton;
    public FormGUI(){
    }
    public JComponent getRootPanel(){
        return rootPanel;
    }

    public  JComponent getReplicasPanel(){
        return replicasPanel;
    }

    public JButton getAddButton() {
        return addButton;
    }
}
