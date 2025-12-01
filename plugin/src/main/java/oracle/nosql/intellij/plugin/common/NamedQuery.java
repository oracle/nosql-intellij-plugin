/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import oracle.nosql.model.schema.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/*
 * This class is DB dependent.
 */
/**
 * A named query maintains different parts of a query such as projected fields,
 * where clause etc.
 * <p>
 * A named query is associated to a single {@link Table table} as queries on
 * Oracle NoSQL database target a single table.
 *
 *
 * @author pinaki poddar
 *
 */
@SuppressWarnings({"SameParameterValue", "WeakerAccess", "unused"})
public class NamedQuery {
    private String name;
    private final Table table;
    private List<String> projections;
    private String whereClause;
    private static final String SELECT = "SELECT "; //$NON-NLS-1$
    private static final String FROM = " FROM "; //$NON-NLS-1$
    private static final String WHERE = " WHERE "; //$NON-NLS-1$
    private static final String ALL = "*"; //$NON-NLS-1$
    private static final String NEWLINE = " \r\n\t "; //$NON-NLS-1$

    /**
     * Creates a query for entire extent of the given table. The name of the
     * query is <code>All {table-name}</code>. All fields of the table is
     * selected by the query.
     *
     * @param table a non-null table
     */
    public NamedQuery(Table table) {
        this("All " + table.getName(), table); //$NON-NLS-1$
    }

    /**
     * Creates a query for entire extent of the given table.
     *
     * @param name name of the query
     * @param table a non-null table
     */
    public NamedQuery(String name, Table table) {
        this(name, table, null, null);
    }

    /**
     * Creates a query of given name on given table with given set of selected
     * fields and give where condition.
     *
     * @param name name of the query
     * @param table a non-null target table
     * @param projections a list of selected fields. null implies all fields of
     * table are selected. must not be empty.
     * @param where a where clause without WHERE keyword. null implies no where
     * condition.
     */
    public NamedQuery(String name,
                      Table table,
                      List<String> projections,
                      String where) {
        this.name = name;
        this.table = table;
        setProjections(projections);
        setWhereClause(where);
    }

    /**
     * affirms if all fields of table are selected
     *
     * @return true if all fields of table are selected by this query
     * @see #getProjections()
     */
    public boolean isAllProjection() {
        return projections == null;
    }

    /**
     * Gets a query string.
     *
     * @param pretty if true, new line characters are added between parts of
     * the query for easier readability.
     * @return a query string
     */
    public String toQueryString(boolean pretty) {
        return createQueryString(pretty,
                table.getName(),
                projections,
                whereClause);
    }

    /**
     * Gets a query string.
     *
     * @return a query string
     */
    public String toQueryString() {
        return toQueryString(false);
    }

    /**
     * Creates a query string
     *
     * @param pretty if true, adds new line between parts of the query.
     * @param tableName name of the target table.
     * @param projections the projected fields. null implies all fields are
     * selected
     * @param where where condition without the 'where' keyword
     * @return query string
     */
    public static String createQueryString(boolean pretty,
                                           String tableName,
                                           List<String> projections,
                                           String where) {
        String s = SELECT;
        boolean allFieldsSelected = projections == null;
        s += (allFieldsSelected) ? ALL : join(projections);
        if (pretty && !allFieldsSelected)
            s += NEWLINE;
        s += FROM + tableName;
        if (pretty)
            s += NEWLINE;
        if (where != null && !where.trim().isEmpty()) {
            s += WHERE + where.trim();
        }
        return s;
    }

    /**
     * Gets the field names selected by this query.
     *
     * @return the fields selected by this query. null if all fields are
     * selected
     * @see #isAllProjection()
     */
    public List<String> getProjections() {
        return isAllProjection() ?
                null :
                Collections.unmodifiableList(projections);
    }

    /**
     * Gets the where clause without the WHERE keyword.
     *
     * @return where clause without the WHERE keyword. null implies no where
     * condition.
     */
    public String getWhereClause() {
        return whereClause;
    }

    /**
     * gets name of this query.
     *
     * @return can be any non-null, non-empty string
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("null of empty query name"); //$NON-NLS-1$
        }
        this.name = name;
    }

    /**
     * gets target table of this query.
     *
     * @return can be any non-null table.
     */
    public Table getTable() {
        return table;
    }

    /**
     * sets selected fields
     *
     * @param fields a list of selected fields. must not be empty. null implies
     * all fields are selected
     */
    public void setProjections(List<String> fields) {
        if (fields == null) {
            projections = null;
        } else if (fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "selected fields must not be empty"); //$NON-NLS-1$
        } else {
            projections = new ArrayList<>(fields);
        }
    }

    /**
     * sets the where clause
     *
     * @param where where clause with WHERE keyword. can be null to imply no
     * condition.
     */
    public void setWhereClause(String where) {
        this.whereClause = where;
    }

    @Override
    public String toString() {
        return "query:" + toQueryString(); //$NON-NLS-1$
    }

    static String join(Iterable<String> strings) {
        return join(',', strings);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    static String join(char sep, Iterable<String> strings) {
        String s = ""; //$NON-NLS-1$
        Iterator<String> elements = strings.iterator();
        while (elements.hasNext()) {
            s += elements.next();
            if (elements.hasNext())
                s += sep;
        }
        return s;
    }
}
