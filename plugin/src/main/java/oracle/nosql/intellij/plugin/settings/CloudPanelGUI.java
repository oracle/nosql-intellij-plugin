/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class CloudPanelGUI {
    public JTabbedPane tabbedPane;
    private JPanel rootPanel;
    private JPanel configPanel;
    private JPanel advancedPanel;
    private CloudConfigFileGUI cloudConfigFileGUI;

    public CloudPanelGUI(Project project) {
        cloudConfigFileGUI = new CloudConfigFileGUI(project);
        configPanel.add(cloudConfigFileGUI.getPanel());
    }

    public void setAdvancePanel(JComponent component) {
        advancedPanel.add(component);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }

    public void apply() throws ConfigurationException {
        cloudConfigFileGUI.apply();
    }

    public String getNameAndUrl() {
        return cloudConfigFileGUI.getNameAndUrl();
    }


}
