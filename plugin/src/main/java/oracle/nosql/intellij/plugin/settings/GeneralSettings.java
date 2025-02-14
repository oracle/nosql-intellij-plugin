/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.MyHelpProvider;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
/**
 * Entry point for creating new settings page in project settings(ctrl+alt+s)
 * creates general setting page.
 *
 * @author amsundar
 */
class GeneralSettings implements Configurable, Disposable {
    private final ConnectionDataProviderService mService;
    private GeneralSettingsGUI mGUI;

    public GeneralSettings(Project project) {
        mService = ConnectionDataProviderService.getInstance(project);
    }

    @Override
    public void reset() {
        mGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        Disposer.dispose(this);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return OracleNoSqlBundle.message("oracle.nosql.settings.general.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return MyHelpProvider.HELP_PREFIX + "." + MyHelpProvider.GENERAL_HELP_ID ;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mGUI = new GeneralSettingsGUI();
        return mGUI.getComponent(mService);
    }

    @Override
    public boolean isModified() {
        return mGUI.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        mGUI.apply();
    }

    @Override
    public void dispose() {
        mGUI = null;
    }
}
