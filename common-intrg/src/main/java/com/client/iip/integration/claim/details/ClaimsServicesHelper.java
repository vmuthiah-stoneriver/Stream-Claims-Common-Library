package com.client.iip.integration.claim.details;


import java.util.Collection;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementItem;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementRequest;

@Pojo(id = "com.client.iip.integration.claims.details.ClaimsServicesHelper")
public class ClaimsServicesHelper {
	private EnterpriseConfigService getEnterpriseConfigService() {
		return MuleServiceFactory.getService(EnterpriseConfigService.class);
	}

	/**
	 * set the Composite Questions.
	 * 
	 * @param claimParam
	 *            the ClaimInformationRequestDTO
	 * @param detailsCompositeDTO
	 *            the ClaimDetailsCompositeDTO
	 */
	public void setCompositeQuestions(ClaimInformationRequestDTO claimParam,
			ClaimDetailsCompositeDTO detailsCompositeDTO) {
		// retrieve the question for Maintain Claim Unit Participant At-Fault
		Collection<ProfileStatementItem> atFaultStatements = retrieveServicesQuestions(
				ClaimsServiceConstants.PROFILE_PARTICIPANT_AT_FAULT_QUESTIONS_AREA_CD,
				ClaimsServiceConstants.PROFILE_PARTICIPANT_AT_FAULT_QUESTIONS_SUB_AREA_CD,
				claimParam.getLobCode());
		detailsCompositeDTO.setAtFaultQuestionsAndAnswers(atFaultStatements);
	}

	/**
	 * @return the Services Questions
	 * @param area
	 *            The Area
	 * @param subarea
	 *            The Sub-area
	 * @param lob
	 *            The line of business
	 */
	protected Collection<ProfileStatementItem> retrieveServicesQuestions(
			String area, String subarea, String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest.setAreaCode(area);
		profileStatementRequest.setSubAreaCode(subarea);
		profileStatementRequest
				.setSubAreaCategoryCode(ClaimsServiceConstants.PROFILE_PROPERTY_DAMAGES_SERVICES_QUESTIONS_SUB_AREA_CATEGORY_CD);

		return ProfileStatementHelper
				.trimProfileStatementResponseForAnswers(entConfigService
						.retrieveProfileStatements(profileStatementRequest));
	}

}
