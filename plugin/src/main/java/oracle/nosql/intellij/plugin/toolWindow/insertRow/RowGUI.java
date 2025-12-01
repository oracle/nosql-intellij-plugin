/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.insertRow;

import javax.swing.*;
import java.awt.*;

public class RowGUI {
    private JPanel rowPanel;
    public JTextField rowTextField;
    public JLabel rowLabel;
    public JButton selectFileToUploadButton;
    public JLabel orLabel;
    public boolean binaryFieldRow;

    public RowGUI() {
        rowPanel.setPreferredSize(new Dimension(600, 46));
        selectFileToUploadButton.setVisible(false);
        //orLabel.setEnabled(false);
        orLabel.setVisible(false);
        orLabel.setName("orLabel");
        binaryFieldRow = false;
    }

    public JComponent getRowPanel() {
        return rowPanel;
    }

}
