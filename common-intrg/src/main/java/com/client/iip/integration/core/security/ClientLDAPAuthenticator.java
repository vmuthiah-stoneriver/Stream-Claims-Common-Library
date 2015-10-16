package com.client.iip.integration.core.security;

import java.util.Iterator;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.util.Assert;

/**
 * This implementation of an authenticator is based on the spring PasswordComparisonAuthenticator.
 * However, we only want to confirm that the user is in LDAP and do not intend to verify that the
 * password is correct.  So the majority of the code is copied from PasswordComparisonAuthenticator
 * and the comparison code is eliminated. 
 * @author brook
 *
 */
public final class ClientLDAPAuthenticator extends AbstractLdapAuthenticator{

    //~ Constructors ===================================================================================================

    public ClientLDAPAuthenticator(BaseLdapPathContextSource contextSource) {
        super(contextSource);
    }

    //~ Methods ========================================================================================================

    public DirContextOperations authenticate(final Authentication authentication) {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Can only process UsernamePasswordAuthenticationToken objects");
        // locate the user and check the password

        DirContextOperations user = null;
        String username = authentication.getName();

        Iterator<String> dns = getUserDns(username).iterator();

        SpringSecurityLdapTemplate ldapTemplate = new SpringSecurityLdapTemplate(getContextSource());

        while (dns.hasNext() && user == null) {
            final String userDn = (String) dns.next();

            try {
                user = ldapTemplate.retrieveEntry(userDn, getUserAttributes());
            } catch (NameNotFoundException ignore) {
            }
        }

        if (user == null && getUserSearch() != null) {
            user = getUserSearch().searchForUser(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username, username);
        }

        return user;
    }

}
