/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import oracle.nosql.model.table.ui.TablePageCache;
import oracle.nosql.model.table.ui.TableRow;

import javax.swing.table.AbstractTableModel;

/**
 * Model for Database Table.
 *
 * @author amsundar
 */
@SuppressWarnings({"SameParameterValue", "unused"})
class DataBaseTableModel extends AbstractTableModel {
    private TablePageCache pageCache;

    DataBaseTableModel(TablePageCache pageCache) {
        super();
        this.pageCache = pageCache;
    }

    public TablePageCache getPageCache() {
        return this.pageCache;
    }
    /**
     * This will be called when table structure changes
     * @param pageCache Table  page
     */
    void setPageCache(TablePageCache pageCache) {
        this.pageCache = pageCache;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        if(pageCache != null) {
            return pageCache.getCurrentPage().getRowCount();
        }
        return 0;
    }

    @Override
    public int getColumnCount() {
        if(pageCache != null) {
            return pageCache.getColumnCount();
        }
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(pageCache != null) {
            TableRow row = pageCache.getCurrentPage().getRow(rowIndex);
            return row.getData(columnIndex).toString();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        if(pageCache != null) {
            return pageCache.getColumnHeaders().get(column);
        }
        return column+Integer.toString(column);
    }


    public void refresh(TablePageCache pageCache) {
        this.pageCache = pageCache;
        fireTableDataChanged();
    }
    public boolean isPrimaryKey(int column) {
        return pageCache.getTable().getFieldAt(column).isPrimaryKey();
    }

    public String getHeaderToolTip(int column) {
        return pageCache.getTable().getFieldAt(column).getType().toString();
    }
}
