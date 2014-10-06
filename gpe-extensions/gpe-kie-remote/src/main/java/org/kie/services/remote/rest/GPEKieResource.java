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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.graph.jaxb.ActiveNodeInfo;
import org.kie.services.remote.IGPEKieService;
import org.kie.services.remote.ObjectList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 */
@Stateless
@Path("/GPEKieResource")
public class GPEKieResource extends ResourceBase {

    @EJB(lookup="java:global/business-central/kieService!org.kie.services.remote.IGPEKieService")
    IGPEKieService rProxy;

    private Logger log = LoggerFactory.getLogger("GPEKieResource");
    
    @POST
    @Path("/{deploymentId: .*}/{processId: .*}/pInstance")
    public Response startProcessAndReturnInflightVars(@PathParam("deploymentId") final String deploymentId, 
    		                                          @PathParam("processId") final String processId){
    	ResponseBuilder builder = null;
    	try {
    		Map<String, String[]> requestParams = getRequestParams();
            String oper = getRelativePath();
            Map<String, Object> params = extractMapFromParams(requestParams, oper);
            
    		Map<String, Object> returnMap = rProxy.startProcessAndReturnInflightVars(deploymentId, processId, params);
    	} catch(Throwable x) {
    		x.printStackTrace();
            builder = Response.status(Status.INTERNAL_SERVER_ERROR);
    	}
    	return builder.build();
    }
    
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
        	x.printStackTrace();
            builder = Response.status(Status.INTERNAL_SERVER_ERROR);
        }
        return builder.build();
    }
    
    /**
     * sample usage :
     *  curl -v -u jboss:brms -X GET docker_bpms:8080/business-central/rest/GPEKieResource/com.redhat.gpe.refarch.bpm_signalling:processTier:1.0/process/activenodes/259
     */
    @GET
    @Path("/{deploymentId: .*}/process/activenodes/{pInstanceId: .*}")
    @Produces({ "application/json" })
    public Response getActiveNodeInfo(@PathParam("deploymentId") final String deploymentId, @PathParam("pInstanceId") final String pInstanceId) {
        ResponseBuilder builder = null;
        try {
            List<ActiveNodeInfo> nodeInfoList = rProxy.getActiveNodeInfo(deploymentId, pInstanceId);
            return marshalList(nodeInfoList, "activeNodeInfoList", ActiveNodeInfo.class, ObjectList.class);
        } catch(Throwable x){
        	x.printStackTrace();
            builder = Response.status(Status.INTERNAL_SERVER_ERROR);
        }
        return builder.build();
    }

    private Response marshalList(List marshalList, String jaxbListName, Class... jaxbTypes) {
        ResponseBuilder builder = null;
        if(marshalList == null || marshalList.isEmpty())
            builder = Response.status(Status.NOT_FOUND);
        else {
            ObjectList pList = new ObjectList(marshalList);
            JAXBContext jc;
            Writer sWriter = null;
            try {
                jc = JAXBContext.newInstance(jaxbTypes);
                Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                QName qName = new QName(jaxbListName);
                JAXBElement<ObjectList> jaxbElement = new JAXBElement<ObjectList>(qName, ObjectList.class, pList);
                //marshaller.marshal(jaxbElement, System.out);
                sWriter = new StringWriter();
                marshaller.marshal(jaxbElement, sWriter);
                builder = Response.ok(sWriter.toString());
            } catch (JAXBException e) {
                e.printStackTrace();
                builder = Response.status(Status.INTERNAL_SERVER_ERROR);
            }finally {
                try {
                    if(sWriter != null)
                        sWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.build();
    }
}

