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
import oracle.nosql.model.profiletype.Onprem;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePageCache;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ConnectionProfile implementation for Proxy.
 *
 */
@SuppressWarnings("serial")
public class OnpremConnectionProfile extends AbstractConnectionProfile
        implements
        IConnectionProfile<CloudConnection> {
    public OnpremConnectionProfile() {
        super(new Onprem());
    }

    /**
     * Setter method for URL attribute of this profile
     *
     * @param url
     * @return OnpremConnectionProfile for chaining methods.
     */
    OnpremConnectionProfile setConnectionName(String name){
        put(Onprem.ONPREM_CONNECTION_NAME.getName(),name);
        return this;
    }
    OnpremConnectionProfile setNamespace(String name){
        put(Onprem.PROPERTY_NAMESPACE.getName(),name);
        return this;
    }
    OnpremConnectionProfile setUrl(String url) {
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
    OnpremConnectionProfile setUrl(URL url) {
        put(Onprem.PROPERTY_URL.getName(), url);
        return this;
    }

    /**
     * Setter method for username attribute of this profile
     *
     * @param username
     * @return OnpremConnectionProfile for chaining methods.
     */
    OnpremConnectionProfile setUsername(String username) {
        put(Onprem.USER_NAME.getName(), username);
        return this;
    }

    /**
     * Setter method for password attribute of this profile
     *
     * @param password
     * @return OnpremConnectionProfile for chaining methods.
     */
    OnpremConnectionProfile setPassword(String password) {
        put(Onprem.PASSWORD.getName(), password);
        return this;
    }

    /**
     * Setter method for security attribute of this profile
     *
     * @param security
     * @return OnpremConnectionProfile for chaining methods.
     */
    OnpremConnectionProfile setSecurity(String security) {
        put(Onprem.SECURITY.getName(), security);
        return this;
    }

    /**
     * Setter method for truststore attribute of this profile
     *
     * @param trustStore
     * @return OnpremConnectionProfile for chaining methods.
     */
    OnpremConnectionProfile setTrustStore(String trustStore) {
        put(Onprem.TRUST_STORE.getName(), trustStore);
        return this;
    }

    /**
     * Setter method for passphrase attribute of this profile
     *
     * @param passphrase
     * @return OnpremConnectionProfile for chaining methods.
     */
    OnpremConnectionProfile setPassphrase(String passphrase) {
        put(Onprem.TS_PASSPHRASE.getName(), passphrase);
        return this;
    }

    public String getConnectionName(){
        return (String) getProperty(Onprem.ONPREM_CONNECTION_NAME.getName());
    }

    public String getNamespace(){
        return (String) getProperty(Onprem.PROPERTY_NAMESPACE.getName());
    }

    /**
     * Getter for URL.
     *
     * @return service URL.
     */
    public URL getServiceUrl() {
        return (URL) getProperty(Onprem.PROPERTY_URL.getName());
    }

    /**
     * Getter for USERNAME.
     *
     * @return username.
     */
    public String getUsername() {
        return (String) getProperty(Onprem.USER_NAME.getName());
    }

    /**
     * Getter for PASSWORD.
     *
     * @return password.
     */
    public String getPassword() {
        return (String) getProperty(Onprem.PASSWORD.getName());
    }

    /**
     * Getter for SECURITY.
     *
     * @return security.
     */
    public String getSecurity() { return (String) getProperty(Onprem.SECURITY.getName()); }

    /**
     * Getter for trustStore.
     *
     * @return trustStore.
     */
    public String getTrustStore() { return (String) getProperty(Onprem.TRUST_STORE.getName()); }

    /**
     * Getter for passphrase.
     *
     * @return passphrase.
     */
    public String getPassphrase() { return (String) getProperty(Onprem.TS_PASSPHRASE.getName()); }

    @Override
    public CloudConnection getConnection() {
        return new CloudConnection(this);
    }

    @Override
    public OnpremConnectionProfile getDefault() {
        OnpremConnectionProfile defaultProfile = new OnpremConnectionProfile();
        defaultProfile.setName("default");
        for (ConfigurableProperty property : getType()
                .getRequiredProperties()) {
            defaultProfile.setProperty(property.getName(),
                    property.getDefaultValue());
        }
        for (ConfigurableProperty Optproperty : getType()
                .getOptionalProperties()) {
            defaultProfile.setProperty(Optproperty.getName(),
                    Optproperty.getDefaultValue());
        }
        return defaultProfile;
    }

    @Override
    public IConnectionProfile<CloudConnection> copy() {
        OnpremConnectionProfile copy = new OnpremConnectionProfile();
        copy.setName("copy of " + this.getName());
        for (String propertyName : getProperties().stringPropertyNames()) {
            copy.put(propertyName, this.getProperty(propertyName));
        }
        return copy;
    }

    @Override
    public String getConnectionString() {
        return ".";
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
    public OnpremConnectionProfile setProperty(String key, String value) {
        if(Onprem.ONPREM_CONNECTION_NAME.getName().equalsIgnoreCase(key)) {
            setConnectionName(value);
        } else if (Onprem.PROPERTY_URL.getName().equalsIgnoreCase(key)) {
            setUrl(value);
        } else if (Onprem.USER_NAME.getName().equalsIgnoreCase(key)) {
            setUsername(value);
        } else if (Onprem.PASSWORD.getName().equalsIgnoreCase(key)) {
            setPassword(value);
        } else if (Onprem.SECURITY.getName().equalsIgnoreCase(key)) {
            setSecurity(value);
        } else if (Onprem.TRUST_STORE.getName().equalsIgnoreCase(key)) {
            setTrustStore(value);
        } else if (Onprem.TS_PASSPHRASE.getName().equalsIgnoreCase(key)) {
            setPassphrase(value);
        } else if(Onprem.PROPERTY_NAMESPACE.getName().equalsIgnoreCase(key)){
            setNamespace(value);
        }
            else {
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