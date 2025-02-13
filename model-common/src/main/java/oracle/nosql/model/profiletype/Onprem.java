/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.profiletype;

import oracle.nosql.model.connection.*;
import oracle.nosql.model.sdk.CloudsimSDK;
import oracle.nosql.model.sdk.SDK;

import java.net.URL;

@SuppressWarnings("serial")
public class Onprem extends AbstractConnectionProfileType implements
        IConnectionProfileType {
    public static final String PROFILE_TYPE = "Onprem";

    public static final ConfigurableProperty ONPREM_CONNECTION_NAME =
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

    public static final ConfigurableProperty PROPERTY_URL =
            new BasicConfigurableProperty(PROFILE_TYPE + "/proxy-url")
                    .setLabel("Proxy URL *")
                    .setDescription("Proxy URL")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        try {
                            URL u = new URL(input);
                            u.toURI();
                        } catch (Exception e) {
                            return "Invalid proxy URL, please enter a valid proxy URL";
                        }
                        return null;
                    });

    public static final ConfigurableProperty SDK_PATH =
            new BasicConfigurableProperty("SDK_PATH")
                    .setLabel("SDK path *")
                    .setDescription("Select path to Nosql Java SDK folder")
                    .setDefaultValue("")
                    .setValidator(CloudsimSDK::validate);

    public static final ConfigurableProperty SECURITY =
            new BasicConfigurableProperty("SECURITY")
                    .setLabel("Security")
                    .setDescription("")
                    .setDefaultValue("SSL")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Username/Password/TrustStore/Passphrase cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty USER_NAME =
            new BasicConfigurableProperty("USER_NAME")
                    .setLabel("Username")
                    .setDescription(
                            "Username to connect to the secure Store")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Username cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PASSWORD =
            new BasicConfigurableProperty("PASSWORD")
                    .setLabel("Password")
                    .setDescription(
                            "Password to connect to the secure Store")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "Password cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty TRUST_STORE =
            new BasicConfigurableProperty("TRUST_STORE")
                    .setLabel("TrustStore")
                    .setDescription("Select TrustStore file path")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "TrustStore cannot be empty";
                        }
                        return null;
                    });
    public static final ConfigurableProperty TS_PASSPHRASE =
            new BasicConfigurableProperty("TS_PASSPHRASE")
                    .setLabel("Passphrase")
                    .setDescription("Passphrase of the TrustStore")
                    .setDefaultValue("")
                    .setValidator(input -> {
                        if (input.equals("")) {
                            return "TrustStore Passphrase cannot be empty";
                        }
                        return null;
                    });

    public static final ConfigurableProperty PROPERTY_NAMESPACE =
            new BasicConfigurableProperty("NAMESPACE")
                    .setLabel("Namespace")
                    .setDescription(
                            " Sets a default namespace ")
                    .setDefaultValue("")
                    .setValidator(null);

    public Onprem() {
        super();
        setDescription(
                "HTTP Proxy based Onprem Store access");
        declareRequiredProperty(ONPREM_CONNECTION_NAME,PROPERTY_URL, SDK_PATH,PROPERTY_NAMESPACE, SECURITY);
        declareOptionalProperty(USER_NAME,PASSWORD,TRUST_STORE,TS_PASSPHRASE);
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
            ConnectionFactory.register((IConnectionProfileType) new Onprem(),
                    "oracle.nosql.model.cloud.connection.OnpremConnectionProfile");
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public SDK getSDK(String path) throws Exception {
        return new CloudsimSDK(path);
    }
}