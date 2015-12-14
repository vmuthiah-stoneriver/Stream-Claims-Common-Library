/**
 * 
 */
package com.client.iip.integration.core.transformer;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.client.iip.integration.core.util.MessageTracker;
import com.fiserv.isd.iip.core.security.IIPUser;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * Create the request for Secure out bound Request.
 * 
 * @author Saurabh.Bhatnagar
 *
 */
public class SecurityOutboundRequestTransformer extends AbstractMessageTransformer {
	

	public static final String PRINCIPLE_ID_PROPERTY = "PRINCIPLE_ID";
	public static final String AUTHORIZATION = "Authorization";
	public static final String USER_NAME = "client.esb.user";
	public static final String USER_PASSWORD = "client.esb.password";

	
	private final Logger logger = LoggerFactory.getLogger(SecurityOutboundRequestTransformer.class);
	
	private Base64Encoder encoder = new Base64Encoder();
	
	/**
	 * Transform the message.
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
			IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
			IIPDataContext context = threadCtx.getDataContext()==null?new IIPDataContext():threadCtx.getDataContext();
			context.setAppData("ExternalInterfaceRequest", "true");
			
			String principleId = null;
			
			// Need to set the HTTP Basic Authentication
			if(System.getProperty(USER_NAME) != null && System.getProperty(USER_PASSWORD) != null){
				String authHeaderStr = System.getProperty(USER_NAME) + ":" + System.getProperty(USER_PASSWORD);
				logger.info("Outbound authHeaderStr : {} ",authHeaderStr);	
                message.setProperty(AUTHORIZATION, "Basic " + encoder.encode(authHeaderStr.getBytes()), PropertyScope.OUTBOUND);
			}

            //Need to read userName from Context
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if(authentication!=null && 	authentication.getPrincipal()!=null){
				if(authentication.getPrincipal() instanceof IIPUser){
					IIPUser user = (IIPUser) authentication.getPrincipal();
					principleId = user.getUsername();
					message.setProperty(PRINCIPLE_ID_PROPERTY, principleId, PropertyScope.OUTBOUND);
					logger.info("Outbound Request PRINCIPLE_ID : {} ", user.getUsername());					
				}
			}
			
			try{			
				if(principleId != null){
					if(MessageTracker.isEnabled()){
						String principleIdData = "PRINCIPLE_ID Sent: " + principleId;
						MessageTracker.write(principleIdData);
						if(message != null && message.getPayload() instanceof String){
							MessageTracker.write("Request Payload : " + message.getPayloadAsString());
						}
					}					
				}
			}catch(Exception ex){
				logger.error("Error in Outbound Request: ", ex);
			}

		return message;
	}
	
}
