/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBPanel;
import oracle.nosql.intellij.plugin.common.MyHelpProvider;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.BorderLayout;
/**
 * Entry point for creating new settings page in project settings(ctrl+alt+s)
 * creates main settings page for plugin.
 *
 * @author amsundar
 */
public class MainSettings implements Configurable, Disposable {
    private JBPanel panel;
    @Nullable
    @Override
    public String getHelpTopic() {
        return MyHelpProvider.HELP_PREFIX;
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
        return OracleNoSqlBundle.message("oracle.nosql.settings.main.name");
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
        panel = new JBPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel(OracleNoSqlBundle.message("oracle.nosql.settings.mainPage")),BorderLayout.NORTH);
        return panel;
    }

    @Override
    public void disposeUIResources() {
        Disposer.dispose(this);
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
    @SuppressWarnings("RedundantThrows")
    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void dispose() {
        panel = null;
    }
}

