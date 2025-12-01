/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.addColumn;

import javax.swing.*;

public class FormColumnGUI {
    private JPanel rootPanel;
    private JPanel mainPkPanel;
    private JButton addColumnButton;
    private int pkPanelCount = 0;
    public JComponent getRootPanel(){
        return rootPanel;
    }
    public JComponent getMainPkPanel(){
        return mainPkPanel;
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
