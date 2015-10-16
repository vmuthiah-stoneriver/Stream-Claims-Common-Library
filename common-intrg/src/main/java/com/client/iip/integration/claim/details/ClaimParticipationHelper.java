package com.client.iip.integration.claim.details;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CALClaimParticipationContactDTO;
import com.stoneriver.iip.claims.ClaimDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.composite.ParticipationHelper;
import com.stoneriver.iip.claims.witness.CWSClaimWitnessService;
import com.stoneriver.iip.claims.witness.ClaimWitnessDTO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementItem;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementRequest;

@Pojo(id = "com.client.iip.integration.claims.details.ClaimParticipationHelper")
public class ClaimParticipationHelper {
	private ParticipationHelper participationHelper;

	private EnterpriseConfigService getEnterpriseConfigService() {
		return MuleServiceFactory.getService(EnterpriseConfigService.class);
	}

	private CWSClaimWitnessService getClaimWitnessService() {
		return MuleServiceFactory.getService(CWSClaimWitnessService.class);
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
	 * Retrieves authorities profile statement items from enterprise config
	 * service.
	 * 
	 * @param lob
	 *            The line of business
	 * @return the profile statements requested.
	 */
	protected HashMap<String, Collection<ProfileStatementItem>> retrieveAuthoritiesProfileStatements(
			String lob) {
		HashMap<String, Collection<ProfileStatementItem>> result = new HashMap<String, Collection<ProfileStatementItem>>();
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_AUTHORITY_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_AUTHORITY_QUESTIONS_SUB_AREA_FIRE_CD);
		profileStatementRequest
				.setSubAreaCategoryCode(ClaimsServiceConstants.PROFILE_AUTHORITY_SUB_AREA_CATEGORY_CD);
		result.put(
				ClaimsServiceConstants.PROFILE_AUTHORITY_QUESTIONS_SUB_AREA_FIRE_CD,
				ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
						.retrieveProfileStatements(profileStatementRequest)));
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_AUTHORITY_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_AUTHORITY_QUESTIONS_SUB_AREA_POLICE_CD);
		profileStatementRequest
				.setSubAreaCategoryCode(ClaimsServiceConstants.PROFILE_AUTHORITY_SUB_AREA_CATEGORY_CD);
		result.put("polc", ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest)));
		return result;
	}

	public Collection<ClaimWitnessDTO> getWitnesses(ClaimDTO claimDto) {

		Collection<ClaimWitnessDTO> witnesses = getClaimWitnessService()
				.retrieveClaimWitnessesAndContacts(claimDto.getRecordId());

		if (witnesses == null) {
			witnesses = new ArrayList<ClaimWitnessDTO>();
		}

		for (ClaimWitnessDTO witness : witnesses) {
			// Normalizing witnesses
			getParticipationHelper().normalizeParticipationDTO(witness);
			// Normalizing contacts
			if (witness.getContacts() != null) {
				for (CALClaimParticipationContactDTO calClaimParticipationContactDTO : witness
						.getContacts()) {
					getParticipationHelper().normalizeParticipationContactDTO(
							calClaimParticipationContactDTO);
				}
			}
		}

		return witnesses;
	}

}
