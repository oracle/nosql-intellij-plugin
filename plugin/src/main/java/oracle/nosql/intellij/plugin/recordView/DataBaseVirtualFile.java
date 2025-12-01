/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.testFramework.LightVirtualFile;
import oracle.nosql.model.schema.Table;

public class DataBaseVirtualFile extends LightVirtualFile {
    private final Table table;
    public DataBaseVirtualFile(Table table) {
        super(table.getName());
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;

        if(obj instanceof  DataBaseVirtualFile) {
            return ((DataBaseVirtualFile) obj).table.getName().equals(this.table.getName());
        }
        return false;
    }
}
