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
import oracle.nosql.intellij.plugin.common.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class AllConnectionsSetting implements Configurable, Disposable {
    private final Project mProject;
    private final MultipleConnectionsDataProviderService mService;
    private AllConnectionsSettingGUI mGUI;

    public AllConnectionsSetting(@NotNull Project project) {
        mProject = project;
        mService = MultipleConnectionsDataProviderService.getInstance(project);
        mGUI = new AllConnectionsSettingGUI(mService, mProject);
    }

    /**
     * Returns the visible name of the configurable component.
     * Note, that this method must return the display name
     * that is equal to the display name declared in XML
     * to avoid unexpected errors.
     *
     * @return the visible name of the configurable component
     */
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return OracleNoSqlBundle.message("oracle.nosql.settings.allConnections.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return mGUI.getHelpTopic();
    }

    /**
     * Creates new Swing form that enables user to configure the settings.
     * Usually this method is called on the EDT, so it should not take a long time.
     * <p>
     * Also this place is designed to allocate resources (subscriptions/listeners etc.)
     *
     * @return new Swing form to show, or {@code null} if it cannot be created
     * @see #disposeUIResources
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        mGUI = new AllConnectionsSettingGUI(mService, mProject);
        return mGUI.createPanel();
    }

    /**
     * Indicates whether the Swing form was modified or not.
     * This method is called very often, so it should not take a long time.
     *
     * @return {@code true} if the settings were modified, {@code false} otherwise
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Stores the settings from the Swing form to the configurable component.
     * This method is called on EDT upon user's request.
     *
     * @throws ConfigurationException if values cannot be applied
     */
    @Override
    public void apply() throws ConfigurationException {
        mGUI.apply();
        ConnectionManagerListener publisher = mProject.getMessageBus().syncPublisher(ConnectionManagerListener.TOPIC);
        publisher.connectionsChanged();
    }

    /**
     * Notifies the configurable component that the Swing form will be closed.
     * This method should dispose all resources associated with the component.
     */
    @Override
    public void disposeUIResources() {
        Disposer.dispose(this);
    }

    /**
     * Loads the settings from the configurable component to the Swing form.
     * This method is called on EDT immediately after the form creation or later upon user's request.
     */
    @Override
    public void reset() {
    }

    @Override
    public void dispose() {
        mGUI = null;
    }
}