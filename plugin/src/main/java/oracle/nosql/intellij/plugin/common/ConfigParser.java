/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.project.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ConfigParser {
    public ConfigParser(Project project){
        try {
            String basePath = project.getBasePath();
            File inputFile = new File(basePath + "/.idea/oracle.nosql.config.xml");
            if (!inputFile.exists()) {
                return;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            ConnectionDataProviderService cloudCon = new ConnectionDataProviderService();
            ConnectionDataProviderService onPremCon = new ConnectionDataProviderService();
            ConnectionDataProviderService cloudSimCon = new ConnectionDataProviderService();

            NodeList nList1 = doc.getElementsByTagName("component");
            if (nList1.getLength() > 1) return;
            else {
                NodeList nList = doc.getElementsByTagName("entry");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String key = element.getAttribute("key");
                        cloudCon.putValue(key, "");
                        onPremCon.putValue(key, "");
                        cloudSimCon.putValue(key, "");
                    }
                }
                String key1 = "/profile_type";
                cloudCon.putValue(key1, "Cloud");
                onPremCon.putValue(key1, "Onprem");
                cloudSimCon.putValue(key1, "Cloudsim");

                String cloudConName = new String();
                String onPremName = new String();
                String cloudSimName = new String();
                String cloudSimTenantId = new String();

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String key = element.getAttribute("key");
                        String value = element.getAttribute("value");

                        if (value.contains("$USER_HOME$")) {
                            value = value.replace("$USER_HOME$", System.getProperty("user.home"));
                        }

                        // Classify the entries into respective maps
                        if (key.startsWith("/Cloud/")) {
                            cloudCon.putValue(key, value);
                        } else if (key.startsWith("/Onprem/")) {
                            onPremCon.putValue(key, value);
                        } else if (key.startsWith("/Cloudsim/")) {
                            cloudSimCon.putValue(key, value);
                        }

                        if (key.equals("/Cloud/Cloud/endpoint")) {
                            cloudCon.putValue("/Cloud/Cloud/connection-name", value);
                            cloudConName = value;
                        }

                        if (key.equals("/Cloudsim/Cloudsim/service-url")) {
                            cloudSimCon.putValue("/Cloudsim/Cloudsim/connection-name", value);
                            cloudSimName = value;
                        }

                        if (key.equals("/Onprem/Onprem/proxy-url")) {
                            onPremCon.putValue("/Onprem/Onprem/connection-name", value);
                            onPremName = value;
                        }

                        if(key.equals("/Cloudsim/TENANT_ID")){
                            cloudSimTenantId = value;
                        }
                    }
                }

                MultipleConnectionsDataProviderService multipleConnectionsDataProviderService = MultipleConnectionsDataProviderService.getInstance(project);
                MultipleConnectionsDataProviderService.State state = multipleConnectionsDataProviderService.getState();
                if (!cloudConName.isEmpty()) {
                    state.nameToUidMap.put(cloudConName, cloudConName);
                    state.uidToTypeMap.put(cloudConName, "Cloud");
                    state.dict.put(cloudConName, cloudCon.getState());
                }
                if (!onPremName.isEmpty()) {
                    state.nameToUidMap.put(onPremName, onPremName);
                    state.uidToTypeMap.put(onPremName, "Onprem");
                    state.dict.put(onPremName, onPremCon.getState());
                }
                if (!cloudSimName.isEmpty()) {
                    if(cloudSimTenantId.isEmpty())
                        cloudSimTenantId = "exampleId";
                    String cloudSimUid = cloudSimName + " : " + cloudSimTenantId;
                    state.nameToUidMap.put(cloudSimUid, cloudSimUid);
                    state.uidToTypeMap.put(cloudSimUid, "Cloudsim");
                    state.dict.put(cloudSimUid, cloudSimCon.getState());
                }

                if (!cloudConName.isEmpty()) {
                    ConnectionDataProviderService.getInstance(project).loadState(cloudCon.getState());
                } else if (!onPremName.isEmpty()) {
                    ConnectionDataProviderService.getInstance(project).loadState(onPremCon.getState());
                } else if (!cloudSimName.isEmpty()) {
                    ConnectionDataProviderService.getInstance(project).loadState(cloudSimCon.getState());
                }
            }
            DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
