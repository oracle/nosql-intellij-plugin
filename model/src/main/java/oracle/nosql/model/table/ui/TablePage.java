/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.table.ui;

import java.util.List;

/**
 * This interface represents the page associated with the UI.
 * 
 * @author Jashkumar Dave
 *
 */
public interface TablePage {
    /**
     * Returns number of rows in this page.
     * 
     * @return number of rows.
     */
    public int getRowCount();

    /**
     * Returns rows present in this page.
     * 
     * @return list of TableRows in this page
     */
    public List<TableRow> getRows();

    /**
     * Returns the row specified by index and null if index doesn't exists.
     * 
     * @param index
     * @return the row specified by index and null if index doesn't exists.
     */
    public TableRow getRow(int index);

    /**
     * Adds the given row to this page.
     * 
     * @param row
     * @return true if the operation was successful otherwise false.
     */
    public boolean addRow(TableRow row);
}
