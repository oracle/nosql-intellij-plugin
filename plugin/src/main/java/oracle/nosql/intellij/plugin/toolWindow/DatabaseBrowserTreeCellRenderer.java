/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import oracle.nosql.intellij.plugin.common.NoSqlIcons;
import oracle.nosql.model.schema.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTree;
import java.util.*;

/**
 * Tree cell render which displays tree node wth image icons.
 *
 * @author amsundar
 */
class DatabaseBrowserTreeCellRenderer extends ColoredTreeCellRenderer {
    private Map<Table, Map<String, String>> fieldNameToTypeMap;
    private Project project;
    private SchemaParser schemaParser;

    DatabaseBrowserTreeCellRenderer(Project project) {
        fieldNameToTypeMap = new HashMap<>();
        this.project = project;
    }
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof SchemaElement) {
            if(value instanceof Schema) {
                setIcon(NoSqlIcons.SCHEMA_ICON);
            } else if(value instanceof Table) {
                setIcon(NoSqlIcons.TABLE);
            } else if(value instanceof Field) {
                Field field = (Field) value;
                if (field.isPrimaryKey()) {
                    if(field.isShardKey()) {
                        setIcon(NoSqlIcons.COL_PRIMARY_SHARD_KEY);
                    } else {
                        setIcon(NoSqlIcons.COL_PRIMARY_KEY_ICON);
                    }
                }
                else if(field.isShardKey()) {
                    setIcon(NoSqlIcons.COL_SHARD_KEY_ICON);
                }
                else if(field.isIndexKey()) {
                    setIcon(NoSqlIcons.COL_INDEX_KEY_ICON);
                }else {
                    setIcon(NoSqlIcons.COL_ICON);
                }
            } else if(value instanceof FieldGroup) {
                FieldGroup fieldGroup = (FieldGroup)(value);
                switch (fieldGroup.getType()) {
                    case PRIMARY_KEY:
                        setIcon(NoSqlIcons.GROUP_PRIMARY_KEY);
                        break;
                    case SHARD_KEY:
                        setIcon(NoSqlIcons.GROUP_SHARD_KEY);
                        break;
                    case INDEX_KEY:
                        setIcon(NoSqlIcons.GROUP_index_KEY);
                        break;
                        default:
                            break;
                }
            }
            if (value instanceof Field) {
                Field field = (Field) value;
                Map<String, String> map;
                if (fieldNameToTypeMap.containsKey(field.getTable())) {
                    map = fieldNameToTypeMap.get(field.getTable());
                } else {
                    schemaParser = new SchemaParser(field.getTable(), project);
                    map = schemaParser.getFieldNameToTypeMap();
                    fieldNameToTypeMap.put(field.getTable(), map);
                }
                if (field.getName().indexOf("|index.path") > 0) {
                    String fieldName = field.getName().substring(0, field.getName().indexOf("|"));
                    if (field.getIndexType() != null) {
                        fieldName += ":" + field.getIndexType();
                    }
                    append(fieldName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                } else if (field.getName().indexOf("|index") > 0) {
                    String name = field.getName().substring(0, field.getName().indexOf("|"));
                    append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                } else if (map.get(field.getName()) != null)
                    append(((Field) value).getName() + ":" + map.get(field.getName()), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                else append(field.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            } else {
                append(((SchemaElement) value).getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }
}

