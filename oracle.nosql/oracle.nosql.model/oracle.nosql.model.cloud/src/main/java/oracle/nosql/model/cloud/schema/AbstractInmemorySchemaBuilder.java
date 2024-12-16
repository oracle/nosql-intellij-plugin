/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.schema;

import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.JsonReader;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Index;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.SchemaBuilder;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.schema.impl.AbstractSchemaBuilder;
import oracle.nosql.model.schema.impl.DatamodelImpl;
import oracle.nosql.model.schema.impl.IndexImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Builds datamodel elements in memory.
 *
 * @author pinaki poddar
 */
public abstract class AbstractInmemorySchemaBuilder extends
        AbstractSchemaBuilder implements
        SchemaBuilder {
    /**
     * Builds a datamodel from given JSON input.
     *
     * @param input an input stream/file containing JSON formatted string or a
     *              string itself. The content of JSON string is formatted by the database
     *              server. The string does not specify a namespace/schema name. This
     *              builder assumes that the schema name is same as the given datamodel
     *              name.
     */
    @Override
    public Datamodel build(String modelName, Object input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException(
                    "canot build from null JSON input");
        }
        Datamodel model = new DatamodelImpl(modelName);
        // schema name is same as model name
        Schema schema = newSchema(model, modelName, true);
        try {
            MapValue json = parseJsonSchema(input);
            parse(json, schema);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "failed to parse input json " + input);
        }
        return model;
    }

    /**
     * Populates given table by parsing given JSON descriptor.
     *
     * @param node JSON descriptor for a table as received from database
     */
    private void parse(MapValue node, Table table) {
        assertArrayProperty(node, "fields");
        if (node.get("primaryKey") != null) {
            node.get("primaryKey").asArray().forEach(pk -> {
                String fieldName = pk.getString();
                Field field = newField(table, fieldName, true);
                field.makePrimarKey();
            });
        }

        node.get("fields").asArray().forEach(fieldValue -> {
            MapValue fieldNode = fieldValue.asMap();
            assertProperty(fieldNode, "name");
            String fieldName = fieldNode.get("name").getString();
            Field field = null;
            if (table.hasField(fieldName)) {
                field = table.getChild(fieldName);
            } else {
                field = newField(table, fieldName, false);
            }
            parse(fieldNode, field);
        });

        if (node.get("shardKey") != null) {
            node.get("shardKey").asArray().forEach(sk -> {
                table.getChild(sk.getString()).makeShardKey();
            });
        } else { // shardkey is same as primarykey
            node.get("primaryKey").asArray().forEach(sk -> {
                table.getChild(sk.getString()).makeShardKey();
            });
        }

        if (node.get("indexes") != null) {
            assertArrayProperty(node, "indexes");
            node.get("indexes").asArray().forEach(fieldValue -> {
                MapValue indexNode = (MapValue) fieldValue;
                assertProperty(indexNode, "name");
                Index index =
                        newIndex(table, indexNode.getString("name"), true);
                parse(indexNode, index, table);
            });
        }
    }

    /**
     * Populates attributes of given field from given JSON node.
     *
     * @param node  a JSON node. Must have 'type' property.
     * @param field a field whose attributes to be populated.
     */
    private void parse(MapValue node, Field field) {
        assertProperty(node, "type");
        Field.Type type =
                Field.Type.valueOf(node.getString("type").toUpperCase());
        field.setType(type);
        field.setNullable(node.getBoolean("nullable"));
        if (node.get("default") != null) {
            field.setDefault(true);
            field.setDefault(node.get("default").getString());
        }
    }

    /**
     * Populates attributes of given index from given JSON node.
     *
     * @param node  a JSON node. Must have 'fields' property.
     * @param index an index whose attributes to be populated.
     */
    private void parse(MapValue node, Index index,Table table) {
        assertArrayProperty(node, "fields");
        if (index == null) {
            throw new IllegalArgumentException("can not populate null index");
        }
        if (index.getTable() == null) {
            throw new IllegalArgumentException(
                    "can not populate index with no owning table");
        }
        //Implemented this functionality to implement detailed schema of field type
        node.get("fields").asArray().forEach(fieldNode -> {
            Field field = null;
            String fieldName = fieldNode.getString();
            if (table.hasField(fieldName)) {
                Field fieldOriginal = index.getTable().getField(fieldName);
                field = newField(fieldOriginal.getName() + "|index");
                field.setType(fieldOriginal.getType());
                table.addField(field, false);
                table.removeChild(field.getName());
            }
              else {
                  field = newField(fieldName);
                  table.addField(field,false);
                  table.removeChild(fieldName);
              }
            index.addField(field);
        });
    }

    /**
     * Populates attributes of given schema from given JSON node.
     *
     * @param schemaNode - a JSON node. Must have 'fields' property.
     * @param schema     - a schema whose attributes to be populated.
     */
    private void parse(MapValue schemaNode, Schema schema) {
        assertArrayProperty(schemaNode, "tables");
        schemaNode.get("tables").asArray().forEach(fieldValue -> {
            MapValue tableNode = (MapValue) fieldValue;
            assertProperty(tableNode, "name");
            Table table =
                    newTable(schema, tableNode.getString("name"), true);
            parse(tableNode, table);
        });
    }

    /**
     * Populates attributes of given table from given description string.
     *
     * @param desc  - Table description as JSON string.
     * @param table - a table whose attributes to be populated.
     */
    void parse(String desc, Table table) {
        MapValue jn = null;
        try {
            jn = parseJsonSchema(desc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        parse(jn, table);
    }

    @Override
    public Datamodel newDatamodel(String name) {
        return new DatamodelImpl(name);
    }

    @Override
    public Index newIndex(String name) {
        return new IndexImpl(name);
    }

    /**
     * Asserts given node has given property.
     *
     * @param node     a non-null JSON node
     * @param property name of a property
     */
    private void assertProperty(MapValue node, String property) {
        if (node == null) {
            throw new RuntimeException(
                    "got null node for property '" + property + "' in JSON ");
        }
        if (node.get(property) == null) {
            throw new RuntimeException("missing property '" +
                    property +
                    "' in JSON " +
                    node.toString());
        }
    }

    /**
     * Asserts given node has given array property.
     *
     * @param node     a non-null JSON node
     * @param property name of an array property
     */
    private void assertArrayProperty(MapValue node, String property) {
        assertProperty(node, property);
        if (node.get(property).getType() != FieldValue.Type.ARRAY) {
            throw new RuntimeException("property '" +
                    property +
                    "' in JSON " +
                    node.toString() +
                    " is not array");
        }
    }

    private MapValue parseJsonSchema(Object input) {
        if (input instanceof InputStream) {
            try (JsonReader reader = new JsonReader((InputStream) input,
                    null)) {
                return reader.iterator().next();
            }
        } else if (input instanceof File) {
            try (JsonReader reader = new JsonReader((File) input,
                    null)) {
                return reader.iterator().next();
            }
        } else if (input instanceof String) {
            try (JsonReader reader = new JsonReader((String) input,
                    null)) {
                return reader.iterator().next();
            }
        } else {
            throw new IllegalArgumentException(
                    "invalid input type " + input.getClass());
        }

    }
}
