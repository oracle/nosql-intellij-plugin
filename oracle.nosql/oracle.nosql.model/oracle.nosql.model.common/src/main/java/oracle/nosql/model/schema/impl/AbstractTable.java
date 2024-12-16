/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import oracle.nosql.model.schema.*;
import oracle.nosql.model.util.NamedMap;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A table descriptor in terms of {@link FieldImpl fields}. The fields in a
 * table are ordered and accessible by a 0-based positional index.
 * 
 * @author pinaki poddar
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractTable extends
        AbstractSchemaContainer<Schema, Field> implements
        Table {
    private final NamedMap<Index> indexes;
    public static final String TABLE_NAME_REGEX = "^[A-Za-z][A-Za-z0-9_$:.]*$";
    private static final Pattern TABLE_NAME_PATTERN =
            Pattern.compile(TABLE_NAME_REGEX);
    private int readKB, writeKB, storageGB, TTL;
    private String TTLUnit;

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public String getTTLUnit() {
        return TTLUnit;
    }

    public void setTTLUnit(String TTLUnit) {
        this.TTLUnit = TTLUnit;
    }

    public int getReadKB() {
        return readKB;
    }

    public void setReadKB(int readKB) {
        this.readKB = readKB;
    }

    public int getWriteKB() {
        return writeKB;
    }

    public void setWriteKB(int writeKB) {
        this.writeKB = writeKB;
    }

    public int getStorageGB() {
        return storageGB;
    }

    public void setStorageGB(int storageGB) {
        this.storageGB = storageGB;
    }

    public AbstractTable(String name) {
        super(name);
        if (!TABLE_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "invalid table name [" + name + "]");
        }
        indexes = new NamedMap<Index>();
    }

    public AbstractTable(Schema schema, String name) {
        this(name);
        setOwner(schema);
    }

    @Override
    public int getFieldCount() {
        return getChildCount();
    }

    /**
     * Gets a list of primary key names.
     * 
     * @return a non-empty, non-null list.
     */
    @Override
    public FieldGroup getPrimaryKeys() {
        List<Field> fields = this
                .getChildren()
                    .stream()
                    .filter(f -> f.isPrimaryKey())
                    .collect(Collectors.toList());
        FieldGroupImpl pkFields = new FieldGroupImpl("PrimaryKey", fields);
        ((AbstractSchemaElement<Table>) pkFields).setOwner(this);
        pkFields.setType(FieldGroup.Type.PRIMARY_KEY);
        return pkFields;
    }

    @Override
    public List<Index> getIndexes() {
        return indexes.values();
    }

    @Override
    public Index getIndex(String indexName) {
        if (!indexes.containsKey(indexName)) {
            throw new NoSuchElementException("index " +
                    indexName +
                    " does not exist. indexes are " +
                    indexes.getNames());
        }
        return indexes.get(indexName);
    }

    /**
     * Gets a list of shard key names.
     * 
     * @return a non-null list. can be empty.
     */
    @Override
    public FieldGroup getShardKeys() {
        List<Field> fields = this
                .getChildren()
                    .stream()
                    .filter(f -> f.isShardKey())
                    .collect(Collectors.toList());
        FieldGroupImpl shardKeys = new FieldGroupImpl("ShardKey", fields);
        ((AbstractSchemaElement<Table>) shardKeys).setOwner(this);
        shardKeys.setType(FieldGroup.Type.SHARD_KEY);
        return shardKeys;
    }

    /**
     * Gets field of this table in the order of their declaration.
     * 
     * @return list of fields. never null.
     */
    @Override
    public List<Field> getFields() {
        return getChildren();
    }

    /**
     * Gets field of given name.
     * 
     * @param name name of a field
     * @return the field
     */
    @Override
    public Field getField(String name) {
        return getChild(name);
    }

    /**
     * Gets field at given position.
     * 
     * @param idx 0-based position of a field
     * @return the field
     */
    @Override
    public Field getFieldAt(int idx) {
        return getChildAt(idx);
    }

    @Override
    public String toString() {
        return "Table " + getName() + " fields:" + getFields();
    }

    @Override
    public String getDropDDL() {
        return "DROP TABLE IF EXISTS " + getName();
    }

    @Override
    public String getCreateDDL() {
        if (getPrimaryKeys().isEmpty()) {
            throw new IllegalStateException("can not create DDL." +
                    " no primary key is defined for table " +
                    getName());
        }
        String ddl = "CREATE TABLE ";
        ddl += getName() + " ( ";
        List<Field> fields = getChildren();
        for (Field field : fields) {
            ddl += field.getName() + " " + field.getType() + ", ";
        }
        String shard = "";
        if (!getShardKeys().isEmpty()) {
            shard += "SHARD(" +
                    joinFieldNames(getShardKeys().getFields()) +
                    ")";
        }
        ddl += "PRIMARY KEY (" +
                shard +
                (shard.isEmpty() ? "" : ",") +
                joinFieldNames(getPrimaryKeys().getFields()) +
                "))";
        return ddl;
    }

    private String joinFieldNames(Iterable<Field> fields) {
        Iterator<Field> iterator = fields.iterator();
        String s = "";
        while (iterator.hasNext()) {
            oracle.nosql.model.schema.Field field = iterator.next();
            s += field.getName() + (iterator.hasNext() ? ", " : "");
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Index addIndex(Index index, boolean overwrite) {
        ((AbstractSchemaElement<Table>) index).setOwner(this);
        indexes.add(index, overwrite);
        return index;
    }

    @Override
    public Field addField(Field field, boolean overwrite) {
        return super.addChild(field, overwrite);
    }

    @Override
    public void removeField(Field field) {
        super.removeChild(field.getName());
    }

    @Override
    public Schema getSchema() {
        return getOwner();
    }

    @Override
    public boolean hasField(String name) {
        return hasChild(name);
    }

    @Override
    public boolean hasIndex(String name) {
        return indexes.containsKey(name);
    }

    @Override
    public void reset() {
        indexes.clear();
        removeAllChildren();
    }
}
