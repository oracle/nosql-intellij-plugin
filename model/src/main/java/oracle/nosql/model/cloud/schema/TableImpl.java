/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.schema;

import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.impl.AbstractTable;

/**
 * Table implementation for CloudSim.
 * 
 * @author Jashkumar Dave
 *
 */
@SuppressWarnings("serial")
public class TableImpl extends AbstractTable {
    private boolean fetched = false;

    public TableImpl(String name) {
        super(name);
    }

    public TableImpl(Schema schema, String name) {
        super(schema, name);
    }

    @Override
    public boolean isFetched() {
        return fetched;
    }

    @Override
    public void refresh() {
        CloudSchemaBuilder builder =
                CloudSchemaBuilder.class.cast(getSchema().getSchemaBuilder());
        reset();
        builder.refresh(this);
        fetched = true;
    }
}
