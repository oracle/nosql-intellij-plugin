/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

import java.util.List;

import oracle.nosql.model.util.Named;

/**
 * An index defined on a table using a group of fields. All fields of an index
 * belongs to the same table.
 *
 */
public interface Index extends FieldGroup, Named {
    /**
     * Gets the table on which this index has been defined.
     * 
     * @return table the owning table
     */
    @Override
    public Table getTable();

    /**
     * Gets the fields that make up this index.
     * 
     * @return a list fields. never null
     */
    @Override
    public List<Field> getFields();

    /**
     * affirms if this index contains a field of given name
     * 
     * @return true if named field is in this index
     */
    @Override
    public boolean hasField(String fieldName);

    /**
     * Adds the given field to this index.
     * 
     * @return a field. must not be null.
     */
    @Override
    public Field addField(Field field);

    /**
     * Gets the DDL to create this index.
     * 
     * @return a DDL string.
     */
    public String getCreateDDL();

    /**
     * Gets the DDL to drop this index.
     * 
     * @return a DDL string.
     */
    public String getDropDDL();
}
