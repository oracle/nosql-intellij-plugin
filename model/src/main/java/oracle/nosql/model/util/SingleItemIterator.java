/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.Iterator;

/**
 * An iterator of a single element.
 * 
 *
 * @param <X> the type of single element
 */
public class SingleItemIterator<X> implements Iterator<X> {
    private boolean hasNext = true;
    private final X single;

    public SingleItemIterator(X obj) {
        single = obj;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public X next() {
        if (hasNext) {
            hasNext = false;
            return single;
        } else {
            throw new IllegalStateException();
        }
    }
}
