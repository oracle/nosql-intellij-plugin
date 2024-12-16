/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

@SuppressWarnings("HardCodedStringLiteral")
public class NoSqlIcons {
    public static final Icon SCHEMA_ICON  = load("/icons/schema.svg");
    public static final Icon TABLE = load("/icons/table.svg");
    public static final Icon COL_PRIMARY_KEY_ICON = load("/icons/colGoldKey.svg");
    public static final Icon COL_SHARD_KEY_ICON = load("/icons/colBlueKey.svg");
    public static final Icon COL_INDEX_KEY_ICON = load("/icons/colIndex.svg");
    public static final Icon COL_ICON = load("/icons/col.svg");
    public static final Icon COL_PRIMARY_SHARD_KEY = load("/icons/colGoldBlueKey.svg");
    public static final Icon GROUP_PRIMARY_KEY = load("/icons/goldKey.svg");
    public static final Icon GROUP_SHARD_KEY = load("/icons/blueKey.svg");
    public static final Icon GROUP_index_KEY = load("/icons/index.svg");
    public static final Icon ORACLE_LOGO = load("/icons/oracle.svg");
    private static Icon load(String path) {
        return IconLoader.getIcon(path,NoSqlIcons.class);
    }

}
