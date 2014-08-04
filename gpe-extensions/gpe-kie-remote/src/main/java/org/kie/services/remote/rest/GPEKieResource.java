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

package org.kie.services.remote.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.kie.remote.services.rest.graph.jaxb.ActiveNodeInfo;
import org.kie.services.remote.IGPEKieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 */
@Stateless
@Path("/GPEKieResource")
public class GPEKieResource {

    @EJB(lookup="java:global/business-central/kieService!org.kie.services.remote.IGPEKieService")
    IGPEKieService rProxy;

    private Logger log = LoggerFactory.getLogger("GPEKieResource");
    
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/GPEKieResource/com.redhat.gpe.refarch.bpm_signalling:processTier:1.0/processes
     */
    @GET
    @Path("/{deploymentId: .*}/processes")
    @Produces({ "application/json" })
    public Response listProcesses(@PathParam("deploymentId") final String deploymentId) {
        List<String> processList = null;
        ResponseBuilder builder = null;
        try {
            processList = rProxy.listProcesses(deploymentId);
            log.info("listProcesses() # of processes = "+processList.size());
            if(processList.size() == 0) {
                builder = Response.status(Status.NO_CONTENT);
            } else {
                StringBuilder sBuilder = new StringBuilder("[");
                for(String processId : processList){
                    sBuilder.append(processId);
                    sBuilder.append(",");
                }
                sBuilder.append("]");
                builder = Response.ok(sBuilder.toString());
            }
        } catch(Throwable x){
            builder = Response.status(Status.INTERNAL_SERVER_ERROR);
        }
        return builder.build();
    }
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/GPEKieResource/com.redhat.gpe.refarch.bpm_signalling:processTier:1.0/process/259
     */
    @GET
    @Path("/{deploymentId: .*}/process/{pInstanceId: .*}")
    @Produces({ "application/json" })
    public Response getActiveNodeInfo(@PathParam("deploymentId") final String deploymentId, @PathParam("pInstanceId") final String pInstanceId) {
        ResponseBuilder builder = null;
        try {
            List<ActiveNodeInfo> aNodeInfo = rProxy.getActiveNodeInfo(deploymentId, pInstanceId);
            builder = Response.status(Status.OK);
        } catch(Throwable x){
            builder = Response.status(Status.INTERNAL_SERVER_ERROR);
        }
        return builder.build();
    }
}

