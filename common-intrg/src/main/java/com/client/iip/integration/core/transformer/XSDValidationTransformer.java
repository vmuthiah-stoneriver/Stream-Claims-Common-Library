package com.client.iip.integration.core.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.MessageTracker;
import com.client.iip.integration.core.util.XSDValidator;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

public class XSDValidationTransformer extends AbstractMessageTransformer {

	public static final String INTERFACE_PROPERTY = "interface";
	
	public static final String WSDL_OPERATION = "operationName";
	
	public static final String SOAP_CLIENT = System.getProperty("iip.interface.soap.client")==null?"DEFAULT":System.getProperty("iip.interface.soap.client");
	
	private Logger logger = LoggerFactory
			.getLogger(XSDValidationTransformer.class);

	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		logger.info("Inside XSDValidationTransformer");
		String synchronousResponse = "false";
		String payload = null;
		try {
	        IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
	        IIPDataContext context = threadCtx.getDataContext();		
			synchronousResponse = (context == null || context.getAppData("ExternalInterfaceRequest")==null)?"false":(String)context.getAppData("ExternalInterfaceRequest");
			if(message.getPayload() != null && message.getPayload() instanceof String){
				payload = message.getPayloadAsString();
				// Check whether payload has system exception
				if (payload.trim().length() > 0 && !payload.contains("iipCoreSystemException") 
						&& !payload.contains("ErrorResponse")) {
					String xsdFileName = (String) message.getOutboundProperty("xsd");
					logger.info("xsdFileName using outbound property scope: {} ", xsdFileName==null?"NULL":xsdFileName);
					if (StringUtils.isEmpty(xsdFileName)) {
						xsdFileName = (String) message.getInboundProperty("xsd");
						logger.info("xsdFileName using inbound property scope: {} ", xsdFileName==null?"NULL":xsdFileName);
					}
					if(xsdFileName == null){
						throw new Exception("XSD Not Available.");
					}
					XSDValidator xsdval = new XSDValidator();
					xsdval.validateSchema(payload, xsdFileName);
				}
				message.setPayload(payload);
			}
		} catch (Exception ex) {
			
			//Log the Payload in Tracking File
			String commType = synchronousResponse.equals("true")?"Response":"Request";
			writeToLogFile(payload, commType);
			logger.error("XML Validation Failed: ", ex);
			message.setProperty(INTERFACE_PROPERTY, "INBNDERR" , PropertyScope.OUTBOUND);
			message.setProperty("http.status", 400 , PropertyScope.OUTBOUND);			
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"XMLValidationfailure"};
			IIPObjectError ioe = new IIPObjectError("XSDValidationTransformer", "transformMessage",
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
	
	private void writeToLogFile(String payload, String commType){
		try{
			if(MessageTracker.isEnabled()){
				String transTime = "Transaction Time: " + DateUtility.getSystemDateTime();
				String payloadData = commType + " Payload : "+ payload;
				MessageTracker.write(transTime);
				MessageTracker.write(payloadData);
			}
		}catch(Exception ex){
			logger.error("Exception while writing to tracker file: " , ex);
		}
	}

}
