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


package org.kie.services.remote.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.kie.services.remote.rest.graph.jaxb.ActiveNodeInfo;
import org.kie.services.remote.IGPEKieService;
import org.kie.services.remote.IGPERemoteKieService;

/*
 * EJB Facade that introduces transaction boundaries and remoting interface for KieService functionality
 */

@Remote(IGPERemoteKieService.class)
@Local(IGPEKieService.class)
@Singleton(name="kieService")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class KieService implements IGPEKieService {
    
    @Inject
    private IGPEKieService kieBean;
    
    public Map<String, Object> startProcessAndReturnInflightVars(String deploymentId, String processId, Map<String, Object> params) {
    	return kieBean.startProcessAndReturnInflightVars(deploymentId, processId, params);
    }

    public List<String> listProcesses(String deploymentId) {
        return kieBean.listProcesses(deploymentId);
    }
    
    /*
    Caused by: java.lang.IllegalStateException: This persistence strategy only deals with UserTransaction instances!
	at org.jbpm.process.audit.strategy.StandaloneJtaStrategy.commitTransaction(StandaloneJtaStrategy.java:98)
	at org.jbpm.process.audit.strategy.StandaloneJtaStrategy.leaveTransaction(StandaloneJtaStrategy.java:89)
	at org.jbpm.process.audit.JPAAuditLogService.closeEntityManager(JPAAuditLogService.java:335) 
	at org.jbpm.process.audit.JPAAuditLogService.findProcessInstance(JPAAuditLogService.java:164
	at org.kie.services.remote.cdi.KieServiceBean.getActiveNodeInfo(KieServiceBean.java:79) [gpe-kie-remote.jar:1.0]
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<ActiveNodeInfo> getActiveNodeInfo(String deploymentId, String instanceId) {
        return kieBean.getActiveNodeInfo(deploymentId, instanceId);
    }

	@Override
	public void test() {
		kieBean.test();
	}

}
