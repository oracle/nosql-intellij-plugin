/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.util.List;

/**
 * Computes distance between two sequences of generic type. Distance between
 * two sequences <code>s1</code> and <code>s2</code> is defined as
 * <em>minimum</em> cost of edit operations applied on <code>s1</code> to make
 * it same as <code>s2</code>.
 * <p>
 * The edit operations are insert, replace or delete an element. The cost of
 * the edit operation are supplied. Also distance between sequence elements
 * must be supplied.
 * <p>
 * 
 * @param <T> element type of matching sequences
 * 
 * @author pinaki poddar
 *
 */
public class SequenceDistance<T> {
    /**
     * finds one sequence among the given candidates that is the closest to the
     * given sequence.
     * 
     * @param key a sequence to match
     * @param candidates a set of candidate against which the key sequence is
     * matched
     * @param editCosts costs to edit operations. An array of three integers
     * for inserting an element, substituting an element and deleting an
     * element from a sequence.
     * @param metric a function to compute distance between individual element
     * 
     * @return sequence that is <em>closest</em> to given key sequence
     */
    public List<T> closest(List<T> key,
            Iterable<List<T>> candidates,
            int[] editCosts,
            DistanceMetric<T> metric) {
        List<T> closest = null;
        int min = Integer.MAX_VALUE;
        for (List<T> candidate : candidates) {
            int d = distance(key, candidate, editCosts, metric);
            if (d < min) {
                min = d;
                closest = candidate;
            }
        }
        return closest;
    }

    /**
     * calculates distance between two given arrays. <br>
     * There are multiple possible ways a sequence can be transformed to
     * another by insert/replace/delete edit operations. This algorithm finds
     * the particular set of operations whose cost is minimum. The
     * computational complexity in NxM where N and M are length of two arrays
     * 
     * @param s1 an array
     * @param s2 another array
     * @param editCosts cost of editing operations
     * @param metric a function to compute distance between individual element
     * @return distance between the arrays
     */
    public int distance(List<T> s1,
            List<T> s2,
            int[] editCosts,
            DistanceMetric<T> metric) {
        int[][] cost_matrix = new int[s1.size()][s2.size()];
        for (int i = 0; i < s1.size(); i++) {
            cost_matrix[i] = new int[s2.size()];
        }
        for (int i = 0; i < s1.size(); i++) {
            cost_matrix[i][0] = metric.distance(s1.get(i), s2.get(0));
        }
        for (int j = 0; j < s2.size(); j++) {
            cost_matrix[0][j] = metric.distance(s1.get(0), s2.get(j));
        }
        for (int i = 1; i < s1.size(); i++) {
            for (int j = 1; j < s2.size(); j++) {
                int c1 = cost_matrix[i - 1][j] +
                        editCosts[0] *
                                metric.distance(s1.get(i - 1), s2.get(j));
                int c2 = cost_matrix[i - 1][j - 1] +
                        editCosts[1] *
                                metric.distance(s1.get(i - 1), s2.get(j - 1));
                int c3 = cost_matrix[i][j - 1] +
                        editCosts[2] *
                                metric.distance(s1.get(i), s2.get(j - 1));
                int min_cost = Math.min(Math.min(c1, c2), c3);
                cost_matrix[i][j] = min_cost;
            }
        }
        return cost_matrix[s1.size() - 1][s2.size() - 1];
    }
}
