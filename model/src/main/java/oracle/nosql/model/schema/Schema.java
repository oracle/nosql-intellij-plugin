/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

import java.util.List;

/**
 * Schema is a collection of {@link Table database tables}. It is owned by a
 * {@link Datamodel}.
 * <p>
 * Schema is modeled as a tree of {@link SchemaElement schema elements}. The
 * {@link Table tables} are first level of the tree, followed by {@link Field
 * fields} which are leaf nodes of the tree. <br>
 * Each schema element has a name as identifier. Schema typically represent
 * persisted tables in a database. But this model also supports an in-memory
 * schema, besides being a representation of database tables.
 * 
 * @author pinaki poddar
 * @author Jashkumar Dave
 *
 */
public interface Schema extends SchemaContainer<Datamodel, Table> {
    Datamodel getDatastore();

    /**
     * Affirms if the table of given name is known to this schema. A schema may
     * not have fetched the details of the table.
     * 
     * @param name name of a table
     * @return true if the table name is known to this schema
     */
    boolean hasTable(String name);

    /**
     * Affirms if details of the table of given name has been fetched.
     * 
     * @param name name of a table
     * @return true if details of the table of given name has been fetched
     */
    boolean hasFetched(String name);

    /**
     * Adds given table to this schema.
     * <p>
     * If a table has already been added, this operation throws exception,
     * unless overwrite is true.
     * 
     * @param table a table. must not be null or empty
     * @param overwrite if true, the table overwrites any existing table of
     * same name. Otherwise an exception is raised
     * @return the table to be added
     * 
     * @exception DuplicateElementException if table of same exists
     */
    public Table addTable(Table table, boolean overwrite);

    /**
     * Gets the name of the tables.
     * 
     * @return a list of table names in lexicographic order. Can be empty, but
     * never null.
     */
    public List<String> getTableNames();

    /**
     * Gets the table of given name.
     * 
     * @param tableName name of a table
     * @return a table or null
     */
    public Table getTable(String tableName);

    /**
     * 
     * @param tableName name of a table
     * 
     * @return whether the table has been removed
     * 
     */
    public boolean removeTable(String tableName);

    /**
     * Sets schema builder, used for refresh.
     * 
     * @param schemaBuilder
     */
    public void setSchemaBuilder(SchemaBuilder schemaBuilder);

    /**
     * @return SchemaBuilder associated with this Schema
     */
    public SchemaBuilder getSchemaBuilder();
}
