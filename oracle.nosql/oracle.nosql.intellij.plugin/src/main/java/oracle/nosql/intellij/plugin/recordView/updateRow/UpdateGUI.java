/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView.updateRow;

import javax.swing.*;
import java.awt.*;

public class UpdateGUI {
    private JPanel rowPanel;
    public JTextField rowTextField;
    public JLabel rowLabel;
    public JButton selectFileToUploadButton;
    public boolean binaryFieldRow;

    public UpdateGUI(){
        rowPanel.setPreferredSize(new Dimension(600, 46));
        selectFileToUploadButton.setVisible(false);
        binaryFieldRow=false;
    }


}
