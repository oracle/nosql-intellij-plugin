/*
 * Copyright (C) 2019, 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */

package oracle.nosql.model.connection;

import oracle.nosql.model.profiletype.Cloudsim;
import oracle.nosql.model.profiletype.Onprem;
import oracle.nosql.model.profiletype.PublicCloud;
import oracle.nosql.model.cloud.connection.CloudConnectionProfile;
import oracle.nosql.model.cloud.connection.PublicCloudConnectionProfile;
import oracle.nosql.model.cloud.connection.OnpremConnectionProfile;


/**
 * ConnectionFactory creates {@link IConnectionProfile connection profile}.
 * Each concrete profile requires its own specific properties to be configured.
 * @author lsatpal
 *
 */
public class ConnectionFactory {
    /**
     * Creates profile of given type.
     *
     * @param profileType the type name of the profile. Has to be one of
     *                    {@link #getProfileTypes()}.
     * @param profileName the name of the profile created.
     * @return a connection profile.
     * @throws Exception
     */
    public static IConnectionProfile<?> createProfile(
            IConnectionProfileType profileType,
            String profileName) throws RuntimeException {
        if (profileType instanceof PublicCloud) {
            PublicCloudConnectionProfile p = new PublicCloudConnectionProfile();
            p.setName(profileName);
            return p;
        }

        if (profileType instanceof Onprem) {
            OnpremConnectionProfile p = new OnpremConnectionProfile();
            p.setName(profileName);
            return p;
        }

        if (profileType instanceof Cloudsim) {
            CloudConnectionProfile p = new CloudConnectionProfile();
            p.setName(profileName);
            return p;
        }

        throw new RuntimeException("Unknown profile type: " + profileType);
    }

    /**
     * Gets the profile types registered to this factory.
     *
     * @return an array of profile types registered to this factory.
     */
    public static IConnectionProfileType[] getProfileTypes() {
        return new IConnectionProfileType[]{
                new PublicCloud(),
                new Onprem(),
                new Cloudsim()
        };
    }

    /**
     * Gets one of the registered/known profile type by given name
     *
     * @param profileTypeName name of the profile type. The name is matched
     *                        ignoring case.
     * @return a profile type
     * @throws RuntimeException if no profile type of given name is known
     */
    public static IConnectionProfileType
    getProfileTypeByName(String profileTypeName) {
        String key = profileTypeName.toLowerCase();

        if (new PublicCloud().getName().equalsIgnoreCase(key)) {
            return new PublicCloud();
        }

        if (new Onprem().getName().equalsIgnoreCase(key)) {
            return new Onprem();
        }

        if (new Cloudsim().getName().equalsIgnoreCase(key)) {
            return new Cloudsim();
        }

        throw new RuntimeException(
                "Unknown profile type: " + profileTypeName
                        + ". Expected one of: PublicCloud, Onprem, Cloudsim"
        );

    }
}
