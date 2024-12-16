/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import oracle.nosql.model.util.NamedMap;

@SuppressWarnings("serial")
public abstract class AbstractConnectionProfileType implements
        IConnectionProfileType {
    private String description;
    private final NamedMap<ConfigurableProperty> requiredProperties;
    private final NamedMap<ConfigurableProperty> optionalProperties;
    private String[] versions;

    protected AbstractConnectionProfileType() {
        requiredProperties = new NamedMap<ConfigurableProperty>();
        optionalProperties = new NamedMap<ConfigurableProperty>();
    }

    public void setDescription(String desc) {
        this.description = desc == null ? "" : desc;
    }

    @Override
    public String getDescripton() {
        return description;
    }

    @Override
    public Collection<ConfigurableProperty> getRequiredProperties() {
        return requiredProperties.values();
    }

    @Override
    public Collection<ConfigurableProperty> getOptionalProperties() {
        return optionalProperties.values();
    }

    @Override
    public boolean isKnownPropertyName(String propertyName) {
        return requiredProperties.containsName(propertyName) ||
                optionalProperties.containsName(propertyName);
    }

    protected void
            declareRequiredProperty(ConfigurableProperty... properties) {
        if (properties == null)
            return;
        for (ConfigurableProperty p : properties) {
            if (p == null)
                continue;
            requiredProperties.add(p, true);
        }
    }

    protected void
            declareOptionalProperty(ConfigurableProperty... properties) {
        if (properties == null)
            return;
        for (ConfigurableProperty p : properties) {
            if (p == null)
                continue;
            optionalProperties.add(p, true);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
                ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractConnectionProfileType other =
                (AbstractConnectionProfileType) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equalsIgnoreCase(other.getName()))
            return false;
        return true;
    }

    /**
     * Gets name of this profile type.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Looks for a resource named <code>{name}.properties</code> in classpath .
     */
    @Override
    public String[] getAvailableVersions() {
        if (versions != null) {
            return versions;
        } else {
            String rsrc = getName() + ".properties";
            versions = readVersions(rsrc);
        }
        return versions;
    }

    /**
     * Read from a properties file, if available, for version.
     * 
     * @param rsrc name of a configuration resource in java.util.Properties
     * format
     * @return an empty array if rsrc can not be found or properties file does
     * not have 'versions' property. never null.
     */
    String[] readVersions(String rsrc) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(rsrc);
            if (in == null) {
                System.err
                        .println("***WARN:can not find configuration " + rsrc);
                return new String[0];
            }
            Properties p = new Properties();
            p.load(in);
            if (!p.containsKey("versions")) {
                return new String[0];
            }
            String[] tokens = p.getProperty("versions").split(",");
            List<String> result = new ArrayList<String>();
            for (int i = 0; i < tokens.length; i++) {
                result.add(tokens[i].trim());
            }
            if (result.isEmpty()) {
                System.err.println("***WARN: Found no versions " +
                        " for " +
                        getName() +
                        " profile");
            } else {
                System.err.println("[INFO] Found versions " +
                        result +
                        " for " +
                        getName() +
                        " profile");
            }
            Collections.sort(result);
            return result.toArray(new String[result.size()]);
        } catch (IOException ex) {
            System.err.println("***WARN:can not read configuration " +
                    rsrc +
                    " due to " +
                    ex.getMessage());
        }
        return new String[0];
    }
}
