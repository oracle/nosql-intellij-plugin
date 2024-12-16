/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

/**
 * RecordViewer provides editor for {@link DataBaseVirtualFile}
 *
 * @author amsundar
 */
class RecordViewer implements FileEditor {
    private final TableView UIComponent;
    DataBaseVirtualFile dbFile;

    RecordViewer(Project project,DataBaseVirtualFile file) {
        UIComponent = new TableView(project,file);
        this.dbFile = file;
    }

    /**
     * @return Query window
     */
    @NotNull
    @Override
    public JComponent getComponent() {
        return UIComponent.getComponent();
    }


    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return UIComponent.getComponent();
    }

    @NotNull
    @Override
    public String getName() {
        return "TableViewer";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }


    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }


    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return dbFile.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        dbFile.putUserData(key,value);
    }

    @Override
    public DataBaseVirtualFile getFile(){
        return dbFile;
    }
}
