/**
 * 
 */
package com.client.iip.integration.core.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.client.iip.integration.core.util.MessageTracker;
import com.client.iip.integration.sa.ClientEnterpriseConfigDAO;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.validation.IIPConfirmationCodeHolder;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;

/**
 * Create the request for Secure out bound Request.
 * 
 * @author Saurabh.Bhatnagar
 *
 */
public class SecurityOutboundResponseTransformer extends AbstractMessageTransformer {
	
	public static final String TRACKING_ID_PROPERTY = "TRACKING_ID";
	
	private Logger logger = LoggerFactory.getLogger(SecurityOutboundResponseTransformer.class);
	
	private ClientEnterpriseConfigDAO clientEnterpriseConfigDAO;
	
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
	 * Transform the message.
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		try{	
			//Setup Import parameter flag in Thread data context, This will be used by the rules invoker to bypass the configured rules for interfaces.
			IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
			IIPDataContext context = threadCtx.getDataContext()==null?new IIPDataContext():threadCtx.getDataContext();
			context.setAppData("ExternalInterfaceResponse", "true");
			String userName = null;
			//Check for External User Session Id
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        if (authentication != null && authentication.getPrincipal() != null){
	        	if (authentication.getPrincipal() instanceof User){
	        		User iu = (User)authentication.getPrincipal();
	        		userName = iu.getUsername();
	        		logger.info("Outbound Response PRINCIPLE_ID : {} ", userName);		
	        	}
	        }
			skipConfirmationMessages();

			//Need to log the Tracking Id comes in response
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
				
			}catch(Exception ex){
				logger.error("Error in Outbound Response: ", ex);
			}
			logger.debug("Payload : {}", message.getPayload());
			return message;
	}
	
	/**
	 * Sets all Confirmation Message Rules code to be ignored if the flag is set
	 * as a property in the service request.
	 * 
	 * @param skipConfirmationMessages
	 * 
	 */
	private void skipConfirmationMessages() {
		logger.debug("Skipping confirmation messages");
			Collection<String> confirmationMessageCodes = retrieveConfirmationMessageCodes();
			if (CollectionUtils.isNotEmpty(confirmationMessageCodes)) {
				IIPConfirmationCodeHolder.addAllConfirmationCodes(confirmationMessageCodes);
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
