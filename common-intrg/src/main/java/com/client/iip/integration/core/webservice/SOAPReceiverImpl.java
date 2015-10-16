package com.client.iip.integration.core.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.util.MuleEndpointAdapter;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;

@WebService(endpointInterface = "com.client.iip.integration.core.webservice.SOAPReceiver", serviceName = "SOAPReceiver")
public class SOAPReceiverImpl extends MuleEndpointAdapter implements SOAPReceiver {
	
	/**
     * logger for this class.
     */
	private static final Logger logger = LoggerFactory.getLogger(SOAPReceiverImpl.class);	
	

	public String XML(String inputDocument) throws Exception{
		IIPDataContext context = IIPThreadContextFactory.getIIPThreadContext().getDataContext();
		String endPointName = (String)context.getAppData("endPointName");
		logger.info("EndPointName : " + (String)context.getAppData("endPointName"));
		logger.info("PRINCIPLE_ID : " + (String)context.getAppData("PRINCIPLE_ID"));
		logger.info("TRACKING_ID : " + (String)context.getAppData("TRACKING_ID"));
		Map<String, Object> messageProperties = new HashMap<String, Object>();
		messageProperties.put("PRINCIPLE_ID", (String)context.getAppData("PRINCIPLE_ID"));
		messageProperties.put("TOKEN_ID", (String)context.getAppData("TOKEN_ID"));
		messageProperties.put("TRACKING_ID", (String)context.getAppData("TRACKING_ID"));
		Object payload = sendSyncEvent("vm://"+ endPointName +"?connector=vmSync", inputDocument, messageProperties);
	    return payload.toString(); 
	  }
	
}
