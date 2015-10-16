package com.client.iip.integration.financials.disbursement;

import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentClaimParticipantData;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailClaimDTO;

public class ClientPaymentDetailClaimDTO extends PaymentDetailClaimDTO {

	private static final long serialVersionUID = -7716408263604653520L;

	private Collection<PaymentClaimParticipantData> claimParticipant;

	/**
	 * @return the paymentDetailItemClaim
	 */
	public Collection<PaymentClaimParticipantData> getClaimParticipant() {
		return claimParticipant;
	}

	/**
	 * @param value
	 *            the paymentDetailItemClaim to set
	 */
	public void setClaimParticipant(
			Collection<PaymentClaimParticipantData> value) {
		this.claimParticipant = value;
	}

}
