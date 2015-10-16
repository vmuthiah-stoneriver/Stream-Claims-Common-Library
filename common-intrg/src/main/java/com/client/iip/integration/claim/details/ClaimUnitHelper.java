package com.client.iip.integration.claim.details;


import java.util.ArrayList;
import java.util.Collection;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;
import com.stoneriver.iip.claims.individual.ClaimIndividualCompositeDTO;
import com.stoneriver.iip.claims.location.ClaimLocationCompositeDTO;
import com.stoneriver.iip.claims.other.ClaimOtherCompositeDTO;
import com.stoneriver.iip.claims.policy.AllClaimsPolicyService;
import com.stoneriver.iip.claims.property.ClaimPropertyCompositeDTO;
import com.stoneriver.iip.claims.unit.AllClaimUnitService;
import com.stoneriver.iip.claims.unit.ClaimAllUnitsCompositeDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitRequestDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitSummaryDTO;
import com.stoneriver.iip.claims.vehicle.ClaimVehicleCompositeDTO;

@Pojo(id = "com.client.iip.integration.claims.details.ClaimUnitHelper")
public class ClaimUnitHelper {
	private ClaimHelper claimHelper;
	private EntityHelper entityHelper;
	private ClaimsCompositeServiceHelper compositeHelper;

	/**
	 * @return the claimHelper
	 */
	public ClaimHelper getClaimHelper() {
		return claimHelper;
	}

	/**
	 * @param claimHelper
	 *            the claimHelper to set
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.ClaimHelper")
	public void setClaimHelper(ClaimHelper claimHelper) {
		this.claimHelper = claimHelper;
	}

	/**
	 * @return the entityHelper
	 */
	public EntityHelper getEntityHelper() {
		return entityHelper;
	}

	/**
	 * @param entityHelper
	 *            the entityHelper to set
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.EntityHelper")
	public void setEntityHelper(EntityHelper entityHelper) {
		this.entityHelper = entityHelper;
	}

	/**
	 * @return the compositeHelper
	 */
	public ClaimsCompositeServiceHelper getCompositeHelper() {
		return compositeHelper;
	}

	/**
	 * @param compositeHelper
	 *            the compositeHelper to set
	 */
	@Inject(PojoRef = "com.client.iip.integration.claims.details.ClaimsCompositeServiceHelper")
	public void setCompositeHelper(ClaimsCompositeServiceHelper compositeHelper) {
		this.compositeHelper = compositeHelper;
	}

	private AllClaimsPolicyService getPolicyService() {
		return MuleServiceFactory.getService(AllClaimsPolicyService.class);
	}

	private AllClaimUnitService getAllClaimUnitService() {
		return MuleServiceFactory.getService(AllClaimUnitService.class);
	}

	/**
	 * Retrieves all First Party or Third Party Unit information.
	 * 
	 * @param claimUnitRequestDTO
	 *            Claim Unit RequestDTO
	 * @param claimAllUnitsCompositeDTO
	 *            composite to set the retrieved units.
	 * @return ClaimAllUnitsCompositeDTO
	 */
	public ClaimAllUnitsCompositeDTO retrieveAllUnits(
			ClaimUnitRequestDTO claimUnitRequestDTO,
			ClaimAllUnitsCompositeDTO claimAllUnitsCompositeDTO) {

		Collection<ClaimUnitSummaryDTO> collUnitSummaryDTO = getAllClaimUnitService()
				.retrieveClaimUnitList(claimUnitRequestDTO);

		claimAllUnitsCompositeDTO.setClmUnitRequestDTO(claimUnitRequestDTO);
		claimAllUnitsCompositeDTO.setMinimalInternalClaim(claimHelper
				.retrieveMinimalInternalClaim(claimUnitRequestDTO));

		claimAllUnitsCompositeDTO.setUnverifiedPolicy(getPolicyService()
				.isUnverifiedPolicy(claimUnitRequestDTO.getClaimId()));

		Collection<ClaimUnitSummaryDTO> vehicleSummary = new ArrayList<ClaimUnitSummaryDTO>();
		Collection<ClaimUnitSummaryDTO> locationSummary = new ArrayList<ClaimUnitSummaryDTO>();
		Collection<ClaimUnitSummaryDTO> propertySummary = new ArrayList<ClaimUnitSummaryDTO>();
		Collection<ClaimUnitSummaryDTO> individualSummary = new ArrayList<ClaimUnitSummaryDTO>();
		Collection<ClaimUnitSummaryDTO> otherUnitsSummary = new ArrayList<ClaimUnitSummaryDTO>();

		// Since retrieveClaimUnitList method returns a collection of
		// ClaimUnitSummaryDTO
		// we need to loop thru' add them to corresponding Composite DTO
		for (ClaimUnitSummaryDTO clmUnitSummaryDTO : collUnitSummaryDTO) {

			ClaimUnitDTO unitDetails = compositeHelper.retrieveClaimUnit(
					clmUnitSummaryDTO, clmUnitSummaryDTO.getUnitDataTypeCode());
			// strip out the profile statement items
			unitDetails.setDescriptionQuestions(ProfileStatementHelper
					.trimProfileStatementResponseForAnswers(unitDetails
							.getDescriptionQuestions()));
			if(unitDetails.getClaimDamageDTO() != null) {
				unitDetails.getClaimDamageDTO().setQuestions(
						ProfileStatementHelper
								.trimProfileStatementResponseForAnswers(unitDetails
										.getClaimDamageDTO().getQuestions()));
			}
			unitDetails
					.getClaimSalvageDTO()
					.getSalvageDetailsDTO()
					.setQuestions(
							ProfileStatementHelper
									.trimProfileStatementResponseForAnswers(unitDetails
											.getClaimSalvageDTO()
											.getSalvageDetailsDTO()
											.getQuestions()));
			
			clmUnitSummaryDTO.setClaimUnit(unitDetails);

			if (clmUnitSummaryDTO.getUnitDataTypeCode().equals(
					ClaimsServiceConstants.UNIT_DATA_TYPE_VEHICLE)) {
				vehicleSummary.add(clmUnitSummaryDTO);
			} else if (clmUnitSummaryDTO.getUnitDataTypeCode().equals(
					ClaimsServiceConstants.UNIT_DATA_TYPE_LOCATION)) {
				locationSummary.add(clmUnitSummaryDTO);
			} else if (clmUnitSummaryDTO.getUnitDataTypeCode().equals(
					ClaimsServiceConstants.UNIT_DATA_TYPE_PROPERTY)) {
				propertySummary.add(clmUnitSummaryDTO);
			} else if (clmUnitSummaryDTO.getUnitDataTypeCode().equals(
					ClaimsServiceConstants.UNIT_DATA_TYPE_INDIVIDUAL)) {
				individualSummary.add(clmUnitSummaryDTO);
			} else if (clmUnitSummaryDTO.getUnitDataTypeCode().equals(
					ClaimsServiceConstants.UNIT_DATA_TYPE_OTHER)) {
				otherUnitsSummary.add(clmUnitSummaryDTO);
			}
		}

		ClaimVehicleCompositeDTO vehicleCompositeDTO = null;
		ClaimLocationCompositeDTO locationCompositeDTO = null;
		ClaimPropertyCompositeDTO propertyCompositeDTO = null;
		ClaimIndividualCompositeDTO individualCompositeDTO = null;
		ClaimOtherCompositeDTO otherCompositeDTO = null;

		/*
		 * NOTE: the 3rd party maintain flow uses different objects than 3rd
		 * party fnol
		 */
		if (claimAllUnitsCompositeDTO instanceof ClaimDetailsCompositeDTO
				&& claimUnitRequestDTO.isRetrieveThirdPartyUnits()) {
			vehicleCompositeDTO = claimAllUnitsCompositeDTO
					.getMaintainThirdPartyVehicleCompositeDTO();
			locationCompositeDTO = claimAllUnitsCompositeDTO
					.getMaintainThirdPartyLocationCompositeDTO();
			propertyCompositeDTO = claimAllUnitsCompositeDTO
					.getMaintainThirdPartyPropertyCompositeDTO();
			individualCompositeDTO = claimAllUnitsCompositeDTO
					.getMaintainThirdPartyIndividualCompositeDTO();
			otherCompositeDTO = claimAllUnitsCompositeDTO
					.getMaintainThirdPartyOtherCompositeDTO();
		} else {
			vehicleCompositeDTO = claimAllUnitsCompositeDTO
					.getVehicleCompositeDTO();
			locationCompositeDTO = claimAllUnitsCompositeDTO
					.getLocationCompositeDTO();
			propertyCompositeDTO = claimAllUnitsCompositeDTO
					.getPropertyCompositeDTO();
			individualCompositeDTO = claimAllUnitsCompositeDTO
					.getIndividualCompositeDTO();
			otherCompositeDTO = claimAllUnitsCompositeDTO
					.getOtherCompositeDTO();
		}

		vehicleCompositeDTO.setVehicles(vehicleSummary);
		vehicleCompositeDTO.setUnverifiedPolicy(claimAllUnitsCompositeDTO
				.isUnverifiedPolicy());
		entityHelper.retrieveVehicleEntities(vehicleCompositeDTO,
				claimUnitRequestDTO);

		locationCompositeDTO.setLocations(locationSummary);
		locationCompositeDTO.setUnverifiedPolicy(claimAllUnitsCompositeDTO
				.isUnverifiedPolicy());
		entityHelper.retrieveLocationEntities(locationCompositeDTO,
				claimUnitRequestDTO);

		propertyCompositeDTO.setClaimPropertySummarys(propertySummary);
		propertyCompositeDTO.setUnverifiedPolicy(claimAllUnitsCompositeDTO
				.isUnverifiedPolicy());
		entityHelper.retrievePropertyEntities(propertyCompositeDTO,
				claimUnitRequestDTO);

		individualCompositeDTO.setIndividuals(individualSummary);
		individualCompositeDTO.setUnverifiedPolicy(claimAllUnitsCompositeDTO
				.isUnverifiedPolicy());
		entityHelper.retrieveIndividualEntities(individualCompositeDTO,
				claimUnitRequestDTO);

		otherCompositeDTO.setOtherUnits(otherUnitsSummary);
		otherCompositeDTO.setUnverifiedPolicy(claimAllUnitsCompositeDTO
				.isUnverifiedPolicy());
		entityHelper.retrieveOtherUnitEntities(otherCompositeDTO,
				claimUnitRequestDTO);

		return claimAllUnitsCompositeDTO;
	}

}
