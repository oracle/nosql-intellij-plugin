/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.Map;

class GeneralSettingsGUI {
    private JPanel mainPanel;
    private JTextField rowField;
    private Project project;
    private ConnectionDataProviderService conService;

    public JComponent getComponent(@NotNull Project project, @NotNull ConnectionDataProviderService service) {
        conService = service;
        this.project = project;
        String rowSize = service.getValue(ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE);
        if(rowSize == null) {
            rowSize = "20";
            service.putValue(ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE,rowSize);
        }
        rowField.setText(rowSize);
        return mainPanel;
    }


    public boolean isModified() {
        return !rowField.getText().equals(conService.getValue(
                ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE));
    }

    public void apply() throws ConfigurationException {
        validate();
        conService.putValue(ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE,rowField.getText());

        // Update PAGE_SIZE in all multiple-connection states
        MultipleConnectionsDataProviderService multi =
                MultipleConnectionsDataProviderService.getInstance(project);

        MultipleConnectionsDataProviderService.State multiState = multi.getState();
        if (multiState == null) {
            return;
        }

        for (Map.Entry<String, ConnectionDataProviderService.State> entry : multiState.dict.entrySet()) {
            ConnectionDataProviderService.State perConnectionState = entry.getValue();
            perConnectionState.dict.put(
                    ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE,
                    rowField.getText()
            );
        }
    }

    private void validate() throws ConfigurationException{
        int rowSize =0;
        try {
            rowSize = Integer.parseInt(rowField.getText());
        } catch(Exception ex) {
            throw new ConfigurationException("Please enter an integer between 5-100");
        }
        if(!(rowSize>=5 && rowSize<=100)) {
            throw new ConfigurationException("Please enter an integer between 5-100");
        }
    }

    public void reset() {
        rowField.setText(conService.getValue(ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE));
    }
}
