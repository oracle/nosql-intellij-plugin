/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService;
import oracle.nosql.model.connection.ConfigurableProperty;
import oracle.nosql.model.connection.ConnectionFactory;
import oracle.nosql.model.connection.IConnectionProfileType;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Class for providing the support of adding cloud connection through cloud config file
 *
 * @author kunalgup
 */
public class CloudConfigFileGUI {
    private JPanel rootPanel;
    private TextFieldWithBrowseButton configfield;
    private JTextField profile;
    private JTextField endpoint;
    private JTextField compartment;
    private JTextField connectionName;
    private List<JComponent> componentList;
    private String tenantId;
    private String userId;
    private String fingerPrint;
    private String privateKey;
    private String inputProfile;
    private Project project;
    private String passphrase;

    public CloudConfigFileGUI(Project project) {
        this.project = project;
        rootPanel.add(createCloudConfigPanel());
    }

    private JPanel createCloudConfigPanel() {

        int i = 3;
        JPanel panel = new JPanel();
        panel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow", // Column constraints
                "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow," + // Row constraints up to row 17
                        "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," + "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," + "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," + "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," + "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," + // Row 18
                        "center:max(d;4px):noGrow" // Row 19
        ));
        CellConstraints cc = new CellConstraints();
        final Spacer spacer1 = new Spacer();
        panel.add(spacer1, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        componentList = new ArrayList<>();

        connectionName = new JTextField("");
        JLabel connectionLabel = new JLabel("Connection Name *");
        connectionName.setName("Connection Name");
        panel.add(connectionLabel, cc.xy(1, i, CellConstraints.LEFT, CellConstraints.FILL));
        panel.add(connectionName, cc.xy(3, i, CellConstraints.FILL, CellConstraints.FILL));
        componentList.add(connectionName);

        endpoint = new JTextField("");
        JLabel endpointLabel = new JLabel("Endpoint *");
        endpoint.setName("Endpoint");
        panel.add(endpointLabel, cc.xy(1, i + 2, CellConstraints.LEFT, CellConstraints.FILL));
        panel.add(endpoint, cc.xy(3, i + 2, CellConstraints.FILL, CellConstraints.FILL));
        componentList.add(endpoint);

        configfield = new TextFieldWithBrowseButton(new JTextField(""));
        configfield.setName("Config Path");
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true, false, false, false, false);
        JLabel configLabel = new JLabel("Configuration File *");
        configfield.addBrowseFolderListener("", "Configuration File", null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        panel.add(configLabel, cc.xy(1, i + 6, CellConstraints.LEFT, CellConstraints.FILL));
        panel.add(configfield, cc.xy(3, i + 6, CellConstraints.FILL, CellConstraints.FILL));

        componentList.add(configfield);

        profile = new JTextField("DEFAULT");
        JLabel profileLabel = new JLabel("Profile *");
        profile.setName("Profile");
        panel.add(profileLabel, cc.xy(1, i + 8, CellConstraints.LEFT, CellConstraints.FILL));
        panel.add(profile, cc.xy(3, i + 8, CellConstraints.FILL, CellConstraints.FILL));
        componentList.add(profile);


        compartment = new JTextField("");
        JLabel compartmentLabel = new JLabel("Compartment");
        compartment.setName(compartmentLabel.getText());
        panel.add(compartmentLabel, cc.xy(1, i + 10, CellConstraints.LEFT, CellConstraints.FILL));
        panel.add(compartment, cc.xy(3, i + 10, CellConstraints.FILL, CellConstraints.FILL));
        componentList.add(compartment);

        return panel;
    }

    public void apply() throws ConfigurationException {
        validate();
        List<String> data = getData();
        ConnectionDataProviderService conService = new ConnectionDataProviderService();
        IConnectionProfileType profileType = ConnectionFactory.getProfileTypeByName("Cloud");
        int i = 0;
        for (ConfigurableProperty property : profileType.getRequiredProperties()) {
            String prefKey = ConnectionDataProviderService.getKeyForProperty(profileType, property);
            conService.putValue(prefKey, data.get(i));
            i++;
        }
        conService.putValue(ConnectionDataProviderService.KEY_PROFILE_TYPE, "Cloud");
        String comp = compartment.getText();
        String UID = endpoint.getText();
        if (comp != null && !comp.isEmpty()) UID += " : " + comp;
        MultipleConnectionsDataProviderService mConService = MultipleConnectionsDataProviderService.getInstance(project);
        assert endpoint.getText() != null;
        mConService.putValue(UID, conService);
        mConService.putUidToType(UID, "Cloud");
        Map<String, String> nameToUidMap = mConService.getNameToUidMap();
        if (!nameToUidMap.containsKey(connectionName.getText()))
            mConService.putNameToUid(connectionName.getText(), UID);
        else {
            String error = "Connection name already exists!";
            throw new ConfigurationException(error);
        }
    }

    private void validate() throws ConfigurationException {
        if (endpoint.getText().isEmpty()) {
            String error = "Endpoint cannot be empty!";
            throw new ConfigurationException(error);
        } else if (connectionName.getText().isEmpty()) {
            String error = "Connection Name cannot be empty!";
            throw new ConfigurationException(error);
        }


    }

    private List<String> getData() throws ConfigurationException {
        List<String> data = new ArrayList<>();

        tenantId = "";
        userId = "";
        fingerPrint = "";
        privateKey = "";
        passphrase = "";
        inputProfile = profile.getText();
        Map<String, String> fileData = parseConfig(configfield.getText(), inputProfile);

        for (Map.Entry<String, String> e : fileData.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (key.equals("user")) userId = value;
            else if (key.equals("fingerprint")) fingerPrint = value;
            else if (key.equals("key_file")) privateKey = value;
            else if (key.equals("tenancy")) tenantId = value;
            else if (key.equals("pass_phrase")) passphrase = value;
        }
        if (tenantId.isEmpty() || userId.isEmpty() || fingerPrint.isEmpty() || privateKey.isEmpty()) {
            String error = "Error in Config file!";
            throw new ConfigurationException(error);
        }
        data.add(connectionName.getText());
        data.add(endpoint.getText());
        data.add(tenantId);
        data.add(userId);
        data.add(fingerPrint);
        data.add(privateKey);

        data.add(passphrase);
        data.add(compartment.getText());
        return data;
    }

    private Map<String, String> parseConfig(String filePath, String section) {
        Map<String, String> sectionConfig = new HashMap<>();
        String currentSection = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    if (section.equals(currentSection)) {
                        sectionConfig.clear(); // Clear previous section's data
                    }
                } else if (line.contains("=") && currentSection != null && currentSection.equals(section)) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    sectionConfig.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sectionConfig;
    }

    public JComponent getPanel() {
        return rootPanel;
    }

    public String getNameAndUrl() {
        return connectionName.getText() + " : " + endpoint.getText();
    }
}
