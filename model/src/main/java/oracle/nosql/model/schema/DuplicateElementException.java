/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema;

/**
 * An exception raised when a schema element of same name is added to a
 * container.
 *
 */
@SuppressWarnings("serial")
public class DuplicateElementException extends RuntimeException {
    private SchemaElement<?> element;

    public DuplicateElementException(String msg) {
        super(msg);
    }

    public DuplicateElementException(SchemaElement<?> e, String msg) {
        super(msg);
        this.element = e;
    }

    public DuplicateElementException(SchemaElement<?> e,
            String msg,
            Exception ex) {
        super(msg, ex);
        this.element = e;
    }

    public SchemaElement<?> getElement() {
        return element;
    }
}
