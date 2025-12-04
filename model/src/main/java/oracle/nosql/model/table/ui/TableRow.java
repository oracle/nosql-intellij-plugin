/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.table.ui;

import java.util.List;

/**
 * This interface represents the UI row for the table.
 * 
 * @author Jashkumar Dave
 *
 */
public interface TableRow {
    /**
     * Gets the data of column specified by index.
     * 
     * @param index - column index
     * @return column data specified by index or null if column doesn't exists.
     */
    public Object getData(int index);

    /**
     * @return list of data associated with columns of this row.
     */
    public List<Object> getData();

    /**
     * @return raw object containing the data.
     */
    public Object getRawData();
}
