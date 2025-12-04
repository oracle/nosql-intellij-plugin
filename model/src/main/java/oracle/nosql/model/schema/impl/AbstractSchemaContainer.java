/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import oracle.nosql.model.schema.DuplicateElementException;
import oracle.nosql.model.schema.SchemaContainer;
import oracle.nosql.model.schema.SchemaElement;
import oracle.nosql.model.util.Levensthien;
import oracle.nosql.model.util.NamedMap;

/**
 * Abstract implementation of a container of {@link SchemaElement schema
 * element}.
 * 
 * @author pinaki poddar
 *
 * @param <O> type of the owner
 * @param <C> type of the child elements
 */
@SuppressWarnings("serial")
abstract class AbstractSchemaContainer<O extends SchemaContainer<?, ?>,
        C extends SchemaElement<?>> extends AbstractSchemaElement<O> implements
        SchemaContainer<O, C> {
    private final NamedMap<C> children = new NamedMap<C>();

    /**
     * creates an element of given name.
     * 
     * @param name name of this container
     */
    protected AbstractSchemaContainer(String name) {
        super(name);
    }

    // protected AbstractSchemaContainer(O owner, String name) {
    // super(owner, name);
    // }
    @Override
    public void removeAllChildren() {
        children.clear();
    }

    @Override
    public final boolean hasChild(String childName) {
        return children.containsKey(childName);
    }

    public final boolean hasChild(C child) {
        return children.containsValue(child);
    }

    @Override
    public C getChild(String name) {
        if (children.containsKey(name)) {
            return children.get(name);
        }
        List<String> candidates = getChildrenNames();
        String closest = Levensthien.findClosest(name, candidates);
        throw new NoSuchElementException("named element " +
                name +
                " does not exist. " +
                "current elements are " +
                prune(candidates, 4) +
                (closest == null ? "" : " did you mean " + closest + "?"));
    }

    String prune(List<String> list, int max) {
        if (list == null)
            return "";
        if (list.size() <= max) {
            return list.toString();
        }
        return list.subList(0, max) +
                " ( ..." +
                (list.size() - max) +
                " more)";
    }

    @Override
    public C getChildAt(int idx) {
        return children.getAt(idx);
    }

    @Override
    public final List<C> getChildren() {
        return children.values();
    }

    @Override
    public final List<String> getChildrenNames() {
        return children.getNames();
    }

    @Override
    public final boolean isEmpty() {
        return children.size() == 0;
    }

    @Override
    public final int getChildCount() {
        return children.size();
    }

    @Override
    public final C removeChild(String name) {
        return children.remove(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public C addChild(C child, boolean overwrite) {
        if (child == null) {
            throw new IllegalArgumentException(
                    "can not add null element to " + this);
        }
        if (child.getOwner() == null) {
            // setOwner() is package protected
            AbstractSchemaElement.class.cast(child).setOwner(this);
        }
        if (hasChild(child.getName()) && !overwrite) {
            throw new DuplicateElementException(child,
                    "can not add " +
                            child +
                            " because an element of same name exists");
        }
        children.add(child, overwrite);
        return child;
    }

    @Override
    public void recursiveRefresh() {
        refresh();
        List<C> children = getChildren();
        for (C child : children) {
            if (SchemaContainer.class.isInstance(child)) {
                SchemaContainer.class.cast(child).recursiveRefresh();
            } else {
                child.refresh();
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        if (children.isEmpty())
            return result;
        for (C child : children) {
            if (child != null && child != this)
                continue;
            result = prime * result + child.hashCode();
        }
        return result;
    }

    /**
     * a container is equal by value if all of its children are equal by
     * reference.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractSchemaContainer other = (AbstractSchemaContainer) obj;
        if (!this.getName().equals(other.getName()))
            return false;
        Iterator<C> i1 = this.children.values().iterator();
        Iterator<?> i2 = other.children.values().iterator();
        while (i1.hasNext()) {
            Object c1 = i1.next();
            Object c2 = i2.next();
            if (c1 == null || c1 == this)
                continue;
            if (c1 != c2)
                return false;
        }
        return true;
    }
}
