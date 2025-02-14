/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * refresh schema tree when refresh icon is clicked in tool window.
 *
 * @author amsundar
 */
class RefreshAction extends AnAction {
    @Override
    public boolean isDumbAware() {
        return true;
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseBrowserManager.getInstance(Objects.requireNonNull(e.getProject())).getToolWindowForm().refresh();
    }
}
