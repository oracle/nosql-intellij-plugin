/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.profiletype;

import oracle.nosql.model.connection.AbstractConnectionProfileType;
import oracle.nosql.model.connection.BasicConfigurableProperty;
import oracle.nosql.model.connection.ConfigurableProperty;
import oracle.nosql.model.connection.IConnectionProfileType;

import java.net.URL;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Cloudsim extends AbstractConnectionProfileType implements
        IConnectionProfileType {
    public static final String PROFILE_TYPE = "Cloudsim";

    public static final ConfigurableProperty CLOUDSIM_CONNECTION_NAME =
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
            new BasicConfigurableProperty(PROFILE_TYPE + "/service-url")
                    .setLabel("Service URL *")
                        .setDescription("Service URL")
                        .setDefaultValue("http://localhost:8080")
                        .setValidator(input -> {
                            // This is basic validation
                            try {
                                URL u = new URL(input);
                                u.toURI();
                            } catch (Exception e) {
                                return "Invalid service URL, please enter a valid service URL";
                            }
                            return null;
                        });

    public static final ConfigurableProperty PROPERTY_TENANT =
            new BasicConfigurableProperty("TENANT_ID")
                    .setLabel("Tenant identifier *")
                        .setDescription(
                                "Identifer for a multi-tenant cloud database service")
                        .setDefaultValue("exampleId")
                        .setValidator(input -> {
                            // This is basic validation
                            if (!Pattern.matches("[A-za-z0-9]+", input)) {
                                return "Invalid tenant identifier, enter a valid alphanumaric tenant identifier";
                            }
                            return null;
                        });

    public Cloudsim() {
        super();
        setDescription(
                "Multi-tenant database as a service in a simulated cloud environment");
        declareRequiredProperty(CLOUDSIM_CONNECTION_NAME,PROPERTY_URL, PROPERTY_TENANT);
    }

    @Override
    public String getName() {
        return PROFILE_TYPE;
    }
}
