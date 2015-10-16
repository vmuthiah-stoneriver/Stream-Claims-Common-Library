package com.client.iip.integration.core.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;

import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

/**
 * Exception transformer for IIP system exceptions.
 * 
 * @author brook
 * 
 */
public class IIPExceptionTransformer extends AbstractMessageTransformer {

	/**
	 * transform exceptions into XML messages.
	 * 
	 * @param message
	 *            the original mule message.
	 * @param outputEncoding
	 *            the output encoding.
	 * @return a mule message.
	 * @throws TransformerException
	 *             if there was an error in transformation.
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {

		if (null != message.getExceptionPayload()) {
 
			// Set the error message / soap fault as payload
			message.setPayload(IIPErrorTransformerHelper
					.formatAndConvertException( message.getExceptionPayload().getException().getCause()));

			String statusProperty = message
					.getOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY);
			//don't overwrite an existing status property
			if (StringUtils.isBlank(statusProperty)) {
				message.setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
						HttpConstants.SC_INTERNAL_SERVER_ERROR);
			}
			message.setExceptionPayload(null);

			return message;
		}else if(null != message.getPayload() && message.getPayload() instanceof List) {
				List payloadList = (ArrayList)message.getPayload();
				
				if(payloadList.isEmpty()){
					Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
					IIPErrorResponse thisError = new IIPErrorResponse();
					Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
					String[] codes = new String[] {"dataNotFound"};
					IIPObjectError ioe = new IIPObjectError("IIPExceptionTransformer", "transformMessage",
							null, codes, null, MessageConstants.SEVERITY_WARN);
					ioe.setFormattedMessage("No Results Found for the Request Criteria");
					ioes.add(ioe);
					thisError.setFormattedErrors(ioes);
					errResponse.add(thisError);
					message.setPayload(errResponse);
					message.setExceptionPayload(null);
				}
				
				return message;
			
		}else {
			return message;
		}
	}
}
