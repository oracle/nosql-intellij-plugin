/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import java.util.HashSet;
import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Index;
import oracle.nosql.model.schema.SchemaContainer;
import oracle.nosql.model.schema.Table;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Set;

/**
 * Treemodel implementation for schema tree.
 *
 * @author amsundar
 */
@SuppressWarnings({"SameParameterValue", "SpellCheckingInspection"})
class BrowserTreeModel implements TreeModel {

    private final Set<TreeModelListener> treeModelListeners = new HashSet<>();
    private Datamodel root;

    public BrowserTreeModel(Datamodel root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        if (root == null) return null;
        return root.getChildAt(0);
    }

    @SuppressWarnings("unused")
    public void setRoot(Datamodel root) {
        Object oldRoot = this.root;
        this.root = root;
        fireTreeStructureChanged(this, null);
    }

    @SuppressWarnings("unused")
    public void reload() {
        fireTreeStructureChanged(this,null);
    }

    @Override
    public Object getChild(Object parent, int index)  {
       if(parent instanceof SchemaContainer) {
           if(parent instanceof Table) {
               Table table = (Table) parent;
               List<Index> indexes = table.getIndexes();
               int fieldCount = table.getFieldCount();
               int idxCount = indexes.size();
               if (index < fieldCount) {
                   return table.getChildAt(index);
               } else if (index < (fieldCount + idxCount)) {
                   return indexes.get(index - fieldCount);
               } else if (index < (fieldCount + idxCount + 1)) {
                   return table.getPrimaryKeys();
               } else {
                   return table.getShardKeys();
               }
           } else {
               return ((SchemaContainer) parent).getChildAt(index);
           }
       } else {
           return null;
       }
    }

    @Override
    public int getChildCount(Object parent) {
        if(parent instanceof  SchemaContainer) {
            if(parent instanceof Table) {
                Table table = (Table) parent;
                return table.getChildCount()+table.getIndexes().size()+2;
            }
            return ((SchemaContainer) parent).getChildCount();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof Field;
    }


    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }


    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        treeModelListeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        treeModelListeners.remove(listener);
    }

    private void fireTreeStructureChanged(Object source, TreePath path)
    {
        TreeModelEvent event = new TreeModelEvent(source,path);
        for (TreeModelListener treeModelListener : treeModelListeners) {
            treeModelListener.treeStructureChanged(event);
        }
    }
}
