package com.client.iip.integration.core.webservice;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.XMLUtility;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

public class SOAPResponseTransformer extends AbstractMessageTransformer {

	public static final String INTERFACE_PROPERTY = "interface";
	
	public static final String WSDL_OPERATION = "operationName";
	
	public static final String SOAP_CLIENT = System.getProperty("iip.interface.soap.client")==null?"DEFAULT":System.getProperty("iip.interface.soap.client");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		String payload = null;
		try{
			if(message.getPayload() != null && message.getPayload() instanceof String){
				payload = message.getPayloadAsString();
				/*
				 * Logic to Deconstruct SOAP Payload
				 */
		
				if(payload.startsWith("<soap:Envelope")){
					if(payload.contains("<soap:Fault")){
						payload = createExceptionPayload(XMLUtility.getInstance().fetchNodeValue(payload, "/Envelope/Body/Fault/faultstring"));
					}else{
						String operationName = message.getProperty(WSDL_OPERATION, PropertyScope.SESSION);
						logger.info("operationName : " +  operationName);
						NodeList resNodeList = null;
						if(operationName != null){
							resNodeList = XMLUtility.getInstance().fetchNodeList(payload,
							"/Envelope/Body/" + operationName + "Response");
						}else{
							resNodeList = XMLUtility.getInstance().fetchNodeList(payload, 
								"/Envelope/Body");
						}
						//Get the first child Node
						Node rootNode = (Node)resNodeList.item(0).getFirstChild();
						//Check if the SubNode under the rootNode is a Element Node or a Text Node. 
						//If Text Node then get the XML String
						switch(rootNode.getFirstChild().getNodeType()){
				  			case Node.ELEMENT_NODE :
				  				logger.info("Element Node Found");
				  				DOMSource source = new DOMSource(rootNode);
				  				StringWriter responsePayload = new StringWriter();
				  				TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(responsePayload));
				  				payload = responsePayload.toString();
				  				break;
				  			case Node.TEXT_NODE :
				  				logger.info("Text Node Found");
				  				payload = rootNode.getFirstChild().getNodeValue();
				  				break;
						}
				 	}
					message.setPayload(payload);
				}
			}
		}catch(Exception ex){
			logger.error("SOAP Response Transformation Failed: ", ex);
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"SOAPResponsefailure"};
			IIPObjectError ioe = new IIPObjectError("SOAPResponseTransformer", "transformMessage",
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

	private String createExceptionPayload(String errorMessage) {
		// TODO Auto-generated method stub
		return "<iipCoreSystemException><detailMessage>"+ errorMessage +"</detailMessage></iipCoreSystemException>";
	}
	
}
