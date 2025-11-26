/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import java.util.List;

import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.SchemaBuilder;
import oracle.nosql.model.schema.Table;

/**
 * Schema is a collection of {@link Table database tables}.
 * 
 * @author pinaki poddar
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractSchema extends
        AbstractSchemaContainer<Datamodel, Table> implements
        Schema {
    private SchemaBuilder schemaBuilder;

    /**
     * Create schema of given name.
     * 
     * @param name name of a schema
     */
    protected AbstractSchema(String name) {
        super(name);
    }

    protected AbstractSchema(Datamodel model, String name) {
        this(name);
        setOwner(model);
    }

    /**
     * Creates a new table. The table is only created in a memory, not in
     * database.
     */
    @Override
    public Table addTable(Table table, boolean overwrite) {
        return super.addChild(table, overwrite);
    }

    /**
     * Gets the name of declared tables.
     * 
     * @return a set of table names. Can be empty, but never null.
     */
    @Override
    public List<String> getTableNames() {
        return getChildrenNames();
    }

    /**
     * Gets the table of given name. If the table has not been fetched, then
     * fetches table information from database.
     */
    @Override
    public Table getTable(String tableName) {
        return super.getChild(tableName);
    }

    @Override
    public boolean removeTable(String tableName) {
        return super.removeChild(tableName) != null;
    }

    @Override
    public Datamodel getDatastore() {
        return getOwner();
    }

    @Override
    public boolean hasTable(String name) {
        return hasChild(name);
    }

    @Override
    public boolean hasFetched(String name) {
        return hasTable(name);
    }

    @Override
    public void setSchemaBuilder(SchemaBuilder schemaBuilder) {
        this.schemaBuilder = schemaBuilder;
    }

    @Override
    public SchemaBuilder getSchemaBuilder() {
        return schemaBuilder;
    }
}
