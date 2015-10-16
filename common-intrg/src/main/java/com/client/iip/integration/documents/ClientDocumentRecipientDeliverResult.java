package com.client.iip.integration.documents;

import java.util.Collection;

/**
 * 
 * Document with List of Recipient Data.
 * @author saurabh.bhatnagar
 *
 */
public class ClientDocumentRecipientDeliverResult {
	
	private Long documentId;
	Collection<DocumentRecipientType> recipients;
	/**
	 * @return the documentId
	 */
	public Long getDocumentId() {
		return documentId;
	}
	/**
	 * @param value the documentId to set
	 */
	public void setDocumentId(Long value) {
		this.documentId = value;
	}
	/**
	 * @return the recipients
	 */
	public Collection<DocumentRecipientType> getRecipients() {
		return recipients;
	}
	/**
	 * @param value the recipients to set
	 */
	public void setRecipients(Collection<DocumentRecipientType> value) {
		this.recipients = value;
	}
	
	

}
