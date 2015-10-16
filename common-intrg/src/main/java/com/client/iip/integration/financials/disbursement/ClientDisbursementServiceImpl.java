package com.client.iip.integration.financials.disbursement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.client.iip.integration.financials.disbursement.external.processor.ClientPaymentHeaderProcessor;
import com.client.iip.integration.financials.disbursement.helper.ClientGeneralLedgerExportHelper;
import com.client.iip.integration.sa.ClientEnterpriseConfigDAO;
import com.fiserv.isd.iip.bc.financials.FinancialsDAO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementActionType;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBankingInfoDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDAO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusReasonType;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusType;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementUserResult;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentClaimParticipantData;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailClaimDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentDetailItemClaimDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentFileHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentFileTrailerDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentService;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.helper.RecordIdComparator;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportSummaryDTO;
import com.fiserv.isd.iip.bc.financials.generalledger.batch.export.GLExportBatchDTO;
import com.fiserv.isd.iip.bc.financials.generalledger.batch.export.GeneralLedgerExportBatchDTO;
import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.data.rdbms.hibernate.JPALogicalDataSource;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DTOUtils;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.meta.annotation.ServiceMethod;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimDTO;
import com.stoneriver.iip.claims.cws.CALClaimBO;
import com.stoneriver.iip.claims.cws.ClaimsAllRetriever;
import com.stoneriver.iip.claims.financial.ClaimParticipantSearchCriteria;
import com.stoneriver.iip.entconfig.api.CompanyLOBInfo;
import com.stoneriver.iip.entconfig.date.BusinessDateType;
import com.stoneriver.iip.entconfig.date.DateService;

/**
 * AP Adapter Interface Implementation used in AP export and import processing.
 * 
 * @author Todd Beals
 * 
 */
@Service(id = "integration.serviceObject.ClientDisbursementService")
public class ClientDisbursementServiceImpl implements ClientDisbursementService{

	private final Logger logger = LoggerFactory.getLogger(ClientDisbursementServiceImpl.class);
	
	private final static int BATCH_SIZE=1000;

	private ExportPaymentHelper exportPaymentHelper;
	private LogicalDataSource lds;
	private DisbursementDAO disbursementDAO;
	private ClientDisbursementDAO clientDisbursementDAO;
	private FinancialsDAO financialsDAO;
	private PaymentService paymentService;
	private ClientPaymentHeaderProcessor paymentProcessor;
	private CWSClaimService claimService;
	private DateService dateService;
	private ClaimsAllRetriever claimsAllRetriever;
	private ClientEnterpriseConfigDAO clientEnterpriseConfigDAO;
	private ClientGeneralLedgerExportHelper glExportHelper;
	private Collection<DisbursementDTO> exportedDisbursements = null;
	
	/**
	 * Export GL Entries for the financials.
	 * @return Collection<GeneralLedgerExportBatchDTO>
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@ServiceMethod
	public Collection<GeneralLedgerExportBatchDTO> exportGL() {

		Map<String, GeneralLedgerExportBatchDTO> glExportBatchMap = new HashMap<String, GeneralLedgerExportBatchDTO>();
		Collection<GeneralLedgerExportBatchDTO> glExportList = new ArrayList<GeneralLedgerExportBatchDTO>();
		GeneralLedgerExportBatchDTO exportDTO = null;
		Date businessDate = DateUtility.toSQLTimestamp(MuleServiceFactory
				.getService(DateService.class).getBusinessDate(1L,
						BusinessDateType.BUSINESS));
		Collection<GLExportBatchDTO> results = getClientDisbursementDAO().retrieveEntriesForGLExport(businessDate);
		for (GLExportBatchDTO result : results) {
			String key = glExportHelper.getKey(result);

			// Create Object for first iteration
			if (glExportBatchMap.get(key) != null) {
				exportDTO = glExportBatchMap.get(key);
			} else {
				if(businessDate == null){
					businessDate = glExportHelper.retrieveBusinessDate();
				}
				exportDTO = glExportHelper.createGLExportData(result, businessDate);				
			}
			
			// Update Debit/Credit amount and count for GL account
			glExportHelper.updateGLExportData(result, exportDTO);
			glExportBatchMap.put(key, exportDTO);
		}
		
		Iterator it = glExportBatchMap.entrySet().iterator();
		
		while(it.hasNext()){
			 Map.Entry entry = (Map.Entry)it.next();
			 glExportList.add((GeneralLedgerExportBatchDTO)entry.getValue());			
		}

		return glExportList;
	}	
	
	/**
	 * The implementation method that retrieves all of the valid disbursements
	 * and exports the data to the external AP system.
	 * 
	 * @param request
	 *            import request
	 * 
	 * @return result exported disbursements result
	 * 
	 */
	public ClientExportDisbursementsResultDTO export(ClientExportDisbursementsRequestDTO request) {

		ClientExportDisbursementsResultDTO result = null;

		try {
			
			long startTime = System.currentTimeMillis();

			Collection<DisbursementDTO> disbursements = getClientDisbursementDAO().retrieveDisbursementsForExport(
					DateUtility.toSQLTimestamp(request.getStartDate()),
					DateUtility.toSQLTimestamp(request.getEndDate()));

			result = populatePaymentResult(disbursements, true);
			
			double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			
			logger.info("APAdapterComponent exported {} disbursements in {} seconds.", result.getTotalRecordCount(), elapsedSeconds);

		} catch (Throwable e) {
			logError("AP Export", e);
			throw new IIPCoreSystemException("System Error occurred during AP Export.  Please review logs for detailed information.", e);
		}

		return result;

	}

	/**
	 * Re Export All Disbursement for a given effective date range and status
	 * type code.
	 * 
	 * @return list disbursements
	 */
	public ClientExportDisbursementsResultDTO reExport(ClientExportDisbursementsRequestDTO request) {

		ClientExportDisbursementsResultDTO result = null;

		try {

			long startTime = System.currentTimeMillis();
			
			Collection<DisbursementDTO> disbursements = getClientDisbursementDAO().retrieveDisbursementsForReExport(
					DateUtility.toSQLTimestamp(request.getStartDate()),
					DateUtility.toSQLTimestamp(request.getEndDate()));

			result = populatePaymentResult(disbursements, false);
			
			double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			
			logger.info("APAdapterComponent reExported {} disbursements in {} seconds.", result.getTotalRecordCount(), elapsedSeconds);

		} catch (Throwable e) {
			logError("AP Re-Export", e);
			throw new IIPCoreSystemException("System Error occurred during AP Re-Export.  Please review logs for detailed information.", e);
		}

		return result;
	}

	/**
	 * @param e
	 * @return
	 */
	private void logError(String method, Throwable e) {
		String errorMessage = "Error occurred in APAdapterComponent. " + method + " with cause: " + e.toString();
		logger.error(errorMessage);
		e.printStackTrace();
	}
	
	/**
	 * The implementation method that imports disbursement summary details from
	 * an external payment processor.
	 * 
	 * @param disbursementImportSummaryDTOs
	 *            Input collection of summary dtos.
	 * @return collection of processed DisbursementImportSummaryDTOs
	 */
	public Collection<DisbursementImportSummaryDTO> importPayment(
			Collection<DisbursementImportSummaryDTO> disbursementImportSummaryDTOs) {

		long startTime = System.currentTimeMillis();
		
		Collection<DisbursementImportSummaryDTO> importedPayments;
		try {
			
			importedPayments = getPaymentService().importPaymentFromExternalSystem(disbursementImportSummaryDTOs);
			
			double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			
			logger.info("APAdapterComponent import processed {} disbursements in {} seconds.", disbursementImportSummaryDTOs.size(), elapsedSeconds);
			
		} catch (Throwable e) {
			logError("importPayment", e);
			throw new IIPCoreSystemException("System Error occurred during AP Import.  Please review logs for detailed information.", e);
		}
		return importedPayments;

	}

	/**
	 * Retrieves a list of disbursements for a given set of Disbursement IDs.
	 * 
	 * @return list disbursements
	 */
	private Collection<DisbursementDTO> retrieveDisbursements(Collection<DisbursementDTO> disbursements) {
		
		Collection<DisbursementDTO> populatedDisbursements = new ArrayList<DisbursementDTO>(disbursements.size());
		
		List<Long> disbursementIdList = new ArrayList<Long>();
		Iterator<DisbursementDTO> disbursementIter = disbursements.iterator();
		while (disbursementIter.hasNext()) {
			DisbursementDTO nextDisbursement = (DisbursementDTO) disbursementIter.next();
			disbursementIdList.add(nextDisbursement.getRecordId());
			if (disbursementIdList.size() == BATCH_SIZE || !disbursementIter.hasNext()) {
				Collection<DisbursementBO> disbursementRecords = retrieveDisbursementsForDisbursementIds(disbursementIdList);
				
				for (DisbursementBO disbursementBO : disbursementRecords) {
					
					DisbursementDTO disbursementDTO = DTOUtils.retrieveDTOHierarchy(disbursementBO, DisbursementDTO.class);
					
					if (!disbursementDTO.isManualCheckIndicator() && !disbursementDTO.isOnDemand()) {
						disbursementDTO.setScheduled(true);
					}

					// get bank account name on basis of bank account id
					DisbursementBankingInfoDTO bankingInfoDTO = disbursementDTO.getDisbursementBankingInfo();
					if (bankingInfoDTO != null) {
						if (bankingInfoDTO.getCompanyBankAccountId() != null) {
							Collection<String> accountNames = disbursementDAO.retrieveCompanyBankNameById(bankingInfoDTO
									.getCompanyBankAccountId());
							if (accountNames != null && !accountNames.isEmpty()) {
								bankingInfoDTO.setBankAccountNameCompany(accountNames.iterator().next());
							}
						}
						if (bankingInfoDTO.getPartyBankAccountIdPayee() != null) {
							Collection<String> accountNames = disbursementDAO.retrievePayeeBankAccountNameById(bankingInfoDTO
									.getPartyBankAccountIdPayee());
							if (accountNames != null && !accountNames.isEmpty()) {
								bankingInfoDTO.setBankAccountNamePayee(accountNames.iterator().next());
							}
						}
					}
					retrieveActionTransactionTypes(disbursementDTO);
					displayStatus(disbursementDTO);
					setCodeNames(disbursementDTO);

					disbursementDTO.setAuthorizationFlag(false);
		
					populatedDisbursements.add(disbursementDTO);
				}
				disbursementIdList.clear();
			}	
		}
		
		return populatedDisbursements;
	}

	/**
	 * Helper method to create a results object with total record count and
	 * total disbursement amount populating payment details for all records.
	 * 
	 * @param disbursementRecords
	 *            disbursement records from database
	 * @param exportFlag
	 *            if exportFlag == true export, otherwise return results
	 *            read-only
	 * 
	 * 
	 * @return Collection of disbursements processed by system.
	 */
	public ClientExportDisbursementsResultDTO populatePaymentResult(Collection<DisbursementDTO> disbursements,
			boolean exportFlag) throws Throwable {
		
		ClientExportDisbursementsResultDTO result = new ClientExportDisbursementsResultDTO();

		result.setAccountingDate(getDateService().getBusinessDate(1L, BusinessDateType.ACCOUNTING));

		Date businessDate = MuleServiceFactory.getService(DateService.class).getBusinessDate(new Long(-1),
				BusinessDateType.BUSINESS);
		
		result.setExecutionDate(businessDate);
		
		Collection<DisbursementDTO> validDisbursements = filterInvalidDates(disbursements, businessDate);
		logger.info("valid Disbursements after filtering for buinsess date: " + validDisbursements.size());
		if (CollectionUtils.isNotEmpty(validDisbursements)) {

			PaymentFileHeaderDTO paymentFileHeader = retrieveDisbursements(exportFlag, validDisbursements);
			
			if (CollectionUtils.isEmpty(paymentFileHeader.getPaymentHeader())) {
				return result;
			}
					
			populateCompanyInfo(paymentFileHeader);

			populateParticipantData(paymentFileHeader);

			PaymentFileTrailerDTO paymentFileTrailer = paymentFileHeader.getPaymentFileTrailer();
			if(paymentFileTrailer != null){
				result.setTotalDisbursementAmount(paymentFileTrailer.getSummaryAmount()==null?BigDecimal.ZERO:paymentFileTrailer.getSummaryAmount());
				result.setTotalRecordCount(paymentFileTrailer.getItemCount()==null?new Long(0):paymentFileTrailer.getItemCount());
			}

			Collection<PaymentHeaderDTO> paymentHeaders = paymentFileHeader.getPaymentHeader();
			formatClaimDetailsNames(paymentHeaders);
			filterUnusedParticipants(paymentHeaders);
			result.setPaymentList(paymentHeaders);
			
			populateAccountingDates(paymentFileHeader);
			
			if(exportFlag){
				if(exportedDisbursements != null){
					getClientExportPaymentHelper().postProcess(exportedDisbursements);
				}
			}

		}

		return result;

	}
	
	/**
	 * @param exportFlag
	 * @param validDisbursements
	 * @return
	 */
	private PaymentFileHeaderDTO retrieveDisbursements(boolean exportFlag,
			Collection<DisbursementDTO> validDisbursements) throws Throwable {
	
		Collection<DisbursementDTO> populatedDisbursements = retrieveDisbursements(validDisbursements);
		for (DisbursementDTO populatedDisbursement : populatedDisbursements) {
			for (DisbursementDTO disbursementDTO : validDisbursements) {
				if (populatedDisbursement.getRecordId().equals(disbursementDTO.getRecordId())) {
					copyExportedProperties(disbursementDTO, populatedDisbursement);
					break;
				}
			}
		}

		if (exportFlag) {
			populatedDisbursements = getPaymentService().exportPaymentToExternalSystem(populatedDisbursements);
		}

		PaymentFileHeaderDTO paymentFileHeader = getExportPaymentHelper().getFileHeaderProcessor()
				.populateStagingData(populatedDisbursements);
		this.exportedDisbursements = populatedDisbursements;
		
		return paymentFileHeader;
	}

	/**
	 * Copy disbursement properties required for export.
	 * 
	 * @param fromDisbursementDTO
	 *            from disbursement
	 * @param toDisbursementDTO
	 *            to disbursement
	 * 
	 */
	private void copyExportedProperties(DisbursementDTO fromDisbursementDTO, DisbursementDTO toDisbursementDTO) {
		toDisbursementDTO.setFinancialAgreementTypeCode(fromDisbursementDTO.getFinancialAgreementTypeCode());
		toDisbursementDTO.setRecordId(fromDisbursementDTO.getRecordId());
		toDisbursementDTO.setCompanyId(fromDisbursementDTO.getCompanyId());
		toDisbursementDTO.setCompanyOrganizationUnitId(fromDisbursementDTO.getCompanyOrganizationUnitId());
		toDisbursementDTO.setFinancialAgreementTypeCode(fromDisbursementDTO.getFinancialAgreementTypeCode());
		toDisbursementDTO.setDisbursementMethodPaymentCode(fromDisbursementDTO.getDisbursementMethodPaymentCode());
		toDisbursementDTO.setDisbursementTypeCode(fromDisbursementDTO.getDisbursementTypeCode());
		toDisbursementDTO.setDisbursementPrintGroupCode(fromDisbursementDTO.getDisbursementPrintGroupCode());
		toDisbursementDTO.setDisbursementInPaymentOfCode(fromDisbursementDTO.getDisbursementInPaymentOfCode());
		toDisbursementDTO.setDisbursementInPaymentOfName(fromDisbursementDTO.getDisbursementInPaymentOfName());
		toDisbursementDTO.setDisbursementRequestNumber(fromDisbursementDTO.getDisbursementRequestNumber());
		toDisbursementDTO.setDisbursementNumber(fromDisbursementDTO.getDisbursementNumber());
		toDisbursementDTO.setDisbursementCreationDate(fromDisbursementDTO.getDisbursementCreationDate());
		toDisbursementDTO.setDisbursementAmount(fromDisbursementDTO.getDisbursementAmount());
		toDisbursementDTO.setComment(fromDisbursementDTO.getComment());
		toDisbursementDTO.setStateCode(fromDisbursementDTO.getStateCode());
		toDisbursementDTO.setStatusCode(fromDisbursementDTO.getStatusCode());
		toDisbursementDTO.setStatusReasonCode(fromDisbursementDTO.getStatusReasonCode());
		toDisbursementDTO.setManualCheckIndicator(fromDisbursementDTO.isManualCheckIndicator());
		toDisbursementDTO.setOnDemand(fromDisbursementDTO.isOnDemand());
		toDisbursementDTO.setEffectiveDate(fromDisbursementDTO.getEffectiveDate());
		toDisbursementDTO.setEndDate(fromDisbursementDTO.getEndDate());
	}

	/**
	 * Helper method to populate the participant data associated with the
	 * disbursement.
	 * 
	 * @param disbursementRecords
	 *            disbursement records from database
	 * 
	 * @return Collection of disbursements processed by system.
	 * 
	 */
	private void populateParticipantData(PaymentFileHeaderDTO paymentFileHeader) {

		Collection<PaymentHeaderDTO> payments = paymentFileHeader.getPaymentHeader();
		if (CollectionUtils.isNotEmpty(payments)) {

			// List<PaymentClaimParticipantData> allParticipants = new
			// ArrayList<PaymentClaimParticipantData>();
			for (PaymentHeaderDTO paymentHeaderDTO : payments) {
				Collection<PaymentDetailHeaderDTO> paymentDetails = paymentHeaderDTO.getPaymentDetailHeader();

				for (PaymentDetailHeaderDTO paymentDetailHeaderDTO : paymentDetails) {
					Collection<PaymentDetailClaimDTO> paymentDetailClaims = paymentDetailHeaderDTO
							.getPaymentDetailClaim();
					ArrayList<PaymentDetailClaimDTO> newPaymentDetailClaims = new ArrayList<PaymentDetailClaimDTO>();

					for (PaymentDetailClaimDTO paymentDetailClaim : paymentDetailClaims) {

						try {

							ClientPaymentDetailClaimDTO newPaymentDetailClaim = new ClientPaymentDetailClaimDTO();
							BeanUtils.copyProperties(newPaymentDetailClaim, paymentDetailClaim);

							Collection<PaymentClaimParticipantData> claimParticipantData = null;
							ClaimParticipantSearchCriteria criteria = new ClaimParticipantSearchCriteria();
							criteria.setClaimId(paymentDetailClaim.getClaimId());
							criteria.setLineOfBusiness(paymentDetailClaim.getLineOfBusiness());
							claimParticipantData = getClientDisbursementDAO().retrieveClaimParticipantData(criteria);

							HashMap<String, PaymentClaimParticipantData> participantMap = new HashMap<String, PaymentClaimParticipantData>();
							for (PaymentClaimParticipantData paymentClaimParticipantData : claimParticipantData) {
								String key = paymentClaimParticipantData.getParticipantName() + ":"
										+ paymentClaimParticipantData.getParticipationType();
								if (!participantMap.containsKey(key)) {
									participantMap.put(key, paymentClaimParticipantData);
								}
							}

							List<PaymentClaimParticipantData> claimParticipants = new ArrayList<PaymentClaimParticipantData>(
									participantMap.values());
							newPaymentDetailClaim.setClaimParticipant(claimParticipants);

							newPaymentDetailClaims.add(newPaymentDetailClaim);
						} catch (Throwable e) {
							logError("populateParticipantData", e);
							logger.error("populateParticipantData with Claim ID {} and LOB {} ", paymentDetailClaim.getClaimId(), paymentDetailClaim.getLineOfBusiness());
						}
					}
					paymentDetailHeaderDTO.setPaymentDetailClaim(newPaymentDetailClaims);
				}

			}

		}
	}
	
	/**
	 * 
	 * Helper method to populate the accounting dates associated with a payment.
	 * 
	 */
	private void populateAccountingDates(PaymentFileHeaderDTO paymentFileHeader) {
		try{
		Collection<PaymentHeaderDTO> payments = paymentFileHeader.getPaymentHeader();
		if (CollectionUtils.isNotEmpty(payments)) {

			for (PaymentHeaderDTO paymentHeaderDTO : payments) {
				Collection<PaymentDetailHeaderDTO> paymentDetails = paymentHeaderDTO.getPaymentDetailHeader();

				for (PaymentDetailHeaderDTO paymentDetailHeaderDTO : paymentDetails) {

					Collection<PaymentDetailClaimDTO> paymentDetailClaims = paymentDetailHeaderDTO
							.getPaymentDetailClaim();

					for (PaymentDetailClaimDTO paymentDetailClaim : paymentDetailClaims) {
						
						logger.info("paymentDetailClaimId: " + paymentDetailClaim.getClaimId());
						Collection<PaymentDetailItemClaimDTO> paymentDetailItemClaims = paymentDetailClaim
								.getPaymentDetailItemClaim();
						//TD36979 12/13/2013 @GR - Check for Null to avoid NPE on paymentDetailItemClaimList
						if(paymentDetailItemClaims != null){
						for (PaymentDetailItemClaimDTO paymentDetailItemClaim : paymentDetailItemClaims) {
							ClientPaymentHeaderDTO clientPaymentHeader = (ClientPaymentHeaderDTO) paymentHeaderDTO;
							clientPaymentHeader.setAccountingBusinessDate(getFinancialsDAO()
									.retrieveClaimFinancialTransaction(paymentDetailItemClaim.getFtcId())
									.getAccountingBusinessDate());
							break;
						}
					  }else{
						  logger.error("No payment detail item found for claim {} Amount {} ", paymentDetailClaim.getClaimId(), paymentDetailClaim.getClaimPaymentAmount());
					  }
					}

				}

			}

		  }
		}catch(Throwable e){
			logError("populateAccountingDates", e);
		}
	}

	private void formatClaimDetailsNames(Collection<PaymentHeaderDTO> paymentHeaderList) {

		try {

			for (PaymentHeaderDTO paymentHeader : paymentHeaderList) {
				Collection<PaymentDetailHeaderDTO> paymentDetailList = paymentHeader.getPaymentDetailHeader();

				for (PaymentDetailHeaderDTO paymentDetail : paymentDetailList) {
					Collection<PaymentDetailClaimDTO> paymentDetailClaimList = paymentDetail.getPaymentDetailClaim();

					for (PaymentDetailClaimDTO paymentDetailClaim : paymentDetailClaimList) {
						ClientPaymentDetailClaimDTO clientDetailClaim = (ClientPaymentDetailClaimDTO) paymentDetailClaim;
						String participantName = paymentDetailClaim.getParticipantName();
						String formattedName = formatParticipantName(participantName,
								clientDetailClaim.getClaimParticipant());
						if (!formattedName.isEmpty()) {
							paymentDetailClaim.setParticipantName(formattedName);
						}

						Collection<PaymentDetailItemClaimDTO> paymentDetailItemClaimList = paymentDetailClaim
								.getPaymentDetailItemClaim();
						logger.info("paymentDetailClaimId: " + paymentDetailClaim.getClaimId());
						//TD36979 12/13/2013 @GR - Check for Null to avoid NPE on paymentDetailItemClaimList
						if(paymentDetailItemClaimList != null){
						for (PaymentDetailItemClaimDTO paymentDetailItemClaim : paymentDetailItemClaimList) {
							String unitParticipantName = paymentDetailItemClaim.getUnitParticipantName();
							String unitParticipantFormattedName = formatParticipantName(unitParticipantName,
									clientDetailClaim.getClaimParticipant());
							if (!unitParticipantFormattedName.isEmpty()) {
								paymentDetailItemClaim.setUnitParticipantName(unitParticipantFormattedName);
							}
						  }
						}else{
							  logger.error("No payment detail item found for claim {} Amount {} " , paymentDetailClaim.getClaimId(), paymentDetailClaim.getClaimPaymentAmount());
						  }

					}
				}
			}
		} catch (Throwable e) {
			logError("formatClaimDetailsNames", e);
		}

	}
		
	private String formatParticipantName(String participantName, Collection<PaymentClaimParticipantData> claimParticipantList) throws Throwable {
		for (PaymentClaimParticipantData claimParticipant : claimParticipantList) {
			String lastNameFirst = formatLastNameFirst(claimParticipant.getParticipantName());
			if (lastNameFirst.trim().equals(participantName)) {
				return claimParticipant.getParticipantName();
			}
		}
		return StringUtils.EMPTY;
	}
	
	private void filterUnusedParticipants(Collection<PaymentHeaderDTO> paymentHeaderList) {

		try {

			for (PaymentHeaderDTO paymentHeader : paymentHeaderList) {
				Collection<PaymentDetailHeaderDTO> paymentDetailList = paymentHeader.getPaymentDetailHeader();

				for (PaymentDetailHeaderDTO paymentDetail : paymentDetailList) {
					Collection<PaymentDetailClaimDTO> paymentDetailClaimList = paymentDetail.getPaymentDetailClaim();

					for (PaymentDetailClaimDTO paymentDetailClaim : paymentDetailClaimList) {
						ClientPaymentDetailClaimDTO clientDetailClaim = (ClientPaymentDetailClaimDTO) paymentDetailClaim;

						Collection<PaymentClaimParticipantData> updatedClaimParticipantList = selectRequiredParticipants(clientDetailClaim
								.getClaimParticipant());
						clientDetailClaim.setClaimParticipant(updatedClaimParticipantList);

					}
				}
			}
		} catch (Throwable e) {
			logError("filterUnusedParticipants", e);
		}

	}

	private Collection<PaymentClaimParticipantData> selectRequiredParticipants(
			Collection<PaymentClaimParticipantData> claimParticipantList) {

		Collection<PaymentClaimParticipantData> updatedClaimParticipantList = new ArrayList<PaymentClaimParticipantData>();

		try {
			for (PaymentClaimParticipantData claimParticipant : claimParticipantList) {
				if (claimParticipant.getParticipationType().equals("insrd")) {
					updatedClaimParticipantList.add(claimParticipant);
				}
			}

		} catch (Throwable e) {
			logError("selectRequiredParticipants", e);
		}

		return updatedClaimParticipantList;

	}
	
	private String formatLastNameFirst(String correctName) throws Throwable {
		String[] splittedName = StringUtils.split(correctName);
		String lastNameFirst = StringUtils.EMPTY;
		if (splittedName.length == 2) {
			lastNameFirst = splittedName[1] + ", " + splittedName[0];
		} else if (splittedName.length == 3) {
			lastNameFirst = splittedName[2] + ", " + splittedName[0] + " " + splittedName[1];
		}
		return lastNameFirst;
	}
	


	/**
	 * Helper method to populate the participant data associated with the
	 * disbursement.
	 * 
	 * @param disbursementRecords
	 *            disbursement records from database
	 * 
	 * @return Collection of disbursements processed by system.
	 * 
	 */
	private void populateCompanyInfo(PaymentFileHeaderDTO paymentFileHeader) {

		Collection<PaymentHeaderDTO> payments = paymentFileHeader.getPaymentHeader();
		if (CollectionUtils.isNotEmpty(payments)) {

			for (PaymentHeaderDTO paymentHeaderDTO : payments) {
				Collection<PaymentDetailHeaderDTO> paymentDetails = paymentHeaderDTO.getPaymentDetailHeader();

				for (PaymentDetailHeaderDTO paymentDetailHeaderDTO : paymentDetails) {
					
					long claimId = -1;
					try {

						Collection<PaymentDetailClaimDTO> paymentDetailClaims = paymentDetailHeaderDTO
								.getPaymentDetailClaim();
						for (PaymentDetailClaimDTO paymentDetailClaimDTO : paymentDetailClaims) {

							claimId = paymentDetailClaimDTO.getClaimId();
							ClaimDTO claimDTO = ClaimDTO.class.cast(getClaimsService().retrieveDTOOfClaim(
									paymentDetailClaimDTO.getClaimId(), ClaimDTO.class));

							paymentDetailClaimDTO.setCompanyLobId(claimDTO.getCompanyLOBId());
							paymentDetailClaimDTO.setClaimType(claimDTO.getClaimTypeCode());
						}
						populateCompanyLOBInfo(paymentDetailClaims);
					} catch (Throwable e) {
						logError("populateCompanyInfo", e);
						logger.error("populateCompanyInfo with Claim ID {} ", claimId);
					}

				}

			}
		}
	}

	/**
	 * Helper method to populate the company LOB data associated with the disbursement.
	 * 
	 * @param disbursementRecords
	 *            disbursement records from database
	 * 
	 * @return Collection of disbursements processed by system.
	 * 
	 */
	private void populateCompanyLOBInfo(Collection<PaymentDetailClaimDTO> paymentDetailClaims) {

		Collection<CompanyLOBInfo> companyLOBInfoList = new ArrayList<CompanyLOBInfo>();

		try {

			List<Long> companyLobIdList = new ArrayList<Long>();
			Iterator<PaymentDetailClaimDTO> paymentDetailIter = paymentDetailClaims.iterator();
			while (paymentDetailIter.hasNext()) {
				PaymentDetailClaimDTO nextPaymentDetail = (PaymentDetailClaimDTO) paymentDetailIter.next();
				companyLobIdList.add(nextPaymentDetail.getCompanyLobId());
				if (companyLobIdList.size() == BATCH_SIZE || !paymentDetailIter.hasNext()) {
					companyLOBInfoList.addAll(clientEnterpriseConfigDAO.retrieveCompanyLOBForCompanies(companyLobIdList));
				}
			}

			for (PaymentDetailClaimDTO paymentDetailClaimDTO : paymentDetailClaims) {
				for (CompanyLOBInfo companyLOBInfo : companyLOBInfoList) {
					if (paymentDetailClaimDTO.getCompanyLobId().equals(companyLOBInfo.getLobId())) {
						paymentDetailClaimDTO.setLineOfBusiness(companyLOBInfo.getLobCode());
						break;
					}
				}
			}
		} catch (Throwable e) {
			logError("populateCompanyLOBInfo", e);

		}

	}

	/**
	 * This method prepare collection of DisbursementActionType. In collection
	 * of DisbursementActionType only one record will be added for state, status
	 * and action code combination. For undo clear payment action immediately
	 * preceding record on which clear payment action is performed is added.
	 * 
	 * @param disbursmentDTO
	 *            DisbursementDTO
	 */
	private void retrieveActionTransactionTypes(DisbursementDTO disbursmentDTO) {

		try {

			String actionCodePrevious = null;
			Collection<DisbursementStatusReasonType> statusReasonTypes = null;
			Collection<String> allowedStatues = getAllowedStatusForClearPayAction(DisbursementConstants.CLEAR_PAYMENT);
			// current active status record
			DisbursementStatusDTO currentStatusDTO = null;
			// immediately preceding status dto on which clear payment action is
			// performed
			DisbursementStatusDTO disbursementStatusDTO = null;
			// get current record and record latest record on which clear
			// payment
			// action is performed
			Collection<DisbursementStatusDTO> statusCollection = disbursmentDTO.getDisbursementStatuses();
			for (DisbursementStatusDTO statusDTO : statusCollection) {
				if (statusDTO.getEndDateTime() == null) {
					currentStatusDTO = statusDTO;
				} else if (isValidRecordForClearPaymentAction(disbursementStatusDTO, statusDTO, allowedStatues)) {
					// get immediately preceding status dto for which the action
					// Cleared Payment is performed
					disbursementStatusDTO = statusDTO;
				}
			}
			// fetch all records based on current state and status
			if (currentStatusDTO.getDisbursementState() == null || currentStatusDTO.getDisbursementStatusType() == null) {
				IIPCoreSystemException ex = new IIPCoreSystemException();
				ex.setErrorCode("invalidCurrentStateOrStatus");
				throw ex;
			}
			Collection<DisbursementStatusType> disbursementStatusTypes = getDisbursementDAO()
					.retrieveDisbursementInfoByStatusAndState(currentStatusDTO.getDisbursementState(),
							currentStatusDTO.getDisbursementStatusType());
			// iterating over DisbursementStatusType collection to get action
			// code
			// and status reason related to it.
			Collection<DisbursementActionType> actionTypes = new ArrayList<DisbursementActionType>();
			for (DisbursementStatusType disbursementStatusType : disbursementStatusTypes) {

				// logic to add only one entry for undo clear payment
				if (DisbursementConstants.UNDO_CLEAR_PAYMENT.equals(disbursementStatusType.getActionCode())
						&& !disbursementStatusDTO.getDisbursementStatusType().equals(
								disbursementStatusType.getDisbursementStatusCode())) {
					continue;
				}
				// current action code, state and status
				String actionCodeCurrent = disbursementStatusType.getActionCode();
				// Create DisbursementActionType only if the current action
				// code,
				// state and status are not same as previous.
				if (actionCodePrevious == null || !actionCodePrevious.equals(actionCodeCurrent)) {

					DisbursementActionType disbursementActionType = new DisbursementActionType();
					statusReasonTypes = actionTypeDataMapper(disbursementActionType, disbursementStatusType);
					if (DisbursementConstants.UNDO_CLEAR_PAYMENT.equals(disbursementStatusType.getActionCode())) {
						setDataForUndoClearPayment(statusReasonTypes, disbursementStatusDTO, disbursementActionType,
								disbursementStatusTypes);
					}
					actionTypes.add(disbursementActionType);
					actionCodePrevious = actionCodeCurrent;
				} else {
					// add all status reason related to a particular action,
					// state
					// and status
					statusReasonTypeDataMapper(statusReasonTypes, disbursementStatusType);
				}
			}
			disbursmentDTO.setActionTransactionTypes(actionTypes);
		} catch (Throwable e) {
			logError("retrieveActionTransactionTypes", e);
		}

	}

	/**
	 * This method sets status reason code and comment for undo clear payment
	 * action.
	 * 
	 * @param statusReasonTypes
	 *            statusReasonTypes
	 * @param disbursementStatusDTO
	 *            disbursementStatusDTO
	 * @param disbursementActionType
	 *            disbursementActionType
	 * @param disbursementStatusTypes
	 *            disbursementStatusTypes
	 */
	private void setDataForUndoClearPayment(Collection<DisbursementStatusReasonType> statusReasonTypes,
			DisbursementStatusDTO disbursementStatusDTO, DisbursementActionType disbursementActionType,
			Collection<DisbursementStatusType> disbursementStatusTypes) {
		try {
			DisbursementStatusReasonType reasonType = statusReasonTypes.iterator().next();
			reasonType.setStatusReasonCode(disbursementStatusDTO.getDisbursementStatusReason());
			disbursementActionType.setDisbursementStatusComment(disbursementStatusDTO.getDisbursementStatusComment());

			for (DisbursementStatusType disbursementStatusType : disbursementStatusTypes) {
				if (disbursementStatusType.getStatusReasonCode().equals(
						disbursementStatusDTO.getDisbursementStatusReason())) {
					reasonType.setStatusReasonName(disbursementStatusType.getStatusReasonName());
					break;
				}
			}
		} catch (Throwable e) {
			logError("setDataForUndoClearPayment", e);
		}

	}

	/**
	 * This method will prepare DisbursementStatusReasonType details from
	 * DisbursementStatusType.
	 * 
	 * @param disbursementActionType
	 *            DisbursementActionType
	 * @param disbursementStatusType
	 *            DisbursementStatusType
	 * @return collection containing DisbursementStatusReasonType
	 */
	private Collection<DisbursementStatusReasonType> actionTypeDataMapper(
			DisbursementActionType disbursementActionType, DisbursementStatusType disbursementStatusType) {
		disbursementActionType.setActionCode(disbursementStatusType.getActionCode());
		disbursementActionType.setActionName(disbursementStatusType.getActionName());
		disbursementActionType.setDisbursementStateCode(disbursementStatusType.getDisbursementStateCode());
		disbursementActionType.setDisbursementStatusCode(disbursementStatusType.getDisbursementStatusCode());
		disbursementActionType.setDisbursementStateName(disbursementStatusType.getDisbursementStateName());
		disbursementActionType.setDisbursementStatusName(disbursementStatusType.getDisbursementStatusName());
		Collection<DisbursementStatusReasonType> statusReasons = new ArrayList<DisbursementStatusReasonType>();
		statusReasonTypeDataMapper(statusReasons, disbursementStatusType);
		disbursementActionType.setStatusReasons(statusReasons);
		return statusReasons;
	}

	/**
	 * This method will prepare DisbursementStatusReasonType from
	 * disbursementStatusType.
	 * 
	 * @param statusReasons
	 *            collection of DisbursementStatusReasonType
	 * @param disbursementStatusType
	 *            DisbursementStatusType
	 */
	private void statusReasonTypeDataMapper(Collection<DisbursementStatusReasonType> statusReasons,
			DisbursementStatusType disbursementStatusType) {
		DisbursementStatusReasonType statusReasonType = new DisbursementStatusReasonType();
		statusReasonType.setStatusReasonCode(disbursementStatusType.getStatusReasonCode());
		statusReasonType.setStatusReasonName(disbursementStatusType.getStatusReasonName());
		statusReasons.add(statusReasonType);
	}

	/**
	 * Get allowed statuses for action code.
	 * 
	 * @param actionCode
	 *            action code
	 * @return collection of allowed statuses
	 */
	private Collection<String> getAllowedStatusForClearPayAction(String actionCode) {
		Collection<String> allowedStatues = getDisbursementDAO().retrieveDisbursementStatusesByActionCode(actionCode);

		return allowedStatues;
	}

	/**
	 * This method checks for immediately preceding record on which Cleared
	 * Payment action is performed.
	 * 
	 * @param disbursementStatusDTO
	 *            immediately preceding status record on which clear payment
	 *            action is performed.
	 * @param statusDTO
	 *            current record from history.
	 * @param allowedStatues
	 *            allowedStatues
	 * 
	 * @return return true if record is latest one for Cleared Payment action,
	 *         false otherwise.
	 */
	private boolean isValidRecordForClearPaymentAction(DisbursementStatusDTO disbursementStatusDTO,
			DisbursementStatusDTO statusDTO, Collection<String> allowedStatues) {
		if (DisbursementConstants.DISB_STATE_TYPE_CODE_OPEN.equals(statusDTO.getDisbursementState())
				&& allowedStatues.contains(statusDTO.getDisbursementStatusType())) {
			if (disbursementStatusDTO == null || statusDTO.getRecordId() > disbursementStatusDTO.getRecordId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method sets the current status and history status for the
	 * disbursement status collection which can be used for UI side.
	 * 
	 * @param disbursementDTO
	 *            DisbursementDTO
	 */
	private void displayStatus(DisbursementDTO disbursementDTO) {
		// The following is used for UI purpose
		Collection<DisbursementStatusDTO> historyStatuses = new ArrayList<DisbursementStatusDTO>();
		DisbursementStatusDTO currentStatus = null;
		for (DisbursementStatusDTO disbursementStatusDTO : disbursementDTO.getDisbursementStatuses()) {
			if (disbursementStatusDTO.getRecordId() != null && disbursementStatusDTO.getEndDateTime() == null) {
				currentStatus = disbursementStatusDTO;
			} else {
				historyStatuses.add(disbursementStatusDTO);
			}
		}
		
		//Manual check processing only has single record following a different process so set current status for this scenario
		if (disbursementDTO.getDisbursementStatuses().size() == 1) {
			currentStatus = disbursementDTO.getDisbursementStatuses().iterator().next();
		}
		
		// sort history collection by end date
		List<DisbursementStatusDTO> sortedlist = new ArrayList<DisbursementStatusDTO>();
		for (DisbursementStatusDTO disbursementStatusDTO : historyStatuses) {
			sortedlist.add(disbursementStatusDTO);
		}
		Collections.sort(sortedlist, new RecordIdComparator());
		disbursementDTO.setCurrentStatus(currentStatus);
		disbursementDTO.setHistoryStatuses(sortedlist);
	}

	/**
	 * 
	 * @param dto DisbursementDTO
	 */
	private void setCodeNames(DisbursementDTO dto) {
		
		if (dto.getCompanyId() != null) {
			Collection<String> companyCodes = disbursementDAO.retrieveCompanyName(dto.getCompanyId());
			if (companyCodes != null && companyCodes.size() > 0) {
				dto.setCompanyName(companyCodes.iterator().next());
			}
			Collection<String> corpCodes = disbursementDAO.retrieveCorporationName(dto.getCompanyId());
			if (corpCodes != null && corpCodes.size() > 0) {
				dto.setCorporationName(corpCodes.iterator().next());
			}
			if (dto.getUserIdUpdated() != null) {
				dto.setUserName(getUserName(dto.getUserIdUpdated()));
			} else {
				dto.setUserName(getUserName(dto.getUserIdCreated()));
			}
		}
		if (dto.getCompanyOrganizationUnitId() != null) {
			Collection<String> companyOrgUnitNames = disbursementDAO.retriveCompanyOrganizationalUnitName(dto
					.getCompanyOrganizationUnitId());
			if (companyOrgUnitNames != null && companyOrgUnitNames.size() > 0) {
				dto.setCompanyOrganizationUnitName(companyOrgUnitNames.iterator().next());
			}
		}
	}

	/**
	 * Fetch user name on basis of user id.
	 * 
	 * @param userId
	 *            user id
	 * @return user name
	 */
	private String getUserName(Long userId) {
		Collection<DisbursementUserResult> userNames = disbursementDAO.retrieveUserNameByUserId(userId);
		String name = null;
		for (DisbursementUserResult userName : userNames) {
			if (userName.getFirstName() != null) {
				name = userName.getFirstName() + " ";
			}
			if (userName.getMidName() != null) {
				name = name + userName.getMidName() + " ";
			}
			if (userName.getLastName() != null) {
				name = name + userName.getLastName();
			}
			break;
		}
		return name;
	}

	@SuppressWarnings("unchecked")
	public List<CALClaimBO> retrieveClaimsForClaimIds(List<Long> claimIds) {
		return ((JPALogicalDataSource)lds).getEntityManager().createQuery(
	        "SELECT claim FROM CALClaimBO AS claim where claim.recordId IN (:claimIds)")
	        .setParameter("claimIds", claimIds)
	        .getResultList();

	}
		
	@SuppressWarnings("unchecked")
	public List<DisbursementBO> retrieveDisbursementsForDisbursementIds(List<Long> disbursementIds) {
		return ((JPALogicalDataSource)lds).getEntityManager().createQuery(
	        "SELECT disbursement FROM DisbursementBO AS disbursement where disbursement.recordId IN (:disbursementIds)")
	        .setParameter("disbursementIds", disbursementIds)
	        .getResultList();

	}

	private Collection<DisbursementDTO> filterInvalidDates(Collection<DisbursementDTO> dtos, Date businessDate) {
		
		Collection<DisbursementDTO> validDisbursementList = new ArrayList<DisbursementDTO>();
		try {
			if (dtos != null && dtos.size() > 0) {			

				for (DisbursementDTO dto : dtos) {
					if ( (DateUtility.compareDate(dto.getEffectiveDate(),businessDate) <= 0)
							&& (dto.getEndDate() == null || DateUtility.compareDate(dto.getEndDate(),businessDate) >= 0) ) {
						validDisbursementList.add(dto);
					}

				}
			}
		} catch (Throwable e) {
			logError("filterInvalidResults", e);
			return dtos;
		}
		return validDisbursementList;
	}

	
	/**
	 * Getter for lds.
	 * 
	 * @return the lds
	 */
	public LogicalDataSource getLds() {
		return lds;
	}

	/**
	 * Setter for lds.
	 * 
	 * @param ldsParam
	 *            the lds to set
	 */
	@Inject(PojoRef="financialsLogicalDataSource")
	public void setLds(LogicalDataSource ldsParam) {
		this.lds = ldsParam;
	}
	
	/**
	 * @return DisbursementDao
	 */
	public DisbursementDAO getDisbursementDAO() {
		return disbursementDAO;
	}

	/**
	 * @param value
	 *            DisbursementDao
	 */
	@Inject(PojoRef="financials.daointerface.disbursementDAO")
	public void setDisbursementDAO(DisbursementDAO value) {
		this.disbursementDAO = value;
	}

	/**
	 * @return the disbursementDAO
	 */
	public ClientDisbursementDAO getClientDisbursementDAO() {
		return clientDisbursementDAO;
	}

	/**
	 * @param value
	 *            the disbursementDAO to set
	 */
	@Inject(DaoInterface="client.daointerface.clientDisbursementDao")
	public void setClientDisbursementDAO(ClientDisbursementDAO value) {
		this.clientDisbursementDAO = value;
	}

	/**
	 * @return the financialsDAO
	 */
	public FinancialsDAO getFinancialsDAO() {
		return financialsDAO;
	}

	/**
	 * @param financialsDAOParam
	 *            the financialsDAO to set
	 */
	@Inject(DaoInterface="financials.daointerface.financialsDao")
	public void setFinancialsDAO(FinancialsDAO financialsDAOParam) {
		this.financialsDAO = financialsDAOParam;
	}
	
	/**
	 * @return the paymentService
	 */
	public PaymentService getPaymentService() {
		return paymentService;
	}

	/**
	 * @param value
	 *            the paymentService to set
	 */
	@Inject(PojoRef="client.financials.disbursement.serviceobject.PaymentService")
	public void setPaymentService(PaymentService value) {
		this.paymentService = value;
	}

	/**
	 * @return the claimsAllRetriever
	 */
	public ClaimsAllRetriever getClaimsAllRetriever() {
		return claimsAllRetriever;
	}

	/**
	 * @param retriever
	 *            the claimsAllRetriever to set
	 */
	@Inject(PojoRef = "claims.all.pojo.claimsAllRetriever")
	public void setClaimsAllRetriever(ClaimsAllRetriever retriever) {
		this.claimsAllRetriever = retriever;
	}

	/**
	 * Getter for CWSClaimService instance.
	 * 
	 * @return the CWSClaimService implementation
	 */
	private CWSClaimService getClaimsService() {
		if (claimService == null) {
			claimService = MuleServiceFactory.getService(CWSClaimService.class);
		}
		return claimService;
	}

	/**
	 * Getter for DateService instance.
	 * 
	 * @return the DateService implementation
	 */
	private DateService getDateService() {
		if (dateService == null) {
			dateService = MuleServiceFactory.getService(DateService.class);
		}
		return dateService;
	}

	/**
	 * @return the paymentProcessor
	 */
	public ClientPaymentHeaderProcessor getPaymentProcessor() {
		return paymentProcessor;
	}

	/**
	 * @param paymentProcessor
	 *            the paymentProcessor to set
	 */
	@Inject(PojoRef = "financials.disbursement.pojo.clientPaymentHeaderProcessorPojo")
	public void setPaymentProcessor(ClientPaymentHeaderProcessor paymentProcessor) {
		this.paymentProcessor = paymentProcessor;
	}

	/**
	 * Getter for exportPaymentHelper.
	 * 
	 * @return the exportPaymentHelper
	 */
	public ExportPaymentHelper getExportPaymentHelper() {
		return exportPaymentHelper;
	}

	/**
	 * Setter for exportPaymentHelper.
	 * 
	 * @param exportPaymentHelperParam
	 *            the exportPaymentHelper to set
	 */
	@Inject(PojoRef = "financials.disbursement.pojo.exportPaymentPojo")
	public void setExportPaymentHelper(ExportPaymentHelper exportPaymentHelperParam) {
		this.exportPaymentHelper = exportPaymentHelperParam;
	}
	
	/**
	 * Getter for exportPaymentHelper.
	 * 
	 * @return the exportPaymentHelper
	 */
	public ExportPaymentHelper getClientExportPaymentHelper() {
		return exportPaymentHelper;
	}

	/**
	 * Setter for exportPaymentHelper.
	 * 
	 * @param exportPaymentHelperParam
	 *            the exportPaymentHelper to set
	 */
	@Inject(PojoRef = "client.financials.disbursement.pojo.exportPaymentPojo")
	public void setClientExportPaymentHelper(
			ExportPaymentHelper exportPaymentHelperParam) {
		this.exportPaymentHelper = exportPaymentHelperParam;
	}
	
	/**
	 * @return the ClientEnterpriseConfigDAO
	 */
	public ClientEnterpriseConfigDAO getClientEnterpriseConfigDAO() {
		return clientEnterpriseConfigDAO;
	}

	/**
	 * @param _clientEnterpriseConfigDAO
	 * 
	 */
	@Inject(DaoInterface = "client.daointerface.clientEntConfigDao")
	public void setClientEnterpriseConfigDAO(ClientEnterpriseConfigDAO _clientEnterpriseConfigDAO) {
		this.clientEnterpriseConfigDAO = _clientEnterpriseConfigDAO;
	}
	
	/**
	 * @return the glExportHelper
	 */
	public ClientGeneralLedgerExportHelper getGlExportHelper() {
		return glExportHelper;
	}

	/**
	 * @param value the glExportHelper to set
	 */
	@Inject(PojoRef="client.financials.helper.generalledger.generalLedgerExportHelper")
	public void setGlExportHelper(ClientGeneralLedgerExportHelper value) {
		this.glExportHelper = value;
	}	
	
}
