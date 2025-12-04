/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Index;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.SchemaBuilder;
import oracle.nosql.model.schema.Table;

/**
 * Abstract implementation of building a datamodel. This abstract
 * implementation adds the schema elements to its owner to maintain referential
 * integrity of the datamodel.
 * 
 * @author pinaki poddar
 *
 */
public abstract class AbstractSchemaBuilder implements SchemaBuilder {
    @Override
    public final Schema
            newSchema(Datamodel model, String name, boolean overwrite) {
        Schema schema = newSchema(name);
        model.addChild(schema, true);
        return schema;
    }

    @Override
    public final Table
            newTable(Schema schema, String name, boolean overwrite) {
        Table table = newTable(name);
        schema.addChild(table, overwrite);
        return table;
    }

    @Override
    public final Field newField(Table table, String name, boolean overwrite) {
        Field field = newField(name);
        table.addChild(field, overwrite);
        return field;
    }

    @Override
    public Field newField(String name) {
        Field field = new FieldImpl(name);
        return field;
    }

    @Override
    public final Index newIndex(Table table, String name, boolean overwrite) {
        Index index = newIndex(name);
        table.addIndex(index, overwrite);
        return index;
    }

    @Override
    public Datamodel newDatamodel(String name) {
        return new DatamodelImpl(name);
    }

    @Override
    public Index newIndex(String name) {
        return new IndexImpl(name);
    }
}
