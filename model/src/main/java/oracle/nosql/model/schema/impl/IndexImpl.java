/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import java.util.Iterator;

import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.Index;
import oracle.nosql.model.schema.Table;

@SuppressWarnings("serial")
public class IndexImpl extends FieldGroupImpl implements Index {
    private boolean withNoNulls = false;
    private boolean withUniqueKeysPerRow = false;

    public IndexImpl(String name) {
        super(name);
    }

    @Override
    public FieldGroup.Type getType() {
        return FieldGroup.Type.INDEX_KEY;
    }

    IndexImpl(Table table, String name) {
        this(name);
        setOwner(table);
    }

    @Override
    public Table getTable() {
        return getOwner();
    }

    @Override
    public void setNoNulls(boolean nonNull){
        withNoNulls = nonNull;
    }

    @Override
    public void setUniqueKeysPerRow(boolean uniqueKeysPerRow){
        withUniqueKeysPerRow = uniqueKeysPerRow;
    }

    @Override
    public String getCreateDDL() {
        Iterator<Field> fields = getFields().iterator();
        StringBuilder columns = new StringBuilder(" (");
        while (fields.hasNext()) {
            Field field = fields.next();
            if (field.getName().indexOf("|index.path") > 0) {
                String name = field.getName().substring(0, field.getName().indexOf("|"));
                columns.append(name);
                if (field.getIndexType() != null) {
                    columns.append(" AS ").append(field.getIndexType());
                }
            } else if (field.getName().indexOf("|index") > 0) {
                String name = field.getName().substring(0, field.getName().indexOf("|"));
                columns.append(name);
            } else {
                columns.append(field.getName());
            }
            columns.append(fields.hasNext() ? ", " : ")");
        }
        String createIdxDdl = "CREATE INDEX " +
                getName() +
                " ON " +
                getTable().getName() +
                columns +
                (withNoNulls ? " WITH NO NULL" : "") +
                (withUniqueKeysPerRow ? " WITH UNIQUE KEYS PER ROW" : "");
        return createIdxDdl;
    }

    @Override
    public String getDropDDL() {
        return "DROP INDEX IF EXISTS " +
                getName() +
                " ON " +
                getTable().getName();
    }
}
