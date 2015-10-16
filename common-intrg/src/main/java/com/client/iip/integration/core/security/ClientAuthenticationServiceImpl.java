package com.client.iip.integration.core.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.security.services.IIPAuthenticationService;

/**
 * Authentication service that calls to the client implementaion of ldap security.
 * @author brook
 *
 */
@Pojo(id="interfaceAuthenticationService")
public class ClientAuthenticationServiceImpl implements IIPAuthenticationService{
	private AuthenticationManager authManager;
	
	/**
	 * @param username  user login name
	 * @param password     password
	 * @return the authentication object
	 */
	public Authentication authenticateUser(
			String username, String password) {
		UsernamePasswordAuthenticationToken login = 
			new UsernamePasswordAuthenticationToken(username, password);
		Authentication auth = authManager.authenticate(login);
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
		SecurityContextHolder.getContext().setAuthentication(auth);
		return auth;
	}

	/**
	 * @return the authManager
	 */
	public AuthenticationManager getAuthManager() {
		return authManager;
	}

	/**
	 * @param authManagerTemp the authManager to set
	 */
	/* Review and Uncomment if required for LDAP
	 * @Inject(PojoRef = "interfaceAuthenticationManager")*/
	public void setAuthManager(AuthenticationManager authManagerTemp) {
		this.authManager = authManagerTemp;
	}

}
