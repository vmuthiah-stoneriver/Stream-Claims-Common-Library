package com.client.iip.integration.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.claim.imports.ClaimImportCompositeDTO;
import com.client.iip.integration.core.util.CloneUtil;
import com.client.iip.integration.policy.ClientPolicyImportWrapperDTO;
import com.fiserv.isd.iip.bc.address.PartyAddressService;
import com.fiserv.isd.iip.bc.address.api.AddressCollectionDTO;
import com.fiserv.isd.iip.bc.address.api.AddressDTO;
import com.fiserv.isd.iip.bc.party.ClearPartyDuplicateService;
import com.fiserv.isd.iip.bc.party.PartyConstants;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.BlockDTO;
import com.fiserv.isd.iip.bc.party.api.DuplicatePartySearchResultComposite;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyDTO;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyRequestDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationNameDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.bc.party.api.PartyLienRestrictionDTO;
import com.fiserv.isd.iip.bc.party.api.PersonDTO;
import com.fiserv.isd.iip.bc.party.api.PersonNameDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleService;
import com.fiserv.isd.iip.bc.party.search.DuplicateSearchResult;
import com.fiserv.isd.iip.bc.party.search.PartySearchServiceImpl;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.stoneriver.iip.casetool.CaseConstants;
import com.stoneriver.iip.casetool.api.CaseDTO;
import com.stoneriver.iip.casetool.api.participant.CaseParticipationDTO;
import com.stoneriver.iip.claims.CALClaimParticipationContactDTO;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;
import com.stoneriver.iip.claims.CALClaimParticipationProvDTO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.authority.ClaimParticipationAuthorityDTO;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecovery;
import com.stoneriver.iip.claims.payment.ClaimDisbursementAdditionalPayee;
import com.stoneriver.iip.claims.payment.ClaimPayable;
import com.stoneriver.iip.claims.payment.ClaimPayment;
import com.stoneriver.iip.claims.payment.ClaimPaymentDisbursement;
import com.stoneriver.iip.claims.policy.ClaimPolicyParticipationDTO;
import com.stoneriver.iip.claims.unit.ClaimDamageServicesDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitVehicleDTO;
import com.stoneriver.iip.claims.witness.ClaimWitnessDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyFinancialInterestParticipationDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyFinancialInterestParticipationDetailDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyParticipationDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyUnitDTO;

/**
 * Policy Import processor to process policy details.
 * !!!! Warning - This class is not thread safe !!!
 * @author sudharsan.sriram
 *
 */
@Pojo(id="claims.client.import.ClaimPolicyPartyImportProcessor")
public class ClientClaimPolicyPartyImportProcessor {

	private Logger logger = LoggerFactory.getLogger(ClientClaimPolicyPartyImportProcessor.class);
	
	private ClearPartyDuplicateService clearPartyDuplicateService;
	
	private PartyService partyService;
	private PartyRoleService partyRoleService;
	
	private PartyAddressService partyAddressService;
	
	private static final String PARTY_THRESHOLD_LEVEL_HIGH = "hi";

	//Store the processed party recordId to avoid duplicate processes.
	Map<Long, Long> processedPartyMap = new HashMap<Long, Long>();
	//Copy the person/organization collection into a map so we can access by record id.
	Map<Long, PersonDTO> personMap = new HashMap<Long, PersonDTO>();
	Map<Long, OrganizationDTO> organizationMap = new HashMap<Long, OrganizationDTO>();
	
	HashMap<String, PartyDTO> partyNameMap = new HashMap<String, PartyDTO>();
	
	Map<String, PolicyFinancialInterestParticipationDTO> financialParticipantMap = new HashMap<String, PolicyFinancialInterestParticipationDTO>();
	
	Map<Long, MinimalPartyDTO> minimalPartyMap = new HashMap<Long, MinimalPartyDTO>();
	
	//TD36270 - 12/12/2012 @GR - unitIdentifier added for Financial interest filter.
	private int unitIdFilter = 0;
	/**
	 * Logic to iterate over the participants on the policy, and either create them in the
	 * party system, or retrieve them if a match already exists.
	 * @param wrapper policy data.
	 * @param partyMap Party - person and organization.
	 */

	public void createParties(ClientPolicyImportWrapperDTO wrapper, Map<Long, PartyDTO> partyMap) {
		personMap = new HashMap<Long, PersonDTO>();
		organizationMap = new HashMap<Long, OrganizationDTO>();
		processedPartyMap = new HashMap<Long, Long>();
		partyNameMap = new HashMap<String, PartyDTO>();
		financialParticipantMap = new HashMap<String, PolicyFinancialInterestParticipationDTO>();
		
		PolicyImportDTO policy = wrapper.getPolicy();
		Collection<PolicyParticipationDTO> participations = null;
		if(policy != null) {
			participations = policy.getParticipations();
		}
		Collection<ClientPersonDTO> persons = wrapper.getPersons();
		Collection<ClientOrganizationDTO> organizations = wrapper.getOrganizations();
		
		//Process person address details
		processPartyAddressDetails(persons);
		
		//Process organization address details
		processPartyAddressDetails(organizations);
		
		if(persons != null) {
			//Populate personMap
			for(PersonDTO person : persons) {
				Long recordId = person.getRecordId();
				if(recordId == null){
					throw new IIPCoreSystemException("RecordId cannot be null for person. Please check your policy xml.");
				}
				if(!personMap.containsKey(recordId)) {
					personMap.put(recordId, person);
					partyMap.put(recordId, person);
				}
			}
		}
		if(organizations != null) {
			//Populate organizationMap
			for(OrganizationDTO organization : organizations) {
				Long recordId = organization.getRecordId();
				if(recordId == null){
					throw new IIPCoreSystemException("RecordId cannot be null for organization. Please check your policy xml.");
				}
				if(!organizationMap.containsKey(recordId)) {
					organizationMap.put(recordId, organization);
					partyMap.put(recordId, organization);
				}
			}
		}
		
		if(policy != null) {
			//Populate unit level participants
			populateUnitLevelParticipants(policy);
			logger.info("Creating/retrieving parties for policy: {} ", policy.getPolicyNumber());
		}
		
		//Iterate through policy level participants to update party Id
		if(participations != null && !participations.isEmpty()){
			unitIdFilter = 0;
			Iterator<PolicyParticipationDTO> iterPolicyParticipants = participations==null?null:participations.iterator();
			while(iterPolicyParticipants != null && iterPolicyParticipants.hasNext()) {
				PolicyParticipationDTO policyParticipant = iterPolicyParticipants.next();
				//Set the effective date time for participation
				if(policyParticipant.getEffectiveDateTime() == null) {
					policyParticipant.setEffectiveDateTime(DateUtility.getSystemDateOnly());
				}
				associateParticipationReference(policyParticipant);
				// TD36270 - 12/12/2012 @GR - Fix for ConcurrentModification Exception
				//Remove the participant if set to "remove" -- Logic applied for financial interest participants
				if(policyParticipant.getObjectId() != null && policyParticipant.getObjectId().equals("remove")){
					 logger.info("Policy Participant removed: " + policyParticipant.getParticipationTypeCd());
					 iterPolicyParticipants.remove();
					 //policy.getParticipations().remove(policyParticipant);
				}
				
			}

		}
				
		wrapper.setPartiesLoaded(true);
	}

	/**
	 * Logic to iterate over the participants on the Claim, and either create them in the
	 * party system, or retrieve them if a match already exists.
	 * @param claimImportDTO Claim Import Data.
	 * @param partyMap Party - person and organization.
	 */

	public void createClaimParties(ClaimImportCompositeDTO claimImportDTO, Map<Long, PartyDTO> partyMap) {
		
		Collection<ClientPersonDTO> persons = new ArrayList<ClientPersonDTO>();
		Collection<ClientOrganizationDTO> organizations = new ArrayList<ClientOrganizationDTO>(); 
		
		if(claimImportDTO.getPersons()!=null){
			persons = claimImportDTO.getPersons();	
		}
		if(claimImportDTO.getOrganizations()!=null){
			organizations = claimImportDTO.getOrganizations();
		}
		
		//Process person address details
		processPartyAddressDetails(persons);
		
		//Process organization address details
		processPartyAddressDetails(organizations);
		
		//Populate personMap
		for(PersonDTO person : persons) {
			Long recordId = person.getRecordId();
			if(recordId == null){
				throw new IIPCoreSystemException("RecordId cannot be null for person. Please check your Claim Import xml.");
			}
			if(!personMap.containsKey(recordId)) {
				personMap.put(recordId, person);
				partyMap.put(recordId, person);
			}
		}
		//Populate organizationMap
		for(OrganizationDTO organization : organizations) {
			Long recordId = organization.getRecordId();
			if(recordId == null){
				throw new IIPCoreSystemException("RecordId cannot be null for organization. Please check your Claim Import xml.");
			}
			if(!organizationMap.containsKey(recordId)) {
				organizationMap.put(recordId, organization);
				partyMap.put(recordId, organization);
			}
		}
		
		Collection<CALClaimParticipationDTO> claimParticipations = new ArrayList<CALClaimParticipationDTO>();
		Collection<CALClaimParticipationDTO> claimParticipationproviders = new ArrayList<CALClaimParticipationDTO>();
		Collection<CALClaimParticipationContactDTO> claimParticipationContactDTOs = new ArrayList<CALClaimParticipationContactDTO>();
		if(claimImportDTO.getClaimDTO() != null && claimImportDTO.getClaimDTO().getClmParticipants() != null) {
			claimParticipations = claimImportDTO.getClaimDTO().getClmParticipants();
		}
		
		//Add Claim Services participants to the Claim Participant Collection
		if(claimImportDTO.getClaimServices() != null 
				&& !claimImportDTO.getClaimServices().isEmpty()) {
			claimParticipations.addAll(claimImportDTO.getClaimServices());
			for(ClaimDamageServicesDTO claimServices : claimImportDTO.getClaimServices()) {
				if(claimServices.isParticipantUnknown()
						|| (claimServices.getParticipation() != null 
								&& claimServices.getParticipation().isUnknownParty())  ){
					continue;
				} else {
					claimParticipationContactDTOs.addAll(claimServices.getContacts());
					claimParticipationproviders.addAll(claimServices.getProviders());
				}
			}
		}
		
		//Add Unit Participants to the Claim Participant Collection
		if(claimImportDTO.getUnits() != null) {
			for(ClaimUnitDTO claimUnit : claimImportDTO.getUnits()) {
				if(claimUnit instanceof ClaimUnitVehicleDTO) {
					ClaimUnitVehicleDTO claimUnitVehicle = (ClaimUnitVehicleDTO) claimUnit;
					if(claimUnitVehicle.getDrivers() != null && !claimUnitVehicle.getDrivers().isEmpty()) {
						claimParticipations.addAll(claimUnitVehicle.getDrivers());
						retrieveParticipantEntities(claimUnitVehicle.getDrivers(), claimParticipationproviders, 
								claimParticipationContactDTOs, claimParticipations);
					}
					if(claimUnitVehicle.getPassengers() != null) {
						claimParticipations.addAll(claimUnitVehicle.getPassengers());
						retrieveParticipantEntities(claimUnitVehicle.getPassengers(), claimParticipationproviders, 
								claimParticipationContactDTOs, claimParticipations);
					}
				}
				if(claimUnit.getClaimDamageDTO() != null 
						&& claimUnit.getClaimDamageDTO().getDamageServicesDTO() != null) {
					claimParticipations.addAll(claimUnit.getClaimDamageDTO().getDamageServicesDTO());
					retrieveParticipantEntities(claimUnit.getClaimDamageDTO().getDamageServicesDTO(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
				if(claimUnit.getClaimSalvageDTO() != null 
						&& claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO() != null) {
					claimParticipations.addAll(claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO());
					retrieveParticipantEntities(claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
				if(claimUnit.getFinancialInterestList() != null && !claimUnit.getFinancialInterestList().isEmpty()) {
					claimParticipations.addAll(claimUnit.getFinancialInterestList());
					retrieveParticipantEntities(claimUnit.getFinancialInterestList(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
				if(claimUnit.getInjuredPersonList() != null && !claimUnit.getInjuredPersonList().isEmpty()) {
					claimParticipations.addAll(claimUnit.getInjuredPersonList());
					retrieveParticipantEntities(claimUnit.getInjuredPersonList(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
				if(claimUnit.getOtherCarriersList() != null && !claimUnit.getOtherCarriersList().isEmpty()) {
					claimParticipations.addAll(claimUnit.getOtherCarriersList());
					retrieveParticipantEntities(claimUnit.getOtherCarriersList(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
				if(claimUnit.getOtherPartiesList() != null && !claimUnit.getOtherPartiesList().isEmpty()) {
					claimParticipations.addAll(claimUnit.getOtherPartiesList());
					retrieveParticipantEntities(claimUnit.getOtherPartiesList(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
				if(claimUnit.getOwners() != null && !claimUnit.getOwners().isEmpty()) {
					claimParticipations.addAll(claimUnit.getOwners());
					retrieveParticipantEntities(claimUnit.getOwners(), claimParticipationproviders, 
							claimParticipationContactDTOs, claimParticipations);
				}
			}
		}
		
		//Add Authorities to the Claim Participant Collection
		if(claimImportDTO.getAuthorities() != null) {
			claimParticipations.addAll(claimImportDTO.getAuthorities());
			for(ClaimParticipationAuthorityDTO authorityDTO : claimImportDTO.getAuthorities()){
				if(authorityDTO.getCitations() != null && !authorityDTO.getCitations().isEmpty()){
					claimParticipations.addAll(authorityDTO.getCitations());	
				}
				
			}
		}
		//Add Witnesses to the Claim Participant Collection
		if(claimImportDTO.getWitnesses() != null) {
			claimParticipations.addAll(claimImportDTO.getWitnesses());
			for(ClaimWitnessDTO witnessDTO : claimImportDTO.getWitnesses()) {
				if(witnessDTO.isParticipantUnknown()
						|| (witnessDTO.getParticipation() != null 
								&& witnessDTO.getParticipation().isUnknownParty())) {
					
					if(witnessDTO.getContacts() != null
							&& !witnessDTO.getContacts().isEmpty()) {
						
						for(CALClaimParticipationContactDTO participationContact : witnessDTO.getContacts()) {
							if(participationContact.getContact() == null 
									|| (participationContact.getContact() != null && 
									participationContact.getContact().getPartyTypeCode() == null)) {
								if(witnessDTO.getContacts().size() == 1) {
									witnessDTO.setContacts(null);
									break;
								}
							} else if(participationContact.getContact() != null) {
								claimParticipationContactDTOs.add(participationContact);
							}
						}
					}
				} else {
					claimParticipationContactDTOs.addAll(witnessDTO.getContacts());
				}
			}
		}
		
		if(claimParticipations != null && !claimParticipations.isEmpty()){
			for(CALClaimParticipationDTO claimParticipant : claimParticipations) {
				if(claimParticipant.getProviders() != null && !claimParticipant.getProviders().isEmpty()){
					claimParticipationproviders.addAll(claimParticipant.getProviders());
				}
			}
			if(claimParticipationproviders != null && !claimParticipationproviders.isEmpty()){
				claimParticipations.addAll(claimParticipationproviders);
			}
		}
		
		//Iterate through claim level participants to update party Id
		if(claimParticipations != null && !claimParticipations.isEmpty()){
			for(CALClaimParticipationDTO claimParticipant : claimParticipations) {
				//Set the effective date time for participation
				if(claimParticipant.getEffectiveDateTime() == null) {
					claimParticipant.setEffectiveDateTime(DateUtility.getSystemDateOnly());
				}
				
				if(claimParticipant.isParticipantUnknown()
						|| (claimParticipant.getParticipation() != null 
								&& claimParticipant.getParticipation().isUnknownParty())  ){
					continue;
				}
				
				associateParticipationReference(claimParticipant);
				
				if(claimParticipant.getContacts() != null && !claimParticipant.getContacts().isEmpty()){
					claimParticipationContactDTOs.addAll(claimParticipant.getContacts());	
				}
			}

			for(CALClaimParticipationDTO claimParticipant : claimParticipations) {
				if(minimalPartyMap.containsKey(claimParticipant.getPartyId())) {
					claimParticipant.setParticipation(minimalPartyMap.get(claimParticipant.getPartyId()));
				} else {
					if(claimParticipant.getPartyId() != null) {
						MinimalPartyDTO minimalParty = retrieveMinimalParty(claimParticipant.getPartyId());
						minimalPartyMap.put(claimParticipant.getPartyId(), minimalParty);
						
						claimParticipant.setParticipation(minimalParty);
					}
				}
			}	
		}
		
		//Iterate through case level participants to update party Id
		if(claimImportDTO.getClaimCases()!=null){
			for(CaseDTO caseDTO : claimImportDTO.getClaimCases()){
				if(caseDTO.getParticipationCompositeDTO() !=null && caseDTO.getParticipationCompositeDTO().getCaseParticipations() !=null &&
					!caseDTO.getParticipationCompositeDTO().getCaseParticipations().isEmpty()){
					for(CaseParticipationDTO caseParticipationDTO : caseDTO.getParticipationCompositeDTO().getCaseParticipations()){
						//Set the effective date time for participation
						if(caseParticipationDTO.getPtcpEffDate() == null) {
							caseParticipationDTO.setPtcpEffDate(DateUtility.getSystemDateOnly());
						}
						if(!CaseConstants.PARTICIPATION_TYPE_OTHER.equals(caseParticipationDTO.getPtcpTypeSrcCode())) {
							associateParticipationReference(caseParticipationDTO);
						}
					}
				}
			}
		}
		//Iterate through claim level participants for contact to update party Id
		if(claimParticipationContactDTOs != null && !claimParticipationContactDTOs.isEmpty()){
			for(CALClaimParticipationContactDTO contactDTO : claimParticipationContactDTOs){
				//Set the effective date time for participation
				if(contactDTO.getEffectiveDateTime() == null) {
					contactDTO.setEffectiveDateTime(DateUtility.getSystemDateOnly());
				}
				
				associateParticipationReference(contactDTO);
			}
			for(CALClaimParticipationContactDTO contactDTO : claimParticipationContactDTOs){
				if(contactDTO.getPartyId() != null) {
					MinimalPartyDTO minimalParty = retrieveMinimalParty(contactDTO.getPartyId());
					minimalPartyMap.put(contactDTO.getPartyId(), minimalParty);
					
					contactDTO.setContact(minimalParty);
				}
			}
		}
		
		//Set Party Id for Payments and Recovery if not available
		if(claimImportDTO.getClaimPayments() != null) {
			for(ClaimPayment claimPayment : claimImportDTO.getClaimPayments()) {
				if(claimPayment.getClaimPayables() != null) {
					for(ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
						if(claimPayable.getPartyId() != null) {
							claimPayable.setPartyId(associatePartyReference(claimPayable.getPartyId(), PartyConstants.CLAIM_CONTEXT));
						}
					}
				}
				if(claimPayment.getClaimPaymentDisbursements() != null) {
					for(ClaimPaymentDisbursement claimPaymentDisbursement : claimPayment.getClaimPaymentDisbursements()) {
						if(claimPaymentDisbursement.getPartyId() != null) {
							claimPaymentDisbursement.setPartyId(
									associatePartyReference(claimPaymentDisbursement.getPartyId(), PartyConstants.CLAIM_CONTEXT));
						}
						if(claimPaymentDisbursement.getInCareOfPayeeId() != null) {
							claimPaymentDisbursement.setInCareOfPayeeId(
									associatePartyReference(claimPaymentDisbursement.getInCareOfPayeeId(), PartyConstants.CLAIM_CONTEXT));
						}
						if(claimPaymentDisbursement.getAdditionalPayeeId() != null) {
							claimPaymentDisbursement.setAdditionalPayeeId(
									associatePartyReference(claimPaymentDisbursement.getAdditionalPayeeId(), PartyConstants.CLAIM_CONTEXT));
						}
						/*if(claimPaymentDisbursement.getPartyBankAccountIdPayee() != null) {
							claimPaymentDisbursement.setPartyBankAccountIdPayee(
									associatePartyReference(claimPaymentDisbursement.getPartyBankAccountIdPayee()));
						}*/
						if(claimPaymentDisbursement.getPartyIdPayee() != null) {
							claimPaymentDisbursement.setPartyIdPayee(
									associatePartyReference(claimPaymentDisbursement.getPartyIdPayee(), PartyConstants.CLAIM_CONTEXT));
						}
						if(claimPaymentDisbursement.getClaimDisbursementAdditionalPayees() != null) {
							for(ClaimDisbursementAdditionalPayee clmDisbursementPayees : claimPaymentDisbursement.getClaimDisbursementAdditionalPayees()) {
								if(clmDisbursementPayees.getPartyIdPayee() != null) {
									clmDisbursementPayees.setPartyIdPayee(
											associatePartyReference(clmDisbursementPayees.getPartyIdPayee(), PartyConstants.CLAIM_CONTEXT));
								}								
							}
						}
					}
				}
			}
		}
		
		if(claimImportDTO.getRecoveries() != null) {
			for(ClaimRecovery claimRecovery : claimImportDTO.getRecoveries()) {
				if(claimRecovery.getClaimRemittance() != null 
						&& claimRecovery.getClaimRemittance().getRemitterPartyNumber() != null) {
					claimRecovery.getClaimRemittance().setRemitterPartyNumber(associatePartyReference(
							claimRecovery.getClaimRemittance().getRemitterPartyNumber(), PartyConstants.CLAIM_CONTEXT));
				}
			}
		}

	}
	
	/**
	 * Add Contact and Provider Participants to the collection for setting the Party ID.
	 * @param lstParticipantDTO
	 * @param claimParticipationproviders
	 * @param claimParticipationContactDTOs
	 * @param claimParticipations
	 */
	private void retrieveParticipantEntities(Collection<? extends CALClaimParticipationDTO> lstParticipantDTO, 
			Collection<CALClaimParticipationDTO> claimParticipationproviders,
			Collection<CALClaimParticipationContactDTO> claimParticipationContactDTOs,
			Collection<CALClaimParticipationDTO> claimParticipations) {
		if(lstParticipantDTO != null && !lstParticipantDTO.isEmpty()) {
			for(CALClaimParticipationDTO participantDTO : lstParticipantDTO) {
				if(participantDTO.isParticipantUnknown()
						|| (participantDTO.getParticipation() != null 
								&& participantDTO.getParticipation().isUnknownParty())  ){

					if(participantDTO.getContacts() != null
							&& !participantDTO.getContacts().isEmpty()) {
						
						for(CALClaimParticipationContactDTO participationContact : participantDTO.getContacts()) {
							if(participationContact.getContact() != null) {
								claimParticipationContactDTOs.add(participationContact);
							}
						}
					}
					if(participantDTO.getProviders() != null
							&& !participantDTO.getProviders().isEmpty()) {
						
						for(CALClaimParticipationProvDTO providers : participantDTO.getProviders()) {
							if(providers.getParticipantTypeCode() != null) {
								claimParticipationproviders.add(providers);
							}
						}
					}
					
					if(participantDTO.getPtcpLevelServices() != null 
							&& !participantDTO.getPtcpLevelServices().isEmpty()) {
						claimParticipations.addAll(participantDTO.getPtcpLevelServices());
						retrieveParticipantEntities(participantDTO.getPtcpLevelServices(), claimParticipationproviders, 
								claimParticipationContactDTOs, claimParticipations);
					}
				} else {
					
					if(participantDTO.getPtcpLevelServices() != null 
							&& !participantDTO.getPtcpLevelServices().isEmpty()) {
						claimParticipations.addAll(participantDTO.getPtcpLevelServices());
						retrieveParticipantEntities(participantDTO.getPtcpLevelServices(), claimParticipationproviders, 
								claimParticipationContactDTOs, claimParticipations);
					}
					
					if(participantDTO.getContacts() != null
							&& !participantDTO.getContacts().isEmpty()) {
						claimParticipationContactDTOs.addAll(participantDTO.getContacts());
					}
					if(participantDTO.getProviders() != null
							&& !participantDTO.getProviders().isEmpty()) {
						claimParticipationproviders.addAll(participantDTO.getProviders());
					}
				}
			}
		}
	}


	/**
	 * Logic to iterate over the participants on the Claim Policy, and either create them in the
	 * party system, or retrieve them if a match already exists.
	 * @param claimImportDTO Claim Import Data.
	 * @param partyMap Party - person and organization.
	 */

	public void createClaimPolicyParties(ClaimImportCompositeDTO claimImportDTO, Map<Long, PartyDTO> partyMap) {
		
		Collection<ClientPersonDTO> persons = new ArrayList<ClientPersonDTO>();
		Collection<ClientOrganizationDTO> organizations = new ArrayList<ClientOrganizationDTO>(); 
		
		if(claimImportDTO.getPersons()!=null){
			persons = claimImportDTO.getPersons();	
		}
		if(claimImportDTO.getOrganizations()!=null){
			organizations = claimImportDTO.getOrganizations();
		}
		
		//Process person address details
		processPartyAddressDetails(persons);
		
		//Process organization address details
		processPartyAddressDetails(organizations);
		
		//Populate personMap
		for(PersonDTO person : persons) {
			Long recordId = person.getRecordId();
			if(recordId == null){
				throw new IIPCoreSystemException("RecordId cannot be null for person. Please check your Claim Import xml.");
			}
			if(!personMap.containsKey(recordId)) {
				personMap.put(recordId, person);
				partyMap.put(recordId, person);
			}
		}
		//Populate organizationMap
		for(OrganizationDTO organization : organizations) {
			Long recordId = organization.getRecordId();
			if(recordId == null){
				throw new IIPCoreSystemException("RecordId cannot be null for organization. Please check your Claim Import xml.");
			}
			if(!organizationMap.containsKey(recordId)) {
				organizationMap.put(recordId, organization);
				partyMap.put(recordId, organization);
			}
		}
		
		Collection<ClaimPolicyParticipationDTO> claimPolicyParticipations = new ArrayList<ClaimPolicyParticipationDTO>();
		if(claimImportDTO.getClaimDTO() != null && claimImportDTO.getClaimDTO().getPolicy() != null) {
			if(claimImportDTO.getClaimDTO().getPolicy().getParticipations() != null) {
				claimPolicyParticipations = claimImportDTO.getClaimDTO().getPolicy().getParticipations();
			}
			if(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyAgencyParticipants() != null) {
				claimPolicyParticipations.addAll(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyAgencyParticipants());
			}
			
			if(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyDriverParticipants() != null) {
				claimPolicyParticipations.addAll(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyDriverParticipants());
			}
			if(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyFinancialInterestParticipants() != null) {
				claimPolicyParticipations.addAll(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyFinancialInterestParticipants());
			}
			if(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyInsuredParticipants() != null) {
				claimPolicyParticipations.addAll(claimImportDTO.getClaimDTO().getPolicy().getClmPolicyInsuredParticipants());
			}
		}
		
		
		//Iterate through claim level participants to update party Id
		if(claimPolicyParticipations != null && !claimPolicyParticipations.isEmpty()){
			for(ClaimPolicyParticipationDTO claimPolicyParticipant : claimPolicyParticipations) {
				//Set the effective date time for participation
				if(claimPolicyParticipant.getParticipationEffectiveDate() == null) {
					claimPolicyParticipant.setParticipationEffectiveDate(DateUtility.getSystemDateOnly());
				}

				associateParticipationReference(claimPolicyParticipant);

			}
		}
	}
	
	/**
	 * Get the Party Record Id for entities that don't have Participation Details. 
	 * @param partyId
	 * @param personMap
	 * @param organizationMap
	 * @return Party Record Id
	 */
	private Long associatePartyReference(Long partyId, String tmpContext) {
		
		Long retPartyId = null;
		
		if(partyId == null) {
			logger.info("WARNING - null party reference.");
			throw new IIPCoreSystemException("Invalid party reference in Claim Import. Please check Claim Import xml");
		}
		if(processedPartyMap.containsKey(partyId)) {
			return processedPartyMap.get(partyId);
		} else if(personMap.containsKey(partyId)) {
			//Party Id of person is provided for the Entity
			PersonDTO person = personMap.get(partyId);
			//Sync Up Party here
			retPartyId = syncPartyInfo(person, null, tmpContext);
			processedPartyMap.put(partyId, retPartyId);
		} else if(organizationMap.containsKey(partyId)) {
			//Party Id of organization is provided for the Entity
			OrganizationDTO organization = organizationMap.get(partyId);
			//Sync Up Party here
			retPartyId = syncPartyInfo(organization, null, tmpContext);
			processedPartyMap.put(partyId, retPartyId);
		} else if (partyId != null){
			//Sync Up Party here
			retPartyId = syncPartyInfo(null, partyId, tmpContext);
			processedPartyMap.put(partyId, retPartyId);
		}
		
		return retPartyId;
	}
	
	public Long syncPartyInfo(PartyDTO partyInfo, Long partyId, String context){
		
		PartyDTO currPartyDTO = null;
		
		if(partyId != null){
			try{
				currPartyDTO = retrievePartyDetails(partyId);
			}catch(Exception ex){
				currPartyDTO = null;
			}
		}
		
		if(partyInfo == null  && (currPartyDTO == null || currPartyDTO.getRecordId() == null)){
			logger.info("Null party returned for participation");
			throw new IIPCoreSystemException( "PartyId: " + partyId +
					" Incorrect partyId/recordId for participation. Please check your policy xml.");	
		}		
		

		
		if(currPartyDTO == null || currPartyDTO.getRecordId() == null) {

			DuplicateSearchResult searchResult = null;
			//Search party duplicates
			if(partyInfo instanceof PersonDTO){
				//Construct the key from the data				
				String firstName = "";
				String lastName = "";
				for(PersonNameDTO name: ((PersonDTO)partyInfo).getNames()){
					if(!name.getNameTypeCode().equals("lgl_nm")){
						continue;
					}					
					firstName = name.getPersonFirstName()==null?"":name.getPersonFirstName();
					lastName =  name.getPersonLastName()==null?"":name.getPersonLastName();
				}
					String dob = ((PersonDTO)partyInfo).getDateOfBirth()==null?"":((PersonDTO)partyInfo).getDateOfBirth().toString();
					
					String partyKey = firstName.trim().toUpperCase() + lastName.trim().toUpperCase() + dob.trim();
				//Lookup with the key
				if(partyNameMap.containsKey(partyKey) && !dob.equals("")){
					logger.info("Same person repeated in the import: {} ", partyKey);
					PersonDTO existingParty = (PersonDTO)partyNameMap.get(partyKey);
					existingParty.setContext((context == null)?PartyConstants.CLAIM_CONTEXT:context);					
					boolean isPartyUpdateRequired = getPartyMerger().mergePersonDTO((ClientPersonDTO)partyInfo, existingParty);
					if(isPartyUpdateRequired){
						logger.info("Saving after merge person: {} ", existingParty.getRecordId());
						saveParty(existingParty);
					}
						return existingParty.getRecordId();
				}else{		
					searchResult = searchPersonDuplicates((PersonDTO)partyInfo);
				}
			}
			
			if(partyInfo instanceof OrganizationDTO){
				//Construct the key from the data
				String organizationName = "";
				for(OrganizationNameDTO name: ((OrganizationDTO)partyInfo).getNames()){
					if(!name.getNameTypeCode().equals("lgl_nm")){
						continue;
					}					
					organizationName = name.getOrganizationName()==null?"":name.getOrganizationName();
				}
					
					String partyKey = organizationName;				
				if(partyNameMap.containsKey(partyKey)){
					logger.info("Same organization repeated in the import: {} ", organizationName);
					OrganizationDTO existingParty = (OrganizationDTO)partyNameMap.get(partyKey);
					existingParty.setContext((context == null)?PartyConstants.CLAIM_CONTEXT:context);
					boolean isPartyUpdateRequired = getPartyMerger().mergeOrganizationDTO((ClientOrganizationDTO)partyInfo, existingParty);
					if(isPartyUpdateRequired){
						logger.info("Saving after merge organization: {} ", existingParty.getRecordId());
						saveParty(existingParty);
					}
					return existingParty.getRecordId();
				}else{				
					searchResult = searchOrganizationDuplicates((OrganizationDTO)partyInfo);
				}
			}
			
			//Check for duplicate name address and communication usages
            ClientPartyValidator.getInstance().partyAttributeDupsFound(partyInfo);			
			
			if(searchResult == null) {
				//create the party
				logger.info("Saving new party for the participant");
				partyInfo.setRecordId(null);
				//Flood with configured address usages
				getPartyMerger().floodAddressUsageCommunicationAgreement(partyInfo, (context == null)?PartyConstants.CLAIM_CONTEXT:context);
				//Save the Party
				partyInfo.setRecordId(saveParty(partyInfo).getRecordId());
			} else {
				
				logger.info("Found match against partyId: {} ", searchResult.getPartyId());
				if(processedPartyMap.containsKey(searchResult.getPartyId())){
					partyInfo.setRecordId(searchResult.getPartyId());
					logger.info("PartyId Found in Map: " + searchResult.getPartyId());
				}else{	
					logger.info("PartyId Not Found in Map: {} ", searchResult.getPartyId());
					PartyDTO existingPartyDTO = retrievePartyDetails(searchResult.getPartyId());
					if(existingPartyDTO != null && existingPartyDTO.getRecordId() != null) {
						//Need to place code for Party Merge
						existingPartyDTO.setContext((context == null)?PartyConstants.CLAIM_CONTEXT:context);
						if(partyInfo instanceof PersonDTO && existingPartyDTO instanceof PersonDTO){
							boolean isPartyUpdateRequired = getPartyMerger().mergePersonDTO((ClientPersonDTO)partyInfo, (PersonDTO)existingPartyDTO);
							if(isPartyUpdateRequired){
								logger.info("Saving after merge person: {} ", existingPartyDTO.getRecordId());								
								saveParty(existingPartyDTO);
							}
						}
						if(partyInfo instanceof OrganizationDTO && existingPartyDTO instanceof OrganizationDTO){
							boolean isPartyUpdateRequired = getPartyMerger().mergeOrganizationDTO((ClientOrganizationDTO)partyInfo,(OrganizationDTO)existingPartyDTO);
							if(isPartyUpdateRequired){
								logger.info("Saving after merge organization: {} ", existingPartyDTO.getRecordId());									
								saveParty(existingPartyDTO);
							}
						}
						partyInfo.setRecordId(existingPartyDTO.getRecordId());
					}
				}
			}
			//Check whether save party was successful
			if(partyInfo.getRecordId() == null) {
				throw new IIPCoreSystemException("Error saving/retrieving party info for policy");
			}
			//Set the processed partyId
			processedPartyMap.put(partyInfo.getRecordId(), partyInfo.getRecordId());

			return partyInfo.getRecordId();	
		}else {
			//Save person
			if(processedPartyMap.containsKey(currPartyDTO.getRecordId())){
				logger.info("PartyId Found in Map: {} ", currPartyDTO.getRecordId());
			}
			else{
				boolean isUpdateRequired = getPartyMerger().floodAddressUsageCommunicationAgreement(currPartyDTO, (context == null)?PartyConstants.CLAIM_CONTEXT:context);
				if(isUpdateRequired && currPartyDTO instanceof PersonDTO){
					logger.info("Saving after flooding person: {} ", currPartyDTO.getRecordId());						
					saveParty(currPartyDTO);
				}
				if(isUpdateRequired && currPartyDTO instanceof OrganizationDTO){
					logger.info("Saving after flooding organization: {} ", currPartyDTO.getRecordId());						
					saveParty(currPartyDTO);
				}

				processedPartyMap.put(currPartyDTO.getRecordId(), currPartyDTO.getRecordId());
			}
			return currPartyDTO.getRecordId();
		}
		
	}
	
	public PartyDTO saveParty(PartyDTO updatableParty){
			PartyDTO newPartyDTO = null;
			
			if(updatableParty instanceof PersonDTO){
				newPartyDTO = saveClientParty(updatableParty);
				logger.info("Saved Person RecordId: {} ", newPartyDTO.getRecordId());
				String firstName = "";
				String lastName = "";
				//Fetch Updated Party Info
				for(PersonNameDTO name: ((PersonDTO)newPartyDTO).getNames()){
					if(!name.getNameTypeCode().equals("lgl_nm")){
						continue;
					}					
					firstName = name.getPersonFirstName()==null?"":name.getPersonFirstName();
					lastName =  name.getPersonLastName()==null?"":name.getPersonLastName();
				}
					String dob = ((PersonDTO)newPartyDTO).getDateOfBirth()==null?"":((PersonDTO)newPartyDTO).getDateOfBirth().toString();
					
					String partyKey = firstName.trim().toUpperCase() + lastName.trim().toUpperCase() + dob.trim();
					
					partyNameMap.put(partyKey, newPartyDTO);
			}
		
 			if(updatableParty instanceof OrganizationDTO){
	
				newPartyDTO = saveClientParty(updatableParty);
				logger.info("saved Organization RecordId: {} ", newPartyDTO.getRecordId());
				String organizationName = "";
				//Fetch Updated Party Info
				for(OrganizationNameDTO name: ((OrganizationDTO)newPartyDTO).getNames()){
					if(!name.getNameTypeCode().equals("lgl_nm")){
						continue;
					}
					organizationName = name.getOrganizationName()==null?"":name.getOrganizationName();
				}

					String partyKey = organizationName;
					
					partyNameMap.put(partyKey, newPartyDTO);				
			
			}
			
		return newPartyDTO;
	}

	/**
	 * Associate policy/unit participation reference with person/organization.
	 * @param participant Participation as Data Transfer Object so that it can used for both Policy and Claim Participants.
	 * @param personMap persons associated with policy.
	 * @param organizationMap organizations associated with policy.
	 */
	//	 * @param processedPartyMap Person/organizations that have been processed.
	private void associateParticipationReference(DataTransferObject participant){
		Long partyIdx = null;
		Long recordIdx = null;
		String tmpContext = null;
		
		String participantTypeCode = null;

		if(participant instanceof PolicyParticipationDTO) {
			partyIdx = ((PolicyParticipationDTO)participant).getPartyId();
			tmpContext = PartyConstants.POLICY_CONTEXT;
			participantTypeCode = ((PolicyParticipationDTO)participant).getParticipationTypeCd();
		} else if (participant instanceof CALClaimParticipationDTO) {
			partyIdx = ((CALClaimParticipationDTO)participant).getPartyId();
			tmpContext = PartyConstants.CLAIM_CONTEXT;			
			participantTypeCode = ((CALClaimParticipationDTO)participant).getParticipantTypeCode();
		} else if(participant instanceof CaseParticipationDTO){
			partyIdx = ((CaseParticipationDTO)participant).getPartyId();
			tmpContext = PartyConstants.CASE_CONTEXT;			
			participantTypeCode = ((CaseParticipationDTO)participant).getPtcpTypeCode();
		} else if (participant instanceof CALClaimParticipationContactDTO){
			partyIdx = ((CALClaimParticipationContactDTO)participant).getPartyId();
			tmpContext = PartyConstants.CLAIM_CONTEXT;			
			participantTypeCode = "Contact Participant";
		} else if (participant instanceof CALClaimParticipationProvDTO) {
			partyIdx = ((CALClaimParticipationProvDTO)participant).getPartyId();
			tmpContext = PartyConstants.CLAIM_CONTEXT;			
			participantTypeCode = ((CALClaimParticipationProvDTO)participant).getParticipantTypeCode();
		} else if (participant instanceof ClaimPolicyParticipationDTO) {
			partyIdx = ((ClaimPolicyParticipationDTO)participant).getPartyId();
			tmpContext = PartyConstants.POLICY_CONTEXT;			
			participantTypeCode = ((ClaimPolicyParticipationDTO)participant).getParticipationTypeCd();
		}
		if(participant.getObjectId()!=null){
			recordIdx = Long.valueOf(participant.getObjectId());
		}
		if(partyIdx == null && recordIdx == null) {
			logger.info("WARNING - null party reference for participant type = " + participantTypeCode);
			throw new IIPCoreSystemException("Invalid party reference in participation of type " + participantTypeCode + ". Please check your policy xml");
		}
		//Go with recordIdx if available..
		Long partyRefId = (recordIdx==null)?partyIdx:recordIdx; 
		/*if (personMap.containsKey(partyIdx)){
			//Party Id of person is provided in participant
			PersonDTO person = personMap.get(partyIdx);
		
			//Sync up Party Here
			if(!processedPartyMap.containsKey(partyIdx)){
				Long retPartyId = syncPartyInfo(person, partyIdx, tmpContext);
				processedPartyMap.put(partyIdx, retPartyId);
			}
			
		}else if(organizationMap.containsKey(partyIdx)){
			//Party Id of organization is provided in participant
			OrganizationDTO organization = organizationMap.get(partyIdx);
			//Sync up Party Here
			if(!processedPartyMap.containsKey(partyIdx)){
				Long retPartyId = syncPartyInfo(organization, partyIdx, tmpContext);	
				processedPartyMap.put(partyIdx, retPartyId);
			}
			
		}else */
		if(personMap.containsKey(partyRefId)){
			Long applicablePartyId;
			//Check whether person has been processed
			if(!processedPartyMap.containsKey(partyRefId)){
				PersonDTO person = personMap.get(partyRefId);
				//Sync up Party here
				applicablePartyId = syncPartyInfo(person, null, tmpContext);
				processedPartyMap.put(partyRefId, applicablePartyId);
			}else{
				applicablePartyId = processedPartyMap.get(partyRefId);
			}
			//Set the party Id in participant
			associateParticipants(participant, applicablePartyId);

		}else if(organizationMap.containsKey(partyRefId)){
			Long applicablePartyId;
			//Check whether organization has been processed
			if(!processedPartyMap.containsKey(partyRefId)){
				OrganizationDTO organization = organizationMap.get(partyRefId);
				//Sync up Party here
				applicablePartyId = syncPartyInfo(organization, null, tmpContext);
				processedPartyMap.put(partyRefId, applicablePartyId);
			}else{
				applicablePartyId = processedPartyMap.get(partyRefId);
			}
			//Set the party Id in participant
			associateParticipants(participant, applicablePartyId);

		}else if (partyIdx != null){
			//Sync Up Party here
			if(!processedPartyMap.containsKey(partyIdx)){
				Long retPartyId = syncPartyInfo(null, partyIdx, tmpContext);
				processedPartyMap.put(partyIdx, retPartyId);
			}
		}else {
			logger.info("No Match returned for participation");
			logger.info("personMap key:value");
			Iterator<Long> perskey = personMap.keySet().iterator();			
			while (perskey.hasNext()) {
				Long key = perskey.next();
				logger.info(key + ": {} ", personMap.get(key));
			}
			logger.info("organizationMap key:value");
			Iterator<Long> orgkey = organizationMap.keySet().iterator();			
			while (orgkey.hasNext()) {
				Long key = orgkey.next();
				logger.info(key + ": {} ", organizationMap.get(key));
			}			
			throw new IIPCoreSystemException("objectId: " + recordIdx +
			" Incorrect objectId/recordId for participation. Please check your policy xml.");
		}
	}
	
	public void associateParticipants(DataTransferObject participant, Long partyId){


		/*
		 * Check if the partyid is found in the map, if found then add the detail to the object else set objectid to be remove
		 */
		logger.info("in associateParticipants");
		
		if(participant instanceof PolicyFinancialInterestParticipationDTO){
			logger.info("participant of type PolicyFinancialInterestParticipationDTO");
			String mapkey = unitIdFilter + "" + partyId;
			if(financialParticipantMap.containsKey(mapkey)){
				PolicyFinancialInterestParticipationDTO previousParticipant = financialParticipantMap.get(mapkey);
				PolicyFinancialInterestParticipationDTO currentParticipant = (PolicyFinancialInterestParticipationDTO)participant;
				//Add the current participant details to the previous one and set to remove
				/*TD36270 - 12/12/2013 @GR - Select distinct financial interest participation detail for a given Unit/Policy level participation
				Check if the interest type code matches, Add to the previous if they are different. Loading them in Map would give us Distinct details*/
				//Load Current
				Map<String, PolicyFinancialInterestParticipationDetailDTO> finDetailMap = new HashMap<String, PolicyFinancialInterestParticipationDetailDTO>();
				for(PolicyFinancialInterestParticipationDetailDTO currfinptcpDetail :currentParticipant.getFinIntTypDet()){
					finDetailMap.put(currfinptcpDetail.getFinancialTypeCode(), currfinptcpDetail);
				}
				//Load Previous
				for(PolicyFinancialInterestParticipationDetailDTO prevfinptcpDetail :previousParticipant.getFinIntTypDet()){
					finDetailMap.put(prevfinptcpDetail.getFinancialTypeCode(), prevfinptcpDetail);
				}
				
				//previousParticipant.getFinIntTypDet().addAll(currentParticipant.getFinIntTypDet());
				//Set distinct details
				previousParticipant.setFinIntTypDet(new ArrayList<PolicyFinancialInterestParticipationDetailDTO>(finDetailMap.values()));
				((PolicyFinancialInterestParticipationDTO)participant).setObjectId("remove");
				logger.info("currentParticipant objectid set to remove");
			}else{
				logger.info("PolicyFinancialInterestParticipationDTO not found in Map adding");
				financialParticipantMap.put(mapkey, (PolicyFinancialInterestParticipationDTO)participant);
			}
		} 
		
		//Set the organization Id in participant
		if(participant instanceof PolicyParticipationDTO) {
			((PolicyParticipationDTO)participant).setPartyId(partyId);
		} else if (participant instanceof CALClaimParticipationDTO) {
			//2012-12-25 @GR - Set Unknown Participant to null when participant is known.
			if(!((CALClaimParticipationDTO) participant).isParticipantUnknown()){
			((CALClaimParticipationDTO)participant).setPartyId(partyId);
			((CALClaimParticipationDTO) participant).setParticipation(null);
			}
		}else if(participant instanceof CaseParticipationDTO){
			((CaseParticipationDTO)participant).setPartyId(partyId);
		}else if(participant instanceof CALClaimParticipationContactDTO){
			((CALClaimParticipationContactDTO)participant).setPartyId(partyId);
		} else if (participant instanceof CALClaimParticipationProvDTO) {
			//2012-12-25 @GR - Set Unknown Participant to null when participant is known.
			if(!((CALClaimParticipationDTO) participant).isParticipantUnknown()){
			((CALClaimParticipationProvDTO)participant).setPartyId(partyId);
			((CALClaimParticipationDTO) participant).setParticipation(null);
			}
		}else if (participant instanceof ClaimPolicyParticipationDTO) {
			((ClaimPolicyParticipationDTO)participant).setPartyId(partyId);
		}		
	}
	
	/**
	 * Retrieves instance of MinimalPartyDTO using Party Id.
	 * 
	 * @param partyId Long
	 * @return minimalParty MinimalPartyDTO
	 */
	public MinimalPartyDTO retrieveMinimalParty(Long partyId) {
		MinimalPartyDTO minimalParty = null;
		MinimalPartyRequestDTO requestDTO = new MinimalPartyRequestDTO();
		requestDTO.setPartyId(partyId);
		requestDTO.setContextType(ClaimsServiceConstants.CONTEXT_CLAIM);
		minimalParty = getPartyService().retrieveMinimalParty(requestDTO);
		return minimalParty;
	}	

	/** 
	 * Populate unit level participants.
	 * @param policy PolicyImportDTO.
	 * @param unitParticipations participation list.
	 */
	private void populateUnitLevelParticipants(PolicyImportDTO policy) {

		//Checking if Policy Units are available before traversing the Collection
		if(policy.getUnits() == null) {
			return;
		}

		//Populate unit level participants 
		for(PolicyUnitDTO unit : policy.getUnits()) {
			unitIdFilter++;
			Iterator<PolicyParticipationDTO> iterUnitParticipants = unit.getParticipations()==null?
									null:unit.getParticipations().iterator();
   			 while(iterUnitParticipants != null && iterUnitParticipants.hasNext()){
   				 	PolicyParticipationDTO unitParticipation = iterUnitParticipants.next();
					//Set the effective date time for participation
					if(unitParticipation.getEffectiveDateTime() == null) {
						unitParticipation.setEffectiveDateTime(DateUtility.getSystemDateOnly());
					}					
					//update unit level participants with party Id
					associateParticipationReference(unitParticipation);
					//Remove the participant if set to "remove" -- Logic applied for financial interest participants
					/* TD36270 - 12/10/2013 @GR - Eliminate duplicate financial interest participants only for the current unit	*/
					if(unitParticipation.getObjectId() != null && unitParticipation.getObjectId().equals("remove")){
						logger.info("Unit Participant removed: " + unitParticipation.getParticipationTypeCd());
						iterUnitParticipants.remove();
						//unit.getParticipations().remove(unitParticipation);
					}
										
				}
			
		}
	}

	/**
	 * Duplicate Party search for Organization based on threshold level.
	 * @param organization source organization.
	 * @return searchResult applicable organization.
	 */
	public DuplicateSearchResult searchOrganizationDuplicates(
			OrganizationDTO organization) {
		
		organization.setRecordId(null);
		/*
		 * 01/22/105 @GR - Performance Tuning. Go with External PartyId if already present. No need to check for Dups.
		 */
		if(organization.getExternalSourceId() != null){
				Long partyId = getPartyService().retrievePartyIdByExternalSystemId(organization.getExternalSourceId());
				if(partyId != null){
					logger.info("Organization fetched with external id : " + organization.getExternalSourceId());					
					DuplicateSearchResult dupResult = new DuplicateSearchResult();
					dupResult.setPartyId(partyId);
					return dupResult;
				}
		}		
		//Search duplicate organizations
		//03/21/2013 @GR - Use Client Composite layer for Dup check - DOB Null check is handled in this api		
		if(organization.getAddressCollection() != null 
				&& organization.getAddressCollection().getAddresses() != null
				&& organization.getAddressCollection().getAddresses().size() > 0) {
			
			if(organization.getNames() != null && organization.getNames().size() > 0) {
				OrganizationNameDTO orgNameDTO = organization.getNames().iterator().next(); 
				Collection<AddressDTO> addresses = organization.getAddressCollection().getAddresses();
				for(AddressDTO adressDTO : addresses) {
					logger.info("For Organization {} ", orgNameDTO.getOrganizationName());
					logger.info("Address Line 1 - {} City - {} ", adressDTO.getAddressLine1(), adressDTO.getCityName());
					logger.info("State - {} Postal Code - {} ", adressDTO.getPostalServiceRegionId(), adressDTO.getPostalCode());
				}
			}
		}
		// 12/01/2014 @GR - Issue with Search Duplicate Parties Base API. It is altering the object signature especially around Email and Other channels. Creating a new Object
		// Clone to get around this issue.
		OrganizationDTO newOrgDTO = (OrganizationDTO)CloneUtil.deepClone(organization);
		DuplicatePartySearchResultComposite duplicatePartySearchResultComposite = getClearPartyDuplicateService()
				.searchDuplicateParties(newOrgDTO, true, true);
		
		//Remove persons from the result
		if(duplicatePartySearchResultComposite != null
			&& duplicatePartySearchResultComposite.getDuplicateParties() != null){
			Iterator<DuplicateSearchResult> itduplicateParties = 
					duplicatePartySearchResultComposite.getDuplicateParties().iterator();
			while(itduplicateParties.hasNext()){
				DuplicateSearchResult result = itduplicateParties.next();
				if(result.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_PERSON)){
					itduplicateParties.remove();
				}
			}
		}

		//DuplicatePartySearchResultComposite duplicatePartySearchResultComposite = partyServiceImpl.duplicatePartySearch(organization);
		//Process duplicate search results based on threshold level
		DuplicateSearchResult duplicateSearchResult = 
			processDuplicateSearchResults(duplicatePartySearchResultComposite);

		return duplicateSearchResult;
	}

	/**
	 * Duplicate Party search for Person based on threshold level.
	 * @param person source person.
	 * @return searchResult applicable person.
	 */
	public DuplicateSearchResult searchPersonDuplicates(PersonDTO person) {
		
		person.setRecordId(null);
		/*
		 * 01/22/105 @GR - Performance Tuning. Go with External PartyId if already present. No need to check for Dups.
		 */
		if(person.getExternalSourceId() != null){
				Long partyId = getPartyService().retrievePartyIdByExternalSystemId(person.getExternalSourceId());
				if(partyId != null){
					logger.info("Person fetched with external id : " + person.getExternalSourceId());					
					DuplicateSearchResult dupResult = new DuplicateSearchResult();
					dupResult.setPartyId(partyId);
					return dupResult;
				}
		}
		//Search duplicate persons
		//03/21/2013 @GR - Use Client Composite layer for Dup check - DOB Null check is handled in this api
		if(person.getAddressCollection() != null 
				&& person.getAddressCollection().getAddresses() != null
				&& person.getAddressCollection().getAddresses().size() > 0) {
			
			if(person.getNames() != null && person.getNames().size() > 0) {
				PersonNameDTO personNameDTO = person.getNames().iterator().next(); 
				Collection<AddressDTO> addresses = person.getAddressCollection().getAddresses();
				for(AddressDTO adressDTO : addresses) {
					logger.info("For Person {} {}", personNameDTO.getPersonLastName(), personNameDTO.getPersonFirstName());
					logger.info("Address Line 1 - {} City - {} ", adressDTO.getAddressLine1(), adressDTO.getCityName());
					logger.info("State - {} Postal Code - {} ", adressDTO.getPostalServiceRegionId(), adressDTO.getPostalCode());
				}
			}
		} else {
			if(person.getNames() != null && person.getNames().size() > 0) {
				PersonNameDTO personNameDTO = person.getNames().iterator().next(); 
				logger.info("Address not available for Person {} {}", personNameDTO.getPersonLastName(), personNameDTO.getPersonFirstName());
			}
		}
		
		// 12/01/2014 @GR - Issue with Search Duplicate Parties Base API. It is altering the object signature especially around Email and Other channels. Creating a new Object
		// Clone to get around this issue.
		PersonDTO newPersonDTO = (PersonDTO)CloneUtil.deepClone(person);		
		DuplicatePartySearchResultComposite duplicatePartySearchResultComposite = getClearPartyDuplicateService()
				.searchDuplicateParties(newPersonDTO, true, true);

		//Filter Organization from the result
		if(duplicatePartySearchResultComposite != null
			&& duplicatePartySearchResultComposite.getDuplicateParties() != null){
			Iterator<DuplicateSearchResult> itduplicateParties = 
					duplicatePartySearchResultComposite.getDuplicateParties().iterator();
			while(itduplicateParties.hasNext()){
				DuplicateSearchResult result = itduplicateParties.next();
				if(result.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)){
					itduplicateParties.remove();
				}
			}
		}
		
		//Process duplicate search results based on threshold level
		DuplicateSearchResult duplicateSearchResult = 
			processDuplicateSearchResults(duplicatePartySearchResultComposite);

		return duplicateSearchResult;
	}

	/**
	 * Process duplicate search results to select the high threshold duplicate.
	 * @param duplicatePartySearchResultComposite duplicate search results.
	 * @return DuplicateSearchResult applicable duplicate party.
	 */
	private DuplicateSearchResult processDuplicateSearchResults(DuplicatePartySearchResultComposite duplicatePartySearchResultComposite) {

		Collection<DuplicateSearchResult> duplicateParties = 
			duplicatePartySearchResultComposite.getDuplicateParties();

		DuplicateSearchResult duplicateSearchResult = null;

		if (duplicateParties != null && !duplicateParties.isEmpty()) {
			logger.info("Found Party duplicate: " + duplicateParties.size());
			//Check whether we have single duplicate party with high threshold level
			if(duplicateParties.size() == 1){
				DuplicateSearchResult duplicateParty = duplicateParties.iterator().next();
				logger.info("Block Size: " + duplicateParty.getBlocks().size());
				// 11/05/2014 @GR - Skip Customer and Vendor blocked parties
				boolean isBlocked = false;
				for (BlockDTO block : duplicateParty.getBlocks()) {
					logger.info("Block Type: " + block.getBlockTypeCode());
					if(block.getBlockTypeCode().equals(PartyConstants.BLOCK_TYPE_VEND_BLK)
						|| block.getBlockTypeCode().equals("party_blk")){
						isBlocked = true;
						logger.info("Blocked Party found: " + duplicateParty.getPartyId());
						break;
					}
				}				
				if(PARTY_THRESHOLD_LEVEL_HIGH.equals(duplicateParty.getThresholdLevel()) && !isBlocked){
					logger.info("Duplicate pers/org Party Id: {} ", duplicateParty.getPartyId());
					duplicateSearchResult = duplicateParty;
				}
			}
			else{
				DuplicateSearchResult tempDuplicateParty = null;
				Long duplicatePartyId = new Long(0);
				Long currentPartyId = null;
				
				for (DuplicateSearchResult duplicateParty : duplicateParties) {
					logger.info("Block Size: " + duplicateParty.getBlocks().size());
					// 11/05/2014 @GR - Skip Customer and Vendor blocked parties
					boolean isBlocked = false;
					for (BlockDTO block : duplicateParty.getBlocks()) {
						logger.info("Block Type: " + block.getBlockTypeCode());
						if(block.getBlockTypeCode().equals(PartyConstants.BLOCK_TYPE_VEND_BLK)
							|| block.getBlockTypeCode().equals("party_blk")){
							isBlocked = true;
							logger.info("Blocked Party found: " + duplicateParty.getPartyId());
							break;
						}
					}
					//Duplicate party with high threshold are considered 
					if(PARTY_THRESHOLD_LEVEL_HIGH.equals(duplicateParty.getThresholdLevel()) && !isBlocked){
						currentPartyId = duplicateParty.getPartyId();
						//Need to select the latest duplicate party
						logger.info("Duplicate pers/org Party Id: {} ", currentPartyId);
						if(currentPartyId != null 
								&& currentPartyId.compareTo(duplicatePartyId) > 0){
							duplicatePartyId = currentPartyId;
							tempDuplicateParty = duplicateParty;
						}
					}
				}
				//Set the selected duplicate party in searchResult 
				duplicateSearchResult = tempDuplicateParty;
			}
			
			if(duplicateSearchResult == null){
				logger.info("!!!!Duplicate Not Found for Party - Adding New Party!!!!");
			}
		}
			
			//Iterate through the duplicates and set authorizations for med/low threshold
			/*
			if(duplicateSearchResult == null){
			for (DuplicateSearchResult duplicateParty : duplicateParties) {
				Collection<PartyAuthorizationDTO> authorizationColl = new ArrayList<PartyAuthorizationDTO>();		
				if(!PARTY_THRESHOLD_LEVEL_HIGH.equals(duplicateParty.getThresholdLevel())){
					logger.info("Medium Threshold Party Id: {} ", duplicateParty.getPartyId());
					PartyAuthorizationDTO authorizationDTO = new PartyAuthorizationDTO();
					authorizationDTO.setPotentialDuplicatePartyId(duplicateParty
							.getPartyId());
					authorizationDTO
							.setAuthorizationStatusCode(PartyConstants.PARTY_AUTH_STATUS_PENDING);
					authorizationDTO.setEffectiveDateTime(DateUtility
							.getSystemDateOnly());

					authorizationColl.add(authorizationDTO);					
					}
					party.setAuthorizations(authorizationColl);
				
				}
			}
		}
		else{
			logger.info("!!!!Duplicate Not Found for Party - Adding New Party!!!!");
		}
		*/
		return duplicateSearchResult;
	}

	
	/**
	 * Process party address details.
	 * @param parties list of parties
	 */
	private void processPartyAddressDetails(Collection<? extends PartyDTO> parties){
		
		if(parties != null && !parties.isEmpty()){
			for (PartyDTO partyDTO : parties) {
				//Process party address details
				AddressCollectionDTO addressCollectionDTO = partyDTO.getAddressCollection();
				//Check whether addressCollection is populated
				if(addressCollectionDTO != null){
					processPostalRegionDetails(addressCollectionDTO);
				}
			}
		}
	}
	
	/**
	 * Process Postal region details in address.
	 * @param addressCollectionDTO AddressCollectionDTO
	 */
	private void processPostalRegionDetails(AddressCollectionDTO addressCollectionDTO) {
		
		Collection<AddressDTO> addresses = addressCollectionDTO.getAddresses();

		//Check whether addresses are populated
		if(addresses != null && !addresses.isEmpty()){
			for (AddressDTO addressDTO : addresses) {
				//Check whether postal service region id is unavailable
				if(addressDTO.getPostalServiceRegionId() == null){
					String postalSvcRegionCode = addressDTO.getPostalSvcRegionCode();
					if(postalSvcRegionCode != null){
						//Retrieve postal service region id based on postal service region code
						Long postalServiceRegionId = 
							getPartyAddressService().retrievePostalServiceRegionIdByRegionAbrv(postalSvcRegionCode);
						if(postalServiceRegionId == null){
							throw new IIPCoreSystemException("Invalid Postal Service Region Code: " + postalSvcRegionCode);
						}
						addressDTO.setPostalServiceRegionId(postalServiceRegionId);
					}
					else{
						throw new IIPCoreSystemException("Postal Service Region Code is null");
					}
				}
			}
		}
	}


	/**
	 * @return PartyAddressService
	 */
	public PartyAddressService getPartyAddressService(){
		if(partyAddressService == null){
			partyAddressService = MuleServiceFactory.getService(PartyAddressService.class);
		}
		return partyAddressService;
	}

	/**
	 * @return ClearPartyDuplicateService
	 */
	private ClearPartyDuplicateService getClearPartyDuplicateService() {
		if(clearPartyDuplicateService == null){
			clearPartyDuplicateService = MuleServiceFactory.getService(ClearPartyDuplicateService.class);
		}
		return clearPartyDuplicateService;
	}

	/**
	 * @return PartyService
	 */
	private PartyService getPartyService(){
		if(partyService == null){
			partyService = MuleServiceFactory.getService(PartyService.class);
		}
		return partyService;
	}

	/**
	 * .
	 * @return PartyRoleService.
	 */
	public PartyRoleService getPartyRoleService() {
		if(partyRoleService == null) {
			partyRoleService = MuleServiceFactory.getService(PartyRoleService.class);
		}
		
		return partyRoleService;
	}
	
	
	/*
	 * @return ClientPartyMerger
	 * This explicitly returns new instance since Party merger is not threadsafe.
	 */
	
	private ClientPartyMerger getPartyMerger(){
		return new ClientPartyMerger();
	}	
	

	
	public PartyDTO saveClientParty(PartyDTO party2Update){
		
		PartyDTO returnParty = null;
		
		if(party2Update instanceof PersonDTO){
			returnParty = getPartyService().savePerson((PersonDTO)party2Update);
		}
		
		if(party2Update instanceof OrganizationDTO){
			returnParty = getPartyService().saveOrganization((OrganizationDTO)party2Update);
		}
		
		logger.info("Adding Party Role");
		//Add Party Roles
		Collection<PartyRoleDTO> roles = MuleServiceFactory.getService(PartyRoleService.class).retrievePartyRoles(returnParty.getRecordId());
		
        returnParty.setPartyRoleCollection(roles);
        
        //No need to retrieve liens again since we dont need updated lien information for party merges on the same party referenced by other participants
        // in this transaction. 
        
		//logger.info("Adding Liens");
        //Add Liens
		//Collection<PartyLienRestrictionDTO> retrievedLiens = MuleServiceFactory.getService(PartyService.class).retrievePartyLiensRestrictions(returnParty.getRecordId());
        //returnParty.setLiensRestrictions(retrievedLiens);
        
        return returnParty;		
		
	}
	
	public PartyDTO retrievePartyDetails(Long partyId){
		PartyDTO returnParty = null;
		if(partyId != null) {
			returnParty = getPartyService().retrievePersonOrOrganization(partyId);
			
			if(returnParty.getRecordId() == null){
				IIPCoreSystemException ex = new IIPCoreSystemException();
	            ex.setErrorCode(PartyConstants.SEARCH_RESULTS_EMPTY);
	            ex.setPayload("Unable to fetch Party for the given Party Id: " + partyId);
	            
	            IIPObjectError iipObjError = new IIPObjectError(
	    				PartySearchServiceImpl.class.getName(), "search", null,
	    				new String[]{"SearchResultsEmpty"}, null, MessageConstants.SEVERITY_ERROR);
				ex.setError(iipObjError);
				
	            throw ex;				
			}
			
			//Remove interActionChannel attributes from PartyDTO since PartyInteractionChannelCompositeDTO is used.
			
			returnParty.setInteractionChannels(null);
			
			//Remove Tax Status when None is available.
			/*if(returnParty.getTaxIdentifier() == null){
				returnParty.setTaxStatusIndicator(null);
			}*/
			
			
			//Remove PartyAgreementTypes (Duplicate info)
			returnParty.setPartyAgreementTypes(null);
			
			
			if(returnParty.getContext() == null){
				returnParty.setContext("party");
			}
			
			AddressCollectionDTO addressCollection = getPartyAddressService().retrieveAddressCollection(partyId);
			returnParty.setAddressCollection(addressCollection);
			
			//Add Party Roles and Liens
			Collection<PartyRoleDTO> roles = getPartyRoleService().retrievePartyRoles(partyId);
			
			Collection<PartyLienRestrictionDTO> retrievedLiens = getPartyService().retrievePartyLiensRestrictions(partyId);
	        returnParty.setPartyRoleCollection(roles);
	        returnParty.setLiensRestrictions(retrievedLiens);

		}
        return returnParty;

	}	
}
