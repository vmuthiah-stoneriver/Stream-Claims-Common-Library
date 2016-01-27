package com.client.iip.integration.claim;

import java.util.Collection;
import java.util.Iterator;

import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimChangeDateOfOccurenceDTO;
import com.stoneriver.iip.claims.CompanyDetailDTO;
import com.stoneriver.iip.claims.composite.search.ClaimsCompositeServiceImpl;
import com.stoneriver.iip.claims.policy.AllClaimsPolicyService;
import com.stoneriver.iip.claims.policy.ClaimPolicyDTO;
import com.stoneriver.iip.claims.search.ClaimDuplicatesSearchCriteriaDTO;
import com.stoneriver.iip.claims.search.ClearClaimSearchResult;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchResultDTO;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchService;

public class ClientClaimsCompositeServiceImpl extends ClaimsCompositeServiceImpl {
	
	
	/**
	 * {@inheritDoc}
	 */
	public ClaimChangeDateOfOccurenceDTO refreshPolicy(ClaimChangeDateOfOccurenceDTO changeDateOfOccurence) {
		
		MuleServiceFactory.getService(AllClaimsPolicyService.class).validateChangeDateOfOccurrence(changeDateOfOccurence);

		ClaimPolicyDTO currentPolicy = MuleServiceFactory.getService(AllClaimsPolicyService.class).retrieveClaimsPolicy(changeDateOfOccurence.getClaimRequest());

		if(!changeDateOfOccurence.getMinimalInternalClaim().isUnverified()) {
			CompanyDetailDTO companyDetails = getPolicyHelper().retrieveCompanyDetails(changeDateOfOccurence.getMinimalInternalClaim().getCompanyLOBId());
		
			com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchCriteriaDTO searchCriteria = new com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchCriteriaDTO();
			searchCriteria.setCompanyId(companyDetails.getCompanyId());
			searchCriteria.setCorpId(companyDetails.getCorporationId());
			searchCriteria.setPolicyNumber(currentPolicy.getPolicyNumber());
			searchCriteria.setLineOfBusiness(changeDateOfOccurence.getClaimRequest().getLobCode());
			//Fix @GR - 01/27/2016 - Use new loss date for policy search
			searchCriteria.setOccurrenceDate(changeDateOfOccurence.getNewOccurrenceDate());
		
			Collection<ClaimsPolicySearchResultDTO> result =  MuleServiceFactory.getService(ClaimsPolicySearchService.class).search(searchCriteria);
			
			//show no policy results.
			//result.clear();
			
			if(result != null && !result.isEmpty()) {
				changeDateOfOccurence.setNewPolicySearchResult(result.iterator().next());
				changeDateOfOccurence.setChangePolicy(!currentPolicy.getExternalSourceId().equals(changeDateOfOccurence.getNewPolicySearchResult().getPolicyExternalId()));
			}
		}
		if(changeDateOfOccurence.getMinimalInternalClaim().isUnverified() || changeDateOfOccurence.getNewPolicySearchResult() != null) {
			ClaimDuplicatesSearchCriteriaDTO duplicatesSearchCriteria = new ClaimDuplicatesSearchCriteriaDTO();
			duplicatesSearchCriteria.setLineOfBusinessCode(changeDateOfOccurence.getClaimRequest().getLobCode());
			duplicatesSearchCriteria.setOccurrenceDate(changeDateOfOccurence.getNewOccurrenceDate());
	
			if(changeDateOfOccurence.getMinimalInternalClaim().isUnverified()) {
				duplicatesSearchCriteria.setPolicyNumber(currentPolicy.getPolicyNumber());
				duplicatesSearchCriteria.setInsuredPartyId(MuleServiceFactory.getService(AllClaimsPolicyService.class)
															.retrieveClaimsPolicyInsured(changeDateOfOccurence.getClaimRequest().getClaimId()).getPartyId());
			} else {
				duplicatesSearchCriteria.setPolicyNumber(changeDateOfOccurence.getNewPolicySearchResult().getPolicyNumber());
				duplicatesSearchCriteria.setInsuredPartyId(currentPolicy.getInsuredPartyId());
			}
			
			Collection<ClearClaimSearchResult> clearClaimSearchResultCol = MuleServiceFactory.getService(CWSClaimService.class).searchClaimDuplicatesByRequest(duplicatesSearchCriteria);
	
			if (clearClaimSearchResultCol != null && !clearClaimSearchResultCol.isEmpty()) {
				for (Iterator<ClearClaimSearchResult> iterator = clearClaimSearchResultCol.iterator(); iterator.hasNext();) {
					ClearClaimSearchResult claimSearchResult = iterator.next();
					if(changeDateOfOccurence.getClaimRequest().getClaimId().equals(claimSearchResult.getRecordId())) {
						iterator.remove();
					} 
				}
			}

			changeDateOfOccurence.setClaimDuplicates(clearClaimSearchResultCol);
		}
		
		return changeDateOfOccurence;
	}	

}
