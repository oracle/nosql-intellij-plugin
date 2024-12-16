/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.openapi.options.ConfigurationException;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

class GeneralSettingsGUI {
    private JPanel mainPanel;
    private JTextField rowField;
    private ConnectionDataProviderService conService;

    public JComponent getComponent(@NotNull ConnectionDataProviderService service) {
        conService = service;
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
