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
import oracle.nosql.model.profiletype.PublicCloud;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePageCache;

/**
 * ConnectionProfile implementation for PublicCloud.
 * 
 * @author Jashkumar Dave
 *
 */
@SuppressWarnings("serial")
public class PublicCloudConnectionProfile extends AbstractConnectionProfile
        implements
        IConnectionProfile<CloudConnection> {
    public PublicCloudConnectionProfile() {
        super(new PublicCloud());
    }
    
    @Override
    public CloudConnection getConnection() {
        return new CloudConnection(this);
    }

    @Override
    public PublicCloudConnectionProfile getDefault() {
    	PublicCloudConnectionProfile defaultProfile = new PublicCloudConnectionProfile();
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
    	PublicCloudConnectionProfile copy = new PublicCloudConnectionProfile();
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
    public String toString() {
        return getConnectionString();
    }

    @Override
    public PublicCloudConnectionProfile setProperty(String key, String value) {
        put(key, value);
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

	@Override
	public void setConnectionString(String connectionURL) {
		// TODO Auto-generated method stub
	}
}
