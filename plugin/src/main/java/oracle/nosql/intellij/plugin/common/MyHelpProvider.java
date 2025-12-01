/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyHelpProvider extends WebHelpProvider {
    public static final String HELP_PREFIX = "oracle.nosql.intellij.help";
    public static final String PROJECT_HELP_ID = "newProjectHelp";
    public static final String CLOUDSIM_CONNECTION_HELP_ID = "simconnectionHelp";
    public static final String CLOUD_CONNECTION_HELP_ID = "cloudconnectionHelp";
    public static final String ONPREM_CONNECTION_HELP_ID = "onpremconnectionHelp";
    public static final String GENERAL_HELP_ID = "generalHelp";

    @NotNull
    @Override
    public String getHelpTopicPrefix() {
        return HELP_PREFIX;
    }

    @Nullable
    @Override
    public String getHelpPageUrl(@NotNull String helpTopicId) {
        final String unprefixedTopicId = helpTopicId.replace(getHelpTopicPrefix() + ".", "");
        switch (unprefixedTopicId) {
            case PROJECT_HELP_ID:
                return "https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=create_project_intellij";
            case CLOUDSIM_CONNECTION_HELP_ID:
                return  "https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=connect_intellij_sim";
            case CLOUD_CONNECTION_HELP_ID:
                return "https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=connect_intellij_cloud";
            case ONPREM_CONNECTION_HELP_ID:
                return "https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=connect_intellij_onprem";
            default:
                return "https://docs.oracle.com/pls/topic/lookup?ctx=nosql-cloud&id=intellij_plugin";
        }
    }
}
