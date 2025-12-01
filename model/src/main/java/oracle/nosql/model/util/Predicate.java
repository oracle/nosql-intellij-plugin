/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

/**
 * A predicate.
 * 
 *
 * @param <E> the type on which the condition is evaluated
 */
@FunctionalInterface
public interface Predicate<E> {
    boolean isTrue(E e);
}
