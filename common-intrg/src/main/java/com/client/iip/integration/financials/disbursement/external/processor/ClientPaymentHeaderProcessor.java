package com.client.iip.integration.financials.disbursement.external.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.financials.disbursement.ClientPaymentHeaderDTO;
import com.fiserv.isd.iip.bc.financials.api.FinancialTransactionDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBankingInfoDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.CheckExpirationDateCriteria;
import com.fiserv.isd.iip.bc.financials.disbursement.external.CheckExpirationDateResult;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentFileHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentMarkerInterface;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentProcessor;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentSequenceGenHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.external.processor.BasePaymentProcessor;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.claims.financial.ClaimFinancialsDAO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;

/**
 * Helper class to export payment to external system.
 * 
 * @author Todd Beals
 * 
 */
@Pojo(id = "financials.disbursement.pojo.clientPaymentHeaderProcessorPojo")
public class ClientPaymentHeaderProcessor extends BasePaymentProcessor
		implements PaymentProcessor {

	private ClaimFinancialsDAO claimFinancialsDAO;
	private EnterpriseConfigService enterpriseConfigService;
	
	public static final String PAY_PAYEE_MANUAL_CHECK = "man_chkwrt";
	public static final String PAY_PAYEE_VENDOR_CC = "vend_cc";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Method to process payment for payment header.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param parentDto
	 *            Input Parent Object where data should be populated and added.
	 * @return Processed object.
	 */
	@Override
	public PaymentHeaderDTO populateStagingData(DisbursementDTO dtoToProcess,
			PaymentMarkerInterface parentDto) {
		
		logger.info("Processing Export disbursement : " + dtoToProcess.getRecordId());
		// Populate Payer related staging data.
		PaymentHeaderDTO paymentHeaderDTO = populatePayerStagingData(dtoToProcess);

		// Populate Payee related Staging Data.
		populatePayeeStagingData(dtoToProcess, paymentHeaderDTO);

		// Populate Payment Detail Header related Staging Data.
		populateDetailHeaderStagingData(dtoToProcess, paymentHeaderDTO);

		// Populate Payment Trailer related Staging Data.
		populatePaymentTrailerStagingData(dtoToProcess, paymentHeaderDTO);

		// Set payment header within parent (I.e. File header)
		setPaymentHeaderInParent(parentDto, paymentHeaderDTO);

		return paymentHeaderDTO;
	}

	/**
	 * Populate Payer related staging data.
	 * 
	 * @param dtoToProcess
	 *            Input dtoToProcess.
	 * @return PaymentHeaderDTO information.
	 */
	private PaymentHeaderDTO populatePayerStagingData(
			DisbursementDTO disbursementDTO) {

		/*
		 * Query to get all information related to Payer.
		 */

		DisbursementStatusDTO disbursementStatus = disbursementDTO
				.getCurrentStatus();
		String disbursementState = disbursementStatus.getDisbursementState();
		String disbursementStaus = disbursementStatus
				.getDisbursementStatusType();

		Collection<PaymentHeaderDTO> paymentHeaders = getExportResourceManager()
				.getDisbursementDAO().retrievePayorAddressonCompanyId(
						disbursementDTO.getCompanyId());
		DisbursementBankingInfoDTO disbursementBankingInfoDTO = disbursementDTO
				.getDisbursementBankingInfo();

		ClientPaymentHeaderDTO paymentHeaderDTO = new ClientPaymentHeaderDTO();
		if (disbursementBankingInfoDTO != null
				&& disbursementBankingInfoDTO.getCompanyBankAccountId() != null) {
			Collection<String> companyBankAccountNames = getExportResourceManager()
					.getDisbursementDAO().retrieveCompanyBankNameById(
							disbursementBankingInfoDTO
									.getCompanyBankAccountId());
			if (companyBankAccountNames != null
					&& !companyBankAccountNames.isEmpty()) {
				paymentHeaderDTO.setBankName(companyBankAccountNames.iterator()
						.next());
			}
		}
		paymentHeaderDTO.setDisbursmentId(disbursementDTO.getRecordId());
		paymentHeaderDTO.setRecordTypeId(new BigDecimal(200));
		paymentHeaderDTO.setDisbursementNumber(disbursementDTO
				.getDisbursementNumber());
		// Increment and add the control number
		paymentHeaderDTO
				.setControlNumber(ExportPaymentSequenceGenHelper
						.getNextSequenceNumber(DisbursementConstants.EXPORT_PAYMENT_HEADER));
		paymentHeaderDTO
				.setPaymentRequestTarget(getReqMethodTarget(disbursementDTO));
		paymentHeaderDTO.setPaymentMethodType(disbursementDTO
				.getDisbursementMethodPaymentCode());
		paymentHeaderDTO.setReferenceNumber(disbursementDTO
				.getDisbursementRequestNumber());
		paymentHeaderDTO.setPaymentDate(disbursementDTO
				.getDisbursementCreationDate());
		paymentHeaderDTO.setPaymentAmount(disbursementDTO
				.getDisbursementAmount());
		Collection<FinancialTransactionDTO> ftps = getExportResourceManager()
				.getDsbFtpMap().get(disbursementDTO.getRecordId());
		CheckExpirationDateCriteria criteria = new CheckExpirationDateCriteria();
		// looping only once as for each Disbursement Id ,there is similar
		// Jurisdiction Id.
		if (ftps != null && ftps.size() > 0) {
			Collection<Long> jurisIds = getExportResourceManager()
					.getDisbursementDAO().retrieveJurisdictionId(
							ftps.iterator().next().getRecordId());
			if (jurisIds != null && jurisIds.size() > 0) {
				criteria.setJurisdictionId(jurisIds.iterator().next());
			}
		}

		Collection<CheckExpirationDateResult> allowableDays = getExportResourceManager()
				.getDisbursementDAO().retrieveAgingRuleAllowingDays(criteria);
		int allowdays = 0;
		if (allowableDays != null && allowableDays.size() > 0) {
			for (CheckExpirationDateResult checkExpirationDateResult : allowableDays) {
				if (checkExpirationDateResult.getJurisdictionId() != null) {
					allowdays = checkExpirationDateResult.getAllowingDays()
							.intValue();
					break;
				}
			}
		}
		/*
		 * ( else { allowdays =
		 * allowableDays.iterator().next().getAllowingDays() .intValue(); }
		 */
		Date checkExpDate = DateUtility.addDays(
				disbursementDTO.getDisbursementCreationDate(), allowdays);
		paymentHeaderDTO.setCheckExpireDate(checkExpDate);
		paymentHeaderDTO.setPaymentID(disbursementDTO
				.getDisbursementRequestNumber());

		if (disbursementBankingInfoDTO != null
				&& disbursementBankingInfoDTO
						.getFinancialInstitutionRouteNoCompany() != null) {
			paymentHeaderDTO.setBankRoutingNumber(disbursementBankingInfoDTO
					.getFinancialInstitutionRouteNoCompany());
			paymentHeaderDTO.setBankAccountNumber(disbursementBankingInfoDTO
					.getPartyBankAccountNoCompany());
		}
		paymentHeaderDTO.setPayorNameLine1(disbursementDTO.getCompanyName());
		if (paymentHeaders != null && paymentHeaders.size() > 0) {
			PaymentHeaderDTO paymentDTO = paymentHeaders.iterator().next();
			paymentHeaderDTO
					.setPayorAdddressId(paymentDTO.getPayorAdddressId());
			paymentHeaderDTO.setPayorAddressLine1(paymentDTO
					.getPayorAddressLine1());
			paymentHeaderDTO.setPayorAddressLine2(paymentDTO
					.getPayorAddressLine2());
			paymentHeaderDTO.setPayorCity(paymentDTO.getPayorCity());
			paymentHeaderDTO.setPayorState(paymentDTO.getPayorState());
			paymentHeaderDTO
					.setPayorAddressZip(paymentDTO.getPayorAddressZip());
			paymentHeaderDTO.setPayorCountry(paymentDTO.getPayorCountry());
			paymentHeaderDTO.setPayorPhoneNumber(paymentDTO
					.getPayorPhoneNumber());
		}
		paymentHeaderDTO.setPrintGroupCode(disbursementDTO
				.getDisbursementPrintGroupCode());
		paymentHeaderDTO.setDipoCode(disbursementDTO
				.getDisbursementInPaymentOfCode());
		paymentHeaderDTO.setCompanyId(disbursementDTO.getCompanyId());
		paymentHeaderDTO.setDipoName(disbursementDTO
				.getDisbursementInPaymentOfName());

		paymentHeaderDTO.setComment(disbursementDTO.getComment());
		if (disbursementDTO != null
				&& disbursementDTO.getCompanyOrganizationUnitId() != null) {
			paymentHeaderDTO.setCompanyOrgUnitId(disbursementDTO
					.getCompanyOrganizationUnitId());
		}
		Collection<CodeLookupBO> codeLookUps = MuleServiceFactory.getService(
				EnterpriseConfigService.class).retrieveCorporationByCompanyId(
				disbursementDTO.getCompanyId());
		if (codeLookUps != null && !codeLookUps.isEmpty()) {
			CodeLookupBO codeLookUp = codeLookUps.iterator().next();
			paymentHeaderDTO.setCorporationId(new Long((codeLookUp.getCode()
					.toString())).longValue());
		}

		if (disbursementState != null
				&& disbursementStaus != null
				&& disbursementState
						.equals(DisbursementConstants.DISB_STATE_TYPE_CODE_INCOMPLETE)
				&& disbursementStaus
						.equals(DisbursementConstants.DISB_STATUS_TYPE_CODE_INCOMPLETE_PAYMENT)) {
			paymentHeaderDTO
					.setRecordType(DisbursementConstants.ISSUE_PAYMENT_REQUEST);
			// set the record type as I (Please add in a constant file this
			// value)
		} else if (disbursementState != null
				&& disbursementStaus != null
				&& disbursementState
						.equals(DisbursementConstants.DISB_STATE_TYPE_CODE_COMPLETE)
				&& disbursementStaus
						.equals(DisbursementConstants.DISB_STATUS_TYPE_CODE_PAYMENT_VOIDED_NOT_REISSUED)) {
			// set the record type as V (Please add in a constant file this
			// value)
			paymentHeaderDTO
					.setRecordType(DisbursementConstants.VOID_PAYMENT_REQUEST);
			paymentHeaderDTO.setVoidDate(disbursementStatus
					.getEffectiveDateTime());
		} else if (disbursementState != null
				&& disbursementStaus != null
				&& disbursementState
						.equals(DisbursementConstants.DISB_STATE_TYPE_CODE_OPEN)
				&& disbursementStaus
						.equals(DisbursementConstants.DISB_STATUS_TYPE_CODE_PAYMENT_STOPPED_NOT_REISSUED)) {

			paymentHeaderDTO
					.setRecordType(DisbursementConstants.STOP_PAYMENT_REQUEST);
			paymentHeaderDTO.setStopPaymentDate(disbursementStatus
					.getStopPaymentDate());
			paymentHeaderDTO.setStopPaymentNumber(disbursementStatus
					.getStopPaymentNumber());
			// get the value of void date
		}
		
		return paymentHeaderDTO;
	}

	/**
	 * Populate Payment Trailer related Staging Data.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param paymentHeaderDTO
	 *            Input PaymentHeaderDTO
	 */
	private void populatePaymentTrailerStagingData(
			DisbursementDTO dtoToProcess, PaymentHeaderDTO paymentHeaderDTO) {
		/*
		 * Populate Payment Trailer related Staging Data. Note that the payment
		 * trailer information is added within parent (I.e. Payment Header)
		 * within its processor.
		 */
		getExportResourceManager().getResourceManagerMap()
				.get(DisbursementConstants.EXPORT_PAYMENT_TRAILER)
				.populateStagingData(dtoToProcess, paymentHeaderDTO);
		/*
		 * Set all detail item count where item count = Count of all 300
		 * Records.
		 */
		paymentHeaderDTO.getPaymentTrailer().setItemCount(
				new Long(paymentHeaderDTO.getPaymentDetailHeader().size()));
	}

	/**
	 * Populate Payment Detail Header related Staging Data.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param paymentHeaderDTO
	 *            Input PaymentHeaderDTO
	 */
	private void populateDetailHeaderStagingData(DisbursementDTO dtoToProcess,
			PaymentHeaderDTO paymentHeaderDTO) {
		/*
		 * Populate Payment Detail Header related Staging Data. Note that the
		 * payment detail header information is added within parent (I.e.
		 * Payment Header) within its processor.
		 */
		getExportResourceManager().getResourceManagerMap()
				.get(DisbursementConstants.EXPORT_DETAIL_HEADER)
				.populateStagingData(dtoToProcess, paymentHeaderDTO);
	}

	/**
	 * Populate Payee related Staging Data.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param paymentHeaderDTO
	 *            Input PaymentHeaderDTO
	 */
	private void populatePayeeStagingData(DisbursementDTO dtoToProcess,
			PaymentHeaderDTO paymentHeaderDTO) {
		if (DisbursementConstants.PAY_PAYEE_CHECK.equals(dtoToProcess.getDisbursementMethodPaymentCode())
				|| DisbursementConstants.PAY_PAYEE_EFT.equals(dtoToProcess.getDisbursementMethodPaymentCode())
				|| PAY_PAYEE_MANUAL_CHECK.equals(dtoToProcess.getDisbursementMethodPaymentCode())
				|| PAY_PAYEE_VENDOR_CC.equals(dtoToProcess.getDisbursementMethodPaymentCode())) {
			/*
			 * Populate Payee Check or EFT Information. Note that payee
			 * check\EFT information is added within parent (I.e. Payment
			 * Header) within its processor.
			 */
			getExportResourceManager().getResourceManagerMap()
					.get(dtoToProcess.getDisbursementMethodPaymentCode())
					.populateStagingData(dtoToProcess, paymentHeaderDTO);
		}
	}

	/**
	 * Set payment header within parent (I.e. File header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentHeaderDTO
	 *            Input payment header DTO.
	 */
	private void setPaymentHeaderInParent(PaymentMarkerInterface parentDto,
			PaymentHeaderDTO paymentHeaderDTO) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentFileHeaderDTO) {
			Collection<PaymentHeaderDTO> paymentHeader = ((PaymentFileHeaderDTO) parentDto)
					.getPaymentHeader();
			if (paymentHeader == null) {
				paymentHeader = new ArrayList<PaymentHeaderDTO>();
			}
			paymentHeader.add(paymentHeaderDTO);
			((PaymentFileHeaderDTO) parentDto).setPaymentHeader(paymentHeader);
		}
	}

	/**
	 * Returns target Method .
	 * 
	 * @param dtoToProcessParam
	 *            Input DisbursementDTO.
	 * @return targetMethod.
	 */
	private String getReqMethodTarget(DisbursementDTO dtoToProcessParam) {
		String targetMethod = null;
		if (dtoToProcessParam.isManualCheckIndicator()) {
			targetMethod = "Manual";
		} else if (dtoToProcessParam.isOnDemand()) {
			targetMethod = "OnDemand";
		} else {
			targetMethod = "schedule";
		}
		return targetMethod;
	}
	
	/**
	 * @return the claimFinancialsDAO
	 */
	public ClaimFinancialsDAO getClaimFinancialsDAO() {
		return claimFinancialsDAO;
	}

	/**
	 * @param value
	 *            the claimFinancialsDAO to set
	 */
	@Inject(DaoInterface = "claims.daointerface.claimFinancialsDao")
	public void setClaimFinancialsDAO(ClaimFinancialsDAO value) {
		this.claimFinancialsDAO = value;
	}
	
	/**
	 * Getter for EnterpriseConfigService instance.
	 * 
	 * @return the EnterpriseConfigService implementation
	 */
	protected EnterpriseConfigService getEnterpriseConfigService() {

		return enterpriseConfigService;
	}
	
	/**
	 * Sets the enterpriseConfigService.
	 * @param value the enterpriseConfigService to set
	 */
	@Inject(PojoRef="entconfig.iipservice.entconfigService")
	public void setEnterpriseConfigService(
			EnterpriseConfigService value) {
		this.enterpriseConfigService = value;
	}
	


}
