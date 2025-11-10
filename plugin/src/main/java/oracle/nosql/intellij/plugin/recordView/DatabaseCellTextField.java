/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.find.EditorSearchSession;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

/**
 * Class responsible for opening a dialog when table cell is clicked and format in JSON.
 * Implementation is copied from {@link com.intellij.ui.LanguageTextField}
 *
 * @author amsundar
 */

@SuppressWarnings("WeakerAccess")
public class DatabaseCellTextField extends EditorTextField {
    private final Language myLanguage;
    private final Project myProject;

    public DatabaseCellTextField(Language language, @Nullable Project project, @NotNull String value, boolean oneLineMode) {
        this(language, project, value, new DatabaseCellTextField.SimpleDocumentCreator(), oneLineMode);
    }

    public DatabaseCellTextField(@Nullable Language language,
                                 @Nullable Project project,
                                 @NotNull String value,
                                 @NotNull DocumentCreator documentCreator,
                                 boolean oneLineMode) {
        super(documentCreator.createDocument(value, language, project), project,
                language != null ? language.getAssociatedFileType() : FileTypes.PLAIN_TEXT, true, oneLineMode);

        myLanguage = language;
        myProject = project;

        setEnabled(language != null);
    }

    public interface DocumentCreator {
        Document createDocument(String value, @Nullable Language language, Project project);
    }

    public static class SimpleDocumentCreator implements DatabaseCellTextField.DocumentCreator {
        @Override
        public Document createDocument(String value, @Nullable Language language, Project project) {
            return DatabaseCellTextField.createDocument(value, language, project, this);
        }

        public void customizePsiFile(Project project, PsiFile file) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                CodeStyleManager.getInstance(project).reformat(file);
            });

        }
    }

    public static Document createDocument(String value, @Nullable Language language, @Nullable Project project,
                                          @NotNull DatabaseCellTextField.SimpleDocumentCreator documentCreator) {
        if (language != null) {
            if (project == null) {
                project = ProjectManager.getInstance().getDefaultProject();
            }
            final PsiFileFactory factory = PsiFileFactory.getInstance(project);
            final FileType fileType = language.getAssociatedFileType();
            assert fileType != null;

            final long stamp = LocalTimeCounter.currentTime();
            final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, value, stamp, true, false);
            documentCreator.customizePsiFile(project, psiFile);
            final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
            assert document != null;
            return document;
        } else {
            return EditorFactory.getInstance().createDocument(value);
        }
    }

    @Override
    protected EditorEx createEditor() {
        final EditorEx ex = super.createEditor();
        ex.setVerticalScrollbarVisible(true);
        ex.setHorizontalScrollbarVisible(true);

        if (myLanguage != null) {
            final FileType fileType = myLanguage.getAssociatedFileType();
            //noinspection ConstantConditions
            ex.setHighlighter(HighlighterFactory.createHighlighter(myProject, fileType));

            // Fold JSON objects
            if (myLanguage.getID().equals("JSON")) {
                ex.getSettings().setFoldingOutlineShown(true);
                ex.getFoldingModel().runBatchFoldingOperation(() -> {
                    String text = ex.getDocument().getText();
                    Stack<Integer> openBracketIndexes = new Stack<>();
                    for (int i = 0; i < text.length(); i++) {
                        if (text.charAt(i) == '{') {
                            openBracketIndexes.push(i);
                        } else if (text.charAt(i) == '}') {
                            if (!openBracketIndexes.isEmpty()) {
                                int start = openBracketIndexes.pop();
                                if (openBracketIndexes.size() >= 2) {
                                    FoldRegion region = ex.getFoldingModel().addFoldRegion(start, i+1, "{...}");
                                    if (region != null) {
                                        region.setExpanded(false);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

//        ApplicationManager.getApplication().invokeLater(() -> {
//            if (!ex.isDisposed() && myLanguage.getID().equals("JSON")) {
//                EditorSearchSession.start(ex, myProject);   // shows the Search palette
//            }
//        });

        ex.setEmbeddedIntoDialogWrapper(true);
        return ex;
    }
}
