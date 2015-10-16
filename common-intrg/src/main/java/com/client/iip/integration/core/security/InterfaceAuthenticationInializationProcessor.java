package com.client.iip.integration.core.security;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class InterfaceAuthenticationInializationProcessor implements MessageProcessor {

	public MuleEvent process(MuleEvent event) throws MuleException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		auth.setAuthenticated(false);
		return event;
	}

}
