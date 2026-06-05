/*
* Copyright (C) 2019, 2026 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.connection;

import oracle.nosql.driver.AuthorizationProvider;
import oracle.nosql.driver.ops.Request;


/**
 * Simple AccessTokenProvider implementation.
 * 
 * @author Jashkumar Dave
 *
 */

public class ExampleAccessTokenProvider implements AuthorizationProvider {
    private final String tenantId;

    ExampleAccessTokenProvider(String tenantId) {
        /*
         * This value is emitted as the Authorization bearer token. Fail closed
         * if it is blank or still set to the old public example value.
         */
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "CloudSim tenant identifier cannot be empty");
        }
        if ("exampleId".equals(tenantId.trim())) {
            throw new IllegalArgumentException(
                    "CloudSim tenant identifier must not use the example value");
        }
        this.tenantId = tenantId;
    }

    @Override
    public String getAuthorizationString(Request request) {
        return "Bearer " + tenantId;
    }

    @Override
    public void close() {}
}


