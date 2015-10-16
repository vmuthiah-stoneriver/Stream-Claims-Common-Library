package com.client.iip.integration.claim.details;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.client.iip.integration.claim.imports.ClaimImportCompositeDTO;
import com.client.iip.integration.party.ClientOrganizationDTO;
import com.client.iip.integration.party.ClientPartyServiceImpl;
import com.client.iip.integration.party.ClientPersonDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.bc.party.api.PersonDTO;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.casetool.api.CaseDTO;
import com.stoneriver.iip.casetool.api.participant.CaseParticipationDTO;
import com.stoneriver.iip.claims.CALClaimParticipationContactDTO;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;
import com.stoneriver.iip.claims.CALClaimParticipationProvDTO;
import com.stoneriver.iip.claims.authority.ClaimParticipationAuthorityDTO;
import com.stoneriver.iip.claims.composite.ClaimAllUnitsCompositeService;
import com.stoneriver.iip.claims.composite.search.ClaimsPolicyCompositeHelper;
import com.stoneriver.iip.claims.details.ClaimDetailRequestCriteria;
import com.stoneriver.iip.claims.details.ClaimDetailsResponseDTO;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecovery;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecoveryPayable;
import com.stoneriver.iip.claims.payment.ClaimDisbursementAdditionalPayee;
import com.stoneriver.iip.claims.payment.ClaimPayable;
import com.stoneriver.iip.claims.payment.ClaimPayment;
import com.stoneriver.iip.claims.payment.ClaimPaymentDisbursement;
import com.stoneriver.iip.claims.policy.ClaimPolicyAgencyParticipationDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyDriverParticipationDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyFinancialInterestParticipationDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyInsuredParticipationDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyParticipationDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitVehicleDTO;
import com.stoneriver.iip.claims.reserve.CALClaimReserveDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitSummaryDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitVehicleDTO;
import com.stoneriver.iip.entconfig.EnterpriseUserConfigurationDAO;
import com.stoneriver.iip.entconfig.users.bo.UserAuthorizationInfoBO;

/**
 * @author vmuthiah
 *
 */
@Pojo(id = "com.client.iip.integration.claims.details.ClaimDetailsHelper")
public class ClaimDetailsHelper {
	
	EnterpriseUserConfigurationDAO enterpriseUserConfigDAO;
	//Map to hold Login Code for a User Id
	Map<Long, String> userLoginMap = new HashMap<Long, String>();
	
	//Map to hold External Source Id for Claim Policy Unit
	Map<Long, String> claimPolicyUnitExternalSourceId = new HashMap<Long, String>();
	
	// Variable to hold the Parties retrieved for Claim Details
	private List<Long> partyList = new ArrayList<Long>();

	ClientPartyServiceImpl clientPartyDetailsCompositeServiceImpl;

	/**
	 * @return the enterpriseUserConfigDAO
	 */
	public EnterpriseUserConfigurationDAO getEnterpriseUserConfigDAO() {
		return enterpriseUserConfigDAO;
	}
	
	/**
	 * @param configDAO the enterpriseUserConfigDAO to set
	 */
	@Inject(DaoInterface = "entconfig.daointerface.entconfigUserConfigurationDAO")
	public void setEnterpriseUserConfigDAO(
			EnterpriseUserConfigurationDAO configDAO) {
		this.enterpriseUserConfigDAO = configDAO;
	}
	
	/**
	 * @return the userLoginMap
	 */
	public Map<Long, String> getUserLoginMap() {
		return userLoginMap;
	}
	/**
	 * @param userLoginMap the userLoginMap to set
	 */
	public void setUserLoginMap(Map<Long, String> loginMap) {
		this.userLoginMap = loginMap;
	}
	
	public String getLoginName(Long userId) {
		if(userLoginMap.containsKey(userId)) {
			return userLoginMap.get(userId);
		} else {
			Collection<UserAuthorizationInfoBO> userLoginDetails = 
				getEnterpriseUserConfigDAO().retrieveUserAuthorizationInfo(userId);
			
			if(userLoginDetails != null  && !userLoginDetails.isEmpty()) {
				UserAuthorizationInfoBO userAuthorizationInfoBO = userLoginDetails.iterator().next();
				userLoginMap.put(userId, userAuthorizationInfoBO.getLoginId());
				return userAuthorizationInfoBO.getLoginId();
			}
		}
		return null;
	}
	
	/**
	 * Formats Claim Policy information to be in synch with Claim Import layout 
	 * @param returnDetailsForImport
	 * @param claimsPolicyCompositeHelper
	 * @param clmDetailResponse
	 */
	public void processClaimPolicy(boolean returnDetailsForImport, 
			ClaimsPolicyCompositeHelper claimsPolicyCompositeHelper, ClaimDetailsResponseDTO clmDetailResponse) {
		ClaimPolicyDTO clmPolicyDTO = clmDetailResponse.getClaimDTO().getPolicy();

		getClaimPolicyUnitExternalSourceId().clear();

		// populate claim policy party info.
		claimsPolicyCompositeHelper.normalizePolicyDetails(clmPolicyDTO);

		// Claim Details returns a Collection of ClaimPolicyParticipationDTO
		// which contains ClaimPolicyFinancialInterestParticipationDTO,
		// ClaimPolicyDriverParticipationDTO,
		// ClaimPolicyAgencyParticipationDTO,
		// ClaimPolicyInsuredParticipationDTO.
		// For Claim Import we need the Participants as separate Collection.
		// So we loop thru' the Collection of ClaimPolicyParticipationDTO
		// add to the corresponding Collections.
		if (clmPolicyDTO != null
				&& clmPolicyDTO.getParticipations() != null) {

			for (ClaimPolicyParticipationDTO participantDTO : clmPolicyDTO
					.getParticipations()) {
				if(participantDTO.getParticipationEffectiveDate() == null) {
					participantDTO.setParticipationEffectiveDate(DateUtility.getSystemDateTime());
				}
				participantDTO.setAlreadyUpdated(true);
				if (participantDTO instanceof ClaimPolicyFinancialInterestParticipationDTO) {
					clmPolicyDTO.getClmPolicyFinancialInterestParticipants()
							.add((ClaimPolicyFinancialInterestParticipationDTO) participantDTO);
				} else if (participantDTO instanceof ClaimPolicyDriverParticipationDTO) {
					clmPolicyDTO.getClmPolicyDriverParticipants()
							.add((ClaimPolicyDriverParticipationDTO) participantDTO);
				} else if (participantDTO instanceof ClaimPolicyAgencyParticipationDTO) {
					clmPolicyDTO.getClmPolicyAgencyParticipants()
							.add((ClaimPolicyAgencyParticipationDTO) participantDTO);
				} else if (participantDTO instanceof ClaimPolicyInsuredParticipationDTO) {
					clmPolicyDTO.getClmPolicyInsuredParticipants()
							.add((ClaimPolicyInsuredParticipationDTO) participantDTO);
				}
			}
			clmPolicyDTO.setParticipations(null);
		}
		// For Claim Import Claim Policy Units should be a Collection of
		// ClaimPolicyUnitVehicleDTO.
		// Claim Details returns the Collection with ClaimPolicyUnitDTO and
		// ClaimPolicyUnitVehicleDTO.
		if (clmPolicyDTO != null && clmPolicyDTO.getUnits() != null) {

			Collection<ClaimPolicyUnitDTO> claimPolicyUnits = new ArrayList<ClaimPolicyUnitDTO>();

			for (ClaimPolicyUnitDTO claimPolicyUnit : clmPolicyDTO.getUnits()) {
				if (returnDetailsForImport) {
					getClaimPolicyUnitExternalSourceId().put(claimPolicyUnit.getRecordId(), claimPolicyUnit.getExternalSourceId());
				}
				if (claimPolicyUnit instanceof ClaimPolicyUnitDTO) {
					ClaimPolicyUnitVehicleDTO claimPolicyVehicleUnit = new ClaimPolicyUnitVehicleDTO();
					BeanUtils.copyProperties(claimPolicyUnit,claimPolicyVehicleUnit);
					claimPolicyUnits.add(claimPolicyVehicleUnit);
				} else {
					claimPolicyUnits.add((ClaimPolicyUnitVehicleDTO) claimPolicyUnit);
				}
			}
			clmPolicyDTO.setUnits(claimPolicyUnits);
			/*
			 * for(ClaimPolicyUnitDTO claimPolicyVehicleUnit:
			 * clmPolicyDTO.getUnits()) { if(claimPolicyVehicleUnit
			 * instanceof ClaimPolicyUnitVehicleDTO) {
			 * claimPolicyUnits.add(claimPolicyVehicleUnit); } }
			 */
		}
	}
	
	/**
	 * Formats Claim Unit information to be in synch with Claim Import layout 
	 * @param clmDetailResponse
	 * @param returnDetailsForImport
	 */
	public void processClaimUnits(ClaimDetailsResponseDTO clmDetailResponse, boolean returnDetailsForImport) {
		Collection<ClaimUnitDTO> returnedClaimUnits = new ArrayList<ClaimUnitDTO>();
		Collection<ClaimUnitDTO> claimUnitIds = clmDetailResponse.getUnits();
		for (ClaimUnitDTO claimUnitDTO : claimUnitIds) {
			ClaimUnitSummaryDTO claimUnitSummaryDTO = new ClaimUnitSummaryDTO();
			claimUnitSummaryDTO.setRecordId(claimUnitDTO.getRecordId());
			claimUnitSummaryDTO.setUnitDataTypeCode(claimUnitDTO.getUnitDataTypeCode());
			claimUnitSummaryDTO.setLobCode(clmDetailResponse.getClaimDTO().getLobCode());
			claimUnitSummaryDTO.setUnitSubTypeCode(claimUnitDTO.getUnitSubTypeCode());
			claimUnitSummaryDTO.setClaimId(clmDetailResponse.getClaimId());
			claimUnitSummaryDTO.setIncludeProfileList(false);
			claimUnitDTO = MuleServiceFactory.getService(
					ClaimAllUnitsCompositeService.class).retrieveUnit(claimUnitSummaryDTO);
			if (claimUnitDTO.getClaimSalvageDTO() != null
					&& claimUnitDTO.getClaimSalvageDTO().getSalvageDetailsDTO() != null
					&& claimUnitDTO.getClaimSalvageDTO().getSalvageDetailsDTO().getRetainedByOwner() == null) {
				claimUnitDTO.getClaimSalvageDTO().getSalvageDetailsDTO().setRetainedByOwner(false);
			}

			if (returnDetailsForImport && claimUnitDTO.getPolicyUnitId() != null) {
				String policyUnitExternalSourceId = getClaimPolicyUnitExternalSourceId().get(claimUnitDTO.getPolicyUnitId());
				
				if(policyUnitExternalSourceId != null && claimUnitDTO.getExternalSourceId() == null) {
					claimUnitDTO.setExternalSourceId(policyUnitExternalSourceId + "_" + claimUnitDTO.getRecordId());
				} else if(policyUnitExternalSourceId != null && 
						policyUnitExternalSourceId.indexOf(claimUnitDTO.getExternalSourceId()) == -1) {
					claimUnitDTO.setExternalSourceId(policyUnitExternalSourceId + 
								"_" + claimUnitDTO.getExternalSourceId());
				}
			}

			if (claimUnitDTO instanceof ClaimUnitVehicleDTO) {
				returnedClaimUnits.add((ClaimUnitVehicleDTO) claimUnitDTO);
			} else {
				ClaimUnitVehicleDTO claimUnitVehicle = new ClaimUnitVehicleDTO();
				BeanUtils.copyProperties(claimUnitDTO, claimUnitVehicle);
				returnedClaimUnits.add(claimUnitVehicle);
			}
		}
		clmDetailResponse.setUnits(returnedClaimUnits);
	}
	
	/**
	 * @param request ClaimDetailRequestCriteria
	 * @param returnObject ClaimImportCompositeDTO
	 */
	public void processClaimDetailsForImport(ClaimDetailRequestCriteria request, ClaimImportCompositeDTO returnObject) {
		// to import Reserve, Payment and Recovery from Claim Details
		// response using Claim Import
		// we need to set external identifier for Unit and Participant and
		// set the same for Reserve, Payment and Recovery

		// First create a List of Unit/Participant Record Id and use the
		// List Index as the external identifier.
		List<String> lstUnitParticipantRecordId = new ArrayList<String>();
		
		Map<Long, String> claimUnitExternalSourceId = new HashMap<Long, String>();
		if (returnObject.getUnits() != null) {
			for (ClaimUnitDTO claimUnit : returnObject.getUnits()) {
				String unitRecordId = claimUnit.getRecordId().toString();
				lstUnitParticipantRecordId.add(unitRecordId);
				
				if(claimUnit.getExternalSourceId() == null) {
					claimUnit.setExternalSourceId("" + (lstUnitParticipantRecordId.indexOf(unitRecordId) + 1));
				}
				
				claimUnitExternalSourceId.put(claimUnit.getRecordId(), claimUnit.getExternalSourceId());


				if (claimUnit instanceof ClaimUnitVehicleDTO) {
					ClaimUnitVehicleDTO claimUnitVehicle = (ClaimUnitVehicleDTO) claimUnit;

					retrieveParticipantRecordId(claimUnitVehicle.getRecordId(), claimUnitVehicle.getDrivers(),lstUnitParticipantRecordId);
					retrieveParticipantRecordId(claimUnitVehicle.getRecordId(), claimUnitVehicle.getPassengers(), lstUnitParticipantRecordId);
				}

				retrieveParticipantRecordId(claimUnit.getRecordId(),claimUnit.getFinancialInterestList(),lstUnitParticipantRecordId);
				retrieveParticipantRecordId(claimUnit.getRecordId(),claimUnit.getInjuredPersonList(),lstUnitParticipantRecordId);
				retrieveParticipantRecordId(claimUnit.getRecordId(),claimUnit.getOtherPartiesList(),lstUnitParticipantRecordId);
				retrieveParticipantRecordId(claimUnit.getRecordId(),claimUnit.getOwners(), lstUnitParticipantRecordId);
			}
		}

		// Set the Identifier for Reserve to associate to Unit and
		// Participant
		// This is require for Claim Import if Units, Participants and
		// Reserves are imported at the same time
		if (request.isReturnClaimReservesWithCoverages()
				&& returnObject.getClaimReservesWithCoverages() != null) {
			for (CALClaimReserveDTO claimReserveDTO : returnObject.getClaimReservesWithCoverages()) {
				
				claimReserveDTO.setExternalUnitIdentifier(claimUnitExternalSourceId.get(claimReserveDTO.getCalClaimUnitIdDerived()));
				
				if(claimReserveDTO.getExternalUnitIdentifier() == null) {
					claimReserveDTO.setExternalUnitIdentifier("" + 
					(lstUnitParticipantRecordId.indexOf(claimReserveDTO.getCalClaimUnitIdDerived().toString()) + 1));
				}
				
				claimReserveDTO.setExternalParticipantIdentifier("" + 
						(lstUnitParticipantRecordId.indexOf(claimReserveDTO.getCalClaimUnitIdDerived().toString()
										+ "_" 
										+ claimReserveDTO.getCalClaimPtcpId().toString()) + 1));
				claimReserveDTO.setExternalClaimIdentifier(null);

				claimReserveDTO.setClaimNumber(null);
				claimReserveDTO.setCalClaimIdDerived(null);
				claimReserveDTO.setCalClaimPtcpId(null);
				claimReserveDTO.setCalClaimUnitIdDerived(null);

				if (claimReserveDTO.getUser() == null) {
					claimReserveDTO.setUser(getLoginName(claimReserveDTO.getUserIdCreated()));
				}
				
				if(claimReserveDTO.getAllAgreementData() != null 
						&& !claimReserveDTO.getAllAgreementData().isEmpty()) {
					claimReserveDTO.getAllAgreementData().clear();
				}
			}
		}

		// Set the Identifier for Payment to associate to Unit and
		// Participant
		// This is require for Claim Import if Units, Participants, Reserves
		// and Payments are imported at the same time
		if (request.isReturnClaimPayment() && returnObject.getClaimPayments() != null) {
			for (ClaimPayment claimPayment : returnObject.getClaimPayments()) {
				if (claimPayment.getClaimPayables() != null) {
					for (ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
						if (claimPayable.getUnitId() != null) {
							
							claimPayable.setExternalUnitIdentifier(claimUnitExternalSourceId.get(claimPayable.getUnitId()));
							
							if(claimPayable.getExternalUnitIdentifier() == null) {
								claimPayable.setExternalUnitIdentifier("" + (lstUnitParticipantRecordId.indexOf(claimPayable.getUnitId().toString()) + 1));
							}
							if (claimPayable.getParticipantId() != null) {
								claimPayable.setExternalParticipantIdentifier(""
												+ (lstUnitParticipantRecordId.indexOf(claimPayable.getUnitId().toString()
												+ "_"
												+ claimPayable.getParticipantId().toString()) + 1));
							}
						}
						claimPayable.setExternalClaimIdentifier(null);
						claimPayable.setClaimId(null);
						claimPayable.setClaimNumber(null);
						claimPayable.setUnitId(null);
						claimPayable.setParticipantId(null);
					}
				}
			}
		}

		// Set the Identifier for Recovery to associate to Unit and
		// Participant
		// This is require for Claim Import if Units, Participants, Reserves
		// and Recoveries are imported at the same time
		if (request.isReturnClaimRecovery()
				&& returnObject.getRecoveries() != null) {
			for (ClaimRecovery claimRecovery : returnObject.getRecoveries()) {
				if (claimRecovery.getClaimRecoveryPayables() != null) {
					for (ClaimRecoveryPayable claimRecoveryPayable : claimRecovery.getClaimRecoveryPayables()) {

						claimRecoveryPayable.setExternalUnitIdentifier(""
										+ (lstUnitParticipantRecordId.indexOf(claimRecoveryPayable.getUnitId().toString()) + 1));
						claimRecoveryPayable.setExternalParticipantIdentifier(""
										+ (lstUnitParticipantRecordId.indexOf(claimRecoveryPayable.getUnitId().toString()
										+ "_"
										+ claimRecoveryPayable.getParticipantId().toString()) + 1));
						claimRecoveryPayable.setExternalClaimIdentifier(null);

						claimRecoveryPayable.setClaimId(null);
						claimRecoveryPayable.setClaimNumber(null);
						claimRecoveryPayable.setUnitId(null);
						claimRecoveryPayable.setParticipantId(null);
					}
				}
			}
		}

		partyList = new ArrayList<Long>();

		// get party id for all participants returned from Claim Details and
		// add it to a Collection.
		returnObject.setPersons(new ArrayList<ClientPersonDTO>());
		returnObject.setOrganizations(new ArrayList<ClientOrganizationDTO>());
		List<DataTransferObject> lstParticipants = new ArrayList<DataTransferObject>();
		if (returnObject.getClaimDTO().getClmParticipants() != null
				&& !returnObject.getClaimDTO().getClmParticipants().isEmpty()) {

			lstParticipants.addAll(returnObject.getClaimDTO().getClmParticipants());
			retrieveParticipantEntities(returnObject.getClaimDTO().getClmParticipants(), lstParticipants);
		}

		// Iterate thru' Claim Policy and add all participants to the List
		// to set Logical Id for Parties
		if (request.isReturnClaimPolicy()
				&& returnObject.getClaimDTO().getPolicy() != null) {
			ClaimPolicyDTO claimPolicy = returnObject.getClaimDTO().getPolicy();
			if (claimPolicy.getClmPolicyAgencyParticipants() != null) {
				lstParticipants.addAll(claimPolicy.getClmPolicyAgencyParticipants());
			}
			if (claimPolicy.getClmPolicyDriverParticipants() != null) {
				lstParticipants.addAll(claimPolicy.getClmPolicyDriverParticipants());
			}
			if (claimPolicy.getClmPolicyFinancialInterestParticipants() != null) {
				lstParticipants.addAll(claimPolicy.getClmPolicyFinancialInterestParticipants());
			}
			if (claimPolicy.getClmPolicyInsuredParticipants() != null) {
				lstParticipants.addAll(claimPolicy.getClmPolicyInsuredParticipants());
			}

			if (claimPolicy.getUnits() != null) {
				for (ClaimPolicyUnitDTO claimPolicyUnit : claimPolicy.getUnits()) {
					if (claimPolicyUnit.getPartyId() != null) {
						claimPolicyUnit.setObjectId(retrieveParty(returnObject, claimPolicyUnit.getPartyId()).toString());
						claimPolicyUnit.setPartyId(null);
					}
					if (claimPolicyUnit.getParticipations() != null) {
						lstParticipants.addAll(claimPolicyUnit.getParticipations());
					}
				}
			}
		}

		// Iterate thru' Claim Services and add all participants to the List
		// to set Logical Id for Parties
		if (returnObject.getClaimServices() != null
				&& !returnObject.getClaimServices().isEmpty()) {
			lstParticipants.addAll(returnObject.getClaimServices());
			retrieveParticipantEntities(returnObject.getClaimServices(),lstParticipants);
		}

		// Iterate thru' Units and add all participants to the List to set
		// Logical Id for Parties
		if (returnObject.getUnits() != null) {
			for (ClaimUnitDTO claimUnit : returnObject.getUnits()) {
				if (claimUnit instanceof ClaimUnitVehicleDTO) {
					ClaimUnitVehicleDTO claimUnitVehicle = (ClaimUnitVehicleDTO) claimUnit;
					if (claimUnitVehicle.getDrivers() != null
							&& !claimUnitVehicle.getDrivers().isEmpty()) {
						lstParticipants.addAll(claimUnitVehicle.getDrivers());
						retrieveParticipantEntities(claimUnitVehicle.getDrivers(),lstParticipants);
					}
					if (claimUnitVehicle.getPassengers() != null
							&& !claimUnitVehicle.getPassengers().isEmpty()) {
						lstParticipants.addAll(claimUnitVehicle.getPassengers());
						retrieveParticipantEntities(claimUnitVehicle.getPassengers(),lstParticipants);
					}
				}

				if (claimUnit.getClaimDamageDTO() != null
						&& claimUnit.getClaimDamageDTO().getDamageServicesDTO() != null) {
					lstParticipants.addAll(claimUnit.getClaimDamageDTO().getDamageServicesDTO());
					retrieveParticipantEntities(claimUnit.getClaimDamageDTO().getDamageServicesDTO(),lstParticipants);
				}
				if (claimUnit.getClaimSalvageDTO() != null
						&& claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO() != null) {
					lstParticipants.addAll(claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO());
					retrieveParticipantEntities(claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO(),lstParticipants);
				}
				if (claimUnit.getFinancialInterestList() != null
						&& !claimUnit.getFinancialInterestList().isEmpty()) {
					lstParticipants.addAll(claimUnit.getFinancialInterestList());
					retrieveParticipantEntities(claimUnit.getFinancialInterestList(),lstParticipants);
				}
				if (claimUnit.getInjuredPersonList() != null
						&& !claimUnit.getInjuredPersonList().isEmpty()) {
					lstParticipants.addAll(claimUnit.getInjuredPersonList());
					retrieveParticipantEntities(claimUnit.getInjuredPersonList(),lstParticipants);
				}
				if (claimUnit.getOtherCarriersList() != null
						&& !claimUnit.getOtherCarriersList().isEmpty()) {
					lstParticipants.addAll(claimUnit.getOtherCarriersList());
					retrieveParticipantEntities(claimUnit.getOtherCarriersList(),lstParticipants);
				}
				if (claimUnit.getOtherPartiesList() != null
						&& !claimUnit.getOtherPartiesList().isEmpty()) {
					lstParticipants.addAll(claimUnit.getOtherPartiesList());
					retrieveParticipantEntities(claimUnit.getOtherPartiesList(),lstParticipants);
				}
				if (claimUnit.getOwners() != null
						&& !claimUnit.getOwners().isEmpty()) {
					lstParticipants.addAll(claimUnit.getOwners());
					retrieveParticipantEntities(claimUnit.getOwners(),lstParticipants);
				}
			}
		}

		// Add Authorities to the Claim Participant Collection
		if (returnObject.getAuthorities() != null) {
			lstParticipants.addAll(returnObject.getAuthorities());
			retrieveParticipantEntities(returnObject.getAuthorities(),lstParticipants);
			for (ClaimParticipationAuthorityDTO authorityDTO : returnObject.getAuthorities()) {
				retrieveParticipantEntities(authorityDTO.getCitations(),lstParticipants);
			}
		}
		// Add Witnesses to the Claim Participant Collection
		if (returnObject.getWitnesses() != null) {
			lstParticipants.addAll(returnObject.getWitnesses());
			retrieveParticipantEntities(returnObject.getWitnesses(),lstParticipants);
		}

		// Iterate through case level participants to update party Id
		if (returnObject.getClaimCases() != null) {
			for (CaseDTO caseDTO : returnObject.getClaimCases()) {
				if (caseDTO.getParticipationCompositeDTO() != null
						&& caseDTO.getParticipationCompositeDTO().getCaseParticipations() != null
						&& !caseDTO.getParticipationCompositeDTO().getCaseParticipations().isEmpty()) {
					lstParticipants.addAll(caseDTO.getParticipationCompositeDTO().getCaseParticipations());
				}
			}
		}

		// For each party in Payments retrieve Party Information and set
		// logical Id so that
		// the response can be used for Claim Import
		if (request.isReturnClaimPayment()
				&& returnObject.getClaimPayments() != null) {
			for (ClaimPayment claimPayment : returnObject.getClaimPayments()) {
				if (claimPayment.getClaimPayables() != null) {
					for (ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
						if (claimPayable.getPartyId() != null) {
							claimPayable.setPartyId(retrieveParty(returnObject,claimPayable.getPartyId()));
						}
					}
				}
				if (claimPayment.getClaimPaymentDisbursements() != null) {
					for (ClaimPaymentDisbursement claimPaymentDisbursement : claimPayment
							.getClaimPaymentDisbursements()) {
						if (claimPaymentDisbursement.getPartyId() != null) {
							claimPaymentDisbursement.setPartyId(retrieveParty(returnObject,claimPaymentDisbursement.getPartyId()));
						}
						if (claimPaymentDisbursement.getInCareOfPayeeId() != null) {
							claimPaymentDisbursement
									.setInCareOfPayeeId(retrieveParty(returnObject,
											claimPaymentDisbursement.getInCareOfPayeeId()));
						}
						if (claimPaymentDisbursement.getAdditionalPayeeId() != null) {
							claimPaymentDisbursement.setAdditionalPayeeId(
									retrieveParty(returnObject,claimPaymentDisbursement.getAdditionalPayeeId()));
						}
						if (claimPaymentDisbursement.getPartyBankAccountIdPayee() != null) {
							// claimPaymentDisbursement.setPartyBankAccountIdPayee(retrieveParty(returnObject,
							// claimPaymentDisbursement.getPartyBankAccountIdPayee()));
						}
						if (claimPaymentDisbursement.getPartyIdPayee() != null) {
							claimPaymentDisbursement.setPartyIdPayee(
									retrieveParty(returnObject,claimPaymentDisbursement.getPartyIdPayee()));
						}
						if (claimPaymentDisbursement.getClaimDisbursementAdditionalPayees() != null) {
							for (ClaimDisbursementAdditionalPayee clmDisbursementPayees : claimPaymentDisbursement.getClaimDisbursementAdditionalPayees()) {
								if (clmDisbursementPayees.getPartyIdPayee() != null) {
									clmDisbursementPayees.setPartyIdPayee(
											retrieveParty(returnObject,clmDisbursementPayees.getPartyIdPayee()));
								}
							}
						}
					}
				}
			}
		}

		// For each party in Recoveries retrieve Party Information and set
		// logical Id so that
		// the response can be used for Claim Import
		if (request.isReturnClaimRecovery()
				&& returnObject.getRecoveries() != null) {
			for (ClaimRecovery claimRecovery : returnObject.getRecoveries()) {
				if (claimRecovery.getClaimRemittance() != null
						&& claimRecovery.getClaimRemittance().getRemitterPartyNumber() != null) {
					claimRecovery.getClaimRemittance().setRemitterPartyNumber(
									retrieveParty(returnObject,claimRecovery.getClaimRemittance().getRemitterPartyNumber()));
				}
			}
		}

		// For each participant in the Claim retrieve Party Information and
		// set logical Id so that
		// the response can be used for Claim Import
		retrieveParty(returnObject, lstParticipants);
	}

	/**
	 * Method adds Unit Record Id and Participant Record Id to the List
	 * 
	 * @param claimUnit
	 * @param lstParticipantDTO
	 * @param lstUnitParticipantRecordId
	 */
	private void retrieveParticipantRecordId(Long unitRecordId,
			Collection<? extends CALClaimParticipationDTO> lstParticipantDTO,
			List<String> lstUnitParticipantRecordId) {

		if (lstParticipantDTO != null) {
			for (CALClaimParticipationDTO participantDTO : lstParticipantDTO) {
				String participantUnitRecordId = unitRecordId + "_" + participantDTO.getRecordId();
				lstUnitParticipantRecordId.add(participantUnitRecordId);

				participantDTO.setExternalSourceId(""
						+ (lstUnitParticipantRecordId.indexOf(participantUnitRecordId) + 1));
			}
		}
	}

	/**
	 * Method get all Provider and Contact participants for a Participant.
	 * 
	 * @param lstParticipantDTO
	 *            Collection of CALClaimParticipationDTO from which to add
	 *            Contact and Providers
	 * @param lstParticipants
	 *            Collection into which to add Contact and Providers
	 */
	private void retrieveParticipantEntities(
			Collection<? extends CALClaimParticipationDTO> lstParticipantDTO,
			List<DataTransferObject> lstParticipants) {
		if (lstParticipantDTO != null && !lstParticipantDTO.isEmpty()) {
			for (CALClaimParticipationDTO participantDTO : lstParticipantDTO) {
				if (participantDTO.getContacts() != null
						&& !participantDTO.getContacts().isEmpty()) {
					lstParticipants.addAll(participantDTO.getContacts());
				}
				if (participantDTO.getProviders() != null
						&& !participantDTO.getProviders().isEmpty()) {
					lstParticipants.addAll(participantDTO.getProviders());
				}
			}
		}
	}

	/**
	 * Method to get the Party Information for Participants and return the
	 * Logical Id for Claim Import.
	 * 
	 * @param claimImportCompositeDTO
	 * @param partyId
	 * @return Logical Id for Claim Import
	 */
	private Long retrieveParty(ClaimImportCompositeDTO claimImportCompositeDTO,
			Long partyId) {
		Long partyIndex = null;
		if (partyList.contains(partyId)) {
			return new Long(partyList.indexOf(partyId) + 1);
		} else {
			PartyDTO partyDTO = getClientPartyService()
					.retrievePartyDetails(partyId);
			if (partyDTO != null) {
				partyList.add(partyId);
				partyDTO.setPreferences(null);
				partyDTO.setPartyPreference(null);
				partyIndex = new Long(partyList.indexOf(partyId) + 1);
				partyDTO.setRecordId(partyIndex);

				if (partyDTO instanceof PersonDTO) {
					claimImportCompositeDTO.getPersons().add((ClientPersonDTO) partyDTO);
				} else {
					claimImportCompositeDTO.getOrganizations().add((ClientOrganizationDTO) partyDTO);
				}
			}
			return partyIndex;
		}
	}

	/**
	 * Method to get the Party information for all participants and get the
	 * logical Id for claim import.
	 * 
	 * @param claimImportCompositeDTO
	 * @param lstParticipants
	 */
	private void retrieveParty(ClaimImportCompositeDTO claimImportCompositeDTO,
			List<DataTransferObject> lstParticipants) {
		for (DataTransferObject participant : lstParticipants) {
			Long partyId = null;
			if (participant instanceof CALClaimParticipationDTO) {
				partyId = ((CALClaimParticipationDTO) participant).getPartyId();
			} else if (participant instanceof CaseParticipationDTO) {
				partyId = ((CaseParticipationDTO) participant).getPartyId();
			} else if (participant instanceof CALClaimParticipationContactDTO) {
				partyId = ((CALClaimParticipationContactDTO) participant).getPartyId();
			} else if (participant instanceof ClaimPolicyParticipationDTO) {
				partyId = ((ClaimPolicyParticipationDTO) participant).getPartyId();
			} else if (participant instanceof CALClaimParticipationProvDTO) {
				partyId = ((CALClaimParticipationProvDTO) participant).getPartyId();
			}

			// If participant is unknown then party id will be null
			if (partyId == null) {
				continue;
			}

			Long partyIndex = null;
			if (partyList.contains(partyId)) {
				partyIndex = new Long(partyList.indexOf(partyId) + 1);
				participant.setObjectId(partyIndex.toString());
			} else {
				PartyDTO partyDTO = getClientPartyService()
						.retrievePartyDetails(partyId);
				if (partyDTO != null) {
					partyList.add(partyId);
					partyDTO.setPreferences(null);
					partyDTO.setPartyPreference(null);
					partyIndex = new Long(partyList.indexOf(partyId) + 1);
					partyDTO.setRecordId(partyIndex);
					participant.setObjectId(partyIndex.toString());
					partyDTO.setAuthorizations(null);
					if (partyDTO instanceof PersonDTO) {
						claimImportCompositeDTO.getPersons().add((ClientPersonDTO) partyDTO);
					} else {
						claimImportCompositeDTO.getOrganizations().add((ClientOrganizationDTO) partyDTO);
					}
				}
			}

			if (participant instanceof CALClaimParticipationDTO) {
				((CALClaimParticipationDTO) participant).setPartyId(null);
			} else if (participant instanceof CaseParticipationDTO) {
				((CaseParticipationDTO) participant).setPartyId(null);
			} else if (participant instanceof CALClaimParticipationContactDTO) {
				((CALClaimParticipationContactDTO) participant).setPartyId(null);
			} else if (participant instanceof ClaimPolicyParticipationDTO) {
				((ClaimPolicyParticipationDTO) participant).setPartyId(null);
			} else if (participant instanceof CALClaimParticipationProvDTO) {
				((CALClaimParticipationProvDTO) participant).setPartyId(null);
			}
		}
	}

	/**
	 * return the clientPartyDetailsCompositeServiceImpl.
	 * 
	 * @return ClientPartyDetailsCompositeServiceImpl
	 */
	private ClientPartyServiceImpl getClientPartyService() {
		if (clientPartyDetailsCompositeServiceImpl == null) {
			clientPartyDetailsCompositeServiceImpl = new ClientPartyServiceImpl();
		}
		return clientPartyDetailsCompositeServiceImpl;
	}
	
	/**
	 * @return the claimPolicyUnitExternalSourceId
	 */
	public Map<Long, String> getClaimPolicyUnitExternalSourceId() {
		return claimPolicyUnitExternalSourceId;
	}
}
