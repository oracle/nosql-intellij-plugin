/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import oracle.nosql.model.schema.Field;

import javax.swing.JComponent;

class DatabaseTableCellDialog extends DialogWrapper {
    private final Project project;
    private final String textToDisplay;
    private final Field.Type fieldType;
    private DatabaseCellTextField textField;

    public DatabaseTableCellDialog(Project project, String text, Field.Type type) {
        super(project, true);
        this.project = project;
        this.textToDisplay = text;
        this.fieldType = type;
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        textField = new DatabaseCellTextField(JsonLanguage.findInstance(JsonLanguage.class), project, textToDisplay, false);
        return textField;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        if (fieldType.equals(Field.Type.JSON)) {
            return textField; // for JSON Dialog, initial focus should be on the JSON Editor
        }
        return null;
    }
}
