/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

@SuppressWarnings("WeakerAccess")
public class OracleNoSqlBundle extends AbstractBundle {
    public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
        return BUNDLE.getMessage(key, params);
    }

    @NonNls
    @SuppressWarnings("WeakerAccess")
    public static final String PATH_TO_BUNDLE = "OracleNosql";
    private static final OracleNoSqlBundle BUNDLE = new OracleNoSqlBundle();

    public OracleNoSqlBundle() {
        super(PATH_TO_BUNDLE);
    }
}
