/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.schema;

import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.impl.AbstractSchema;

/**
 * Schema implementation for CloudSim.
 * 
 * @author pinaki poddar
 * @author Jashkumar Dave
 *
 */
@SuppressWarnings("serial")
public class SchemaImpl extends AbstractSchema {
    private boolean fetched = false;

    public SchemaImpl(String name) {
        super(name);
    }

    public SchemaImpl(Datamodel model, String name) {
        super(model, name);
    }

    @Override
    public boolean isFetched() {
        return fetched;
    }

    @Override
    public void refresh() {
        CloudSchemaBuilder builder =
                CloudSchemaBuilder.class.cast(getSchemaBuilder());
        removeAllChildren();
        builder.refresh(this);
        fetched = true;
    }
}
