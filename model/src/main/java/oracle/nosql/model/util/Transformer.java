/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.List;

/**
 * Transformation function.
 * 
 *
 * @param <I> input type to transform
 * @param <O> transformed output type
 */
public interface Transformer<I, O> {
    /**
     * Transform given input.
     * 
     * @param input an input to transform
     * @return the transformed output
     */
    O transform(I input);

    /**
     * Transform each element of given list.
     * 
     * @param original a list of elements to transform
     * @return the transformed output of each element in the same input order
     */
    List<O> transform(List<I> original);
}
