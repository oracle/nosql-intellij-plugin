/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.ArrayList;

/**
 * A list where an element can be added only if it satisfies a condition.
 * 
 *
 * @param <E> type of element in the list
 */
@SuppressWarnings("serial")
public class PredicatedList<E> extends ArrayList<E> {
    private final Predicate<E> condition;

    /**
     * Create the list with given condition.
     * 
     * @param predicate a condition
     */
    public PredicatedList(Predicate<E> predicate) {
        condition = predicate;
    }

    /**
     * Adds the element to this list only if it satisfies the condition.
     * 
     * @exception IllegalArgumentException if the given element does not
     * satisfy condition
     */
    @Override
    public boolean add(E e) {
        if (condition.isTrue(e)) {
            return super.add(e);
        } else {
            throw new IllegalArgumentException(
                    e + " is not allowd to be added");
        }
    }
}
