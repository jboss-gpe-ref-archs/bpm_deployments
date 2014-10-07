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

package org.kie.services.remote;

import java.util.List;
import java.util.Map;

import org.kie.remote.services.rest.graph.jaxb.ActiveNodeInfo;


public interface IGPEKieService {
    
    String PROCESS_INSTANCE_ID = "pInstanceId";
	String PROCESS_INSTANCE_STATE = "pInstanceState";
	public Map<String, Object> startProcessAndReturnInflightVars(String deploymentId, String processId, Map<String, Object> params);
	public List<ActiveNodeInfo> getActiveNodeInfo(String deploymentId, String instanceId);
    public List<String> listProcesses(String deploymentId);
	public void test();
}
