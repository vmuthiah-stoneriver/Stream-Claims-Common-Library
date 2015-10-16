package com.client.iip.integration.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.meta.annotation.ServiceMethod;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.stoneriver.iip.policy.mediation.adapter.PolicyMediationIntegrationAdapter;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchCriteriaDTO;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchResultDTO;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchService;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchServiceImpl;


public class ClientPolicySearchServiceImpl extends ClaimsPolicySearchServiceImpl {
	
	
	/**
	 * Implementation of the search method.
	 * @param criteria the search criteria.
	 * @return collection of results. runtime exception is thrown if there is a validation failure or a null result set
	 */
	@Override
	@ServiceMethod(validators="policyMediation.claims.SearchCriteriaValidator")
	public Collection<ClaimsPolicySearchResultDTO> search(
			ClaimsPolicySearchCriteriaDTO criteria) {
		
		ArrayList<ClaimsPolicySearchResultDTO> res = new ArrayList<ClaimsPolicySearchResultDTO>();
		
		Set<String> recordSrcKeys = getAdapters().keySet();
		for(String recSrc : recordSrcKeys) {
			PolicyMediationIntegrationAdapter adapter = getAdapters().get(recSrc);
			Collection<ClaimsPolicySearchResultDTO> adapterRes = adapter.search(criteria);
			if(adapterRes != null) {
				for(ClaimsPolicySearchResultDTO result : adapterRes) {
					/*
					 * Bug 10/13/2014 @GR - Use the Record Source code from the target policy system response.
					 */
					//result.setRecordSrcCd(recSrc);
					res.add(result);
				}
			}
		}
		
		if(res.size() >0) {
			return res;
		} else {
			IIPCoreSystemException ex =  new IIPCoreSystemException();
			IIPObjectError iipObjError = new IIPObjectError(
	    				ClaimsPolicySearchService.class.getName(), "search", null,
	    				new String[]{"SearchResultsEmpty"}, null, MessageConstants.SEVERITY_INFO);
			ex.setError(iipObjError);
			throw ex;

		}
	}
	
	/***
	 * {@inheritDoc}
	 */
	public Collection<ClaimsPolicySearchResultDTO> searchPoliciesWithSameInsured(ClaimsPolicySearchCriteriaDTO criteria) {
		ArrayList<ClaimsPolicySearchResultDTO> res = new ArrayList<ClaimsPolicySearchResultDTO>();

		Set<String> recordSrcKeys = getAdapters().keySet();
		for(String recSrc : recordSrcKeys) {
			PolicyMediationIntegrationAdapter adapter = getAdapters().get(recSrc);
			Collection<ClaimsPolicySearchResultDTO> adapterRes = adapter.retrievePoliciesWithSameInsured(criteria);
			if(adapterRes != null) {
				for(ClaimsPolicySearchResultDTO result : adapterRes) {
					/*
					 * Bug 10/13/2014 @GR - Use the Record Source code from the target policy system response.
					 */
					//result.setRecordSrcCd(recSrc);
					res.add(result);
				}
			}
		}
		
		if(res.size() >0) {
			return res;
		} else {
			IIPCoreSystemException ex =  new IIPCoreSystemException();
			IIPObjectError iipObjError = new IIPObjectError(
	    				ClaimsPolicySearchService.class.getName(), "search", null,
	    				new String[]{"SearchResultsEmpty"}, null, MessageConstants.SEVERITY_INFO);
			ex.setError(iipObjError);
			throw ex;

		}
		
	}	
	

}
