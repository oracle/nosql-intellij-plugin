/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.table.ui;

import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePage;
import oracle.nosql.model.table.ui.TablePageCache;
import oracle.nosql.model.table.ui.TableRow;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TablePageCache for Cloudsim
 * 
 * @author Jashkumar Dave
 *
 */
public class CloudTablePageCache implements TablePageCache {
    private Iterator<MapValue> result;
    private int pageSize = 10;
    private int currentPage = 0;
    private List<TablePage> pages;
    private Table table;

    public CloudTablePageCache() {
    }

    @Override
    public boolean hasNextPage() throws Exception {
        if (currentPage < pages.size()) {
            return true;
        }
        return result.hasNext();
    }

    @Override
    public boolean hasPrevPage() {
        if (currentPage > 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean nextPage() throws Exception {
        boolean ret = hasNextPage();
        if (!ret)
            return false;
        currentPage++;
        if (currentPage > pages.size()) {
            List<TableRow> rows = new LinkedList<TableRow>();
            TablePage page = new CloudTablePage(rows);
            for (int i = 0; i < pageSize && result.hasNext(); i++) {
                MapValue row = result.next();
                rows.add(new CloudTableRow(row, table, page));
            }
            pages.add(page);
        }
        return true;
    }

    @Override
    public boolean nextPage(boolean isJsonCollection) throws Exception {
        if(!isJsonCollection)
            return nextPage();

        boolean ret = hasNextPage();
        if (!ret)
            return false;
        currentPage++;
        if (currentPage > pages.size()) {
            List<TableRow> rows = new LinkedList<TableRow>();
            TablePage page = new CloudTablePage(rows);
            FieldGroup pkGroup = table.getPrimaryKeys();
            List<Field> pkList = pkGroup.getFields();
            
            for (int i = 0; i < pageSize && result.hasNext(); i++) {
                MapValue row = result.next();
                MapValue transformedRow = getTransformedEntries(row, pkList);
                rows.add(new CloudTableRow(transformedRow, table, page));
            }
            pages.add(page);
        }
        return true;
    }

    private static MapValue getTransformedEntries(@NotNull MapValue row, List<Field> pkList) {
        Map<String, FieldValue> rowMap = row.getMap();
        MapValue rowData = new MapValue();
        MapValue transformedRow = new MapValue(true, 4);

        for (Map.Entry<String, FieldValue> entry : rowMap.entrySet()) {
            String key = entry.getKey();
            FieldValue value = entry.getValue();
            boolean isPrimaryKey = false;
            for (int j = 0; j < pkList.size(); j++) {
                Field currPkField = pkList.get(j);
                if (key.equals(currPkField.getName())) {
                    transformedRow.put(key, value);
                    isPrimaryKey = true;
                }
            }
            if(!isPrimaryKey)
                rowData.put(key, value);
        }

        transformedRow.put("Rowdata", rowData);
        return transformedRow;
    }

    @Override
    public boolean prevPage() {
        boolean ret = hasPrevPage();
        if (currentPage > 1)
            currentPage--;
        return ret;
    }

    @Override
    public int getColumnCount() {
        try {
            return getCurrentPage().getRows().get(0).getData().size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public TablePage getCurrentPage() {
        if (currentPage == 0)
            throw new RuntimeException("No page data available"); //$NON-NLS-1$
        return pages.get(currentPage - 1);
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public int getCurrentPageNumber() {
        return currentPage;
    }

    @Override
    public List<String> getColumnHeaders() {
        List<String> headers = new ArrayList<String>();
        try {
            MapValue row =
                    (MapValue) getCurrentPage().getRows().get(0).getRawData();
            headers = extractHeaders(row);
        } catch (Exception ex) {
            // ignore
        }
        return headers;
    }

    /**
     * Extract the column names as list from MapValue of a row.
     * 
     * @param row - row as MapValue.
     * @return list of column names.
     */
    private static List<String> extractHeaders(MapValue row) {
        return row.entrySet().stream().map(e -> {
            return e.getKey();
        }).collect(Collectors.toList());
    }

    @Override
    public List<TablePage> getPages() {
        return pages;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setResult(Object result, Table table) {
        this.result = Iterator.class.cast(result);
        currentPage = 0;
        this.table = table;
        pages = new LinkedList<TablePage>();
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
