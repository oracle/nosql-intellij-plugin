/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.io.Serializable;
import java.util.Properties;

import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePageCache;
import oracle.nosql.model.util.Named;

/**
 * A connection profile holds details that are sufficient to establish a
 * {@link IConnectionProfile#getConnection() connection}.
 * <p>
 * A profile is identified by a {@link #getName() moniker}. Besides, a concrete
 * profile specifies the properties it
 * {@link IConnectionProfileType#getRequiredProperties() requires} to create a
 * connection.
 * 
 * @author pinaki poddar
 *
 * @param <C> the type of connection created by this profile.
 */
public interface IConnectionProfile<
        C extends IConnection> extends Named, Serializable {
    /**
     * gets name of this profile. The name for a profile is a moniker rather
     * than an identifier.
     * 
     * @return name of this profile. Never null or empty.
     */
    @Override
    String getName();

    /**
     * Sets name of this profile.
     * 
     * @param name name of this profile. must not be null or empty
     * @return the same profile with name set
     */
    IConnectionProfile<?> setName(String name);

    /**
     * Get all properties of this profile.
     * 
     * @return a properties object whose keys are names (string) of
     * {@link ConfigurableProperty}.
     * 
     */
    Properties getProperties();

    /**
     * Gets an URL-like string to describe this profile.
     */
    String getConnectionString();

    /**
     * Sets an URL-like string for this profile. The profile may parse the
     * string to configure itself, overwriting all existing properties.
     * 
     * @param connectionURL connection string for this profile
     */
    void setConnectionString(String connectionURL);

    /**
     * Gets connection with current set of properties. The properties used are
     * accessible via {@link #getProperties()}.
     * 
     * @return gets a connection. The profile may cache connections.
     */
    C getConnection();

    /**
     * Gets the <em>default</em> profile. A default profile is populated with
     * {@link ConfigurableProperty#getDefaultValue() default values} of
     * {@link IConnectionProfileType#getRequiredProperties() required
     * properties}.
     * 
     * @return a profile with default properties.
     */
    IConnectionProfile<?> getDefault();

    /**
     * Gets the property value of the given key. The key should be
     * {@link IConnectionProfileType#isKnownPropertyName(String) known property
     * name}
     * 
     * @param key a property name
     * @return a property value. null if the property key is not recognized or
     * property value is null.
     */
    Object getProperty(String key);

    /**
     * Sets a property of given name with given string value.
     * 
     * @param key a property key. The profile may ignore unrecognized key.
     * 
     * @param value value of a property as a string. The profile may need to
     * convert/parse the given string to appropriate value type.
     * 
     * @return the same profile
     */
    IConnectionProfile<?> setProperty(String key, String value);

    /**
     * Creates a copy this profile.
     * 
     * @return a new copied profile with a new name.
     */
    IConnectionProfile<?> copy();

    /**
     * Gets the type of this profile.
     * 
     * @return a profile kind. never null.
     */
    IConnectionProfileType getType();

    /**
     * Parses the given result object into the {@link TablePageCache} instance.
     * 
     * @param result - output of query.
     * @param table - subject table in the query.
     * @return result as {@link TablePageCache} instance.
     */
    TablePageCache getTablePageCacheInstance(Object result, Table table);
}
