/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;

/**
 * Invokes the JECacheSizingPanel class
 *
 * @author lsatpal
 */
public class SizingToolAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SwingUtilities.invokeLater(() -> new JECacheSizingPanel(e.getProject()));
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
