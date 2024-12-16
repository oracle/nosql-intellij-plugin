/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.io.Serializable;
import java.util.Collection;

import oracle.nosql.model.sdk.SDK;

/**
 * A profile type describes the properties of a {@link IConnectionProfile
 * profile}.
 * 
 * @author pinaki poddar
 *
 */
public interface IConnectionProfileType extends Serializable {
    /**
     * A name for this type such as <code>Cloudsim</code> or <code>KV</code>.
     * 
     * @return a name. not null or empty.
     */
    public String getName();

    /**
     * Gets a detailed description of this kind.
     * 
     * @return a description. not null, but can be empty.
     */
    public String getDescripton();

    /**
     * Gets the property keys that must be provided to establish a connection.
     * 
     * @return a set of keys that must be specified to profile. Empty if no
     * property is required.
     */
    Collection<ConfigurableProperty> getRequiredProperties();

    /**
     * Gets the property keys that should be provided to establish a connection
     * or configure a connection after it has been established.
     * 
     * @return a set of keys that should be specified to profile. Empty if no
     * property is required.
     */
    Collection<ConfigurableProperty> getOptionalProperties();

    /**
     * Affirms if given property name is required or optional.
     * 
     * @param propertyName a property name
     * @return true if required or optional property name.
     */
    boolean isKnownPropertyName(String propertyName);

    /**
     * Gets a list of versions available for this profile type.
     * 
     * @return a list of versions of this profile type. An empty list if no
     * version is explicitly known. Never null.
     */
    String[] getAvailableVersions();

    /**
     * Given the path to SDK's folder this function returns the SDK instance
     * 
     * @param path - path to SDK's folder
     * @return {@link SDK} instance
     * @throws Exception if path/SDK is invalid.
     */
    public SDK getSDK(String path) throws Exception;
}