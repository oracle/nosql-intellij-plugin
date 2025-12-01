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
    public String getCreateDDL() {
        Iterator<Field> fields = getFields().iterator();
        String columns = " (";
        while (fields.hasNext()) {
            columns +=
                    fields.next().getName() + (fields.hasNext() ? "," : ")");
        }
        String createIdxDdl = "create index " +
                getName() +
                " on " +
                getTable().getName() +
                columns;
        return createIdxDdl;
    }

    @Override
    public String getDropDDL() {
        return "drop index if exists " +
                getName() +
                " on " +
                getTable().getName();
    }
}
