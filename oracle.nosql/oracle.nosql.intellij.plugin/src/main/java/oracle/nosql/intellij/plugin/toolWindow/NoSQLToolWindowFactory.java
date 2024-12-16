/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactoryImpl;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point for creating toolwindow.
 *
 * @author amsundar
 */
@SuppressWarnings("WeakerAccess")
public class NoSQLToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        NoSQLToolWindow toolWindowForm = DatabaseBrowserManager.getInstance(project).getToolWindowForm();
        ContentFactory contentFactory = new ContentFactoryImpl();
        Content content = contentFactory.createContent(toolWindowForm, null, false);
        toolWindow.getContentManager().addContent(content);
    }
}
