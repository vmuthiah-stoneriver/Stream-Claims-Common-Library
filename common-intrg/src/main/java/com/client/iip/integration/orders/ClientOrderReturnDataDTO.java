package com.client.iip.integration.orders;

import java.util.Collection;

import com.client.iip.integration.documents.ClientDocumentDTO;
import com.stoneriver.iip.orders.api.OrderReturnDataDTO;


public class ClientOrderReturnDataDTO extends OrderReturnDataDTO {
	
	private String claimNumber; 
	
	private Collection<ClientDocumentDTO> orderDocs;

	/**
	 * @return the orderDocs
	 */
	public Collection<ClientDocumentDTO> getOrderDocs() {
		return orderDocs;
	}

	/**
	 * @param orderDocs the orderDocs to set
	 */
	public void setOrderDocs(Collection<ClientDocumentDTO> orderDocs) {
		this.orderDocs = orderDocs;
	}

	/**
	 * @return the claimNumber
	 */
	public String getClaimNumber() {
		return claimNumber;
	}

	/**
	 * @param claimNumber the claimNumber to set
	 */
	public void setClaimNumber(String claimNumber) {
		this.claimNumber = claimNumber;
	}
	

}
