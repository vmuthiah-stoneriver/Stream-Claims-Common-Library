package com.client.iip.integration.sa;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transport.email.MailUtils;
import org.mule.transport.email.SmtpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.transformer.SMTPEMailMessageTransformer;
import com.stoneriver.iip.entconfig.api.UserDetailsDTO;
import com.stoneriver.iip.entconfig.tool.ToolDeliveryDetailsCompositeDTO;

public class SMTPDeliveyToolEMailMessageTransformer extends
		SMTPEMailMessageTransformer {
	
	private final Logger logger = LoggerFactory.getLogger(SMTPDeliveyToolEMailMessageTransformer.class);
	
	public SMTPDeliveyToolEMailMessageTransformer(){
		super();
	}
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException{		
		ToolDeliveryDetailsCompositeDTO tool = (ToolDeliveryDetailsCompositeDTO) message.getPayload();
		Message email=null;
		try{
			email = new MimeMessage(((SmtpConnector) endpoint.getConnector()).getSessionDetails(endpoint).getSession());
			
    		//From User			
			String fromEmail = "";
			if(tool.getReplyToEmail() == null ){
				if(endpoint.getProperty("fromAddress") == null) {
					throw new TransformerException(this, new Exception("From Address not configured. Please set \"emailfrom\" JVM property."));
				}else{
					fromEmail = (String)endpoint.getProperty("fromAddress");
				}
			}else{
				fromEmail = tool.getReplyToEmail();
			}		
			
			email.setFrom(MailUtils.stringToInternetAddresses(fromEmail)[0]);
			
			//Recipient Email
            String toEmail = "";
    		
            for(UserDetailsDTO userDetail:tool.getToRecipients()){
            	if(!toEmail.isEmpty()) toEmail = toEmail + ";";
    			if(userDetail.getEmail() == null) {
    				throw new TransformerException(this, new Exception("To Email not configured. Please configure email address for the user - " 
    												+ userDetail.getFirstName() + " " + userDetail.getLastName()));
    			}           	
            	toEmail = userDetail.getEmail();
            }
			
			email.setRecipients(Message.RecipientType.TO, MailUtils.stringToInternetAddresses(toEmail));			
			email.setSubject(tool.getSubject());	
			email.setContent(tool.getDescription(), "text/html");
			
		}catch (Exception ex) {
			throw new TransformerException(this, ex);			
		}
		
		return email;
		
	}

}
