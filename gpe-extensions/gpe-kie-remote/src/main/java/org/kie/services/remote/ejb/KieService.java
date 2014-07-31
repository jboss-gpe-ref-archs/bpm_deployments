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

import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.kie.services.remote.IGPEKieService;

/*
 * EJB Facade that introduces transaction boundaries and remoting interface for KieService functionality
 */

@Local(IGPEKieService.class)
@Singleton(name="kieService")
@Startup
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class KieService implements IGPEKieService {
    
    @Inject
    private IGPEKieService kieBean;

    public List<String> listProcesses(String deploymentId) {
        return kieBean.listProcesses(deploymentId);
    }
}