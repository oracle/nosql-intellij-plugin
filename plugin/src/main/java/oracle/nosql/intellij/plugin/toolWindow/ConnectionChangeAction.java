/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService.State;
import oracle.nosql.intellij.plugin.toolWindow.connectionChange.connectionChangeGUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

public class ConnectionChangeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<List<String>> connectionsList = new ArrayList<>();
        for(int i=0;i<3;i++)
            connectionsList.add(new ArrayList<>());
        State state = MultipleConnectionsDataProviderService.getInstance(Objects.requireNonNull(e.getProject())).getState();
        assert state != null;
        Map<String,String[]> nameAndUrlToPair = new HashMap<>();
        for(Map.Entry<String,String> e1:state.nameToUidMap.entrySet()){
            String name = e1.getKey();
            String uid = e1.getValue();
            String type = state.uidToTypeMap.get(uid);
            String nameAndUrl = name+" : "+uid;
            if(type!=null && type.equals("Cloud"))
                connectionsList.get(0).add(nameAndUrl);
            else if(type!=null && type.equals("Onprem"))
                connectionsList.get(1).add(nameAndUrl);
            else
                connectionsList.get(2).add(nameAndUrl);
            nameAndUrlToPair.put(nameAndUrl,new String[]{name,uid});
        }
        List<String> connections =new ArrayList<>();
        for(List<String> list : connectionsList){
            connections.addAll(list);
        }
        String[] connectionsArray = new String[connections.size()];
        for(int i = 0; i< connections.size(); i++)
            connectionsArray[i]= connections.get(i);

        SwingUtilities.invokeLater(() -> new connectionChangeGUI(connectionsArray,nameAndUrlToPair,state,e.getProject()));

    }
    @Override
    public boolean isDumbAware() {
        return true;
    }
}