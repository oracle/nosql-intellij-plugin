/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.connection;

import oracle.nosql.model.cloud.table.ui.CloudTablePageCache;
import oracle.nosql.model.connection.AbstractConnectionProfile;
import oracle.nosql.model.connection.ConfigurableProperty;
import oracle.nosql.model.connection.IConnectionProfile;
import oracle.nosql.model.connection.IConnectionProfileType;
import oracle.nosql.model.profiletype.Cloudsim;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePageCache;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ConnectionProfile implementation for CloudSim.
 * 
 * @author Jashkumar Dave
 *
 */
@SuppressWarnings("serial")
public class CloudConnectionProfile extends AbstractConnectionProfile
        implements
        IConnectionProfile<CloudConnection> {
    public CloudConnectionProfile() {
        super(new Cloudsim());
    }


    CloudConnectionProfile setConnectionName(String name) {
        put(Cloudsim.CLOUDSIM_CONNECTION_NAME.getName(), name);
        return this;
    }

    /**
     * Setter method for URL attribute of this profile
     * 
     * @param url
     * @return CloudConnectionProfile for chaining methods.
     */
    CloudConnectionProfile setUrl(String url) {
        try {
            setUrl(new URL(url));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    /**
     * Setter method for URL attribute of this profile
     * 
     * @param url
     * @return CloudConnectionProfile for chaining methods.
     */
    CloudConnectionProfile setUrl(URL url) {
        put(Cloudsim.PROPERTY_URL.getName(), url);
        return this;
    }

    /**
     * Setter method for tenantId attribute of this profile
     * 
     * @param tenantId
     * @return CloudConnectionProfile for chaining methods.
     */
    CloudConnectionProfile setTenantId(String tenantId) {
        put(Cloudsim.PROPERTY_TENANT.getName(), tenantId);
        return this;
    }

    public String getConnectionName(){
        return (String) getProperty(Cloudsim.CLOUDSIM_CONNECTION_NAME.getName());
    }

    /**
     * Getter for URL.
     * 
     * @return service URL.
     */
    public URL getServiceUrl() {
        return (URL) getProperty(Cloudsim.PROPERTY_URL.getName());
    }

    /**
     * Getter for Tenant ID.
     * 
     * @return tenant ID.
     */
    public String getTenantId() {
        return (String) getProperty(Cloudsim.PROPERTY_TENANT.getName());
    }

    @Override
    public CloudConnection getConnection() {
        return new CloudConnection(this);
    }

    @Override
    public CloudConnectionProfile getDefault() {
        CloudConnectionProfile defaultProfile = new CloudConnectionProfile();
        defaultProfile.setName("default");
        for (ConfigurableProperty property : getType()
                .getRequiredProperties()) {
            defaultProfile.setProperty(property.getName(),
                    property.getDefaultValue());
        }
        return defaultProfile;
    }

    @Override
    public IConnectionProfile<CloudConnection> copy() {
        CloudConnectionProfile copy = new CloudConnectionProfile();
        copy.setName("copy of " + this.getName());
        for (String propertyName : getProperties().stringPropertyNames()) {
            copy.put(propertyName, this.getProperty(propertyName));
        }
        return copy;
    }

    @Override
    public String getConnectionString() {
        return (getTenantId().isEmpty() ? "" : getTenantId() + '@') +
                getServiceUrl();
    }

    @Override
    public void setConnectionString(String s) {
        if (s == null) {
            throw new IllegalArgumentException(
                    "invalid connection string " + s);
        }
        try {
            URL tempUrl = new URL(s);
            setUrl(tempUrl);
            setTenantId(tempUrl.getAuthority());
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(
                    "invalid connection string " + s,
                    ex);
        }
    }

    @Override
    public String toString() {
        return getConnectionString();
    }

    @Override
    public CloudConnectionProfile setProperty(String key, String value) {
        if (Cloudsim.CLOUDSIM_CONNECTION_NAME.getName().equalsIgnoreCase(key)) {
            setConnectionName(value);
        }else if (Cloudsim.PROPERTY_URL.getName().equalsIgnoreCase(key)) {
            setUrl(value);
        } else if (Cloudsim.PROPERTY_TENANT.getName().equalsIgnoreCase(key)) {
            setTenantId(value);
        } else {
            System.err.println("***WARNING: unknown property " +
                    key +
                    " value " +
                    value +
                    " is ignored");
        }
        return this;
    }

    @Override
    public IConnectionProfile<?> setName(String name) {
        super._setName(name);
        return this;
    }

    @Override
    public IConnectionProfileType getType() {
        return super._type();
    }

    @Override
    public TablePageCache getTablePageCacheInstance(Object result,
            Table table) {
        TablePageCache cloudTablePageCache = null;
        cloudTablePageCache = new CloudTablePageCache();
        cloudTablePageCache.setResult(result, table);
        return cloudTablePageCache;
    }
}
