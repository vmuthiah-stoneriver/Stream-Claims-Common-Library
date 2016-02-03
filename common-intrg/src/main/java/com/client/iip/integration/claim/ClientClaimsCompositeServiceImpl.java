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
import com.stoneriver.iip.claims.policy.ClaimPolicyInformationDTO;
import com.stoneriver.iip.claims.refreshunit.RefreshUnitRequestDTO;
import com.stoneriver.iip.claims.search.ClaimDuplicatesSearchCriteriaDTO;
import com.stoneriver.iip.claims.search.ClearClaimSearchResult;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchResultDTO;
import com.stoneriver.iip.policy.mediation.claims.ClaimsPolicySearchService;
import com.stoneriver.iip.policy.mediation.policyimport.ListUnitsCriteriaDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportService;

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
	
	
	/**
	 * {@inheritDoc}
	 */
	public void changeDateOfOccurrence(ClaimChangeDateOfOccurenceDTO changeDateOfOccurrence) {
		if(!changeDateOfOccurrence.getMinimalInternalClaim().isUnverified()) {
			if(changeDateOfOccurrence.getChangePolicy() && changeDateOfOccurrence.getNewPolicySearchResult() != null) {
				ListUnitsCriteriaDTO listUnitsCriteria = new ListUnitsCriteriaDTO();
				listUnitsCriteria.setPolicyNumber(changeDateOfOccurrence.getNewPolicySearchResult().getPolicyNumber());
				listUnitsCriteria.setRecordSrcCd(changeDateOfOccurrence.getNewPolicySearchResult().getRecordSrcCd());
				listUnitsCriteria.setOccurrenceDate(changeDateOfOccurrence.getNewOccurrenceDate());
				
				RefreshUnitRequestDTO refreshUnitsRequest = new RefreshUnitRequestDTO();
				refreshUnitsRequest.setClaimPolicyInformation(new ClaimPolicyInformationDTO());
				refreshUnitsRequest.getClaimPolicyInformation().setClaimId(changeDateOfOccurrence.getClaimRequest().getClaimId());
				refreshUnitsRequest.getClaimPolicyInformation().setCmpyLobId(changeDateOfOccurrence.getNewPolicySearchResult().getCompanyLobId());
				refreshUnitsRequest.getClaimPolicyInformation().setLobCode(changeDateOfOccurrence.getNewPolicySearchResult().getLineOfBusiness());
				refreshUnitsRequest.getClaimPolicyInformation().setPolicyExternalId(changeDateOfOccurrence.getNewPolicySearchResult().getPolicyExternalId());
				refreshUnitsRequest.getClaimPolicyInformation().setPolicyId(changeDateOfOccurrence.getNewPolicySearchResult().getRecordId());
				refreshUnitsRequest.getClaimPolicyInformation().setPolicyNumber(changeDateOfOccurrence.getNewPolicySearchResult().getPolicyNumber());
				refreshUnitsRequest.getClaimPolicyInformation().setRecordSourceCode(changeDateOfOccurrence.getNewPolicySearchResult().getRecordSrcCd());
				refreshUnitsRequest.setMarkOldUnitsAsInactive(true);
				refreshUnitsRequest.setSelectedUnits(MuleServiceFactory.getService(PolicyImportService.class).listPolicyUnits(listUnitsCriteria));
				//DE5922: Setting loss date because we need to check the whether  the claim policy unit coverage is effective.
				//Fix @GR - 01/27/2016 - Use new loss date for policy search
				refreshUnitsRequest.setOccurrenceDate(changeDateOfOccurrence.getNewOccurrenceDate());
				
				getPolicyHelper().refreshUnits(refreshUnitsRequest);
			}
		}
		
		MuleServiceFactory.getService(CWSClaimService.class).changeDateOfOccurrence(changeDateOfOccurrence);
	
	}	

}
