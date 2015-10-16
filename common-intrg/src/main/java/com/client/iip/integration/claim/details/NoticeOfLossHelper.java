package com.client.iip.integration.claim.details;


import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.CWSNoticeOfLossService;
import com.stoneriver.iip.claims.ClaimDTO;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;
import com.stoneriver.iip.claims.loss.ClaimNoticeOfLossCompositeDTO;
import com.stoneriver.iip.claims.wc.WCNoticeOfLossService;

@Pojo(id = "com.client.iip.integration.claims.details.NoticeOfLossHelper")
public class NoticeOfLossHelper {
	private ProfileStatementHelper profileStatementHelper;

	/**
	 * @return the profileStatementHelper
	 */
	public ProfileStatementHelper getProfileStatementHelper() {
		return profileStatementHelper;
	}

	/**
	 * @param profileStatementHelper
	 *            the profileStatementHelper to set
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.ProfileStatementHelper")
	public void setProfileStatementHelper(ProfileStatementHelper value) {
		this.profileStatementHelper = value;
	}

	private CWSNoticeOfLossService getNoticeOfLossService() {
		return MuleServiceFactory.getService(CWSNoticeOfLossService.class);
	}

	private WCNoticeOfLossService getWcNoticeOfLossService() {
		return MuleServiceFactory.getService(WCNoticeOfLossService.class);
	}

	private CWSClaimService getClaimService() {
		return MuleServiceFactory.getService(CWSClaimService.class);
	}

	/**
	 * This method is formatting the value for NOL Information.
	 * 
	 * @param cdcDTO
	 *            the composite
	 * @param claimParam
	 *            the parameters
	 */
	public void prepareNOLInformation(ClaimDetailsCompositeDTO cdcDTO,
			ClaimInformationRequestDTO claimParam) {

		ClaimNoticeOfLossCompositeDTO cnoflcDTO;

		if (isWC(claimParam.getLobCode())) {
			cnoflcDTO = getWcNoticeOfLossService().retrieveNoticeOfLoss(
					claimParam);
		} else {
			cnoflcDTO = getNoticeOfLossService().retrieveNoticeOfLoss(
					claimParam);
		}
		cdcDTO.setNolDescriptionDTO(cnoflcDTO.getNolDescriptionDTO());
		cdcDTO.setClaimNOLOccurrenceDTO(cnoflcDTO.getClaimNOLOccurrenceDTO());
		cdcDTO.setInjury(cnoflcDTO.getInjury());
		cdcDTO.setClaimWC(cnoflcDTO.getClaimWC());

		cdcDTO.setLossQuestions(profileStatementHelper
						.retrieveLossQuestions(claimParam.getClaimId(),
								claimParam.getLobCode()));
		cdcDTO.setJurisdictionQuestions(cnoflcDTO.getJurisdictionQuestions());

		cdcDTO.setMedicareQuestions(profileStatementHelper
						.retrieveMedicareMedicaidQuestions(
								claimParam.getClaimId(),
								claimParam.getLobCode()));

		// retrieve Claim Information to check Claim Source Code
		ClaimDTO clmDTO = ClaimDTO.class.cast(getClaimService()
				.retrieveDTOOfClaim(claimParam.getClaimId(), ClaimDTO.class));
		if (ClaimsServiceConstants.CLAIM_SOURCE_QUICK_CLAIM.equals(clmDTO
				.getClaimSourceCode())) {

			cdcDTO.getQuickClaimQuestions()
					.addAll(profileStatementHelper
									.retrieveClaimGeneralQuestions(
											claimParam.getClaimId(),
											claimParam.getLobCode()));
			cdcDTO.getQuickClaimQuestions()
					.addAll(profileStatementHelper
									.retrieveClaimDetailsOfIncident(
											claimParam.getClaimId(),
											claimParam.getLobCode()));
		}
	}

	/***
	 * This method checks if the LOB code is Work Comp (wc). If the parameter is
	 * empty or null, an {@link IIPCoreSystemException} exception will be
	 * thrown.
	 * 
	 * @param lobCode
	 *            the LOB to check.
	 * @return True if the lobCode is Work Comp, False in other case.
	 */
	public static boolean isWC(String lobCode) {
		if (lobCode == null || lobCode.length() == 0) {
			throw new IIPCoreSystemException(
					"The LOB cannot be null nor empty.");
		}
		return lobCode
				.equalsIgnoreCase(ClaimsServiceConstants.LOB_WORKERS_COMP);
	}

}
