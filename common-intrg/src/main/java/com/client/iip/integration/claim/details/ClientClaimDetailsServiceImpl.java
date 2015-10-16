package com.client.iip.integration.claim.details;


import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.mule.api.MuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.client.iip.integration.claim.ClientClaimSearchCriteriaDTO;
import com.client.iip.integration.claim.imports.ClaimImportCompositeDTO;
import com.client.iip.integration.claim.imports.ClientAssignmentStatusCompositeDTO;
import com.client.iip.integration.claim.imports.ClientCaseDTO;
import com.client.iip.integration.claim.imports.ClientCaseIssueDTO;
import com.client.iip.integration.claim.imports.ClientCaseIssueStatusCompositeDTO;
import com.client.iip.integration.claim.imports.ClientCaseStatusCompositeDTO;
import com.client.iip.integration.claim.imports.ClientClaimBillCycleDTO;
import com.client.iip.integration.claim.imports.ClientClaimBillDTO;
import com.client.iip.integration.claim.imports.ClientImportAssignmentDTO;
import com.client.iip.integration.claim.imports.ClientWorkItemDTO;
import com.client.iip.integration.documents.ClientDocumentDTO;
import com.client.iip.integration.party.ClientOrganizationDTO;
import com.client.iip.integration.party.ClientPersonDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationDTO;
import com.fiserv.isd.iip.bc.party.api.PersonDTO;
import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.dto.search.SearchResultsDTO;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.meta.annotation.ServiceMethod;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.fiserv.isd.iip.core.validation.Validateable;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.assignment.api.AssignmentDTO;
import com.stoneriver.iip.assignment.api.AssignmentService;
import com.stoneriver.iip.assignment.search.ViewAssignmentResultDTO;
import com.stoneriver.iip.assignment.search.ViewAssignmentsCriteria;
import com.stoneriver.iip.casetool.api.CaseDTO;
import com.stoneriver.iip.casetool.api.CaseIssueDTO;
import com.stoneriver.iip.casetool.api.CaseService;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimCoverageDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.authority.ClaimParticipationAuthorityDTO;
import com.stoneriver.iip.claims.bill.ClaimBillDetailDTO;
import com.stoneriver.iip.claims.claimblock.ClaimBlockInfoDTO;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksCompositeDTO;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksDTO;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksService;
import com.stoneriver.iip.claims.composite.ParticipationHelper;
import com.stoneriver.iip.claims.composite.search.ClaimsPolicyCompositeHelper;
import com.stoneriver.iip.claims.cws.codelookup.ClaimsAllClaimCoverageCodePojo;
import com.stoneriver.iip.claims.details.ClaimDetailRequestCriteria;
import com.stoneriver.iip.claims.details.ClaimDetailsResponseDTO;
import com.stoneriver.iip.claims.details.ClaimDetailsService;
import com.stoneriver.iip.claims.payment.ClaimPayable;
import com.stoneriver.iip.claims.payment.ClaimPayment;
import com.stoneriver.iip.claims.payment.ClaimPaymentDisbursement;
import com.stoneriver.iip.claims.reserve.CALClaimReserveDTO;
import com.stoneriver.iip.claims.reserve.CalClaimReserveTransactionDTO;
import com.stoneriver.iip.claims.reserve.ClaimReserveService;
import com.stoneriver.iip.claims.search.ClaimSearchCriteriaDTO;
import com.stoneriver.iip.claims.search.ClaimSearchException;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;
import com.stoneriver.iip.claims.search.ClaimsBlockSearchCriteriaDTO;
import com.stoneriver.iip.claims.search.ClaimsSearchConstants;
import com.stoneriver.iip.claims.status.ClaimStatusConstants;
import com.stoneriver.iip.claims.status.ClaimStatusDTO;
import com.stoneriver.iip.claims.status.ClaimStatusTransitionDTO;
import com.stoneriver.iip.claims.witness.ClaimWitnessDTO;
import com.stoneriver.iip.document.DocumentSourceCode;
import com.stoneriver.iip.document.api.DocumentDTO;
import com.stoneriver.iip.document.api.DocumentElectronicMediaFileDTO;
import com.stoneriver.iip.document.api.DocumentService;
import com.stoneriver.iip.document.search.DocumentSearchCriteriaDTO;
import com.stoneriver.iip.document.search.DocumentSearchResultDTO;
import com.stoneriver.iip.entconfig.EnterpriseConfigConstants;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.api.ProductDefinitionInfo;
import com.stoneriver.iip.entconfig.api.UserDetailsDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserCommInformationCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserInfoDTO;
import com.stoneriver.iip.entconfig.sa.api.users.UserPhoneDTO;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionAssociationDTO;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionService;
import com.stoneriver.iip.entconfig.users.EnterpriseUserConfigurationService;
import com.stoneriver.iip.workflow.api.WorkItemDTO;
import com.stoneriver.iip.workflow.api.WorkItemStatusCompositeDTO;
import com.stoneriver.iip.workflow.api.WorkItemStatusDTO;
import com.stoneriver.iip.workflow.api.WorkflowService;
import com.stoneriver.iip.workflow.search.WorkItemSearchCriteriaDTO;
import com.stoneriver.iip.workflow.search.WorkItemSearchResultDTO;


/**
 * Claim details composite service that returns various aspects of a claim.
 * 
 * @author brook
 * 
 */
@Service(id = "integration.serviceObject.ClientClaimDetailsService")
public class ClientClaimDetailsServiceImpl implements
		ClientClaimDetailsService {

	private Logger logger = LoggerFactory.getLogger(ClientClaimDetailsServiceImpl.class);
	
	private final String CLAIM_CONTEXT = "clm";

	private EnterpriseUserConfigurationService enterpriseUserConfigService;

	JurisdictionHelper jurisdictionHelper;
	ClaimDetailsHelper claimDetailsHelper;

	private EnterpriseConfigService enterpriseConfigService;

	private ClientClaimStatusDAO clientClaimStatusDAO;

	private ClaimsPolicyCompositeHelper claimsPolicyCompositeHelper;
	
	private ClaimsAllClaimCoverageCodePojo claimsAllClaimCoverageCodePojo;
	
	//List containing the attributes to be set to null when returning Claim Details 
	//for Claim Import
	List<String> lstAttributesToRemove = Arrays.asList(new String[] {
			"recordId", "version", "disbursementId", "claimId", "claimNumber",
			"claimPolicyId", "coverageId", "claimPolicyUnitId", "policyUnitId",
			"calClaimIdDerived", "calClaimUnitIdDerived", "calClaimPtcpId",
			"claimCoverageId", "calClaimParticipantId", "participantId",
			"claimUnitId", "unitId", "reserveId" });

	private ParticipationHelper participationHelper;
	
	private CWSClaimService allClaimService;

	/**
	 * @return the claimsPolicyCompositeHelper
	 */
	public ClaimsPolicyCompositeHelper getPolicyHelper() {
		return claimsPolicyCompositeHelper;
	}

	/**
	 * @param value the claimsPolicyCompositeHelper to set
	 */
	@Inject(PojoRef="com.stoneriver.iip.claims.composite.search.ClaimsPolicyCompositeHelper")
	public void setPolicyHelper(ClaimsPolicyCompositeHelper value) {
		this.claimsPolicyCompositeHelper = value;
	}
	
	/**
	 * @param helper
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.JurisdictionHelper")
	public void setJurisdictionHelper(JurisdictionHelper helper) {
		jurisdictionHelper = helper;
	}

	private EnterpriseUserConfigurationService getEnterpriseUserConfigurationService() {
		if (enterpriseUserConfigService == null) {
			return MuleServiceFactory
					.getService(EnterpriseUserConfigurationService.class);
		}
		return enterpriseUserConfigService;
	}

	/**
	 * get the workflow service.
	 * 
	 * @return the workflow service
	 */
	private WorkflowService getWorkflowService() {
		return MuleServiceFactory.getService(WorkflowService.class);
	}

	/**
	 * get the assignment service.
	 * 
	 * @return return the assignment service
	 */
	private AssignmentService getAssignmentService() {
		return MuleServiceFactory.getService(AssignmentService.class);
	}

	/**
	 * return the claim details service.
	 * 
	 * @return the claim details service
	 */
	private ClaimDetailsService getClaimDetailsService() {
		return MuleServiceFactory.getService(ClaimDetailsService.class);
	}

	/**  
	 * Search the Claim based on the criteria.
	 * 
	 * @param criteria
	 *            the claim search criteria
	 * @return SearchResultDTO the search results
	 * @throws ClaimSearchException
	 *             in case of any errors
	 */
	@Override
	@ServiceMethod(cache = false, validators = { "claims.validator.claimsSearch" })
	public Collection<ClaimSearchResultDTO> searchClaim(ClientClaimSearchCriteriaDTO criteria){
		SearchResultsDTO searchResultsDTO = new SearchResultsDTO();
		try {
			searchResultsDTO = this.getAllClaimService().searchClaim(criteria);
		}catch(ClaimSearchException e) {
			if (e.isTooManyResultFound()) {
				SystemOptionAssociationDTO option = MuleServiceFactory.getService(SystemOptionService.class).retrieveSystemOptionValue(ClaimsSearchConstants.MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE);
				throwTooManySearchResultsException(criteria, option.getLongValue());
			} else { // if it is unknown error
				throw new IIPCoreSystemException("Unable to call All System:"
						+ e.getMessage());
			}
		}

		validateSearchResults(criteria, searchResultsDTO);
		/*Collection<ClaimSearchResultDTO> results = searchResultsDTO.getSearchResults();
		
		for(ClaimSearchResultDTO claimSearchResultDTO:results){
			claimSearchResultDTO.setBlockFlag(processClaimBlockInformation(claimSearchResultDTO.getRecordId()));
		}*/
		
		return searchResultsDTO.getSearchResults();
	}

	/**
	 * throws too many search results exception.
	 * 
	 * @param criteria
	 *            criteria object
	 * @param maxResults
	 *            maximum results
	 */
	private void throwTooManySearchResultsException(ClaimSearchCriteriaDTO criteria, Long maxResults) {
		IIPCoreSystemException excp = new IIPCoreSystemException();
		excp.setErrorCode(ClaimsSearchConstants.ERROR_SEARCH_RESULT_EXCEED_LIMIT);
		Object[] errorArgs = { Long.valueOf(maxResults) };
		excp.setErrorArgs(errorArgs);
		excp.setPayload(criteria);

		IIPObjectError iipObjError = new IIPObjectError(
				ClaimSearchCriteriaDTO.class.getName(),
				"searchClaim",
				null,
				new String[] { ClaimsSearchConstants.ERROR_SEARCH_RESULT_EXCEED_LIMIT },
				errorArgs, MessageConstants.SEVERITY_INFO);
		excp.setError(iipObjError);

		throw excp;
	}
	
	/**
	 * validates search results.
	 * 
	 * @param criteria
	 *            criteria object
	 * @param searchResult
	 *            search results
	 */
	private void validateSearchResults(ClaimSearchCriteriaDTO criteria,
			SearchResultsDTO searchResult) {
		if (searchResult == null) {
			return;
		}
		Collection<ClaimSearchResultDTO> results = searchResult
				.getSearchResults();
		IIPObjectError iipObjError;
		if ((results == null) || (results.size() == 0)) {
			IIPCoreSystemException ex = new IIPCoreSystemException();
			ex.setErrorCode(ClaimsSearchConstants.ERROR_EMPTY_SEARCH_RESULT);
			ex.setPayload(criteria);

			iipObjError = new IIPObjectError(
					ClaimSearchCriteriaDTO.class.getName(),
					"search",
					null,
					new String[] { ClaimsSearchConstants.ERROR_EMPTY_SEARCH_RESULT },
					null, MessageConstants.SEVERITY_INFO);
			ex.setError(iipObjError);
			throw ex;
		}

		// Now check for results exceeding search return limit
		SystemOptionAssociationDTO option = MuleServiceFactory.getService(SystemOptionService.class).retrieveSystemOptionValue(ClaimsSearchConstants.MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE);

		long maxResults = option.getLongValue();
		
		searchResult.setMaximumResultsAllowed((int)maxResults);

		if (results.size() > maxResults) {
			throwTooManySearchResultsException(criteria, option.getLongValue());
		}
	}
	
	/**
	 * 
	 * @param claimId
	 *            the BlockSearch 
	 * @return the boolean with Block information
	 */
	private  boolean processClaimBlockInformation(Long claimId) {
		ClaimsBlockSearchCriteriaDTO blockSearchCriteriaDTO = new ClaimsBlockSearchCriteriaDTO();
		blockSearchCriteriaDTO.setClaimId(claimId);
		ClaimBlockInfoDTO claimBlockInfoDTO = getAllClaimService().retrieveClaimBlockInformation(blockSearchCriteriaDTO);
		return claimBlockInfoDTO.isBlockIndicator();
	}	
	
	/**
	 * Determines if the mocked service is returned or the real service.
	 * 
	 * @return ClaimsService the All Claims Service
	 */
	private CWSClaimService getAllClaimService() {
		if(allClaimService == null) {
			allClaimService = MuleServiceFactory.getService(CWSClaimService.class);
		}
		
		return allClaimService;
	}	

	/**
	 * retrieve the claim details.
	 * 
	 * @param request
	 *            the ClaimDetailRequestCriteria
	 * @throws MuleException
	 */
	@Override
	public ClaimImportCompositeDTO retrieveClaimDetails(
			ClaimDetailRequestCriteria request) throws MuleException {

		ClaimImportCompositeDTO returnObject = new ClaimImportCompositeDTO();

		ClaimDetailsResponseDTO clmDetailResponse = getClaimDetailsService()
				.retrieveClaimDetails(request);

		if (request.isReturnClaimPolicy()
				&& clmDetailResponse.getClaimDTO() != null) {
			claimDetailsHelper.processClaimPolicy(request.isReturnDetailsForClaimImport(), claimsPolicyCompositeHelper, clmDetailResponse);
		}

		if (request.isReturnClaimReservesWithCoverages()
				&& clmDetailResponse.getClaimReservesWithCoverages() != null) {
			for (CALClaimReserveDTO claimReserveDTO : clmDetailResponse.getClaimReservesWithCoverages()) {
				//Set the outstanding amount on the reserve
				Long reserveId = claimReserveDTO.getRecordId();
				if(reserveId != null) {
					BigDecimal outstandingAmount = MuleServiceFactory.getService(
							ClaimReserveService.class).calculateOutstandingReserve(reserveId);
					if (outstandingAmount == null){
						outstandingAmount = BigDecimal.ZERO;
					}
					claimReserveDTO.setExistingOutstandingAmount(outstandingAmount);
				}
				if (claimReserveDTO.getClmCoverageCode() == null) {
					if (claimReserveDTO.getCoverages() != null
							&& !claimReserveDTO.getCoverages().isEmpty()) {
						ClaimCoverageDTO claimCoverageDTO = claimReserveDTO.getCoverages().iterator().next();
						claimReserveDTO.setClmCoverageCode(claimCoverageDTO.getClaimCoverageCode());
					}
				}
				
				if (claimReserveDTO.getActionType() == null) {
					if (claimReserveDTO.getCalClaimReserveTransactionCollections() != null
							&& !claimReserveDTO.getCalClaimReserveTransactionCollections().isEmpty()) {
						CalClaimReserveTransactionDTO reserveTransactionDTO = claimReserveDTO
								.getCalClaimReserveTransactionCollections().iterator().next();
						claimReserveDTO.setReasonCode(reserveTransactionDTO.getReserveTransReasonCode());
						claimReserveDTO.setActionType(reserveTransactionDTO.getReserveTransTypeCode());
					}
				}
				
				//If the agreement data map has null values then remove it.
				if(claimReserveDTO.getAllAgreementData() != null && 
						!claimReserveDTO.getAllAgreementData().isEmpty()) {
					Set<String> keySet = claimReserveDTO.getAllAgreementData().keySet();
					Iterator<String> iter = keySet.iterator();
					while(iter.hasNext()) {
						Object key = iter.next();
						if(claimReserveDTO.getAllAgreementData().get(key) == null) {
							iter.remove();
						}
					}
				}
			}
		}

		if (request.isReturnClaimPayment()
				&& clmDetailResponse.getClaimPayments() != null) {
			for (ClaimPayment claimPayment : clmDetailResponse.getClaimPayments()) {
				BigDecimal totalPayment = new BigDecimal(0);
				if (claimPayment.getAuthorizationRequired() == null) {
					claimPayment.setAuthorizationRequired(false);
				}

				if (claimPayment.getClaimPayables() != null) {
					for (ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
						if (claimPayable.getTransactionAmount().intValue() < 0) {
							claimPayable.setTransactionAmount(claimPayable.getTransactionAmount().negate());
						}
						if (claimPayable.getSurchargeIndicator() == null) {
							claimPayable.setSurchargeIndicator(false);
						}
						totalPayment = totalPayment.add(claimPayable.getTransactionAmount());
					}
				}
				if (totalPayment.intValue() !=0 && claimPayment.getClaimPaymentDisbursements() != null) {
					for (ClaimPaymentDisbursement claimPaymentDisbursement : claimPayment.getClaimPaymentDisbursements()) {
						// percentage allocation
						int percentAllocation = (claimPaymentDisbursement.getDisbursementAmount().intValue() * 100)/ totalPayment.intValue();
						claimPaymentDisbursement.setPercentageAllocation(new BigDecimal(percentAllocation));
					}
				}
			}
		}



		// Since all of units details are being fetched at the composite layer,
		// we need to invoke retrieveUnit for each unit on a claim.
		if (request.isReturnUnits() && clmDetailResponse.getUnits() != null) {
			claimDetailsHelper.processClaimUnits(clmDetailResponse, request.isReturnDetailsForClaimImport());
		}

		BeanUtils.copyProperties(clmDetailResponse, returnObject);
		
		// get documents if requested
		if (request.isReturnDocuments()) {
			this.retrieveClaimDocumentDetails(returnObject);
		}
		
		if(request.isReturnWitnesses() && clmDetailResponse.getWitnesses() != null) {
			for(ClaimWitnessDTO clmWitnessDTO : clmDetailResponse.getWitnesses()) {
				getParticipationHelper().normalizeParticipationDTO(clmWitnessDTO);
			}
		}

		if(request.isReturnAuthorities() && clmDetailResponse.getAuthorities() != null) {
			for(ClaimParticipationAuthorityDTO clmAuthorityDTO : clmDetailResponse.getAuthorities()) {
				getParticipationHelper().normalizeParticipationDTO(clmAuthorityDTO);
			}
		}

		// get cases if requested
		if (request.isReturnCases()) {
			returnObject = this.retrieveClaimCasesDetails(returnObject);
		}

		// get work items if requested
		if (request.isReturnWorkItems()) {
			returnObject = this.retrieveClaimWorkItemDetails(returnObject);
		}

		// get assignments if requested
		if (request.isReturnAssignments()) {
			returnObject = this.retrieveClaimAssignmentsDetails(returnObject);
		}

		if (request.isReturnBills()) {
			Collection<ClaimBillDetailDTO> claimBills = returnObject.getBillDetails();

			Collection<ClientClaimBillDTO> clientClaimBills = new ArrayList<ClientClaimBillDTO>();

			if (claimBills != null) {
				for (ClaimBillDetailDTO billDetails : claimBills) {
					ClientClaimBillDTO clientClaimBillDTO = new ClientClaimBillDTO();

					BeanUtils.copyProperties(billDetails, clientClaimBillDTO);

					if (billDetails.getCurrentCycle() != null) {
						ClientClaimBillCycleDTO clientClaimBillCycle = new ClientClaimBillCycleDTO();
						BeanUtils.copyProperties(billDetails.getCurrentCycle(),clientClaimBillCycle);
						clientClaimBillDTO.setCurrentBillCycle(clientClaimBillCycle);
					}

					if (billDetails.getPreviousCycles() != null) {
						Collection<ClientClaimBillCycleDTO> previousBillCycle = new ArrayList<ClientClaimBillCycleDTO>();
						BeanUtils.copyProperties(billDetails.getPreviousCycles(),previousBillCycle);

						clientClaimBillDTO.setPreviousBillCycles(previousBillCycle);
					}

					billDetails.setCurrentCycle(null);
					billDetails.setPreviousCycles(null);

					clientClaimBillDTO.setCurrentCycle(null);
					clientClaimBillDTO.setPreviousCycles(null);

					clientClaimBills.add(clientClaimBillDTO);
				}
			}
			returnObject.setBillDetails(null);
			returnObject.setBills(clientClaimBills);
		}

		if (returnObject.getClaimDTO() != null
				&& returnObject.getClaimDTO().getClaimStatus() != null
				&& returnObject.getClaimDTO().getClaimStatus().getCurrent() != null) {
			setClaimStatusActionCode(returnObject);
		}
		
		// 11/21/2013 @GR - Retrieve Claim Blocks
		
		Collection<ClaimBlocksDTO> claimBlocks = MuleServiceFactory.getService(ClaimBlocksService.class).retrieveClaimBlocksByClaimId(returnObject.getClaimId());
		
		ClaimBlocksCompositeDTO claimBlocksComposite = new ClaimBlocksCompositeDTO();
		claimBlocksComposite.setClaimBlocks(claimBlocks);
		returnObject.setClaimBlocksCompositeDTO(claimBlocksComposite);		

		if (request.isReturnDetailsForClaimImport()) {
			claimDetailsHelper.processClaimDetailsForImport(request, returnObject);

			ClaimDetailLiteralDescriptionUtility utility = new ClaimDetailLiteralDescriptionUtility();
			checkFieldSerializable(returnObject, utility);
		}

		returnObject.setUseExternalIdentifier(true);

		// Retrieve Company ID information using Company LOB and set it in the
		// response
		ProductDefinitionInfo prodDefintion = new ProductDefinitionInfo();
		prodDefintion.setCompanyLOBId(returnObject.getClaimDTO().getCompanyLOBId());

		prodDefintion = getEnterpriseConfigService().getCompanyId(prodDefintion);

		returnObject.setCompanyId(prodDefintion.getCompanyId());

		// get literal descriptions, if requested
		if (request.isReturnLiteralDescription()) {
			returnObject = (ClaimImportCompositeDTO) new ClaimDetailLiteralDescriptionUtility()
					.updateCodeToDisplayValue(returnObject);
			
			//If flag to retrieve Literal Description is true then we need to retrieve Coverage Name and set in the map.
			//Literal Description logic returns Coverage Abbreviation name which is wrong.
			//Hard coding the key which is the variable name because even for using reflection we need to
			//hard code.
			if (request.isReturnClaimReservesWithCoverages()
					&& clmDetailResponse.getClaimReservesWithCoverages() != null) {
				Collection<CodeLookupBO> coverageCodeLookup = getClaimsAllClaimCoverageCodePojo().findCodeData("");
				for (CALClaimReserveDTO claimReserveDTO : clmDetailResponse.getClaimReservesWithCoverages()) {
					if (claimReserveDTO.getClmCoverageCode() != null) {
						if(coverageCodeLookup != null && !coverageCodeLookup.isEmpty()) {
							for(CodeLookupBO lookup : coverageCodeLookup) {
								if(lookup.getCode().equals(claimReserveDTO.getClmCoverageCode())) {
									claimReserveDTO.getLiteralDescriptionMap().put("clmCoverageCode", lookup.getValue().toString());
									if (claimReserveDTO.getCoverages() != null
											&& !claimReserveDTO.getCoverages().isEmpty()) {
										ClaimCoverageDTO claimCoverageDTO = claimReserveDTO.getCoverages().iterator().next();
										claimCoverageDTO.getLiteralDescriptionMap().put("claimCoverageCode", lookup.getValue().toString());
									}
									break;
								}
							}
						}
					}
				}
			}
		}

		return returnObject;
	}

	/**
	 * retrieve the work item details.
	 */
	@Override
	public ClaimImportCompositeDTO retrieveClaimWorkItemDetails(
			ClaimImportCompositeDTO request) {
		request.setWorkItems(new ArrayList<ClientWorkItemDTO>());
		WorkflowService wfService = this.getWorkflowService();

		// get the list of work items
		WorkItemSearchCriteriaDTO criteria = new WorkItemSearchCriteriaDTO();
		criteria.setWorkItemContextIdFilter(request.getClaimId());
		criteria.setWorkItemContextFilter(CLAIM_CONTEXT);
		SearchResultsDTO results = wfService.searchWorkItem(criteria);

		// get the details for each returned work item
		for (DataTransferObject dto : results.getSearchResults()) {
			if (dto instanceof WorkItemSearchResultDTO) {
				WorkItemSearchResultDTO searchResult = (WorkItemSearchResultDTO) dto;
				WorkItemDTO workItem = wfService.retrieveWorkItem(searchResult.getWorkItemId());
				WorkItemStatusCompositeDTO wiStatus = workItem.getWorkItemStatus();
				if (wiStatus != null) {
					WorkItemStatusDTO wis = wiStatus.getCurrent();
					if (wis != null && wis.getEffectiveDateTime() != null) {
						// create fresh instance of the date
						Date newDt = new Date();
						newDt.setTime(((Date) wis.getEffectiveDateTime()).getTime());
						wis.setEffectiveDateTime(newDt);
					}
				}
				ClientWorkItemDTO clientWI = new ClientWorkItemDTO(workItem);
				request.getWorkItems().add(clientWI);
			}
		}

		return request;
	}

	/**
	 * retrieve the assignment details.
	 */
	@Override
	public ClaimImportCompositeDTO retrieveClaimAssignmentsDetails(
			ClaimImportCompositeDTO request) {
		request.setClaimAssignments(new ArrayList<ClientImportAssignmentDTO>());
		AssignmentService assignmentService = this.getAssignmentService();

		// get the list of work items
		ViewAssignmentsCriteria criteria = new ViewAssignmentsCriteria();
		criteria.setAssignmentContextIdFilter(request.getClaimId());
		criteria.setAssignmentContextFilter(CLAIM_CONTEXT);
		
		SearchResultsDTO assignmentSearchResults = assignmentService.viewAssignments(criteria);
		
		Collection<ViewAssignmentResultDTO> results = assignmentSearchResults.getSearchResults();

		for (ViewAssignmentResultDTO viewAssignment : results) {

			AssignmentDTO assignmentDTO = assignmentService.retrieveAssignment(viewAssignment.getAssignmentId());
			ClientImportAssignmentDTO clientAssignment = new ClientImportAssignmentDTO();
			BeanUtils.copyProperties(assignmentDTO, clientAssignment);

			// set the user first name and last name
			if (assignmentDTO.getUserId() != null) {
				UserDetailsDTO udDto = getEnterpriseConfigService().retrieveUserDetails(assignmentDTO.getUserId());
				clientAssignment.setUserFirstName(udDto.getFirstName());
				clientAssignment.setUserMiddleName(udDto.getMiddleName());
				clientAssignment.setUserLastName(udDto.getLastName());
			}

			ClientAssignmentStatusCompositeDTO clientAssignmentStatusCompositeDTO = new ClientAssignmentStatusCompositeDTO(
					assignmentDTO.getAssignmentStatus());

			clientAssignment.setClientAssignmentStatus(clientAssignmentStatusCompositeDTO);
			clientAssignment.setAsgnStatus(clientAssignmentStatusCompositeDTO
					.getAssignmentStatusDTO().getAssignmentStatusCode());

			clientAssignment.setAssignmentStatus(null);
			
			if( clientAssignment.getUserId() != null) {
				// Retrieve Adjuster Fax Number
				UserCommInformationCompositeDTO userCommInformation = getEnterpriseUserConfigurationService()
						.retrieveUserCommunication(clientAssignment.getUserId());
	
				for (UserPhoneDTO userPhone : userCommInformation.getUserPhoneInfo()) {
					if(userPhone.isPrimaryIndicator()) {
						clientAssignment.setUserPhoneNumber(userPhone.getPhoneNo());
					}
					if (userPhone.getPhoneType().equals(EnterpriseConfigConstants.INTERACTION_CHANNEL_TYPE_CD_FAX)) {
						clientAssignment.setUserFaxNumber(userPhone.getPhoneNo());
						break;
					}
				}
	
				UserInfoDTO userInfo = getEnterpriseUserConfigurationService()
						.retrieveUser(clientAssignment.getUserId());
				if (userInfo.getSupervisorUserId() != null) {
					userCommInformation = getEnterpriseUserConfigurationService()
							.retrieveUserCommunication(userInfo.getSupervisorUserId());
					for (UserPhoneDTO userPhone : userCommInformation.getUserPhoneInfo()) {
						if (userPhone.getPhoneType().equals(EnterpriseConfigConstants.INTERACTION_CHANNEL_TYPE_CD_FAX)) {
							clientAssignment.setSupervisorFaxNumber(userPhone.getPhoneNo());
						}
						//else if (userPhone.getPhoneType().equals(EnterpriseConfigConstants.INTERACTION_CHANNEL_TYPE_CD_BUSN_PHONE)) {
						if(userPhone.isPrimaryIndicator()) {
							clientAssignment.setSupervisorPhoneNumber(userPhone.getPhoneNo());
						}
						//}
					}
				}
			}
			request.getClaimAssignments().add(clientAssignment);
		}

		return request;
	}

	/**
	 * Retrieve the Case details.
	 * 
	 * @param request
	 *            the ClaimDetailsResponseDTO
	 * @return update ClaimDetailsResponseDTO with documents
	 */
	public ClaimImportCompositeDTO retrieveClaimCasesDetails(
			ClaimImportCompositeDTO request) {

		CaseService caseService = MuleServiceFactory
				.getService(CaseService.class);
		Collection<CaseDTO> cases = caseService.retrieveCasesByAgreement(
				request.getClaimId(), ClaimsServiceConstants.CONTEXT_CLAIM);

		Collection<ClientCaseDTO> claimCases = new ArrayList<ClientCaseDTO>();

		for (CaseDTO caseDTO : cases) {
			ClientCaseDTO clientCaseDTO = new ClientCaseDTO();
			BeanUtils.copyProperties(caseDTO, clientCaseDTO);

			if (clientCaseDTO.getRecordSourceCode() == null) {
				clientCaseDTO.setRecordSourceCode(clientCaseDTO.getCaseContext().getRecordSourceCode());
			}

			ClientCaseStatusCompositeDTO clientCaseStatus = new ClientCaseStatusCompositeDTO(
					caseDTO.getCaseStatus());

			clientCaseDTO.setClientCaseStatus(clientCaseStatus);

			clientCaseDTO.setCaseStatus(null);

			Collection<ClientCaseIssueDTO> clientCaseIssueColl = new ArrayList<ClientCaseIssueDTO>();

			for (CaseIssueDTO caseIssue : caseDTO.getCaseIssueColl()) {
				ClientCaseIssueDTO clientCaseIssue = new ClientCaseIssueDTO();

				clientCaseIssue.setCaseIssueCode(caseIssue.getCaseIssueCode());
				clientCaseIssue.setCaseIssueSumDesc(caseIssue.getCaseIssueSumDesc());
				clientCaseIssue.setCaseIssuePrimaryIndicator(caseIssue.getCaseIssuePrimaryIndicator());

				clientCaseIssue.setClientCaseIssueStatusColl(new ClientCaseIssueStatusCompositeDTO(
								caseIssue.getCaseIssueStatusColl()));
				clientCaseIssueColl.add(clientCaseIssue);

				clientCaseIssue.setCaseIssueStatusColl(null);

			}

			clientCaseDTO.setClientCaseIssueColl(clientCaseIssueColl);
			clientCaseDTO.setCaseIssueColl(null);

			claimCases.add(clientCaseDTO);
		}

		request.setClaimCases(claimCases);

		return request;
	}

	/**
	 * Retrieve the Document details.
	 * 
	 * @param request
	 *            the ClaimDetailsResponseDTO
	 * @return update ClaimDetailsResponseDTO with documents
	 */
	public void retrieveClaimDocumentDetails(
			ClaimImportCompositeDTO request) {

		request.setClaimDocs(new ArrayList<ClientDocumentDTO>());

		DocumentService docService = MuleServiceFactory
				.getService(DocumentService.class);
		DocumentSearchCriteriaDTO docReq = new DocumentSearchCriteriaDTO();
		docReq.setDocumentContextIdFilter(request.getClaimId());
		docReq.setDocumentContextFilter(ClaimsServiceConstants.CONTEXT_CLAIM);
		// get the list of doc ids
		Collection<DocumentSearchResultDTO> docList = docService.searchDocument(docReq);

		// get the details for each note and add to composite object
		for (DocumentSearchResultDTO result : docList) {
			// Duplicate DocumentDTO in the response - Skip Expired Documents
			if (result.getDocumentStatusExpirationDate() != null) {
				continue;
			}
			DocumentDTO thisDoc = docService.retrieveDocument(result
					.getDocumentId());
			ClientDocumentDTO clientDoc = new ClientDocumentDTO();
			//Copy Properties
			BeanUtils.copyProperties(thisDoc, clientDoc);
			//Fetch Media File attributes if the document source is Media
			if(clientDoc.getSourceCode().equals(DocumentSourceCode.ELECTRONIC_MEDIA_FILE)){
				DocumentElectronicMediaFileDTO documentElectronicMediaFile = docService.retrieveElectronicMediaDocument(clientDoc.getRecordId());
				clientDoc.setDocumentFileName(documentElectronicMediaFile.getMediaFileName());
				clientDoc.setDocumentFileLocationCode(documentElectronicMediaFile.getDocumentSavedAs());
			}
			request.getClaimDocs().add(clientDoc);
		}
		
		request.setDocs(null);
	}

	/**
	 * @param helper
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.ClaimDetailsHelper")
	public void setClaimDetailsHelper(ClaimDetailsHelper helper) {
		claimDetailsHelper = helper;
	}

	/**
	 * @return Instance of Enterprise Config Service
	 */
	private EnterpriseConfigService getEnterpriseConfigService() {
		if (enterpriseConfigService == null) {
			return MuleServiceFactory.getService(EnterpriseConfigService.class);
		}
		return enterpriseConfigService;
	}

	/**
	 * @return clientClaimStatusDAO
	 */
	public ClientClaimStatusDAO getClientClaimStatusDAO() {
		return clientClaimStatusDAO;
	}

	/**
	 * @param value
	 *            Client Claim Status DAO to set
	 */
	@Inject(DaoInterface = "client.daointerface.clientClaimStatusDao")
	public void setClientClaimStatusDAO(ClientClaimStatusDAO value) {
		this.clientClaimStatusDAO = value;
	}

	/**
	 * Sets the Action Code for a status based on the current and the previous
	 * status using the Status transitions configured
	 * 
	 * @param returnObject
	 *            Claim Import Composite DTO
	 */
	private void setClaimStatusActionCode(ClaimImportCompositeDTO returnObject) {
		Collection<ClaimStatusTransitionDTO> claimStatusTransitions = getClientClaimStatusDAO()
				.retrieveAllStatusTransitionDetails();

		// Map to hold the Claim Status Transitions
		// Key --> statusFromCode + stateFromCode + statusToCode + stateToCode
		// Value --> actionCode
		Map<String, String> mClaimStatusTransitions = new HashMap<String, String>();

		for (ClaimStatusTransitionDTO claimStatusTransitionDTO : claimStatusTransitions) {
			StringBuffer keyBuffer = new StringBuffer();
			if (claimStatusTransitionDTO.getStatusFromCode() != null) {
				keyBuffer.append(claimStatusTransitionDTO.getStatusFromCode());
			}
			if (claimStatusTransitionDTO.getStateFromCode() != null) {
				keyBuffer.append(claimStatusTransitionDTO.getStateFromCode());
			}
			keyBuffer.append(claimStatusTransitionDTO.getStatusToCode());
			keyBuffer.append(claimStatusTransitionDTO.getStateToCode());

			mClaimStatusTransitions.put(keyBuffer.toString(),
					claimStatusTransitionDTO.getActionCode());
		}
		

		if ((returnObject.getClaimDTO().getClaimStatus().getExpired() != null
				&& returnObject.getClaimDTO().getClaimStatus().getExpired().isEmpty())
				|| returnObject.getClaimDTO().getClaimStatus().getExpired() == null) {
			
			ClaimStatusDTO claimStatusDTO = returnObject.getClaimDTO().getClaimStatus().getCurrent();

			//This shouldn't happen but in certain scenarios the status transition will not be available. 
			//To prevent XSD validation error defaulting it to "Initiate Claim"
			if(claimStatusDTO.getActionCode() == null) {
				claimStatusDTO.setActionCode(ClaimStatusConstants.ACTION_INITIATE_CLAIM);
			}
		} else if (returnObject.getClaimDTO().getClaimStatus().getExpired() != null
				&& !returnObject.getClaimDTO().getClaimStatus().getExpired().isEmpty()) {
			 Collections.sort((List<ClaimStatusDTO>)returnObject.getClaimDTO().getClaimStatus().getExpired(), new ClaimStatusIdSort());
			 
			if (returnObject.getClaimDTO().getClaimStatus().getExpired().size() == 1) {

				ClaimStatusDTO expiredClaimStatusDTO = returnObject.getClaimDTO().getClaimStatus().getExpired().iterator().next();

				expiredClaimStatusDTO.setActionCode(mClaimStatusTransitions.get(expiredClaimStatusDTO.getStatusCode()
								+ expiredClaimStatusDTO.getStateCode()));

				ClaimStatusDTO claimStatusDTO = returnObject.getClaimDTO().getClaimStatus().getCurrent();

				claimStatusDTO.setActionCode(mClaimStatusTransitions.get(expiredClaimStatusDTO.getStatusCode()
								+ expiredClaimStatusDTO.getStateCode()
								+ claimStatusDTO.getStatusCode()
								+ claimStatusDTO.getStateCode()));
				
				//This shouldn't happen but in certain scenarios the status transition will not be available. 
				//To prevent XSD validation error defaulting it to "Initiate Claim"
				if(claimStatusDTO.getActionCode() == null) {
					claimStatusDTO.setActionCode(ClaimStatusConstants.ACTION_INITIATE_CLAIM);
				}
			} else {
				List<ClaimStatusDTO> lstClaimStatuses = new ArrayList<ClaimStatusDTO>(returnObject.getClaimDTO().getClaimStatus().getExpired());
				for (int i = 0; i < lstClaimStatuses.size(); i++) {

					if (i == 0) {
						ClaimStatusDTO expiredClaimStatusDTO = lstClaimStatuses.get(i);

						expiredClaimStatusDTO.setActionCode(mClaimStatusTransitions
										.get(expiredClaimStatusDTO.getStatusCode()
												+ expiredClaimStatusDTO.getStateCode()));
					} else if (i == (lstClaimStatuses.size() - 1)) {
						ClaimStatusDTO expiredClaimStatusDTO = lstClaimStatuses.get(i);

						ClaimStatusDTO prevClaimStatusDTO = lstClaimStatuses.get(i - 1);

						expiredClaimStatusDTO.setActionCode(mClaimStatusTransitions
										.get(prevClaimStatusDTO.getStatusCode()
												+ prevClaimStatusDTO.getStateCode()
												+ expiredClaimStatusDTO.getStatusCode()
												+ expiredClaimStatusDTO.getStateCode()));

						//This shouldn't happen but in certain scenarios the status transition will not be available. 
						//To prevent XSD validation error defaulting it to "Initiate Claim"
						if(expiredClaimStatusDTO.getActionCode() == null) {
							expiredClaimStatusDTO.setActionCode(ClaimStatusConstants.ACTION_INITIATE_CLAIM);
						}

						ClaimStatusDTO claimStatusDTO = returnObject
								.getClaimDTO().getClaimStatus().getCurrent();
						claimStatusDTO.setActionCode(mClaimStatusTransitions
								.get(expiredClaimStatusDTO.getStatusCode()
										+ expiredClaimStatusDTO.getStateCode()
										+ claimStatusDTO.getStatusCode()
										+ claimStatusDTO.getStateCode()));
						//This shouldn't happen but in certain scenarios the status transition will not be available. 
						//To prevent XSD validation error defaulting it to "Initiate Claim"
						if(claimStatusDTO.getActionCode() == null) {
							claimStatusDTO.setActionCode(ClaimStatusConstants.ACTION_INITIATE_CLAIM);
						}
					} else {
						ClaimStatusDTO expiredClaimStatusDTO = lstClaimStatuses.get(i);

						ClaimStatusDTO prevClaimStatusDTO = lstClaimStatuses.get(i - 1);

						expiredClaimStatusDTO.setActionCode(mClaimStatusTransitions.get(prevClaimStatusDTO.getStatusCode()
												+ prevClaimStatusDTO.getStateCode()
												+ expiredClaimStatusDTO.getStatusCode()
												+ expiredClaimStatusDTO.getStateCode()));
						//This shouldn't happen but in certain scenarios the status transition will not be available. 
						//To prevent XSD validation error defaulting it to "Initiate Claim"
						if(expiredClaimStatusDTO.getActionCode() == null) {
							expiredClaimStatusDTO.setActionCode(ClaimStatusConstants.ACTION_INITIATE_CLAIM);
						}
					}
				}
			}
		}
	}

	/**
	 * Checks whether the attributes need to be set to null.
	 * @param dto
	 *            Object in which the attributes need to be checked
	 * @param utility
	 *            Utility class to get the DTO Property Value
	 * @return DTO
	 */
	public Serializable checkFieldSerializable(Serializable dto,
			ClaimDetailLiteralDescriptionUtility utility) {
		/*
		 * If the dto is null, don't check if attribute needs to be removed.
		 */
		if (dto == null) {
			return dto;
		}

		/*
		 * Even if there are no properties configured, the nested DTOs inside
		 * this DTO might have attributes that needs to be removed. The
		 * iteration into nested DTOs and collection of DTOs should always
		 * happen
		 */
		PropertyDescriptor[] dtoProperties = BeanUtils
				.getPropertyDescriptors(dto.getClass());
		for (PropertyDescriptor dtoProperty : dtoProperties) {
			if (Collection.class.isAssignableFrom(dtoProperty.getPropertyType())) {
				/*
				 * If the collection contains items that are instance of
				 * Serializable then check if any attributes needs to be removed
				 */
				checkFieldSerializableForList(
						(List<Serializable>) utility.getDTOPropertyValue(
								dtoProperty.getReadMethod(), dto, dtoProperty),
						utility);
			} else if (Validateable.class.isAssignableFrom(dtoProperty.getPropertyType())) {
				/*
				 * If the property is an instance of Serializable then check if
				 * any attributes needs to be removed
				 */
				checkFieldSerializable((Serializable) utility.getDTOPropertyValue(
								dtoProperty.getReadMethod(), dto, dtoProperty),utility);
			}
		}


		try {
			// By default set Record Id and Version to NULL
			// DK - remove the hard-coded set of these because it 
			//generates NoSuchMethod exceptions when applied to primitives.
			//This is redundant anyway since they are both included in the lstAttributesToRemove list
			//
			//PropertyUtils.setProperty(dto, "recordId", null);
			//PropertyUtils.setProperty(dto, "version", null);
			for (PropertyDescriptor dtoProperty : dtoProperties) {

				String methodName = dtoProperty.getName();
				if (lstAttributesToRemove.contains(methodName)) {
					if (!(dto instanceof PersonDTO) && !(dto instanceof OrganizationDTO)
							&& !(dto instanceof ClientPersonDTO) && !(dto instanceof ClientOrganizationDTO)) {
						PropertyUtils.setProperty(dto, methodName, null);
					}
				}
			}
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException encountered in ClientClaimDetailsCompositeServiceImpl.checkFieldSerializable", e);
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException encountered in ClientClaimDetailsCompositeServiceImpl.checkFieldSerializable", e);
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException encountered in ClientClaimDetailsCompositeServiceImpl.checkFieldSerializable", e);
		}

		return dto;
	}

	/**
	 * @param dtos
	 *            Object in which the attributes need to be check
	 * @param utility
	 *            Utility class to get the DTO Property Value
	 */
	private void checkFieldSerializableForList(List<Serializable> dtos,
			ClaimDetailLiteralDescriptionUtility utility) {

		if ((dtos == null) || (dtos.size() == 0)) {
			return;
		}

		for (Serializable dto : dtos) {
			if ((dto != null)) {
				checkFieldSerializable(dto, utility);
			}
		}
	}
	
	class ClaimStatusIdSort implements Comparator<ClaimStatusDTO>{
		
		public int compare(ClaimStatusDTO claimStatusDTO1, ClaimStatusDTO claimStatusDTO2) {
			if(claimStatusDTO1 != null && claimStatusDTO2 != null) {
				return claimStatusDTO1.getRecordId().compareTo(claimStatusDTO2.getRecordId());				
			}
			return 0;
		}
	}

	/**
	 * @return Claim Coverage Code Pojo
	 */
	public ClaimsAllClaimCoverageCodePojo getClaimsAllClaimCoverageCodePojo() {
		return claimsAllClaimCoverageCodePojo;
	}
	
	/**
	 * @param value Claim Coverage Code Pojo
	 */
	@Inject(PojoRef="claims.pojo.claimAllClaimCoverageCodePojo")
	public void setClaimsAllClaimCoverageCodePojo(ClaimsAllClaimCoverageCodePojo value) {
		claimsAllClaimCoverageCodePojo = value;
	}

	/**
	 * @return the participationHelper
	 */
	public ParticipationHelper getParticipationHelper() {
		return participationHelper;
	}

	/**
	 * @param value
	 *            the participationHelper to set
	 */
	@Inject(PojoRef = "com.stoneriver.iip.claims.composite.ParticipationHelper")
	public void setParticipationHelper(ParticipationHelper value) {
		this.participationHelper = value;
	}
}