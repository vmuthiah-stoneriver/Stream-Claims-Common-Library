package com.client.iip.integration.core.webservice;


import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;

public class SOAPHeaderTransformer extends AbstractMessageTransformer {
	
	private final Logger logger = LoggerFactory.getLogger(SOAPHeaderTransformer.class);
	
	public static final String ENDPOINT_NAME = "endPointName";
	public static final String PRINCIPLE_ID_PROPERTY = "PRINCIPLE_ID";
	public static final String TOKEN_ID_PROPERTY = "TOKEN_ID";
	public static final String TRACKING_ID_PROPERTY = "TRACKING_ID";	
	
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException
	    {
	    	try{
	    		String vmEndPointName = message.getProperty(ENDPOINT_NAME, PropertyScope.SESSION);
	    		logger.info("endPointName : " +  vmEndPointName);
	    		String userName = message.getProperty(PRINCIPLE_ID_PROPERTY, PropertyScope.INBOUND);
	    		String trackingId = message.getProperty(TRACKING_ID_PROPERTY, PropertyScope.INBOUND);
	    		String url = message.getProperty(TOKEN_ID_PROPERTY, PropertyScope.INBOUND);
				IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
				IIPDataContext context = new IIPDataContext();
				context.setAppData(ENDPOINT_NAME, vmEndPointName);
				context.setAppData(PRINCIPLE_ID_PROPERTY, userName);
				context.setAppData(TRACKING_ID_PROPERTY, trackingId);
				context.setAppData(TOKEN_ID_PROPERTY, url);
				threadCtx.setDataContext(context);
				
	    	}catch(Exception ex){
	    		throw new TransformerException(this, ex);
	    	}
	    	return message;
	    }
	
}
