package com.client.iip.integration.claim.details;


import java.util.Collection;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.composite.ParticipationHelper;
import com.stoneriver.iip.claims.unit.AllClaimUnitService;
import com.stoneriver.iip.claims.unit.ClaimDamageServicesDTO;
import com.stoneriver.iip.claims.unit.ClaimServicesCompositeDTO;

@Pojo(id = "com.client.iip.integration.claims.details.CompositeServiceHelper")
public class CompositeServiceHelper {
	private ParticipationHelper participationHelper;
	private ClaimHelper claimHelper;
	private ClaimsServicesHelper claimServicesHelper;
	
	private AllClaimUnitService getAllClaimUnitService(){
		return MuleServiceFactory.getService(AllClaimUnitService.class);
	}

	/**
	 * @return the claimHelper
	 */
	public ClaimHelper getClaimHelper() {
		return claimHelper;
	}

	/**
	 * @param claimHelper the claimHelper to set
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.ClaimHelper")
	public void setClaimHelper(ClaimHelper value) {
		this.claimHelper = value;
	}

	/**
	 * @return the claimServicesHelper
	 */
	public ClaimsServicesHelper getClaimServicesHelper() {
		return claimServicesHelper;
	}

	/**
	 * @param claimServicesHelper the claimServicesHelper to set
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.ClaimsServicesHelper")
	public void setClaimServicesHelper(ClaimsServicesHelper value) {
		this.claimServicesHelper = value;
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
	 * @param claimRequest the ClaimInformationRequestDTO
	 * @return the ClaimServicesCompositeDTO
	 */
	protected ClaimServicesCompositeDTO retrieveServicesComposite(ClaimInformationRequestDTO claimRequest) {
		ClaimServicesCompositeDTO serviceCompositeDTO = new ClaimServicesCompositeDTO();
		Collection<ClaimDamageServicesDTO> services = getAllClaimUnitService().retrieveClaimServices(claimRequest.getClaimId(),
						claimRequest.getLobCode(), new String());		
		normalizeParticipationDTOList(services);
		serviceCompositeDTO.setClaimServicesSummarys(services);
		serviceCompositeDTO.setClaimRequest(claimRequest);
		serviceCompositeDTO.setMinimalInternalClaim(claimHelper.retrieveMinimalInternalClaim(claimRequest));
		serviceCompositeDTO.setServicesQuestions(claimServicesHelper.retrieveServicesQuestions(ClaimsServiceConstants.PROFILE_SERVICES_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_SERVICES_QUESTIONS_SUB_AREA_CD,
						claimRequest.getLobCode()));
		//do not load all categories
		//serviceCompositeDTO.setVendorCategories(getAllClaimUnitService().retrieveVendorTypeFiltered("", getAllClaimUnitService().retrieveParticipationTypeByPtcpAreaTypeLobCode(ClaimsAllParticipationConstants.PARTICIPATION_AREA_ROOM_SERVICE_PROVIDER, claimRequest.getLobCode())));		
		return serviceCompositeDTO;
	}
	
	/**
	 * @param list the list to Normalize CALClaimParticipationDTO	 * 
	 */
	protected void normalizeParticipationDTOList(
			Collection<? extends CALClaimParticipationDTO> list) {
		if (list != null) {
			for (CALClaimParticipationDTO cALClaimParticipationDTO : list) {
				getParticipationHelper().normalizeParticipationDTO(
						cALClaimParticipationDTO);
			}
		}
	}
	
}
