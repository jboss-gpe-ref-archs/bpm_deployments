package com.redhat.gpe.refarch.bpm_deployment.test;

import java.util.HashSet;
import java.util.Set;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;

import com.redhat.gpe.refarch.bpm_deployment.domain.Driver;

public class JaxbCommandsRequestTest {
    
    private static final String processId = "";
    private static final String deploymentId = "";
    private static final String DRIVER = "driver";
    private static final String POLICY_NAME = "policyName";
    public static final String EXTRA_JAXB_CLASSES_PROPERTY_NAME = "extraJaxbClasses";
    
    public static void main(String[] args) {
        
        Driver dObj = new Driver(98765, "alex policy");
        StartProcessCommand cmd = new StartProcessCommand(processId);
        cmd.putParameter(DRIVER, dObj);
        cmd.putParameter(POLICY_NAME, "Alex");
        
        JaxbCommandsRequest jaxbRequest = new JaxbCommandsRequest(deploymentId, cmd);
        JaxbSerializationProvider jaxbSerializationProvider = new JaxbSerializationProvider();
        
        /* ----- Required for deserialization on the server ---------- */
        Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
        extraJaxbClasses.add(Driver.class);
        String extraJaxbClassesPropertyValue = JaxbSerializationProvider.classSetToCommaSeperatedString(extraJaxbClasses);
        //msg.setStringProperty(EXTRA_JAXB_CLASSES_PROPERTY_NAME, extraJaxbClassesPropertyValue);

        /* ------- Required for the server to locate the target deployment for the process --------- */
        //msg.setStringProperty(DEPLOYMENT_ID, deploymentId);

        /* -------  Required for proper serialization on the Client side (for the JAXB context) ------- */
        jaxbSerializationProvider.addJaxbClasses(Driver.class);

        String xmlStr = jaxbSerializationProvider.serialize(jaxbRequest);
        System.out.println(xmlStr);

        
    }

}
