/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import oracle.nosql.model.schema.DuplicateElementException;

/**
 * A map of {@link Named named elements} that use {@link Named#getName() name}
 * as the key. It implies that a null element or an element with a null or
 * empty name can not be added to this map. <br>
 * The iterator of the map is stable. The iteration order is same as the order
 * in which elements have been {@link #add(Named, boolean) added}. <br>
 * <br>
 * 
 * 
 * @author pinaki poddar.
 * 
 *
 * @param <V> the type of Named element in the map
 */
@SuppressWarnings("serial")
public class NamedMap<V extends Named> extends LinkedHashMap<String, V>
        implements
        Iterable<V> {
    /**
     * Declare an element by given name. If given name exists, the existing
     * element is returned. Otherwise, the given element is added by given
     * name. This method is used when an element is available, but not its
     * name. This is different than {@link #add(Named, boolean) adding} a named
     * element.
     * 
     * @param name a name to which the given element may be recorded.
     * @param v the element to be added
     * @return the existing element if the name has been declared otherwise the
     * give element
     */
    public V declareName(String name, V v) {
        if (super.containsKey(name)) {
            return super.get(name);
        } else {
            put(name, v);
            return v;
        }
    }

    /**
     * Gets the element at given 0-based index.
     * 
     * @param idx a 0-based index of child element
     * @return the element at given index
     */
    public V getAt(int idx) {
        Iterator<V> iterator = super.values().iterator();
        V v = null;
        for (int i = 0; i <= idx; i++) {
            if (iterator.hasNext()) {
                v = iterator.next();
            } else {
                throw new NoSuchElementException("element at index " +
                        idx +
                        " do not exist in range (0, " +
                        idx +
                        "])");
            }
        }
        return v;
    }

    /**
     * Adds an element to this map. The {@link Named#getName() name} of the
     * element itself is used as key to the element.
     * 
     * @param v a non-null element with non-null, non-empty name.
     * @param overwrite if true, the given element overwrites any element of
     * same name. Otherwise an exception is raised.
     */
    public void add(V v, boolean overwrite) {
        if (v == null) {
            throw new IllegalArgumentException("can not add null value");
        }
        if (v.getName() == null || v.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "can not add value with null/empty name");
        }
        if (containsKey(v.getName()) && !overwrite) {
            throw new DuplicateElementException("can not add element " +
                    v.getName() +
                    " an element of same name exists");
        }
        put(v.getName(), v);
    }

    /**
     * Overwrites the given value v
     * 
     * @param v - value
     */
    public void replace(V v) {
        add(v, true);
    }

    @Override
    public List<V> values() {
        List<V> values = new ArrayList<V>();
        values.addAll(super.values());
        return values;
    }

    /**
     * @return all the names in this map.
     */
    public List<String> getNames() {
        return getNames(null);
    }

    /**
     * @param name
     * @return true if name is present in map, otherwise false.
     */
    public boolean containsName(String name) {
        List<String> names = getNames();
        for (String lname : names) {
            if (lname.equalsIgnoreCase(name))
                return true;
        }
        return false;
        // return getNames().contains(name);
    }

    /**
     * @param key
     * @return true if key is present in map, otherwise false.
     */
    public boolean containsKey(String key) {
        if (super.containsKey(key))
            return true;
        Set<String> keys = keySet();
        for (String lkey : keys) {
            if (lkey.equalsIgnoreCase(key))
                return true;
        }
        return false;
    }

    /**
     * @param key
     * @return value associated with the given key.
     */
    public V get(String key) {
        V ret = super.get(key);
        if (ret != null)
            return ret;
        Set<Map.Entry<String, V>> entries = entrySet();
        for (Map.Entry<String, V> entry : entries) {
            if (entry.getKey().equalsIgnoreCase(key))
                return entry.getValue();
        }
        return null;
    }

    /**
     * Gets a keys as list of string. The order of names depends on given
     * comparator. If the comparator is null, the result set is ordered of
     * adding the keys. Otherwise it is ordered by the comparator.
     * 
     * @param comp a comparator
     * @return the keys in given order
     */
    public List<String> getNames(Comparator<String> comp) {
        Set<String> set = comp != null ?
                new TreeSet<String>(comp) :
                new LinkedHashSet<String>();
        List<String> keys = new ArrayList<String>();
        set.addAll(keySet());
        keys.addAll(set);
        return keys;
    }

    @Override
    public Iterator<V> iterator() {
        return super.values().iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (other.getClass() != this.getClass())
            return false;
        Iterator<V> i1 = this.iterator();
        Iterator<V> i2 = NamedMap.class.cast(other).iterator();
        while (i1.hasNext()) {
            V e1 = i1.next();
            if (!i2.hasNext())
                return false;
            V e2 = i2.next();
            if (!e1.equals(e2))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 31;
        Iterator<V> i1 = this.iterator();
        while (i1.hasNext()) {
            V e1 = i1.next();
            hash += e1.hashCode();
        }
        return hash;
    }
}
