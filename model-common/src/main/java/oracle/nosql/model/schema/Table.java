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
 * A table descriptor in terms of {@link Field fields}. The fields in a table
 * are ordered and accessible by a 0-based positional index.
 * 
 * @author pinaki poddar
 *
 */
public interface Table extends SchemaContainer<Schema, Field> {
    /**
     * @return table's schema.
     */
    public Schema getSchema();

    /**
     * @param name - field name
     * @return true if this table has field with given name, otherwise false.
     */
    public boolean hasField(String name);

    /**
     * @param name - field name
     * @return true if the given index is part of this table, otherwise false.
     */
    public boolean hasIndex(String name);

    /**
     * Gets number of fields in this table.
     * 
     * @return number of fields in this table.
     */
    public int getFieldCount();

    /**
     * Gets a list of fields that form the primary keys.
     * 
     * @return a field group.
     */
    public FieldGroup getPrimaryKeys();

    /**
     * Gets a list of indexes in this table.
     * 
     * @return a non-empty, non-null list.
     */
    public List<Index> getIndexes();

    /**
     * Gets index of given name.
     * 
     * @param indexName name of an index
     * @return an index.
     * @throws NoSuchElementException if no such index exists
     */
    public Index getIndex(String indexName);

    /**
     * Gets a list of fields that form the shard keys.
     * 
     * @return a non-null list. can be empty.
     */
    public FieldGroup getShardKeys();

    /**
     * Gets field of this table in the order of their declaration.
     * 
     * @return list of fields. never null.
     */
    public List<Field> getFields();

    /**
     * Adds given field.
     * 
     * @param field a field to be added.
     * @param overwrite if true, the given field overwrites any existing field
     * of same name. Otherwise an exception is raised
     * @return the field added
     * @throws DuplicateElementException if named field exists and overwrite is
     * false
     */
    public Field addField(Field field, boolean overwrite);

    /**
     * Removes the given field from this table.
     * 
     * @param field
     */
    public void removeField(Field field);

    /**
     * Adds given index.
     * 
     * @param index an index to be added.
     * @param overwrite if true, the given field overwrites any existing field
     * of same name. Otherwise an exception is raised
     * @return the index added
     * @throws DuplicateElementException if named index exists and overwrite is
     * false
     */
    public Index addIndex(Index index, boolean overwrite);

    /**
     * Gets field of given name.
     * 
     * @param name name of a field
     * @return the field
     * @throws NoSuchElementException if no field exists with given name
     */
    public Field getField(String name);

    /**
     * Gets field at given position.
     * 
     * @param idx 0-based position of a field
     * @return the field
     * @throws NoSuchElementException if no field exists at given position
     */
    public Field getFieldAt(int idx);

    /**
     * @return DDL query to drop this table.
     */
    public String getDropDDL();

    /**
     * @return create table DDL query.
     */
    public String getCreateDDL();

    /**
     * @return read limit of this table in KBps.
     */
    public int getReadKB();

    /**
     * @return write limit of this table in KBps.
     */
    public int getWriteKB();

    /**
     * @return storage limit of this table in GB.
     */
    public int getStorageGB();

    /**
     * @return time to live.
     */
    public int getTTL();

    /**
     * @return unit used for TTL (DAYS/HOURS).
     */
    public String getTTLUnit();

    /**
     * Sets the read KBps limit for this table.
     * 
     * @param readKB - speed in KBps.
     */
    public void setReadKB(int readKB);

    /**
     * Sets the write KBps limit for this table.
     * 
     * @param writeKB - speed in KBps.
     */
    public void setWriteKB(int writeKB);

    /**
     * Sets the storage space limit for this table.
     * 
     * @param storageGB - space in GB.
     */
    public void setStorageGB(int storageGB);

    /**
     * Sets default TTL for all records in this table.
     * 
     * @param ttl - TTL value.
     */
    public void setTTL(int ttl);

    /**
     * Sets the unit to be used for TTL
     * 
     * @param ttlUnit - DAYS/HOURS
     */
    public void setTTLUnit(String ttlUnit);

    /**
     * Resets the table as if it is new instance.
     */
    public void reset();
}
