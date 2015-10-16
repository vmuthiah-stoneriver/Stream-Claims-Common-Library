package com.client.iip.integration.claim.details;


import java.util.Collection;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.RelatedClaimDTO;
import com.stoneriver.iip.claims.RelatedClaimService;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.api.UserDetailsDTO;

@Pojo(id = "com.client.iip.integration.claims.details.RelatedClaimHelper")
public class RelatedClaimHelper {
	private RelatedClaimService relatedClaimService;

	/**
	 * @return RelatedClaimService
	 */
	public RelatedClaimService getRelatedClaimService() {
		if (relatedClaimService == null) {
			relatedClaimService = MuleServiceFactory
					.getService(RelatedClaimService.class);
		}
		return relatedClaimService;
	}

	private CWSClaimService getClaimService() {
		return MuleServiceFactory.getService(CWSClaimService.class);
	}

	/**
	 * Updates Created User Name, Claim Number and Insured Name in
	 * RelatedClaimDTO.
	 * 
	 * @param relatedClaimDTOs
	 *            Related Claims to be updated
	 * @param retrieveClaimInformation
	 *            flag whether indicating whether to return Claim Number and
	 *            Insured information.
	 * @return Collection of Related Claims
	 */
	public Collection<RelatedClaimDTO> setRelatedClaimDetails(
			Collection<RelatedClaimDTO> relatedClaimDTOs,
			boolean retrieveClaimInformation) {
		EnterpriseConfigService entConfigService = MuleServiceFactory
				.getService(EnterpriseConfigService.class);
		UserDetailsDTO createdUser = null;
		for (RelatedClaimDTO relatedClaim : relatedClaimDTOs) {
			createdUser = entConfigService.retrieveUserDetails(relatedClaim
					.getUserIdCreated());

			relatedClaim.setCreatedBy(createdUser.getFirstName() + " "
					+ createdUser.getLastName());

			if (!relatedClaim.isExternalClaimFlag() && retrieveClaimInformation) {
				// Retrieve Claim Information to get Claim Number.
				ClaimDTO clmDTO = ClaimDTO.class.cast(getClaimService()
						.retrieveDTOOfClaim(relatedClaim.getRelatedClaimId(),
								ClaimDTO.class));
				relatedClaim.setExternalClaimNumber(clmDTO.getClaimNumber());

				// Retrieve Insured Party information for the claim.
				Collection<CALClaimParticipationDTO> returnParticipants = getClaimService()
						.retrieveParticipationDetails(
								relatedClaim.getRelatedClaimId(),
								ClaimsServiceConstants.CAL_CLAIM_INSURED_PARTICIPANT_TYPE_CODE);

				// Check if the Insured is Primary and set the Name
				if (returnParticipants != null) {
					for (CALClaimParticipationDTO participantDTO : returnParticipants) {
						if (participantDTO.isParticipantPrimaryIndicator()) {
							relatedClaim
									.setExternalClaimInsuredName(participantDTO
											.getParticipation().getPartyName());
							break;
						}
					}
				}
			}
		}
		return relatedClaimDTOs;
	}

	/**
	 * Return the related claim info for the passed in claimId.
	 * 
	 * @param claim
	 *            the claim id
	 * @return Collection of Related Claims
	 */
	public Collection<RelatedClaimDTO> populateRelatedClaims(ClaimDTO claim) {
		Collection<RelatedClaimDTO> relatedClaimDTOs = getRelatedClaimService()
				.retrieveRelatedClaims(claim.getRecordId());

		relatedClaimDTOs = setRelatedClaimDetails(relatedClaimDTOs, true);

		return relatedClaimDTOs;
	}

}
