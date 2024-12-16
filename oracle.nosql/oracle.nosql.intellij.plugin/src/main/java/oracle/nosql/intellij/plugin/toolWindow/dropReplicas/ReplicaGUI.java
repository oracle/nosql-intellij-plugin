/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.dropReplicas;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

import static oracle.nosql.intellij.plugin.toolWindow.dropReplicas.DropReplicasGUI.replicasNames;

public class ReplicaGUI {
    private JComboBox<String> replicationRegionComboBox;
    private JButton removeButton;
    private JPanel replicaPanel;

    public ReplicaGUI(){
        removeButton.setForeground(JBColor.RED);
        if(!replicasNames.isEmpty())
            replicationRegionComboBox.setModel(new DefaultComboBoxModel<>(replicasNames.toArray(new String[0])));
    }

    public JButton getRemoveButton(){
        return removeButton;
    }

    public JComponent getReplicaPanel(){
        return replicaPanel;
    }
    public void setRemoveButton(JPanel ReplicasPanel){
        removeButton.addActionListener(e -> {
            ReplicasPanel.remove(replicaPanel);
            ReplicasPanel.updateUI();
            for(Component component: ReplicasPanel.getComponents()){
                if(component instanceof JPanel ) {
                    ((JPanel) component).updateUI();
                }
            }
        });
    }
}
