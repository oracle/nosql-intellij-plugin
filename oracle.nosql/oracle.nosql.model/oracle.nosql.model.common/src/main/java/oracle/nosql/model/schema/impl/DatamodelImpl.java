/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.schema.impl;

import java.util.List;

import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.Schema;

@SuppressWarnings("serial")
public class DatamodelImpl extends AbstractSchemaContainer<Datamodel, Schema>
        implements
        Datamodel {
    /**
     * Create a datamodel of given name
     * 
     * @param name name of the model. must not be null or empty
     */
    public DatamodelImpl(String name) {
        super(name);
    }

    @Override
    public List<String> getSchemaNames() {
        return getChildrenNames();
    }

    @Override
    public Schema getSchema(String name) {
        return getChild(name);
    }

    @Override
    public boolean removeSchema(String name) {
        return removeChild(name) != null;
    }

    @Override
    public boolean hasSchema(String name) {
        return hasChild(name);
    }

    @Override
    public boolean isFetched() {
        // Always true as it is not related to any server side element.
        return true;
    }

    @Override
    public void refresh() {
        // Does nothing
    }
}
