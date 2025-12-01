/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Index;
import oracle.nosql.model.schema.Table;

/**
 * A field is a possible representation of a database field. However, a field
 * may be part of a query result and not necessarily belong to a database
 * {@link Table table}.
 * 
 * @author pinaki poddar
 *
 */
@SuppressWarnings("serial")
public class FieldImpl extends AbstractSchemaElement<Table> implements Field {
    private Field.Type type;
    private boolean primaryKey;
    private boolean shardKey;
    private boolean nullable;
    private boolean isDefault;
    private String defaultValue, syntax, size;

    public FieldImpl(String name) {
        super(name);
        type = Type.STRING;
    }

    /**
     * Create a field of given name that belongs to given table. The field type
     * defaults to STRING.
     * 
     * @param table owning table
     * @param name name of a field
     */
    public FieldImpl(Table table, String name) {
        this(name);
        setOwner(table);
    }

    @Override
    public Table getTable() {
        return getOwner();
    }

    @Override
    public Field.Type getType() {
        return type;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public Field setType(Field.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public Field setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public FieldImpl setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    @Override
    public boolean isShardKey() {
        return shardKey;
    }

    public FieldImpl setShardKey(boolean shardKey) {
        this.shardKey = shardKey;
        return this;
    }

    @Override
    public String toString() {
        return getName() + ":" + getType();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
                ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + (nullable ? 1231 : 1237);
        result = prime * result + (primaryKey ? 1231 : 1237);
        result = prime * result + (shardKey ? 1231 : 1237);
        result = prime * result +
                ((getOwner() == null) ? 0 : getOwner().hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldImpl other = (FieldImpl) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (nullable != other.nullable)
            return false;
        if (primaryKey != other.primaryKey)
            return false;
        if (shardKey != other.shardKey)
            return false;
        if (getOwner() == null) {
            if (other.getOwner() != null)
                return false;
        } else if (!getOwner().equals(other.getOwner()))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public Field makePrimarKey() {
        return setPrimaryKey(true);
    }

    @Override
    public Field makeShardKey() {
        return setShardKey(true);
    }

    @Override
    public boolean isIndexKey() {
        for (Index index : getTable().getIndexes()) {
            if (index.hasField(this.getName()))
                return true;
        }
        return false;
    }

    @Override
    public boolean isFetched() {
        // Always true
        return true;
    }

    @Override
    public void refresh() {
        // Does nothing
    }

    @Override
    public Field setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Field setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }

    @Override
    public String getCreateDDL() {
        Table table = getOwner();
        String addNewColumnDdl = "alter table " +
                table.getName() +
                " (add " +
                getFlattenedForm() +
                ")";
        return addNewColumnDdl;
    }

    @Override
    public Field setSyntax(String syntax) {
        this.syntax = syntax;
        return this;
    }

    @Override
    public String getSyntax() {
        return syntax;
    }

    @Override
    public Field setSize(String size) {
        this.size = size;
        return this;
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public String getFlattenedForm() {
        String colString = ""; //$NON-NLS-1$
        colString += getName() + " "; //$NON-NLS-1$
        switch (getType()) {
        case INTEGER:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case NUMBER:
        case STRING:
        case TIMESTAMP:
            colString += getType();
            if (getType() == Type.TIMESTAMP) {
                colString += "(9)";
            }
            if (!isNullable()) {
                colString += " NOT NULL"; //$NON-NLS-1$
            }
            if (isDefault()) {
                if (Type.STRING == getType()) {
                    String def = getDefault().replace("\"", "\\\"");
                    colString += " DEFAULT \"" + def + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    colString += " DEFAULT " + getDefault(); //$NON-NLS-1$
                }
            }
            break;
        case BOOLEAN:
            colString += getType();
            if (isDefault()) {
                colString += " DEFAULT " + getDefault(); //$NON-NLS-1$
            }
            break;
        case ENUM:
        case ARRAY:
        case MAP:
            // case RECORD:
            // colString += getSyntax().trim();
            // break;
        case BINARY:
            colString += getType();
            String sz = getSize().trim();
            long szLong = 0;
            try {
                szLong = Long.parseLong(sz);
            } catch (Exception e) {
            }
            if (szLong > 0) {
                colString += "(" + sz + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            break;
        case JSON:
            colString += getType();
            break;
        default:
            throw new RuntimeException("Unhandled field type:" + getType());
        }
        return colString;
    }
}
