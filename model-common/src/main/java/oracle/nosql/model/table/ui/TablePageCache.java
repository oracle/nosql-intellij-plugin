/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.table.ui;

import java.util.List;

import oracle.nosql.model.schema.Table;

/**
 * This interface represents the page cache for the UI.
 * 
 * @author Jashkumar Dave
 */
public interface TablePageCache {
    /**
     * @return count of the column in these table pages.
     */
    public int getColumnCount();

    /**
     * @return the TablePage associated with currently displayed UI page.
     */
    public TablePage getCurrentPage();

    /**
     * @return the pages in the cache.
     */
    public List<TablePage> getPages();

    /**
     * Moves to next page.
     * 
     * @return true if the operation was successful otherwise false.
     * @throws Exception
     */
    public boolean nextPage() throws Exception;

    /**
     * Moves to next page.
     *
     * @return true if the operation was successful otherwise false.
     * @throws Exception
     */
    public boolean nextPage(boolean isJsonCollection) throws Exception;

    /**
     * Moves to previous page.
     * 
     * @return true if the operation was successful otherwise false.
     */
    public boolean prevPage();

    /**
     * @return true if next page exists otherwise false.
     * @throws Exception
     */
    public boolean hasNextPage() throws Exception;

    /**
     * @return true if previous page exists otherwise false.
     */
    public boolean hasPrevPage();

    /**
     * @return Table associated with this page cache.
     */
    public Table getTable();

    /**
     * @return current page number.
     */
    public int getCurrentPageNumber();

    List<String> getColumnHeaders();

    public void setResult(Object result, Table table);

    void setPageSize(int pageSize);
}
