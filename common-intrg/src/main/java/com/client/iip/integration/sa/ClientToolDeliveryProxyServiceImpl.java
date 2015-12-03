package com.client.iip.integration.sa;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.util.MuleEndpointAdapter;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.entconfig.tool.IIPToolDeliveryException;
import com.stoneriver.iip.entconfig.tool.ToolDeliveryDetailsCompositeDTO;
import com.stoneriver.iip.entconfig.tool.ToolDeliveryProxyService;


public class ClientToolDeliveryProxyServiceImpl extends MuleEndpointAdapter 
			implements ToolDeliveryProxyService {
	
	private Logger logger = LoggerFactory.getLogger(ClientToolDeliveryProxyServiceImpl.class);
	
	private final static String EMAIL_TOOL_ENDPOINT = "emailToolEndpoint";

	@Override
	public void sendToolInformation(
			ToolDeliveryDetailsCompositeDTO deliveryDetailsCompositeDTO)
			throws IIPToolDeliveryException {
		//Call SMTP Email endpoint here.
		try{
			sendSyncEvent(EMAIL_TOOL_ENDPOINT, deliveryDetailsCompositeDTO);
		}catch (Throwable e) {
			logger.error("Exception Occurred while Calling Email Tool: ", e);
			Throwable cause = e.getCause();
			logger.error("Exception Cause Occurred Calling sendToolInformation: ", cause);
			throw new IIPCoreSystemException(e);
		}

	}

	@Override
	public void printDocuments(
			ToolDeliveryDetailsCompositeDTO deliveryDetailsCompositeDTO)
			throws IIPToolDeliveryException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<String> retrievePrintersList()
			throws IIPToolDeliveryException {
		// TODO Auto-generated method stub
		return null;
	}

}
