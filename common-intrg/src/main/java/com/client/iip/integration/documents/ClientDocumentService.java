package com.client.iip.integration.documents;

import java.util.Collection;

import com.fiserv.isd.iip.core.service.ServiceEndpoint;

@ServiceEndpoint("integration.endpoint.documentInboundEndPoint")
public interface ClientDocumentService {
	
	/**
	 * This method will update document delivery status
	 * @param request DocumentRecipientDeliverResult
	 */
	
	String updateDocumentStatus(Collection<ClientDocumentRecipientDeliverResult> requestList);
	
	/**
	 * Save Inbound Electronic media file attachments.
	 * @param document
	 */
	public void createNewInboundAttachment(ClientDocumentDTO document);

}
