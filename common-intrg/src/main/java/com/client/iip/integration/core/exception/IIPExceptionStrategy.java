package com.client.iip.integration.core.exception;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.exception.AbstractMessagingExceptionStrategy;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.springframework.beans.factory.BeanFactory;

import com.fiserv.isd.iip.core.messageresolver.IIPMessageException;
import com.fiserv.isd.iip.core.messageresolver.MessageContextResolver;
import com.fiserv.isd.iip.core.spring.BeanFactoryAccessor;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

/**
 * IIP Exception Strategy.
 * 
 * @author brook
 * 
 */
public class IIPExceptionStrategy extends AbstractMessagingExceptionStrategy {

	/**
	 * Default constructor taking a MuleContext.
	 * 
	 * @param muleContext
	 *            local mule context
	 * 
	 */
	public IIPExceptionStrategy(MuleContext muleContext) {
		super(muleContext);
	}

	/**
	 * Handle the exception by returning it as the payload of the message and
	 * clear out the previous message exception payload.
	 * 
	 * @param ex
	 *            inbound exception
	 * @param event
	 *            mule event
	 * @return a mule event that contains the new message with the exception as
	 *         the message payload
	 */
	public MuleEvent handleException(Exception ex, MuleEvent event) {
		MuleMessage message = event.getMessage();
		// Set the error message / soap fault as payload
		IIPMessageException msgEx = new IIPMessageException(ex.getCause());

		// get the message resolver
		BeanFactory factory = BeanFactoryAccessor.getBeanFactory();
		Object obj = factory.getBean("messageContextResolver");
		MessageContextResolver resolver = (MessageContextResolver) obj;
		resolver.translateValidationException(msgEx);

		// do the transformation to Client response
		message.setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
				HttpConstants.SC_INTERNAL_SERVER_ERROR);
		StringBuffer response = new StringBuffer("<errorResponse>\n");
		for (IIPObjectError error : msgEx.getFormattedErrors()) {
			response.append("  <error>\n");
			response.append("    <errorCode>" + error.getCode()
					+ "</errorCode>\n");
			response.append("    <formattedMessage>"
					+ error.getFormattedMessage() + "</formattedMessage>\n");
			response.append("    <severityLevel>" + error.getSeverityLevel()
					+ "</severityLevel>\n");
			response.append("  </error>\n");
		}
		response.append("</errorResponse>");
		/*
		 * Collection<IIPErrorResponse> response = new
		 * ArrayList<IIPErrorResponse>();
		 * 
		 * for (IIPObjectError error : msgEx.getFormattedErrors()){
		 * IIPErrorResponse newError = new IIPErrorResponse();
		 * newError.setErrorCode(error.getCode());
		 * newError.setFormattedMessage(error.getFormattedMessage());
		 * newError.setSeverityCode(error.getSeverityLevel());
		 * response.add(newError); }
		 */
		message.setPayload(response.toString());

		return event;
	}
}
