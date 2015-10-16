package com.client.iip.integration.claim.details;


import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.unit.AllClaimUnitService;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementItem;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementRequest;

@Pojo(id = "com.client.iip.integration.claims.details.ProfileStatementHelper")
public class ProfileStatementHelper {
	private AllClaimUnitService getAllClaimUnitService() {
		return MuleServiceFactory.getService(AllClaimUnitService.class);
	}

	/**
	 * Returns the Loss indicators.
	 * 
	 * @param claimId
	 *            The claimId
	 * @param lobCode
	 *            The LOB
	 * @return The questions.
	 */
	protected Collection<ProfileStatementItem> retrieveLossIndicators(
			Long claimId, String lobCode) {
		return retrieveProfileStatementsWithAnswers(
				claimId,
				ClaimsServiceConstants.PROFILE_CLAIM_INDICATORS_AREA,
				ClaimsServiceConstants.PROFILE_CLAIM_INDICATORS_LOSS_INDICATORS_SUBAREA,
				null, lobCode, null);
	}

	/**
	 * This method returns the questions with theirs answers.
	 * 
	 * @param claimId
	 *            The claim ID
	 * @param areaCode
	 *            PRFL_AREA_CD.
	 * @param subAreaCode
	 *            PRFL_SUBAREA_CD.
	 * @param subAreaCategoryCode
	 *            PRFL_SUBAREA_CTG_CD. Can be null.
	 * @param lob
	 *            line of business. Can be null.
	 * @param jurisdictionId
	 *            jurisdiction id. can be null.
	 * @return profile statements requested with answers.
	 */
	protected Collection<ProfileStatementItem> retrieveProfileStatementsWithAnswers(
			Long claimId, String areaCode, String subAreaCode,
			String subAreaCategoryCode, String lob, Long jurisdictionId) {
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest.setAreaCode(areaCode);
		profileStatementRequest.setSubAreaCode(subAreaCode);
		profileStatementRequest.setSubAreaCategoryCode(subAreaCategoryCode);
		profileStatementRequest.setJurisdictionCode(jurisdictionId);

		Collection<ProfileStatementItem> statements = getAllClaimUnitService()
				.retrieveProfileStatementsWithAnswers(claimId,
						profileStatementRequest);
		return ProfileStatementHelper
				.trimProfileStatementResponseForAnswers(statements);
	}

	/**
	 * Returns the Loss indicators.
	 * 
	 * @param claimId
	 *            The claimId
	 * @param lobCode
	 *            The LOB
	 * @return The questions.
	 */
	protected Collection<ProfileStatementItem> retrievePolicyIndicators(
			Long claimId, String lobCode) {
		return retrieveProfileStatementsWithAnswers(
				claimId,
				ClaimsServiceConstants.PROFILE_CLAIM_INDICATORS_AREA,
				ClaimsServiceConstants.PROFILE_CLAIM_INDICATORS_POLICY_INDICATORS_SUBAREA,
				null, lobCode, null);
	}

	/**
	 * Returns the Loss questions.
	 * 
	 * @param claimId
	 *            The claimId
	 * @param lobCode
	 *            The LOB
	 * @return The questions.
	 */
	protected Collection<ProfileStatementItem> retrieveLossQuestions(
			Long claimId, String lobCode) {
		return retrieveProfileStatementsWithAnswers(claimId,
				ClaimsServiceConstants.PROFILE_LOSS_QUESTIONS_AREA,
				ClaimsServiceConstants.PROFILE_LOSS_QUESTIONS_SUBAREA, null,
				lobCode, null);
	}

	/**
	 * Returns the Medicare/Medicaid questions.
	 * 
	 * @param claimId
	 *            The claimId
	 * @param lobCode
	 *            The LOB
	 * @return The questions Collection of ProfileStatementItem.
	 */
	protected Collection<ProfileStatementItem> retrieveMedicareMedicaidQuestions(
			Long claimId, String lobCode) {
		Collection<ProfileStatementItem> profiles = retrieveProfileStatementsWithAnswers(
				claimId,
				ClaimsServiceConstants.QUICK_CLAIM_QUESTIONS_AREA_CODE,
				ClaimsServiceConstants.QUICK_CLAIM_GENERAL_DETAILS_SUB_AREA_CODE,
				null, lobCode, null);

		return profiles;
	}

	protected Collection<ProfileStatementItem> retrieveClaimGeneralQuestions(
			Long claimId, String lobCode) {
		Collection<ProfileStatementItem> profiles = retrieveProfileStatementsWithAnswers(
				claimId, ClaimsServiceConstants.PROFILE_LOSS_QUESTIONS_AREA,
				ClaimsServiceConstants.PROFILE_MEDICARE_QUESTIONS_SUBAREA,
				null, lobCode, null);

		return profiles;
	}

	protected Collection<ProfileStatementItem> retrieveClaimDetailsOfIncident(
			Long claimId, String lobCode) {
		Collection<ProfileStatementItem> profiles = retrieveProfileStatementsWithAnswers(
				claimId,
				ClaimsServiceConstants.QUICK_CLAIM_QUESTIONS_AREA_CODE,
				ClaimsServiceConstants.QUICK_CLAIM_DETAILS_OF_INCIDENT_SUB_AREA_CODE,
				null, lobCode, null);

		return profiles;
	}

	public static Collection<ProfileStatementItem> trimProfileStatementResponseForAnswers(
			Collection<ProfileStatementItem> items) {
		Collection<ProfileStatementItem> returnList = new ArrayList<ProfileStatementItem>();
		if (null != items) {
			for (ProfileStatementItem item : items) {
				boolean answerNotBlank = StringUtils.isNotBlank(item
						.getAnswer());
				boolean selectionNotBlank = StringUtils.isNotBlank(item
						.getListSelectionValueCd());
				if (answerNotBlank || selectionNotBlank) {
					// remove the profile statement selection. This is just the
					// list of all the options.
					item.setProfileSection(null);
					returnList.add(item);
				} else {
					// do nothing
				}
			}
		}
		return returnList;

	}
}
