package com.client.iip.integration.core.security;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;

@Pojo(id = "interfaceSecurityFilter")
public class InterfaceSecurityFilter implements Filter {

	public boolean accept(MuleMessage message) {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		// we want to continue if authentication has not passed
		if ((auth == null) || (!auth.isAuthenticated())) {
			return false;
		} else {
			return true;
		}
	}

}
