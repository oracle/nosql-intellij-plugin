/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.table.ui;

import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePage;
import oracle.nosql.model.table.ui.TableRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TableRow for Cloudsim.
 * 
 * @author Jashkumar Dave
 *
 */
public class CloudTableRow implements TableRow {
    private MapValue row;
    private List<Object> cachedData;
    private List<String> headers;

    public CloudTableRow(MapValue row, Table table, TablePage parentpage) {
        this.row = row;
        headers = extractHeaders(row);
    }

    /**
     * Extract the column names as list from MapValue of a row.
     * 
     * @param row - row as MapValue.
     * @return list of column names.
     */
    private static List<String> extractHeaders(MapValue row) {
        return row.entrySet().parallelStream().map((e) -> {
            return e.getKey();
        }).collect(Collectors.toList());
    }

    @Override
    public Object getData(int i) {
        return getData().get(i);
    }

    @Override
    public List<Object> getData() {
        if (cachedData == null) {
            cachedData = extractData();
        }
        return cachedData;
    }

    /**
     * Extract the column data of this row.
     * 
     * @return list of column data.
     */
    private List<Object> extractData() {
        List<Object> ret = new ArrayList<Object>(headers.size());
        Map<String, FieldValue> entries = row.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        for (String header : headers) {
            ret.add(getData(entries.get(header)));
        }
        return ret;
    }

    /**
     * Extract the data from given column.
     * 
     * @param field.
     * @return field's data.
     */
    private String getData(FieldValue field) {
        if (field.getType() == FieldValue.Type.NULL) {
            return ""; //$NON-NLS-1$
        } else if (field.getType() == FieldValue.Type.BINARY) {
            return new String(field.getBinary());
        }
        return field.toJson();
    }

    @Override
    public Object getRawData() {
        return row;
    }
}
