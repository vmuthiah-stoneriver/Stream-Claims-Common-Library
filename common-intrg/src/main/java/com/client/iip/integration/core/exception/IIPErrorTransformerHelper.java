package com.client.iip.integration.core.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactory;

import com.fiserv.isd.iip.core.messageresolver.IIPMessageException;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.messageresolver.MessageContextResolver;
import com.fiserv.isd.iip.core.spring.BeanFactoryAccessor;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

/**
 * Helper for converting exceptions to XML.
 * @author brook
 *
 */
public final class IIPErrorTransformerHelper {

	/**
	 * private constructor.
	 */
	private static ResourceBundle codeMap = ResourceBundle.getBundle("properties/client-error-codes");
	
	private IIPErrorTransformerHelper() {
		super();
	}
	
	/**
	 * convert the formatted message exception to XML.
	 * @param msgEx the message exception to transform.
	 * @return XML representation of the message.
	 */
	public static String convertToXML(IIPMessageException msgEx){
		//do the transformation to Client response
		StringBuffer response = new StringBuffer("<errorResponse>\n");
		for (IIPObjectError error : msgEx.getFormattedErrors()){
			response.append("  <error>\n");
			response.append("    <errorCode>"+error.getCode()+"</errorCode>\n");
			response.append("    <formattedMessage>"+error.getFormattedMessage()+"</formattedMessage>\n");
			response.append("    <severityLevel>"+error.getSeverityLevel()+"</severityLevel>\n");
			response.append("  </error>\n");
		}
		response.append("</errorResponse>");
		return response.toString();
	}
	
	/**
	 * convert the formatted message exception to a response object.
	 * @param msgEx the message exception to transform.
	 * @return a collection of response objects.
	 */
	public static Collection<IIPErrorResponse> convertToMessageObject(IIPMessageException msgEx){
		//do the transformation to Client response
		Collection<IIPErrorResponse> response = new ArrayList<IIPErrorResponse>();		
		IIPErrorResponse thisError = new IIPErrorResponse();
		//thisError.setFormattedErrors(msgEx.getFormattedErrors());
		Collection<IIPObjectError> errors = new ArrayList<IIPObjectError>();

		for (IIPObjectError error : msgEx.getFormattedErrors()){
			String[] codes = null;
			if(error.getCode() == null 
					|| (error.getCode() != null && error.getCode().trim().equals("unknownError"))){
				codes = new String[] {resolveErrorCode(error)};
				error.setObjectName(IIPErrorTransformerHelper.class.getName());
				error.setMethodName("convertToMessageObject");
				error.setSeverityLevel(MessageConstants.SEVERITY_ERROR);
			}else{
				codes = new String[] {error.getCode()};
			}
			IIPObjectError ioe = new IIPObjectError(error.getObjectName(), error.getMethodName(),
					null, codes, null, error.getSeverityLevel());				
			ioe.setFormattedMessage(error.getFormattedMessage());
			errors.add(ioe);
		}
		thisError.setFormattedErrors(errors);
		response.add(thisError);
		return response;
	}
	
	/**
	 * New Method added to Resolve Unhandled Error Codes
	 */
	public static String resolveErrorCode(IIPObjectError error){
		String errorCode = "internalError";
		if(error.getFormattedMessage() != null){
			Enumeration<String> errorCodes = codeMap.getKeys();
			while (errorCodes.hasMoreElements()) {
				String errCode = (String) errorCodes.nextElement().trim();
				String errorMessage = codeMap.getString(errCode).trim();
				for(String errMessage:Arrays.asList(errorMessage.split(","))){
					if(StringUtils.containsIgnoreCase(error.getFormattedMessage(), errMessage)){
						errorCode = errCode;
						break;
					}
				}
			}			
		}else{
			error.setFormattedMessage("System Internal error was encountered - Please contact System Administration");
		}
		
		return errorCode;
	}
	/**
	 * Populate the exception with the formatted message using a message resolver.
	 * @param msgEx the message exception that contains the error data.
	 * @return a populated IIPMessageException.
	 */
	public static IIPMessageException getFormattedException(IIPMessageException msgEx){
		// get the message resolver
		BeanFactory factory = BeanFactoryAccessor.getBeanFactory();
		Object obj = factory.getBean("messageContextResolver");
		MessageContextResolver resolver = (MessageContextResolver) obj;
		resolver.translateValidationException(msgEx);
		return msgEx;
	}
	
	/**
	 * Populate and format an IIP exception (IIPCoreSystemException or ValidationException).
	 * @param t the exception to handle
	 * @return the XML representation.
	 */
	public static Object formatAndConvertException(Throwable t){
		IIPMessageException msgEx = new IIPMessageException(t);
		return convertToMessageObject(getFormattedException(msgEx));
	}
}
