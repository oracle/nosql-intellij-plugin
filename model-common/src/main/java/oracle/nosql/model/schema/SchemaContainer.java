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
 * A schema container is a recursive data structure that contains an ordered
 * list of schema elements. <br>
 * The child elements can be looked up in a container either by name or by
 * 0-based position index. The child elements are identified by their
 * {@link SchemaElement#getName() name}.
 * 
 * @author pinaki poddar
 *
 * @param <O> type of owning schema element
 * @param <C> type of containing schema element
 */
public interface SchemaContainer<O extends SchemaContainer<?, ?>,
        C extends SchemaElement<?>> extends SchemaElement<O> {
    /**
     * Affirms if a child of given name exists.
     * 
     * @param name name of a contained element
     * @return true if a child of given name exists.
     */
    boolean hasChild(String name);

    /**
     * Gets the child with given name.
     * 
     * @param name name of a child
     * @return a child with given name
     * @throws NoSuchElementException if no such child exists
     */
    C getChild(String name);

    /**
     * Gets the child at given position
     * 
     * @param pos 0-based position of the child
     * @return a child at given position
     * @throws NoSuchElementException if no such child exists
     */
    C getChildAt(int pos);

    /**
     * Adds given child to this container. This container must not contain a
     * child of given name. If it does, and overwrite is false, this method
     * throws an exception.
     * 
     * @param child a child to be added. The child must not be null. The child
     * must have the same {@link SchemaElement#getOwner() owner} as this
     * receiver or no owner at all.
     * 
     * @param overwrite flags if given child is added even if a child of same
     * name exists
     * @return the same child added
     * @exception DuplicateElementException if a child of given name already
     * exists and overwrite is false
     */
    C addChild(C child, boolean overwrite);

    /**
     * Removes a child of given name from this container.
     * 
     * @param name name of a child
     * @return the child removed or null if the child never existed
     */
    C removeChild(String name);

    /**
     * Gets total number of children.
     * 
     * @return number of children in this container.
     */
    int getChildCount();

    /**
     * Affirms if this container is without any children.
     * 
     * @return true if this container is without any children.
     */
    boolean isEmpty();

    /**
     * Gets the children in order. The order is often, but not necessarily, the
     * order the child has been created/added to this container.
     * 
     * @return an unmodifiable list of children
     */
    List<C> getChildren();

    /**
     * Gets the name of the children in order. The order is often, but
     * necessarily, the order the child has been added to this container.
     * 
     * @return name of child elements
     */
    List<String> getChildrenNames();

    /**
     * Removes all children
     */
    public void removeAllChildren();

    /**
     * Refreshes itself and all it's children
     */
    public void recursiveRefresh();
}
