package com.client.iip.integration.core.webservice;

import java.util.ArrayList;
import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

public class SOAPRequestTransformer extends AbstractMessageTransformer {
	
	private final Logger logger = LoggerFactory.getLogger(SOAPRequestTransformer.class);
	public static final String TARGET_URL = "targetURL";
	public static final String WSDL_OPERATION = "operationName";
	public static final String SOAP_CLIENT = System.getProperty("iip.interface.soap.client")==null?"DEFAULT":System.getProperty("iip.interface.soap.client");
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		String payload = null;
	try{
			/*
			 * Logic to convert payload into SOAP Message
			 */
			if(message.getPayload() != null && message.getPayload() instanceof String){
				payload = message.getPayloadAsString();	
				String targetURL = message.getProperty(TARGET_URL, PropertyScope.SESSION);
				logger.info("targetURL : " +  targetURL);
				if(targetURL != null && targetURL.endsWith("wsdl")){
					String operationName = message.getProperty(WSDL_OPERATION, PropertyScope.SESSION);
					logger.info("operationName : " +  operationName);
					SOAPMessageBuilder messageBuilder = new SOAPMessageBuilder(targetURL, operationName);
					if(SOAP_CLIENT.equals("GENERIC")){
						payload =  messageBuilder.buildGenericSOAPMessage(payload);
					}else{
						payload =  messageBuilder.buildDefaultSOAPMessage(payload);
					}
					targetURL = messageBuilder.getRouterURL();
					message.setProperty("SOAPAction", messageBuilder.getSOAPAction(), PropertyScope.OUTBOUND);
				}
				/*Set targetURL - Strip the protocol since this would be appended by the outbound endpoint.
				 * Mule outbound endpoint would not bootstrap without the protocol in the address. so we are stripping it here and appending it 
				 * in the endpoint definition.*/
				if(targetURL != null){
					message.setProperty(TARGET_URL, targetURL.replace("http://", "").replace("https://", ""), PropertyScope.SESSION);
				}
				
		
				message.setPayload(payload);
			}
		}catch (Exception ex) {
			logger.error("SOAP Request Transformation Failed: ", ex);
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"SOAPRequestfailure"};
			IIPObjectError ioe = new IIPObjectError("SOAPRequestTransformer", "transformMessage",
					null, codes, null, MessageConstants.SEVERITY_ERROR);
			ioe.setFormattedMessage(ex.toString());
			ioes.add(ioe);
			thisError.setFormattedErrors(ioes);
			errResponse.add(thisError);
			message.setPayload(errResponse);
			message.setExceptionPayload(null);
		}
		
		return message;
		
	}
}
