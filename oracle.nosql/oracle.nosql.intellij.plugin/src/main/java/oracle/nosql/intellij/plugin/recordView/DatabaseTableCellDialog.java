/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

class DatabaseTableCellDialog extends DialogWrapper {
    private final Project project;
    private final String textToDisplay;

    public DatabaseTableCellDialog(Project project,String text) {
        super(project, true);
        this.project = project;
        this.textToDisplay = text;
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return new DatabaseCellTextField(JsonLanguage.findInstance(JsonLanguage.class), project, textToDisplay, false);
    }
}
