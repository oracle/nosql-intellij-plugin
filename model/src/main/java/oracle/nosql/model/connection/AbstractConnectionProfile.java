/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.util.Properties;

public abstract class AbstractConnectionProfile {
    private final IConnectionProfileType type;
    private String name;
    private final Properties properties;

    /**
     * Create a profile of given type.
     * 
     * @param type type of the profile.
     */
    public AbstractConnectionProfile(IConnectionProfileType type) {
        super();
        this.type = type;
        this.properties = new Properties();
    }

    protected final IConnectionProfileType _type() {
        return type;
    }

    public String getName() {
        return name;
    }

    protected void _setName(String name) {
        this.name = name;
    }

    public final Properties getProperties() {
        return (Properties) this.properties.clone();
    }

    public boolean isKnownPropertyName(String propertyName) {
        return type.isKnownPropertyName(propertyName);
    }

    public final Object getProperty(String key) {
        if (!isKnownPropertyName(key)) {
            throw new RuntimeException("unknown property name:" + key);
        }
        return properties.get(key);
    }

    protected void put(String key, Object value) {
        properties.put(key, value);
    }
}
