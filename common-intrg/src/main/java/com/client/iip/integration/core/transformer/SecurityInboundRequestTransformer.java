package com.client.iip.integration.core.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.MessageTracker;
import com.client.iip.integration.sa.ClientEnterpriseConfigDAO;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.security.IIPUser;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.thread.IIPUserContext;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPConfirmationCodeHolder;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.entconfig.users.EnterpriseUserConfigurationService;


/**
 * Create the request for Secure in bound Request.
 * 
 * @author Saurabh.Bhatnagar
 *
 */
public class SecurityInboundRequestTransformer extends AbstractMessageTransformer {
	
	private com.client.iip.integration.sa.ClientEnterpriseConfigDAO clientEnterpriseConfigDAO;
	private UserDetailsService userDetailsService;
	private Logger logger = LoggerFactory.getLogger(SecurityInboundRequestTransformer.class);
	
	public static final String PRINCIPLE_ID_PROPERTY = "PRINCIPLE_ID";
	public static final String SKIP_CONFIRMATION_MESSAGES_PROPERTY = "SKIP_CONFIRMATION_MESSAGES";
	public static final String TOKEN_ID_PROPERTY = "TOKEN_ID";
	public static final String TRACKING_ID_PROPERTY = "TRACKING_ID";
	
	public static final int SUCCESS = 200;
	public static final String INTERFACE_PROPERTY = "interface";
	public static final String HTTP_METHOD = "http.method";
	public static final String GET = "GET";
	public static final String SECURITY_ENABLE = "iip.interface.security.config";
	
	/**
	 * @return clientClaimStatusDAO
	 */
	public ClientEnterpriseConfigDAO getClientEnterpriseConfigDAO() {
		return clientEnterpriseConfigDAO;
	}

	/**
	 * @param value
	 *            Client Enterprise Config DAO to set
	 */
	@Inject(DaoInterface = "client.daointerface.clientEntConfigDao")
	public void setClientEnterpriseConfigDAO(ClientEnterpriseConfigDAO value) {
		this.clientEnterpriseConfigDAO = value;
	}	
	
	/**
	 * @return the userDetailsService
	 */
	public UserDetailsService getUserDetailsService() {
		if(this.userDetailsService == null){
			this.userDetailsService = MuleServiceFactory.getService(UserDetailsService.class);
		}
		return userDetailsService;
	}
	/**
	 * @return the enterpriseUserConfigurationService
	 */
	public EnterpriseUserConfigurationService getEnterpriseUserConfigurationService() {
		EnterpriseUserConfigurationService enterpriseUserConfigurationService = 
			MuleServiceFactory.getService(EnterpriseUserConfigurationService.class);
		return enterpriseUserConfigurationService;
	}
	
	/**
	 * Transform the message.
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		
		//STEP 0 - Enable Security
		String enableSecurity = System.getProperty(SECURITY_ENABLE);
		try{
			//Setup Import parameter flag in Thread data context, This will be used by the rules invoker to bypass the configured rules for interfaces.
			IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
			IIPDataContext context = threadCtx.getDataContext()==null?new IIPDataContext():threadCtx.getDataContext();
			context.setAppData("ExternalUser", "true");
			
			//Generate RequestId and assign to MDC Context for Log4j debug tracking
			MDC.put("REQUEST_ID", generateRequestId()); 
			

			//By-pass confirmation messages, if flag is turned ON - default to TRUE to by-pass messages
			String skipConfirmationMessages = message.getProperty(SKIP_CONFIRMATION_MESSAGES_PROPERTY, PropertyScope.INBOUND);
			skipConfirmationMessages(skipConfirmationMessages);
	
			if(enableSecurity !=null && enableSecurity.equalsIgnoreCase("true")){
					//STEP 1 - Submit the URL to retrieve the status. 
					String url = message.getProperty(TOKEN_ID_PROPERTY, PropertyScope.INBOUND);
					HttpClient client = new HttpClient();
					GetMethod method = null;
					method = new GetMethod(url);
					client.executeMethod(method);
					int returnCode = method.getStatusLine().getStatusCode();
					logger.info("Http Token URL Return Code: {} ", returnCode);
					if(returnCode != SUCCESS){
						logger.error("Error verifying TOKEN_ID: {}. The error status code = {} ", url, returnCode);
						throw new IIPCoreSystemException("Error verifying TOKEN_ID = " + url + ". The error status code = " + returnCode);
					}					
				}
				//payload = message.getPayloadAsString();
				String userName = message.getProperty(PRINCIPLE_ID_PROPERTY, PropertyScope.INBOUND);
				// STEP -2 - Need to authorize user provided in PRINCIPLE_ID.
					if(userName!=null){
						UserDetails details = this.retrieveUser(userName);
						if(details!=null){
							// STEP 3 - Need to log the TRACKING_ID in log file.
							String trackingId = message.getProperty(TRACKING_ID_PROPERTY, PropertyScope.INBOUND);
							if(MessageTracker.isEnabled()){
								trackingId = trackingId==null?"":trackingId;
								String trackingIdData = "TRACKING_ID Received : "+ trackingId;
								String principleIdData = "PRINCIPLE_ID Received : " + userName;
								MessageTracker.write(trackingIdData);
								MessageTracker.write(principleIdData);
								logger.info("TRACKING_ID Received : {} and Date_Time : {} ", 
									trackingId, DateUtility.getSystemDateTime());
							}
							// STEP 4 - Set the user id in Context
							IIPUser iipUser = (IIPUser)details;
							IIPUserContext userContext = IIPThreadContextFactory.getUserContext();
							userContext.setId(iipUser.getUserId());
							
							//Creating User instance to set in Authentication object
							User userDetails = new User(details.getUsername(), details.getPassword(), iipUser.isEnabled(), 
									iipUser.isAccountNonExpired(), iipUser.isCredentialsNonExpired(), 
									iipUser.isAccountNonLocked(), iipUser.getAuthorities());
							
							UsernamePasswordAuthenticationToken login = 
								new UsernamePasswordAuthenticationToken(userDetails, details.getPassword(), iipUser.getAuthorities());
							SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
							SecurityContextHolder.getContext().setAuthentication(login);
				
						}else {
							throw new IIPCoreSystemException("UserName does not exists in Stream");
						}
					}else {
						logger.warn("!!!!PRINCIPLE_ID Not provided for external interface. Possible Audit issue. Please Fix!!!");
						//throw new IIPCoreSystemException("UserName should be sent as PRINCIPLE_ID");
					}
				
			
		} catch (Exception ex){
			logger.error("Exception in transformMessage", ex);
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"InterfaceSecurityfailure"};
			IIPObjectError ioe = new IIPObjectError("SecurityInboundRequestTransformer", "transformMessage",
					null, codes, null, MessageConstants.SEVERITY_ERROR);
			ioe.setFormattedMessage(ex.toString());
			ioes.add(ioe);
			thisError.setFormattedErrors(ioes);
			errResponse.add(thisError);
			message.setPayload(errResponse);
			message.setExceptionPayload(null);			
			message.setProperty(INTERFACE_PROPERTY, "INBNDERR" , PropertyScope.OUTBOUND);	
			return message;
		}
		return message;
	}
	
	private String generateRequestId(){
		Random ran = new Random();
		return Integer.toString(ran.nextInt(10000000));
	}
	
	/**
	 * Return the User Details if user exist in stream.
	 * @param username
	 * @return UserDetails
	 * @throws AuthenticationException
	 */
	public UserDetails retrieveUser(String username)
			throws AuthenticationException {
		//copied from DaoAuthenticationProvider
	    UserDetails loadedUser;

        try {
            loadedUser = this.getUserDetailsService().loadUserByUsername(username);
        }
        catch (DataAccessException repositoryProblem) {
            throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new AuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
	}
	
	/**
	 * Sets all Confirmation Message Rules code to be ignored if the flag is set
	 * as a property in the service request.
	 * 
	 * @param skipConfirmationMessages
	 * 
	 */
	private void skipConfirmationMessages(String skipConfirmationMessages) {

		logger.debug("Skip confirmation messages property = {}", skipConfirmationMessages);

		boolean skipConfirmationMessagesFlag = "false".equalsIgnoreCase(skipConfirmationMessages) ? false : true;

		if (skipConfirmationMessagesFlag) {
			logger.debug("Skipping confirmation messages");
			Collection<String> confirmationMessageCodes = retrieveConfirmationMessageCodes();
			if (CollectionUtils.isNotEmpty(confirmationMessageCodes)) {
				IIPConfirmationCodeHolder.addAllConfirmationCodes(confirmationMessageCodes);
			}
		}
	}

	/**
	 * Retrieves all Confirmation Message Rule Codes to be ignored.
	 * 
	 * @return confirmationMessageRuleCodes
	 * 
	 */
	public Collection<String> retrieveConfirmationMessageCodes() {

		Collection<CodeLookupBO> confirmationMessageCodes = getClientEnterpriseConfigDAO()
				.retrieveConfirmationMessageRules();

		Collection<String> confirmationMessageRuleCodes = new ArrayList<String>();

		if (CollectionUtils.isNotEmpty(confirmationMessageCodes)) {
			for (CodeLookupBO codelookup : confirmationMessageCodes) {
				confirmationMessageRuleCodes.add(codelookup.getCode().toString());
			}
		}
		return confirmationMessageRuleCodes;
	}

}
