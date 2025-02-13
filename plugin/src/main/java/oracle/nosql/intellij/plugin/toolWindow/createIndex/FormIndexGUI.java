/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.createIndex;

import javax.swing.*;

public class FormIndexGUI {
    private JPanel rootPanel;
    private JPanel mainPkPanel;
    private JButton addIndexColumnButton;
    private int pkPanelCount = 0;
    public JComponent getRootPanel(){
        return rootPanel;
    }
    public JComponent getMainPkPanel(){
        return mainPkPanel;
    }
    public JButton getAddColumnButton() {
        return addIndexColumnButton;
    }
    public int getPkPanelCount(){return pkPanelCount;}
    public void increasePkPanelCount(){
        pkPanelCount++;
    }
    public void decreasePkPanelCount(){
        pkPanelCount--;
    }
}
