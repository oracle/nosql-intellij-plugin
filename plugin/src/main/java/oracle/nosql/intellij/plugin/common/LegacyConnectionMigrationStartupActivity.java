/*
 * Copyright (C) 2026, 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Migrates pre-1.5.0 single-connection project state to the multiple-connection
 * state model without parsing project XML directly or opening a network
 * connection.
 */
public class LegacyConnectionMigrationStartupActivity
        implements StartupActivity.DumbAware {
    private static final Logger LOG =
            Logger.getInstance(LegacyConnectionMigrationStartupActivity.class);

    private static final String CLOUD = "Cloud";
    private static final String ONPREM = "Onprem";
    private static final String CLOUDSIM = "Cloudsim";

    @Override
    public void runActivity(@NotNull Project project) {
        try {
            migrate(project);
        } catch (RuntimeException e) {
            LOG.warn("Unable to migrate legacy Oracle NoSQL connections", e);
        }
    }

    private static void migrate(Project project) {
        /*
         * Migration uses IntelliJ's already-deserialized PersistentStateComponent
         * data. It deliberately does not open .idea/oracle.nosql.config.xml with
         * a custom XML parser, which removes the previous XXE attack surface.
         */
        ConnectionDataProviderService connectionService =
                ConnectionDataProviderService.getInstance(project);
        ConnectionDataProviderService.State legacyState =
                connectionService.getState();
        if (legacyState == null || legacyState.dict.isEmpty()) {
            return;
        }

        MultipleConnectionsDataProviderService multipleService =
                MultipleConnectionsDataProviderService.getInstance(project);
        MultipleConnectionsDataProviderService.State multipleState =
                multipleService.getState();
        if (multipleState == null || !multipleState.dict.isEmpty()) {
            /*
             * If the multiple-connection store already has entries, the project
             * is either new enough or has already been migrated. Never merge or
             * overwrite here; doing so could duplicate connections or replace a
             * user-selected current connection.
             */
            return;
        }

        ConnectionDataProviderService.State firstMigratedState = null;
        /*
         * Preserve the 1.4.x behavior where a project could have one Cloud, one
         * Onprem, and one Cloudsim connection in the single legacy state map.
         * Each non-empty profile is copied into its own 1.5.x multiple-connection
         * entry. No schema refresh or network connection is made during this.
         */
        firstMigratedState = firstNonNull(firstMigratedState,
                migrateCloud(project, legacyState, multipleState));
        firstMigratedState = firstNonNull(firstMigratedState,
                migrateOnprem(project, legacyState, multipleState));
        firstMigratedState = firstNonNull(firstMigratedState,
                migrateCloudsim(project, legacyState, multipleState));

        if (firstMigratedState != null &&
                isBlank(legacyState.dict.get(
                        ConnectionDataProviderService.KEY_PROFILE_TYPE))) {
            connectionService.loadState(firstMigratedState);
        }
    }

    private static ConnectionDataProviderService.State migrateCloud(
            Project project,
            ConnectionDataProviderService.State legacyState,
            MultipleConnectionsDataProviderService.State multipleState) {
        String target = trimToNull(legacyState.dict.get("/Cloud/Cloud/endpoint"));
        if (target == null && isTrue(legacyState.dict.get("/Cloud/USE_CONFIG_FILE"))) {
            target = buildCloudConfigUid(legacyState);
        }
        if (target == null) {
            return null;
        }

        ConnectionDataProviderService.State state =
                copyProfileState(legacyState, "/Cloud/");
        state.dict.put(ConnectionDataProviderService.KEY_PROFILE_TYPE, CLOUD);
        String name = firstNonBlank(
                state.dict.get("/Cloud/Cloud/connection-name"), target);
        state.dict.put("/Cloud/Cloud/connection-name", name);
        /* Move legacy Cloud passphrases out of project XML during migration. */
        ConnectionDataProviderService.migrateSensitiveValues(project, state);

        String uid = target;
        String compartment = trimToNull(state.dict.get("/Cloud/COMPARTMENT"));
        if (compartment != null && !uid.contains(" : " + compartment)) {
            uid += " : " + compartment;
        }

        return addMigratedConnection(multipleState, name, uid, CLOUD, state) ?
                state : null;
    }

    private static ConnectionDataProviderService.State migrateOnprem(
            Project project,
            ConnectionDataProviderService.State legacyState,
            MultipleConnectionsDataProviderService.State multipleState) {
        String target = trimToNull(
                legacyState.dict.get("/Onprem/Onprem/proxy-url"));
        if (target == null) {
            return null;
        }

        ConnectionDataProviderService.State state =
                copyProfileState(legacyState, "/Onprem/");
        state.dict.put(ConnectionDataProviderService.KEY_PROFILE_TYPE, ONPREM);
        String name = firstNonBlank(
                state.dict.get("/Onprem/Onprem/connection-name"), target);
        state.dict.put("/Onprem/Onprem/connection-name", name);
        /* Move legacy Onprem passwords/passphrases out of project XML. */
        ConnectionDataProviderService.migrateSensitiveValues(project, state);

        String uid = target;
        String namespace = trimToNull(state.dict.get("/Onprem/NAMESPACE"));
        if (namespace != null) {
            uid += " : " + namespace;
        }

        return addMigratedConnection(multipleState, name, uid, ONPREM, state) ?
                state : null;
    }

    private static ConnectionDataProviderService.State migrateCloudsim(
            Project project,
            ConnectionDataProviderService.State legacyState,
            MultipleConnectionsDataProviderService.State multipleState) {
        String target = trimToNull(
                legacyState.dict.get("/Cloudsim/Cloudsim/service-url"));
        if (target == null) {
            return null;
        }

        ConnectionDataProviderService.State state =
                copyProfileState(legacyState, "/Cloudsim/");
        state.dict.put(ConnectionDataProviderService.KEY_PROFILE_TYPE, CLOUDSIM);
        String tenantId = trimToNull(state.dict.get("/Cloudsim/TENANT_ID"));
        if (tenantId != null) {
            state.dict.put("/Cloudsim/TENANT_ID", tenantId);
        }
        /* Store CloudSim bearer-token material in PasswordSafe before adding it. */
        ConnectionDataProviderService.migrateSensitiveValues(project, state);

        /* Keep the migrated UID stable enough for users but free of token text. */
        String tenantIdentifier = ConnectionDataProviderService
                .getNonSecretIdentifierForStoredValue(
                        state.dict.get("/Cloudsim/TENANT_ID"));
        if (tenantIdentifier.isEmpty()) {
            tenantIdentifier = "tenant-not-set";
        }
        String uid = target + " : " + tenantIdentifier;
        String name = firstNonBlank(
                state.dict.get("/Cloudsim/Cloudsim/connection-name"), uid);
        state.dict.put("/Cloudsim/Cloudsim/connection-name", name);

        return addMigratedConnection(multipleState, name, uid, CLOUDSIM, state) ?
                state : null;
    }

    private static ConnectionDataProviderService.State copyProfileState(
            ConnectionDataProviderService.State legacyState,
            String profilePrefix) {
        ConnectionDataProviderService.State state =
                new ConnectionDataProviderService.State();
        for (Map.Entry<String, String> entry : legacyState.dict.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(profilePrefix) ||
                    ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE.equals(key)) {
                state.dict.put(key, entry.getValue());
            }
        }
        return state;
    }

    private static boolean addMigratedConnection(
            MultipleConnectionsDataProviderService.State multipleState,
            String name,
            String uid,
            String profileType,
            ConnectionDataProviderService.State connectionState) {
        /*
         * The multiple-connection UI keys entries by both display name and UID.
         * Skip conflicting legacy entries instead of overwriting user data.
         */
        if (isBlank(name) || isBlank(uid) || multipleState.dict.containsKey(uid) ||
                multipleState.nameToUidMap.containsKey(name)) {
            return false;
        }
        multipleState.nameToUidMap.put(name, uid);
        multipleState.uidToTypeMap.put(uid, profileType);
        multipleState.dict.put(uid, connectionState);
        return true;
    }

    private static String buildCloudConfigUid(
            ConnectionDataProviderService.State state) {
        String configFile = trimToNull(state.dict.get("/Cloud/CONFIG_FILE"));
        if (configFile == null) {
            return null;
        }
        String profile = firstNonBlank(state.dict.get("/Cloud/CONFIG_PROFILE"),
                "DEFAULT");
        return configFile + " : " + profile;
    }

    private static ConnectionDataProviderService.State firstNonNull(
            ConnectionDataProviderService.State first,
            ConnectionDataProviderService.State second) {
        return first != null ? first : second;
    }

    private static String firstNonBlank(String first, String second) {
        String value = trimToNull(first);
        return value == null ? second : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return trimToNull(value) == null;
    }

    private static boolean isTrue(String value) {
        return "true".equalsIgnoreCase(trimToNull(value));
    }
}