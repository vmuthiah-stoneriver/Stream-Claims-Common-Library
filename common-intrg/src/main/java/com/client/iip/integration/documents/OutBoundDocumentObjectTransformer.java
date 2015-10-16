package com.client.iip.integration.documents;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.document.recipient.api.DocumentRecipientDeliverResult;

/**
 * 
 * Convert List<Strings> to Map<Long, Map<Long, DocumentRecipientDeliverResult>> 
 * 
 * @author Gaurav Rai 
 * 
 */

public class OutBoundDocumentObjectTransformer extends AbstractMessageTransformer {

public Object transformMessage(MuleMessage message, String outputEncoding)
		throws TransformerException {
	
	Map<Long, Map<Long, DocumentRecipientDeliverResult>> deliveryResultMap = new HashMap<Long, Map<Long, DocumentRecipientDeliverResult>>();
	ClientDocumentRecipientDeliverResult deliverResponse = null;
	
	try {
		deliverResponse = (ClientDocumentRecipientDeliverResult) message.getPayload();
		Map<Long, DocumentRecipientDeliverResult> tempResultMap = new HashMap<Long, DocumentRecipientDeliverResult>();
		for(DocumentRecipientType recipientType : deliverResponse.getRecipients()){
			DocumentRecipientDeliverResult deliverResult = new DocumentRecipientDeliverResult();
			deliverResult.setDocumentId(deliverResponse.getDocumentId());
			deliverResult.setRecipientPartyId(recipientType.getRecipientRecordId());
			deliverResult.setSentDate(recipientType.getSentDate());
			deliverResult.setSentStatus(recipientType.getSentStatus());
			deliverResult.setSentToDevice(recipientType.getSentToDevice());
			tempResultMap.put(recipientType.getRecipientRecordId(), deliverResult);			
		}
		deliveryResultMap.put(deliverResponse.getDocumentId(), tempResultMap);
		message.setPayload(deliveryResultMap);
		return message;

	} catch (Exception ex) {
		logger.error("Exception occured while converting to Object: ", ex);
		IIPCoreSystemException coreEx = new IIPCoreSystemException(
				ex.toString());
		message.setPayload(coreEx);
		message.setExceptionPayload(null);
		return message;
		}
	}
	
}
