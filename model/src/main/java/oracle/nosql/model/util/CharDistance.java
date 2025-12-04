/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

public class CharDistance implements DistanceMetric<Character> {
    private final boolean caseInsensitive;

    public CharDistance() {
        this(false);
    }

    public CharDistance(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public int distance(Character t1, Character t2) {
        char c1 = caseInsensitive ? Character.toLowerCase(t1) : t1;
        char c2 = caseInsensitive ? Character.toLowerCase(t2) : t2;
        if (c1 == c2)
            return 0;
        return 1;
    }
}
