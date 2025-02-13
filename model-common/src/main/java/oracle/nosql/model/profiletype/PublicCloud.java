/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.profiletype;

import oracle.nosql.model.connection.AbstractConnectionProfileType;
import oracle.nosql.model.connection.BasicConfigurableProperty;
import oracle.nosql.model.connection.ConfigurableProperty;
import oracle.nosql.model.connection.ConnectionFactory;
import oracle.nosql.model.connection.IConnectionProfileType;
import oracle.nosql.model.sdk.CloudsimSDK;
import oracle.nosql.model.sdk.SDK;

@SuppressWarnings("serial")
public class PublicCloud extends AbstractConnectionProfileType implements
        IConnectionProfileType {
    public static final String PROFILE_TYPE = "Cloud";
    public static final ConfigurableProperty CLOUD_CONNECTION_NAME =
            new BasicConfigurableProperty(PROFILE_TYPE + "/connection-name")
                    .setLabel("Connection Name *")
                    .setDescription("Connection Name")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Connection name cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_ENDPOINT =
            new BasicConfigurableProperty(PROFILE_TYPE + "/endpoint")
                    .setLabel("Endpoint *")
                        .setDescription("Data Region endpoint")
                        .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Endpoint cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_TENANTID =
            new BasicConfigurableProperty("TENANTID")
                    .setLabel("Tenant ID *")
                    .setDescription(
                            "OCID of your tenancy")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Tenant ID cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_USERID =
            new BasicConfigurableProperty("USERID")
                    .setLabel("User ID *")
                    .setDescription(
                            "OCID of the user calling the API.")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "User ID cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_FINGEPRINT =
            new BasicConfigurableProperty("FINGERPRINT")
                    .setLabel("Fingerprint *")
                    .setDescription(
                            "Fingerprint for the key pair being used.")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Fingerprint cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_PRIVATEKEY =
            new BasicConfigurableProperty("PRIVATEKEY")
                    .setLabel("Private Key *")
                    .setDescription(
                            "Full path and filename of the private key.")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Private Key cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_PASSPHRASE =
            new BasicConfigurableProperty("PASSPHRASE")
                    .setLabel("Passphrase")
                    .setDescription(
                            "Passphrase used for the key, if it is encrypted.")
                    .setDefaultValue("")
                    .setValidator(null);

    public static final ConfigurableProperty PROPERTY_COMPARTMENT =
            new BasicConfigurableProperty("COMPARTMENT")
                    .setLabel("Compartment")
                    .setDescription(
                            " Sets a default compartment.")
                    .setDefaultValue("")
                    .setValidator(null);

    public static final ConfigurableProperty SDK_PATH =
            new BasicConfigurableProperty("SDK_PATH")
                    .setLabel("SDK path *")
                        .setDescription("Select path to SDK folder")
                        .setDefaultValue("")
                        .setValidator(CloudsimSDK::validate);

    public PublicCloud() {
        super();
        setDescription(
                "Multi-tenant database as a service in a cloud environment");
        declareRequiredProperty(CLOUD_CONNECTION_NAME,PROPERTY_ENDPOINT, SDK_PATH,
                                PROPERTY_TENANTID, PROPERTY_USERID,
                                PROPERTY_FINGEPRINT, PROPERTY_PRIVATEKEY,
                                PROPERTY_PASSPHRASE, PROPERTY_COMPARTMENT);
    }

    @Override
    public String getName() {
        return PROFILE_TYPE;
    }

    /**
     * Register this profile to a connection factory.
     */
    static {
        try {
            ConnectionFactory.register((IConnectionProfileType) new PublicCloud(),
                    "oracle.nosql.model.cloud.connection.PublicCloudConnectionProfile");
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public SDK getSDK(String path) throws Exception {
        return new CloudsimSDK(path);
    }
}
