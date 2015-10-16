package com.client.iip.integration.claim.imports;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.client.iip.integration.documents.ClientDocumentDTO;
import com.client.iip.integration.documents.ClientDocumentService;
import com.client.iip.integration.party.ClientClaimPolicyPartyImportProcessor;
import com.client.iip.integration.policy.ClientPolicyImportWrapperDTO;
import com.client.iip.integration.sa.ClientEnterpriseConfigDAO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.BankAccountDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.core.data.rdbms.hibernate.JPALogicalDataSource;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DTOUtils;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.bound.BoundValidationException;
import com.fiserv.isd.iip.util.bean.ExternalInterfaceBeanMerge;
import com.stoneriver.iip.assignment.api.AssignmentService;
import com.stoneriver.iip.bill.BillCycleDTO;
import com.stoneriver.iip.bill.lineitem.BillLineItemDTO;
import com.stoneriver.iip.casetool.api.CaseDTO;
import com.stoneriver.iip.casetool.api.CaseService;
import com.stoneriver.iip.claims.CALClaimICDStatusDTO;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimInitiateDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.bill.ClaimBillCycleNCDTO;
import com.stoneriver.iip.claims.bill.ClaimBillDTO;
import com.stoneriver.iip.claims.bill.ClaimBillService;
import com.stoneriver.iip.claims.bill.lineitem.ClaimBillLineItemService;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksBO;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksCompositeDTO;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksDTO;
import com.stoneriver.iip.claims.dao.ClaimUnitDAO;
import com.stoneriver.iip.claims.event.GenericEventDispatcher;
import com.stoneriver.iip.claims.event.GenericEventDispatcher.EventDispatcherHelper;
import com.stoneriver.iip.claims.financial.ClaimMiscTransactionDTO;
import com.stoneriver.iip.claims.financial.ImportUtilPojo;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecovery;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecoveryPayable;
import com.stoneriver.iip.claims.payment.ClaimDisbursementAdditionalPayee;
import com.stoneriver.iip.claims.payment.ClaimPayable;
import com.stoneriver.iip.claims.payment.ClaimPayment;
import com.stoneriver.iip.claims.payment.ClaimPaymentDisbursement;
import com.stoneriver.iip.claims.policy.ClaimPolicyHelper;
import com.stoneriver.iip.claims.policy.ClaimPolicyParticipationDTO;
import com.stoneriver.iip.claims.reserve.CALClaimReserveDTO;
import com.stoneriver.iip.claims.reserve.ClaimReserveConstants;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;
import com.stoneriver.iip.claims.search.ICDCodeSearchCriteria;
import com.stoneriver.iip.claims.status.ClaimStatusConstants;
import com.stoneriver.iip.claims.status.ClaimStatusDTO;
import com.stoneriver.iip.claims.unit.CWSClaimParticipationHelper;
import com.stoneriver.iip.claims.unit.ClaimUnitDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitVehicleDTO;
import com.stoneriver.iip.document.DocumentServiceHelper;
import com.stoneriver.iip.document.DocumentSourceCode;
import com.stoneriver.iip.document.api.InboundDocumentType;
import com.stoneriver.iip.entconfig.api.document.DocumentExternalIndexDTO;
import com.stoneriver.iip.entconfig.contextdata.api.ContextData;
import com.stoneriver.iip.notepad.model.NotepadDTO;
import com.stoneriver.iip.notepad.service.NotepadService;
import com.stoneriver.iip.workflow.api.WorkflowService;

/**
 * Claim Import Component is used for converting the ClaimImportCompositeDTO to ClaimInitiateDTO and calling the API for Claim Import.
 * 
 * @author Saurabh.Bhatnagar
 *
 */
@Service(id = "integration.serviceObject.ClientClaimImportService")
public class ClientClaimImportServiceImpl implements ClientClaimImportService{
	
	private JPALogicalDataSource lds;
	private CWSClaimService claimService = null;
	
	private Logger logger = LoggerFactory.getLogger(ClientClaimImportServiceImpl.class);

	private static final String WORKITEM_LOAD_BALANCING_RULE_CODE_SPECIFIC = "spfc";
	
	//Util class to check whether Unit or Participant is valid and to retrieve 
	//the corresponding record IDs 
	ImportUtilPojo importUtilPojo;

	//Class to merge DTO's when updating an existing record
	private ExternalInterfaceBeanMerge externalInterfaceMerge;
	
	CWSClaimParticipationHelper claimParticipationHelper;
	
	ClaimPolicyHelper claimPolicyHelper;
	
	private GenericEventDispatcher eventDispatcher;

	private ClaimUnitDAO claimUnitDAO;
	
	private DocumentServiceHelper documentServiceHelper;
	
	/**
	 * @return the lds
	 */
	public JPALogicalDataSource getLds() {
		return lds;
	}
	/**
	 * @param logicalDS the lds to set
	 */
	@Inject(PojoRef="claimsAllLogicalDataSource")
	public void setLds(JPALogicalDataSource logicalDS) {
		this.lds = logicalDS;
	}

	/**
	 * util to flush the claims data source.
	 */
	public void flushDS() {
		try {			
			EntityManager em = lds.getEntityManager();
			em.flush();
		} catch (ClassCastException ex) {
			logger.error("Incorrect LDS type - can't flush", ex);
		}
	}

	ClientEnterpriseConfigDAO clientEnterpriseConfigDAO = null;
	/**
	 * @return clientClaimStatusDAO
	 */
	public ClientEnterpriseConfigDAO getClientEnterpriseConfigDAO() {
		return clientEnterpriseConfigDAO;
	}

	/**
	 * @param value
	 *            Client Enterprise Config DAO to set
	 */
	@Inject(DaoInterface = "client.daointerface.clientEntConfigDao")
	public void setClientClaimStatusDAO(ClientEnterpriseConfigDAO value) {
		this.clientEnterpriseConfigDAO = value;
	}
	
	/**
	 * @return documentServiceHelper
	 */
	public DocumentServiceHelper getDocumentServiceHelper() {
		return documentServiceHelper;
	}	
	/*
	 * Set DocumentServiceHelper
	 */
	@Inject(DaoInterface = "document.instschedule.pojo.DocumentServiceHelper")
	public void setDocumentServiceHHelper(DocumentServiceHelper value) {
		this.documentServiceHelper = value;
	}	
	
	/**
	 * @return the claimUnitDAO
	 */
	public ClaimUnitDAO getClaimUnitDAO() {
		return claimUnitDAO;
	}

	/**
	 * @param value
	 *            the claimUnitDAO to set
	 */
	@Inject(DaoInterface = "claimsall.daointerface.claimsAllUnitDao")
	public void setClaimUnitDAO(ClaimUnitDAO value) {
		this.claimUnitDAO = value;
	}	
	/**
	 * set the ExternalInterfaceBeanMerge class.
	 * @param arg the ExternalInterfaceBeanMerge to set
	 */
	@Inject(PojoRef = "com.fiserv.isd.iip.util.dto.SimpleBeanMerge")
	public void setMerger(ExternalInterfaceBeanMerge arg) {
		this.externalInterfaceMerge = arg;
	}
	
	/**
	 * @return Instance of ExternalInterfaceBeanMerge
	 */
	public ExternalInterfaceBeanMerge getExternalInterfaceBeanMerge() {
		return externalInterfaceMerge;
	}

	/**
	 * Getter for CWSClaimService instance.
	 * @return the CWSClaimService implementation
	 */
	private CWSClaimService getClaimsService() {
		if (claimService == null) {
			claimService = MuleServiceFactory.getService(CWSClaimService.class);
		}
		return claimService;
	}	
	
	/**
	 * @param claimImportCompositeDTO Claim Information
	 * @param claimImportHelper Helper class to import Claim and its entities
	 */
	@Transactional
	public void importClaimEntities(ClaimImportCompositeDTO claimImportCompositeDTO, ClaimImportHelper claimImportHelper) {
		ClaimInitiateDTO claimInitiateDTO = null;
		Long claimId = null;
		
		//Indicates whether the claim is imported for the first time or being updated.
		boolean newClaim = false;
		
		ClaimSearchResultDTO claimSearchResultDTO = null;

		//PolicyImportProcessor policyImportProcessor = new PolicyImportProcessor();
		//Map<Long, PartyDTO> partyMap = new HashMap<Long, PartyDTO>();
		//If Claim Number in null in ClaimImportCompositeDTO then Initiate Claim.
		// claim should be searched first and then if clam id is not present then create it other wise use the claim id of searched claim.

		if(claimImportCompositeDTO.getClaimNumber() != null || claimImportCompositeDTO.getClaimExternalSourceId() != null
				|| claimImportCompositeDTO.getClaimId() != null){
			claimSearchResultDTO = claimImportHelper.searchClaim(claimImportCompositeDTO);	
		} else {
			if(claimImportCompositeDTO.getClaimDTO() != null
					&& claimImportCompositeDTO.getClaimDTO().getClaimNumber() != null) {
				claimImportCompositeDTO.setClaimNumber(claimImportCompositeDTO.getClaimDTO().getClaimNumber());
				claimSearchResultDTO = claimImportHelper.searchClaim(claimImportCompositeDTO);
				if(claimSearchResultDTO != null && claimSearchResultDTO.getRecordId() != null) {
					throw new IIPCoreSystemException("Claim is already available in the application. Please pass a new claim number to import");
				}
			}
		}
		
		if(claimSearchResultDTO == null || claimSearchResultDTO.getRecordId() == null) {
			if(claimImportCompositeDTO.getClaimDTO() == null) {
				throw new IIPCoreSystemException("Claim Information is not available. Please pass claim information to import.");
			}

			//Claim is a new one
			newClaim = true;
			
			if(claimImportCompositeDTO.getClaimDTO().getPolicy() == null || 
					claimImportCompositeDTO.getClaimDTO().getPolicy().getPolicyNumber() == null) {
				//TranslatePolicyImportDTO to ClaimPolicyDTO
				if(claimImportCompositeDTO.getPolicy() != null) {
					claimImportCompositeDTO.getClaimDTO().setPolicy(getClaimPolicyHelper().mapClaimsPolicyDTO(claimImportCompositeDTO.getPolicy()));
				}
			} else {
				if(claimImportCompositeDTO.getClaimDTO().getPolicy() != null 
						&& (claimImportCompositeDTO.getClaimDTO().getPolicy().getParticipations() == null
								|| claimImportCompositeDTO.getClaimDTO().getPolicy().getParticipations().isEmpty()) ) {
					
					Collection<ClaimPolicyParticipationDTO> participations = new ArrayList<ClaimPolicyParticipationDTO>();
					if(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyAgencyParticipants() != null) {
						participations.addAll(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyAgencyParticipants());
					}
					if(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyDriverParticipants() != null) {
						participations.addAll(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyDriverParticipants());
					}
					if(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyFinancialInterestParticipants() != null) {
						participations.addAll(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyFinancialInterestParticipants());
					}
					if(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyInsuredParticipants() != null) {
						participations.addAll(claimImportCompositeDTO.getClaimDTO().getPolicy().getClmPolicyInsuredParticipants());
					}
					claimImportCompositeDTO.getClaimDTO().getPolicy().setParticipations(participations);
				}
			}
			
			//Translate ClaimDTO to ClaimInitiateDTO and create Claim
			claimInitiateDTO = claimImportHelper.initiateClaim(
					claimImportCompositeDTO.getClaimDTO());

			claimImportCompositeDTO.setClaimNumber(claimInitiateDTO.getClaimNumber());
			claimImportCompositeDTO.setClaimExternalSourceId(claimInitiateDTO.getExternalSourceId());
			claimId = (Long) claimInitiateDTO.getRecordId();
			claimImportCompositeDTO.getClaimDTO().setRecordId(claimId);
			claimImportCompositeDTO.getClaimDTO().setClaimNumber(claimInitiateDTO.getClaimNumber());
			claimImportCompositeDTO.getClaimDTO().setClaimPolicyNumber(claimInitiateDTO.getPolicyNumber());
			claimImportCompositeDTO.setClaimId(claimId);
		} else {
			claimId = claimSearchResultDTO.getRecordId();
			Long versionId = claimSearchResultDTO.getVersion();
			claimImportCompositeDTO.setClaimId(claimId);
			claimImportCompositeDTO.setClaimNumber(claimSearchResultDTO.getClaimNumber());
			if(claimImportCompositeDTO.getClaimDTO() != null 
					&& claimImportCompositeDTO.getClaimDTO().getRecordId() == null){
				claimImportCompositeDTO.getClaimDTO().setRecordId(claimId);
				claimImportCompositeDTO.getClaimDTO().setVersion(versionId);
			}
			
			claimImportHelper.setLobCode(claimSearchResultDTO.getLineOfBusiness());
			claimImportHelper.companyId = claimSearchResultDTO.getCompanyId();
		}
		
		if(claimId == null) {
			throw new IIPCoreSystemException("Unable to import or find claim. Claim Id is null.");
		}
		
		//If Use External Identifier flag is false then append Claim Id to External Identifier being passed.
		//If External Identifier is not passed then don't append Claim Id.
		if(!claimImportCompositeDTO.isUseExternalIdentifier()) {
			if(claimImportCompositeDTO.getUnits() != null && !claimImportCompositeDTO.getUnits().isEmpty()) {
				
				for(ClaimUnitDTO claimUnit : claimImportCompositeDTO.getUnits()) {
					if(claimUnit.getExternalSourceId() != null && claimUnit.getRecordId() == null) {
						claimUnit.setExternalSourceId(claimUnit.getExternalSourceId() + "_" + claimId);
					}
					
					setExternalIdentifierForParticipants(claimUnit.getOwners(), claimId);
					if(claimUnit instanceof ClaimUnitVehicleDTO) {
						ClaimUnitVehicleDTO claimUnitVehicle = (ClaimUnitVehicleDTO) claimUnit;
						setExternalIdentifierForParticipants(claimUnitVehicle.getDrivers(), claimId);						
					}

					setExternalIdentifierForParticipants(claimUnit.getInjuredPersonList(), claimId);

					if(claimUnit instanceof ClaimUnitVehicleDTO) {
						ClaimUnitVehicleDTO claimUnitVehicle = (ClaimUnitVehicleDTO) claimUnit;
						setExternalIdentifierForParticipants(claimUnitVehicle.getPassengers(), claimId);						
					}

					setExternalIdentifierForParticipants(claimUnit.getOtherPartiesList(), claimId);
				}
			}
		}
		
		//Save Claim Indicators
		if(claimImportCompositeDTO.getClaimDTO() != null 
				&& claimImportCompositeDTO.getClaimDTO().getClaimIndicators() != null) {
			if(newClaim && !claimImportCompositeDTO.getClaimDTO().getClaimIndicators().getExternalIndicator()) {
				claimImportCompositeDTO.getClaimDTO().getClaimIndicators().setExternalIndicator(true);
			}
		}

		//Save Claim Loss
		if(claimImportCompositeDTO.getClaimDTO() != null){
			claimImportHelper.saveClaimNoticeOfLoss(claimImportCompositeDTO);
		}
		
		//Save Claim Profile Statements
		if(claimImportCompositeDTO.getClaimDTO() != null) {
			claimImportHelper.saveProfileStatements(claimImportCompositeDTO.getClaimDTO(), getExternalInterfaceBeanMerge());
		}
		
        boolean closeClaim = false;
        ClaimStatusDTO closeClaimStatus = null;
        
        // Save status if claimStatus and claimImportCompositeDTO.getClaimDTO() != null 
		if(claimImportCompositeDTO.getClaimDTO() != null && claimImportCompositeDTO.getClaimDTO().getClaimStatus() != null
				&& claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent() != null) {
			String CLOSE_CLAIM_ACTION_CODE = "closclm";
			if(newClaim && claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent() != null
					&& claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getActionCode().equals(CLOSE_CLAIM_ACTION_CODE)) {
				
				closeClaim = true;
				//if action code is close claim then release the claim and then finally close so that other
				//entities can be imported
				
				closeClaimStatus = new ClaimStatusDTO();
				
				BeanUtils.copyProperties(
						claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent(), closeClaimStatus);
				
				ClaimStatusDTO status = claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent();
				
				status.setActionCode(ClaimStatusConstants.ACTION_RELEASE_CLAIM);
				status.setReasonCode(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getReasonCode());
				status.setStateCode(ClaimStatusConstants.STATE_OPEN);
				status.setStatusCode(ClaimStatusConstants.STATUS_ACCEPTED);

				claimImportCompositeDTO.getClaimDTO().getClaimStatus().setCurrent(status);
			}

			claimImportHelper.saveClaimStatus(claimImportCompositeDTO);
		}
		
		//Save Claim Units if available
		if(claimImportCompositeDTO.getUnits() != null &&
				!claimImportCompositeDTO.getUnits().isEmpty()) {
			claimImportHelper.saveClaimUnit(claimImportCompositeDTO, getExternalInterfaceBeanMerge(), 
					getClaimParticipationHelper());
		}
		
		//Save Authorities if available
		if(claimImportCompositeDTO.getAuthorities() != null &&
				!claimImportCompositeDTO.getAuthorities().isEmpty()) {
			claimImportHelper.saveClaimLevelParticipantAuthority(claimImportCompositeDTO, getExternalInterfaceBeanMerge());
		}

		//Save Witnesses if available
		if(claimImportCompositeDTO.getWitnesses() != null &&
				!claimImportCompositeDTO.getWitnesses().isEmpty()) {
			claimImportHelper.saveClaimLevelParticipantWitness(claimImportCompositeDTO, getExternalInterfaceBeanMerge());
		}
		
		//Save Related Claims if available
		if(claimImportCompositeDTO.getRelatedClaims() != null 
				&& !claimImportCompositeDTO.getRelatedClaims().isEmpty()) {
			claimImportHelper.saveRelatedClaims(claimImportCompositeDTO, getExternalInterfaceBeanMerge());
		}
		
		flushDS();
		//Save Misc Transactions if available
		if(claimImportCompositeDTO.getClaimMiscTransactions() != null && 
				!claimImportCompositeDTO.getClaimMiscTransactions().isEmpty()){
			for(ClaimMiscTransactionDTO claimMiscTransactionDTO : claimImportCompositeDTO.getClaimMiscTransactions()){
				if(claimMiscTransactionDTO.getExternalClaimIdentifier()==null){
					claimMiscTransactionDTO.setExternalClaimIdentifier(claimImportCompositeDTO.getClaimExternalSourceId());
				}
				if(claimMiscTransactionDTO.getClaimId() == null) {
					claimMiscTransactionDTO.setClaimId(claimId); 
				}
				//If Use External Identifier flag is false then append Claim Id to External Identifier being passed.
				//If External Identifier is not passed then don't append Claim Id.
				if(!claimImportCompositeDTO.isUseExternalIdentifier()) {
					if(claimMiscTransactionDTO.getExternalUnitIdentifier()  != null 
							&& claimMiscTransactionDTO.getRecordId() == null) {
						claimMiscTransactionDTO.setExternalUnitIdentifier(claimMiscTransactionDTO.getExternalUnitIdentifier() + "_" + claimId);
					}

					if(claimMiscTransactionDTO.getExternalParticipantIdentifier() != null
							&& claimMiscTransactionDTO.getRecordId() == null) {
						claimMiscTransactionDTO.setExternalParticipantIdentifier(claimMiscTransactionDTO.getExternalParticipantIdentifier() + "_" + claimId);
					}					
				}

				claimImportHelper.importClaimMiscellaneousTransactions(claimMiscTransactionDTO);
			}
		}
		
		//Save Claim Reserves if available
		if(claimImportCompositeDTO.getClaimReservesWithCoverages() != null && 
				!claimImportCompositeDTO.getClaimReservesWithCoverages().isEmpty()){
			
			for(CALClaimReserveDTO calClaimReserveDTO : claimImportCompositeDTO.getClaimReservesWithCoverages()) {
				claimImportHelper.importClaimReserveTransactions(claimImportCompositeDTO, 
						calClaimReserveDTO, getImportUtilPojo(), getClaimUnitDAO());
				flushDS();
			}
		}
		// save Claim Bill if Available
		if(claimImportCompositeDTO.getBills() != null && !claimImportCompositeDTO.getBills().isEmpty()){
			for(ClientClaimBillDTO billDTO : claimImportCompositeDTO.getBills()){
				ClaimBillService billService = MuleServiceFactory.getService(ClaimBillService.class);
				billDTO.setClaimId(claimId);
				
				billDTO.setCurrentCycle(billDTO.getCurrentBillCycle());
				
				if(billDTO.getPreviousBillCycles() != null
						&& !billDTO.getPreviousBillCycles().isEmpty()) {
					
					Collection<BillCycleDTO> claimBillCycles 
								= new ArrayList<BillCycleDTO>();
					for(ClientClaimBillCycleDTO clientClaimBillCycle : billDTO.getPreviousBillCycles()) {
						ClaimBillCycleNCDTO claimBillCycle = new ClaimBillCycleNCDTO();
						BeanUtils.copyProperties(clientClaimBillCycle, claimBillCycle);
						
						claimBillCycles.add(claimBillCycle);
					}
					billDTO.setPreviousCycles(claimBillCycles);
				}
				
				
				ClaimBillDTO clmBillDTO = billService.createClaimBill(billDTO);
				//After Bill and Bill Cycle are saved check whether Bill Line Items are available for Bill Cycle
				//If available then check if Line Items are available and set the Bill Cycle ID.
				if(clmBillDTO.getCurrentCycle() != null && billDTO.getCurrentBillCycle().getClaimBillLineItems() != null) {
					for(BillLineItemDTO billLineItemDTO : billDTO.getCurrentBillCycle().getClaimBillLineItems()) {
						billLineItemDTO.setBillCycleId(clmBillDTO.getCurrentCycle().getRecordId());
					}
					ClaimBillLineItemService billLineItemService = MuleServiceFactory.getService(ClaimBillLineItemService.class);
					billLineItemService.saveBillLineItems(billDTO.getCurrentBillCycle().getClaimBillLineItems());
				}
			}
		}
		flushDS();
		if(claimImportCompositeDTO.getClaimPayments() != null && !claimImportCompositeDTO.getClaimPayments().isEmpty()) {
			Long companyId = null;
			for(ClaimPayment claimPayment : claimImportCompositeDTO.getClaimPayments()){
				for(ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
					if(companyId == null) {
						companyId = claimPayable.getCompanyId();
					}
					if(claimPayable.getClaimId() == null) {
						claimPayable.setClaimId(claimId);
					}
					if(claimPayable.getClaimNumber() == null) {
						claimPayable.setClaimNumber(claimImportCompositeDTO.getClaimNumber());
					}
					
					/**
					 * User supplied date should override system accounting and transaction dates, Required for Conversions
					 */
					//claimPayable.setAccountingDate(null);
					//claimPayable.setTransactionDate(null);
					
					//If Use External Identifier flag is false then append Claim Id to External Identifier being passed.
					//If External Identifier is not passed then don't append Claim Id.
					if(!claimImportCompositeDTO.isUseExternalIdentifier()) {
						if(claimPayable.getExternalUnitIdentifier()  != null 
								&& claimPayable.getRecordId() == null) {
							claimPayable.setExternalUnitIdentifier(claimPayable.getExternalUnitIdentifier() + "_" + claimId);
						}

						if(claimPayable.getExternalParticipantIdentifier() != null 
								&& claimPayable.getRecordId() == null) {
							claimPayable.setExternalParticipantIdentifier(claimPayable.getExternalParticipantIdentifier() + "_" + claimId);
						}
					}
					//Check if Reserve exists for the Payment. If it doesn't exist then create the reserve.
					claimImportHelper.createReserveIfNotAvailable(claimPayment, claimPayable, getImportUtilPojo(), getClaimUnitDAO());					
				}
				
				if(claimPayment.getOperationTypeForPayment() == null) {
					claimPayment.setOperationTypeForPayment(ClaimReserveConstants.OPERATION_TYPE_ISSUE_PAYMENT);
				}
				//Check if the disbursement is EFT payment and whether Banking account # and routing # is passed for the Payee.
				//If it is not passed check whether Party Bank Account ID is passed. If it is not passed then throw an error.
				
				//If Party Bank Account ID is passed and Banking account # or routing # is not passed then call
				//Party Service to get Party Banking Information.
				for(ClaimPaymentDisbursement claimPaymentDisbursement : claimPayment.getClaimPaymentDisbursements()) {
					
					if(claimPaymentDisbursement.getCompanyId() == null) {
						if(companyId != null) {
							claimPaymentDisbursement.setCompanyId(companyId);
						} else if(claimImportCompositeDTO.getCompanyId() != null) {
							claimPaymentDisbursement.setCompanyId(claimImportCompositeDTO.getCompanyId());
						} else if(claimImportHelper.companyId != null) {
							claimPaymentDisbursement.setCompanyId(claimImportHelper.companyId);
						}
					}
					/**
					 * User supplied date should override system accounting and transaction dates, Required for Conversions
					 */
					//claimPaymentDisbursement.setAccountingDate(null);
					//claimPaymentDisbursement.setTransactionDate(null);
					
					/*
					//If Payee Party Id is passed and additional payee collection is empty or null then 
					//create a new instance of Additional Payee collection add the party 
					if ( (claimPaymentDisbursement.getPartyIdPayee() != null)
							&& (CollectionUtils.isEmpty(claimPaymentDisbursement.getClaimDisbursementAdditionalPayees())) ) {
						
						Collection<ClaimDisbursementAdditionalPayee> additionalPayess = new ArrayList<ClaimDisbursementAdditionalPayee>();
						ClaimDisbursementAdditionalPayee additionalPayee = new ClaimDisbursementAdditionalPayee();
						additionalPayee.setPartyIdPayee(claimPaymentDisbursement.getPartyIdPayee());
						additionalPayee.setPayToTheOrderOfSequence(0L);
						additionalPayess.add(additionalPayee);
						
						claimPaymentDisbursement.setClaimDisbursementAdditionalPayees(additionalPayess);
					} else */
					if( (claimPaymentDisbursement.getPartyIdPayee() != null) 
							&& (!CollectionUtils.isEmpty(claimPaymentDisbursement.getClaimDisbursementAdditionalPayees())) ) {
						
						//If Payee Party Id is passed and additional payee collection is not empty then add the party 
						//as the first element in Additional Payee collection. 						
						
						ArrayList<ClaimDisbursementAdditionalPayee> existingAdditionalPayees = (ArrayList<ClaimDisbursementAdditionalPayee>)
																		(claimPaymentDisbursement.getClaimDisbursementAdditionalPayees());
						
						//If there is only one element in the Additional Payee list and 
						//disbursement payee id is same as additional payee id then then don't
						//add disbursement payee id to additional payee list
						if(existingAdditionalPayees.size() == 1) {
							ClaimDisbursementAdditionalPayee claimDisbursementAdditionalPayee = 
										claimPaymentDisbursement.getClaimDisbursementAdditionalPayees().iterator().next();
							if(!claimPaymentDisbursement.getPartyIdPayee().equals(claimDisbursementAdditionalPayee.getPartyIdPayee())) {
								ClaimDisbursementAdditionalPayee additionalPayee = new ClaimDisbursementAdditionalPayee();
								additionalPayee.setPartyIdPayee(claimPaymentDisbursement.getPartyIdPayee());
								additionalPayee.setPayToTheOrderOfSequence(0L);
								
								existingAdditionalPayees.add(0, additionalPayee);
							}
						} else {
							ClaimDisbursementAdditionalPayee additionalPayee = new ClaimDisbursementAdditionalPayee();
							additionalPayee.setPartyIdPayee(claimPaymentDisbursement.getPartyIdPayee());
							additionalPayee.setPayToTheOrderOfSequence(0L);
							
							existingAdditionalPayees.add(0, additionalPayee);
						}
						
						// Move the legal wording code up a level
						int index = 0;
						for(ClaimDisbursementAdditionalPayee claimDisbursementAdditionalPayee : existingAdditionalPayees) {
							int nextIndex = index + 1;
							if ( nextIndex < existingAdditionalPayees.size()) {
								claimDisbursementAdditionalPayee.setLegalWordingCode(existingAdditionalPayees.get(nextIndex).getLegalWordingCode());
							} else {
								claimDisbursementAdditionalPayee.setLegalWordingCode(null);
							}
							index++;
						}
					}
					
					if(claimPaymentDisbursement.getBankAccountTypeCodePayee() != null && 
							claimPaymentDisbursement.getBankAccountTypeCodePayee().equals(
									DisbursementConstants.PAYMENT_METHOD_PREFERENCE_CD_EFT)) {
						
						if((claimPaymentDisbursement.getPartyBankAccountNoPayee() == null 
								|| claimPaymentDisbursement.getFinancialInstitutionRouteNoPayee() == null)
								&& claimPaymentDisbursement.getPartyBankAccountIdPayee() == null) {
								
								throw new IIPCoreSystemException(
										"Unable to import Claim Payment. Party Bank Account information is not available.");
							}
						
						if((claimPaymentDisbursement.getPartyBankAccountNoPayee() == null 
								|| claimPaymentDisbursement.getFinancialInstitutionRouteNoPayee() == null)) {
							
							PartyService partyService = MuleServiceFactory.getService(PartyService.class);
							BankAccountDTO bankAccountInfo = 
								partyService.retrieveBankAccountDetails(claimPaymentDisbursement.getPartyBankAccountIdPayee());
							
							if(bankAccountInfo == null ||
									bankAccountInfo.getBankAccountNumber() == null ||
									bankAccountInfo.getRoutingNumber() == null) {
								throw new IIPCoreSystemException(
								"Unable to import Claim Payment. Party Bank Account information is not valid for " 
										+ claimPaymentDisbursement.getPartyBankAccountIdPayee());
							}
							
							claimPaymentDisbursement.setPartyBankAccountNoPayee(bankAccountInfo.getBankAccountNumber());
							claimPaymentDisbursement.setFinancialInstitutionRouteNoPayee(bankAccountInfo.getRoutingNumber());
						}
					}
				}
				flushDS();
				claimImportHelper.importClaimDisbursementTransactions(claimPayment);
			}
		}
		
		//Save Claim Recoveries if available
		if(claimImportCompositeDTO.getRecoveries() != null 
				&& !claimImportCompositeDTO.getRecoveries().isEmpty()) {
			
			for(ClaimRecovery claimRecovery : claimImportCompositeDTO.getRecoveries()){
				for(ClaimRecoveryPayable claimRecoveryPayable : claimRecovery.getClaimRecoveryPayables()) {
					if(claimRecoveryPayable.getClaimId() == null) {
						claimRecoveryPayable.setClaimId(claimId);
					}
					if(claimRecoveryPayable.getClaimNumber() == null) {
						claimRecoveryPayable.setClaimNumber(claimImportCompositeDTO.getClaimNumber());
					}
					/**
					 * User supplied date should override system accounting and transaction dates, Required for Conversions
					 */					
					//claimRecoveryPayable.setTransactionDate(null);
					
					//If Use External Identifier flag is false then append Claim Id to External Identifier being passed.
					//If External Identifier is not passed then don't append Claim Id.
					if(!claimImportCompositeDTO.isUseExternalIdentifier()) {
						if(claimRecoveryPayable.getExternalUnitIdentifier()  != null 
								&& claimRecoveryPayable.getRecordId() == null) {
							claimRecoveryPayable.setExternalUnitIdentifier(claimRecoveryPayable.getExternalUnitIdentifier() + "_" + claimId);
						}

						if(claimRecoveryPayable.getExternalParticipantIdentifier() != null 
								&& claimRecoveryPayable.getRecordId() == null) {
							claimRecoveryPayable.setExternalParticipantIdentifier(claimRecoveryPayable.getExternalParticipantIdentifier() + "_" + claimId);
						}
					}
				}
				
				/**
				 * User supplied date should override system accounting and transaction dates, Required for Conversions
				 */				
				//claimRemittance.setTransactionDate(null);
				
				claimImportHelper.saveClaimRecoveries(claimRecovery);
			}
		}

		//save Claim Services if available
		if(claimImportCompositeDTO.getClaimServices() != null 
				&& !claimImportCompositeDTO.getClaimServices().isEmpty()) {
			claimImportHelper.saveClaimServices(claimImportCompositeDTO, getExternalInterfaceBeanMerge());
		}

		// Save Collection of Notes if available
		if(claimImportCompositeDTO.getNotes()!=null && !claimImportCompositeDTO.getNotes().isEmpty()){
			for(NotepadDTO notepadDTO : claimImportCompositeDTO.getNotes()){
				claimImportHelper.buildNotepad(notepadDTO, claimId, claimImportCompositeDTO);
				NotepadService notepadService = MuleServiceFactory.getService(NotepadService.class);
				notepadService.saveNotepad(notepadDTO);
			}
			
		}
		// Save Case if Available
		if(claimImportCompositeDTO.getClaimCases()!=null && !claimImportCompositeDTO.getClaimCases().isEmpty()){
			for(CaseDTO caseDTO : claimImportCompositeDTO.getClaimCases()){
				claimImportHelper.buildCaseDTO(caseDTO, claimImportCompositeDTO);
				CaseService caseService =  MuleServiceFactory.getService(CaseService.class);
				caseService.saveCase(caseDTO);
			}
		}

		// Save Assignment if Available
		if(claimImportCompositeDTO.getClaimAssignments()!=null && !claimImportCompositeDTO.getClaimAssignments().isEmpty()){
			for(ClientImportAssignmentDTO assignmentDTO : claimImportCompositeDTO.getClaimAssignments()){
				assignmentDTO.setAgreementIdDerived(claimId);
				//set Claim Unit ID or Claim Participant ID and the sub type code
				claimImportHelper.setSubTypeDetails(claimImportCompositeDTO.isUseExternalIdentifier(), claimId, 
							assignmentDTO, assignmentDTO.getAgreementSubTypeIdDerived());
				
				claimImportHelper.setAgreementDataDetails(assignmentDTO, claimImportCompositeDTO);
				
				if(claimImportCompositeDTO.getClaimDTO() == null) {
					claimImportHelper.retrieveClaim(claimImportCompositeDTO);
				}
				
				//Check if Company Org Unit is passed. If it is not then get for the User to whom the claim is assigned
				claimImportHelper.setCompanyOrgUnitIdForAssignments(assignmentDTO, claimImportCompositeDTO.getClaimDTO().getCompanyLOBId(), 
						getClientEnterpriseConfigDAO());

				//Check if Functional Role Code is passed. If it is not then get the functional role code the User
				//to whom the claim is assigned
				claimImportHelper.setFunctionalRoleCodeForAssignments(assignmentDTO, getClientEnterpriseConfigDAO());
				
				//Setting the Load Balancing Rule Code to "specific" similar to how it is hard coded in Stream UI
				assignmentDTO.setLoadBalancingRuleCode(WORKITEM_LOAD_BALANCING_RULE_CODE_SPECIFIC);
				
				//Hard coding to true to suppress warning message and continue saving
				//TODO: Remove hard coding after base is fixed suppress warning messages.
				assignmentDTO.setSaveEvenIfRuleExist(true);
				
				AssignmentService assignmentService = MuleServiceFactory.getService(AssignmentService.class);
				assignmentService.saveAssignment(assignmentDTO);
			}
		}
		flushDS();
		// Save Work Item if Available
		if(claimImportCompositeDTO.getWorkItems()!=null && !claimImportCompositeDTO.getWorkItems().isEmpty()){
			for(ClientWorkItemDTO workItemDTO : claimImportCompositeDTO.getWorkItems()){
				workItemDTO.setAgreementIdDerived(claimId);
				//set Claim Unit ID or Claim Participant ID and the sub type code
				claimImportHelper.setSubTypeDetails(claimImportCompositeDTO.isUseExternalIdentifier(), claimId, 
						workItemDTO, workItemDTO.getAgreementSubTypeIdDerived());
				
				claimImportHelper.setAgreementDataDetails(workItemDTO, claimImportCompositeDTO);

				if(claimImportCompositeDTO.getClaimDTO() == null) {
					claimImportHelper.retrieveClaim(claimImportCompositeDTO);
				}

				//Check if Company Org Unit is passed. If it is not then get Company Org Unit for the User 
				//to whom the work item is assigned
				claimImportHelper.setCompanyOrgUnitIdForWorkItems(workItemDTO, 
						claimImportCompositeDTO.getClaimDTO().getCompanyLOBId(), getClientEnterpriseConfigDAO());

				//Hard coding to true to suppress warning message and continue saving
				//TODO: Remove hard coding after base is fixed suppress warning messages.
				workItemDTO.setSaveEvenIfRuleExist(true);
				
				WorkflowService workflowService = MuleServiceFactory.getService(WorkflowService.class);
				workflowService.saveWorkItem(workItemDTO);
			}
		}
		//Save Documents
		saveDocuments(claimImportHelper, claimImportCompositeDTO);

        if(closeClaim && closeClaimStatus != null) {
			claimImportCompositeDTO.getClaimDTO().getClaimStatus().setCurrent(closeClaimStatus);
			claimImportHelper.saveClaimStatus(claimImportCompositeDTO);
		}
           // Save Claim Blocks
        if(claimImportCompositeDTO.getClaimBlocksCompositeDTO() != null && !claimImportCompositeDTO.getClaimBlocksCompositeDTO().getClaimBlocks().isEmpty()){
        	Collection<ClaimBlocksDTO> claimBlocks = claimImportCompositeDTO.getClaimBlocksCompositeDTO().getClaimBlocks();
        	if(claimBlocks != null && !claimBlocks.isEmpty()){
        		for(ClaimBlocksDTO claimBlock:claimBlocks) {
        			claimBlock.setClaimId(claimId);
        			logger.info("Setting Block ClaimId: " + claimId);
        		}
                logger.info("Saving Claim Blocks");
        		//ClaimBlocksService claimBlockService = MuleServiceFactory.getService(ClaimBlocksService.class);
            	saveClaimBlocks(claimImportCompositeDTO.getClaimBlocksCompositeDTO());        		
        	}       	
        }

	}
	
	
	public void saveDocuments(ClaimImportHelper claimImportHelper, ClaimImportCompositeDTO claimImportCompositeDTO){

		if(claimImportCompositeDTO.getClaimDocs() != null && !claimImportCompositeDTO.getClaimDocs().isEmpty()) {
			for(ClientDocumentDTO document : claimImportCompositeDTO.getClaimDocs()) {
				document.setAgreementIdDerived(claimImportCompositeDTO.getClaimId());
				//set Claim Unit ID or Claim Participant ID and the sub type code
				claimImportHelper.setSubTypeDetails(claimImportCompositeDTO.isUseExternalIdentifier(), claimImportCompositeDTO.getClaimId(), 
						document, document.getAgreementSubTypeIdDerived());
				
				claimImportHelper.setAgreementDataDetails(document, claimImportCompositeDTO);
				/* 10/02/2014 @GR - Document Import Bug - Save as Inbound Document for FNOL.
				documentService.saveDocument(document);
				DocumentInboundDTO inboundDoc = new DocumentInboundDTO();
				BeanUtils.copyProperties(document, inboundDoc);
				inboundDoc.setInboundDocTypeCode("fnol");
				documentService.saveInboundDocument(inboundDoc);*/
				
				// 10/18/2014 @GR - Use Create New Document API for inbound documents
				if(document.getDocumentSourceCode().equals(DocumentSourceCode.DOCUMENT_INBOUND)){
					createNewInboundDocument(document, claimImportCompositeDTO.getClaimDTO().getJurisdictionId());
				}else if(document.getDocumentSourceCode().equals(DocumentSourceCode.ELECTRONIC_MEDIA_FILE)){
					MuleServiceFactory.getService(ClientDocumentService.class).createNewInboundAttachment(document);
				}else if(document.getDocumentSourceCode().equals(DocumentSourceCode.DOCUMENT_OUTBOUND)){
					//TODO - Need to Implement this
				}
			}
		}
	}
	

	/*
	 * Create New Inbound Document
	 */
	public void createNewInboundDocument(ClientDocumentDTO document, Long jurisdictionId){
		
		InboundDocumentType inboundDocumentType=new InboundDocumentType();
		inboundDocumentType.setAgreementTypeCode(document.getAgreementTypeCodeDerived());
		inboundDocumentType.setDocumentCategoryCode(document.getCategoryCode());
		inboundDocumentType.setDocumentCategoryTypeCode(document.getCategoryTypeCode());
		inboundDocumentType.setInboundDocTypeCode("fnol");
		inboundDocumentType.setInboundDocTypeDocRefNumber(document.getDocumentReferenceNumber());
		inboundDocumentType.setInboundDocTypeExternalCode("FNOL");
		inboundDocumentType.setInboundDocTypeName("First Notice of Loss");
		
		ContextData data = new ContextData();
		data.setAgreementId(document.getAgreementIdDerived());
		data.setJurisdictionId(jurisdictionId);
		data.setCompanyId(document.getCompanyIdDerived());
		data.setRecordSourceCode(document.getRecordSourceCode());
		data.setAgreementTypeCode(document.getAgreementTypeCodeDerived());
		
		DocumentExternalIndexDTO indexedDocumentDTO=new DocumentExternalIndexDTO();
		
		indexedDocumentDTO.setDocumentDate(DateUtility.getSystemDateOnly());
		indexedDocumentDTO.setDocumentTypeCode("fnol");
		indexedDocumentDTO.setContextTypeCode(document.getAgreementTypeCodeDerived());
		indexedDocumentDTO.setDocumentImageId(document.getDocumentReferenceNumber());
		
		getDocumentServiceHelper().createNewInboundDocument(inboundDocumentType,indexedDocumentDTO,data, null);		
	}
	
	@Transactional
	public ClaimBlocksCompositeDTO saveClaimBlocks(
			ClaimBlocksCompositeDTO claimBlocks) {
		
		Collection<ClaimBlocksDTO> claimBlocksDTO = claimBlocks.getClaimBlocks();
		boolean isNew = false;
		ClaimBlocksBO blockBO = null;
		for (ClaimBlocksDTO claimBlockDTO : claimBlocksDTO) {			
			
			if (claimBlockDTO.getClaimId() == null) {
				throw new IllegalArgumentException("Claim Id "
						+ "is null and it is required for saving claim blocks");
			}
			claimBlockDTO.setBlockLevel(ClaimsServiceConstants.CLAIM_BLOCK_LEVEL_TYPE);

			isNew = claimBlockDTO.isNew();
			
			blockBO = DTOUtils.saveEntityWithAssociations(lds, claimBlockDTO, ClaimBlocksBO.class);
			
			if (isNew) {
				fireEvent(ClaimsServiceConstants.CLAIM_BLOCK_CREATE_EVENT,
						blockBO);
			} else {
				fireEvent(ClaimsServiceConstants.CLAIM_BLOCK_UPDATE_EVENT,
						blockBO);
			}

		}
		return claimBlocks;
	}
	

	/**
	 * Fire events.
	 * 
	 * @param eventName
	 *            The event Name.
	 * @param blockBO
	 *            The bo object.
	 */
	private void fireEvent(String eventName, ClaimBlocksBO blockBO) {
		EventDispatcherHelper helper = new EventDispatcherHelper();

		helper.setEventName(eventName);
		helper.addParameter("block_id", blockBO.getRecordId(), "nbr");		
		helper.addParameter("clm_id", blockBO.getClaimId(), "nbr");

		eventDispatcher.dispatchEvent(helper);
	}
	
	/**
	 * @return the eventDispatcher
	 */
	public GenericEventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	/**
	 * @param value
	 *            the eventDispatcher to set
	 */
	@Inject(PojoRef = "claimsall.service.pojo.genericEventDispatcher")
	public void setEventDispatcher(GenericEventDispatcher value) {
		this.eventDispatcher = value;
	}	
	
	/**
	 * @param participants
	 * @param claimId
	 */
	private void setExternalIdentifierForParticipants(Collection<? extends CALClaimParticipationDTO> participants, Long claimId) {
		if(participants != null && !participants.isEmpty()) {
			for(CALClaimParticipationDTO claimParticipant : participants) {
				if(claimParticipant.getExternalSourceId() != null 
						&& claimParticipant.getRecordId() == null) {
					claimParticipant.setExternalSourceId(claimParticipant.getExternalSourceId() + "_" + claimId);
				}
				//populate ICD code descriptions
				Collection<CALClaimICDStatusDTO> icdStatuses = claimParticipant.getIcdStatusDetails();
				if(icdStatuses != null && !icdStatuses.isEmpty()) {
					ICDCodeSearchCriteria criteria = new ICDCodeSearchCriteria();
					for (CALClaimICDStatusDTO icdStatus : icdStatuses) {
						if(icdStatus.getIcdCode() != null) {
							criteria.setCode(icdStatus.getIcdCode());
							Collection<CALClaimICDStatusDTO> searchResults = getClaimsService().retrieveIcdCodeList(criteria);
							if(searchResults != null && !searchResults.isEmpty()) {
								//should fine only 1, when searching by ICD code
								CALClaimICDStatusDTO icdSearchDTO = (CALClaimICDStatusDTO) searchResults.iterator().next();
								icdStatus.setIcdDescription(icdSearchDTO.getIcdDescription());
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Import Claim from ClaimImportCompositeDTO provided by client.
	 * @param claimImportCompositeDTO
	 * @return	Claim Number
	 * @throws BoundValidationException
	 */
	//@Transactional(propagation = Propagation.REQUIRED)
	public String importClaim(ClaimImportCompositeDTO claimImportCompositeDTO)
			throws BoundValidationException {
		
		//Set Dates in Local Thread Context
		IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
		IIPDataContext context = threadCtx.getDataContext()==null?new IIPDataContext():threadCtx.getDataContext();
	
		if(claimImportCompositeDTO.getClaimPayments() != null && !claimImportCompositeDTO.getClaimPayments().isEmpty()) {
			for(ClaimPayment claimPayment : claimImportCompositeDTO.getClaimPayments()){
				for(ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
					context.setAppData("transDate", claimPayable.getTransactionDate());
					context.setAppData("acctDate", claimPayable.getAccountingDate());	
					break;
				}
			}
		}else if(claimImportCompositeDTO.getClaimReservesWithCoverages() != null && 
				!claimImportCompositeDTO.getClaimReservesWithCoverages().isEmpty()){
			for(CALClaimReserveDTO calClaimReserveDTO : claimImportCompositeDTO.getClaimReservesWithCoverages()) {
				context.setAppData("transDate", calClaimReserveDTO.getReserveTransactionDate());
				context.setAppData("acctDate", calClaimReserveDTO.getReserveTransactionDate());	
				break;
		
			}
		}else if(claimImportCompositeDTO.getRecoveries() != null 
				&& !claimImportCompositeDTO.getRecoveries().isEmpty()){
				for(ClaimRecovery claimRecovery : claimImportCompositeDTO.getRecoveries()){
					for(ClaimRecoveryPayable claimRecoveryPayable : claimRecovery.getClaimRecoveryPayables()) {
						context.setAppData("transDate", claimRecoveryPayable.getTransactionDate());
						context.setAppData("acctDate", claimRecoveryPayable.getTransactionDate());
						break;
					}
				}
		}else if(claimImportCompositeDTO.getRecoveries() != null 
				&& !claimImportCompositeDTO.getRecoveries().isEmpty()){
			for(ClaimRecovery claimRecovery : claimImportCompositeDTO.getRecoveries()){
				if(claimRecovery.getClaimRemittance() != null){
					context.setAppData("transDate", claimRecovery.getClaimRemittance().getTransactionDate());
					context.setAppData("acctDate", claimRecovery.getClaimRemittance().getTransactionDate());
					break;					
				}
			}
		}
		
		context.setAppData("closeRecoveryReserve", new Boolean(claimImportCompositeDTO.isCloseRecovery()));
		
		ClaimImportHelper claimImportHelper = new ClaimImportHelper(); 

		ClientClaimPolicyPartyImportProcessor clientClaimPolicyPartyImportProcessor = 
			new ClientClaimPolicyPartyImportProcessor();
		
		Map<Long, PartyDTO> partyMap = new HashMap<Long, PartyDTO>();

		ClientPolicyImportWrapperDTO policyImportWrapper = new ClientPolicyImportWrapperDTO();
		policyImportWrapper.setPolicy(claimImportCompositeDTO.getPolicy());
		policyImportWrapper.setPersons(claimImportCompositeDTO.getPersons());
		policyImportWrapper.setOrganizations(claimImportCompositeDTO.getOrganizations());
		//Create Policy parties and associate participants with party
		clientClaimPolicyPartyImportProcessor.createParties(policyImportWrapper, partyMap);
		flushDS();
		if(claimImportCompositeDTO.getClaimDTO() != null && claimImportCompositeDTO.getClaimDTO().getPolicy() != null) {
			clientClaimPolicyPartyImportProcessor.createClaimPolicyParties(claimImportCompositeDTO, partyMap);
			flushDS();
		}
		//Create Claim Parties
		clientClaimPolicyPartyImportProcessor.createClaimParties(claimImportCompositeDTO, partyMap);
		flushDS();
		importClaimEntities(claimImportCompositeDTO, claimImportHelper);

		return claimImportCompositeDTO.getClaimNumber();
	}

	/**
	 * @return ImportUtilPojo instance
	 */
	public ImportUtilPojo getImportUtilPojo() {
		return importUtilPojo;
	}
	
	/**
	 * @param pojo
	 */
	@Inject(PojoRef="claims.financials.pojo.importUtilPojo")
	public void setImportUtilPojo(ImportUtilPojo pojo) {
		importUtilPojo = pojo;
	}
	/**
	 * @return the claimParticipationHelper
	 */
	public CWSClaimParticipationHelper getClaimParticipationHelper() {
		return claimParticipationHelper;
	}
	/**
	 * @param helper the claimParticipationHelper to set
	 */
	@Inject(PojoRef="claimsall.pojo.claimParticipationHelper")
	public void setClaimParticipationHelper(CWSClaimParticipationHelper helper) {
		this.claimParticipationHelper = helper;
	}
	/**
	 * @return the claimPolicyHelper
	 */
	public ClaimPolicyHelper getClaimPolicyHelper() {
		return claimPolicyHelper;
	}
	/**
	 * @param claimPolicyHelper the claimPolicyHelper to set
	 */
	@Inject(PojoRef="claim.policy.claimPolicyHelper")
	public void setClaimPolicyHelper(ClaimPolicyHelper claimPolicyHelper) {
		this.claimPolicyHelper = claimPolicyHelper;
	}
}
