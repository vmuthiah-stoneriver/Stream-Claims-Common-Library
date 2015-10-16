package com.client.iip.integration.policy;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.policy.mediation.adapter.PolicyMediationIntegrationAdapter;
import com.stoneriver.iip.policy.mediation.policyimport.ListUnitsCriteriaDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyDetailsDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportRequestDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportServiceImpl;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportStatusDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyRiskLocationDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyRiskLocationImportRequestDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyUnitDescDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyWCCoverageDTO;
import com.stoneriver.iip.policy.mediation.policyimport.ReImportRequestDTO;

public class ClientPolicyImportServiceImpl extends PolicyImportServiceImpl {
	
	private Logger logger = LoggerFactory.getLogger(ClientPolicyImportServiceImpl.class);
	
	public PolicyMediationIntegrationAdapter getAdapter(){
		PolicyMediationIntegrationAdapter adapter = null;
		Set<String> recordSrcKeys = getAdapters().keySet();
		for(String recSrc : recordSrcKeys) {
			adapter = getAdapters().get(recSrc);
		}
		return adapter;
		
	}
	
	/**
	 * return a mid-level summary of the policy.
	 * @param req the policy import request.
	 * @return policy summary detail dto.
	 */
	@Override
	public PolicyDetailsDTO retrievePolicyDetails(PolicyImportRequestDTO req) {
		String recordSrc =req.getRecordSrcCd(); 
		if(recordSrc == null) {
			//should be a validation
			return null;
		}
		
		PolicyMediationIntegrationAdapter adapter = getAdapter();
		if(adapter == null) {
			//runtime exception?
			return null;
		}
		PolicyDetailsDTO returnDetails = adapter.retrievePolicyDetails(req);
		if(returnDetails == null) {
			logger.warn("Null return from adapter for recSrc={} for retrievePolicyDetails",getAdapter());
			return null;
		}
		return returnDetails;
		
	}

	/**
	 * import the policy, including any parties associated with the policy.
	 * This will be subject to revision, as the process for handling party duplicate checking
	 * needs to be identified.
	 * @param req policy import request object.
	 * @return nothing at the moment.
	 */
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public PolicyImportStatusDTO importPolicy(PolicyImportStatusDTO req) {
		PolicyImportRequestDTO impReq = req.getRequest();
		String recordSrc =impReq.getRecordSrcCd(); 
		if(recordSrc == null) {
			//should be a validation
			return null;
		}
		logger.info("Importing policy {} from recordSrc={}",impReq.getPolicyNumber(),recordSrc);
		PolicyMediationIntegrationAdapter adapter = getAdapter();
		if(adapter == null) {
			//runtime exception?
			return null;
		}
		
		PolicyImportStatusDTO returnDTO = adapter.importPolicy(req);
		if(returnDTO == null) {
			logger.warn("Null return from adapter for recSrc={} for importPolicy",getAdapter());
			return null;
		}		
		returnDTO.getPolicy().setRecordSourceCode(recordSrc);

		return returnDTO;
	}
	
	/**
	 * This method can be used to retrieve the Risk Locations that are associated with 
	 * the Policy.
	 * The Policy is searched / retrieved based on the Policy Number. 
	 * The Risk Location will also have the associated coverages (if any).
	 * 
	 * @param req Policy import request. the policy number needs to be populated for the Policy to be found.
	 * 									 the record source needs to be populated and is mandatory.
	 * @return Collection of PolicyRiskLocationDTO. null will be returned if the Policy cannot be found or if there
	 * are no risk locations for that Policy.
	 */		
	@Override
	public Collection<PolicyRiskLocationDTO> retrieveRiskLocationsForPolicy(PolicyImportRequestDTO req) {
		String recordSrc =req.getRecordSrcCd(); 
		if(recordSrc == null) {
			throw new IIPCoreSystemException("The Record Source is necessary for the Mediation API's . Add the Record Source to the PolicyImportRequestDTO");
		}
		
		PolicyMediationIntegrationAdapter adapter = getAdapter();
		if(adapter == null) {
			throw new IIPCoreSystemException("Could not find an appropriate adapter for the Record Source ." + recordSrc + ". Check the record source configuration for the Mediation adapters.");
		}
		
		Collection<PolicyRiskLocationDTO> returnDetails = null;
		if(req.getPolicyNumber() != null){
			//Get the Risk Locations
			returnDetails = adapter.retrieveRiskLocationsForPolicy(req);
			if(returnDetails == null) {
				logger.warn("Null return from adapter for recSrc={} for retrieveRiskLocationsForPolicy. No Risk Locations available for that Policy.",recordSrc);			
			}
		}
		
		return returnDetails;
	}
	
	/***
	 * {@inheritDoc}
	 */
	@Override
	public Collection<PolicyWCCoverageDTO> retrieveWCCoveragesForRiskLocation(PolicyRiskLocationImportRequestDTO req) {
		String recordSrc =req.getRecordSrcCd(); 
		if(recordSrc == null) {
			throw new IIPCoreSystemException("The Record Source is necessary for the Mediation API's . Add the Record Source to the PolicyImportRequestDTO");
		}
		
		PolicyMediationIntegrationAdapter adapter = getAdapter();
		if(adapter == null) {
			throw new IIPCoreSystemException("Could not find an appropriate adapter for the Record Source ." + recordSrc + ". Check the record source configuration for the Mediation adapters.");
		}
		
		Collection<PolicyWCCoverageDTO> returnDetails = null;
		if(req.getPolicyNumber() != null){
			//Get the Coverages for the Risk Location passed in 
			returnDetails = adapter.retrieveWCCoveragesForRiskLocation(req);
			if(returnDetails == null) {
				logger.warn("Null return from adapter for recSrc={} for retrieveWCCoveragesForRiskLocation. No Coverages available for Risk Location : {}",recordSrc, req.getRiskLocationName());			
			}
		}
		
		return returnDetails;
	}

	/**
	 * service to retrieve the list of units on the policy. Uses a criteria
	 * object, although currently there is no filtering done and it just returns
	 * everything on the Policy.
	 * @param req request describing the policy.
	 * @return list of matching units.
	 */
	public Collection<PolicyUnitDescDTO> listPolicyUnits(ListUnitsCriteriaDTO req) {
		String recordSrc =req.getRecordSrcCd(); 
		if(recordSrc == null) {
			//should be a validation
			return null;
		}
		
		PolicyMediationIntegrationAdapter adapter = getAdapter();
		if(adapter == null) {
			//runtime exception?
			return null;
		}
		Collection<PolicyUnitDescDTO> returnUnits = adapter.listPolicyUnits(req);
		if(returnUnits == null) {
			logger.warn("Null return from adapter for recSrc={} for retrievePolicyDetails",recordSrc);
			return null;
		}
		return returnUnits;
	
	}
	
	/**
	 * perform a subsequent import on a policy that has been previously imported. this
	 * is generally used to do 2 things
	 * - refresh the data associated with existing units on a claim
	 * - retrieve new or additional units that may have been added subsequent to the initial import.
	 * @param req  import request, including a list of units to include.
	 * @return imported policy. similar to standard import return, but only including specified units.
	 */
	public PolicyImportStatusDTO reImportPolicy(ReImportRequestDTO req) {
		String recordSrc =req.getRecordSrcCd(); 
		if(recordSrc == null) {
			//should be a validation
			return null;
		}
		
		PolicyMediationIntegrationAdapter adapter = getAdapter();
		if(adapter == null) {
			//runtime exception?
			return null;
		}
		PolicyImportStatusDTO returnPol = adapter.reImportPolicy(req);
		if(returnPol == null) {
			logger.warn("Null return from adapter for recSrc={} for retrievePolicyDetails",recordSrc);
			return null;
		}
		return returnPol;
		
	}	
	

}
