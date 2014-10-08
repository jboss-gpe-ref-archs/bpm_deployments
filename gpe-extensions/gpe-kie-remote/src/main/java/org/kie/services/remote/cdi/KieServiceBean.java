/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.remote.cdi;

import java.security.Policy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.kie.services.remote.rest.ResourceBase;
import org.kie.services.remote.rest.graph.jaxb.ActiveNodeInfo;
import org.kie.services.remote.rest.graph.jaxb.DiagramInfo;
import org.kie.services.remote.rest.graph.jaxb.DiagramNodeInfo;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.IGPEKieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Alternative
@Default
public class KieServiceBean extends ResourceBase implements IGPEKieService {
    
    private static final Logger logger = LoggerFactory.getLogger(KieServiceBean.class);
    private static Logger log = LoggerFactory.getLogger("KieServiceBean");
    
    @PersistenceUnit
    private EntityManagerFactory emf;

    @Inject
    private DeploymentInfoBean dInfoBean;

    /* KIE information and processing */
    @Inject
    private ProcessRequestBean processRequestBean;
    
    @PostConstruct
    public void start() {
        StringBuilder sBuilder = new StringBuilder("start() registered deploymentIds = ");
        logger.info("start");
        Collection<String> dCollection = dInfoBean.getDeploymentIds();
        for(String dId : dCollection){
            sBuilder.append("\n\t");
            sBuilder.append(dId);
        }
    }
    
    public Map<String, Object> startProcessAndReturnInflightVars(String deploymentId, String processId, Map<String, Object> params) {

        WorkflowProcessInstanceImpl pInstance = (WorkflowProcessInstanceImpl)processRequestBean.doKieSessionOperation(
                new StartProcessCommand(processId, params), 
                deploymentId, 
                null,
                "Unable to start process with process definition id "+processId);
        Map<String, Object> variables = pInstance.getVariables();

        // need to create new Map because the variables map available from WorkflowProcessInstanceImpl is immutable
        Map<String, Object> returnMap = new HashMap<String, Object>();
        for (String key : variables.keySet()) {
            logger.info("key = "+key+" : value = "+variables.get(key));
            returnMap.put(key, variables.get(key));
        }
        returnMap.put(IGPEKieService.PROCESS_INSTANCE, pInstance);
        return returnMap;
    }
    
    public List<String> listProcesses(String deploymentId){
        Command<?> cmd = new GetProcessIdsCommand();
        List<String> pList = (List<String>) processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                null,
                "Unable to list processes for deploymentId = "+deploymentId
                );
        return pList;
    }
    
    
    public List<ActiveNodeInfo> getActiveNodeInfo(String deploymentId, String instanceId) {
        AuditLogService auditLogService = new JPAAuditLogService(emf);
        ProcessInstanceLog processInstance = auditLogService.findProcessInstance(new Long(instanceId));
        if (processInstance == null) {
            throw new IllegalArgumentException("Could not find process instance " + instanceId);
        }
        Map<String, NodeInstanceLog> nodeInstances = new HashMap<String, NodeInstanceLog>();
        for (NodeInstanceLog nodeInstance: auditLogService.findNodeInstances(new Long(instanceId))) {
        if (nodeInstance.getType() == NodeInstanceLog.TYPE_ENTER) {
            nodeInstances.put(nodeInstance.getNodeInstanceId(), nodeInstance);
            } else {
            nodeInstances.remove(nodeInstance.getNodeInstanceId());
            }
        }
        if (!nodeInstances.isEmpty()) {
            List<ActiveNodeInfo> result = new ArrayList<ActiveNodeInfo>();
            for (NodeInstanceLog nodeInstance: nodeInstances.values()) {
            boolean found = false;
            DiagramInfo diagramInfo = getDiagramInfo(deploymentId, processInstance.getProcessId());
            if (diagramInfo != null) {
                for (DiagramNodeInfo nodeInfo: diagramInfo.getNodeList()) {
                    if (nodeInfo.getName().equals(nodeInstance.getNodeId())) {
                        result.add(new ActiveNodeInfo(diagramInfo.getWidth(), diagramInfo.getHeight(), nodeInfo));
                        found = true;
                        break;
                    }
                }
            } else {
                throw new IllegalArgumentException("Could not find info for diagram for process " + processInstance.getProcessId());
            }
            if (!found) {
                throw new IllegalArgumentException("Could not find info for node " + nodeInstance.getNodeId() + " of process " + processInstance.getProcessId());
            }
            }
            return result;
        }
        return null;
    }

    private DiagramInfo getDiagramInfo(String deploymentId, String processId) {
        WorkflowProcess processObj = this.getProcess(deploymentId,processId);
        
        DiagramInfo result = new DiagramInfo();
        result.setWidth(932);
        result.setHeight(541);
        List<DiagramNodeInfo> nodeList = new ArrayList<DiagramNodeInfo>();
        addNodesInfo(nodeList, processObj.getNodes(), "id=");
        result.setNodeList(nodeList);
        return result;
    }
    
    private WorkflowProcess getProcess(String deploymentId, String processId) {
        RuntimeEngine rEngine = dInfoBean.getRuntimeEngine(deploymentId, null);
        KieBase kBase = rEngine.getKieSession().getKieBase();
        WorkflowProcess pObj = (WorkflowProcess)kBase.getProcess(processId);
        log.info("getProcess() "+processId+" : # of nodes = "+pObj.getNodes().length);
        return pObj;
    }
  
    private void addNodesInfo(List<DiagramNodeInfo> nodeInfos, Node[] nodes, String prefix) {
        for (Node node : nodes) {
            String uniqueId = org.jbpm.bpmn2.xml.XmlBPMNProcessDumper.getUniqueNodeId(node);
            nodeInfos.add(new DiagramNodeInfo(
                    uniqueId,
                    (Integer)node.getMetaData().get("x"),
                    (Integer)node.getMetaData().get("y"),
                    (Integer)node.getMetaData().get("width"),
                    (Integer)node.getMetaData().get("height")));
            if (node instanceof NodeContainer) {
               addNodesInfo(nodeInfos, ((NodeContainer) node).getNodes(), prefix + node.getId() + ":");
            }
        }
    }


}
