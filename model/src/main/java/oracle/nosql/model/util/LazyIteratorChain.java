/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A chain of iterators fetch data lazily. Once an iterator is exhausted,
 * {@link LazyIteratorChain#nextIterator(int) next iterator} is supplied by the
 * concrete implemention.
 * 
 * @author pinaki poddar
 *
 * @param <E> type of iterated element.
 */
public abstract class LazyIteratorChain<E> implements Iterator<E> {
    private Iterator<E> current;
    private int iteratorCount = -1;

    /**
     * Creates a chain of iterators with first iterator.
     * 
     */
    public LazyIteratorChain() {
    }

    @Override
    public final boolean hasNext() {
        if (current == null) {
            current = nextIterator(++iteratorCount);
        }
        while(current != null && !current.hasNext()) {
            current = nextIterator(++iteratorCount);
        }
        return current != null ? true : false;
    }

    @Override
    public final E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current.next();
    }

    /**
     * Implement this method.
     * 
     * @param iteratorCount number of times this method is called. At
     * construction, this method will be called with 0
     * 
     * @return null to terminate iteration.
     */
    protected abstract Iterator<E> nextIterator(int iteratorCount);
}
