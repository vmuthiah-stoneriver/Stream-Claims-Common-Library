package com.client.iip.integration.claim.details;


import java.util.Collection;

import com.fiserv.isd.iip.bc.party.PartyConstants;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.composite.ParticipationHelper;
import com.stoneriver.iip.claims.composite.search.ClaimsPolicyCompositeHelper;
import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;
import com.stoneriver.iip.claims.policy.AllClaimsPolicyService;
import com.stoneriver.iip.claims.policy.ClaimPolicyDTO;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;

@Pojo(id = "com.client.iip.integration.claims.details.ClaimPolicyHelper")
public class ClaimPolicyHelper {
	private ClaimsPolicyCompositeHelper claimsPolicyCompositeHelper;
	private ParticipationHelper participationHelper;

	private AllClaimsPolicyService getAllClaimsPolicyService(){
		return MuleServiceFactory.getService(AllClaimsPolicyService.class);
	}
	private CWSClaimService getClaimService() {
		return MuleServiceFactory.getService(CWSClaimService.class);
	}

	/**
	 * @param value the policy Helper to set
	 */
	@Inject(PojoRef="com.stoneriver.iip.claims.composite.search.ClaimsPolicyCompositeHelper")
	public void setPolicyHelper(ClaimsPolicyCompositeHelper value) {
		this.claimsPolicyCompositeHelper = value;
	}
	
	/**
	 * .
	 * @return .
	 */
	public ClaimsPolicyCompositeHelper getPolicyHelper() {
		return claimsPolicyCompositeHelper;
	}
	
	/**
	 * @param value
	 *            the participationHelper to set
	 */
	@Inject(PojoRef = "com.stoneriver.iip.claims.composite.ParticipationHelper")
	public void setParticipationHelper(ParticipationHelper value) {
		this.participationHelper = value;
	}

	/**
	 * @return the participationHelper
	 */
	public ParticipationHelper getParticipationHelper() {
		return participationHelper;
	}
	
	/**
	 * retrieve policy related information.
	 * @param detailsCompositeDTO composite dto 
	 * @param claimParam claim information request.
	 */
	public ClaimDetailsCompositeDTO populatePolicyInformation(ClaimDetailsCompositeDTO detailsCompositeDTO,
			ClaimInformationRequestDTO claimParam) {
		ClaimPolicyDTO policy = getAllClaimsPolicyService().retrieveClaimsPolicy(claimParam); 
		detailsCompositeDTO.setPolicyDetails(policy);
		if(policy!=null){
			Collection<ClaimSearchResultDTO> claims = 
				getPolicyHelper().searchClaimsByPolicy(policy.getRecordId(), policy.getPolicyNumber(), 
						policy.getPolicyEffectiveDate(), policy.getPolicyEndDate());
			policy.setClaimsByPolicy(claims);
			
			//setting policy insured contact as claim contact
			policy.setInsuredContact(getClaimService()
					.retrieveClaimContactDetails(claimParam.getClaimId(), 
							claimParam.getLobCode()));

			getPolicyHelper().normalizePolicyDetails(detailsCompositeDTO.getPolicyDetails());
			detailsCompositeDTO.setCompanyDetailDTO(getPolicyHelper().retrieveCompanyDetails(detailsCompositeDTO.getPolicyDetails()));
			
			if (detailsCompositeDTO.getPolicyDetails().getInsuredContact() != null
					&& detailsCompositeDTO.getPolicyDetails()
							.getInsuredContact().getContact() != null
					&& detailsCompositeDTO.getPolicyDetails()
							.getInsuredContact().getContact().getRecordId() == null) {
				detailsCompositeDTO.getPolicyDetails()
						.getInsuredContact()
						.getContact()
						.setParticipation(
								getParticipationHelper().createMinimalParty(
										ClaimsServiceConstants.CONTEXT_CLAIM,
										PartyConstants.PARTY_TYPE_PERSON));
				detailsCompositeDTO.getPolicyDetails().setUpdated(false);
			}
			getPolicyHelper().setChangePolicyAllowedIndicator(policy, 
					claimParam.getClaimId());
		}
		
		return detailsCompositeDTO;
	}

}
