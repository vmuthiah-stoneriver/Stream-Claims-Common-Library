package com.client.iip.integration.core.security;

import java.util.Collection;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LDAPUserDetailsContextMapper implements UserDetailsContextMapper {

	private UserDetailsService userDetailsService;

	/**
	 * @return the userDetailsService
	 */
	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	/**
	 * @param userDetailsService
	 *            the userDetailsService to set
	 */
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<GrantedAuthority> authority) {
		UserDetailsService service = userDetailsService;
		UserDetails details = service.loadUserByUsername(username);
		return details;
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		// not used.
	}

}
