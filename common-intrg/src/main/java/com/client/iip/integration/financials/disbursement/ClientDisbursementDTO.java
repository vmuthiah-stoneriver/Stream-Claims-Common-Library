package com.client.iip.integration.financials.disbursement;

import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailClaimDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentFileHeaderDTO;

/**
 * Client specialization of DisbursementDTO to include claim payment details.
 * 
 * @author Todd Beals
 */

public class ClientDisbursementDTO extends DisbursementDTO {

	private static final long serialVersionUID = 721201240374930008L;

	private PaymentFileHeaderDTO paymentFileHeader;

	Collection<PaymentDetailClaimDTO> claimPaymentDetails;

	public ClientDisbursementDTO() {
		super();
	}

	/**
	 * @return the paymentFileHeader
	 */
	public PaymentFileHeaderDTO getPaymentFileHeader() {
		return paymentFileHeader;
	}

	/**
	 * @param paymentFileHeader
	 *            the paymentFileHeader to set
	 */
	public void setPaymentFileHeader(PaymentFileHeaderDTO paymentFileHeader) {
		this.paymentFileHeader = paymentFileHeader;
	}

	/**
	 * Get Claim Payment Details
	 * 
	 * @return Collection
	 */
	public Collection<PaymentDetailClaimDTO> getClaimPaymentDetails() {
		return claimPaymentDetails;
	}

	/**
	 * Set Claim Payment Details
	 * 
	 * @param claimPaymentDetails
	 *            the claim payment details
	 * @return Collection
	 */
	public void setClaimPaymentDetails(
			Collection<PaymentDetailClaimDTO> claimPaymentDetails) {
		this.claimPaymentDetails = claimPaymentDetails;
	}

}
