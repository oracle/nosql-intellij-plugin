/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class which parses the table schema and get the exact field type for
 * a particular field and stores it in the map.
 * This map also contains the exact index fields types.
 *
 * @author kunalgup
 */

public class SchemaParser {
    Table table;
    private final Project project;
    public Map<String, String> fieldNameToTypeMap;

    SchemaParser(Table table, Project project) {
        this.table = table;
        this.project = project;
        fieldNameToTypeMap = new HashMap<>();

        //Filling the map with all the fields name and exact types.
        String schema = getSchema();
        List<String[]> list = parseFields(schema);
        for (String[] str : list)
            fieldNameToTypeMap.put(str[0], str[1]);

        //Filling the map with all the index name and exact location to that indexes
        list = parseIndex(schema);
        for (String[] str : list) {
            if (!fieldNameToTypeMap.containsKey(str[0])) fieldNameToTypeMap.put(str[0], str[1]);
        }
    }

    /**
     *Parse all the fields name and exact types.
     */
    private List<String[]> parseFields(String jsonSchema) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String[]> list = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonSchema);

            // Extract types of each field
            JsonNode fieldsNode = rootNode.get("fields");
            for (JsonNode fieldNode : fieldsNode) {
                String fieldName = fieldNode.path("name").asText();
                String fieldType = extractFieldType(fieldNode);
                list.add(new String[]{fieldName, fieldType});
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to parse Schema : " + e.getMessage());
        }
        return list;
    }

    private String extractFieldType(@NotNull JsonNode fieldNode) {
        String fieldType = fieldNode.path("type").asText();

        if ("ARRAY".equals(fieldType) || "MAP".equals(fieldType)) {
            JsonNode collectionNode = fieldNode.path("collection");
            if (!collectionNode.isMissingNode()) {
                return fieldType + "(" + extractFieldType(collectionNode) + ")";
            }
        } else if ("RECORD".equals(fieldType)) {
            JsonNode fieldsNode = fieldNode.path("fields");
            if (!fieldsNode.isMissingNode() && fieldsNode.isArray()) {
                StringBuilder recordType = new StringBuilder("RECORD(");
                for (JsonNode subFieldNode : fieldsNode) {
                    String subFieldName = subFieldNode.path("name").asText();
                    String subFieldType = extractFieldType(subFieldNode);
                    recordType.append(subFieldName).append(" ").append(subFieldType).append(", ");
                }
                if (recordType.length() > 9) { // Check if there are fields
                    recordType.setLength(recordType.length() - 2); // Remove the trailing comma and space
                }
                recordType.append(")");
                return recordType.toString();
            }
        } else if ("FIXED_BINARY".equals(fieldType)) {
            return "BINARY(" + fieldNode.path("size") + ")";

        }

        return fieldType;
    }

    /**
     *Parse all the index name and exact location of index.
     */
    private List<String[]> parseIndex(String jsonSchema) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String[]> list = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonSchema);
            JsonNode indexesNode = rootNode.path("indexes");
            for (JsonNode indexNode : indexesNode) {

                JsonNode fieldsNode = indexNode.path("fields");
                int idx = 0;
                for (JsonNode fieldNode : fieldsNode) {
                    String fieldName = fieldNode.asText();
                    String fieldType = getFieldAndType(indexNode, idx++);
                    list.add(new String[]{fieldName, fieldType});
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

    private String getFieldAndType(JsonNode rootNode, int idx) {
        JsonNode typesNode = rootNode.path("types");
        if (!typesNode.isMissingNode() && typesNode.isArray() && !typesNode.isEmpty()) {
            return typesNode.get(idx).asText();
        }

        return null;
    }

    /**
     *Method to fetch the schema of table using getSchema api.
     */
    private String getSchema() {
        IConnection con;
        String result;
        try {
            con = DBProject.getInstance(project).getConnection();
            try {
                result = con.showSchema(table);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to get Schema : " + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to get Schema : " + ex.getMessage());
        }
        return result;
    }

    public Map<String, String> getFieldNameToTypeMap() {
        return fieldNameToTypeMap;
    }
}
