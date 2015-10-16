package com.client.iip.integration.policy;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.MuleEndpointAdapter;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.stoneriver.iip.policy.mediation.adapter.PolicyMediationIntegrationAdapter;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchCriteriaDTO;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchResultDTO;
import com.stoneriver.iip.policy.mediation.policyimport.ListUnitsCriteriaDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyDetailsDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportRequestDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportStatusDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyRiskLocationDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyRiskLocationImportRequestDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyUnitDescDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyWCCoverageDTO;
import com.stoneriver.iip.policy.mediation.policyimport.ReImportRequestDTO;

/**
 * Implementation of the mediation adapter that uses Policy Star.
 * 
 * @author sudharsan.sriram
 * 
 */
public class PolicyPullMediationAdapterImpl extends MuleEndpointAdapter 
		implements	PolicyMediationIntegrationAdapter {

	private Logger logger = LoggerFactory.getLogger(PolicyPullMediationAdapterImpl.class);

	private final static String SEARCH_ENDPOINT = "policySearchEndpoint";

	private final static String RETRIEVE_DETAILS_ENDPOINT = "policyRetrieveDetailsEndpoint";

	private final static String IMPORT_POLICY_ENDPOINT = "policyImportEndpoint";
	
	private final static String REIMPORT_POLICY_ENDPOINT = "policyReImportEndpoint";
	
	private final static String LIST_UNITS_ENDPOINT = "policyListUnitsEndpoint";
	
	private PolicyImportProcessor policyImportProcessor;
	

	/**
	 * @param policyImportProcessor the policyImportProcessor to set
	 */
	public void setPolicyImportProcessor(PolicyImportProcessor policyImportProcessor) {
		this.policyImportProcessor = policyImportProcessor;
	}

	/**
	 * Implementation for searching policies in Policy Pull.
	 * 
	 * @param criteria
	 *            search criteria
	 * 
	 * @return collection of search results if found. returns null if not found.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<ClaimsPolicySearchResultDTO> search(
			ClaimsPolicySearchCriteriaDTO criteria) {

		logger.info("Entering Policy Search with Criteria: {} ", criteria);

		Collection<ClaimsPolicySearchResultDTO> searchResults = null;
		Object payload = null;
		try {
			payload = sendSyncEvent(SEARCH_ENDPOINT, criteria);
		}
		catch (Throwable e) {
			logger.error("Exception Occurred Calling Policy Search Results: ", e);
			Throwable cause = e.getCause();
			logger.error("Exception Cause Occurred Calling Policy Search Results: ", cause);
			throw new IIPCoreSystemException(e);
		}

		if(payload!= null){
			if(payload instanceof IIPCoreSystemException){
				//This flow indicates exception was thrown by system
				IIPCoreSystemException coreSystemException = (IIPCoreSystemException)payload;
				logger.error("Exception occurred in Policy Pull search", coreSystemException);
				throw coreSystemException;
			}else if(payload instanceof Collection){
				//This flow indicates response XML validation error on the stream side
				for(Object obj:(Collection<?>)payload){
					if (obj instanceof IIPErrorResponse){
						Collection<IIPObjectError> iipObjectErrors = ((IIPErrorResponse) obj).getFormattedErrors();
						if(iipObjectErrors != null && !iipObjectErrors.isEmpty()){
							for (IIPObjectError ioe : iipObjectErrors) {
								String errorMessage = ioe.getFormattedMessage();
								logger.error("Exception occurred in Policy Pull: ", errorMessage);
								IIPCoreSystemException ex = new IIPCoreSystemException("Exception occurred in Policy Interface: " + errorMessage);
								throw ex;
							}
						}					
					}else if (obj instanceof ClaimsPolicySearchResultDTO){
						searchResults = (Collection<ClaimsPolicySearchResultDTO>) payload;
						break;
					}
				}

			}else{
				throw new IIPCoreSystemException(
						"Policy Search process could not be completed. Please check your system logs.");
			}
		}

		if (CollectionUtils.isEmpty(searchResults)) {
			logger.warn("Policy Search Results returned no results");
		} else {
			logger.info("Policy Search found size - {} : results: {}",  searchResults.size(), searchResults.toString());
		}

		return searchResults;
	}

	/**
	 * Implementation of the the retrieve details method. this will synthesize a
	 * detail entry from the fully specified policy.
	 * 
	 * @param req
	 *            populated policy import request.
	 * @return populated policy details object, or null if not found (shouldn't
	 *         happen in a normal case)
	 */
	@Override
	public PolicyDetailsDTO retrievePolicyDetails(PolicyImportRequestDTO req) {

		logger.info("Entering Policy Retrieve Policy Details with Request: {} ", req.toString());

		PolicyDetailsDTO policyDetails = null;
		Object payload = null;
		try {
			payload = sendSyncEvent(RETRIEVE_DETAILS_ENDPOINT, req);
		}
		catch (Throwable e) {
			logger.error("Exception Occurred Calling Retrieve Policy Details: ", e);
			Throwable cause = e.getCause();
			logger.error("Exception Cause Occurred Calling Retrieve Policy Details: ", cause);
			throw new IIPCoreSystemException(e);
		}

		if(payload!= null){
			if(payload instanceof IIPCoreSystemException){
				//This flow indicates exception was thrown by system
				IIPCoreSystemException coreSystemException = (IIPCoreSystemException)payload;
				logger.error("Exception occured in Policy Pull retrievePolicyDetails");
				throw coreSystemException;
			}else if(payload instanceof Collection){
				//This flow indicates response XML validation error on the stream side
				for(Object obj:(Collection<?>)payload){
					if (obj instanceof IIPErrorResponse){
						Collection<IIPObjectError> iipObjectErrors = ((IIPErrorResponse) obj).getFormattedErrors();
						if(iipObjectErrors != null && !iipObjectErrors.isEmpty()){
							for (IIPObjectError ioe : iipObjectErrors) {
								String errorMessage = ioe.getFormattedMessage();
								logger.error("Exception occurred in Policy Pull: ", errorMessage);
								IIPCoreSystemException ex = new IIPCoreSystemException("Exception occurred in Policy Interface: " + errorMessage);
								throw ex;
							}
						}					
					}
				}
			}else if(payload instanceof PolicyDetailsDTO){
				//This flow indicates policy details were successfully returned by Policy Pull 
				policyDetails = (PolicyDetailsDTO) payload;
			}else{
				throw new IIPCoreSystemException(
				"Policy details retrieval process could not be completed. Please check your system logs.");
			}
		}

		if (policyDetails == null) {
			logger.warn("Retrieve Policy Details returned no results");
		} else {
			logger.info("Retrieve Policy Details returned results: {} ", policyDetails);
		}

		return policyDetails;
	}

	/**
	 * Retrieve full policy as though imported. This will return the policy data
	 * as loaded from XML for the specified policy.
	 * 
	 * @param stat
	 *            populated import status object.
	 * @return same status object, but with the policy data filled in.
	 */
	@Override
	public PolicyImportStatusDTO importPolicy(PolicyImportStatusDTO stat) {
		PolicyImportRequestDTO req = stat.getRequest();

		//PolicyImportProcessor policyImportProcessor = new PolicyImportProcessor();
		logger.info("Entering Import Policy with Request: {} ", req.toString());

		ClientPolicyImportWrapperDTO policyImportWrapper = null;
		PolicyImportStatusDTO importStatus = null;
		Object payload = null;
		try {
			payload = sendSyncEvent(IMPORT_POLICY_ENDPOINT, req);
		}
		catch (Throwable e) {
			logger.error("Exception Occurred Calling Import Policy: ", e);
			Throwable cause = e.getCause();
			logger.error("Exception Cause Occurred Calling Import Policy: ", cause);
			throw new IIPCoreSystemException(e);
		}

		if(payload!= null){
			if(payload instanceof IIPCoreSystemException){
				//This flow indicates exception was thrown by system
				IIPCoreSystemException coreSystemException = (IIPCoreSystemException)payload;
				logger.error("Exception occurred in Policy Pull importPolicy");
				throw coreSystemException;
			}else if(payload instanceof Collection){
				//This flow indicates response XML validation error on the stream side
				for(Object obj:(Collection<?>)payload){
					if (obj instanceof IIPErrorResponse){
						Collection<IIPObjectError> iipObjectErrors = ((IIPErrorResponse) obj).getFormattedErrors();
						if(iipObjectErrors != null && !iipObjectErrors.isEmpty()){
							for (IIPObjectError ioe : iipObjectErrors) {
								String errorMessage = ioe.getFormattedMessage();
								logger.error("Exception occurred in Policy Pull: ", errorMessage);
								IIPCoreSystemException ex = new IIPCoreSystemException("Exception occurred in Policy Interface: " + errorMessage);
								throw ex;
							}
						}				
					}
				}
			}else if (payload instanceof ClientPolicyImportWrapperDTO){
				//This flow indicates policy import was successful from Policy Pull 
				policyImportWrapper = (ClientPolicyImportWrapperDTO) payload;
				try {
					importStatus = policyImportProcessor.processPolicy(policyImportWrapper);
				} catch (Exception e) {
					logger.error("Exception encountered in policyImportProcessor.processPolicy", e);
					throw new IIPCoreSystemException(e);
				}
			}else{
				throw new IIPCoreSystemException(
						"Policy Import process could not be completed. Please check your system logs.");
			}
		}

		if (importStatus == null) {
			logger.warn("Import Policy returned no results");
			throw new IIPCoreSystemException("Import Policy returned no results");
		} else {
			logger.info("Import Policy returned results: {} ", importStatus);
		}

		return importStatus;
	}

	/**
	 * Service to retrieve the list of units on the policy. Uses a criteria
	 * object, although currently there is no filtering done and it just returns
	 * everything on the Policy.
	 * 
	 * @param req
	 *            request describing the policy.
	 * @return list of matching units.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<PolicyUnitDescDTO> listPolicyUnits(
			ListUnitsCriteriaDTO req) {

		logger.info("Entering List Policy Units with Request: {} ", req);

		Object payload = null;
		Collection<PolicyUnitDescDTO> policyUnits = null;
		try {
			payload = sendSyncEvent(LIST_UNITS_ENDPOINT, req);
		} catch (Throwable e) {
			logger.error("Exception Occurred Calling Policy List Units: ", e);
			Throwable cause = e.getCause();
			logger.error("Exception Cause Occurred Calling Policy List Units: ", cause);
			throw new IIPCoreSystemException(e);
		}

		if (payload != null) {
			if (payload instanceof IIPCoreSystemException) {
				// This flow indicates exception was thrown by system
				IIPCoreSystemException coreSystemException = (IIPCoreSystemException) payload;
				logger.error("Exception occurred in Policy Pull Retrieving List Units");
				throw coreSystemException;
			}else if(payload instanceof Collection){
				//This flow indicates response XML validation error on the stream side
				for(Object obj:(Collection<?>)payload){
					if (obj instanceof IIPErrorResponse){
						Collection<IIPObjectError> iipObjectErrors = ((IIPErrorResponse) obj).getFormattedErrors();
						if(iipObjectErrors != null && !iipObjectErrors.isEmpty()){
							for (IIPObjectError ioe : iipObjectErrors) {
								String errorMessage = ioe.getFormattedMessage();
								logger.error("Exception occurred in Policy Pull: ", errorMessage);
								IIPCoreSystemException ex = new IIPCoreSystemException("Exception occurred in Policy Interface: " + errorMessage);
								throw ex;
							}
						}					
					}else if (obj instanceof PolicyUnitDescDTO){
						policyUnits = (Collection<PolicyUnitDescDTO>) payload;
						break;
					}
				}

			}else {
				throw new IIPCoreSystemException(
						"Policy Units could not be retrieved. Please check your system logs.");
			}
		}

		if (CollectionUtils.isEmpty(policyUnits)) {
			logger.warn("Policy List Units returned no results");
			throw new IIPCoreSystemException(
					"Policy List Units returned no results");
		} else {
			logger.info("Policy List Units returned size - {} : results {} ", policyUnits.size(), policyUnits.toString());
		}

		return policyUnits;

	}

	/**
	 * Perform a subsequent import on a policy that has been previously
	 * imported. This is generally used to do 2 things;
	 * 
	 * - Refresh the data associated with existing units on a claim
	 * - Retrieve new or additional units that may have been added subsequent to the initial import.
	 * 
	 * @param req
	 *            import request, including a list of units to include.
	 * @return imported policy. similar to standard import return, but only
	 *         including specified units.
	 */
	@Override
	public PolicyImportStatusDTO reImportPolicy(ReImportRequestDTO req) {
		
		//PolicyImportProcessor policyImportProcessor = new PolicyImportProcessor();
		logger.info("Entering Re-Import Policy with Request: {} ", req);
		//Convert Flex Collection to plain java collection type.
		Collection<PolicyUnitDescDTO> units = new ArrayList<PolicyUnitDescDTO>();
		for(PolicyUnitDescDTO unitDescDTO:req.getImportUnits()){
			units.add(unitDescDTO);
		}
		
    	req.setImportUnits(units);

		ClientPolicyImportWrapperDTO policyImportWrapper = null;
		PolicyImportStatusDTO importStatus = null;
		Object payload = null;
		try {
			payload = sendSyncEvent(REIMPORT_POLICY_ENDPOINT, req);
		}
		catch (Throwable e) {
			logger.error("Exception Occurred Calling Re-Import Policy: ", e);
			Throwable cause = e.getCause();
			logger.error("Exception Cause Occurred Calling Re-Import Policy: ", cause);
			throw new IIPCoreSystemException(e);
		}

		if(payload!= null){
			if(payload instanceof IIPCoreSystemException){
				//This flow indicates exception was thrown by system
				IIPCoreSystemException coreSystemException = (IIPCoreSystemException)payload;
				logger.error("Exception occurred in Policy Pull reImportPolicy");
				throw coreSystemException;
			}else if(payload instanceof Collection){
				//This flow indicates response XML validation error on the stream side
				for(Object obj:(Collection<?>)payload){
					if (obj instanceof IIPErrorResponse){
						Collection<IIPObjectError> iipObjectErrors = ((IIPErrorResponse) obj).getFormattedErrors();
						if(iipObjectErrors != null && !iipObjectErrors.isEmpty()){
							for (IIPObjectError ioe : iipObjectErrors) {
								String errorMessage = ioe.getFormattedMessage();
								logger.error("Exception occurred in Policy Pull: ", errorMessage);
								IIPCoreSystemException ex = new IIPCoreSystemException("Exception occurred in Policy Interface: " + errorMessage);
								throw ex;
							}
						}				
					}
				}
			}else if (payload instanceof ClientPolicyImportWrapperDTO){
				//This flow indicates policy import was successful from Policy Pull 
				policyImportWrapper = (ClientPolicyImportWrapperDTO) payload;
				try {
					importStatus = policyImportProcessor.processPolicy(policyImportWrapper);
				} catch (Exception e) {
					logger.error("Exception encountered in policyImportProcessor.processPolicy", e);					
					throw new IIPCoreSystemException(e);
				}
			}else{
				throw new IIPCoreSystemException(
						"Policy Re-Import process could not be completed. Please check your system logs.");
			}
		}

		if (importStatus == null) {
			logger.warn("Import Policy returned no results");
			throw new IIPCoreSystemException("Re-Import Policy returned no results");
		} else {
			logger.info("Re-Import Policy returned results: {} ", importStatus.toString());
		}

		return importStatus;
	}
	
	
	/**
	 * Method to change the policy status.
	 * @param companyLobId Long
	 * @param policyPeriod PolicyPeriodDataRplDTO
	 * @param policyStatusCode String
	 */
	/*public void sendPolicyStatusNotificationAsync(BillingPolicyStatusDTO criteria){
		//Dummy impl -- To be uncommented for 8.105
		
	}*/

	/**
	 * This method can be used to retrieve the Risk Locations that are
	 * associated with the Policy. The Policy is searched / retrieved based on
	 * the Policy Number. The Risk Location will also have the associated
	 * coverages (if any).
	 * 
	 * @param req
	 *            Policy import request. the policy number needs to be populated
	 *            for the Policy to be found.
	 * @return Collection of PolicyRiskLocationDTO. null will be returned if the
	 *         Policy cannot be found or if there are no risk locations for that
	 *         Policy.
	 */
	@Override
	public Collection<PolicyRiskLocationDTO> retrieveRiskLocationsForPolicy(
			PolicyImportRequestDTO req) {
		return null;
	}

	/**
	 * This method can be used to retrieve the Coverages associated with a Risk
	 * Locations that are associated with the Policy. The Policy is searched /
	 * retrieved based on the Policy Number. The Risk Location will be
	 * identified on the basis of the Risk Location name passed in . If the Risk
	 * Location name is null or blank, then all the distinct Coverages
	 * associated with all the Risk Locations will be returned. If the Risk
	 * Location name is passed in (not null or blank), the coverages associated
	 * with the identified risk Location will be returned.
	 * 
	 * @param req
	 *            Policy Risk Location import request. The policy number needs
	 *            to be populated for the Policy to be found. The risk location
	 *            name needs to be set as well
	 * @return Collection of PolicyWCCoverageDTO. null will be returned if the
	 *         Policy cannot be found or if there are no risk locations for that
	 *         Policy or if there are no coverages for that risk location.
	 */
	@Override
	public Collection<PolicyWCCoverageDTO> retrieveWCCoveragesForRiskLocation(
			PolicyRiskLocationImportRequestDTO req) {
		return null;
	}

	/**
	 * This method retrieves the Policies for the Insured Participant criteria
	 * passed in which meet the following conditions Insured Name = Insured
	 * Participant Name provided in the criteria & Insured Tax Id = Insured
	 * Participant Tax Id provided in the criteria & DOC falls between the
	 * Policy Effective Date and Policy End Date. Only LOB = WC is supported for
	 * now The Tax Id is optional in the sense that if the Tax Id is passed in
	 * as null - then only the Policies that have Insured Participants that do
	 * not have a Tx Id will be retrieved. This method emulates the search, but
	 * uses a different predicate to account for the Insured Participant
	 * criteria.
	 * 
	 * @param criteria
	 *            ClaimsPolicySearchCriteriaDTO.
	 * @return Collection of ClaimsPolicySearchResultDTO [Policy summary data]
	 */
	@Override
	public Collection<ClaimsPolicySearchResultDTO> retrievePoliciesWithSameInsured(
			ClaimsPolicySearchCriteriaDTO criteria) {
		return null;
	}

	/**
	 * Record source tracking method.
	 * 
	 * @return configured record src value for this implementation.
	 */
	@Override
	public String getRecordSrc() {
		return "pstar";
	}

}
