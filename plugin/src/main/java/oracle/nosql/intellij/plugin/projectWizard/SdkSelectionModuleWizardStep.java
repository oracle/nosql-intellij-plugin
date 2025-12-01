/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.projectWizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import oracle.nosql.intellij.plugin.common.MyHelpProvider;
import org.jetbrains.annotations.NonNls;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.File;

@SuppressWarnings({"DialogTitleCapitalization", "unused"})

public class SdkSelectionModuleWizardStep extends ModuleWizardStep {
    private JPanel mainPanel;
    @NonNls
    private TextFieldWithBrowseButton sdkTextField;
    private JLabel sdkLabel;
    private final NoSqlModuleBuilder myBuilder;

    public SdkSelectionModuleWizardStep(NoSqlModuleBuilder builder) {
        myBuilder= builder;
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false,
                false, false, false);
        sdkTextField.addBrowseFolderListener(null,fileChooserDescriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if(!isValidNoSQLSdk(sdkTextField.getText())) {
            throw new ConfigurationException("<html>Invalid SDK path. <p>Please check for setting up Plugin for <a href=https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=setup_intellij_plugin>Oracle NoSQL Database Cloud Service</a></p><p>and <a href=https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=setup_intellij_onprem> Oracle NoSQL Database.</a></p></html>");
        }
        return true;
    }
    private boolean isValidNoSQLSdk(String path) {
        @NonNls VirtualFile home = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
        if (home != null && home.exists() && home.isDirectory()) {
            VirtualFile example = home.findChild("examples");
            @SuppressWarnings("ConstantConditions") VirtualFile lib = home.findChild("lib");
            return lib != null && lib.isDirectory() && example != null && example.isDirectory();
        }
        return false;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void updateDataModel() {
       //update sdk path from GUI to builder
        myBuilder.setNosqlSDKPath(sdkTextField.getText());
    }

    @Override
    public String getHelpId() {
        return MyHelpProvider.HELP_PREFIX + "." + MyHelpProvider.PROJECT_HELP_ID;
    }
}
