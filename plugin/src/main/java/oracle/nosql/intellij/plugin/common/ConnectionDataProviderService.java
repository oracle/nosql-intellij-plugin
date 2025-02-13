/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
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
import oracle.nosql.model.connection.ConfigurableProperty;
import oracle.nosql.model.connection.IConnectionProfileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class for getting and setting config properties like
 * connection type, sdk path etc.
 * oracle.nosql.config.xml will be used to persist data.
 *
 * @author amsundar
 */

@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
@State(name = "ConnectionDataProviderService",
        storages = {
                @Storage("oracle.nosql.config.xml")
        })

public class ConnectionDataProviderService implements Serializable,PersistentStateComponent<ConnectionDataProviderService.State> {
    public static final String KEY_PROFILE_TYPE = "/profile_type";
    public static final String KEY_SHOW_TABLE_PAGE_SIZE = "/show_table/page_size";


    public static String getKeyForProperty(IConnectionProfileType profileType, ConfigurableProperty property) {
        return "/" + profileType.getName() + "/" + property.getName();
    }

    public void putValue(String prefKey, String defaultValue) {
        connectionState.dict.put(prefKey,defaultValue);
    }

    public static class State implements Serializable {
        public final HashMap<String,String> dict = new HashMap<>();
    }
    State connectionState;


    public ConnectionDataProviderService() {
        connectionState = new State();
    }

    public static ConnectionDataProviderService getInstance(@NotNull Project project) {
        return project.getService(ConnectionDataProviderService.class);
    }
    public String getValue(String key) {
        return connectionState.dict.get(key);
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