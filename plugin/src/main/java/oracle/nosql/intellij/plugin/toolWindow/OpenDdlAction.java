/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService;
import oracle.nosql.intellij.plugin.toolWindow.executeDDL.executeDdlGUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.Objects;

/**
 * Invokes the System DDL execution class
 */
public class OpenDdlAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }
        String prefKeyForProfileType = "/profile_type";
        String profileType = ConnectionDataProviderService
                .getInstance(Objects.requireNonNull(e.getProject())).
                getValue(prefKeyForProfileType);
        if (profileType != null && profileType.equals("Onprem")) {
            SwingUtilities.invokeLater(() -> new executeDdlGUI(e.getProject()));
        }
    }
    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        boolean condition = project != null && checkYourCondition(project);
        e.getPresentation().setEnabledAndVisible(condition);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // Choose either EDT or BGT here
        return ActionUpdateThread.BGT;
    }

    private boolean checkYourCondition(Project project) {
        if (project == null) return false;
        MultipleConnectionsDataProviderService mConService = MultipleConnectionsDataProviderService.getInstance(project);
        ConnectionDataProviderService conService = ConnectionDataProviderService.getInstance(project);
        ConnectionDataProviderService.State conState = conService.getState();
        String uid="";
        assert mConService.getState() != null;
        for(Map.Entry<String,ConnectionDataProviderService.State> e : mConService.getState().dict.entrySet()){
            if(e.getValue().dict.equals(conState.dict)){
                uid = e.getKey();
                break;
            }
        }
        if(uid.equals(""))
            return false;
        Map<String,String> uidToTypeMap = mConService.getState().uidToTypeMap;
        String type = uidToTypeMap.get(uid);
        return "Onprem".equals(type);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

}
