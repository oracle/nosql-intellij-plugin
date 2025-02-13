/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.table.ui;

import java.util.List;

import oracle.nosql.model.table.ui.TablePage;
import oracle.nosql.model.table.ui.TableRow;

/**
 * Implementation of TablePage for Cloudsim
 * 
 * @author Jashkumar Dave
 *
 */
public class CloudTablePage implements TablePage {
    private List<TableRow> rows;

    public CloudTablePage(List<TableRow> rows) {
        this.rows = rows;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public List<TableRow> getRows() {
        return rows;
    }

    @Override
    public TableRow getRow(int index) {
        return rows.get(index);
    }

    @Override
    public boolean addRow(TableRow row) {
        // TODO Auto-generated method stub
        return false;
    }
}
