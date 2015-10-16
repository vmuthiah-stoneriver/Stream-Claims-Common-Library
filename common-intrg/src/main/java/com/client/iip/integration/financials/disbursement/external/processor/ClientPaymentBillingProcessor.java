package com.client.iip.integration.financials.disbursement.external.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementAddressDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementPartyDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailBillingDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentMarkerInterface;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentProcessor;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentSequenceGenHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.external.processor.BasePaymentProcessor;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;

/**
 * This class will populate PaymentDetailBillingDTO data after parsing
 * Disbursement DTO and getting additional information based on disbursement
 * transfer object.
 * 
 * @author skumar
 */
@Pojo(id = "financials.disbursement.pojo.clientPaymentBillingProcessorPojo")
public class ClientPaymentBillingProcessor extends BasePaymentProcessor implements PaymentProcessor {

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
	public PaymentDetailBillingDTO populateStagingData(DisbursementDTO dtoToProcess, PaymentMarkerInterface parentDto) {
		DisbursementPartyDTO disbursementPartyDTO = dtoToProcess.getDisbursementParty();
		DisbursementAddressDTO disbursementAddressDTO = dtoToProcess.getDisbursementAddress();

		PaymentDetailBillingDTO paymentDetailBillingDTO = new PaymentDetailBillingDTO();
		paymentDetailBillingDTO.setRecordTypeId(new BigDecimal(305));
		Long billAccId = ((PaymentDetailHeaderDTO) parentDto).getContextId();
		String billAccNumber = getExportResourceManager().getDisbursementDAO().findBillingAccountNumberById(billAccId)
				.iterator().next();
		paymentDetailBillingDTO.setAccountNumber(billAccNumber);

		if (disbursementPartyDTO != null) {
			paymentDetailBillingDTO.setPrimaryPayerName(disbursementPartyDTO.getPartyPayeePrimaryNameDerived());
		}
		if (disbursementAddressDTO != null) {
			paymentDetailBillingDTO.setAddressLine1(disbursementAddressDTO.getAddressLine1());
			paymentDetailBillingDTO.setAddressLine2(disbursementAddressDTO.getAddressLine2());
			paymentDetailBillingDTO.setCity(disbursementAddressDTO.getCity());
			paymentDetailBillingDTO.setState(disbursementAddressDTO.getPrimarySubDivision());
			paymentDetailBillingDTO.setAddressZip(disbursementAddressDTO.getPostalCode());
			paymentDetailBillingDTO.setCountry(disbursementAddressDTO.getCountry());
		}
		setPaymentDeatilBillingInParent(parentDto, paymentDetailBillingDTO);
		return paymentDetailBillingDTO;
	}

	/**
	 * Set payment header within parent (I.e. Payment Detail header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentDetailBillingDTO
	 *            Input payment detail billing DTO.
	 */
	private void setPaymentDeatilBillingInParent(PaymentMarkerInterface parentDto,
			PaymentDetailBillingDTO paymentDetailBillingDTO) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentDetailHeaderDTO) {
			// Set control number within detail header for context billing.
			((PaymentDetailHeaderDTO) parentDto).setDetailControlNumber(ExportPaymentSequenceGenHelper
					.getNextSequenceNumber(DisbursementConstants.FINANCIAL_CONTEXT_BILL));
			Collection<PaymentDetailBillingDTO> billingDtos = ((PaymentDetailHeaderDTO) parentDto)
					.getPaymentDetailBilling();
			if (billingDtos == null) {
				billingDtos = new ArrayList<PaymentDetailBillingDTO>();
			}
			billingDtos.add(paymentDetailBillingDTO);
			((PaymentDetailHeaderDTO) parentDto).setPaymentDetailBilling(billingDtos);
		}
	}
}
