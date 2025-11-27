/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.util.function.Function;

@SuppressWarnings("serial")
public class BasicConfigurableProperty implements ConfigurableProperty {
    private final String name;
    private String label;
    private String description;
    private String defaultValue;
    private Class<?> type;
    private Function<String, String> validator;

    public BasicConfigurableProperty(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "can not create property with " + " or empty name");
        }
        this.name = name;
        this.label = name;
        this.description = "";
        this.defaultValue = "";
        this.type = String.class;
        this.validator = input -> {
            return null;
        };
    }

    /**
     * Gets (immutable) name of this property.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public BasicConfigurableProperty setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public BasicConfigurableProperty setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public BasicConfigurableProperty setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public BasicConfigurableProperty setType(Class<?> type) {
        this.type = type;
        return this;
    }

    @Override
    public Function<String, String> getValidator() {
        return validator;
    }

    public BasicConfigurableProperty
            setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }
}
