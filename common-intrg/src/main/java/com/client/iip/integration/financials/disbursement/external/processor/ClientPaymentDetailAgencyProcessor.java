package com.client.iip.integration.financials.disbursement.external.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.agency.AgencyFinancialsService;
import com.fiserv.isd.iip.bc.financials.agency.ViewFinancialTransactionCriteria;
import com.fiserv.isd.iip.bc.financials.agency.ViewFinancialTransactionResult;
import com.fiserv.isd.iip.bc.financials.api.FinancialTransactionDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementAddressDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementPartyDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailAgencyCommissionDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailAgencyPaymentDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentMarkerInterface;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentProcessor;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentSequenceGenHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.external.processor.BasePaymentProcessor;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyDTO;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyRequestDTO;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

/**
 * Helper class to export payment to external system.
 * 
 * @author Mumtaz Ali
 */
@Pojo(id = "financials.disbursement.pojo.clientPaymentAgencyProcessorPojo")
public class ClientPaymentDetailAgencyProcessor extends BasePaymentProcessor implements PaymentProcessor {

	/**
	 * Method to process payment detail header.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param parentDto
	 *            Input Parent Object where data should be populated and added.
	 * @return Processed object.
	 */
	public PaymentDetailAgencyPaymentDTO populateStagingData(DisbursementDTO dtoToProcess,
			PaymentMarkerInterface parentDto) {
		Collection<FinancialTransactionDTO> ftps = getExportResourceManager().getDsbFtpMap().get(
				dtoToProcess.getRecordId());
		/*
		 * Flush all context for application context Agency to re initialize
		 * sequence generator.
		 */
		ExportPaymentSequenceGenHelper.flushAllApplicationCtxPolicy();
		for (FinancialTransactionDTO financialTransactionDTO : ftps) {
			/*
			 * Configure context (e.g. Payment, Commission, etc) within
			 * financial mule configuration file to dynamically populate context
			 * based data within Payment Detail Header.
			 */
			if (DisbursementConstants.EXPORT_CTX_AGCY.equals(dtoToProcess.getFinancialAgreementTypeCode())) {
				ViewFinancialTransactionCriteria request = new ViewFinancialTransactionCriteria();
				request.setAgencyId(financialTransactionDTO.getFinancialContext().getAgencyId());
				Collection<ViewFinancialTransactionResult> results = MuleServiceFactory.getService(
						AgencyFinancialsService.class).retrieveFinancialTransactions(request);
				populateAgencyDetails(dtoToProcess, parentDto, results);
				if (DisbursementConstants.EXPORT_CONTEXT_AGENCY_COMMISSION.equals(dtoToProcess
						.getDisbursementTypeCode())) {
					populateAgencyCommissionDetails(dtoToProcess, parentDto, results);
				}
			}

		}
		// Increment and set Item count for Payment Detail Trailer.
		setPaymentDetailTrailerItemCount(parentDto, ftps);
		return new PaymentDetailAgencyPaymentDTO();
	}

	/**
	 * Method to process Agency Details.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param parentDto
	 *            Input Parent Object where data should be populated and added.
	 * @param resultsParam
	 *            Input Collection of ViewFinancialTransactionResult
	 */
	public void populateAgencyDetails(DisbursementDTO dtoToProcess, PaymentMarkerInterface parentDto,
			Collection<ViewFinancialTransactionResult> resultsParam) {
		PaymentDetailAgencyPaymentDTO paymentDetailAgencyPaymentDTO = new PaymentDetailAgencyPaymentDTO();
		paymentDetailAgencyPaymentDTO.setRecordTypeId(new BigDecimal(340));
		DisbursementPartyDTO disbursementPartyDTO = dtoToProcess.getDisbursementParty();
		DisbursementAddressDTO disbursementAddressDTO = dtoToProcess.getDisbursementAddress();

		if (resultsParam != null && resultsParam.size() > 0) {
			ViewFinancialTransactionResult dto = resultsParam.iterator().next();
			paymentDetailAgencyPaymentDTO.setAgencyNumber(dto.getAgencyNumber());
			paymentDetailAgencyPaymentDTO.setAgencyName(dto.getAgencyName());
		}
		if (disbursementPartyDTO != null) {
			paymentDetailAgencyPaymentDTO.setAgencyPartyId(disbursementPartyDTO.getPartyIdPayeePrimary());
		}
		if (disbursementAddressDTO != null) {
			paymentDetailAgencyPaymentDTO.setAddressLine1(disbursementAddressDTO.getAddressLine1());
			paymentDetailAgencyPaymentDTO.setAddressLine2(disbursementAddressDTO.getAddressLine2());
			paymentDetailAgencyPaymentDTO.setAddressZip(disbursementAddressDTO.getPostalCode());
			paymentDetailAgencyPaymentDTO.setCity(disbursementAddressDTO.getCity());
			paymentDetailAgencyPaymentDTO.setCountry(disbursementAddressDTO.getCountry());
			paymentDetailAgencyPaymentDTO.setState(disbursementAddressDTO.getPrimarySubDivision());
		}
		setPaymentDetailAgencyInParent(parentDto, paymentDetailAgencyPaymentDTO);
	}

	/**
	 * Set payment header within parent (I.e. Payment Detail header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentDetailAgencyDTO
	 *            Input payment detail Agency DTO.
	 */
	private void setPaymentDetailAgencyInParent(PaymentMarkerInterface parentDto,
			PaymentDetailAgencyPaymentDTO paymentDetailAgencyDTO) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentDetailHeaderDTO) {
			// Set control number within detail header for context Agency.
			((PaymentDetailHeaderDTO) parentDto).setDetailControlNumber(ExportPaymentSequenceGenHelper
					.getNextSequenceNumber(DisbursementConstants.FINANCIAL_CONTEXT_AGENCY));
			Collection<PaymentDetailAgencyPaymentDTO> agencyDtos = ((PaymentDetailHeaderDTO) parentDto)
					.getPaymentDetailAgencyPayment();
			if (agencyDtos == null) {
				agencyDtos = new ArrayList<PaymentDetailAgencyPaymentDTO>();
			}
			agencyDtos.add(paymentDetailAgencyDTO);
			((PaymentDetailHeaderDTO) parentDto).setPaymentDetailAgencyPayment(agencyDtos);
		}
	}

	/**
	 * Increment and set Item count for Payment Detail Trailer.
	 * 
	 * @param parentDto
	 *            Input PaymentDetailHeaderDTO
	 * @param ftps
	 *            FinancialTransactionDTOs.
	 */
	private void setPaymentDetailTrailerItemCount(PaymentMarkerInterface parentDto,
			Collection<FinancialTransactionDTO> ftps) {
		if (parentDto instanceof PaymentDetailHeaderDTO && ftps != null) {
			// Increment and set Item count for Payment Detail Trailer.
			Long itemCount = ((PaymentDetailHeaderDTO) parentDto).getPaymentDetailTrailer().getItemCount();
			if (itemCount == null) {
				itemCount = new Long(0);
			}
			itemCount = new Long(itemCount.longValue() + ftps.size());
			((PaymentDetailHeaderDTO) parentDto).getPaymentDetailTrailer().setItemCount(itemCount);
		}
	}

	/**
	 * Method to process Agency Commission Details.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param parentDto
	 *            Input Parent Object where data should be populated and added.
	 * @param resultsParam
	 *            Input Collection of ViewFinancialTransactionResult
	 */
	public void populateAgencyCommissionDetails(DisbursementDTO dtoToProcess, PaymentMarkerInterface parentDto,
			Collection<ViewFinancialTransactionResult> resultsParam) {
		PaymentDetailAgencyCommissionDTO paymentDetailAgencyCommissionDTO = new PaymentDetailAgencyCommissionDTO();

		// only one record for each FTP ID
		if (resultsParam != null && resultsParam.size() > 0) {
			ViewFinancialTransactionResult dto = resultsParam.iterator().next();
			MinimalPartyRequestDTO minimalPartyRequestDTO = new MinimalPartyRequestDTO();
			minimalPartyRequestDTO.setPartyId(dto.getPartyId());
			/*
			 * Need to set something, it's only used for fetching address, which
			 * we don't care about
			 */
			minimalPartyRequestDTO.setContextType("");
			MinimalPartyDTO minParty = MuleServiceFactory.getService(PartyService.class).retrieveMinimalParty(
					minimalPartyRequestDTO);
			if (minParty != null) {
				paymentDetailAgencyCommissionDTO.setInsuredName(minParty.isOrganization() ? minParty
						.getOrganizationName().getOrganizationName() : minParty.getPersonName().getPersonFirstName()
						+ " , " + minParty.getPersonName().getPersonMiddleName() + " , "
						+ minParty.getPersonName().getPersonLastName());
			}
			paymentDetailAgencyCommissionDTO.setRecordTypeId(new BigDecimal(341));
			paymentDetailAgencyCommissionDTO.setCmsnEffectiveDate(dto.getCmsnStmntEffectiveDate());
			paymentDetailAgencyCommissionDTO.setCmsnEndDate(dto.getCmsnStmntEndDate());
			paymentDetailAgencyCommissionDTO.setComment(dto.getComment());
			paymentDetailAgencyCommissionDTO.setCommissionAmount(dto.getCommissionFlatAmount());
			paymentDetailAgencyCommissionDTO.setCommissionRate(dto.getCommissionRate());
			paymentDetailAgencyCommissionDTO.setPolicyEffectiveDate(dto.getPolicyEffDate());
			paymentDetailAgencyCommissionDTO.setPolicyEndDate(dto.getPolicyEndDate());
			paymentDetailAgencyCommissionDTO.setPolicyNumber(dto.getPolicyNumber());
			paymentDetailAgencyCommissionDTO.setStatementId(dto.getFasId());
			paymentDetailAgencyCommissionDTO.setTransactionDate(dto.getTransactionDate());
		}
		setPaymentDetailAgencyCommissionInParent(parentDto, paymentDetailAgencyCommissionDTO);
	}

	/**
	 * Set payment header within parent (I.e. Payment Detail header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentDetailAgencyCommissionDTO
	 *            Input payment detail Agency DTO.
	 */
	private void setPaymentDetailAgencyCommissionInParent(PaymentMarkerInterface parentDto,
			PaymentDetailAgencyCommissionDTO paymentDetailAgencyCommissionDTO) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentDetailHeaderDTO) {
			// Set control number within detail header for context Agency
			// Commission.
			((PaymentDetailHeaderDTO) parentDto).setDetailControlNumber(ExportPaymentSequenceGenHelper
					.getNextSequenceNumber(DisbursementConstants.EXPORT_CONTEXT_AGENCY_COMMISSION));
			Collection<PaymentDetailAgencyCommissionDTO> agencyDtos = ((PaymentDetailHeaderDTO) parentDto)
					.getPaymentDetailAgencyCommission();
			if (agencyDtos == null) {
				agencyDtos = new ArrayList<PaymentDetailAgencyCommissionDTO>();
			}
			agencyDtos.add(paymentDetailAgencyCommissionDTO);
			((PaymentDetailHeaderDTO) parentDto).setPaymentDetailAgencyCommission(agencyDtos);
		}
	}
}
