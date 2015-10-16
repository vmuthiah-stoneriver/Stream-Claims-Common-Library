package com.client.iip.integration.claim.details;


import java.util.Collection;
import java.util.HashMap;

import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.individual.ClaimIndividualCompositeDTO;
import com.stoneriver.iip.claims.location.ClaimLocationCompositeDTO;
import com.stoneriver.iip.claims.other.ClaimOtherCompositeDTO;
import com.stoneriver.iip.claims.property.ClaimPropertyCompositeDTO;
import com.stoneriver.iip.claims.unit.AllClaimUnitService;
import com.stoneriver.iip.claims.unit.ClaimUnitRequestDTO;
import com.stoneriver.iip.claims.vehicle.ClaimVehicleCompositeDTO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementItem;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementRequest;

@Pojo(id = "com.client.iip.integration.claims.details.EntityHelper")
public class EntityHelper {

	private EnterpriseConfigService getEnterpriseConfigService() {
		return MuleServiceFactory.getService(EnterpriseConfigService.class);
	}

	private AllClaimUnitService getAllClaimUnitService() {
		return MuleServiceFactory.getService(AllClaimUnitService.class);
	}

	/**
	 * Retrieves Vehicle Entities like Other Carries, Driver and Passenger
	 * Profile Statements.
	 * 
	 * @param vehicleCompositeDTO
	 *            Vehicle Information
	 * @param claimUnitRequestDTO
	 *            Claim Unit RequestDTO
	 * @return ClaimVehicleCompositeDTO
	 */
	public ClaimVehicleCompositeDTO retrieveVehicleEntities(
			ClaimVehicleCompositeDTO vehicleCompositeDTO,
			ClaimUnitRequestDTO claimUnitRequestDTO) {

		// Passing LOB Code to retrieve other carriers Questions as profiles can
		// be defined by LOB also

		vehicleCompositeDTO
				.setConfiguredOtherCarriersQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveOtherCarriersProfileStatements(
								ClaimsServiceConstants.PROFILE_OTHER_CARRIERS_VEHICLES_SUB_AREA_CD,
								claimUnitRequestDTO.getLobCode())));

		vehicleCompositeDTO
				.setServicesQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveServicesQuestions(
								ClaimsServiceConstants.PROFILE_VEHICLE_DAMAGES_SERVICES_QUESTIONS_AREA_CD,
								ClaimsServiceConstants.PROFILE_VEHICLE_DAMAGES_SERVICES_QUESTIONS_SUB_AREA_CD,
								claimUnitRequestDTO.getLobCode())));

		// Getting Claim Unit Vehicle Participants Driver Injury Questions.
		vehicleCompositeDTO
				.setInjuryQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveVehicleInjuryProfileStatements(
								claimUnitRequestDTO.getLobCode(),
								ClaimsServiceConstants.CAL_CLAIM_DRIVER_PARTICIPANT_TYPE_CODE)));

		// Getting Claim Unit Vehicle Participants Passenger Injury Questions.
		vehicleCompositeDTO
				.setPsgrInjuryQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveVehicleInjuryProfileStatements(
								claimUnitRequestDTO.getLobCode(),
								ClaimsServiceConstants.CAL_CLAIM_PASSENGER_PARTICIPANT_TYPE_CODE)));

		vehicleCompositeDTO
				.setConfiguredSalvageQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveVehicleSalvageQuestions(claimUnitRequestDTO
								.getLobCode())));
		vehicleCompositeDTO
				.setConfiguredSalvorQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveVehicleSalvageSalvorQuestions(claimUnitRequestDTO
								.getLobCode())));

		/*
		 * vehicleCompositeDTO.setVendorCategories(retrieveVendorCategories(
		 * ClaimsAllParticipationConstants
		 * .PARTICIPATION_AREA_UNIT_SERVICE_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_VEHICLE, claimUnitRequestDTO));
		 * 
		 * vehicleCompositeDTO.setTreatmentVendorCategories(retrieveVendorCategories
		 * ( ClaimsAllParticipationConstants.PARTICIPATION_AREA_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_VEHICLE, claimUnitRequestDTO));
		 * 
		 * vehicleCompositeDTO.setSalvageVendorCategories(retrieveVendorCategories
		 * ( ClaimsAllParticipationConstants.PARTICIPATION_AREA_SALVOR_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_VEHICLE, claimUnitRequestDTO));
		 */

		vehicleCompositeDTO
				.setDescriptionQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(retrieveProfileStatementsWithAnswers(
								claimUnitRequestDTO.getClaimId(),
								ClaimsServiceConstants.PROFILE_CLAIM_UNIT_AREA_CD,
								ClaimsServiceConstants.PROFILE_UNIT_VEHICLE_DESCRIPTION_SUBAREA_CD,
								null, claimUnitRequestDTO.getLobCode(), null)));

		return vehicleCompositeDTO;
	}

	/**
	 * Retrieves Location Entities like Other Carries, Service Profile
	 * Statements and Vendor Categories.
	 * 
	 * @param locationCompositeDTO
	 *            Location Information
	 * @param claimUnitRequestDTO
	 *            Claim Unit RequestDTO
	 * @return ClaimLocationCompositeDTO
	 */
	public ClaimLocationCompositeDTO retrieveLocationEntities(
			ClaimLocationCompositeDTO locationCompositeDTO,
			ClaimUnitRequestDTO claimUnitRequestDTO) {
		// don't load reference data

		Collection<ProfileStatementItem> injuryProfileStmts = retrieveLocationInjuryProfileStatements(claimUnitRequestDTO
				.getLobCode());
		locationCompositeDTO.setInjuryQuestions(ProfileStatementHelper
				.trimProfileStatementResponseForAnswers(injuryProfileStmts));

		Collection<ProfileStatementItem> otherCarrierProfileStmts = retrieveOtherCarriersProfileStatements(
				ClaimsServiceConstants.PROFILE_OTHER_CARRIERS_LOCATIONS_SUB_AREA_CD,
				claimUnitRequestDTO.getLobCode());
		locationCompositeDTO
				.setConfiguredOtherCarriersQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(otherCarrierProfileStmts));

		Collection<ProfileStatementItem> serviceProfileStmts = retrieveServicesQuestions(
				ClaimsServiceConstants.PROFILE_LOCATIONS_DAMAGES_SERVICES_QUESTIONS_AREA_CD,
				ClaimsServiceConstants.PROFILE_LOCATIONS_DAMAGES_SERVICES_QUESTIONS_SUB_AREA_CD,
				claimUnitRequestDTO.getLobCode());
		locationCompositeDTO.setServicesQuestions(ProfileStatementHelper
				.trimProfileStatementResponseForAnswers(serviceProfileStmts));

		Collection<ProfileStatementItem> locationSalvageProfileStmts = retrieveLocationSalvageQuestions(claimUnitRequestDTO
				.getLobCode());
		locationCompositeDTO
				.setConfiguredSalvageQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(locationSalvageProfileStmts));

		Collection<ProfileStatementItem> locationSalvorProfileStmts = retrieveLocationSalvageSalvorQuestions(claimUnitRequestDTO
				.getLobCode());
		locationCompositeDTO
				.setConfiguredSalvorQuestions(ProfileStatementHelper
						.trimProfileStatementResponseForAnswers(locationSalvorProfileStmts));

		// Don't load reference data

		/*
		 * Collection<CodeLookupNestedBO> vendorCategories =
		 * retrieveVendorCategories(
		 * ClaimsAllParticipationConstants.PARTICIPATION_AREA_UNIT_SERVICE_PROVIDER
		 * , ClaimsServiceConstants.UNIT_DATA_TYPE_LOCATION,
		 * claimUnitRequestDTO);
		 * locationCompositeDTO.setVendorCategories(vendorCategories);
		 * 
		 * String particpationAreaCode = null; if
		 * (ClaimsServiceConstants.LOB_WORKERS_COMP.equals(claimUnitRequestDTO
		 * .getLobCode()) && WCLiteHelper.isWCLite()) { particpationAreaCode =
		 * ClaimsAllParticipationConstants
		 * .PARTICIPATION_AREA_ROOM_SERVICE_PROVIDER; } else {
		 * particpationAreaCode =
		 * ClaimsAllParticipationConstants.PARTICIPATION_AREA_PROVIDER; }
		 * 
		 * Collection<CodeLookupNestedBO> vendorTreatmentCategories =
		 * retrieveVendorCategories( particpationAreaCode,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_LOCATION, claimUnitRequestDTO);
		 * locationCompositeDTO
		 * .setTreatmentVendorCategories(vendorTreatmentCategories);
		 * 
		 * Collection<CodeLookupNestedBO> vendorSalvageCategories =
		 * retrieveVendorCategories(
		 * ClaimsAllParticipationConstants.PARTICIPATION_AREA_SALVOR_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_LOCATION, claimUnitRequestDTO);
		 * locationCompositeDTO
		 * .setSalvageVendorCategories(vendorSalvageCategories);
		 */

		return locationCompositeDTO;
	}

	/**
	 * Retrieves Property Entities like Damage, Other Carries, Service Profile
	 * Statements and Vendor Categories.
	 * 
	 * @param claimPropertyCompositeDTO
	 *            Property Information
	 * @param claimUnitRequestDTO
	 *            Claim Unit RequestDTO
	 * @return ClaimPropertyCompositeDTO
	 */
	public ClaimPropertyCompositeDTO retrievePropertyEntities(
			ClaimPropertyCompositeDTO claimPropertyCompositeDTO,
			ClaimUnitRequestDTO claimUnitRequestDTO) {
		// don't load reference data

		claimPropertyCompositeDTO
				.setDamageQuestions(ProfileStatementHelper.trimProfileStatementResponseForAnswers(retrieveDamageProfileStatement(
						claimUnitRequestDTO.getLobCode(),
						ClaimsServiceConstants.PROFILE_PROPERTY_DAMAGES_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_PROPERTY_DAMAGES_QUESTIONS_SUB_AREA_CD,
						ClaimsServiceConstants.PROFILE_PROPERTY_DAMAGES_QUESTIONS_SUB_AREA_CATEGORY_CD)));
		claimPropertyCompositeDTO
				.setServicesQuestions(ProfileStatementHelper.trimProfileStatementResponseForAnswers(retrieveServicesQuestions(
						ClaimsServiceConstants.PROFILE_PROPERTY_DAMAGES_SERVICES_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_PROPERTY_DAMAGES_SERVICES_QUESTIONS_SUB_AREA_CD,
						claimUnitRequestDTO.getLobCode())));
		claimPropertyCompositeDTO
				.setConfiguredSalvageQuestions(ProfileStatementHelper.trimProfileStatementResponseForAnswers(retrievePropertySalvageQuestions(claimUnitRequestDTO
						.getLobCode())));
		claimPropertyCompositeDTO
				.setConfiguredSalvorQuestions(ProfileStatementHelper.trimProfileStatementResponseForAnswers(retrievePropertySalvageSalvorQuestions(claimUnitRequestDTO
						.getLobCode())));

		//don't load categories
		/*
		claimPropertyCompositeDTO
				.setVendorCategories(retrieveVendorCategories(
						ClaimsAllParticipationConstants.PARTICIPATION_AREA_UNIT_SERVICE_PROVIDER,
						ClaimsServiceConstants.UNIT_TYPE_PROPERTY,
						claimUnitRequestDTO));

		claimPropertyCompositeDTO
				.setTreatmentVendorCategories(retrieveVendorCategories(
						ClaimsAllParticipationConstants.PARTICIPATION_AREA_PROVIDER,
						ClaimsServiceConstants.UNIT_TYPE_PROPERTY,
						claimUnitRequestDTO));

		claimPropertyCompositeDTO
				.setSalvageVendorCategories(retrieveVendorCategories(
						ClaimsAllParticipationConstants.PARTICIPATION_AREA_SALVOR_PROVIDER,
						ClaimsServiceConstants.UNIT_DATA_TYPE_PROPERTY,
						claimUnitRequestDTO));
*/
		claimPropertyCompositeDTO
				.setConfiguredOtherCarriersQuestions(retrieveOtherCarriersProfileStatements(
						ClaimsServiceConstants.PROFILE_OTHER_CARRIERS_PROPERTY_SUB_AREA_CD,
						claimUnitRequestDTO.getLobCode()));
		return claimPropertyCompositeDTO;
	}

	/**
	 * Retrieves Property Entities like Damage, Other Carries, Service Profile
	 * Statements and Vendor Categories.
	 * 
	 * @param claimIndividualCompositeDTO
	 *            Property Information
	 * @param claimUnitRequestDTO
	 *            Claim Unit RequestDTO
	 * @return ClaimPropertyCompositeDTO
	 */
	public ClaimIndividualCompositeDTO retrieveIndividualEntities(
			ClaimIndividualCompositeDTO claimIndividualCompositeDTO,
			ClaimUnitRequestDTO claimUnitRequestDTO) {
		// don't load reference data
		/*
		 * claimIndividualCompositeDTO
		 * .setDamageQuestions(retrieveServicesQuestions(
		 * ClaimsServiceConstants.PROFILE_CLAIM_UNIT_AREA_CD,
		 * ClaimsServiceConstants
		 * .PROFILE_INDIVIDUAL_DAMAGES_QUESTIONS_SUB_AREA_CD,
		 * claimUnitRequestDTO.getLobCode()));
		 * 
		 * claimIndividualCompositeDTO
		 * .setServicesQuestions(retrieveServicesQuestions(
		 * ClaimsServiceConstants.PROFILE_CLAIM_UNIT_AREA_CD,
		 * ClaimsServiceConstants
		 * .PROFILE_INDIVIDUAL_DAMAGES_SERVICES_QUESTIONS_SUB_AREA_CD,
		 * claimUnitRequestDTO.getLobCode()));
		 * 
		 * 
		 * claimIndividualCompositeDTO
		 * .setVendorCategories(retrieveVendorCategories(
		 * ClaimsAllParticipationConstants
		 * .PARTICIPATION_AREA_UNIT_SERVICE_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_INDIVIDUAL,
		 * claimUnitRequestDTO));
		 * 
		 * claimIndividualCompositeDTO
		 * .setTreatmentVendorCategories(retrieveVendorCategories(
		 * ClaimsAllParticipationConstants.PARTICIPATION_AREA_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_INDIVIDUAL,
		 * claimUnitRequestDTO));
		 * 
		 * claimIndividualCompositeDTO
		 * .setConfiguredOtherCarriersQuestions(retrieveServicesQuestions(
		 * ClaimsServiceConstants.PROFILE_CLAIM_UNIT_AREA_CD,
		 * ClaimsServiceConstants
		 * .PROFILE_INDIVIDUAL_OTHER_CARRIERS_QUESTIONS_SUB_AREA_CD,
		 * claimUnitRequestDTO.getLobCode()));
		 * 
		 * claimIndividualCompositeDTO
		 * .setInjuryQuestions(retrieveServicesQuestions(
		 * ClaimsServiceConstants.PROFILE_CLAIM_UNIT_AREA_CD,
		 * ClaimsServiceConstants
		 * .PROFILE_INDIVIDUAL_INJURY_QUESTIONS_SUB_AREA_CD,
		 * claimUnitRequestDTO.getLobCode()));
		 */

		return claimIndividualCompositeDTO;
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

		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * Retrieves profile statement items from enterprise config service.
	 * 
	 * @param lob
	 *            the line of business
	 * @param areaCode
	 *            the area code
	 * @param subAreaCode
	 *            the sub area code
	 * @param subAreaCodeCategory
	 *            the sub area code category
	 * @return profile statements requested.
	 */
	protected Collection<ProfileStatementItem> retrieveDamageProfileStatement(
			String lob, String areaCode, String subAreaCode,
			String subAreaCodeCategory) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest.setAreaCode(areaCode);
		profileStatementRequest.setSubAreaCode(subAreaCode);
		profileStatementRequest.setSubAreaCategoryCode(subAreaCodeCategory);
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * Retrieves profile statement items for Other from enterprise config
	 * service.
	 * 
	 * @param lob
	 *            the LineOfBusinessCode
	 * @param areaCode
	 *            the AreaCode
	 * @param subAreaCode
	 *            the SubAreaCode
	 * @param category
	 *            the SubAreaCategoryCode
	 * @return profile statements requested.
	 */
	protected Collection<ProfileStatementItem> retrieveOtherQuestions(
			String lob, String areaCode, String subAreaCode, String category) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest.setAreaCode(areaCode);
		profileStatementRequest.setSubAreaCode(subAreaCode);
		profileStatementRequest.setSubAreaCategoryCode(category);

		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * @param lob
	 *            The lineOfBusinessCodeTemp
	 * @return The vehicle salvage questions
	 */
	protected Collection<ProfileStatementItem> retrieveVehicleSalvageQuestions(
			String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_VEHICLE_SALVAGE_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_VEHICLE_SALVAGE_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest.setSubAreaCategoryCode("");
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * @param lob
	 *            The lineOfBusinessCodeTemp
	 * @return The vehicle salvage salvor questions
	 */
	protected Collection<ProfileStatementItem> retrieveVehicleSalvageSalvorQuestions(
			String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_VEHICLE_SALVAGE_SALVOR_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_VEHICLE_SALVAGE_SALVOR_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest.setSubAreaCategoryCode("");
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * @param lob
	 *            The lineOfBusinessCodeTemp
	 * @return The property salvage questions
	 */
	protected Collection<ProfileStatementItem> retrievePropertySalvageQuestions(
			String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_PROPERTY_SALVAGE_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_PROPERTY_SALVAGE_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest.setSubAreaCategoryCode("");
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * @param lob
	 *            The lineOfBusinessCodeTemp
	 * @return The property salvage salvor questions
	 */
	protected Collection<ProfileStatementItem> retrievePropertySalvageSalvorQuestions(
			String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_PROPERTY_SALVAGE_SALVOR_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_PROPERTY_SALVAGE_SALVOR_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest.setSubAreaCategoryCode("");
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * @param lob
	 *            The lineOfBusinessCodeTemp
	 * @return The vehicle salvage questions
	 */
	protected Collection<ProfileStatementItem> retrieveLocationSalvageQuestions(
			String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_LOCATION_SALVAGE_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_LOCATION_SALVAGE_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest.setSubAreaCategoryCode("");
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * @param lob
	 *            The lineOfBusinessCodeTemp
	 * @return The vehicle salvage salvor questions
	 */
	protected Collection<ProfileStatementItem> retrieveLocationSalvageSalvorQuestions(
			String lob) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_LOCATION_SALVAGE_SALVOR_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_LOCATION_SALVAGE_SALVOR_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest.setSubAreaCategoryCode("");
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * Retrieves injury profile statement items from enterprise config service.
	 * 
	 * @param lobCode
	 *            LOB Code for which to retrieve Location Inj Profiles
	 * @return injury profile statements requested.
	 */
	public Collection<ProfileStatementItem> retrieveLocationInjuryProfileStatements(
			String lobCode) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lobCode);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_LOCATION_PARTICIPANT_INJURY_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_LOCATION_PARTICIPANT_INJURY_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest
				.setSubAreaCategoryCode(ClaimsServiceConstants.PROFILE_LOCATION_PARTICIPANT_INJURY_QUESTIONS_SUB_AREA_CATEGORY_CD);
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * Retrieves other carriers profile statement items from enterprise config
	 * service.
	 * 
	 * @param otherCarriersSubAreaCd
	 *            Sub Area code.
	 * @param lobCode
	 *            LOB Code for which to retrieve Other Carrier Profiles
	 * @return other carriers profile statements requested.
	 */
	protected Collection<ProfileStatementItem> retrieveOtherCarriersProfileStatements(
			String otherCarriersSubAreaCd, String lobCode) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lobCode);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_OTHER_CARRIERS_QUESTIONS_AREA_CD);
		profileStatementRequest.setSubAreaCode(otherCarriersSubAreaCd);
		profileStatementRequest
				.setSubAreaCategoryCode(ClaimsServiceConstants.PROFILE_OTHER_CARRIERS_QUESTIONS_SUB_AREA_CATEGORY_CD);
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
	}

	/**
	 * Retrieves damage vehicle profile statement items from enterprise config
	 * service.
	 * 
	 * @param lob
	 *            The line of business
	 * @param subAreaCategory
	 *            The subAreaCategory
	 * @return the profile statements requested.
	 */
	protected Collection<ProfileStatementItem> retrieveDamageVehicleProfileStatements(
			String lob, String subAreaCategory) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lob);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_UNIT_DAMAGE_VEHICLE_AREA);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_UNIT_DAMAGE_VEHICLE_SUBAREA);
		profileStatementRequest.setSubAreaCategoryCode(subAreaCategory);
		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
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

	/**
	 * @param lobCode
	 *            The line of business
	 * @param participationTypeCode
	 *            The participation Type Code
	 * @return injury profile statements requested.
	 */
	public Collection<ProfileStatementItem> retrieveVehicleInjuryProfileStatements(
			String lobCode, String participationTypeCode) {
		EnterpriseConfigService entConfigService = getEnterpriseConfigService();
		ProfileStatementRequest profileStatementRequest = new ProfileStatementRequest();
		profileStatementRequest.setLineOfBusinessCode(lobCode);
		profileStatementRequest
				.setAreaCode(ClaimsServiceConstants.PROFILE_VEHICLE_PARTICIPANT_INJURY_QUESTIONS_AREA_CD);
		profileStatementRequest
				.setSubAreaCode(ClaimsServiceConstants.PROFILE_VEHICLE_PARTICIPANT_INJURY_QUESTIONS_SUB_AREA_CD);
		profileStatementRequest
				.setSubAreaCategoryCode(ClaimsServiceConstants.PROFILE_VEHICLE_PARTICIPANT_INJURY_QUESTIONS_SUB_AREA_CATEGORY_CD);
		profileStatementRequest.setParticipationTypeCode(participationTypeCode);

		return ProfileStatementHelper.trimProfileStatementResponseForAnswers(entConfigService
				.retrieveProfileStatements(profileStatementRequest));
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

		return getAllClaimUnitService().retrieveProfileStatementsWithAnswers(
				claimId, profileStatementRequest);
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
				claimId, ClaimsServiceConstants.PROFILE_LOSS_QUESTIONS_AREA,
				ClaimsServiceConstants.PROFILE_MEDICARE_QUESTIONS_SUBAREA,
				null, lobCode, null);

		return profiles;
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
	 * Retrieves Other Unit Entities like Other Carries, Service Profile
	 * Statements and Vendor Categories.
	 * 
	 * @param otherUnitCompositeDTO
	 *            Other Unit Information
	 * @param claimUnitRequestDTO
	 *            Claim Unit RequestDTO
	 * @return otherUnitCompositeDTO
	 */
	public ClaimOtherCompositeDTO retrieveOtherUnitEntities(
			ClaimOtherCompositeDTO otherUnitCompositeDTO,
			ClaimUnitRequestDTO claimUnitRequestDTO) {

		// retrieve Damage Questions
		otherUnitCompositeDTO
				.setDamageQuestions(retrieveProfileStatementsWithAnswers(
						claimUnitRequestDTO.getClaimId(),
						ClaimsServiceConstants.PROFILE_OTHER_DAMAGES_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_DAMAGES_QUESTIONS_SUB_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_DAMAGES_QUESTIONS_SUB_AREA_CATEGORY_CD,
						claimUnitRequestDTO.getLobCode(), null));

		// don't retrieve treatment vendor categories
		/*
		 * otherUnitCompositeDTO
		 * .setTreatmentVendorCategories(retrieveVendorCategories(
		 * ClaimsAllParticipationConstants.PARTICIPATION_AREA_PROVIDER,
		 * ClaimsServiceConstants.UNIT_DATA_TYPE_OTHER, claimUnitRequestDTO));
		 */

		// retrieve Damage Service Questions
		otherUnitCompositeDTO
				.setServicesQuestions(retrieveProfileStatementsWithAnswers(
						claimUnitRequestDTO.getClaimId(),
						ClaimsServiceConstants.PROFILE_OTHER_SERVICE_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_SERVICE_QUESTIONS_SUB_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_SERVICE_QUESTIONS_SUB_AREA_CATEGORY_CD,
						claimUnitRequestDTO.getLobCode(), null));

		// retrieve Other Carrier
		otherUnitCompositeDTO
				.setConfiguredOtherCarriersQuestions(retrieveProfileStatementsWithAnswers(
						claimUnitRequestDTO.getClaimId(),
						ClaimsServiceConstants.PROFILE_OTHRCAR_OTHER_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHRCAR_OTHER_QUESTIONS_SUB_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHRCAR_OTHER_QUESTIONS_SUB_AREA_CATEGORY_CD,
						claimUnitRequestDTO.getLobCode(), null));

		// retrieve description questions
		otherUnitCompositeDTO
				.setDescriptionQuestions(retrieveProfileStatementsWithAnswers(
						claimUnitRequestDTO.getClaimId(),
						ClaimsServiceConstants.PROFILE_OTHER_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_UNIT_OTHER_DESCRIPTION_SUB_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_QUESTIONS_SUB_AREA_CATEGORY_CD,
						claimUnitRequestDTO.getLobCode(), null));

		otherUnitCompositeDTO
				.setInjuryQuestions(retrieveProfileStatementsWithAnswers(
						claimUnitRequestDTO.getClaimId(),
						ClaimsServiceConstants.PROFILE_OTHER_INJURY_QUESTIONS_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_INJURY_QUESTIONS_SUB_AREA_CD,
						ClaimsServiceConstants.PROFILE_OTHER_INJURY_QUESTIONS_SUB_AREA_CATEGORY_CD,
						claimUnitRequestDTO.getLobCode(), null));

		return otherUnitCompositeDTO;
	}

}
