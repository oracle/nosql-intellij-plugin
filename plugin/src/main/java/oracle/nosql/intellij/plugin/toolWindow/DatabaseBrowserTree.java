/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.NamedQuery;
import oracle.nosql.intellij.plugin.recordView.DataBaseVirtualFile;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.Table;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Schema tree view which contains Tables.
 *
 * @author amsundar
 */
class DatabaseBrowserTree extends JTree {
    private final Project project;

    public DatabaseBrowserTree(BrowserTreeModel model, Project project) {
        super(model);
        this.setCellRenderer(new DatabaseBrowserTreeCellRenderer(project));
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    //open record view when table in tree is double clicked
                    if (event.getClickCount() > 1) {
                        TreePath path = getPathForLocation(event.getX(), event.getY());
                        processSelectEvent(event, path, event.getClickCount() > 1);
                    }
                }
            }

            public void mouseReleased(final MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    final TreePath path = getPathForLocation(event.getX(), event.getY());
                    if (path != null) {
                        Object lastPathEntity = path.getLastPathComponent();
                        //open context menu on right click in table node
                        if (lastPathEntity instanceof Table) {
                            ActionGroup actionGroup = new TableNodeContextMenuActionGroup((Table) lastPathEntity,project);
                            ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                            JPopupMenu popupMenu = actionPopupMenu.getComponent();
                            popupMenu.show(DatabaseBrowserTree.this, event.getX(), event.getY());
                        } else if (lastPathEntity instanceof Schema) {
                            ActionGroup actionGroup = new SchemaNodeContextMenuActionGroup(project, (Schema) lastPathEntity);
                            ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                            JPopupMenu popupMenu = actionPopupMenu.getComponent();
                            popupMenu.show(DatabaseBrowserTree.this, event.getX(), event.getY());
                        } else if (lastPathEntity instanceof Field) {
                            ActionGroup actionGroup = new FieldNodeContextMenuActionGroup((Field) lastPathEntity);
                            ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                            JPopupMenu popupMenu = actionPopupMenu.getComponent();
                            popupMenu.show(DatabaseBrowserTree.this, event.getX(), event.getY());
                        } else if (lastPathEntity instanceof FieldGroup) {
                            ActionGroup actionGroup = new FieldGroupNodeContextMenuActionGroup((FieldGroup) lastPathEntity);
                            ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                            JPopupMenu popupMenu = actionPopupMenu.getComponent();
                            popupMenu.show(DatabaseBrowserTree.this, event.getX(), event.getY());
                        }

                    }
                }
            }

        };
        addMouseListener(mouseListener);
        this.project = project;
    }

    @SuppressWarnings("unused")
    private void processSelectEvent(InputEvent event, TreePath path, boolean deliberate) {
        if (path != null) {
            Object lastPathEntity = path.getLastPathComponent();
            if (lastPathEntity instanceof Table) {
                Table table = (Table) lastPathEntity;
                NamedQuery query = new NamedQuery(table);
                FileEditorManager mgr = FileEditorManager.getInstance(project);
                DataBaseVirtualFile vFile = new DataBaseVirtualFile(table);//,PlainTextFileType.INSTANCE, "file content");
                mgr.openFile(vFile, true);
            }
        }
    }
}
