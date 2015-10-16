package com.client.iip.integration.financials.disbursement.external.processor;

import java.util.ArrayList;
import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.api.ClaimFinancialsProxyService;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.DisbursementFTCriteria;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailClaimDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailItemClaimDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentMarkerInterface;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentProcessor;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentSequenceGenHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.external.processor.BasePaymentProcessor;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

/**
 * This class will populate PaymentDetailClaimDTO data after parsing
 * Disbursement DTO and getting additional information based on disbursement
 * transfer object.
 * 
 */
@Pojo(id = "financials.disbursement.pojo.clientPaymentClaimProcessorPojo")
public class ClientPaymentClaimProcessor extends BasePaymentProcessor implements PaymentProcessor {

	/**
	 * Method to process payment for context billing.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param parentDto
	 *            Input Parent Object where data should be populated and added.
	 * @return Processed object.
	 */
	@Override
	public PaymentDetailClaimDTO populateStagingData(DisbursementDTO dtoToProcess, PaymentMarkerInterface parentDto) {
		Collection<PaymentDetailClaimDTO> paymentDetailColl = new ArrayList<PaymentDetailClaimDTO>();
		PaymentDetailClaimDTO paymentDetailclaimDTO = new PaymentDetailClaimDTO();
		// return new PaymentDetailBillingDTO();
		ExportPaymentSequenceGenHelper.flushAllApplicationCtxPolicy();
		DisbursementFTCriteria dsbFTCriteria = new DisbursementFTCriteria();
		dsbFTCriteria.setDisbursementId(dtoToProcess.getRecordId());
		dsbFTCriteria.setUserId(dtoToProcess.getUserIdCreated());

		paymentDetailColl = MuleServiceFactory.getService(ClaimFinancialsProxyService.class)
				.retrieveClaimPaymentDetails(dsbFTCriteria);
		if (paymentDetailColl != null) {
			for (PaymentDetailClaimDTO paymentDetailClaims : paymentDetailColl) {
				if (paymentDetailClaims.getPaymentDetailItemClaim() != null) {
					Collection<PaymentDetailItemClaimDTO> paymentDetailsItemColl = paymentDetailClaims
							.getPaymentDetailItemClaim();
					for (PaymentDetailItemClaimDTO paymentDetailsItem : paymentDetailsItemColl) {
						paymentDetailsItem.setSurchangeIndicator("n");
					}
				}
			}
		}
		setPaymentDeatilClaimInParent(parentDto, paymentDetailColl);

		// Increment and set Item count for Payment Detail Trailer.
		setPaymentDetailTrailerItemCount(parentDto, paymentDetailColl);
		return paymentDetailclaimDTO;
	}

	/**
	 * Increment and set Item count for Payment Detail Trailer.
	 * 
	 * @param parentDto
	 *            Input PaymentDetailHeaderDTO
	 * @param claimInfo
	 *            Collection.
	 */
	private void setPaymentDetailTrailerItemCount(PaymentMarkerInterface parentDto,
			Collection<PaymentDetailClaimDTO> claimInfo) {
		if (parentDto instanceof PaymentDetailHeaderDTO && claimInfo != null) {
			// Increment and set Item count for Payment Detail Trailer.
			Long itemCount = ((PaymentDetailHeaderDTO) parentDto).getPaymentDetailTrailer().getItemCount();
			if (itemCount == null) {
				itemCount = new Long(0);
			}
			itemCount = new Long(itemCount.longValue() + claimInfo.size());
			((PaymentDetailHeaderDTO) parentDto).getPaymentDetailTrailer().setItemCount(itemCount);
		}
	}

	/**
	 * Set payment header within parent (I.e. Payment Detail header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentDetailclaimColl
	 *            Collection.
	 */
	private void setPaymentDeatilClaimInParent(PaymentMarkerInterface parentDto,
			Collection<PaymentDetailClaimDTO> paymentDetailclaimColl) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentDetailHeaderDTO) {
			// Set control number within detail header for context billing.
			((PaymentDetailHeaderDTO) parentDto).setDetailControlNumber(ExportPaymentSequenceGenHelper
					.getNextSequenceNumber(DisbursementConstants.FINANCIAL_CONTEXT_CLM));
			Collection<PaymentDetailClaimDTO> claimDtos = ((PaymentDetailHeaderDTO) parentDto).getPaymentDetailClaim();
			if (claimDtos == null) {
				claimDtos = new ArrayList<PaymentDetailClaimDTO>();
			}
			((PaymentDetailHeaderDTO) parentDto).setPaymentDetailClaim(paymentDetailclaimColl);
		}
	}
}
