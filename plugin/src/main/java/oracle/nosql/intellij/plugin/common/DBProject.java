/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import oracle.nosql.model.connection.ConfigurableProperty;
import oracle.nosql.model.connection.ConnectionFactory;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.connection.IConnectionProfile;
import oracle.nosql.model.connection.IConnectionProfileType;

import java.io.File;

public class DBProject {

    private Project project;
    @SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
    private static File libDir;
    private static final String ID = OracleNoSqlBundle.message("oracle.nosql.plugin.Id");
    static {
        libDir = new File(String.valueOf(GetPluginDesc().getPluginPath()),"lib/");
        ConnectionFactory.setLibDir(libDir);
    }

    private static IdeaPluginDescriptor GetPluginDesc() {
        return PluginManagerCore.getPlugin(PluginId.getId(ID));
    }


    private DBProject(Project project) {
        this.project = project;
    }

    public  static DBProject getInstance(Project project) {
        return project.getService(DBProject.class);
    }

    public IConnection getConnection() throws Exception {
        return getConnectionProfile().getConnection();
    }

    private String getSDKPath() {
        String profileName = ConnectionDataProviderService.getInstance(project).getValue(ConnectionDataProviderService.KEY_PROFILE_TYPE);

        if (profileName == null) {
            return null;
        }
        String PrefKeyForSDK =
                "/" + profileName + "/" + "SDK_PATH"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return ConnectionDataProviderService.getInstance(project).getValue(PrefKeyForSDK);
    }

    public IConnectionProfile<?>  getConnectionProfile() throws Exception {
        IConnectionProfileType profileType =
                ConnectionFactory.getProfileTypes()[0]; // default
        String selectedProfileTypeAsString = ConnectionDataProviderService.getInstance(project).getValue(ConnectionDataProviderService.KEY_PROFILE_TYPE);
        for (IConnectionProfileType pType : ConnectionFactory
                .getProfileTypes()) {
            String pTypeValue = pType.getName();
            if (selectedProfileTypeAsString!=null && selectedProfileTypeAsString.equals(pTypeValue)) {
                profileType = pType;
                break;
            }
        }
        String SDK_Path = getSDKPath();
        IConnectionProfile<?> profile;
        profile = ConnectionFactory.createProfile(profileType,
                "default", //$NON-NLS-1$
                SDK_Path,
                getClass().getClassLoader());
        for (ConfigurableProperty property : profileType
                .getRequiredProperties()) {
            String perfPropertyValue = ConnectionDataProviderService.getInstance(project).getValue(
                    ConnectionDataProviderService.getKeyForProperty(profileType,property));
            if(perfPropertyValue == null ) {
                perfPropertyValue = property.getDefaultValue();
            }
            profile.setProperty(property.getName(), perfPropertyValue);
        }
        for (ConfigurableProperty property : profileType
                .getOptionalProperties()) {
            String perfPropertyValue = ConnectionDataProviderService.getInstance(project).getValue(
                    ConnectionDataProviderService.getKeyForProperty(profileType,property));
            if(perfPropertyValue == null ) {
                perfPropertyValue = property.getDefaultValue();
            }
            profile.setProperty(property.getName(), perfPropertyValue);
        }
        return profile;
    }
}
