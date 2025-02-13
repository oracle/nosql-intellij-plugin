/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Table;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Table which is presented to user.
 *
 * @author amsundar
 */
class DatabaseTable extends JBTable {
    public DatabaseTable(Project project,DataBaseTableModel model) {
        super(model);
        // setCellSelectionEnabled(false);
        setRowSelectionAllowed(true);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    final JTable jTable = (JTable) e.getSource();
                    final int row = jTable.rowAtPoint(e.getPoint());
                    final int column = jTable.columnAtPoint(e.getPoint());
                    if(row>jTable.getRowCount() || column>jTable.getColumnCount() || row ==-1 || column==-1)
                        return;
                    final String valueInCell = (String) jTable.getValueAt(row, column);
                    //create dialog and show contents
                    Table table = model.getPageCache().getTable();
                    String bin = jTable.getColumnName(jTable.columnAtPoint(e.getPoint()));
                    boolean binField = isBinField(table, bin);
                    if (!binField) {
                        DatabaseTableCellDialog dialog = new DatabaseTableCellDialog(project, valueInCell);
                        dialog.show();
                    }
                }
                //If the cell is clicked one time, it is a binary field, then it calls the method
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    final JTable jTable = (JTable) e.getSource();
                    Table table = model.getPageCache().getTable();
                    String bin = jTable.getColumnName(jTable.columnAtPoint(e.getPoint()));
                    final int row = jTable.rowAtPoint(e.getPoint());
                    final int column = jTable.columnAtPoint(e.getPoint());
                    if(row>jTable.getRowCount() || column>jTable.getColumnCount() || row ==-1 || column==-1)
                        return;
                    final String valueInCell = (String) jTable.getValueAt(row, column);
                    boolean binField = isBinField(table, bin);
                    if (binField && valueInCell!=null &&  !valueInCell.isEmpty()) {
                        new DownloadBinaryObject(jTable, e, project, table);
                    }
                }
            }

            public void mouseReleased(final MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    final JTable jTable = (JTable) event.getSource();
                    Table table = model.getPageCache().getTable();
                    //open context menu on right click in table node
                    ActionGroup actionGroup = new TableRowNodeContextMenuActionGroup(table, jTable, project);
                    ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                    JPopupMenu popupMenu = actionPopupMenu.getComponent();
                    popupMenu.show(DatabaseTable.this, event.getX(), event.getY());
                }
            }

            private boolean isBinField(Table table, String bin) {
                boolean binField = false;
                Set<String> binSet = new HashSet<>();
                for (int i = 0; i < table.getFieldCount(); i++) {
                    String name = table.getFields().get(i).getName();
                    Field.Type type = table.getFields().get(i).getType();
                    if (type.equals(Field.Type.BINARY) || type.equals(Field.Type.FIXED_BINARY)) {
                        binSet.add(name);
                    }
                }
                if (binSet.contains(bin))
                    binField = true;
                return binField;
            }
        });
    }

    /*@NotNull
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JBTableHeader() {
            @Override
            public String getToolTipText(@NotNull MouseEvent event) {
                DataBaseTableModel model = (DataBaseTableModel) getModel();
                final int i = columnAtPoint(event.getPoint());
                final int infoIndex = i >= 0 ? convertColumnIndexToModel(i) : -1;
                final String tooltipText = infoIndex >= 0 && infoIndex <model.getColumnCount() ?model.getHeaderToolTip(infoIndex):null;
                if(tooltipText != null) {
                    return tooltipText;
                }
                return super.getToolTipText(event);
            }
//            public String getToolTipText(MouseEvent e) {
//                String tip = null;
//                Point p = e.getPoint();
//                int index = columnModel.getColumnIndexAtX(p.x);
//                if(index == -1) { return "";}
//                int realIndex = columnModel.getColumn(index).getModelIndex();
//                DataBaseTableModel model = (DataBaseTableModel) getModel();
//                return model.getHeaderToolTip(realIndex);
//            }
        };
    }*/
}
