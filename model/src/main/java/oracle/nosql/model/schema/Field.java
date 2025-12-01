/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

/**
 * A field is a possible representation of a database field. However, a field
 * may be part of a query result and not necessarily belong to a {@link Table}.
 * 
 * @author pinaki poddar
 *
 */
public interface Field extends SchemaElement<Table> {
    /**
     * Type of fields that can be used to define schema.
     */
    public static enum Type {
        BOOLEAN,
        INTEGER,
        LONG,
        DOUBLE,
        NUMBER,
        FLOAT,
        ENUM,
        STRING,
        BINARY,
        FIXED_BINARY,
        TIMESTAMP,
        ARRAY,
        MAP,
        JSON,
        RECORD
        // , STRUCT
    };

    /**
     * @return parent table of this Field.
     */
    public Table getTable();

    /**
     * @return type of this field.
     */
    public Field.Type getType();

    /**
     * Sets the field type.
     * 
     * @param type - filed type
     * @return Field instance for chaining the functions.
     */
    public Field setType(Field.Type type);

    /**
     * @return true if the field is nullable, otherwise false.
     */
    public boolean isNullable();

    /**
     * @return true if the field is part of primary key, otherwise false.
     */
    public boolean isPrimaryKey();

    /**
     * @return true if the field is part of shard key, otherwise false.
     */
    public boolean isShardKey();

    /**
     * @return true if the field is part of index key, otherwise false.
     */
    public boolean isIndexKey();

    /**
     * @return true if the field has default value, otherwise false.
     */
    public boolean isDefault();

    /**
     * Marks the field as part of primary key.
     * 
     * @return Field instance for chaining the functions.
     */
    public Field makePrimarKey();

    /**
     * Marks the field as part of shard key.
     * 
     * @return Field instance for chaining the functions.
     */
    public Field makeShardKey();

    /**
     * Marks the filed as nullable if the flag is true, otherwise non nullable.
     * 
     * @param flag - value to be set.
     * @return Field instance for chaining the functions.
     */
    public Field setNullable(boolean flag);

    /**
     * Sets whether the field has default value or not.
     * 
     * @param isdefault - value to be set.
     * @return Field instance for chaining the functions.
     */
    public Field setDefault(boolean isdefault);

    /**
     * Sets the default value of the field.
     * 
     * @param defaultValue - value on default.
     * @return Field instance for chaining the functions.
     */
    public Field setDefault(String defaultValue);

    /**
     * @return the default value of the field.
     */
    public String getDefault();

    /**
     * Sets the syntax for creation of this field.
     * 
     * @param syntax - syntax as String.
     * @return Field instance for chaining the functions.
     */
    public Field setSyntax(String syntax);

    /**
     * @return the field creation syntax string.
     */
    public String getSyntax();

    /**
     * Sets the max size for this field.
     * 
     * @param size
     * @return Field instance for chaining the functions.
     */
    public Field setSize(String size);

    /**
     * @return size of this field.
     */
    public String getSize();

    /**
     * @return creation syntax for this field in string form.
     */
    public String getFlattenedForm();

    /**
     * @return create DDL statement for this field.
     */
    public String getCreateDDL();
}
