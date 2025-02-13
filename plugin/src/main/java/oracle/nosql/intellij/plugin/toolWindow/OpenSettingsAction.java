/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import oracle.nosql.intellij.plugin.settings.MainSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Opens setting dialog when settings icon is clicked in tool window.
 *
 * @author amsundar
 */
@SuppressWarnings("WeakerAccess")
public class OpenSettingsAction extends AnAction {
     /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtilImpl.getInstance().showSettingsDialog(e.getProject(), MainSettings.class);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
