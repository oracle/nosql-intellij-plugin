/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.Iterator;

/**
 * A specialized iterator where an element can be pushed back after it has been
 * iterated.
 *
 * @param <X> element type
 */
public class PushbackIterator<X> extends LazyIteratorChain<X> {
    private SingleItemIterator<X> pushedBack;
    private final Iterator<X> delegate;

    /**
     * create an iterator with the pushed back item.
     * 
     * @param single the single pushed back item which has always the first
     * item to iterate
     * @param del the following iterator
     */
    public PushbackIterator(X single, Iterator<X> del) {
        this.delegate = del;
        this.pushedBack = new SingleItemIterator<X>(single);
    }

    @Override
    protected final Iterator<X> nextIterator(int iteratorCount) {
        if (iteratorCount == 0) {
            return pushedBack;
        } else if (iteratorCount == 1) {
            return delegate;
        } else {
            return null;
        }
    }
}
