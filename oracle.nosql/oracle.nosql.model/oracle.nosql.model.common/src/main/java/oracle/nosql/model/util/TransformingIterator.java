/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.Iterator;

/**
 * an iterator that transforms each element as it iterates
 * 
 *
 * @param <I> the type of element of original iterator
 * @param <V> the type of element after transformation
 */
public class TransformingIterator<I, V> implements Iterator<V> {
    private final Iterator<I> del;
    private final Transformer<I, V> transformer;

    /**
     * Create an iterator that applies given transformer to each iterated
     * element
     * 
     * @param del original input iterator
     * @param transformer a transformation to be applied
     */
    public TransformingIterator(Iterator<I> del,
            Transformer<I, V> transformer) {
        super();
        this.del = del;
        this.transformer = transformer;
    }

    @Override
    public boolean hasNext() {
        return del.hasNext();
    }

    @Override
    public V next() {
        return transformer.transform(del.next());
    }
}
