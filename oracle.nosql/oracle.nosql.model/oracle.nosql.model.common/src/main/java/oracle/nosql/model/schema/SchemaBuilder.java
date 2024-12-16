/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

import java.io.IOException;

/**
 * A factory for {@link SchemaElement elements} of a {@link Datamodel} .
 * <p>
 * A builder can create {@link SchemaElement elements of datamodel} with or
 * without context of a {@link SchemaContainer owner element}. When an element
 * is created in the context of a non-null owner, the created element is
 * implicitly added to the owner. <br>
 * An element is created with a valid name and without any attributes. The
 * attributes are to be set on the created element itself.
 * <p>
 * A builder {@link #build(String, Object) builds} a datamodel from
 * <em>other</em> sources. The exact nature of the source is not specified at
 * this interface.
 * 
 * 
 * @author pinaki poddar
 *
 */
public interface SchemaBuilder {
    /**
     * Creates a {@link Datamodel} of given name which forms the root of
     * schema.
     * 
     * @param name name of a datamodel. must not be null or empty.
     * @return an empty datamodel.
     */
    Datamodel newDatamodel(String name);

    /**
     * Creates schema of given name that belongs to given datamodel. The schema
     * is added to the datamodel.
     * 
     * @param store a datamodel. must not be null.
     * @param name name of the schema.
     * @param overwrite if true, add new schema even if a schema of same name
     * exists, overwriting the existing element. Otherwise throws exception.
     * @return the newly created schema, empty except a name.
     * @throws DuplicateElementException if a schema of same name exists and
     * overwrite is false.
     */
    Schema newSchema(Datamodel store, String name, boolean overwrite);

    /**
     * Creates schema of given name that does not belong to any datamodel.
     * 
     * @param name name of the schema.
     * @return the newly created schema, empty except a name.
     */
    Schema newSchema(String name);

    /**
     * Creates table of given name that does not belong to any schema.
     * 
     * @param name name of the table.
     * @return the newly created table, empty except a name.
     */
    Table newTable(String name);

    /**
     * Creates table of given name that belongs to given schema. The table is
     * added to the schema.
     * 
     * @param schema a schema. must not be null.
     * @param name name of the table.
     * @param overwrite if true, add new table even if a table of same name
     * exists, overwriting the existing table. Otherwise throws exception.
     * @return the newly created table, empty except a name.
     * @throws DuplicateElementException if a table of same name exists and
     * overwrite is false.
     */
    Table newTable(Schema schema, String name, boolean overwrite);

    /**
     * Creates field of given name that does not belong to any table.
     * 
     * @param name name of the field.
     * @return the newly created field, empty except a name.
     */
    Field newField(String name);

    /**
     * Creates field of given name that belongs to given table. The field is
     * added to the table.
     * 
     * @param table a table. must not be null.
     * @param name name of the field.
     * @param overwrite if true, add new field even if a field of same name
     * exists, overwriting the existing field. Otherwise throws exception.
     * @return the newly created field, empty except a name.
     * @throws DuplicateElementException if a field of same name exists and
     * overwrite is false.
     */
    Field newField(Table table, String name, boolean overwrite);

    /**
     * Creates index of given name that does not belong to any table.
     * 
     * @param name name of the index.
     * @return the newly created index, empty except a name.
     */
    Index newIndex(String name);

    /**
     * Creates index of given name that belongs to given table. The index is
     * added to the table.
     * 
     * @param table a table. must not be null.
     * @param name name of the index.
     * @param overwrite if true, add new index even if a index of same name
     * exists, overwriting the existing index. Otherwise throws exception.
     * @return the newly created index, empty except a name.
     * @throws DuplicateElementException if a index of same name exists and
     * overwrite is false.
     */
    Index newIndex(Table table, String name, boolean overwrite);

    /**
     * Builds schema information from given input.
     * 
     * @param modelName name of the datamodel whose schema would be built. Must
     * not be null or empty. The name is used for informational purpose.
     * @param input can be different, based on concrete implementation.
     * @return a datamodel
     * @exception IOException if can not process input
     */
    Datamodel build(String modelName, Object input) throws IOException;
    /**
     * Resolves the given model. Resolving a model is specific to kind of
     * sources that the builder can handle. A schema builder that is connected
     * to a database might fetch all unresolved schema elements.
     */
    // boolean resolve(Datamodel mode) throws IOException;
}
