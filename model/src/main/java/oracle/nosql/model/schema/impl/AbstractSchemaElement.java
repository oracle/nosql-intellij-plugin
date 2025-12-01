/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import oracle.nosql.model.schema.SchemaContainer;
import oracle.nosql.model.schema.SchemaElement;

@SuppressWarnings("serial")
public abstract class AbstractSchemaElement<
        O extends SchemaContainer<?, ?>> implements SchemaElement<O> {
    private String name;
    private O owner;

    /**
     * Creates an element with given name and itself as owner. This special
     * purpose constructor is used for root element.
     * 
     * @param name name of this element. Must not be null or empty.
     */
    protected AbstractSchemaElement(String name) {
        setName(name);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "can not create with " + " null or empty name");
        }
        this.name = name;
    }

    /**
     * Create an element with given name and owner.
     * 
     * @param owner an owner of this element. must not be null.
     * @param name name of this element. can be null to create an element that
     * does not belong to any container.
     */
    protected AbstractSchemaElement(O owner, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "can not create with " + " null or empty name");
        }
        this.name = name;
        this.owner = owner;
    }

    /**
     * Protected to change owner.
     * 
     * @param owner new owner. A new owner is only allowed if no owner exists.
     * @throws IllegalStateException if a different owner exists.
     */
    final void setOwner(O newOwner) {
        O currentOwner = getOwner();
        if (currentOwner == null) {
            this.owner = newOwner;
        } else if (!currentOwner.equals(newOwner)) {
            throw new IllegalStateException("can not change owner of " +
                    this +
                    " to " +
                    newOwner +
                    " becuase " +
                    this +
                    " already belong to " +
                    currentOwner);
        }
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final O getOwner() {
        return owner;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        if (owner != null && owner != this) {
            result += owner.hashCode();
        }
        return result;
    }

    /**
     * schema elements are equal by name equals by value and owner equals by
     * reference.
     * 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractSchemaElement<?> other = (AbstractSchemaElement<?>) obj;
        if (!name.equals(other.name))
            return false;
        if (owner == null)
            return other.owner == null;
        return owner == other.owner;
    }
}
