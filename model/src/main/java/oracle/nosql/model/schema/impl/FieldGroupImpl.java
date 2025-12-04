/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import java.util.Collections;
import java.util.List;

import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.Table;

/**
 * A group of fields. The fields belong to same table.
 * 
 * @author pinaki poddar
 *
 */
@SuppressWarnings("serial")
public class FieldGroupImpl extends AbstractSchemaContainer<Table, Field>
        implements
        FieldGroup {
    private FieldGroup.Type type;

    /**
     * Create a field group of given name and category. The group has no field.
     * 
     * @param table the owner table. all fields of the group must owned by the
     * same table.
     * @param name name of the group
     */
    @SuppressWarnings("unchecked")
    FieldGroupImpl(String name) {
        this(name, Collections.EMPTY_LIST);
    }

    public void setType(FieldGroup.Type type) {
        this.type = type;
    }

    public FieldGroup.Type getType() {
        return type;
    }

    /**
     * Create a field group of given name, given fields and category.
     * 
     * @param table the owner table. all fields of the group must owned by the
     * same table.
     * @param name name of a group
     * @param fields initial fields added to the group
     */
    FieldGroupImpl(String name, List<Field> fields) {
        super(name);
        for (Field field : fields) {
            addField(field);
        }
    }

    /**
     * Gets unmodifiable list of field
     * 
     * @return an unmodifiable list of fields. Can be empty but not null.
     */
    @Override
    public List<Field> getFields() {
        return super.getChildren();
    }

    @Override
    public String toString() {
        return getName() + " fields=" + getFields();
    }

    @Override
    public Field addField(Field field) {
        return super.addChild(field, true);
    }

    @Override
    public boolean hasField(String fieldName) {
        return hasChild(fieldName);
    }

    @Override
    public Table getTable() {
        return getOwner();
    }

    @Override
    public int size() {
        return super.getChildCount();
    }

    @Override
    public boolean hasField(Field field) {
        return super.hasChild(field);
    }

    @Override
    public boolean isFetched() {
        // Always true as it is not related to any server side element.
        return true;
    }

    @Override
    public void refresh() {
        // Does nothing
    }
}
