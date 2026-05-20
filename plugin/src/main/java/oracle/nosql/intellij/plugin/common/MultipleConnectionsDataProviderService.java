/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for getting and setting multiple config properties for connections.
 * It consists of all the connections states.
 * oracle.nosql.config.xml will be used to persist data.
 * author @kunalgup
 */

@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
@State(name = "MultipleConnectionsDataProviderService", storages = {@Storage("oracle.nosql.config.xml")})

public class MultipleConnectionsDataProviderService implements Serializable, PersistentStateComponent<MultipleConnectionsDataProviderService.State> {

    public void putValue(String conName, ConnectionDataProviderService conService) {
        connectionState.dict.put(conName, conService.getState());
    }

    public void putUidToType(String name, String type) {
        connectionState.uidToTypeMap.put(name, type);
    }

    public void putNameToUid(String name, String url) {
        connectionState.nameToUidMap.put(name, url);
    }

    public static class State implements Serializable {
        public final Map<String, String> uidToTypeMap = new HashMap<>();
        public final Map<String, ConnectionDataProviderService.State> dict = new HashMap<>();
        public final Map<String, String> nameToUidMap = new HashMap<>();
    }

    private transient Project project;
    State connectionState;


    public MultipleConnectionsDataProviderService() {
        connectionState = new State();
    }

    public MultipleConnectionsDataProviderService(Project project) {
        this();
        this.project = project;
    }

    public static MultipleConnectionsDataProviderService getInstance(@NotNull Project project) {
        MultipleConnectionsDataProviderService service =
                project.getService(MultipleConnectionsDataProviderService.class);
        service.project = project;
        return service;
    }

    public ConnectionDataProviderService.State getValue(String conName) {
        return connectionState.dict.get(conName);
    }

    public String getConType(String uid) {
        return connectionState.uidToTypeMap.get(uid);
    }

    public String getUid(String name) {
        return connectionState.nameToUidMap.get(name);
    }

    public Map<String, String> getNameToUidMap() {
        return connectionState.nameToUidMap;
    }

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from default (i.e. the value of newly instantiated class) are serialized. {@code null} value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see XmlSerializer
     */
    @Nullable
    @Override
    public State getState() {
        migrateNestedSecrets(connectionState);
        return connectionState;
    }

    /**
     * This method is called when new component state is loaded. The method can and will be called several times, if
     * config files were externally changed while IDEA running.
     *
     * @param state loaded component state
     * @see XmlSerializerUtil#copyBean(Object, Object)
     */
    @Override
    public void loadState(@NotNull State state) {
        connectionState = state == null ? new State() : state;
        migrateNestedSecrets(connectionState);
    }

    private void migrateNestedSecrets(State state) {
        if (state == null) {
            return;
        }
        /*
         * Multiple-connections state embeds individual ConnectionDataProvider
         * states, so each nested connection must receive the same plaintext to
         * PasswordSafe migration as the currently selected connection.
         */
        for (ConnectionDataProviderService.State connection :
                state.dict.values()) {
            ConnectionDataProviderService.migrateSensitiveValues(project,
                    connection);
        }
        rewriteCloudSimUids(state);
    }

    private void rewriteCloudSimUids(State state) {
        List<String> oldUids = new ArrayList<>(state.dict.keySet());
        for (String oldUid : oldUids) {
            ConnectionDataProviderService.State connection =
                    state.dict.get(oldUid);
            if (connection == null || !"Cloudsim".equals(
                    connection.dict.get(ConnectionDataProviderService.KEY_PROFILE_TYPE))) {
                continue;
            }
            String target = connection.dict.get(
                    "/Cloudsim/Cloudsim/service-url");
            if (target == null || target.trim().isEmpty()) {
                continue;
            }
            String tenantIdentifier =
                    ConnectionDataProviderService
                            .getNonSecretIdentifierForStoredValue(
                                    connection.dict.get("/Cloudsim/TENANT_ID"));
            if (tenantIdentifier.isEmpty()) {
                tenantIdentifier = "tenant-not-set";
            }
            /*
             * Older CloudSim UIDs included the tenant token itself. Rewrite them
             * to use a non-secret identifier derived from the PasswordSafe
             * reference while preserving name->UID and type maps.
             */
            String newUid = uniqueUid(state,
                    target + " : " + tenantIdentifier, oldUid);
            if (oldUid.equals(newUid)) {
                continue;
            }
            state.dict.remove(oldUid);
            state.dict.put(newUid, connection);
            String type = state.uidToTypeMap.remove(oldUid);
            state.uidToTypeMap.put(newUid,
                    type == null ? "Cloudsim" : type);
            for (Map.Entry<String, String> entry :
                    state.nameToUidMap.entrySet()) {
                if (oldUid.equals(entry.getValue())) {
                    entry.setValue(newUid);
                }
            }
        }
    }

    private static String uniqueUid(State state, String desiredUid,
                                    String existingUid) {
        if (!state.dict.containsKey(desiredUid) ||
                desiredUid.equals(existingUid)) {
            return desiredUid;
        }
        int suffix = 2;
        String candidate;
        do {
            candidate = desiredUid + " (" + suffix++ + ")";
        } while (state.dict.containsKey(candidate) &&
                !candidate.equals(existingUid));
        return candidate;
    }
}