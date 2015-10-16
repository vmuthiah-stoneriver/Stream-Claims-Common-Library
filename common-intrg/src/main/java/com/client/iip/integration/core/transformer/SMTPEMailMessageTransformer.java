/**
 * 
 */
package com.client.iip.integration.core.transformer;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.email.MailUtils;
import org.mule.transport.email.SmtpConnector;

import com.fiserv.isd.iip.core.security.services.mail.MailConfigDTO;

/**
 * @author vmuthiah
 *
 */
public class SMTPEMailMessageTransformer extends AbstractMessageTransformer {
	
	public SMTPEMailMessageTransformer() {
		super();
		registerSourceType(DataTypeFactory.OBJECT);
		setReturnDataType(DataTypeFactory.OBJECT);
	}

	/**
	 * This API will take mule message and transform it to mail configuration DTO.
	 * @param message MuleMessage.
	 * @param arg1 String.
	 * @throws TransformerException exception.
	 * @return Obj Object.
	 */
	@Override
	public Object transformMessage(MuleMessage message, String arg1)
			throws TransformerException {
		MailConfigDTO mailInfo = (MailConfigDTO)message.getPayload();		
		logger.info("Original Payload:"+message.getOriginalPayload()+"------------");
		Message email=null;
		
		try {
			email = new MimeMessage(((SmtpConnector) endpoint.getConnector()).getSessionDetails(endpoint).getSession());
			if(endpoint.getProperty("fromAddress") == null) {
				throw new TransformerException(this, new Exception("From Address not configured. Please set \"emailfrom\" JVM property."));
			}
			email.setFrom(MailUtils.stringToInternetAddresses((String)endpoint.getProperty("fromAddress"))[0]);
			if (mailInfo.getToRecepients() == null || mailInfo.getToRecepients().isEmpty()) {
				throw new TransformerException(this, new Exception("Email Address not configured for user"));
			}
			email.setRecipients(Message.RecipientType.TO, MailUtils.stringToInternetAddresses(mailInfo.getToRecepients().iterator().next()));

			if(endpoint.getProperty("subject") == null) {
				throw new TransformerException(this, new Exception("Subject not configured. Please set \"emailsubject\" JVM property."));
			}
			
			email.setSubject((String)endpoint.getProperty("subject"));
			email.setContent("Your New password: " + mailInfo.getPassword(),"text/html");
			
			//message.setOutboundProperty(MailProperties.TO_ADDRESSES_PROPERTY,mailInfo.getTo());
			//message.setOutboundProperty(MailProperties.CC_ADDRESSES_PROPERTY,"veera.muthiah@stoneriver.com");		
			//message.setOutboundProperty(MailProperties.INBOUND_SUBJECT_PROPERTY, "Password Reset.");
			//message.setOutboundProperty(MailProperties.FROM_ADDRESS_PROPERTY, "veera.muthiah@stoneriver.com");
			
			logger.info("Password reset notification send to receipient");
		} catch (Exception e) {
			throw new TransformerException(this, e);			
		}
		return email;
	}
}
