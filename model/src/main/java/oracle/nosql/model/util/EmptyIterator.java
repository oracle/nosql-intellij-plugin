/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * an empty iterator has no element to iterate.
 * 
 *
 */
@SuppressWarnings("rawtypes")
public class EmptyIterator implements Iterator {
    public static final Iterator<?> INSTANCE = new EmptyIterator();

    /**
     * Always return false.
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * Always throws exception.
     * 
     * @exception NoSuchElementException always throws
     */
    @Override
    public Object next() {
        throw new NoSuchElementException("empty iterator has no element");
    }
}
