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
import java.util.HashMap;
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

    State connectionState;


    public MultipleConnectionsDataProviderService() {
        connectionState = new State();
    }

    public static MultipleConnectionsDataProviderService getInstance(@NotNull Project project) {
        return project.getService(MultipleConnectionsDataProviderService.class);
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
        connectionState = state;
    }
}