package com.client.iip.integration.financials.disbursement.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.financials.disbursement.DisbursementStatusDTOMapper;
import com.fiserv.isd.iip.bc.financials.FinancialsConstants;
import com.fiserv.isd.iip.bc.financials.FinancialsService;
import com.fiserv.isd.iip.bc.financials.api.FinancialTransactionDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDAO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementPartyDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusBO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.FTPDisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentSequenceGenHelper;
import com.fiserv.isd.iip.bc.party.PartyDAO;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.BankAccountDTO;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyDTO;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyRequestDTO;
import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DTOUtils;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.entconfig.date.BusinessDateType;
import com.stoneriver.iip.entconfig.date.DateService;

/**
 * ClientExportPaymentHelper is a specialized version of ExportPaymentHelper
 * 
 * @see com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentHelper
 * 
 *      ExportPaymentHelper writes disbursement requests to a set of staging
 *      tables with the intent that Stream Clients will read/write data records
 *      from the tables using SQL. The ClientExportPaymentHelper class is
 *      designed in line with a Service Oriented Architecture where the data is
 *      sent/received as a set of services.
 * 
 */
@Pojo(id = "client.financials.disbursement.pojo.exportPaymentPojo")
public class ClientExportPaymentHelper extends ExportPaymentHelper {

	private final Logger logger = LoggerFactory.getLogger(ClientExportPaymentHelper.class);

	private PartyDAO partyDAO;
	
	private LogicalDataSource lds;
	
	private DisbursementDAO disbursementDAO;
	
	/**
	 * The method should be invoked before collection of disbursement DTO has
	 * been saved within staging database.
	 * 
	 * @param dtos
	 *            Input collection of DisbursementDTO
	 * @return collection of DisbursementDTO processed successfully.
	 */
	public Collection<DisbursementDTO> preProcess(
			Collection<DisbursementDTO> dtos) {
		/*
		 * Disbursement State of "Incomplete" and Disbursement
		 * Status of "Incomplete Payment" that have not been sent to Payment
		 * Processor for processing and a Disbursement Date less than or equal
		 * to the Business Date is eligible for processing.
		 */
		
		Collection<DisbursementDTO> validDisbursement = new ArrayList<DisbursementDTO>();
		if (dtos != null && dtos.size() > 0) {
			logger.debug("Process Started. [ClientExportPaymentHelper] Validate disbursement to be exported.");
			Date businessDate = MuleServiceFactory.getService(DateService.class).getBusinessDate(1L, BusinessDateType.BUSINESS);

			for (DisbursementDTO dto : dtos) {
				
				if (DisbursementConstants.DISB_STATE_TYPE_CODE_INCOMPLETE.equalsIgnoreCase(dto.getStateCode())
						&& DisbursementConstants.DISB_STATUS_TYPE_CODE_INCOMPLETE_PAYMENT.equalsIgnoreCase(dto
								.getStatusCode())
						&& (DateUtility.compareDate(dto.getEffectiveDate(), businessDate) <= 0)
						&& (dto.getEndDate() == null || DateUtility.compareDate(dto.getEndDate(), businessDate) >= 0)) {
					validDisbursement.add(dto);

				} else if (DisbursementConstants.DISB_STATE_TYPE_CODE_OPEN
                        .equalsIgnoreCase(dto.getStateCode())
                           && DisbursementConstants.DISB_STATUS_TYPE_CODE_PAYMENT_STOPPED_NOT_REISSUED
                              .equalsIgnoreCase(dto.getStatusCode())
                                   && (DateUtility.compareDate(dto.getEffectiveDate(),businessDate) <= 0)
                                        && (dto.getEndDate() == null || DateUtility
                                             .compareDate(dto.getEndDate(), businessDate) >= 0)) {
                    validDisbursement.add(dto);
				} else if (DisbursementConstants.DISB_STATE_TYPE_CODE_OPEN.equalsIgnoreCase(dto.getStateCode())
                           && DisbursementConstants.DISB_STATUS_TYPE_CODE_ISSUED_PAYMENT.equalsIgnoreCase(dto.getStatusCode())
                           && (dto.getDisbursementStatuses().size() == 1)
                           && (DateUtility.compareDate(dto.getEffectiveDate(),businessDate) <= 0)
                           && (dto.getEndDate() == null || DateUtility.compareDate(dto.getEndDate(), businessDate) >= 0)) {
                    validDisbursement.add(dto);
				} else if (DisbursementConstants.DISB_STATE_TYPE_CODE_COMPLETE.equalsIgnoreCase(dto.getStateCode())
						&& DisbursementConstants.DISB_STATUS_TYPE_CODE_PAYMENT_VOIDED_NOT_REISSUED.equalsIgnoreCase(dto
								.getStatusCode())
						&& (DateUtility.compareDate(dto.getEffectiveDate(), businessDate) <= 0)
						&& (dto.getEndDate() == null || DateUtility.compareDate(dto.getEndDate(), businessDate) >= 0)) {
					validDisbursement.add(dto);
				} else {
					logger.debug("[ClientExportPaymentHelper] Criteria Not Met for Disbursement ID = {} ", dto.getRecordId());
				}

			}
			// Flush all context to re initialize sequence generator.
			ExportPaymentSequenceGenHelper.flushAllContext();
		}
		return validDisbursement;
	}


	/**
	 * The method will override the default processing using a staging tables
	 * implementation. This method builds the PaymentDTO hierarchy without
	 * persisting the data to the staging tables and returns the objects ready
	 * to send to an external client for processing.
	 * 
	 * @param dtos
	 *            Input collection of staging dtos.
	 * 
	 * @return collection of processed DisbursementImportSummaryDTOs
	 * 
	 */
	public Collection<DisbursementDTO> process(Collection<DisbursementDTO> dtos) {

		// Get DTOs only for context type agency and billing
		logger.debug("Before Filtering Valid Disbursements : " + dtos.size());
		Collection<DisbursementDTO> disbursements = fetchValidDisbursements(dtos);
		logger.debug("After Filtering Valid Disbursements : " + disbursements.size());
		return disbursements;
	}
	
	/**
	 * The method should be invoked after collection of disbursement DTO has
	 * been saved within staging database.
	 * 
	 * @param dtos
	 *            Input collection of DisbursementDTO
	 * @return collection of DisbursementDTO processed successfully.
	 */
	public Collection<DisbursementDTO> postProcess(
			Collection<DisbursementDTO> dtos) {

		
		/*
		Collection<DisbursementDTO> disbursements = new ArrayList<DisbursementDTO>();
		
		for (Iterator<DisbursementDTO> iterator = dtos.iterator(); iterator.hasNext();) {
			DisbursementDTO disbursementDTO = (DisbursementDTO) iterator.next();
			if (!isValidIssuedPayment(disbursementDTO)) {
				disbursements.add(disbursementDTO);
			}
		}
		*/
		
		if (CollectionUtils.isNotEmpty(dtos)) {

			// Update EFT Bank Info for Payee
			
			updateBankEFTStatus(dtos);
		
			// Update Disbursement Status
			updateDisbursementStatus(dtos);
			
		}
		return dtos;
	}


	/**
	 * Fetch disbursements only for context types supported currently by
	 * external payment processor.
	 * 
	 * @param dtoParams
	 *            Input collection of DisbursementDTO
	 * 
	 * @return collection of processed DisbursementImportSummaryDTOs
	 */
	private Collection<DisbursementDTO> fetchValidDisbursements(
			Collection<DisbursementDTO> dtoParams) {
		FinancialTransactionDTO financialTransactionDTO = null;
		Collection<FinancialTransactionDTO> ftdtos = null;
		Collection<DisbursementDTO> validDisbursements = new ArrayList<DisbursementDTO>();
		for (DisbursementDTO dtoParam : dtoParams) {
			ftdtos = new ArrayList<FinancialTransactionDTO>();
			// fetching ftps for disbursement
			if (dtoParam.getFinancialAgreementTypeCode().equals(
					FinancialsConstants.FINANCIAL_AGREEMENT_TYPE_PLCY)) {
				Collection<FTPDisbursementDTO> fTPDisbursements = dtoParam.getFtpDisbursements();
				// fetching ftps for context type agency and billing as export
				// flow support only those two types currently.
				for (FTPDisbursementDTO fTPDisbursementDTO : fTPDisbursements) {
					financialTransactionDTO = MuleServiceFactory.getService(
							FinancialsService.class)
							.retrieveFinancialTransaction(
									fTPDisbursementDTO.getFtpId(),
									dtoParam.getFinancialAgreementTypeCode());

					String contextType = financialTransactionDTO
							.getFinancialContext().getContextType();
					getExportResourceManager().setContext(contextType);
					if (FinancialsConstants.AGREEMENT_TYPE_BILLING
							.equals(contextType)
							|| FinancialsConstants.AGREEMENT_TYPE_AGENCY
									.equals(contextType)) {
						ftdtos.add(financialTransactionDTO);
						validDisbursements.add(dtoParam);
					} else {
						// logging info messages if disbursement is not of type
						// billing or agency.
						logger.info("Processor doesn't support for type - {} " +
								"and Context for Disbursement ID = {}", contextType, dtoParam.getRecordId());
					}
				}
				if (!ftdtos.isEmpty()) {
					getExportResourceManager().getDsbFtpMap().put(
							dtoParam.getRecordId(), ftdtos);
				}
			} else if (dtoParam.getFinancialAgreementTypeCode().equals(
					FinancialsConstants.FINANCIAL_AGREEMENT_TYPE_CLAIM)) {
				getExportResourceManager().setContext(
						FinancialsConstants.FINANCIAL_AGREEMENT_TYPE_CLAIM);

				// Collection<PaymentClaimInfo> claimCollection =
				// MuleServiceFactory.getService(ClaimFinancialsProxyService.class).retrieveClaimInfo(dtoParam.getRecordId());
				// paymentClaimCollection = new ArrayList<PaymentClaimInfo>();
				// for (PaymentClaimInfo paymentClaimInfo : claimCollection) {
				// paymentClaimCollection.add(paymentClaimInfo);
				// }
				validDisbursements.add(dtoParam);
				// if (!paymentClaimCollection.isEmpty()){
				// getExportResourceManager().getClaimDsbMap().put(dtoParam.getRecordId(),
				// paymentClaimCollection);
				// }
			}
		}
		return validDisbursements;
	}
	
	/**
	 * Update Disbursement Status.
	 * 
	 * @param disbursements
	 *            DisbursementDTO collection
	 */
	public void updateDisbursementStatus(
			Collection<DisbursementDTO> disbursements) {
		for (DisbursementDTO dto : disbursements) {
			Collection<DisbursementStatusDTO> statusColl = dto.getDisbursementStatuses();
			
		    /* This logic is updating the existing man_pay record, This should be no different from regular status update, expire the existing record
		     * and create a new record for DISB_STATUS_REASON_CODE_SENT_TO_PAYMENT_PROCESSOR
		     * if (updateManualCheckDisbursementStatus(dto)) {
			    continue;
		    }*/
			DisbursementStatusDTO voidedDisbStatus = null;
			for (DisbursementStatusDTO disbursementStatusDTO : statusColl) {
				if (disbursementStatusDTO.getEndDateTime() == null){
					disbursementStatusDTO.setEndDateTime(DateUtility.getSystemDateTime());
				}
				//Check for voids and stop pay no issue statuses
				if(disbursementStatusDTO.getDisbursementStatusType().
						equals(DisbursementConstants.DISB_STATUS_TYPE_CODE_PAYMENT_VOIDED_NOT_REISSUED)
						|| disbursementStatusDTO.getDisbursementStatusType().
						equals(DisbursementConstants.DISB_STATUS_TYPE_CODE_PAYMENT_STOPPED_NOT_REISSUED)){
						voidedDisbStatus = disbursementStatusDTO;
				}
			}
			
			DisbursementStatusDTO currentStatus = new DisbursementStatusDTO();
			
			if(voidedDisbStatus != null){
				//If voided copy the current codes for the new status record.
				currentStatus
				.setDisbursementStatusType(voidedDisbStatus.getDisbursementStatusType());
				currentStatus
				.setDisbursementState(voidedDisbStatus.getDisbursementState());				
			}else if( (dto.isManualCheckIndicator()) ||
					(dto.getFinancialAgreementTypeCode().equals("dsb") &&  dto.getDisbursementTypeCode().equals("prenote"))	) {
				currentStatus.setDisbursementStatusType(DisbursementConstants.DISB_STATUS_TYPE_CODE_ISSUED_PAYMENT);
				currentStatus.setDisbursementState(DisbursementConstants.DISB_STATE_TYPE_CODE_OPEN);
			}else{
				currentStatus
				.setDisbursementStatusType(DisbursementConstants.DISB_STATUS_TYPE_CODE_AWAITING_PAYMENT);
				currentStatus
				.setDisbursementState(DisbursementConstants.DISB_STATE_TYPE_CODE_INCOMPLETE);
			}

			currentStatus.setDisbursementStatusReason(DisbursementConstants.DISB_STATUS_REASON_CODE_SENT_TO_PAYMENT_PROCESSOR);
			currentStatus.setEffectiveDateTime(DateUtility
					.getSystemDateTime());

			/**
			 * AP Export Performance Fix - @GR - 03/30/2014 - Save just the Disbursement Status record.
			 */
			//dto.getDisbursementStatuses().add(currentStatus);
			statusColl.add(currentStatus);
			//DTOUtils.saveEntityWithAssociations(lds, dto);
			/*
			 * Save the DisbursementStatusBO along with all its
			 * associations from the values in DTO
			 */
			// fetch DisbursementBO
			DisbursementBO disbursement = getDisbursementDao().retrieveDisbursement(dto.getRecordId());
			/*
			 * Instantiate the custom mapper for
			 * DisbursementStatusBO
			 */
			DisbursementStatusDTOMapper disbStatusDTOMapper 
				= new DisbursementStatusDTOMapper(disbursement);			
			
			for(DisbursementStatusDTO disbstatus : statusColl){
				DTOUtils.saveEntityWithAssociations(
						lds, disbstatus, DisbursementStatusBO.class, 
						disbStatusDTOMapper, true);
			}
			
		}
	}
	
	private boolean updateManualCheckDisbursementStatus(DisbursementDTO disbursement) {

		boolean updated = false;
		
		if ( (DisbursementConstants.PAY_PAYEE_CHECK.equals(disbursement.getDisbursementMethodPaymentCode())) && (disbursement.isManualCheckIndicator()) ) {
			
			Collection<DisbursementStatusDTO> disbursementStatusList = disbursement.getDisbursementStatuses();
			for (DisbursementStatusDTO disbursementStatus : disbursementStatusList) {
				if (DisbursementConstants.DISB_STATUS_TYPE_CODE_ISSUED_PAYMENT.equals(disbursementStatus.getDisbursementStatusType())
						&& (DisbursementConstants.DISB_STATUS_REASON_CODE_MANUAL_PAYMENT.equals(disbursementStatus.getDisbursementStatusReason())) ) {
					disbursementStatus.setDisbursementStatusReason(DisbursementConstants.DISB_STATUS_REASON_CODE_SENT_TO_PAYMENT_PROCESSOR);
					DTOUtils.saveEntityWithAssociations(lds, disbursement);
					updated = true;
				}
			}
		}
		return updated;
	}


	/**
	 * Update Bank EFT Status.
	 * 
	 * @param disbursements
	 *            DisbursementDTO
	 * 
	 */
	public void updateBankEFTStatus(Collection<DisbursementDTO> disbursements) {
		for (DisbursementDTO disbursementDTOParam : disbursements) {
			DisbursementPartyDTO disbursementPartyDTO = disbursementDTOParam.getDisbursementParty();
			boolean preNoteInd = false;
			/*
			 * Use MinimalPartyPojo to retrieve all other data. Note: context is
			 * for getting address by context, we won't be using the address
			 * data anyway
			 */
			if (disbursementPartyDTO == null) {
				return;
			}
			//12/17/2013 @GR - Performance Tuning - Fetch MinimalParty only for PreNotes
			/*MinimalPartyRequestDTO minimalPartyRequestDTO = new MinimalPartyRequestDTO();
			minimalPartyRequestDTO.setPartyId(disbursementPartyDTO
					.getPartyIdPayeePrimary());
			/*
			 * Need to set something, it's only used for fetching address, which
			 * we don't care about
			 *
			minimalPartyRequestDTO.setContextType("");
			MinimalPartyDTO minParty = MuleServiceFactory.getService(
					PartyService.class).retrieveMinimalParty(
					minimalPartyRequestDTO);
			minParty.setContext(getExportResourceManager().getContext());*/
				Collection<BankAccountDTO> bankAccounts = retrievePartyBankAccountInformation(disbursementPartyDTO.getPartyIdPayeePrimary());
				for (BankAccountDTO bankAccountDTO : bankAccounts) {

					//Trying to avoid multiple saves to the same Party since was producing Party Merge Optimistic Locking Exception
					if ( !DisbursementConstants.EFT_SENT_STAUTS.equals(bankAccountDTO.getEFTStatusCode()) && bankAccountDTO.ispreNoteIndicator()) {
						preNoteInd = true;
						bankAccountDTO
								.setEFTStatusCode(DisbursementConstants.EFT_SENT_STAUTS);
						bankAccountDTO.setEFTStatusEffDate(DateUtility
								.getSystemDateTime());
					}
				}
				if (preNoteInd) {
					MinimalPartyRequestDTO minimalPartyRequestDTO = new MinimalPartyRequestDTO();
					minimalPartyRequestDTO.setPartyId(disbursementPartyDTO
							.getPartyIdPayeePrimary());
					/*
					 * Need to set something, it's only used for fetching address, which
					 * we don't care about
					 */
					minimalPartyRequestDTO.setContextType("");
					MinimalPartyDTO minParty = MuleServiceFactory.getService(
							PartyService.class).retrieveMinimalParty(
							minimalPartyRequestDTO);
					minParty.setBankInformations(bankAccounts);
					MuleServiceFactory.getService(PartyService.class)
							.saveMinimalParty(minParty);
				}
			
		}
	}
	

	public Collection<BankAccountDTO> retrievePartyBankAccountInformation(Long partyId) {
		Collection<BankAccountDTO> bankAccountDTOs = getPartyDAO().retrievePartyBankAccountsInformation(partyId);
		if(bankAccountDTOs != null && !bankAccountDTOs.isEmpty()) {
			return bankAccountDTOs;
		}
		return new ArrayList<BankAccountDTO>();
	}
	
	/**
	 * @return the partyDAO
	 */
	public PartyDAO getPartyDAO() {
		return partyDAO;
	}

	/**
	 * @param value the partyDAO to set
	 */
	@Inject(DaoInterface = "party.daointerface.partyDao")
	public void setPartyDAO(PartyDAO value) {
		this.partyDAO = value;
	}	

	private boolean disbursementAlreadyIssued(DisbursementDTO disbursement) {
		
		boolean issued = false;
		for (DisbursementStatusDTO status : disbursement.getDisbursementStatuses()) {
			if (DisbursementConstants.DISB_STATUS_TYPE_CODE_ISSUED_PAYMENT.equals(status.getDisbursementStatusType()) ) {
				issued = true;
				break;
			}
		}
		return issued;		
	}
	
	/**
	 * @return the lds
	 */
	public LogicalDataSource getLds() {
		return lds;
	}

	/**
	 * @param logicalDS the lds to set
	 */
	@Inject(PojoRef = "claimsAllLogicalDataSource")
	public void setLds(LogicalDataSource logicalDS) {
		this.lds = logicalDS;
	}	
	
	/**
	 * @return DisbursementDao
	 */
	public DisbursementDAO getDisbursementDao() {
		return disbursementDAO;
	}
	/**
	 * @param value DisbursementDao
	 */
	@Inject(DaoRef = "financials.daointerface.disbursementDAO")
	public void setDisbursementDao(DisbursementDAO value) {
		this.disbursementDAO = value;
	}	


}
