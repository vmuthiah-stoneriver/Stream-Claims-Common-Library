package com.client.iip.integration.core.security;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;

import com.client.iip.integration.core.exception.IIPErrorTransformerHelper;
import com.fiserv.isd.iip.core.messageresolver.MessageContextResolver;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;

public class FailedAuthenticationMessageProcessor implements MessageProcessor {
	//message context resolver defined in core-impl
	private MessageContextResolver resolver;
	
	/**
	 * @return the resolver
	 */
	public MessageContextResolver getResolver() {
		return resolver;
	}

	/**
	 * @param resolver the resolver to set
	 */
	@Inject(PojoRef="messageContextResolver")
	public void setResolver(MessageContextResolver resolverIn) {
		resolver = resolverIn;
	}



	public MuleEvent process(MuleEvent event) throws MuleException {
		if (event.getMessage().getExceptionPayload()!=null){
			//clear out any exceptions and use our custom exception handling
			event.getMessage().setExceptionPayload(null);
		}
		MuleMessage message = event.getMessage();
		// set HTTP status code
		message.setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
				HttpConstants.SC_UNAUTHORIZED);
		// create the message payload
		IIPCoreSystemException credentialEx = new IIPCoreSystemException();
		credentialEx.setErrorCode("securityInvalidUsernamePassword");

		Object response = IIPErrorTransformerHelper.formatAndConvertException(credentialEx);
		
		//add error response to message payload and return
		message.setPayload(response);
		return event;
	}

}
