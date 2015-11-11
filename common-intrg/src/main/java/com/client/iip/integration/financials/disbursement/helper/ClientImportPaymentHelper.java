package com.client.iip.integration.financials.disbursement.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hdpagination.web.validate.ValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementService;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementStatusDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.helper.RecordIdComparator;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportDetailDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportSummaryDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.helper.ImportDisbursementCriteria;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.helper.ImportDisbursementFactory;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.helper.ImportPaymentHelper;
import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.data.rdbms.hibernate.JPALogicalDataSource;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DTOUtils;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.entconfig.batch.BatchLogConstants;
import com.stoneriver.iip.entconfig.batch.BatchLogDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogDetailDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogService;
import com.stoneriver.iip.entconfig.date.BusinessDateType;
import com.stoneriver.iip.entconfig.date.DateService;

/**
 * Helper class to import payment from external system.
 * 
 */
@Pojo(id = "client.financials.disbursement.pojo.importPaymentPojo")
public class ClientImportPaymentHelper extends ImportPaymentHelper {
	
	private ImportDisbursementFactory importDisbursementFactory;
	private final Logger logger = LoggerFactory.getLogger(ClientImportPaymentHelper.class);
	private LogicalDataSource lds;
	private BatchLogService batchLogService;
	
	
	/**
	 * The method will receive Disbursement status updates from an external payment processor  
	 * and processes the status update based on the valid status transitions for disbursement.
	 * 
	 * @param dtos collection of DisbursementImportSummaryDTOs
	 * @return collection of processed DisbursementImportSummaryDTOs
	 */
	public Collection<DisbursementImportSummaryDTO> process(Collection<DisbursementImportSummaryDTO> dtos) {

		for(DisbursementImportSummaryDTO summaryDTO : dtos) {
			
			for(DisbursementImportDetailDTO detailDTO : summaryDTO.getDisbursementImportDetails()) {
				logger.info("Processing Import disbursement : " + detailDTO.getDsbId());
				DisbursementDTO disbursementDTO = getDisbursement(detailDTO);
				if (disbursementDTO == null) {
					continue;
				}
				// set new state and status to currentDisbursementStaus record
				// currently new state and status are static as no data base mapping for record type is there as discussed with POs
				getImportDisbursementFactory().getImportPojos().get(
						detailDTO.getRecordType()).updateDisbursementInformation(disbursementDTO, detailDTO);
				
				// updates the disbursement status.
				updateDisbursementStatus(detailDTO, disbursementDTO);
			}
		}
		
		return dtos;
	}

	/**
	 * Fetch disbursement for information provided by external system.
	 * @param detailDTO DisbursementImportDetailDTO
	 * @return DisbursementDTO
	 */
	public DisbursementDTO getDisbursement(DisbursementImportDetailDTO detailDTO) {
		Long dsbId = detailDTO.getDsbId();
		if(dsbId == null) {
			// get disbursement from bank account, disbursement number and amount
			ImportDisbursementCriteria criteria = new ImportDisbursementCriteria();
			criteria.setDsbNo(detailDTO.getDisbursementNumber());
			criteria.setAmount(detailDTO.getDisbursementAmount());
			criteria.setPayorBankAcctNumber(detailDTO.getPayerAccountNumber());
			criteria.setPayerRoutingNumber(detailDTO.getPayerRoutingNumber());
			Collection<Long> dsbIds = getDisbursementDAO().retrieveDisbursementId(criteria);
			// if more than one record or not a single record is there log error
			if(dsbIds == null || dsbIds.size() != 1) {
				String errorMessage = null;
				detailDTO.setStatusUpdateInd(false);
				if (CollectionUtils.isEmpty(dsbIds)) {
					errorMessage = "No disbursement records found for given criteria";
				} else {
					errorMessage = "More than one disbursement found for given criteria.  Size of list: " + dsbIds.size();
				}
				detailDTO.setComment(errorMessage);
				writeIntoBatchLog(errorMessage + " dsbIds : " + dsbIds==null?"null":dsbIds.toString());
				logger.info("Validation Error in ClientImportPaymentHelper.getDisbursement() {} ", errorMessage);
				return null;
			}
			dsbId = dsbIds.iterator().next();
		} 
		
		/*
		 * retrieveDisbursement(dsbId) fetches profile statements for the disbursment and is resulting in hibernate flushing heavy when update is applied on the DTO.
		 * This degrades performance on high volume. Switching to light weight API.
		 */
		//DisbursementDTO disbursementDTO = MuleServiceFactory.getService(DisbursementService.class).retrieveDisbursement(dsbId);
		
		DisbursementDTO disbursementDTO = retrieveDisbursementForDisbursementId(dsbId);
		
		if(!validDisbursement(detailDTO, disbursementDTO)) {
			detailDTO.setStatusUpdateInd(false);
			return null;
		}
		return disbursementDTO;
	}
	
	private DisbursementDTO retrieveDisbursementForDisbursementId(Long disbursementId) {
		try{
			DisbursementBO disbBO =  (DisbursementBO)((JPALogicalDataSource)lds).getEntityManager().createQuery(
					"SELECT disbursement FROM DisbursementBO AS disbursement where disbursement.recordId = :disbursementId ")
					.setParameter("disbursementId", disbursementId).getSingleResult();
		
			DisbursementDTO disbursementDTO = DTOUtils.retrieveDTOHierarchy(disbBO, DisbursementDTO.class);
			displayStatus(disbursementDTO);
			return disbursementDTO;
		}catch(Exception ex){
			logger.info("No disbursement records found for given disbursement id = " + disbursementId, ex.getMessage());
			return null;			
		}
	}	
	
	/**
	 * This method sets the current status and history status
	 * for the disbursement status collection which can be used
	 * for UI side.
	 * @param disbursementDTO DisbursementDTO
	 */
	private void displayStatus(DisbursementDTO disbursementDTO) {
		// The following is used for UI purpose
		Collection<DisbursementStatusDTO> historyStatuses = new ArrayList <DisbursementStatusDTO>();
		DisbursementStatusDTO currentStatus = null;
		for(DisbursementStatusDTO disbursementStatusDTO: disbursementDTO.getDisbursementStatuses()) {
			if(disbursementStatusDTO.getRecordId() != null
					&& disbursementStatusDTO.getEndDateTime() == null) {
				currentStatus = disbursementStatusDTO;
			} else {
				historyStatuses.add(disbursementStatusDTO);
			}
		}
		// sort history collection by end date
		List<DisbursementStatusDTO> sortedlist = new ArrayList<DisbursementStatusDTO>();
		for(DisbursementStatusDTO disbursementStatusDTO: historyStatuses) {
			sortedlist.add(disbursementStatusDTO);
		}
		Collections.sort(sortedlist,  new RecordIdComparator());
		disbursementDTO.setCurrentStatus(currentStatus);
		disbursementDTO.setHistoryStatuses(sortedlist);
	}

	/**
	 * Updates the disbursement status.
	 * @param detailDTO DisbursementImportDetailDTO
	 * @param disbursementDTO DisbursementDTO
	 */
	private void updateDisbursementStatus(DisbursementImportDetailDTO detailDTO,
			DisbursementDTO disbursementDTO) {
		
		String actionCode = setDisbursementActionCode(disbursementDTO);
		if(actionCode != null) {
			disbursementDTO.setActionCode(actionCode);
			setDefaultReasonCode(disbursementDTO,detailDTO);
			//Setting Import Disbursement Reconciliation as true so that
			//validations are not fired when updating.
			disbursementDTO.setImportDisbursementReconciliation(true);
			try {
				//the saved disbursement has last state and status and above will be change state and status
				MuleServiceFactory.getService(DisbursementService.class).updateDisbursement(disbursementDTO);
				detailDTO.setStatusUpdateInd(true);
				detailDTO.setComment("Status update successful");
			} catch(ValidateException e) {
				logger.info("ValidateException in ClientImportPaymentHelper.updateDisbursementStatus", e);
				detailDTO.setStatusUpdateInd(false);
				detailDTO.setComment(e.getMessage());
				writeIntoBatchLog("dsbId : " + detailDTO.getDsbId() + "  " + e.getMessage());
			}
		} else {
			detailDTO.setStatusUpdateInd(false);
			detailDTO.setComment("Invalid status transitions for disbursement");
			writeIntoBatchLog("dsbId : " + detailDTO.getDsbId() + "  " + "Invalid status transitions for disbursement");			
			logger.info("Invalid status transitions for disbursement");
		}
	}
	
	/**
	 * Set default reason code and comments for action code.
	 * @param disbursementDTO disbursementDTO
	 * @param detailDTO DisbursementImportDetailDTO
	 */
	private void setDefaultReasonCode(DisbursementDTO disbursementDTO,DisbursementImportDetailDTO detailDTO) {
		disbursementDTO.getCurrentStatus().setImportProcess(true);
		if(detailDTO.getDisbursementReasonCode() != null){
			detailDTO.setDibursemrntRequestNumber(disbursementDTO.getDisbursementRequestNumber());
			Collection<Long> configuredReasonCount = getDisbursementDAO().retrieveReasonCodeCount(disbursementDTO.getActionCode(), detailDTO.getDisbursementReasonCode());
			if (configuredReasonCount != null && configuredReasonCount.iterator().next() > 0){
				detailDTO.setValidReasonCode(true);
				disbursementDTO.getCurrentStatus().setDisbursementStatusReason(detailDTO.getDisbursementReasonCode());
			}else{
				detailDTO.setValidReasonCode(false);
				logger.info("Invalid reason for disbursement {}", disbursementDTO.getDisbursementNumber());
			}
		}else{
			Collection<String> reasonCodes = getDisbursementDAO().retrieveDefaultReasonCode(disbursementDTO.getActionCode());
			String reasonCode = reasonCodes.iterator().next();
			disbursementDTO.getCurrentStatus().setDisbursementStatusReason(reasonCode);
		}
	
		if(DisbursementConstants.DISB_STATUS_REASON_CODE_SEE_COMMENT.equals(disbursementDTO.getCurrentStatus().getDisbursementStatusReason())) {
			disbursementDTO.getCurrentStatus().setDisbursementStatusComment("System Reconciliation");
		}
	}
	
	/**
	 * Checks disbursement validity for amount and effective date.
	 * 
	 * @param detailDTO
	 *            DisbursementImportDetailDTO
	 * @param disbursementDTO
	 *            disbursementDTO
	 * @return true if disbursement is valid false otherwise.
	 */
	private boolean validDisbursement(DisbursementImportDetailDTO detailDTO, DisbursementDTO disbursementDTO) {
		
		if (disbursementDTO == null) {
			detailDTO.setComment("Invalid disbursement ID provided in request.");
			writeIntoBatchLog("dsbId : " + detailDTO.getDsbId() + "  " + "Invalid disbursement ID provided in request");			
			return false;
		}
		
		boolean isValidDisbursement = true;

		if (detailDTO.getDisbursementAmount().compareTo(disbursementDTO.getDisbursementAmount()) != 0) {
			logger.info("Invalid disbursement amount specified in import request.  Expected amount: {} ", disbursementDTO.getDisbursementAmount());
			detailDTO.setComment("Invalid disbursement amount specified in import request.  Expected amount: " + disbursementDTO.getDisbursementAmount());
			writeIntoBatchLog("dsbId : " + detailDTO.getDsbId() + "  " + "Invalid disbursement amount specified in import request.  Expected amount: " + disbursementDTO.getDisbursementAmount());			
			isValidDisbursement = false;
		}
		
		/* TD 33060 -If the date is null then use the System Date - The system date logic is available in the Respective Transaction POJO's		
		
		if (DisbursementConstants.RECORD_TYPE_ISSUED.equals(detailDTO.getRecordType())
				&& (detailDTO.getIssuedDate() == null)) {
			errorMessage = "Issued date required for import request";
			logger.info(errorMessage);
			detailDTO.setComment(errorMessage);
			isValidDisbursement = false;
		}

		if (DisbursementConstants.RECORD_TYPE_STOP.equals(detailDTO.getRecordType())
				&& (detailDTO.getStopPaymentDate() == null)) {
			errorMessage = "Stop payment date required for import request";
			logger.info(errorMessage);
			detailDTO.setComment(errorMessage);
			isValidDisbursement = false;

		}

		if (DisbursementConstants.RECORD_TYPE_VOID.equals(detailDTO.getRecordType())
				&& (detailDTO.getVoidDate() == null)) {
			errorMessage = "Void date required for import request";
			logger.info(errorMessage);
			detailDTO.setComment(errorMessage);
			isValidDisbursement = false;
		}

		if (DisbursementConstants.RECORD_TYPE_PAID.equals(detailDTO.getRecordType())
				&& (detailDTO.getPaidDate() == null)) {
			errorMessage = "Paid date required for import request";
			logger.info(errorMessage);
			detailDTO.setComment(errorMessage);
			isValidDisbursement = false;
		}

		if (DisbursementConstants.RECORD_TYPE_ESC_HEATED.equals(detailDTO.getRecordType())
				&& (detailDTO.getEscheatedDate() == null)) {
			errorMessage = "Escheated date required for import request";
			logger.info(errorMessage);
			detailDTO.setComment(errorMessage);
			isValidDisbursement = false;
		}*/
		
		

		return isValidDisbursement;
	}
	
	/**
	 * writes into batch log with message.
	 * 
	 * @param message
	 *            message to be logged
	 */
	private void writeIntoBatchLog(String message) {
		BatchLogDTO batchLogDTO = new BatchLogDTO();

		batchLogDTO.setBatchLogTypeCode(BatchLogConstants.BATCH_PROCESS_ERROR);
		batchLogDTO
				.setAgreementTypeCode("clm");
		batchLogDTO.setBatchJobTypeCode("AcctsPayableImp");
		batchLogDTO.setDescription(message);
		batchLogDTO.setRunDate(MuleServiceFactory
				.getService(DateService.class).getBusinessDate(1L,
						BusinessDateType.BUSINESS));
		BatchLogDetailDTO batchLogDetailDTO = new BatchLogDetailDTO();
		batchLogDetailDTO.setCreateDate(DateUtility.getSystemDateTime());
		batchLogDetailDTO.setMessage(message);

		if (batchLogDTO.getBatchLogDetail() != null) {
			batchLogDTO.getBatchLogDetail().add(batchLogDetailDTO);
		} else {
			Collection<BatchLogDetailDTO> batchLogDetails = new ArrayList<BatchLogDetailDTO>();
			batchLogDetails.add(batchLogDetailDTO);

			batchLogDTO.setBatchLogDetail(batchLogDetails);
		}
		getBatchLogService().saveBatchLog(batchLogDTO);
	}	
	
	/**
	 * @return the batchLogService
	 */
	public BatchLogService getBatchLogService() {
		if(batchLogService == null) {
			batchLogService = MuleServiceFactory.getService(BatchLogService.class);
		}
		return batchLogService;
	}	

	/**
	 * Getter method for importDisbursementFactory.
	 * @return the importDisbursementFactory
	 */
	public ImportDisbursementFactory getImportDisbursementFactory() {
		return importDisbursementFactory;
	}

	/**
	 * Setter method for importDisbursementFactory.
	 * @param value the importDisbursementFactory to set
	 */
	@Inject(PojoRef = "client.financials.disbursement.pojo.importDisbursementFactory")
	public void setImportDisbursementFactory(ImportDisbursementFactory value) {
		this.importDisbursementFactory = value;
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
	@Inject(PojoRef = "claimsAllLogicalDataSource")
	public void setLds(LogicalDataSource ldsParam) {
		this.lds = ldsParam;
	}	
}

