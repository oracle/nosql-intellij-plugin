/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes <em>distance</em> between strings.
 * 
 *
 */
public class Levensthien {
    /**
     * Finds the string among the given candidates that is closest to the given
     * key string.
     * 
     * @param key a string to be matched
     * @param candidates a set of strings to match
     * @return a string closest to the given key string.
     */
    public static String findClosest(String key, List<String> candidates) {
        return findClosest(key, candidates, false);
    }

    /**
     * Finds the string among the given candidates that is closest to the given
     * key string.
     * 
     * @param key a string to be matched
     * @param candidates a set of strings to match
     * @param caseInsensitive if true, the matching ignores character case.
     * @return a string closest to the given key string.
     */
    public static String findClosest(String key,
            List<String> candidates,
            boolean caseInsensitive) {
        int[] editCosts = new int[] { 1, 1, 1 };
        DistanceMetric<Character> metric = new CharDistance(caseInsensitive);
        List<List<Character>> lists = new ArrayList<List<Character>>();
        for (String c : candidates) {
            lists.add(toCharList(c));
        }
        List<Character> closest = new SequenceDistance<Character>()
                .closest(toCharList(key), lists, editCosts, metric);
        return fromCharList(closest);
    }

    private static List<Character> toCharList(String s) {
        List<Character> list = new ArrayList<Character>();
        for (int i = 0; i < s.length(); i++) {
            list.add(s.charAt(i));
        }
        return list;
    }

    private static String fromCharList(List<Character> list) {
        char[] chars = new char[list.size()];
        for (int i = 0; i < list.size(); i++) {
            chars[i] = list.get(i);
        }
        return new String(chars);
    }
}
