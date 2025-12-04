/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

/**
 * Anything with a non-null, non-empty name. The name of an element is used as
 * identifier.
 * 
 *
 */
public interface Named {
    String getName();
}
