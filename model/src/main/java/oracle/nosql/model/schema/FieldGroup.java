/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

import java.util.List;

/**
 * A group of fields (generally represents indexes/keys).
 * 
 * @author pinaki poddarCategory
 *
 */
public interface FieldGroup extends SchemaContainer<Table, Field> {
    enum Type {
        PRIMARY_KEY, SHARD_KEY, INDEX_KEY
    };

    /**
     * @return number of field in this group.
     */
    int size();

    /**
     * Checks whether the given field is part of this FieldGroup or not.
     * 
     * @param field - field to be checked.
     * @return true if field is part of this FieldGroup, otherwise false.
     */
    boolean hasField(Field field);

    /**
     * @return parent table of this FieldGroup.
     */
    public Table getTable();

    /**
     * @return This FieldGroup's type.
     */
    public Type getType();

    /**
     * Gets unmodifiable list of field
     * 
     * @return an unmodifiable list of fields. Can be empty but not null.
     */
    public List<Field> getFields();

    /**
     * adds a field to this group. all fields in a group must belong to same
     * table.
     * 
     * @param field name of a field to be added to this group. The named field
     * must exist on the same owner of this group. Because all fields of a
     * group must have the same owner.
     * 
     * @return the field added
     */
    public Field addField(Field field);

    /**
     * Checks whether the given field (by name) is part of this FieldGroup or
     * not.
     * 
     * @param fieldName - name of the field to be checked.
     * @return true if field is part of this FieldGroup, otherwise false.
     */
    public boolean hasField(String fieldName);
}
