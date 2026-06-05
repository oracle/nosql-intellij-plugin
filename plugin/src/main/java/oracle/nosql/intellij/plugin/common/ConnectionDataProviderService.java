/*
* Copyright (C) 2019, 2026 Oracle and/or its affiliates.
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
import java.util.Map;

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

    public synchronized void putValue(String prefKey, String defaultValue) {
        if (ConnectionSecretStore.isSensitiveKey(prefKey)) {
            /*
             * Sensitive connection fields must not be serialized into the
             * project-level oracle.nosql.config.xml file. Store the real value
             * in PasswordSafe and keep only an opaque reference in this state.
             */
            String storedValue = connectionState.dict.get(prefKey);
            connectionState.dict.put(prefKey,
                    ConnectionSecretStore.store(project, prefKey,
                            defaultValue, storedValue));
            return;
        }
        connectionState.dict.put(prefKey,defaultValue);
    }

    public static class State implements Serializable {
        public final HashMap<String,String> dict = new HashMap<>();
    }
    private transient Project project;
    State connectionState;


    public ConnectionDataProviderService() {
        connectionState = new State();
    }

    public ConnectionDataProviderService(Project project) {
        this();
        this.project = project;
    }

    public static ConnectionDataProviderService getInstance(@NotNull Project project) {
        ConnectionDataProviderService service =
                project.getService(ConnectionDataProviderService.class);
        service.project = project;
        return service;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public synchronized String getValue(String key) {
        String storedValue = connectionState.dict.get(key);
        if (ConnectionSecretStore.isSensitiveKey(key)) {
            /*
             * Callers continue to use getValue normally; the service resolves
             * secret references only in memory right before building UI/profile
             * objects. The persisted state remains non-secret.
             */
            return ConnectionSecretStore.resolve(project, key, storedValue);
        }
        return storedValue;
    }

    public synchronized String getNonSecretIdentifierForKey(String key) {
        return ConnectionSecretStore.publicIdentifier(connectionState.dict.get(key));
    }

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from default (i.e. the value of newly instantiated class) are serialized. {@code null} value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see XmlSerializer
     */
    @Nullable
    @Override
    public synchronized State getState() {
        /* Ensure legacy plaintext secrets are converted before IntelliJ serializes state. */
        migrateSensitiveValues(project, connectionState);
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
    public synchronized void loadState(@NotNull State state) {
        connectionState = state == null ? new State() : state;
        /* Convert any plaintext values already present in old project XML. */
        migrateSensitiveValues(project, connectionState);
    }

    static void migrateSensitiveValues(Project project, State state) {
        if (state == null) {
            return;
        }
        for (Map.Entry<String, String> entry : state.dict.entrySet()) {
            entry.setValue(ConnectionSecretStore.migrateIfPlainText(
                    project, entry.getKey(), entry.getValue()));
        }
    }

    static String getNonSecretIdentifierForStoredValue(String storedValue) {
        return ConnectionSecretStore.publicIdentifier(storedValue);
    }
}