package com.client.iip.integration.financials.disbursement.external.processor;

import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentPayeeEFTDTO;

/**
 * The DTO contains Payee information related to check payment and process 
 * Disbursement to External Disbursement Processor.
 * 
 * @author gradhak
 *
 * Payment Requestor
 */

public class ClientPaymentPayeeEFTDTO extends PaymentPayeeEFTDTO {

	private String requestedBy;

	/**
	 * @return the requestedBy
	 */
	public String getRequestedBy() {
		return requestedBy;
	}

	/**
	 * @param requestedBy the requestedBy to set
	 */
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	
}
