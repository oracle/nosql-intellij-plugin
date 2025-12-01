/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

import java.io.Serializable;

import oracle.nosql.model.util.Named;

/**
 * A basic element in {@link Datamodel}. A schema element has a {@link Named
 * name} and often owned by a {@link SchemaContainer container element}.
 * 
 * <br>
 * Schema forms a <em>closed</em> group. If an element <code>a</code> in a
 * schema refers to another element <code>b</code>, then <code>b</code> also
 * belongs to same schema. <br>
 * For example, a {@link Field field} of {@link Index index} is owned by a
 * {@link Table table}, which in turn, owned by a {@link Schema schema}. <br>
 * Fully connected schema implies that all references in a schema are resolved
 * in the schema itself. <br>
 * For example, a {@link Table#getPrimaryKeys() primary key} of a table may
 * have multiple {@link FieldGroup#getFields() fields}. The fields of a primary
 * key must also be resolved in the same table
 * <p>
 * 
 * 
 * @author pinaki poddar
 *
 * @param <O> the type of owning element
 */
public interface SchemaElement<
        O extends SchemaContainer<?, ?>> extends Named, Serializable {
    /**
     * Gets the owning element.
     * 
     * @return owning element. Can be null when the element does not belong to
     * any {@link Datamodel model} yet.
     */
    O getOwner();

    /**
     * Returns weather this element is fetched at least once.
     * 
     * @return true if fetched at least once otherwise false.
     */
    public boolean isFetched();

    /**
     * Refreshes all the information fetched till now. That is, it will
     * re-fetch all the information to match with recent data on server.
     * Implementation of this method must also take care of
     * {@link #isFetched()} method.
     */
    public void refresh();
}
