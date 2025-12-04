/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
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
    private String tenantId;

    ExampleAccessTokenProvider(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getAuthorizationString(Request request) {
        return "Bearer " + tenantId;
    }

    @Override
    public void close() {}
}


