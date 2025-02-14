/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.io.Serializable;
import java.util.function.Function;

import oracle.nosql.model.util.Named;

/**
 * Property of a {@link IConnectionProfile connection profile}. The property
 * values are always string. The profile transforms the property value to
 * appropriate type when {@link IConnectionProfile#setProperty(String, String)
 * set}.
 * 
 * @author pinaki poddar
 *
 */
public interface ConfigurableProperty extends Named, Serializable {
    /**
     * A short descriptive label for this property. May be used in a UI.
     * 
     * @return a string. not null or empty.
     */
    String getLabel();

    /**
     * A detailed description for this property. May be used in a UI.
     * 
     * @return a string. not null.
     */
    String getDescription();

    /**
     * Gets default value of this property.
     * 
     * @return a default value.
     */
    String getDefaultValue();

    /**
     * Gets the type of value of this property.
     * 
     * @return type of the property. String, by default
     */
    Class<?> getType();

    /**
     * Returns the validator function for this property.
     * 
     * @return A function that validates the input string for this property and
     * returns the error message or null if no error.
     */
    Function<String, String> getValidator();
}
