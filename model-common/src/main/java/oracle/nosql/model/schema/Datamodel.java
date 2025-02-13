/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Datamodel captures the meta-data for persistent data in a tree structure.
 * The immediate children of a Datamodel are {@link Schema schemas} that are
 * referred as <em>namespace</em> in Oracle NoSQL database context.
 * <p>
 * The tree nodes of a Datamodel are {@link SchemaElement}. The non-leaf nodes
 * are {@link SchemaContainer}.
 * <p>
 * A datamodel <code>M</code> forms a <em>closed</em> group, i.e. if a node
 * <code>a</code> refers to another node <code>b</code>, then <code>b</code>
 * also belongs to model <code>M</code>.
 * <p>
 * 
 * @author pinaki poddar
 *
 */
public interface Datamodel extends SchemaContainer<Datamodel, Schema> {
    /**
     * Gets name of all schema names.
     * 
     * @return a list of schema names.
     */
    List<String> getSchemaNames();

    /**
     * Gets a schema by name.
     * 
     * @param name name of a schema.
     * @return the schema
     * @throws NoSuchElementException if named schema does not exist.
     */
    Schema getSchema(String name);

    /**
     * Removes a schema by name.
     * 
     * @param name name of a schema.
     * @return true if schema has been removed successfully
     */
    boolean removeSchema(String name);

    /**
     * Affirms contains a schema of given name.
     * 
     * @param name name of a schema.
     * @return true if schema has been exists
     */
    boolean hasSchema(String name);
}
