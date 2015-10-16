package com.client.iip.integration.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.converter.IIPXStreamDateConverter;
import com.client.iip.integration.core.util.IIPXStream;
import com.fiserv.isd.iip.bc.address.PartyAddressService;
import com.fiserv.isd.iip.bc.address.api.AddressCollectionDTO;
import com.fiserv.isd.iip.bc.address.api.AddressDTO;
import com.fiserv.isd.iip.bc.address.api.AddressUsageDTO;
import com.fiserv.isd.iip.bc.address.api.UsageCollectionDTO;
import com.fiserv.isd.iip.bc.party.PartyConstants;
import com.fiserv.isd.iip.bc.party.api.AddressChannelDTO;
import com.fiserv.isd.iip.bc.party.api.ContactDTO;
import com.fiserv.isd.iip.bc.party.api.DegreeProfessionalDesignationDTO;
import com.fiserv.isd.iip.bc.party.api.IdentifierDTO;
import com.fiserv.isd.iip.bc.party.api.InteractionChannelAgreementTypeDTO;
import com.fiserv.isd.iip.bc.party.api.LanguageDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationOwnershipDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationOwnershipDetailDTO;
import com.fiserv.isd.iip.bc.party.api.PartyAgreementTypeDTO;
import com.fiserv.isd.iip.bc.party.api.PartyBankruptcyDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.bc.party.api.PartyInteractionChannelCompositeDTO;
import com.fiserv.isd.iip.bc.party.api.PartyLienPaymentDetailsDTO;
import com.fiserv.isd.iip.bc.party.api.PartyLienRestrictionDTO;
import com.fiserv.isd.iip.bc.party.api.PersonDTO;
import com.fiserv.isd.iip.bc.party.api.PhoneChannelDTO;
import com.fiserv.isd.iip.bc.party.api.PhoneDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleAgencyDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleAgencyLicenseDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleAgencyLicenseLOBDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleDriverLicenseDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleDriversDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleFinancialInstitutionDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleRegulatorDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleRegulatorJurisdictionDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleTrafficIncidentDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleVendorAssessmentSurchargeDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleVendorDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleVendorFeesDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleVendorTypeDTO;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;

/**
 * Will Merge the Imported Party With Existing Party.
 * !!!! Warning - This class is not thread safe !!!
 * @author saurabh.bhatnagar
 *
 */

@Pojo(id="claims.client.import.PartyMerger")
public class ClientPartyMerger {
	
	private Logger logger = LoggerFactory.getLogger(ClientPartyMerger.class);
	
	private PartyAddressService partyAddressService;
	
	private IIPXStream xstream;
	
	boolean updateRequiredFlag = false;
	
	//boolean to indicate whether to merge additional party info or not
	boolean mergeAdditionalPartyInfo = false;
	
	/**
	 * @return PartyAddressService
	 */
	public PartyAddressService getPartyAddressService(){
		if(partyAddressService == null){
			partyAddressService = MuleServiceFactory.getService(PartyAddressService.class);
		}
		return partyAddressService;
	}
	
	private IIPXStream getXStreamInstance() throws Exception{
		if(xstream == null){
			xstream = new IIPXStream();
			xstream.registerConverter(new IIPXStreamDateConverter());
			xstream.addDefaultImplementation(java.sql.Date.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.sql.Timestamp.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.sql.Time.class, java.util.Date.class);
		}
		return xstream;
	}
	
	
	/**
	 * Merger Imported Person With Existing Person.
	 * @param importedPerson
	 * @param existingPerson
	 */
		public boolean  mergePersonDTO(ClientPersonDTO importedPerson, PersonDTO existingPerson){
			//Disable Party Merge by default.
			if(System.getProperty("enablePartyMerge") == null || !System.getProperty("enablePartyMerge").equals("true")){
				return false;
			}			
			updateRequiredFlag = false;
			mergeAdditionalPartyInfo = importedPerson.isMergeAdditionalPartyInfoOnly();
			
			//Check for duplicate name address and communication usages
			if(ClientPartyValidator.getInstance().partyAttributeDupsFound(importedPerson)){
				return false;
			}
			
			
    		 if(null != importedPerson.getPersonGenderCode()){
	   			 if((existingPerson.getPersonGenderCode() == null && mergeAdditionalPartyInfo)
	   					 || (!mergeAdditionalPartyInfo && (existingPerson.getPersonGenderCode() == null ||
	   						!existingPerson.getPersonGenderCode().equals(importedPerson.getPersonGenderCode())))){
	   				existingPerson.setPersonGenderCode(importedPerson.getPersonGenderCode());
	   				 updateRequiredFlag = true;
	   			 }
	   		 }
	   		 
	   		 if(null != importedPerson.getPersonMaritalStatusCode()){
	   			 if((existingPerson.getPersonMaritalStatusCode() == null && mergeAdditionalPartyInfo)
	   					 || (!mergeAdditionalPartyInfo && (existingPerson.getPersonMaritalStatusCode() == null ||
	   						!existingPerson.getPersonMaritalStatusCode().equals(importedPerson.getPersonMaritalStatusCode())))){
	   				existingPerson.setPersonMaritalStatusCode(importedPerson.getPersonMaritalStatusCode());
	   				 updateRequiredFlag = true;
	   			 }
	   		 }
	   		 //Dates special handling - Dates are overlaid only when null regardless of the flag
	   		 if(null != importedPerson.getDateOfBirth() && existingPerson.getDateOfBirth() == null){
	   				existingPerson.setDateOfBirth(importedPerson.getDateOfBirth());
	   				 updateRequiredFlag = true;
	   			 }


	   		 if(null != importedPerson.getDateOfDeath() && existingPerson.getDateOfDeath() == null){
	   				existingPerson.setDateOfDeath(importedPerson.getDateOfDeath());
	   				 updateRequiredFlag = true;
	   			 }

	   		 
			if(importedPerson.isForeignCitizenIndicator() != existingPerson.isForeignCitizenIndicator()){
				existingPerson.setForeignCitizenIndicator(importedPerson.isForeignCitizenIndicator());
				updateRequiredFlag = true;
			}
			
			if(importedPerson.isNoDateOfBirthIndicator() != existingPerson.isNoDateOfBirthIndicator()){
				existingPerson.setNoDateOfBirthIndicator(importedPerson.isNoDateOfBirthIndicator());
				updateRequiredFlag = true;
			}
			
     	    if(null != importedPerson.getDegrees() && !importedPerson.getDegrees().isEmpty()){
				mergeDegrees(importedPerson, existingPerson);
			}
     	    

     	    return mergePartyDTO(importedPerson,existingPerson);
			
		}
		
		public void mergeDegrees(PersonDTO importedPersonDTO,PersonDTO existingPersonDTO){
			Collection<DegreeProfessionalDesignationDTO> tempDegreeDTO = new ArrayList<DegreeProfessionalDesignationDTO>();
			for(DegreeProfessionalDesignationDTO importedDegreeDTO:importedPersonDTO.getDegrees()){
				boolean flag = true; 
				for(DegreeProfessionalDesignationDTO existingDegreeDTO:existingPersonDTO.getDegrees()){
					if(null != importedDegreeDTO.getProfDesignationCode()&& null != existingDegreeDTO.getProfDesignationCode() &&
							importedDegreeDTO.getProfDesignationCode().equals(existingDegreeDTO.getProfDesignationCode())){
						flag = false;
						if(importedDegreeDTO.isProfDesignationPrintIndicator() != existingDegreeDTO.isProfDesignationPrintIndicator()){
							existingDegreeDTO.setProfDesignationPrintIndicator(importedDegreeDTO.isProfDesignationPrintIndicator());
							updateRequiredFlag = true;
						}
						
						if(importedDegreeDTO.isPrimaryDegreeProfDesignationIndicator() != existingDegreeDTO.isPrimaryDegreeProfDesignationIndicator()){
							existingDegreeDTO.setPrimaryDegreeProfDesignationIndicator(importedDegreeDTO.isPrimaryDegreeProfDesignationIndicator());
							updateRequiredFlag = true;
						}						

						
				   		 if(null != importedDegreeDTO.getProfDesignationPrintOrder()
				   			 && (existingDegreeDTO.getProfDesignationPrintOrder() == null
				   					 || (!mergeAdditionalPartyInfo && existingDegreeDTO.getProfDesignationPrintOrder().compareTo(importedDegreeDTO.getProfDesignationPrintOrder()) != 0))){
				   				existingDegreeDTO.setProfDesignationPrintOrder(importedDegreeDTO.getProfDesignationPrintOrder());
				   				updateRequiredFlag = true;
				   			 }
				   		 
				   		 if(null != importedDegreeDTO.getExternalSourceId()
					   			 && (existingDegreeDTO.getExternalSourceId() == null
					   					 || (!mergeAdditionalPartyInfo && !existingDegreeDTO.getExternalSourceId().equals(importedDegreeDTO.getExternalSourceId())))){
					   				existingDegreeDTO.setExternalSourceId(importedDegreeDTO.getExternalSourceId());
					   				updateRequiredFlag = true;
					   			 }
					   		 }					   		 
				   		 }

				if(flag){
					tempDegreeDTO.add(importedDegreeDTO);
					updateRequiredFlag = true;
				}
			}
			if(!tempDegreeDTO.isEmpty()){
				if(null != existingPersonDTO.getDegrees()){
					existingPersonDTO.getDegrees().addAll(tempDegreeDTO);
				}
				else{
					Collection<DegreeProfessionalDesignationDTO> personDegreeDTOs = new ArrayList<DegreeProfessionalDesignationDTO>();
					personDegreeDTOs.addAll(tempDegreeDTO);
					existingPersonDTO.setDegrees(personDegreeDTOs);
				}
			}
    		logger.info("mergeDegrees update flag status : {}", updateRequiredFlag);			
			
		}		
		
		/**
		 * Merger Imported Organization With Existing Organization.
		 * @param importedPerson
		 * @param existingPerson
		 */
		public boolean mergeOrganizationDTO(ClientOrganizationDTO importedOrganization, OrganizationDTO existingOrganization){
			//Disable Party Merge by default.
			if(System.getProperty("enablePartyMerge") == null || !System.getProperty("enablePartyMerge").equals("true")){
				return false;
			}
			updateRequiredFlag = false;
			mergeAdditionalPartyInfo = importedOrganization.isMergeAdditionalPartyInfoOnly();
			
			//Check for duplicate name, address and communication usages
			if(ClientPartyValidator.getInstance().partyAttributeDupsFound(importedOrganization)){
				return false;
			}
		
			//Dates special handling - Dates are overlaid only when null regardless of the flag
	   		 if(null != importedOrganization.getYearEstablished() && existingOrganization.getYearEstablished() == null){
	   				existingOrganization.setYearEstablished(importedOrganization.getYearEstablished());
	   				 updateRequiredFlag = true;
	   			 }
	   		 

		    if(null != importedOrganization.getCeasedOperationDate() && existingOrganization.getCeasedOperationDate() == null){
		   				existingOrganization.setCeasedOperationDate(importedOrganization.getCeasedOperationDate());
		   				 updateRequiredFlag = true;
		   	 	}

		    
	   		 if(null != importedOrganization.getSicCode()){
	   			 if((existingOrganization.getSicCode() == null && mergeAdditionalPartyInfo)
	   					 || (!mergeAdditionalPartyInfo && (existingOrganization.getSicCode() == null ||
	   						!existingOrganization.getSicCode().equals(importedOrganization.getSicCode())))){
	   				existingOrganization.setSicCode(importedOrganization.getSicCode());
	   				 updateRequiredFlag = true;
	   			 }
	   		 }
	   		 
	   		 if(null != importedOrganization.getNaicsCode()){
	   			 if((existingOrganization.getNaicsCode() == null && mergeAdditionalPartyInfo)
	   					 || (!mergeAdditionalPartyInfo && (existingOrganization.getNaicsCode() == null ||
	   						!existingOrganization.getNaicsCode().equals(importedOrganization.getNaicsCode())))){
	   				existingOrganization.setNaicsCode(importedOrganization.getNaicsCode());
	   				 updateRequiredFlag = true;
	   			 }
	   		 }		   		 

	   		 
	   		 if(null != importedOrganization.getOwnerships() && !importedOrganization.getOwnerships().isEmpty()){
				mergeOwnerships(importedOrganization, existingOrganization);
			}
	   		 
	   		 return mergePartyDTO(importedOrganization,existingOrganization);
		}
		
		
		public void mergeOwnerships(OrganizationDTO importedOrganizationDTO,OrganizationDTO existingOrganizationDTO){
			Collection<OrganizationOwnershipDTO> tempOrganizationOwnershipDTO = new ArrayList<OrganizationOwnershipDTO>();
			for(OrganizationOwnershipDTO importedOrganizationOwnershipDTO:importedOrganizationDTO.getOwnerships()){
				boolean flag = true; 
				for(OrganizationOwnershipDTO existingOrganizationOwnershipDTO:existingOrganizationDTO.getOwnerships()){
					if(null != importedOrganizationOwnershipDTO.getOwnershipTypeCode()&& null != existingOrganizationOwnershipDTO.getOwnershipTypeCode() &&
							importedOrganizationOwnershipDTO.getOwnershipTypeCode().equals(existingOrganizationOwnershipDTO.getOwnershipTypeCode())){
							flag = false;
				   		 if(null != importedOrganizationOwnershipDTO.getOtherOwnershipDescription()){
				   			 if((existingOrganizationOwnershipDTO.getOtherOwnershipDescription() == null && mergeAdditionalPartyInfo)
				   					 || (!mergeAdditionalPartyInfo && (existingOrganizationOwnershipDTO.getOtherOwnershipDescription() == null ||
				   						!existingOrganizationOwnershipDTO.getOtherOwnershipDescription().equals(importedOrganizationOwnershipDTO.getOtherOwnershipDescription())))){
				   				existingOrganizationOwnershipDTO.setOtherOwnershipDescription(importedOrganizationOwnershipDTO.getOtherOwnershipDescription());
				   				 updateRequiredFlag = true;
				   			 }
				   		 }	
				   		 //Dates special handling - Dates are overlaid only when null regardless of the flag
						if(null != importedOrganizationOwnershipDTO.getOwnershipEffectiveDate() && existingOrganizationOwnershipDTO.getOwnershipEffectiveDate() == null){
							existingOrganizationOwnershipDTO.setOwnershipEffectiveDate(importedOrganizationOwnershipDTO.getOwnershipEffectiveDate());
							 updateRequiredFlag = true;
						}
						if(null != importedOrganizationOwnershipDTO.getOwnershipEndDate() && existingOrganizationOwnershipDTO.getOwnershipEndDate() == null){
							existingOrganizationOwnershipDTO.setOwnershipEndDate(importedOrganizationOwnershipDTO.getOwnershipEndDate());
							updateRequiredFlag = true;
						}
						
				   		 if(null != importedOrganizationOwnershipDTO.getTotalOwnershipPercentage()){
				   			 if((existingOrganizationOwnershipDTO.getTotalOwnershipPercentage() == null && mergeAdditionalPartyInfo)
				   					 || (!mergeAdditionalPartyInfo && (existingOrganizationOwnershipDTO.getTotalOwnershipPercentage() == null ||
				   						existingOrganizationOwnershipDTO.getTotalOwnershipPercentage().compareTo(importedOrganizationOwnershipDTO.getTotalOwnershipPercentage()) != 0))){
				   				existingOrganizationOwnershipDTO.setTotalOwnershipPercentage(importedOrganizationOwnershipDTO.getTotalOwnershipPercentage());
				   				 updateRequiredFlag = true;
				   			 }
				   		 }						

						if(null != importedOrganizationOwnershipDTO.getOwnershipDetails() && !importedOrganizationOwnershipDTO.getOwnershipDetails().isEmpty()){
							Collection<OrganizationOwnershipDetailDTO> tempOrganizationOwnershipDetailDTO = new ArrayList<OrganizationOwnershipDetailDTO>();
							for(OrganizationOwnershipDetailDTO importedOrganizationOwnershipDetailDTO:importedOrganizationOwnershipDTO.getOwnershipDetails()){
								boolean tempflag = true;
								for(OrganizationOwnershipDetailDTO existingOrganizationOwnershipDetailDTO:existingOrganizationOwnershipDTO.getOwnershipDetails()){
									if(null != importedOrganizationOwnershipDetailDTO.getOwnerType() && null != existingOrganizationOwnershipDetailDTO.getOwnerType() &&
											importedOrganizationOwnershipDetailDTO.getOwnerType().equals(existingOrganizationOwnershipDetailDTO.getOwnerType())){
										tempflag = false;
										
								   		 if(null != importedOrganizationOwnershipDetailDTO.getOwnerName()){
								   			 if((existingOrganizationOwnershipDetailDTO.getOwnerName() == null && mergeAdditionalPartyInfo)
								   					 || (!mergeAdditionalPartyInfo && (existingOrganizationOwnershipDetailDTO.getOwnerName() == null ||
								   						!existingOrganizationOwnershipDetailDTO.getOwnerName().equals(importedOrganizationOwnershipDetailDTO.getOwnerName())))){
								   				existingOrganizationOwnershipDetailDTO.setOwnerName(importedOrganizationOwnershipDetailDTO.getOwnerName());
								   				 updateRequiredFlag = true;
								   			 }
								   		 }										
										
								   		 if(null != importedOrganizationOwnershipDetailDTO.getOtherOwnershipTypeCode()){
								   			 if((existingOrganizationOwnershipDetailDTO.getOtherOwnershipTypeCode() == null && mergeAdditionalPartyInfo)
								   					 || (!mergeAdditionalPartyInfo && (existingOrganizationOwnershipDetailDTO.getOtherOwnershipTypeCode() == null ||
								   						!existingOrganizationOwnershipDetailDTO.getOtherOwnershipTypeCode().equals(importedOrganizationOwnershipDetailDTO.getOtherOwnershipTypeCode())))){
								   				existingOrganizationOwnershipDetailDTO.setOtherOwnershipTypeCode(importedOrganizationOwnershipDetailDTO.getOtherOwnershipTypeCode());
								   				 updateRequiredFlag = true;
								   			 }
								   		 }										

								   		 if(null != importedOrganizationOwnershipDetailDTO.getOwnershipPercentage()){
								   			 if((importedOrganizationOwnershipDetailDTO.getOwnershipPercentage() == null && mergeAdditionalPartyInfo)
								   					 || (!mergeAdditionalPartyInfo && (existingOrganizationOwnershipDetailDTO.getOwnershipPercentage() == null ||
								   							existingOrganizationOwnershipDetailDTO.getOwnershipPercentage().compareTo(importedOrganizationOwnershipDetailDTO.getOwnershipPercentage()) != 0))){
								   				existingOrganizationOwnershipDetailDTO.setOwnershipPercentage(importedOrganizationOwnershipDetailDTO.getOwnershipPercentage());
								   				 updateRequiredFlag = true;
								   			 }
								   		 }											
										if(null != importedOrganizationOwnershipDetailDTO.getPartyId() && existingOrganizationOwnershipDetailDTO.getPartyId() == null){
											existingOrganizationOwnershipDetailDTO.setPartyId(importedOrganizationOwnershipDetailDTO.getPartyId());
										}
									}
								}
								if(tempflag){
									tempOrganizationOwnershipDetailDTO.add(importedOrganizationOwnershipDetailDTO);
								}
							}
							if(!tempOrganizationOwnershipDetailDTO.isEmpty()){
								if(null != existingOrganizationOwnershipDTO.getOwnershipDetails()){
									existingOrganizationOwnershipDTO.getOwnershipDetails().addAll(tempOrganizationOwnershipDetailDTO);
								}
								else{
									Collection<OrganizationOwnershipDetailDTO> organizationOwnershipDetailDTOs = new ArrayList<OrganizationOwnershipDetailDTO>();
									organizationOwnershipDetailDTOs.addAll(tempOrganizationOwnershipDetailDTO);
									existingOrganizationOwnershipDTO.setOwnershipDetails(organizationOwnershipDetailDTOs);
								}
								updateRequiredFlag = true;
							}
						}
					}
				}
				if(flag){
					tempOrganizationOwnershipDTO.add(importedOrganizationOwnershipDTO);
				}
			}
			if(!tempOrganizationOwnershipDTO.isEmpty()){
				if(null != existingOrganizationDTO.getOwnerships()){
					existingOrganizationDTO.getOwnerships().addAll(tempOrganizationOwnershipDTO);
				}
				else{
					Collection<OrganizationOwnershipDTO> organizationOwnershipDTOs = new ArrayList<OrganizationOwnershipDTO>();
					organizationOwnershipDTOs.addAll(tempOrganizationOwnershipDTO);
					existingOrganizationDTO.setOwnerships(organizationOwnershipDTOs);
				}
				updateRequiredFlag = true;
			}
		}	
	
	/*
	 * New Method added to flood address and communication with configured usage types depending on the context of the party
	 * 
	 * party context - flood party usage types
	 * clm context - flood party, clm usage types
	 * plcy context - flood party, clm, plcy usage types
	 * case context - flood party, clm, case usage types
	 */
	
	/**
	 * @param party
	 * @param ctx
	 * @return
	 */
	public boolean floodAddressUsageCommunicationAgreement(PartyDTO party, String ctx){
		
		String partyTypeCode = null;
		
		UsageCollectionDTO usageDTO = null;
		
		updateRequiredFlag = false;
		
		Collection <AddressUsageDTO> usages = new ArrayList<AddressUsageDTO>();
		InteractionChannelAgreementTypeDTO channelAgreement = null;
		PartyAgreementTypeDTO agreementPtcp = new PartyAgreementTypeDTO();
		Collection<InteractionChannelAgreementTypeDTO> channelAgreementTypes = new ArrayList<InteractionChannelAgreementTypeDTO>();
		Collection<PartyAgreementTypeDTO> agreementTypesPtcp = new ArrayList<PartyAgreementTypeDTO>();
		
		if(party instanceof OrganizationDTO){
			partyTypeCode = PartyConstants.PARTY_TYPE_ORGANIZATION;
		}else if(party instanceof PersonDTO){
			partyTypeCode = PartyConstants.PARTY_TYPE_PERSON;
		}else{
			partyTypeCode = party.getPartyTypeCode();
		}
		
		if(ctx != null && (ctx.equals(PartyConstants.PARTY_CONTEXT) || ctx.equals(PartyConstants.CLAIM_CONTEXT) 
				|| ctx.equals(PartyConstants.POLICY_CONTEXT) || ctx.equals(PartyConstants.CASE_CONTEXT))
				&& party.getAddressCollection() != null && party.getAddressCollection().getAddresses() != null
				&& party.getAddressCollection().getAddresses().size() == 1
				&& ((AddressDTO)party.getAddressCollection().getAddresses().iterator().next()).getUsages() != null){
					usageDTO = getPartyAddressService().retrieveAddressUsages(partyTypeCode, PartyConstants.PARTY_CONTEXT);
					usages.addAll(usageDTO.getUsages());
					//Set Interaction Channels
					channelAgreement = new InteractionChannelAgreementTypeDTO();
					channelAgreement.setAgreementTypeCode(PartyConstants.PARTY_CONTEXT);
					channelAgreement.setPrimaryUsageIndicator(true);
					channelAgreement.setEffectiveDate(DateUtility.getSystemDateOnly());
					channelAgreementTypes.add(channelAgreement);
					
					if(ctx.equals(PartyConstants.CLAIM_CONTEXT) || ctx.equals(PartyConstants.POLICY_CONTEXT) || ctx.equals(PartyConstants.CASE_CONTEXT)){
						usageDTO = getPartyAddressService().retrieveAddressUsages(partyTypeCode, PartyConstants.CLAIM_CONTEXT);
						usages.addAll(usageDTO.getUsages());
						//Set Interaction Channels
						channelAgreement = new InteractionChannelAgreementTypeDTO();
						channelAgreement.setAgreementTypeCode(PartyConstants.CLAIM_CONTEXT);
						channelAgreement.setPrimaryUsageIndicator(true);
						channelAgreement.setEffectiveDate(DateUtility.getSystemDateOnly());
						channelAgreementTypes.add(channelAgreement);
						//Set Agreement Types
						agreementPtcp = new PartyAgreementTypeDTO();
						agreementPtcp.setAggrementType(PartyConstants.CLAIM_CONTEXT);
						agreementPtcp.setEffectiveDateTime(DateUtility.getSystemDateOnly());
						agreementTypesPtcp.add(agreementPtcp);						
					}
					
					if(ctx.equals(PartyConstants.POLICY_CONTEXT)){
						usageDTO = getPartyAddressService().retrieveAddressUsages(partyTypeCode, PartyConstants.POLICY_CONTEXT);
						usages.addAll(usageDTO.getUsages());
						//Set Interaction Channels
						channelAgreement = new InteractionChannelAgreementTypeDTO();
						channelAgreement.setAgreementTypeCode(PartyConstants.POLICY_CONTEXT);
						channelAgreement.setPrimaryUsageIndicator(true);
						channelAgreement.setEffectiveDate(DateUtility.getSystemDateOnly());
						channelAgreementTypes.add(channelAgreement);
						//Set Agreement Types
						agreementPtcp = new PartyAgreementTypeDTO();
						agreementPtcp.setAggrementType(PartyConstants.POLICY_CONTEXT);
						agreementPtcp.setEffectiveDateTime(DateUtility.getSystemDateOnly());
						agreementTypesPtcp.add(agreementPtcp);							
					}
					
					if(ctx.equals(PartyConstants.CASE_CONTEXT)){
						usageDTO = getPartyAddressService().retrieveAddressUsages(partyTypeCode, PartyConstants.CASE_CONTEXT);
						usages.addAll(usageDTO.getUsages());	
						//Set Interaction Channels
						channelAgreement = new InteractionChannelAgreementTypeDTO();
						channelAgreement.setAgreementTypeCode(PartyConstants.CASE_CONTEXT);
						channelAgreement.setPrimaryUsageIndicator(true);
						channelAgreement.setEffectiveDate(DateUtility.getSystemDateOnly());
						channelAgreementTypes.add(channelAgreement);
						//Set Agreement Types
						agreementPtcp = new PartyAgreementTypeDTO();
						agreementPtcp.setAggrementType(PartyConstants.CASE_CONTEXT);
						agreementPtcp.setEffectiveDateTime(DateUtility.getSystemDateOnly());
						agreementTypesPtcp.add(agreementPtcp);							
					}

					//Set the Configured usage for the party address
					if(((AddressDTO)party.getAddressCollection().getAddresses().iterator().next()).getUsages().isEmpty()){
						((AddressDTO)party.getAddressCollection().getAddresses().iterator().next()).setUsages(usages);		
						//Check for communication agreement types
						if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getPhoneChannels() != null){
							Collection<PhoneChannelDTO> phoneChannels = party.getPartyInteractionChannelCompositeDTO().getPhoneChannels();
							for(PhoneChannelDTO phone: phoneChannels){
								if(phone.getInteractionChannelAgreementTypes() != null && phone.getInteractionChannelAgreementTypes().isEmpty()){
									phone.setInteractionChannelAgreementTypes(channelAgreementTypes);
									updateRequiredFlag = true;
								}
							}

						}
						
						if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getEmailChannels() != null){
							Collection<AddressChannelDTO> addressChannels = party.getPartyInteractionChannelCompositeDTO().getEmailChannels();
							for(AddressChannelDTO address: addressChannels){
								if(address.getInteractionChannelAgreementTypes() != null && address.getInteractionChannelAgreementTypes().isEmpty()){
									address.setInteractionChannelAgreementTypes(channelAgreementTypes);
									updateRequiredFlag = true;
								}
							}

						}						

						if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getOtherChannels() != null){
							Collection<AddressChannelDTO> addressChannels = party.getPartyInteractionChannelCompositeDTO().getOtherChannels();
							for(AddressChannelDTO address: addressChannels){
								if(address.getInteractionChannelAgreementTypes() != null && address.getInteractionChannelAgreementTypes().isEmpty()){
									address.setInteractionChannelAgreementTypes(channelAgreementTypes);
									updateRequiredFlag = true;
								}
							}

						}
						
						//Check for agreement type participation
						if(party.getAgreementTypesPtcp() != null && party.getAgreementTypesPtcp().isEmpty()){
							party.setAgreementTypesPtcp(agreementTypesPtcp);
							updateRequiredFlag = true;
						}
						
						
					}else{
						//Sync address usage types
						Collection<AddressUsageDTO> existingUsages = ((AddressDTO)party.getAddressCollection().getAddresses().iterator().next()).getUsages();
						for(AddressUsageDTO configusage: usages){
							boolean usagefound = false;
							for(AddressUsageDTO currusage: existingUsages){
									if(currusage.getAgreementTypeCd().equals(configusage.getAgreementTypeCd()) 
										&& currusage.getUsageTypeCd().equals(configusage.getUsageTypeCd())){
										usagefound = true;
										break;
									}
							}
							
							if(!usagefound){
								//Add the missing usage type
								existingUsages.add(configusage);
								updateRequiredFlag = true;
							}
						}
						((AddressDTO)party.getAddressCollection().getAddresses().iterator().next()).setUsages(existingUsages);
						//sync communication agreement types
						//Phone
						if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getPhoneChannels() != null){
							Collection<PhoneChannelDTO> phoneChannels = party.getPartyInteractionChannelCompositeDTO().getPhoneChannels();
							for(InteractionChannelAgreementTypeDTO configAgreement: channelAgreementTypes){
								boolean agrefound = false;
								//Phones
								for(PhoneChannelDTO phone: phoneChannels){
									agrefound = false;	
									Collection<InteractionChannelAgreementTypeDTO> tmpAgreementTypes = phone.getInteractionChannelAgreementTypes();
									for(InteractionChannelAgreementTypeDTO currAgreementType: tmpAgreementTypes){
										if(currAgreementType.getAgreementTypeCode().equals(configAgreement.getAgreementTypeCode())){
											agrefound = true;
											break;
										}
									}
									
									if(!agrefound){
										//Add the agreement type
										InteractionChannelAgreementTypeDTO tmpChannelagreement = new InteractionChannelAgreementTypeDTO();
										tmpChannelagreement.setAgreementTypeCode(configAgreement.getAgreementTypeCode());
										tmpChannelagreement.setPrimaryUsageIndicator(true);
										tmpChannelagreement.setEffectiveDate(DateUtility.getSystemDateOnly());
										tmpAgreementTypes.add(tmpChannelagreement);
										phone.setInteractionChannelAgreementTypes(tmpAgreementTypes);
										updateRequiredFlag = true;
									}
								}
							}
						}
						
						//AddressChannel - Email
						if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getEmailChannels() != null){
							Collection<AddressChannelDTO> addressChannels = party.getPartyInteractionChannelCompositeDTO().getEmailChannels();
							for(InteractionChannelAgreementTypeDTO configAgreement: channelAgreementTypes){
								boolean agrefound = false;
								//Phones
								for(AddressChannelDTO address: addressChannels){
									agrefound = false;	
									Collection<InteractionChannelAgreementTypeDTO> tmpAgreementTypes = address.getInteractionChannelAgreementTypes();
									for(InteractionChannelAgreementTypeDTO currAgreementType: tmpAgreementTypes){
										if(currAgreementType.getAgreementTypeCode().equals(configAgreement.getAgreementTypeCode())){
											agrefound = true;
											break;
										}
									}
									
									if(!agrefound){
										//Add the agreement type
										InteractionChannelAgreementTypeDTO tmpChannelagreement = new InteractionChannelAgreementTypeDTO();
										tmpChannelagreement.setAgreementTypeCode(configAgreement.getAgreementTypeCode());
										tmpChannelagreement.setPrimaryUsageIndicator(true);
										tmpChannelagreement.setEffectiveDate(DateUtility.getSystemDateOnly());
										tmpAgreementTypes.add(tmpChannelagreement);
										address.setInteractionChannelAgreementTypes(tmpAgreementTypes);
										updateRequiredFlag = true;
									}
								}
							}
						}
						
						//AddressChannel - Other
						if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getOtherChannels() != null){
							Collection<AddressChannelDTO> addressChannels = party.getPartyInteractionChannelCompositeDTO().getOtherChannels();
							for(InteractionChannelAgreementTypeDTO configAgreement: channelAgreementTypes){
								boolean agrefound = false;
								//Phones
								for(AddressChannelDTO address: addressChannels){
									agrefound = false;	
									Collection<InteractionChannelAgreementTypeDTO> tmpAgreementTypes = address.getInteractionChannelAgreementTypes();
									for(InteractionChannelAgreementTypeDTO currAgreementType: tmpAgreementTypes){
										if(currAgreementType.getAgreementTypeCode().equals(configAgreement.getAgreementTypeCode())){
											agrefound = true;
											break;
										}
									}
									
									if(!agrefound){
										//Add the agreement type
										InteractionChannelAgreementTypeDTO tmpChannelagreement = new InteractionChannelAgreementTypeDTO();
										tmpChannelagreement.setAgreementTypeCode(configAgreement.getAgreementTypeCode());
										tmpChannelagreement.setPrimaryUsageIndicator(true);
										tmpChannelagreement.setEffectiveDate(DateUtility.getSystemDateOnly());
										tmpAgreementTypes.add(tmpChannelagreement);
										address.setInteractionChannelAgreementTypes(tmpAgreementTypes);
										updateRequiredFlag = true;
									}
								}
							}
						}
						
						//Sync agreement type participation
						if(party.getAgreementTypesPtcp() != null){
							Collection<PartyAgreementTypeDTO> partyAgreements = party.getAgreementTypesPtcp();
							for(PartyAgreementTypeDTO configAgreement:agreementTypesPtcp){
								boolean agrefound = false;
								for(PartyAgreementTypeDTO agreement:partyAgreements){
									if(configAgreement.getAggrementType().equals(agreement.getAggrementType())){
										agrefound = true;
										break;									
									}
								}
								if(!agrefound){
									//Add the agreement type
									PartyAgreementTypeDTO tmpAgreementTypePtcp = new PartyAgreementTypeDTO();
									tmpAgreementTypePtcp.setAggrementType(configAgreement.getAggrementType());
									tmpAgreementTypePtcp.setEffectiveDateTime(DateUtility.getSystemDateOnly());
									partyAgreements.add(tmpAgreementTypePtcp);
									updateRequiredFlag = true;
								}
							}
							party.setAgreementTypesPtcp(partyAgreements);
						}						
			
				}
				
				//Remove Party agreement type ptcp - It is found to cause duplicate party participations.
				removePartyAgreementTypeParticipants(party);
		} else if(ctx != null 
				&& party.getAddressCollection() != null && party.getAddressCollection().getAddresses() != null
				&& !party.getAddressCollection().getAddresses().isEmpty()
				&& ((AddressDTO)party.getAddressCollection().getAddresses().iterator().next()).getUsages() != null) {
			
			List<String> claimUsageContextList = new ArrayList<String>();
			List<String> usageContextList = new ArrayList<String>();
			
			for(AddressDTO addressDTO : party.getAddressCollection().getAddresses()) {
				for(AddressUsageDTO addressUsageDTO : addressDTO.getUsages()) {
					if(addressUsageDTO.getAgreementTypeCd().equals(ctx) ) {
						usageContextList.add(addressUsageDTO.getUsageTypeCd());
					}
				}
			}

			for(AddressDTO addressDTO : party.getAddressCollection().getAddresses()) {
				for(AddressUsageDTO addressUsageDTO : addressDTO.getUsages()) {
					if(addressUsageDTO.getAgreementTypeCd().equals(PartyConstants.CLAIM_CONTEXT)) {
						claimUsageContextList.add(addressUsageDTO.getUsageTypeCd());
					} 
				}
				for(String usage : claimUsageContextList) {
					if(!usageContextList.contains(usage)) {
						usageContextList.add(usage);
						
						AddressUsageDTO addressUsage = new AddressUsageDTO();
						addressUsage.setAgreementTypeCd(ctx);
						addressUsage.setEffectiveDateTime(DateUtility.getSystemDateOnly());
						addressUsage.setUsageTypeCd(usage);
						
						addressDTO.getUsages().add(addressUsage);
						updateRequiredFlag = true;
					}
				}
			}
			
			if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getPhoneChannels() != null){
				Collection<PhoneChannelDTO> phoneChannels = party.getPartyInteractionChannelCompositeDTO().getPhoneChannels();
				claimUsageContextList = new ArrayList<String>();
				usageContextList = new ArrayList<String>();

				//Phones
				for(PhoneChannelDTO phone: phoneChannels){
					Collection<InteractionChannelAgreementTypeDTO> tmpAgreementTypes = phone.getInteractionChannelAgreementTypes();
					boolean agrefound = false;
					for(InteractionChannelAgreementTypeDTO currAgreementType: tmpAgreementTypes){
						if(currAgreementType.getAgreementTypeCode().equals(ctx)) {
							agrefound = true;
						}
					}
					
					if(!agrefound) {
						InteractionChannelAgreementTypeDTO tmpChannelagreement = new InteractionChannelAgreementTypeDTO();
						tmpChannelagreement.setAgreementTypeCode(ctx);
						tmpChannelagreement.setPrimaryUsageIndicator(true);
						tmpChannelagreement.setEffectiveDate(DateUtility.getSystemDateOnly());
						tmpAgreementTypes.add(tmpChannelagreement);
						phone.setInteractionChannelAgreementTypes(tmpAgreementTypes);							
						updateRequiredFlag = true;
					}
				}
			}
			
			//AddressChannel - Email
			if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getEmailChannels() != null){
				Collection<AddressChannelDTO> addressChannels = party.getPartyInteractionChannelCompositeDTO().getEmailChannels();
				claimUsageContextList = new ArrayList<String>();
				usageContextList = new ArrayList<String>();

				for(AddressChannelDTO address: addressChannels){
					Collection<InteractionChannelAgreementTypeDTO> tmpAgreementTypes = address.getInteractionChannelAgreementTypes();
					boolean agrefound = false;
					for(InteractionChannelAgreementTypeDTO currAgreementType: tmpAgreementTypes){
						if(currAgreementType.getAgreementTypeCode().equals(ctx)) {
							agrefound = true;
						}
					}
					if(!agrefound) {
						InteractionChannelAgreementTypeDTO tmpChannelagreement = new InteractionChannelAgreementTypeDTO();
						tmpChannelagreement.setAgreementTypeCode(ctx);
						tmpChannelagreement.setPrimaryUsageIndicator(true);
						tmpChannelagreement.setEffectiveDate(DateUtility.getSystemDateOnly());
						tmpAgreementTypes.add(tmpChannelagreement);
						address.setInteractionChannelAgreementTypes(tmpAgreementTypes);							
						updateRequiredFlag = true;
					}
				}
			}

			//AddressChannel - Other
			if(party.getPartyInteractionChannelCompositeDTO() != null && party.getPartyInteractionChannelCompositeDTO().getOtherChannels() != null){
				Collection<AddressChannelDTO> addressChannels = party.getPartyInteractionChannelCompositeDTO().getOtherChannels();
				claimUsageContextList = new ArrayList<String>();
				usageContextList = new ArrayList<String>();

				for(AddressChannelDTO address: addressChannels){
					Collection<InteractionChannelAgreementTypeDTO> tmpAgreementTypes = address.getInteractionChannelAgreementTypes();
					boolean agrefound = false;
					for(InteractionChannelAgreementTypeDTO currAgreementType: tmpAgreementTypes){
						if(currAgreementType.getAgreementTypeCode().equals(ctx)) {
							agrefound = true;
						}
					}
					if(!agrefound) {
						InteractionChannelAgreementTypeDTO tmpChannelagreement = new InteractionChannelAgreementTypeDTO();
						tmpChannelagreement.setAgreementTypeCode(ctx);
						tmpChannelagreement.setPrimaryUsageIndicator(true);
						tmpChannelagreement.setEffectiveDate(DateUtility.getSystemDateOnly());
						tmpAgreementTypes.add(tmpChannelagreement);
						address.setInteractionChannelAgreementTypes(tmpAgreementTypes);							
						updateRequiredFlag = true;
					}
				}
			}

			//Remove Party agreement type ptcp - It is found to cause duplicate party participations.
			removePartyAgreementTypeParticipants(party);
		}
		
		//Set the context to party
		party.setContext(PartyConstants.PARTY_CONTEXT);		
		
		logger.info("floodAddressUsageCommunicationAgreement update flag status : {}", updateRequiredFlag);
		
		//Logging
		try{			
			logger.info("Party Image Before Persisting: {}", getXStreamInstance().convert2XML(party));
			}catch(Exception ex){
				throw new IIPCoreSystemException(ex);
			}
		
		return updateRequiredFlag;
		
	}
	
	/**
	 * Remove Party Agreement Type Participants 
	 * @param party
	 */
	private void removePartyAgreementTypeParticipants(PartyDTO party) {
		if(party.getAgreementTypesPtcp() != null){
			party.setPartyAgreementTypes(null);
			Collection<PartyAgreementTypeDTO> partyAgrePtcp = party.getAgreementTypesPtcp();
			Collection<PartyAgreementTypeDTO> tmpPartyAgrePtcp = new ArrayList<PartyAgreementTypeDTO>();
			for(PartyAgreementTypeDTO agreeptcp:partyAgrePtcp){
				if(!agreeptcp.getAggrementType().equals(PartyConstants.PARTY_CONTEXT)){
					tmpPartyAgrePtcp.add(agreeptcp);
					if(agreeptcp.getRecordId() == null){
						updateRequiredFlag = true;
					}
				}		
			}
			party.setAgreementTypesPtcp(tmpPartyAgrePtcp);
		}					

	}
	

	
	
	/**
	 * Merge Imported PartyDTO to Existing PartyDTO
	 * @param importedParty
	 * @param existingParty
	 */
	public boolean mergePartyDTO(PartyDTO importedParty, PartyDTO existingParty){

		if(mergeAdditionalPartyInfo) {
			if(importedParty.getCountryCode() != null  
					&& existingParty.getCountryCode() == null) {
				existingParty.setCountryCode(importedParty.getCountryCode());
				updateRequiredFlag = true;
			}
		} else {
			if(importedParty.getCountryCode() != null 
				&& existingParty.getCountryCode() != null 
				&& !existingParty.getCountryCode().equalsIgnoreCase(importedParty.getCountryCode())) {
				existingParty.setCountryCode(importedParty.getCountryCode());
				updateRequiredFlag = true;
			}
		}
		
		if(existingParty.isIncompDocuments() != importedParty.isIncompDocuments()){
			existingParty.setIncompDocuments(importedParty.isIncompDocuments());
			updateRequiredFlag = true;
		}
		if(existingParty.isProtectedRecordIndicator() != importedParty.isProtectedRecordIndicator()){
			existingParty.setProtectedRecordIndicator(importedParty.isProtectedRecordIndicator());
			updateRequiredFlag = true;
		}
		
		if(mergeAdditionalPartyInfo) {
			if(importedParty.getRecordSourceCode() != null  
					&& existingParty.getRecordSourceCode() == null) {
				existingParty.setRecordSourceCode(importedParty.getRecordSourceCode());
				updateRequiredFlag = true;
			}
		} else {
			if(importedParty.getRecordSourceCode() != null 
				&& existingParty.getRecordSourceCode() != null 
				&& !existingParty.getRecordSourceCode().equalsIgnoreCase(importedParty.getRecordSourceCode())) {
				existingParty.setRecordSourceCode(importedParty.getRecordSourceCode());
				updateRequiredFlag = true;
			}
		}
		/*if(null!=importedParty.getPartyNumber()){
				existingParty.setPartyNumber(importedParty.getPartyNumber());
		}*/
		existingParty.setVendorRoleExists(importedParty.getVendorRoleExists());
		if(null != importedParty.getContext()){
			existingParty.setContext(importedParty.getContext());
		}else{
			existingParty.setContext(PartyConstants.CLAIM_CONTEXT);
		}
		mergeTaxStatusIndicator(importedParty,existingParty);
		mergeTaxIdentifier(importedParty,existingParty);
		mergeLanguages(importedParty,existingParty);
		mergeContacts(importedParty,existingParty);
		mergeIdentifiers(importedParty,existingParty);
		//mergeChildSupports(importedParty,existingParty);
		mergeAddressColletionDTO(importedParty,existingParty);
		mergePartyRoleCollection(importedParty,existingParty);
		mergePartyIntractionChannelCompositeDTO(importedParty,existingParty);
		mergeAgreementTypePtcp(importedParty,existingParty);
		mergeBankruptcyDetails(importedParty,existingParty);
		//mergeAuthorizations(importedParty,existingParty);
		mergeLiensRestrictions(importedParty,existingParty);
		//Sync Address Usage and communication context with the configuration.
		boolean tmpFlag = updateRequiredFlag;
		floodAddressUsageCommunicationAgreement(existingParty, existingParty.getContext());
		
		logger.info("mergePartyDTO update flag status : {}", ((tmpFlag)?true:updateRequiredFlag));
		
		return (tmpFlag)?true:updateRequiredFlag;
	}
	/**
	 * Merge Tax Status Indicator
	 * @param importedParty
	 * @param existingParty
	 */
	public void mergeTaxStatusIndicator(PartyDTO importedParty, PartyDTO existingParty){
		if(importedParty.getTaxStatusIndicator() != null 
				&& (existingParty.getTaxIdentifier()==null || (existingParty.getTaxIdentifier()!=null && existingParty.getTaxIdentifier().getTaxIdentifierTypeCode()==null))){
			existingParty.setTaxStatusIndicator(importedParty.getTaxStatusIndicator());
		}
	}
	
	/**
	 * Merge Tax Identifier
	 * @param importedParty
	 * @param existingParty
	 */
	public void mergeTaxIdentifier(PartyDTO importedParty, PartyDTO existingParty){

			if(existingParty.getTaxIdentifier()!=null && importedParty.getTaxIdentifier()!=null
				&& existingParty.getTaxIdentifier().getTaxIdentifierTypeCode()	!= null && importedParty.getTaxIdentifier().getTaxIdentifierTypeCode() != null					
				&& existingParty.getTaxIdentifier().getTaxIdentifierTypeCode().equals(importedParty.getTaxIdentifier().getTaxIdentifierTypeCode())){
			existingParty.getTaxIdentifier().setUpdated(importedParty.getTaxIdentifier().isUpdated());
			if(importedParty.getTaxIdentifier().getTaxIdentifierNumber()!=null){
				if((existingParty.getTaxIdentifier().getTaxIdentifierNumber() == null && mergeAdditionalPartyInfo)
						|| (!mergeAdditionalPartyInfo && (existingParty.getTaxIdentifier().getTaxIdentifierNumber() == null ||
							!existingParty.getTaxIdentifier().getTaxIdentifierNumber().equals(importedParty.getTaxIdentifier().getTaxIdentifierNumber())))){
					existingParty.getTaxIdentifier().setTaxIdentifierNumber(importedParty.getTaxIdentifier().getTaxIdentifierNumber());
					updateRequiredFlag = true;
				}
			}
			
			   /*Effective - End Dates special handling - They are set only when it is null regardless of the flag.
			   Since these are system dates and would be different everytime on the incoming party data*/
			if(importedParty.getTaxIdentifier().getTaxIdentifierEndDate() != null &&
					existingParty.getTaxIdentifier().getTaxIdentifierEndDate() == null ){
					existingParty.getTaxIdentifier().setTaxIdentifierEndDate(importedParty.getTaxIdentifier().getTaxIdentifierEndDate());
					updateRequiredFlag = true;
				}

			
			if(importedParty.getTaxIdentifier().getTaxIdentifierEffDate() !=null &&
				existingParty.getTaxIdentifier().getTaxIdentifierEffDate() == null){
					existingParty.getTaxIdentifier().setTaxIdentifierEffDate(importedParty.getTaxIdentifier().getTaxIdentifierEffDate());
					updateRequiredFlag = true;
				}
		
			if(existingParty.getTaxIdentifier().isTaxIdAlreadyExistFlag() != importedParty.getTaxIdentifier().isTaxIdAlreadyExistFlag() && !mergeAdditionalPartyInfo){
				existingParty.getTaxIdentifier().setTaxIdAlreadyExistFlag(importedParty.getTaxIdentifier().isTaxIdAlreadyExistFlag());
				updateRequiredFlag = true;
			}
			
			if(importedParty.getTaxIdentifier().getTaxFillingStatusCode()!=null){
				if((existingParty.getTaxIdentifier().getTaxFillingStatusCode() == null && mergeAdditionalPartyInfo) 
						|| (!mergeAdditionalPartyInfo && (existingParty.getTaxIdentifier().getTaxFillingStatusCode() == null ||
							!existingParty.getTaxIdentifier().getTaxFillingStatusCode().equals(importedParty.getTaxIdentifier().getTaxFillingStatusCode())))){
					existingParty.getTaxIdentifier().setTaxFillingStatusCode(importedParty.getTaxIdentifier().getTaxFillingStatusCode());
					updateRequiredFlag = true;
				}
			}
			if(importedParty.getTaxIdentifier().getTaxExmtCount()!=null){
				if((importedParty.getTaxIdentifier().getTaxExmtCount() == null  && mergeAdditionalPartyInfo) 
						|| (!mergeAdditionalPartyInfo && (importedParty.getTaxIdentifier().getTaxExmtCount() == null ||
							importedParty.getTaxIdentifier().getTaxExmtCount().compareTo(importedParty.getTaxIdentifier().getTaxExmtCount()) != 0))){
					existingParty.getTaxIdentifier().setTaxExmtCount(importedParty.getTaxIdentifier().getTaxExmtCount());
					updateRequiredFlag = true;
				}
			}
		}else if(importedParty.getTaxIdentifier()!=null){
			existingParty.setTaxIdentifier(importedParty.getTaxIdentifier());
			updateRequiredFlag = true;
		}
		
		logger.info("mergeTaxIdentifier update flag status : {} ", updateRequiredFlag);
	}
	
	/**
	 * Merge Languages
	 * @param importedParty
	 * @param existingParty
	 */
	public void mergeLanguages(PartyDTO importedParty, PartyDTO existingParty){
		if(null != importedParty.getLanguages()&& !importedParty.getLanguages().isEmpty()){
			Collection<LanguageDTO> tempLanguageDTOs = new ArrayList<LanguageDTO>();
			for(LanguageDTO importedLanguageDTO : importedParty.getLanguages()){
				boolean flag = true;
				for(LanguageDTO existingLanguageDTO : existingParty.getLanguages()){
					
					if(mergeAdditionalPartyInfo) {
						if(existingLanguageDTO.getLanguageTypeCode() == null 
								&& importedLanguageDTO.getLanguageTypeCode() != null) {
							flag = false;
							existingLanguageDTO.setLanguageTypeCode(importedLanguageDTO.getLanguageTypeCode());
							
							if(importedLanguageDTO.getExternalSourceId() != null 
									&& existingLanguageDTO.getExternalSourceId() == null){
							     existingLanguageDTO.setExternalSourceId(importedLanguageDTO.getExternalSourceId());
							}
							updateRequiredFlag = true;
						}
					} else {
						if(importedLanguageDTO.getLanguageTypeCode().equals(existingLanguageDTO.getLanguageTypeCode())) {
							flag = false;
							existingLanguageDTO.setPrimaryLanguageIndicator(importedLanguageDTO.isPrimaryLanguageIndicator());
							if(importedLanguageDTO.getExternalSourceId() != null 
									&& (existingLanguageDTO.getExternalSourceId() == null 
										|| !existingLanguageDTO.getExternalSourceId().equals(importedLanguageDTO.getExternalSourceId()))) {
							     	existingLanguageDTO.setExternalSourceId(importedLanguageDTO.getExternalSourceId());
							     	updateRequiredFlag = true;
							}
						}
					}
				}
				if(flag){
					tempLanguageDTOs.add(importedLanguageDTO);
					updateRequiredFlag = true;
				}
			}
			if(!tempLanguageDTOs.isEmpty()){
				if(null != existingParty.getLanguages() && !existingParty.getLanguages().isEmpty()){
					existingParty.getLanguages().addAll(tempLanguageDTOs);
				}else{
					Collection<LanguageDTO> languageDTOs = new ArrayList<LanguageDTO>();
					languageDTOs.addAll(tempLanguageDTOs);
					existingParty.setLanguages(languageDTOs);
				}
			}
		}
		logger.info("mergeLanguages update flag status : {}", updateRequiredFlag);
	}
	
	/**
	 * Merge Contacts
	 * @param importedParty
	 * @param existingParty
	 */
	public void mergeContacts(PartyDTO importedParty, PartyDTO existingParty){
		if(null != importedParty.getContacts() && !importedParty.getContacts().isEmpty()){
			Collection<ContactDTO> tempContactDTOs = new ArrayList<ContactDTO>();
			for(ContactDTO importedContactDTO : importedParty.getContacts()){
				boolean flag = true;
				for(ContactDTO existingContactDTO : existingParty.getContacts()) {
					if(importedContactDTO.getContactTypeCode()!=null && existingContactDTO.getContactTypeCode()!=null 
							&& importedContactDTO.getContactTypeCode().equals(existingContactDTO.getContactTypeCode())
							&& ((importedContactDTO.getContactNameFirst()!=null && existingContactDTO.getContactNameFirst()!=null
							&& importedContactDTO.getContactNameFirst().equals(existingContactDTO.getContactNameFirst())
							&& importedContactDTO.getContactNameLast()!=null && existingContactDTO.getContactNameLast()!=null
							&& importedContactDTO.getContactNameLast().equals(existingContactDTO.getContactNameLast()))
							|| (importedContactDTO.getContactName()!=null && existingContactDTO.getContactName()!=null
							&& importedContactDTO.getContactName().equals(existingContactDTO.getContactName()))
							)){
						flag = false;
						
						if(mergeAdditionalPartyInfo) {
							if(importedContactDTO.getContactNameFormat() != null 
									&& existingContactDTO.getContactNameFormat() == null) {
								existingContactDTO.setContactNameFormat(importedContactDTO.getContactNameFormat());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getComment() != null 
									&& existingContactDTO.getComment() == null) {
								existingContactDTO.setComment(importedContactDTO.getComment());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getEmailAddress() != null 
									&& existingContactDTO.getEmailAddress() == null) {
								existingContactDTO.setEmailAddress(importedContactDTO.getEmailAddress());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getContactEffDate() != null
									&& existingContactDTO.getContactEffDate() == null){
								existingContactDTO.setContactEffDate(importedContactDTO.getContactEffDate());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getContactEndDate() != null
									&& existingContactDTO.getContactEndDate() == null){
								existingContactDTO.setContactEndDate(importedContactDTO.getContactEndDate());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getContactEffDate() != null
									&& existingContactDTO.getContactEffDate() == null){
								existingContactDTO.setContactEffDate(importedContactDTO.getContactEffDate());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getContactEndDate() != null
									&& existingContactDTO.getContactEndDate() == null){
								existingContactDTO.setContactEndDate(importedContactDTO.getContactEndDate());
								updateRequiredFlag = true;
							}
						} else {
							if(importedContactDTO.getContactNameFormat() != null 
									&& (existingContactDTO.getContactNameFormat() == null 
										|| !existingContactDTO.getContactNameFormat().equals(importedContactDTO.getContactNameFormat()))) {
								existingContactDTO.setContactNameFormat(importedContactDTO.getContactNameFormat());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getComment()!= null
								&& (existingContactDTO.getComment() == null 
										|| !existingContactDTO.getComment().equals(importedContactDTO.getComment()))){
								existingContactDTO.setComment(importedContactDTO.getComment());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getEmailAddress() != null 
								&& (existingContactDTO.getEmailAddress() == null 
										|| !existingContactDTO.getEmailAddress().equals(importedContactDTO.getEmailAddress()))) {
								existingContactDTO.setEmailAddress(importedContactDTO.getEmailAddress());
								updateRequiredFlag = true;
							}
 						   /*Effective - End Dates special handling - They are set only when it is null regardless of the flag.
 						   Since these are system dates and would be different everytime on the incoming party data*/
							if(importedContactDTO.getContactEffDate()!= null && existingContactDTO.getContactEffDate() == null){
								existingContactDTO.setContactEffDate(importedContactDTO.getContactEffDate());
								updateRequiredFlag = true;
							}
							if(importedContactDTO.getContactEndDate() != null && existingContactDTO.getContactEndDate() == null){
								existingContactDTO.setContactEndDate(importedContactDTO.getContactEndDate());
								updateRequiredFlag = true;
							}
						}
						
						Collection<PhoneDTO> tempPhoneDTOs = new ArrayList<PhoneDTO>();
						for(PhoneDTO importPhoneDTO : importedContactDTO.getContactPhones()){
							boolean phoneFlag = true;
							for (PhoneDTO exportPhoneDTO : existingContactDTO.getContactPhones()){
								if(importPhoneDTO.getPhoneTypeCode().equals(exportPhoneDTO.getPhoneTypeCode())){
									phoneFlag = false;
									
									if(mergeAdditionalPartyInfo) {
										if(importPhoneDTO.getPhoneNumber() != null
												&& exportPhoneDTO.getPhoneNumber() == null) {
											exportPhoneDTO.setPhoneNumber(importPhoneDTO.getPhoneNumber());
											updateRequiredFlag = true;
										}
										if(importPhoneDTO.getPhoneNumberExtension() != null 
												&& exportPhoneDTO.getPhoneNumberExtension() == null) {
											exportPhoneDTO.setPhoneNumberExtension(importPhoneDTO.getPhoneNumberExtension());
											updateRequiredFlag = true;
										}
									} else {
										if(importPhoneDTO.getPhoneNumber() != null
												&& (exportPhoneDTO.getPhoneNumber() == null 
													|| !exportPhoneDTO.getPhoneNumber().equals(importPhoneDTO.getPhoneNumber()))){
											exportPhoneDTO.setPhoneNumber(importPhoneDTO.getPhoneNumber());
											updateRequiredFlag = true;
										}
										
										if(importPhoneDTO.getPhoneNumberExtension() != null
												&& (exportPhoneDTO.getPhoneNumberExtension() == null 
													|| !exportPhoneDTO.getPhoneNumberExtension().equals(importPhoneDTO.getPhoneNumberExtension()))){
											exportPhoneDTO.setPhoneNumberExtension(importPhoneDTO.getPhoneNumberExtension());
											updateRequiredFlag = true;
										}
									}
									exportPhoneDTO.setPrimary(importPhoneDTO.isPrimary());
								}
							}
							if(phoneFlag){
								tempPhoneDTOs.add(importPhoneDTO);
								updateRequiredFlag = true;
							}
						}
						if(!tempPhoneDTOs.isEmpty()){
							existingContactDTO.getContactPhones().addAll(tempPhoneDTOs);
						}
					}
				}
				if(flag){
					tempContactDTOs.add(importedContactDTO);
					updateRequiredFlag = true;
				}
			}
			if(!tempContactDTOs.isEmpty()){
				if(null != existingParty.getContacts() && !existingParty.getContacts().isEmpty()){
					existingParty.getContacts().addAll(tempContactDTOs);
				}else{
					Collection<ContactDTO> contactDTOs = new ArrayList<ContactDTO>();
					contactDTOs.addAll(tempContactDTOs);
					existingParty.setContacts(contactDTOs);
				}
			}
		}
		logger.info("mergeContacts update flag status : {}", updateRequiredFlag);
	}

	/**
	 * Merge Identifier
	 * @param importedParty
	 * @param existingParty
	 */
    public void mergeIdentifiers(PartyDTO importedParty, PartyDTO existingParty){
    	if(null != importedParty.getIdentifiers() && !importedParty.getIdentifiers().isEmpty()){
	        Collection<IdentifierDTO> tempIdentifiersList = new ArrayList<IdentifierDTO>();
	        for(IdentifierDTO importedIdentifierDTO:importedParty.getIdentifiers()){
	        	boolean flag = true;
	            for(IdentifierDTO existingIdentifierDTO:existingParty.getIdentifiers()) {
	            	if(null != importedIdentifierDTO.getIdentifierTypeCode() && null != existingIdentifierDTO.getIdentifierTypeCode() &&
	            			importedIdentifierDTO.getIdentifierTypeCode().equals(existingIdentifierDTO.getIdentifierTypeCode())){
	                        flag = false;

	                        if(mergeAdditionalPartyInfo) {
	                        	if(null != importedIdentifierDTO.getIdentifierNumber()
	                        			&& existingIdentifierDTO.getIdentifierNumber() == null) {
	                        		existingIdentifierDTO.setIdentifierNumber(importedIdentifierDTO.getIdentifierNumber());
		                        	updateRequiredFlag = true;
	                        	}
	                        	if(null != importedIdentifierDTO.getIdentifierCountryCode()
	                        			&& existingIdentifierDTO.getIdentifierCountryCode() == null) {
	                        		existingIdentifierDTO.setIdentifierCountryCode(importedIdentifierDTO.getIdentifierCountryCode());
		                        	updateRequiredFlag = true;
	                        	}
	                        	if(null != importedIdentifierDTO.getLicenseTypeCode()
	                        			&& existingIdentifierDTO.getLicenseTypeCode() == null){
	                        		existingIdentifierDTO.setLicenseTypeCode(importedIdentifierDTO.getLicenseTypeCode());
	                        		updateRequiredFlag = true;
	                        	}
	                        	if(null != importedIdentifierDTO.getJurTypeId()
	                        			&& existingIdentifierDTO.getJurTypeId() == null) {
	                        		existingIdentifierDTO.setJurTypeId(importedIdentifierDTO.getJurTypeId());
		                        	updateRequiredFlag = true;
	                        	}
	                        	if(null != importedIdentifierDTO.getRoleTypeCode()
	                        			&& existingIdentifierDTO.getRoleTypeCode() == null){
	                        		existingIdentifierDTO.setRoleTypeCode(importedIdentifierDTO.getRoleTypeCode());
		                        	updateRequiredFlag = true;
	                        	}
	                        	if(null != importedIdentifierDTO.getExternalSourceText()
	                        			&& existingIdentifierDTO.getExternalSourceText() == null) {
	                        		existingIdentifierDTO.setExternalSourceText(importedIdentifierDTO.getExternalSourceText());
		                        	updateRequiredFlag = true;
	                        	}
	                        } else {
	                        	if(null != importedIdentifierDTO.getIdentifierNumber()
	                        			&& (existingIdentifierDTO.getIdentifierNumber() == null
	                        			|| !existingIdentifierDTO.getIdentifierNumber().equals(importedIdentifierDTO.getIdentifierNumber()))){
	                        		existingIdentifierDTO.setIdentifierNumber(importedIdentifierDTO.getIdentifierNumber());
		                        	updateRequiredFlag = true;
		                          }
		                          if(null != importedIdentifierDTO.getIdentifierCountryCode()
		                        		  && (existingIdentifierDTO.getIdentifierCountryCode() == null 
		                        			  || !existingIdentifierDTO.getIdentifierCountryCode().equals(importedIdentifierDTO.getIdentifierCountryCode()))) {
		                        	  existingIdentifierDTO.setIdentifierCountryCode(importedIdentifierDTO.getIdentifierCountryCode());
		                        	  updateRequiredFlag = true;
		                          }
		                          if(null != importedIdentifierDTO.getLicenseTypeCode()
		                        		  && (existingIdentifierDTO.getLicenseTypeCode() == null 
		                        			  || !existingIdentifierDTO.getLicenseTypeCode().equals(importedIdentifierDTO.getLicenseTypeCode()))){
		                        	  existingIdentifierDTO.setLicenseTypeCode(importedIdentifierDTO.getLicenseTypeCode());
		                        	  updateRequiredFlag = true;
		                          }
		                          
		                          if(null != importedIdentifierDTO.getJurTypeId()
		                        		  && (existingIdentifierDTO.getJurTypeId() == null 
		                        			  || existingIdentifierDTO.getJurTypeId().compareTo(importedIdentifierDTO.getJurTypeId()) != 0)) {
		                        	  existingIdentifierDTO.setJurTypeId(importedIdentifierDTO.getJurTypeId());
		                        	  updateRequiredFlag = true;
		                          }
		                          if(null != importedIdentifierDTO.getRoleTypeCode()
		                        		  && (existingIdentifierDTO.getRoleTypeCode() == null 
		                        			  || !existingIdentifierDTO.getRoleTypeCode().equals(importedIdentifierDTO.getRoleTypeCode()))){
		                        	  existingIdentifierDTO.setRoleTypeCode(importedIdentifierDTO.getRoleTypeCode());
		                        	  updateRequiredFlag = true;
		                          }
		                          if(null != importedIdentifierDTO.getExternalSourceText()
		                        		  && (existingIdentifierDTO.getExternalSourceText() == null 
		                        			  || !existingIdentifierDTO.getExternalSourceText().equals(importedIdentifierDTO.getExternalSourceText()))){
		                        	  existingIdentifierDTO.setExternalSourceText(importedIdentifierDTO.getExternalSourceText());
		                        	  updateRequiredFlag = true;
		                          }           
	                        }
	            	}
	            }
	            if(flag){
	            	tempIdentifiersList.add(importedIdentifierDTO);
	            	updateRequiredFlag = true;
	            }
	        }
		    if(!tempIdentifiersList.isEmpty()){
		        if(null != existingParty.getIdentifiers() && !existingParty.getIdentifiers().isEmpty()){
		              existingParty.getIdentifiers().addAll(tempIdentifiersList);
		        }else{
		              Collection<IdentifierDTO> newIdentifiersList = new ArrayList<IdentifierDTO>();
		              newIdentifiersList.addAll(tempIdentifiersList);
		              existingParty.setIdentifiers(newIdentifiersList);
		        }
		    }
	    }
		logger.info("mergeIdentifiers update flag status : {}", updateRequiredFlag);
       }
    
    /**
	 * Merge Child Support
	 * @param importedParty
	 * @param existingParty
	 
       private void mergeChildSupports(PartyDTO importedParty, PartyDTO existingParty){
         if(null != importedParty.getChildSupports() && !importedParty.getChildSupports().isEmpty()){
               for(ChildSupportDTO existingCHildSupportDTO: existingParty.getChildSupports()){
                     existingCHildSupportDTO.setEndDateTime(DateUtility.getSystemDateOnly());
               }
         }
              existingParty.setChildSupports(importedParty.getChildSupports());
       }
      */
    
    /**
   	 * Merge Party Role Collection
   	 * @param importedParty
   	 * @param existingParty
   	 */
       public void mergePartyRoleCollection(PartyDTO importedParty, PartyDTO existingParty){
    	   if(importedParty.getPartyRoleCollection()!=null && !importedParty.getPartyRoleCollection().isEmpty()) {
    		   Collection<PartyRoleDTO> tempPartyRoleDTOs = new ArrayList<PartyRoleDTO>();
    		   for(PartyRoleDTO importedPartyRoleDTO:importedParty.getPartyRoleCollection()){
    			   boolean flag = true;
    			   for(PartyRoleDTO existingPartyRoleDTO:existingParty.getPartyRoleCollection()){
    				   if(null != importedPartyRoleDTO.getPartyRoleTypeCd() && null != existingPartyRoleDTO.getPartyRoleTypeCd() 
    						   && importedPartyRoleDTO.getPartyRoleTypeCd().equals(existingPartyRoleDTO.getPartyRoleTypeCd())) {
    						   flag = false;
    	    				   if(importedPartyRoleDTO instanceof PartyRoleVendorDTO && existingPartyRoleDTO instanceof PartyRoleVendorDTO){
    	    					   mergePartyRoleVendor((PartyRoleVendorDTO)importedPartyRoleDTO,(PartyRoleVendorDTO)existingPartyRoleDTO);
    	    				   }else if(importedPartyRoleDTO instanceof PartyRoleDriversDTO && existingPartyRoleDTO instanceof PartyRoleDriversDTO){
    	    					   mergePartyRoleDriver((PartyRoleDriversDTO)importedPartyRoleDTO,(PartyRoleDriversDTO)existingPartyRoleDTO);
    	    				   }else if(importedPartyRoleDTO instanceof PartyRoleAgencyDTO && existingPartyRoleDTO instanceof PartyRoleAgencyDTO){
    	    					   mergePartyRoleAgency((PartyRoleAgencyDTO)importedPartyRoleDTO,(PartyRoleAgencyDTO)existingPartyRoleDTO);
    	    				   }else if(importedPartyRoleDTO instanceof PartyRoleRegulatorDTO && existingPartyRoleDTO instanceof PartyRoleRegulatorDTO){
    	    					   mergePartyRoleRegulator((PartyRoleRegulatorDTO)importedPartyRoleDTO,(PartyRoleRegulatorDTO)existingPartyRoleDTO);
    	    				   }else if(importedPartyRoleDTO instanceof PartyRoleFinancialInstitutionDTO && existingPartyRoleDTO instanceof PartyRoleFinancialInstitutionDTO){
    	    					   mergePartyRoleFinancialInstitution((PartyRoleFinancialInstitutionDTO)importedPartyRoleDTO,(PartyRoleFinancialInstitutionDTO)existingPartyRoleDTO);
    	    				   }    						   
    						   /*Effective - End Dates special handling - They are set only when it is null regardless of the flag.
    						   Since these are system dates and would be different everytime on the incoming party data*/
    						   if(null != importedPartyRoleDTO.getEffectiveDateTime()
    								   && existingPartyRoleDTO.getEffectiveDateTime() == null){
    							   existingPartyRoleDTO.setEffectiveDateTime(importedPartyRoleDTO.getEffectiveDateTime());
    							   updateRequiredFlag = true;
    						   }
    						   if(null != importedPartyRoleDTO.getEndDateTime()
    								   && existingPartyRoleDTO.getEndDateTime() == null){
    							   existingPartyRoleDTO.setEndDateTime(importedPartyRoleDTO.getEndDateTime());
    							   updateRequiredFlag = true;
    						   }
    					   
    				   }
    			   }
				   if(flag){
					   tempPartyRoleDTOs.add(importedPartyRoleDTO);
					   updateRequiredFlag = true;
				   }
    		   }
    		   if(!tempPartyRoleDTOs.isEmpty()){
    				   if(null != existingParty.getPartyRoleCollection() && !existingParty.getPartyRoleCollection().isEmpty()){
    					   existingParty.getPartyRoleCollection().addAll(tempPartyRoleDTOs);
    				   } else {
    					   Collection<PartyRoleDTO> newPartyRoleDTOs = new ArrayList<PartyRoleDTO>();
    					   newPartyRoleDTOs.addAll(tempPartyRoleDTOs);
    					   existingParty.setPartyRoleCollection(newPartyRoleDTOs);
    				   }
    			   }
    		   
    		   logger.info("mergePartyRoleCollection update flag status : {}", updateRequiredFlag);
    	   }
       }
       
       private void mergePartyRoleRegulator(PartyRoleRegulatorDTO importedPartyRoleDTO,PartyRoleRegulatorDTO existingPartyRoleDTO){
    	   
    	   if(importedPartyRoleDTO.getRegulatorTypeCode() != null && existingPartyRoleDTO.getRegulatorTypeCode() != null
    			&& importedPartyRoleDTO.getRegulatorTypeCode().equals(existingPartyRoleDTO.getRegulatorTypeCode())){
        	   //Add new Jurisdiction
        	   Collection<PartyRoleRegulatorJurisdictionDTO> tempjurisdictionDTOs = new ArrayList<PartyRoleRegulatorJurisdictionDTO>();
        	   for(PartyRoleRegulatorJurisdictionDTO importedjurisdictionDTO: importedPartyRoleDTO.getRegulatorJurisdiction()){
        		   boolean flag = true;
        		   for(PartyRoleRegulatorJurisdictionDTO existingjusrisdictionDTO: existingPartyRoleDTO.getRegulatorJurisdiction()){
        			   if(importedjurisdictionDTO.getJurisdictionCodeId().compareTo(existingjusrisdictionDTO.getJurisdictionCodeId()) == 0){
        				   flag = false;
        			   }
        		   }
        		   if(flag){
        			   tempjurisdictionDTOs.add(importedjurisdictionDTO);
        			   updateRequiredFlag = true;
        		   }
        	   }
        	   
    		   if(!tempjurisdictionDTOs.isEmpty()){
    			   if(null != existingPartyRoleDTO.getRegulatorJurisdiction() && !existingPartyRoleDTO.getRegulatorJurisdiction().isEmpty()){
    				   existingPartyRoleDTO.getRegulatorJurisdiction().addAll(tempjurisdictionDTOs);
    			   } else {
    				   Collection<PartyRoleRegulatorJurisdictionDTO> newJurisdictionDTOs = new ArrayList<PartyRoleRegulatorJurisdictionDTO>();
    				   newJurisdictionDTOs.addAll(tempjurisdictionDTOs);
    				   existingPartyRoleDTO.setRegulatorJurisdiction(newJurisdictionDTOs);
    			   }   			   
    		   }
    	   }else if(importedPartyRoleDTO.getRegulatorTypeCode() != null && existingPartyRoleDTO.getRegulatorTypeCode() == null){
    		   existingPartyRoleDTO = importedPartyRoleDTO;
    		   updateRequiredFlag = true;
    	   }
    		   
        logger.info("mergePartyRoleRegulator update flag status : {}", updateRequiredFlag); 
    	   
       }
       
       private void mergePartyRoleAgency(PartyRoleAgencyDTO importedPartyRoleDTO, PartyRoleAgencyDTO existingPartyRoleDTO){
    	   
    	   if(importedPartyRoleDTO.getMgaIndicator() != existingPartyRoleDTO.getMgaIndicator()){
    		   existingPartyRoleDTO.setMgaIndicator(importedPartyRoleDTO.getMgaIndicator());
    		   updateRequiredFlag = true;
    	   }
    	   //Merge Licenses
    	   if(importedPartyRoleDTO.getLicenses() != null && !importedPartyRoleDTO.getLicenses().isEmpty()){
    		   Collection<PartyRoleAgencyLicenseDTO> templicenseDTOs = new ArrayList<PartyRoleAgencyLicenseDTO>();
    		   for(PartyRoleAgencyLicenseDTO importedlicenseDTO:importedPartyRoleDTO.getLicenses()){
    			   boolean flag = true;
    			   for(PartyRoleAgencyLicenseDTO existinglicenseDTO:existingPartyRoleDTO.getLicenses()){
    				   if(importedlicenseDTO.getAgentResidencyTypeCode() != null && existinglicenseDTO.getAgentResidencyTypeCode() != null
    					  && importedlicenseDTO.getAgentResidencyTypeCode().equals(existinglicenseDTO.getAgentResidencyTypeCode())){
    					   flag = false;
    					   mergeAgencyLicense(importedlicenseDTO, existinglicenseDTO);
    				   }
    			   }
    			  if(flag){
    				  templicenseDTOs.add(importedlicenseDTO);
    				  updateRequiredFlag = true;
    			  }
    		   }
    		   if(!templicenseDTOs.isEmpty()){
				   if(null != existingPartyRoleDTO.getLicenses() && !existingPartyRoleDTO.getLicenses().isEmpty()){
					   existingPartyRoleDTO.getLicenses().addAll(templicenseDTOs);
				   } else {
					   Collection<PartyRoleAgencyLicenseDTO> newPartyLicenseDTOs = new ArrayList<PartyRoleAgencyLicenseDTO>();
					   newPartyLicenseDTOs.addAll(templicenseDTOs);
					   existingPartyRoleDTO.setLicenses(newPartyLicenseDTOs);
				   }   			   
    		   }
    		}
    	   
    	   logger.info("mergePartyRoleAgency update flag status : {}", updateRequiredFlag);    	   
    	   
        }
       
      private void mergeAgencyLicense(PartyRoleAgencyLicenseDTO importedlicenseDTO, PartyRoleAgencyLicenseDTO existinglicenseDTO){
    	   
    	   if(importedlicenseDTO.getAgencyLicenseNumber() != null 
    			   &&(existinglicenseDTO.getAgencyLicenseNumber() == null || (!mergeAdditionalPartyInfo && 
    					   !existinglicenseDTO.getAgencyLicenseNumber().equals(importedlicenseDTO.getAgencyLicenseNumber())))
    			   ){
    		   existinglicenseDTO.setAgencyLicenseNumber(importedlicenseDTO.getAgencyLicenseNumber());
    		   updateRequiredFlag = true;
    	   }

    	   if(importedlicenseDTO.isCountersignerAuthIndicator() != existinglicenseDTO.isCountersignerAuthIndicator()){
    		   existinglicenseDTO.setCountersignerAuthIndicator(importedlicenseDTO.isCountersignerAuthIndicator());
    		   updateRequiredFlag = true;
    	   }
    	   
    	   if(importedlicenseDTO.getJurisdictionCodeId() != null 
    			   &&(existinglicenseDTO.getJurisdictionCodeId() == null || (!mergeAdditionalPartyInfo && 
    					   existinglicenseDTO.getJurisdictionCodeId().compareTo(importedlicenseDTO.getJurisdictionCodeId()) != 0))
    			   ){
    		   existinglicenseDTO.setJurisdictionCodeId(importedlicenseDTO.getJurisdictionCodeId());
    		   updateRequiredFlag = true;
    	   }
    	   
		   if(null != importedlicenseDTO.getEffectiveDate()
				   && existinglicenseDTO.getEffectiveDate() == null){
			   existinglicenseDTO.setEffectiveDate(importedlicenseDTO.getEffectiveDate());
			   updateRequiredFlag = true;
		   }
		   
		   if(null != importedlicenseDTO.getExpiryDate()
				   && existinglicenseDTO.getExpiryDate() == null){
			   existinglicenseDTO.setExpiryDate(importedlicenseDTO.getExpiryDate());
			   updateRequiredFlag = true;
		   }
		   
    	   if(importedlicenseDTO.getExternalSourceId() != null 
    			   &&(existinglicenseDTO.getExternalSourceId() == null || (!mergeAdditionalPartyInfo && 
    					   !existinglicenseDTO.getExternalSourceId().equals(importedlicenseDTO.getExternalSourceId())))
    			   ){
    		   existinglicenseDTO.setExternalSourceId(importedlicenseDTO.getExternalSourceId());
    		   updateRequiredFlag = true;
    	   }
    	   //Add new License LOBs
    	   Collection<PartyRoleAgencyLicenseLOBDTO> templicenseDTOs = new ArrayList<PartyRoleAgencyLicenseLOBDTO>();
    	   for(PartyRoleAgencyLicenseLOBDTO importedlicenseLOBDTO: importedlicenseDTO.getLicenseLOBs()){
    		   boolean flag = true;
    		   for(PartyRoleAgencyLicenseLOBDTO existinglicenseLOBDTO: existinglicenseDTO.getLicenseLOBs()){
    			   if(importedlicenseLOBDTO.getLineOfBusinessCode().equals(existinglicenseLOBDTO.getLineOfBusinessCode())){
    				   flag = false;
    			   }
    		   }
    		   if(flag){
    			   templicenseDTOs.add(importedlicenseLOBDTO);
    			   updateRequiredFlag = true;
    		   }
    	   }
    	   
		   if(!templicenseDTOs.isEmpty()){
			   if(null != existinglicenseDTO.getLicenseLOBs() && !existinglicenseDTO.getLicenseLOBs().isEmpty()){
				   existinglicenseDTO.getLicenseLOBs().addAll(templicenseDTOs);
			   } else {
				   Collection<PartyRoleAgencyLicenseLOBDTO> newPartyLicenseDTOs = new ArrayList<PartyRoleAgencyLicenseLOBDTO>();
				   newPartyLicenseDTOs.addAll(templicenseDTOs);
				   existinglicenseDTO.setLicenseLOBs(newPartyLicenseDTOs);
			   }   			   
		   }
		   
    	   logger.info("mergeAgencyLicense update flag status : {}", updateRequiredFlag);   		   
    	   
       }       
       
       private void mergePartyRoleFinancialInstitution(PartyRoleFinancialInstitutionDTO importedPartyRoleDTO, PartyRoleFinancialInstitutionDTO existingPartyRoleDTO){
    	   
    	   if(importedPartyRoleDTO.getRoutingNumber() != null 
    			   &&(existingPartyRoleDTO.getRoutingNumber() == null || (!mergeAdditionalPartyInfo && 
    					   !existingPartyRoleDTO.getRoutingNumber().equals(importedPartyRoleDTO.getRoutingNumber())))
    			   ){
    		   existingPartyRoleDTO.setRoutingNumber(importedPartyRoleDTO.getRoutingNumber());
    		   updateRequiredFlag = true;
    	   }
    	   
    	   logger.info("mergePartyRoleFinancialInstitution update flag status : {}", updateRequiredFlag);   	    	   
       }
       
       private void mergePartyRoleDriver(PartyRoleDriversDTO importedPartyRoleDTO, PartyRoleDriversDTO existingPartyRoleDTO){
    	   //Profiles are not merged. It will be addressed at a later time.
    	   //Merge Licenses
    	   if(importedPartyRoleDTO.getLicenses() != null && !importedPartyRoleDTO.getLicenses().isEmpty()){
    		   Collection<PartyRoleDriverLicenseDTO> templicenseDTOs = new ArrayList<PartyRoleDriverLicenseDTO>();
    		   outerLoop:
    		   for(PartyRoleDriverLicenseDTO importedlicenseDTO:importedPartyRoleDTO.getLicenses()){
    			   boolean flag = true;
    			   for(PartyRoleDriverLicenseDTO existinglicenseDTO:existingPartyRoleDTO.getLicenses()){
    				   if(existinglicenseDTO.getExpirationDate() != null) continue;
    				   if(importedlicenseDTO.getDriverClassTypeCode() != null && existinglicenseDTO.getDriverClassTypeCode() != null
    					  && importedlicenseDTO.getDriverClassTypeCode().equals(existinglicenseDTO.getDriverClassTypeCode())){
    					   flag = false;
    					   mergeDriverLicense(importedlicenseDTO, existinglicenseDTO);
    				   }else if(importedlicenseDTO.getDriverClassTypeCode() == null && existinglicenseDTO.getDriverClassTypeCode() == null){
    					   //Just update the first License and bail out
    					   mergeDriverLicense(importedlicenseDTO, existinglicenseDTO);
    					   break outerLoop;
    				   }
    			   }
    			  if(flag){
    				  templicenseDTOs.add(importedlicenseDTO);
    				  updateRequiredFlag = true;
    			  }
    		   }
    		   if(!templicenseDTOs.isEmpty()){
				   if(null != existingPartyRoleDTO.getLicenses() && !existingPartyRoleDTO.getLicenses().isEmpty()){
					   existingPartyRoleDTO.getLicenses().addAll(templicenseDTOs);
				   } else {
					   Collection<PartyRoleDriverLicenseDTO> newPartyLicenseDTOs = new ArrayList<PartyRoleDriverLicenseDTO>();
					   newPartyLicenseDTOs.addAll(templicenseDTOs);
					   existingPartyRoleDTO.setLicenses(newPartyLicenseDTOs);
				   }   			   
    		   }
    		}
    	   //Merge Traffic Incidents
    	   if(importedPartyRoleDTO.getTrafficIncidentsColl() != null && !importedPartyRoleDTO.getTrafficIncidentsColl().isEmpty()){
    		   Collection<PartyRoleTrafficIncidentDTO> tempincidentDTOs = new ArrayList<PartyRoleTrafficIncidentDTO>();
    		   for(PartyRoleTrafficIncidentDTO importedincidentDTO:importedPartyRoleDTO.getTrafficIncidentsColl()){
    			   boolean flag = true;
    			   for(PartyRoleTrafficIncidentDTO existingincidentDTO:existingPartyRoleDTO.getTrafficIncidentsColl()){
    				   if(importedincidentDTO.getTrafficIncidentSourceCode() != null && existingincidentDTO.getTrafficIncidentSourceCode() != null
    					  && importedincidentDTO.getTrafficIncidentSourceCode().equals(existingincidentDTO.getTrafficIncidentSourceCode())
    					  &&importedincidentDTO.getTrafficIncidentCode() != null && existingincidentDTO.getTrafficIncidentCode() != null
    	    			  && importedincidentDTO.getTrafficIncidentCode().compareTo(existingincidentDTO.getTrafficIncidentCode()) == 0
    	    			  &&importedincidentDTO.getPartyRoleId() != null && existingincidentDTO.getPartyRoleId() != null
    	    			  && importedincidentDTO.getPartyRoleId().compareTo(existingincidentDTO.getPartyRoleId()) == 0    						   
    						   ){
    					   flag = false;
    			    	   if(importedincidentDTO.getPenalityPointCode() != null 
    			    			   &&(existingincidentDTO.getPenalityPointCode() == null || (!mergeAdditionalPartyInfo && 
    			    					   !existingincidentDTO.getPenalityPointCode().equals(importedincidentDTO.getPenalityPointCode())))
    			    			   ){
    			    		   existingincidentDTO.setPenalityPointCode(importedincidentDTO.getPenalityPointCode());
    			    		   updateRequiredFlag = true;
    			    	   }
    			    	   if(importedincidentDTO.getAccidentViolationTypeName() != null 
    			    			   &&(existingincidentDTO.getAccidentViolationTypeName() == null || (!mergeAdditionalPartyInfo && 
    			    					   !existingincidentDTO.getAccidentViolationTypeName().equals(importedincidentDTO.getAccidentViolationTypeName())))
    			    			   ){
    			    		   existingincidentDTO.setAccidentViolationTypeName(importedincidentDTO.getAccidentViolationTypeName());
    			    		   updateRequiredFlag = true;
       			    	   }
    			    	   if(importedincidentDTO.getInjuryOrDeathInd() != existingincidentDTO.getInjuryOrDeathInd()){
    			    		   existingincidentDTO.setInjuryOrDeathInd(importedincidentDTO.getInjuryOrDeathInd());
    			    		   updateRequiredFlag = true;
    			    	   }
    			    	   if(importedincidentDTO.getFaultInd() != existingincidentDTO.getFaultInd()){
    			    		   existingincidentDTO.setFaultInd(importedincidentDTO.getFaultInd());
    			    		   updateRequiredFlag = true;
    			    	   }
    			    	   if(importedincidentDTO.getTrafficIncidentConvictionDate() != null 
    			    			   && existingincidentDTO.getTrafficIncidentConvictionDate() == null){
    			    		   existingincidentDTO.setTrafficIncidentConvictionDate(importedincidentDTO.getTrafficIncidentConvictionDate());
    			    		   updateRequiredFlag = true;
    			    	   }
    			    	   if(importedincidentDTO.getTrafficIncidentDate() != null 
    			    			   && existingincidentDTO.getTrafficIncidentDate() == null){
    			    		   existingincidentDTO.setTrafficIncidentDate(importedincidentDTO.getTrafficIncidentDate());
    			    		   updateRequiredFlag = true;
    			    	   }
    			    	   if(importedincidentDTO.getDamageAmount() != null 
    			    			   &&(existingincidentDTO.getDamageAmount() == null || (!mergeAdditionalPartyInfo && 
    			    					   existingincidentDTO.getDamageAmount().compareTo(importedincidentDTO.getDamageAmount()) != 0))
    			    			   ){
    			    		   existingincidentDTO.setDamageAmount(importedincidentDTO.getDamageAmount());
    			    		   updateRequiredFlag = true;
    			    	   }    			    	   
    				   }
    			   }
    			  if(flag){
    				  tempincidentDTOs.add(importedincidentDTO);
    				  updateRequiredFlag = true;
    			  }
    		   }
    		   if(!tempincidentDTOs.isEmpty()){
				   if(null != existingPartyRoleDTO.getTrafficIncidentsColl() && !existingPartyRoleDTO.getTrafficIncidentsColl().isEmpty()){
					   existingPartyRoleDTO.getTrafficIncidentsColl().addAll(tempincidentDTOs);
				   } else {
					   Collection<PartyRoleTrafficIncidentDTO> newPartyIncidentDTOs = new ArrayList<PartyRoleTrafficIncidentDTO>();
					   newPartyIncidentDTOs.addAll(newPartyIncidentDTOs);
					   existingPartyRoleDTO.setTrafficIncidentsColl(newPartyIncidentDTOs);
				   }   			   
    		   }
    	  }
    	   
    	   logger.info("mergePartyRoleDriver update flag status : {}", updateRequiredFlag);      	   
    	   
    }
       
       
       private void mergeDriverLicense(PartyRoleDriverLicenseDTO importedlicenseDTO, PartyRoleDriverLicenseDTO existinglicenseDTO){
    	   
    	   if(importedlicenseDTO.getCountryCode() != null 
    			   &&(existinglicenseDTO.getCountryCode() == null || (!mergeAdditionalPartyInfo && 
    					   !existinglicenseDTO.getCountryCode().equals(importedlicenseDTO.getCountryCode())))
    			   ){
    		   existinglicenseDTO.setCountryCode(importedlicenseDTO.getCountryCode());
    		   updateRequiredFlag = true;
    	   }
    	   if(importedlicenseDTO.getLicenseNumber() != null 
    			   &&(existinglicenseDTO.getLicenseNumber() == null || (!mergeAdditionalPartyInfo && 
    					   !existinglicenseDTO.getLicenseNumber().equals(importedlicenseDTO.getLicenseNumber())))
    			   ){
    		   existinglicenseDTO.setLicenseNumber(importedlicenseDTO.getLicenseNumber());
    		   updateRequiredFlag = true;
    	   }
    	   if(importedlicenseDTO.getLicenseSuspendedIndicator() != existinglicenseDTO.getLicenseSuspendedIndicator()){
    		   existinglicenseDTO.setLicenseSuspendedIndicator(importedlicenseDTO.getLicenseSuspendedIndicator());
    		   updateRequiredFlag = true;
    	   }
		   if(null != importedlicenseDTO.getOriginalIssueDate()
				   && existinglicenseDTO.getOriginalIssueDate() == null){
			   existinglicenseDTO.setOriginalIssueDate(importedlicenseDTO.getOriginalIssueDate());
			   updateRequiredFlag = true;
		   }
		   if(null != importedlicenseDTO.getExpirationDate()
				   && existinglicenseDTO.getExpirationDate() == null){
			   existinglicenseDTO.setExpirationDate(importedlicenseDTO.getExpirationDate());
			   updateRequiredFlag = true;
		   }
		   
    	   if(importedlicenseDTO.getExternalSourceId() != null 
    			   &&(existinglicenseDTO.getExternalSourceId() == null || (!mergeAdditionalPartyInfo && 
    					   !existinglicenseDTO.getExternalSourceId().equals(importedlicenseDTO.getExternalSourceId())))
    			   ){
    		   existinglicenseDTO.setExternalSourceId(importedlicenseDTO.getExternalSourceId());
    		   updateRequiredFlag = true;
    	   }
    	   
    	   if(importedlicenseDTO.getJurisdictionId() != null 
    			   &&(existinglicenseDTO.getJurisdictionId() == null || (!mergeAdditionalPartyInfo && 
    					   existinglicenseDTO.getJurisdictionId().compareTo(importedlicenseDTO.getJurisdictionId()) != 0))
    			   ){
    		   existinglicenseDTO.setJurisdictionId(importedlicenseDTO.getJurisdictionId());
    		   updateRequiredFlag = true;
    	   }
    	   
    	   logger.info("mergeDriverLicense update flag status : {}", updateRequiredFlag);         	   
       }
  
       
       /**
        * Merge Party Role Vendor
        * @param importedPartyRoleVendorDTO
        * @param existingPartyRoleVendorDTO
        */
       private void mergePartyRoleVendor(PartyRoleVendorDTO importedPartyRoleVendorDTO,PartyRoleVendorDTO existingPartyRoleVendorDTO){
    	   if(null != importedPartyRoleVendorDTO.getAccountsPayableVendorTypeCode() && null != existingPartyRoleVendorDTO.getAccountsPayableVendorTypeCode() &&
    			   importedPartyRoleVendorDTO.getAccountsPayableVendorTypeCode().equals(existingPartyRoleVendorDTO.getAccountsPayableVendorTypeCode())){
    		   
    		   if(mergeAdditionalPartyInfo) {
        		   if(null != importedPartyRoleVendorDTO.getDaysHoursOfService() 
        				   && existingPartyRoleVendorDTO.getDaysHoursOfService() == null) {
        			   existingPartyRoleVendorDTO.setDaysHoursOfService(importedPartyRoleVendorDTO.getDaysHoursOfService());
        			   updateRequiredFlag = true;
        		   }
        		   if(existingPartyRoleVendorDTO.getRecommendedVendorIndicator() != importedPartyRoleVendorDTO.getRecommendedVendorIndicator()){
        			   existingPartyRoleVendorDTO.setRecommendedVendorIndicator(importedPartyRoleVendorDTO.getRecommendedVendorIndicator());
        			   updateRequiredFlag = true;
        		   }
        		   
        		   if(null != importedPartyRoleVendorDTO.getAccountsPayableVendorId()
        				   && existingPartyRoleVendorDTO.getAccountsPayableVendorId() == null){
        			   existingPartyRoleVendorDTO.setAccountsPayableVendorId(importedPartyRoleVendorDTO.getAccountsPayableVendorId());
        			   updateRequiredFlag = true;
        		   }

        		   if(null != importedPartyRoleVendorDTO.getVendorRatingCode()
        				   && existingPartyRoleVendorDTO.getVendorRatingCode() == null){
        			   existingPartyRoleVendorDTO.setVendorRatingCode(importedPartyRoleVendorDTO.getVendorRatingCode());
        			   updateRequiredFlag = true;
        		   }
        		   if(existingPartyRoleVendorDTO.getVendorSubjectToSurchargeInd() != importedPartyRoleVendorDTO.getVendorSubjectToSurchargeInd()){
        			   existingPartyRoleVendorDTO.setVendorSubjectToSurchargeInd(importedPartyRoleVendorDTO.getVendorSubjectToSurchargeInd());
        			   updateRequiredFlag = true;
        		   }
        		   if(existingPartyRoleVendorDTO.getVendorReceipentOfSurchargeInd() != importedPartyRoleVendorDTO.getVendorReceipentOfSurchargeInd()){
        			   existingPartyRoleVendorDTO.setVendorReceipentOfSurchargeInd(importedPartyRoleVendorDTO.getVendorReceipentOfSurchargeInd());
        			   updateRequiredFlag = true;
        		   }
    			   
    		   } else {
        		   if(null != importedPartyRoleVendorDTO.getDaysHoursOfService()
        				   && (existingPartyRoleVendorDTO.getDaysHoursOfService() == null 
        					   || !existingPartyRoleVendorDTO.getDaysHoursOfService().equals(importedPartyRoleVendorDTO.getDaysHoursOfService()))){
        				   existingPartyRoleVendorDTO.setDaysHoursOfService(importedPartyRoleVendorDTO.getDaysHoursOfService());
        				   updateRequiredFlag = true;
        		   }
        		   if(existingPartyRoleVendorDTO.getRecommendedVendorIndicator() != importedPartyRoleVendorDTO.getRecommendedVendorIndicator()){
        			   existingPartyRoleVendorDTO.setRecommendedVendorIndicator(importedPartyRoleVendorDTO.getRecommendedVendorIndicator());
        			   updateRequiredFlag = true;
        		   }
        		   
        		   if(null != importedPartyRoleVendorDTO.getAccountsPayableVendorId() 
        				   && (existingPartyRoleVendorDTO.getAccountsPayableVendorId() == null 
        					   || !existingPartyRoleVendorDTO.getAccountsPayableVendorId().equals(importedPartyRoleVendorDTO.getAccountsPayableVendorId()))){
        				   existingPartyRoleVendorDTO.setAccountsPayableVendorId(importedPartyRoleVendorDTO.getAccountsPayableVendorId());
        				   updateRequiredFlag = true;
        		   }
        		   if(null != importedPartyRoleVendorDTO.getVendorRatingCode() 
        				   && (existingPartyRoleVendorDTO.getVendorRatingCode() == null 
        					   || !existingPartyRoleVendorDTO.getVendorRatingCode().equals(importedPartyRoleVendorDTO.getVendorRatingCode()))){
        				   existingPartyRoleVendorDTO.setVendorRatingCode(importedPartyRoleVendorDTO.getVendorRatingCode());
        				   updateRequiredFlag = true;
        		   }

        		   if(existingPartyRoleVendorDTO.getVendorSubjectToSurchargeInd() != importedPartyRoleVendorDTO.getVendorSubjectToSurchargeInd()){
        			   existingPartyRoleVendorDTO.setVendorSubjectToSurchargeInd(importedPartyRoleVendorDTO.getVendorSubjectToSurchargeInd());
        			   updateRequiredFlag = true;
        		   }
        		   if(existingPartyRoleVendorDTO.getVendorReceipentOfSurchargeInd() != importedPartyRoleVendorDTO.getVendorReceipentOfSurchargeInd()){
        			   existingPartyRoleVendorDTO.setVendorReceipentOfSurchargeInd(importedPartyRoleVendorDTO.getVendorReceipentOfSurchargeInd());
        			   updateRequiredFlag = true;
        		   }
    		   }
    		   if(null != importedPartyRoleVendorDTO.getVendorFees() && !importedPartyRoleVendorDTO.getVendorFees().isEmpty()){
    			   if(null != existingPartyRoleVendorDTO.getVendorFees()){
    				   mergeVendorFees(importedPartyRoleVendorDTO.getVendorFees(),existingPartyRoleVendorDTO.getVendorFees());
    			   }else{
    				   Collection<PartyRoleVendorFeesDTO> partyRoleVendorFeesDTOs = new ArrayList<PartyRoleVendorFeesDTO>();
    				   partyRoleVendorFeesDTOs.addAll(importedPartyRoleVendorDTO.getVendorFees());
    				   existingPartyRoleVendorDTO.setVendorFees(partyRoleVendorFeesDTOs);
    				   updateRequiredFlag = true;
    			   }
    		   }
    		   if(null != importedPartyRoleVendorDTO.getVendorSurcharges() && !importedPartyRoleVendorDTO.getVendorSurcharges().isEmpty()){
    			   if(null != existingPartyRoleVendorDTO.getVendorSurcharges()){
    				   mergeVendorSurcharges(importedPartyRoleVendorDTO.getVendorSurcharges(),existingPartyRoleVendorDTO.getVendorSurcharges());
    			   }else{
    				   Collection<PartyRoleVendorAssessmentSurchargeDTO> partyRoleVendorSurchargeDTOs = new ArrayList<PartyRoleVendorAssessmentSurchargeDTO>();
    				   partyRoleVendorSurchargeDTOs.addAll(importedPartyRoleVendorDTO.getVendorSurcharges());
    				   existingPartyRoleVendorDTO.setVendorSurcharges(partyRoleVendorSurchargeDTOs);
    				   updateRequiredFlag = true;
    			   }
    		   }
    		   if(null != importedPartyRoleVendorDTO.getVendorTypes() && !importedPartyRoleVendorDTO.getVendorTypes().isEmpty()){
    			   if(null != existingPartyRoleVendorDTO.getVendorTypes()){
    				   mergeVendorTypes(importedPartyRoleVendorDTO.getVendorTypes(),existingPartyRoleVendorDTO.getVendorTypes());
    			   }else{
    				   Collection<PartyRoleVendorTypeDTO> partyRoleVendorTypeDTOs = new ArrayList<PartyRoleVendorTypeDTO>();
    				   partyRoleVendorTypeDTOs.addAll(importedPartyRoleVendorDTO.getVendorTypes());
    				   existingPartyRoleVendorDTO.setVendorTypes(partyRoleVendorTypeDTOs);
    				   updateRequiredFlag = true;
    			   }
    		   }
    	   }
    		logger.info("mergePartyRoleVendor update flag status : {}", updateRequiredFlag);
       } 
       /**
        * Merge Vendor Fees
        * @param importedPartyRoleVendorFeesDTOs
        * @param existingPartyRoleVendorFeesDTOs
        */
       private void mergeVendorFees(Collection<PartyRoleVendorFeesDTO> importedPartyRoleVendorFeesDTOs, Collection<PartyRoleVendorFeesDTO> existingPartyRoleVendorFeesDTOs){
    	   Collection<PartyRoleVendorFeesDTO> tempPartyRoleVendorFeesDTOs = new ArrayList<PartyRoleVendorFeesDTO>();
    	   if(importedPartyRoleVendorFeesDTOs != null && !importedPartyRoleVendorFeesDTOs.isEmpty()){
    	   for(PartyRoleVendorFeesDTO importedPartyRoleVendorFeesDTO:importedPartyRoleVendorFeesDTOs){
    		   boolean flag = true;
    		   for(PartyRoleVendorFeesDTO existingPartyRoleVendorFeesDTO:existingPartyRoleVendorFeesDTOs){
        		   if(null != importedPartyRoleVendorFeesDTO.getVendorFeeTypeCode() && null != existingPartyRoleVendorFeesDTO.getVendorFeeTypeCode() &&
        				   importedPartyRoleVendorFeesDTO.getVendorFeeTypeCode().equals(existingPartyRoleVendorFeesDTO.getVendorFeeTypeCode())){
        			   flag = false;
        			   if(mergeAdditionalPartyInfo) {
        				   if(null != importedPartyRoleVendorFeesDTO.getVendorFeeAmount()
            					   && existingPartyRoleVendorFeesDTO.getVendorFeeAmount() == null) {
            				   existingPartyRoleVendorFeesDTO.setVendorFeeAmount(importedPartyRoleVendorFeesDTO.getVendorFeeAmount());
           						updateRequiredFlag = true;
        				   }
            			   if(null != importedPartyRoleVendorFeesDTO.getExternalSourceId()
            					   && existingPartyRoleVendorFeesDTO.getExternalSourceId() == null) {
            				   existingPartyRoleVendorFeesDTO.setExternalSourceId(importedPartyRoleVendorFeesDTO.getExternalSourceId());
            				   updateRequiredFlag = true;
            			   }   
        			   } else {
            			   if(null != importedPartyRoleVendorFeesDTO.getVendorFeeAmount()
            					   && (existingPartyRoleVendorFeesDTO.getVendorFeeAmount() == null 
            						   || existingPartyRoleVendorFeesDTO.getVendorFeeAmount().compareTo(importedPartyRoleVendorFeesDTO.getVendorFeeAmount()) != 0)) {
            				   existingPartyRoleVendorFeesDTO.setVendorFeeAmount(importedPartyRoleVendorFeesDTO.getVendorFeeAmount());
            					updateRequiredFlag = true;
            			   }
            			   if(null != importedPartyRoleVendorFeesDTO.getExternalSourceId()
            					   && (existingPartyRoleVendorFeesDTO.getExternalSourceId() == null 
            						   || !existingPartyRoleVendorFeesDTO.getExternalSourceId().equals(importedPartyRoleVendorFeesDTO.getExternalSourceId()))) {
            				   existingPartyRoleVendorFeesDTO.setExternalSourceId(importedPartyRoleVendorFeesDTO.getExternalSourceId());
            					updateRequiredFlag = true;
            			   }   
        			   }
        		   }
        	   }
    		   if(flag){
    			   tempPartyRoleVendorFeesDTOs.add(importedPartyRoleVendorFeesDTO);
    			   updateRequiredFlag = true;
    		   }
    	   }
    	   		if(!tempPartyRoleVendorFeesDTOs.isEmpty()){
    	   			existingPartyRoleVendorFeesDTOs.addAll(tempPartyRoleVendorFeesDTOs);
    	   		}
    	   }
   			logger.info("mergeVendorFees update flag status : {}", updateRequiredFlag);    	   
       } 
       /**
        * Merge Vendor Surcharges
        * @param importedPartyRoleVendorAssessmentSurchargeDTOs
        * @param existingPartyRoleVendorAssessmentSurchargeDTOs
        */
       private void mergeVendorSurcharges(Collection<PartyRoleVendorAssessmentSurchargeDTO> importedPartyRoleVendorAssessmentSurchargeDTOs,Collection<PartyRoleVendorAssessmentSurchargeDTO> existingPartyRoleVendorAssessmentSurchargeDTOs){
    	   Collection<PartyRoleVendorAssessmentSurchargeDTO> tempPartyRoleVendorAssessmentSurchargeDTOs = new ArrayList<PartyRoleVendorAssessmentSurchargeDTO>();
    	   if(importedPartyRoleVendorAssessmentSurchargeDTOs != null && !importedPartyRoleVendorAssessmentSurchargeDTOs.isEmpty()){
    	   for(PartyRoleVendorAssessmentSurchargeDTO importedPartyRoleVendorAssessmentSurchargeDTO:importedPartyRoleVendorAssessmentSurchargeDTOs){
    		   boolean flag = true;
    		   for(PartyRoleVendorAssessmentSurchargeDTO existingPartyRoleVendorAssessmentSurchargeDTO:existingPartyRoleVendorAssessmentSurchargeDTOs){
        		   if(null != importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeTypeCode() && null != existingPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeTypeCode() &&
        				   importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeTypeCode().equals(existingPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeTypeCode())){
        			   flag = false;
        			   if(mergeAdditionalPartyInfo) {
            			   if(null != importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId()
            					   && existingPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId() == null) {
            				   existingPartyRoleVendorAssessmentSurchargeDTO.setAssessmentSurchargeJurId(
            						   importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId());
            				   updateRequiredFlag = true;
            			   }
        			   } else {
            			   if(null != importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId()
            					   && (existingPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId() == null 
            						   || existingPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId().compareTo
            						   (importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId()) != 0)) {
            					   existingPartyRoleVendorAssessmentSurchargeDTO.setAssessmentSurchargeJurId(importedPartyRoleVendorAssessmentSurchargeDTO.getAssessmentSurchargeJurId());
            					   updateRequiredFlag = true;
            			   }
        			   }
        		   }
        	   }
    		   if(flag){
    			   tempPartyRoleVendorAssessmentSurchargeDTOs.add(importedPartyRoleVendorAssessmentSurchargeDTO);
    			   updateRequiredFlag = true;
    		   }
    	   }
    	   		if(!tempPartyRoleVendorAssessmentSurchargeDTOs.isEmpty()){
    	   			existingPartyRoleVendorAssessmentSurchargeDTOs.addAll(tempPartyRoleVendorAssessmentSurchargeDTOs);
    	   		}
    	   }
  			logger.info("mergeVendorSurcharges update flag status : {}", updateRequiredFlag);    	   
       }
       /**
        * Merge Vendor Types
        * @param importedPartyRoleVendorTypeDTOs
        * @param existingPartyRoleVendorTypeDTOs
        */
       private void mergeVendorTypes(Collection<PartyRoleVendorTypeDTO> importedPartyRoleVendorTypeDTOs,Collection<PartyRoleVendorTypeDTO> existingPartyRoleVendorTypeDTOs){
    	   Collection<PartyRoleVendorTypeDTO> tempPartyRoleVendorTypeDTOs = new ArrayList<PartyRoleVendorTypeDTO>();
    	   if(importedPartyRoleVendorTypeDTOs != null && !importedPartyRoleVendorTypeDTOs.isEmpty()){
    	   for(PartyRoleVendorTypeDTO importedPartyRoleVendorTypeDTO:importedPartyRoleVendorTypeDTOs){
    		   boolean flag = true;
    		   for(PartyRoleVendorTypeDTO existingPartyRoleVendorTypeDTO:existingPartyRoleVendorTypeDTOs){
        		   if(null != importedPartyRoleVendorTypeDTO.getVendorCategoryCode() && null != existingPartyRoleVendorTypeDTO.getVendorCategoryCode() &&
        			 importedPartyRoleVendorTypeDTO.getVendorCategoryCode().equals(existingPartyRoleVendorTypeDTO.getVendorCategoryCode())&&
        			 null != importedPartyRoleVendorTypeDTO.getVendorTypeCode() && null != existingPartyRoleVendorTypeDTO.getVendorTypeCode() &&
        			 importedPartyRoleVendorTypeDTO.getVendorTypeCode().equals(existingPartyRoleVendorTypeDTO.getVendorTypeCode())){
        			   flag = false;
        			   existingPartyRoleVendorTypeDTO.setPrimaryIndicator(importedPartyRoleVendorTypeDTO.getPrimaryIndicator());
        			   if(mergeAdditionalPartyInfo) {
            			   if(null != importedPartyRoleVendorTypeDTO.getTaxonomyData()
            					   && existingPartyRoleVendorTypeDTO.getTaxonomyData() == null){
            				   existingPartyRoleVendorTypeDTO.setTaxonomyData(importedPartyRoleVendorTypeDTO.getTaxonomyData());
            				   updateRequiredFlag = true;
            			   }
            			   if(null != importedPartyRoleVendorTypeDTO.getPartyRoleVendorServiceStatus()
            					   && existingPartyRoleVendorTypeDTO.getPartyRoleVendorServiceStatus() == null){
            				   existingPartyRoleVendorTypeDTO.setPartyRoleVendorServiceStatus(importedPartyRoleVendorTypeDTO.getPartyRoleVendorServiceStatus());
            				   updateRequiredFlag = true;
              			   }
            			   if(null != importedPartyRoleVendorTypeDTO.getExternalSourceId()
            					   && existingPartyRoleVendorTypeDTO.getExternalSourceId() == null) {
            				   existingPartyRoleVendorTypeDTO.setExternalSourceId(importedPartyRoleVendorTypeDTO.getExternalSourceId());
            				   updateRequiredFlag = true;
            			   }
        			   } else {
            			   if(existingPartyRoleVendorTypeDTO.getAreaWideServiceIndicator() !=  importedPartyRoleVendorTypeDTO.getAreaWideServiceIndicator()){
            				   existingPartyRoleVendorTypeDTO.setAreaWideServiceIndicator(importedPartyRoleVendorTypeDTO.getAreaWideServiceIndicator());
            				   updateRequiredFlag = true;
            			   }
            			   if(existingPartyRoleVendorTypeDTO.getPerformDrugTestingIndicator() !=  importedPartyRoleVendorTypeDTO.getPerformDrugTestingIndicator()){
            				   existingPartyRoleVendorTypeDTO.setPerformDrugTestingIndicator(importedPartyRoleVendorTypeDTO.getPerformDrugTestingIndicator());
            				   updateRequiredFlag = true;
            			   }
            			   if(null != importedPartyRoleVendorTypeDTO.getTaxonomyData()){
            				   existingPartyRoleVendorTypeDTO.setTaxonomyData(importedPartyRoleVendorTypeDTO.getTaxonomyData());
            				   updateRequiredFlag = true;
            			   }
            			   if(null != importedPartyRoleVendorTypeDTO.getPartyRoleVendorServiceStatus()){
            				   existingPartyRoleVendorTypeDTO.setPartyRoleVendorServiceStatus(importedPartyRoleVendorTypeDTO.getPartyRoleVendorServiceStatus());
            				   updateRequiredFlag = true;
              			   }
            			   if(null != importedPartyRoleVendorTypeDTO.getExternalSourceId()
            					   && (existingPartyRoleVendorTypeDTO.getExternalSourceId() == null 
            						   || !existingPartyRoleVendorTypeDTO.getExternalSourceId().equals(importedPartyRoleVendorTypeDTO.getExternalSourceId()))){
            				   existingPartyRoleVendorTypeDTO.setExternalSourceId(importedPartyRoleVendorTypeDTO.getExternalSourceId());
            				   updateRequiredFlag = true;
            			   }
        			   }
        		   }
        	   }
    		   if(flag){
    			   tempPartyRoleVendorTypeDTOs.add(importedPartyRoleVendorTypeDTO);
    			   updateRequiredFlag = true;
    		   }
    	   }
    	   		if(!tempPartyRoleVendorTypeDTOs.isEmpty()){
    	   			existingPartyRoleVendorTypeDTOs.addAll(tempPartyRoleVendorTypeDTOs);
    	   		}
    	   }
 			logger.info("mergeVendorTypes update flag status : {}", updateRequiredFlag);     	   
       }
    /**
   	 * Merge Party Interaction Channel 
   	 * @param importedParty
   	 * @param existingParty
   	 */
       public void mergePartyIntractionChannelCompositeDTO(PartyDTO importedParty, PartyDTO existingParty){
         if(null != importedParty.getPartyInteractionChannelCompositeDTO() && null != existingParty.getPartyInteractionChannelCompositeDTO()){
               if(null != importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels() && !importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels().isEmpty()){
                     if(null != existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels()){
                          mergePhoneChannels(importedParty,existingParty);
                     }else{
                           Collection<PhoneChannelDTO> tempPhoneChannelDTO = new ArrayList<PhoneChannelDTO>();
                           tempPhoneChannelDTO.addAll(importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels());
                           existingParty.getPartyInteractionChannelCompositeDTO().setPhoneChannels(tempPhoneChannelDTO);
                           updateRequiredFlag = true;
                     }
               }
              if(null != importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels() && !importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels().isEmpty()){
            	  if(null != existingParty.getPartyInteractionChannelCompositeDTO().getEmailChannels()){
                           mergeAddressChannel(importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels(),existingParty.getPartyInteractionChannelCompositeDTO().getEmailChannels());
                     }else{
                           Collection<AddressChannelDTO> tempemailChannelDTO = new ArrayList<AddressChannelDTO>();
                           tempemailChannelDTO.addAll(importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels());
                           existingParty.getPartyInteractionChannelCompositeDTO().setEmailChannels(tempemailChannelDTO);
                           updateRequiredFlag = true;
                     }
              }
              if(null != importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels() && !importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels().isEmpty()){
            	  if( null != existingParty.getPartyInteractionChannelCompositeDTO().getOtherChannels()){
                           mergeAddressChannel(importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels(),existingParty.getPartyInteractionChannelCompositeDTO().getOtherChannels());
                   }else{
                           Collection<AddressChannelDTO> tempOtherChannelDTO = new ArrayList<AddressChannelDTO>();
                           tempOtherChannelDTO.addAll(importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels());
                           existingParty.getPartyInteractionChannelCompositeDTO().setOtherChannels(tempOtherChannelDTO);
                           updateRequiredFlag = true;
                     }
              }
         }else if(null != importedParty.getPartyInteractionChannelCompositeDTO()){
               PartyInteractionChannelCompositeDTO partyInteractionChannelCompositeDTO = new PartyInteractionChannelCompositeDTO();
               if(null != importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels() && !importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels().isEmpty()){
                           Collection<PhoneChannelDTO> tempPhoneChannelDTO = new ArrayList<PhoneChannelDTO>();
                           tempPhoneChannelDTO.addAll(importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels());
                           partyInteractionChannelCompositeDTO.setPhoneChannels(tempPhoneChannelDTO);
                     }
                     if(null != importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels() && !importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels().isEmpty()){
                           Collection<AddressChannelDTO> tempemailChannelDTO = new ArrayList<AddressChannelDTO>();
                           tempemailChannelDTO.addAll(importedParty.getPartyInteractionChannelCompositeDTO().getEmailChannels());
                           partyInteractionChannelCompositeDTO.setEmailChannels(tempemailChannelDTO);
                     }
                     if(null != importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels() && !importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels().isEmpty()){
                           Collection<AddressChannelDTO> tempOtherChannelDTO = new ArrayList<AddressChannelDTO>();
                           tempOtherChannelDTO.addAll(importedParty.getPartyInteractionChannelCompositeDTO().getOtherChannels());
                           partyInteractionChannelCompositeDTO.setOtherChannels(tempOtherChannelDTO);
                     }
                     existingParty.setPartyInteractionChannelCompositeDTO(partyInteractionChannelCompositeDTO);
                     updateRequiredFlag = true;
               }
			logger.info("mergePartyIntractionChannelCompositeDTO update flag status : {}", updateRequiredFlag);          
         }
       
    /**
   	 * Merge Phone Channel
   	 * @param importedParty
   	 * @param existingParty
   	 */
       public void mergePhoneChannels(PartyDTO importedParty, PartyDTO existingParty){
         Collection<PhoneChannelDTO> tempPhoneChannelDTO = new ArrayList<PhoneChannelDTO>();
    	 Collection<PhoneChannelDTO> newPhoneChannelDTO = new ArrayList<PhoneChannelDTO>();
    	 Iterator<PhoneChannelDTO> itexistingPhoneChannelDTO = null;
      	Map<String, String> phoneUsageMap = new HashMap<String, String>();
         boolean processPhoneMerge = true;

         
         for(PhoneChannelDTO importedPhoneChannelDTO:importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels()){
        	 if(importedPhoneChannelDTO.getUsageTypeCode().equals(phoneUsageMap.get(importedPhoneChannelDTO.getUsageTypeCode()))){
        		 processPhoneMerge = false;
        		 break;
        	 }else{
        		 phoneUsageMap.put(importedPhoneChannelDTO.getUsageTypeCode(), importedPhoneChannelDTO.getUsageTypeCode());
        	 }
        	 
         }
         
         if(!processPhoneMerge){
        	 phoneUsageMap.clear();
         for(PhoneChannelDTO existingPhoneChannelDTO:existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels()){
        	 if(existingPhoneChannelDTO.getUsageTypeCode().equals(phoneUsageMap.get(existingPhoneChannelDTO.getUsageTypeCode()))){
        		 processPhoneMerge = false;
        		 break;
        	 }else{
        		 phoneUsageMap.put(existingPhoneChannelDTO.getUsageTypeCode(), existingPhoneChannelDTO.getUsageTypeCode());
        	 }
        	 
         	}
         }    	 
    	 
         if(processPhoneMerge){
         for(PhoneChannelDTO importedPhoneChannelDTO:importedParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels()){
        	 int matchCount = 0;
             boolean flag = true;
             itexistingPhoneChannelDTO = (existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels()==null)
  	   			   ?null:existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels().iterator();             
             outerloop:
             if(null != importedPhoneChannelDTO.getInteractionChannelAgreementTypes() && !importedPhoneChannelDTO.getInteractionChannelAgreementTypes().isEmpty()){
            	   	   int importedCount =  importedPhoneChannelDTO.getInteractionChannelAgreementTypes().size(); 
            		   while(itexistingPhoneChannelDTO != null && itexistingPhoneChannelDTO.hasNext()){
               			   PhoneChannelDTO existingPhoneChannelDTO = itexistingPhoneChannelDTO.next();
               			   int existingCount =  existingPhoneChannelDTO.getInteractionChannelAgreementTypes().size(); 
                            if(null != importedPhoneChannelDTO.getUsageTypeCode() && null != existingPhoneChannelDTO.getUsageTypeCode()&&
                                 importedPhoneChannelDTO.getUsageTypeCode().equals(existingPhoneChannelDTO.getUsageTypeCode())){
                            	for(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO:importedPhoneChannelDTO.getInteractionChannelAgreementTypes()){                            	
                            		for(InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO:existingPhoneChannelDTO.getInteractionChannelAgreementTypes()){
                                		   if(null != importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode() && null != existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode() &&
                                				   importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode().equals(existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode())){
                                			   matchCount++;
                                			   // New logic Added - 04/25/2014 @GR - Sync up agreement types from the imported objects.
                                			   updateInteractionChannelAgreementTypes(importedInteractionChannelAgreementTypeDTO, existingInteractionChannelAgreementTypeDTO);
                                  			 //After address flooding no synchronization of agreement type would need to occur. apply merge when it matches with existingCount as well                                			   
                                			 if(matchCount == importedCount || matchCount == existingCount){
                                				 updatePhoneNumber(importedPhoneChannelDTO, existingPhoneChannelDTO);
                                				 //set primaryUsageIndicator
                                				existingInteractionChannelAgreementTypeDTO.setPrimaryUsageIndicator(true);
                                				newPhoneChannelDTO.add(existingPhoneChannelDTO);
                                             	existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels().remove(existingPhoneChannelDTO);
                                             	flag = false;
                                             	break outerloop;
                                			 }
                                		   }
                                	   }
                            	   }
                            	}//Sync Agreement types if the phone numbers are same.
                            	if(null != importedPhoneChannelDTO.getUsageTypeCode() && null != existingPhoneChannelDTO.getUsageTypeCode()&&
                                        importedPhoneChannelDTO.getUsageTypeCode().equals(existingPhoneChannelDTO.getUsageTypeCode()) 
                                        && importedPhoneChannelDTO.getPhoneNumber().equals(existingPhoneChannelDTO.getPhoneNumber())){
                            			/*
                            			 * Per conversation with Kristy and John on 09/19/2013 - we can not expire existing agreement types but we need to add the
                            			 * new agreement types from the imported one if not found - Commented the logic for the first part below
                            			 */
                            			//Expire the existing agreement types if not found.
                            			/* 
                            			boolean existMatchNotFound = true;
                                    	for(InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO:existingPhoneChannelDTO.getInteractionChannelAgreementTypes()){
                                    		existMatchNotFound = true;
                                    		for(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO:importedPhoneChannelDTO.getInteractionChannelAgreementTypes()){
                                    			 if(null != importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode() && null != existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode() &&
                                      				   importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode().equals(existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode())){
                                    				 	existMatchNotFound = false;
                                    			 	}
                                    			}
                                    			if(existMatchNotFound){
                                    				existingInteractionChannelAgreementTypeDTO.setEndDate(DateUtility.getSystemDateOnly());
                                    				updateRequiredFlag = true;
                                    			}
                                    			 //set primaryUsageIndicator
                                				existingInteractionChannelAgreementTypeDTO.setPrimaryUsageIndicator(true);                                    			
                                    		} 
                                    	*/                           			
                          			
                            			//Add new agreement types from the imported one.
                            			boolean impMatchNotFound = true;
                                    	for(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO:importedPhoneChannelDTO.getInteractionChannelAgreementTypes()){
                                    		impMatchNotFound = true;
                                    		for(InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO:existingPhoneChannelDTO.getInteractionChannelAgreementTypes()){
                                    			 if(null != importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode() && null != existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode() &&
                                      				   importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode().equals(existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode())){
                                    				 	impMatchNotFound = false;
                                    			 	}
                                    			}
                                    			if(impMatchNotFound){
                                    				Collection<InteractionChannelAgreementTypeDTO> currIntrchagreList = existingPhoneChannelDTO.getInteractionChannelAgreementTypes();
                                    				currIntrchagreList.add(importedInteractionChannelAgreementTypeDTO);
                                    				existingPhoneChannelDTO.setInteractionChannelAgreementTypes(currIntrchagreList);
                                    				updateRequiredFlag = true;
                                    			}
                                    		}
                        				newPhoneChannelDTO.add(existingPhoneChannelDTO);
                                     	existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels().remove(existingPhoneChannelDTO);                                    	
                            			flag = false;
                            			break outerloop;
                            	}
            	   	   		}
          		   
             			}
             			//Add to current communication for default flooding
				if(flag && importedPhoneChannelDTO.getInteractionChannelAgreementTypes() != null){
					//Check if None of the usage type code matches and then add else update the matching usage type
					while(itexistingPhoneChannelDTO != null && itexistingPhoneChannelDTO.hasNext()){
						PhoneChannelDTO existingPhoneChannelDTO = itexistingPhoneChannelDTO.next();
						if(null != importedPhoneChannelDTO.getUsageTypeCode() && null != existingPhoneChannelDTO.getUsageTypeCode()&&
                                importedPhoneChannelDTO.getUsageTypeCode().equals(existingPhoneChannelDTO.getUsageTypeCode())){
							 	updatePhoneNumber(importedPhoneChannelDTO, existingPhoneChannelDTO);
							 	flag = false;
						}
					}
					if(flag){
						tempPhoneChannelDTO.add(importedPhoneChannelDTO);
						updateRequiredFlag = true;
					}
				}
        }
         
       logger.debug("tempPhoneChannelDTO size: {}", tempPhoneChannelDTO.size());
       logger.debug("newPhoneChannelDTO size: {}", newPhoneChannelDTO.size());
  
       //Add the existing Phone Channel that did not have a match.
  
       if(!existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels().isEmpty()){
    	   newPhoneChannelDTO.addAll(existingParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels());
       }
       

       /*logic added to expire existing usage agreements before adding new ones.
       	for(PhoneChannelDTO tempDto : tempPhoneChannelDTO){
      		if(null != tempDto.getInteractionChannelAgreementTypes() && !tempDto.getInteractionChannelAgreementTypes().isEmpty()){
      			 for(InteractionChannelAgreementTypeDTO tempAgreementTypeDTO:tempDto.getInteractionChannelAgreementTypes()){
      				 for(PhoneChannelDTO newPhoneDto : newPhoneChannelDTO){
      					 if(tempDto.getUsageTypeCode() != null && newPhoneDto.getUsageTypeCode() != null 
      					    && tempDto.getUsageTypeCode().equals(newPhoneDto.getUsageTypeCode())){
      						 
      					 for(InteractionChannelAgreementTypeDTO newAgreementTypeDTO:newPhoneDto.getInteractionChannelAgreementTypes()){
      						//if agreement type code matches than expire that agreement type.
                               if(null != tempAgreementTypeDTO.getAgreementTypeCode() && null != newAgreementTypeDTO.getAgreementTypeCode() && 
                            		   tempAgreementTypeDTO.getAgreementTypeCode().equals(newAgreementTypeDTO.getAgreementTypeCode())){
                            	   if (newAgreementTypeDTO.getEndDate() != null) continue;
                            	   	newAgreementTypeDTO.setEndDate(DateUtility.getSystemDateOnly());
                               }
      					 	}
      				  }
                   	}
      			 }
      		}
      	}*/
       	
       	if(!newPhoneChannelDTO.isEmpty()){
       		tempPhoneChannelDTO.addAll(newPhoneChannelDTO);
      	} 
       	
	     if(!tempPhoneChannelDTO.isEmpty()){
	    	 existingParty.getPartyInteractionChannelCompositeDTO().setPhoneChannels(tempPhoneChannelDTO);
	     }
      }
			logger.info("mergePhoneChannels update flag status : {}",updateRequiredFlag);          
   }
       
   private void updateInteractionChannelAgreementTypes(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO, 
		   										InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO) {
		/*Effective - End Dates special handling - They are set only when it is null regardless of the flag.
   		Since these are system dates and would be different everytime on the incoming party data*/
      if(existingInteractionChannelAgreementTypeDTO.getEffectiveDate()== null && importedInteractionChannelAgreementTypeDTO.getEffectiveDate() != null){
    	  existingInteractionChannelAgreementTypeDTO.setEffectiveDate(importedInteractionChannelAgreementTypeDTO.getEffectiveDate());
            updateRequiredFlag = true;
      }
      
      if(existingInteractionChannelAgreementTypeDTO.getEndDate() == null && importedInteractionChannelAgreementTypeDTO.getEndDate() != null){
    	  existingInteractionChannelAgreementTypeDTO.setEndDate(importedInteractionChannelAgreementTypeDTO.getEndDate());
            updateRequiredFlag = true;
      }	   
   }
       
   private void updatePhoneNumber(PhoneChannelDTO importedPhoneChannelDTO,PhoneChannelDTO existingPhoneChannelDTO){
   		
       	if(null != importedPhoneChannelDTO.getPhoneNumber()){
   			 if((existingPhoneChannelDTO.getPhoneNumber() == null && mergeAdditionalPartyInfo)
   					 || (!mergeAdditionalPartyInfo && (existingPhoneChannelDTO.getPhoneNumber() == null ||
   							 !existingPhoneChannelDTO.getPhoneNumber().equals(importedPhoneChannelDTO.getPhoneNumber())))){
   				 existingPhoneChannelDTO.setPhoneNumber(importedPhoneChannelDTO.getPhoneNumber());
   				 updateRequiredFlag = true;
   			 }
   		 }
       	/* Not Required for update
   		 if(null != importedPhoneChannelDTO.getOldPhoneNumber()){
   			 if(existingPhoneChannelDTO.getOldPhoneNumber() == null
   					 || !existingPhoneChannelDTO.getOldPhoneNumber().equals(importedPhoneChannelDTO.getOldPhoneNumber())){
   				 existingPhoneChannelDTO.setOldPhoneNumber(importedPhoneChannelDTO.getOldPhoneNumber());
   				 updateRequiredFlag = true;
   			 }
   		 }*/
   		 if(null != importedPhoneChannelDTO.getPhoneNumberExtension()){
   			 if((existingPhoneChannelDTO.getPhoneNumberExtension() == null && mergeAdditionalPartyInfo)
   					 || (!mergeAdditionalPartyInfo && (existingPhoneChannelDTO.getPhoneNumberExtension() == null ||
   						!existingPhoneChannelDTO.getPhoneNumberExtension().equals(importedPhoneChannelDTO.getPhoneNumberExtension())))){
   				 existingPhoneChannelDTO.setPhoneNumberExtension(importedPhoneChannelDTO.getPhoneNumberExtension());
   				 updateRequiredFlag = true;
   			 }
   		 }
   		 if(null != importedPhoneChannelDTO.getInteractionChannelComment()){
   			 if((existingPhoneChannelDTO.getInteractionChannelComment() == null && mergeAdditionalPartyInfo)
   					 || (!mergeAdditionalPartyInfo && (existingPhoneChannelDTO.getInteractionChannelComment() == null ||
   						!existingPhoneChannelDTO.getInteractionChannelComment().equals(importedPhoneChannelDTO.getInteractionChannelComment())))){
   				 existingPhoneChannelDTO.setInteractionChannelComment(importedPhoneChannelDTO.getInteractionChannelComment());
   				 updateRequiredFlag = true;
   			 }
   		 }	
       	
       }       
       
    /**
     * Merge Address
     * @param importedAddressChannelDTOs
     * @param existingAddressChannelDTOs
     */
       private void mergeAddressChannel(Collection<AddressChannelDTO> importedAddressChannelDTOs,Collection<AddressChannelDTO> existingAddressChannelDTOs){
         Collection<AddressChannelDTO> tempAddressChannelDTO = new ArrayList<AddressChannelDTO>();
         Collection<AddressChannelDTO> newAddressChannelDTO = new ArrayList<AddressChannelDTO>();
         Iterator<AddressChannelDTO> itexistingAddressChannelDTO = null;
         
     	Map<String, String> addressUsageMap = new HashMap<String, String>();
         
         boolean processAddressMerge = true;

         
         for(AddressChannelDTO importedAddressChannelDTO:importedAddressChannelDTOs){
        	 if(importedAddressChannelDTO.getUsageTypeCode().equals(addressUsageMap.get(importedAddressChannelDTO.getUsageTypeCode()))){
        		 processAddressMerge = false;
        	 }else{
        		 addressUsageMap.put(importedAddressChannelDTO.getUsageTypeCode(), importedAddressChannelDTO.getUsageTypeCode());
        	 }
        	 
         }
         
         if(!processAddressMerge){
        	 addressUsageMap.clear();
         for(AddressChannelDTO existingAddressChannelDTO:existingAddressChannelDTOs){
        	 if(existingAddressChannelDTO.getUsageTypeCode().equals(addressUsageMap.get(existingAddressChannelDTO.getUsageTypeCode()))){
        		 processAddressMerge = false;
        	 }else{
        		 addressUsageMap.put(existingAddressChannelDTO.getUsageTypeCode(), existingAddressChannelDTO.getUsageTypeCode());
        	 }
        	 
         	}
         }
         
         
    	 
         if(processAddressMerge){
         
        	 for(AddressChannelDTO importedAddressChannelDTO:importedAddressChannelDTOs){
        	   int matchCount = 0;
               boolean flag = true;
      		   itexistingAddressChannelDTO = (existingAddressChannelDTOs==null)?null:existingAddressChannelDTOs.iterator();
               outerloop:
               if(null != importedAddressChannelDTO.getInteractionChannelAgreementTypes() && !importedAddressChannelDTO.getInteractionChannelAgreementTypes().isEmpty()){
            		 int importedCount =  importedAddressChannelDTO.getInteractionChannelAgreementTypes().size(); 
            		 while(itexistingAddressChannelDTO != null && itexistingAddressChannelDTO.hasNext()){
            			 AddressChannelDTO existingAddressChannelDTO = itexistingAddressChannelDTO.next();
            			 int existingCount =  existingAddressChannelDTO.getInteractionChannelAgreementTypes().size(); 
                     if(null != importedAddressChannelDTO.getUsageTypeCode() && null != existingAddressChannelDTO.getUsageTypeCode()&&
                                 importedAddressChannelDTO.getUsageTypeCode().equals(existingAddressChannelDTO.getUsageTypeCode())){
                    	 	for(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO:importedAddressChannelDTO.getInteractionChannelAgreementTypes()){                    	 
                    	 		 for(InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO:existingAddressChannelDTO.getInteractionChannelAgreementTypes()){
                            		   if(null != importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode() && null != existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode() &&
                            				   importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode().equals(existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode())){
                            			 matchCount++;
                            			 //After address flooding no synchronization of agreement type would need to occur. apply merge when it matches with existingCount as well
                          			   	// New logic Added - 04/25/2014 @GR - Sync up agreement types from the imported objects.
                          			   	updateInteractionChannelAgreementTypes(importedInteractionChannelAgreementTypeDTO, existingInteractionChannelAgreementTypeDTO);                            			 
                            			 if (matchCount == importedCount || matchCount == existingCount){
                            				 updateCommunicationAddress(importedAddressChannelDTO, existingAddressChannelDTO);
                                			 //set primaryUsageIndicator
                             				 existingInteractionChannelAgreementTypeDTO.setPrimaryUsageIndicator(true);                                                                				 
                            				 newAddressChannelDTO.add(existingAddressChannelDTO);
                            				 existingAddressChannelDTOs.remove(existingAddressChannelDTO);
                            				 flag = false;
                            				 break outerloop;
                            			 }
                            		   }
                            	   }
                        	   }
                        	}//Sync Agreement types if the Email addresses are same.
                 			if(null != importedAddressChannelDTO.getUsageTypeCode() && null != existingAddressChannelDTO.getUsageTypeCode()&&
                 					importedAddressChannelDTO.getUsageTypeCode().equals(existingAddressChannelDTO.getUsageTypeCode()) 
                 					&& importedAddressChannelDTO.getInteractionChannelTypeAddress().equals(existingAddressChannelDTO.getInteractionChannelTypeAddress())){
                    		/*
                    			 * Per conversation with Kristy and John on 09/19/2013 - we can not expire existing agreement types but we need to add the
                    			 * new agreement types from the imported one if not found - Commented the logic for the first part below
                    		*/
                			//Expire the existing agreement types if not found.
                 			/* 
                 			boolean existMatchNotFound = true;
                        	for(InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO:existingAddressChannelDTO.getInteractionChannelAgreementTypes()){
                        		existMatchNotFound = true;
                        		for(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO:importedAddressChannelDTO.getInteractionChannelAgreementTypes()){
                        			 if(null != importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode() && null != existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode() &&
                          				   importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode().equals(existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode())){
                        				 	existMatchNotFound = false;
                        			 	}
                        			}
                        			if(existMatchNotFound){
                        				existingInteractionChannelAgreementTypeDTO.setEndDate(DateUtility.getSystemDateOnly());
                        				updateRequiredFlag = true;
                        			}
                        			 //set primaryUsageIndicator
                    				existingInteractionChannelAgreementTypeDTO.setPrimaryUsageIndicator(true);                                    			
                        		}
                        	*/                            			
              			
                			//Add new agreement types from the imported one.
                			boolean impMatchNotFound = true;
                        	for(InteractionChannelAgreementTypeDTO importedInteractionChannelAgreementTypeDTO:importedAddressChannelDTO.getInteractionChannelAgreementTypes()){
                        		impMatchNotFound = true;
                        		for(InteractionChannelAgreementTypeDTO existingInteractionChannelAgreementTypeDTO:existingAddressChannelDTO.getInteractionChannelAgreementTypes()){
                        			 if(null != importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode() && null != existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode() &&
                          				   importedInteractionChannelAgreementTypeDTO.getAgreementTypeCode().equals(existingInteractionChannelAgreementTypeDTO.getAgreementTypeCode())){
                        				 	impMatchNotFound = false;
                        			 	}
                        			}
                        			if(impMatchNotFound){
                        				Collection<InteractionChannelAgreementTypeDTO> currIntrchagreList = existingAddressChannelDTO.getInteractionChannelAgreementTypes();
                        				currIntrchagreList.add(importedInteractionChannelAgreementTypeDTO);
                        				existingAddressChannelDTO.setInteractionChannelAgreementTypes(currIntrchagreList);
                        				updateRequiredFlag = true;
                        			}
                        		}
                        	 newAddressChannelDTO.add(existingAddressChannelDTO);
                        	 existingAddressChannelDTOs.remove(existingAddressChannelDTO);                                    	
                			flag = false;
                			break outerloop;
                	}
                     
            	 		}

               }
               //Add to the current channel for default flooding
				if(flag && importedAddressChannelDTO.getInteractionChannelAgreementTypes() != null){
					//Check if None of the usage type code matches and then add else update the matching usage type
					while(itexistingAddressChannelDTO != null && itexistingAddressChannelDTO.hasNext()){
						AddressChannelDTO existingAddressChannelDTO = itexistingAddressChannelDTO.next();
						if(null != importedAddressChannelDTO.getUsageTypeCode() && null != existingAddressChannelDTO.getUsageTypeCode()&&
                               importedAddressChannelDTO.getUsageTypeCode().equals(existingAddressChannelDTO.getUsageTypeCode())){
							 	updateCommunicationAddress(importedAddressChannelDTO, existingAddressChannelDTO);
							 	flag = false;
						}
					}
					if(flag){
						tempAddressChannelDTO.add(importedAddressChannelDTO);
						updateRequiredFlag = true;
					}
				}             
         }
         
         //Add the existing Address Channel that did not have a match.
         
         if(!existingAddressChannelDTOs.isEmpty()){
        	 newAddressChannelDTO.addAll(existingAddressChannelDTOs);
         }
         
         
   /*     	for(AddressChannelDTO tempDto : tempAddressChannelDTO){
          		if(null != tempDto.getInteractionChannelAgreementTypes() && !tempDto.getInteractionChannelAgreementTypes().isEmpty()){
          			 for(InteractionChannelAgreementTypeDTO tempAgreementTypeDTO:tempDto.getInteractionChannelAgreementTypes()){
          				 for(AddressChannelDTO newAddressDto : newAddressChannelDTO){
          					 if(tempDto.getUsageTypeCode() != null && newAddressDto.getUsageTypeCode() != null 
          					    && tempDto.getUsageTypeCode().equals(newAddressDto.getUsageTypeCode())){
          					 for(InteractionChannelAgreementTypeDTO newAgreementTypeDTO:newAddressDto.getInteractionChannelAgreementTypes()){
          						//if agreement type code matches than expire that agreement type.
                                   if(null != tempAgreementTypeDTO.getAgreementTypeCode() && null != newAgreementTypeDTO.getAgreementTypeCode() && 
                                		   tempAgreementTypeDTO.getAgreementTypeCode().equals(newAgreementTypeDTO.getAgreementTypeCode())){
                                	   		if (newAgreementTypeDTO.getEndDate() != null) continue;
                                	   		newAgreementTypeDTO.setEndDate(DateUtility.getSystemDateOnly());
                                 }
          					 }
          				  }
                       	}
          			 }
          		}
          	}  */       
        
         if(!newAddressChannelDTO.isEmpty()){
        	 tempAddressChannelDTO.addAll(newAddressChannelDTO);
          }             
         if(!tempAddressChannelDTO.isEmpty()){
        	   existingAddressChannelDTOs.clear();
               existingAddressChannelDTOs.addAll(tempAddressChannelDTO);
         }
     }
			logger.info("mergeAddressChannel update flag status : {}", updateRequiredFlag);           
   }
       
       
   private void	updateCommunicationAddress(AddressChannelDTO importedAddressChannelDTO,AddressChannelDTO existingAddressChannelDTO){
    	   
			 if(null != importedAddressChannelDTO.getInteractionChannelTypeAddress()){
				 if((existingAddressChannelDTO.getInteractionChannelTypeAddress() == null && mergeAdditionalPartyInfo)
						 || (!mergeAdditionalPartyInfo && (existingAddressChannelDTO.getInteractionChannelTypeAddress() == null ||
						!existingAddressChannelDTO.getInteractionChannelTypeAddress().equals(importedAddressChannelDTO.getInteractionChannelTypeAddress())))){
					 existingAddressChannelDTO.setInteractionChannelTypeAddress(importedAddressChannelDTO.getInteractionChannelTypeAddress());
					 updateRequiredFlag = true;
				 }
			 }
			 /* Not Required for update.
			 if(null != importedAddressChannelDTO.getOldInteractionChannelTypeAddress()){
				 if(existingAddressChannelDTO.getOldInteractionChannelTypeAddress() == null 
						 || !existingAddressChannelDTO.getOldInteractionChannelTypeAddress().equals(importedAddressChannelDTO.getOldInteractionChannelTypeAddress()) ){
					 existingAddressChannelDTO.setOldInteractionChannelTypeAddress(importedAddressChannelDTO.getOldInteractionChannelTypeAddress());
					 updateRequiredFlag = true;
				 }
			 }*/
			 
			 if(null != importedAddressChannelDTO.getInteractionChannelComment()){
				 if((existingAddressChannelDTO.getInteractionChannelComment() == null && mergeAdditionalPartyInfo)
						 || (!mergeAdditionalPartyInfo && (existingAddressChannelDTO.getInteractionChannelComment() == null ||
						!existingAddressChannelDTO.getInteractionChannelComment().equals(importedAddressChannelDTO.getInteractionChannelComment())))){
					 existingAddressChannelDTO.setInteractionChannelComment(importedAddressChannelDTO.getInteractionChannelComment());
					 updateRequiredFlag = true;
				 }
			 }    	   
    	   
       }       
    /**
   	 * Merge Agreement Type Participation
   	 * @param importedParty
   	 * @param existingParty
   	 */
       public void mergeAgreementTypePtcp(PartyDTO importedParty, PartyDTO existingParty){
    	   Collection<PartyAgreementTypeDTO> newPartyAgreementTypeDTO = new ArrayList<PartyAgreementTypeDTO>();
	       Collection<PartyAgreementTypeDTO> tempPartyAgreementTypeDTO = new ArrayList<PartyAgreementTypeDTO>();
    	   Iterator<PartyAgreementTypeDTO> itexistingPartyAgreementTypeDTO = null;
    	   if(importedParty.getAgreementTypesPtcp()!=null && !importedParty.getAgreementTypesPtcp().isEmpty()){    	   
	         for(PartyAgreementTypeDTO importedPartyAgreementTypeDTO:importedParty.getAgreementTypesPtcp()){
            	 //Skip party agreement types if found - It is creating duplicates
            	 if(importedPartyAgreementTypeDTO.getAggrementType().equals(PartyConstants.PARTY_CONTEXT)){
            		 continue;
            	 	}	        	 
	             boolean flag = true;
	             itexistingPartyAgreementTypeDTO = (existingParty.getAgreementTypesPtcp() == null)?null:existingParty.getAgreementTypesPtcp().iterator();
	             outerloop:
	             while(itexistingPartyAgreementTypeDTO != null && itexistingPartyAgreementTypeDTO.hasNext()){
	            	 PartyAgreementTypeDTO existingPartyAgreementTypeDTO = itexistingPartyAgreementTypeDTO.next();
	            	 	if(null != importedPartyAgreementTypeDTO.getAggrementType() && null != existingPartyAgreementTypeDTO.getAggrementType() &&
	                                 importedPartyAgreementTypeDTO.getAggrementType().equals(existingPartyAgreementTypeDTO.getAggrementType())){
	            	 			/*Effective - End Dates special handling - They are set only when it is null regardless of the flag.
 						   		Since these are system dates and would be different everytime on the incoming party data*/
	                           if(existingPartyAgreementTypeDTO.getEffectiveDateTime() == null && importedPartyAgreementTypeDTO.getEffectiveDateTime() != null){
	                                 existingPartyAgreementTypeDTO.setEffectiveDateTime(importedPartyAgreementTypeDTO.getEffectiveDateTime());
	                                 updateRequiredFlag = true;
	                           }
	                           if(existingPartyAgreementTypeDTO.getEndDateTime() == null && importedPartyAgreementTypeDTO.getEndDateTime() != null){
	                                 existingPartyAgreementTypeDTO.setEndDateTime(importedPartyAgreementTypeDTO.getEndDateTime());
	                                 updateRequiredFlag = true;
	                           }
	                           newPartyAgreementTypeDTO.add(existingPartyAgreementTypeDTO);
	                           existingParty.getAgreementTypesPtcp().remove(existingPartyAgreementTypeDTO);
	                           flag = false;
	                           break outerloop;
	                     }
	               }
	               if(flag){
                       tempPartyAgreementTypeDTO.add(importedPartyAgreementTypeDTO);
                       updateRequiredFlag = true;
                 }
	         }
            //Add the existing Agreement Participants that did not have a match.
	         
	         if(!existingParty.getAgreementTypesPtcp().isEmpty()){
	        	 newPartyAgreementTypeDTO.addAll(existingParty.getAgreementTypesPtcp());
	         }	    	
	    	
	    	if(!newPartyAgreementTypeDTO.isEmpty()){
	    		tempPartyAgreementTypeDTO.addAll(newPartyAgreementTypeDTO);
	    	}

		     if(!tempPartyAgreementTypeDTO.isEmpty()){
		    	 existingParty.setAgreementTypesPtcp(tempPartyAgreementTypeDTO);
		     }
		     
	    	/*
		     if(!tempPartyAgreementTypeDTO.isEmpty()){
		         if(null != existingParty.getAgreementTypesPtcp() && !existingParty.getAgreementTypesPtcp().isEmpty()){
		               existingParty.getAgreementTypesPtcp().addAll(tempPartyAgreementTypeDTO);
		         }
		         else{
		               Collection<PartyAgreementTypeDTO> newPartyAgreementTypeDTO = new ArrayList<PartyAgreementTypeDTO>(); 
		               newPartyAgreementTypeDTO.addAll(tempPartyAgreementTypeDTO);
		               existingParty.setAgreementTypesPtcp(newPartyAgreementTypeDTO);
		         }
		      }
		      */
    	   }
				logger.info("mergeAgreementTypePtcp update flag status : {}", updateRequiredFlag);    		     
	    	
       }
       
    /**
   	 * Merge Bankruptcy 
   	 * @param importedParty
   	 * @param existingParty
   	 */
      public void mergeBankruptcyDetails(PartyDTO importedParty, PartyDTO existingParty){
	    if(null != importedParty.getBankruptcyDetails() && !importedParty.getBankruptcyDetails().isEmpty()){
	        Collection<PartyBankruptcyDTO> tempPartyBankruptcyDTO = new ArrayList<PartyBankruptcyDTO>();
	            for(PartyBankruptcyDTO importedPartyBankruptcyDTO:importedParty.getBankruptcyDetails()){
	               boolean flag = true;
	               for(PartyBankruptcyDTO existingPartyBankruptcyDTO:existingParty.getBankruptcyDetails()){
	                     if(null != importedPartyBankruptcyDTO.getBankruptcyTypeCode() && null != existingPartyBankruptcyDTO.getBankruptcyTypeCode() &&
	                                importedPartyBankruptcyDTO.getBankruptcyTypeCode().equals(existingPartyBankruptcyDTO.getBankruptcyTypeCode())){
	                           flag = false;

	                           if(mergeAdditionalPartyInfo) {
		                           if(null != importedPartyBankruptcyDTO.getBankruptcyFileDate()
		                        		   && existingPartyBankruptcyDTO.getBankruptcyFileDate() == null){
		                                existingPartyBankruptcyDTO.setBankruptcyFileDate(importedPartyBankruptcyDTO.getBankruptcyFileDate());
		                                updateRequiredFlag = true;
		                           }
		                           if(null != importedPartyBankruptcyDTO.getBankruptcyReleaseDate()
		                        		   && existingPartyBankruptcyDTO.getBankruptcyReleaseDate() == null){
		                                existingPartyBankruptcyDTO.setBankruptcyReleaseDate(importedPartyBankruptcyDTO.getBankruptcyReleaseDate());
		                                updateRequiredFlag = true;
		                           }
	                           } else {
		                           if(existingPartyBankruptcyDTO.getBankruptcyFileDate() == null || 
		                        		   (null != importedPartyBankruptcyDTO.getBankruptcyFileDate() 
		                        		   && importedPartyBankruptcyDTO.getBankruptcyFileDate().compareTo(existingPartyBankruptcyDTO.getBankruptcyFileDate()) != 0)){
		                                existingPartyBankruptcyDTO.setBankruptcyFileDate(importedPartyBankruptcyDTO.getBankruptcyFileDate());
		                                updateRequiredFlag = true;
		                           }
		                           if(existingPartyBankruptcyDTO.getBankruptcyReleaseDate() == null ||
		                        		(null != importedPartyBankruptcyDTO.getBankruptcyReleaseDate()
		                        		&& importedPartyBankruptcyDTO.getBankruptcyReleaseDate().compareTo(existingPartyBankruptcyDTO.getBankruptcyReleaseDate()) != 0)){
		                                existingPartyBankruptcyDTO.setBankruptcyReleaseDate(importedPartyBankruptcyDTO.getBankruptcyReleaseDate());
		                                updateRequiredFlag = true;
		                           }
	                           }
	                     }    
	               }
	               if(flag){
                       tempPartyBankruptcyDTO.add(importedPartyBankruptcyDTO);
                       updateRequiredFlag = true;
                  }
	         }
		     if(!tempPartyBankruptcyDTO.isEmpty()){
		         if(null != existingParty.getBankruptcyDetails() && !existingParty.getBankruptcyDetails().isEmpty()){      	 
		               existingParty.getBankruptcyDetails().addAll(tempPartyBankruptcyDTO);
		         }
		         else{
		               Collection<PartyBankruptcyDTO> newPartyBankruptcyDTO = new ArrayList<PartyBankruptcyDTO>(); 
		               newPartyBankruptcyDTO.addAll(tempPartyBankruptcyDTO);
		               existingParty.setBankruptcyDetails(newPartyBankruptcyDTO);
		         }
		     }
	    }
		logger.info("mergeBankruptcyDetails update flag status : {}", updateRequiredFlag); 	    
      }
      
    /**
  	 * Merge Authorization
  	 * @param importedParty
  	 * @param existingParty
  	 */
      /*private void mergeAuthorizations(PartyDTO importedParty, PartyDTO existingParty){
        if(null != importedParty.getAuthorizations() && !importedParty.getAuthorizations().isEmpty()){
               for(PartyAuthorizationDTO existingPartyAuthorizationDTO: existingParty.getAuthorizations()){
                     existingPartyAuthorizationDTO.setEndDateTime(DateUtility.getSystemDateOnly());
               }
         }
              existingParty.setAuthorizations(importedParty.getAuthorizations());
      }*/
    /**
  	 * Merge Liens Restrictions 
  	 * @param importedParty
  	 * @param existingParty
  	 */
      public void mergeLiensRestrictions(PartyDTO importedParty, PartyDTO existingParty){
          if(null!=importedParty.getLiensRestrictions() && !importedParty.getLiensRestrictions().isEmpty()){
        	  Collection<PartyLienRestrictionDTO> tempPartyLienRestrictionDTO = new ArrayList<PartyLienRestrictionDTO>();
        	  for(PartyLienRestrictionDTO importedPartyLienRestrictionDTO:importedParty.getLiensRestrictions()){
        		  boolean flag = true;
        		  if(null!=existingParty.getLiensRestrictions() && !existingParty.getLiensRestrictions().isEmpty()){
                      for(PartyLienRestrictionDTO existingPartyLienRestrictionDTO:existingParty.getLiensRestrictions()){
                            if((null != importedPartyLienRestrictionDTO.getTypeCode() && null != existingPartyLienRestrictionDTO.getTypeCode()
                            		&& importedPartyLienRestrictionDTO.getTypeCode().equals(existingPartyLienRestrictionDTO.getTypeCode())) 
                                        && (null != importedPartyLienRestrictionDTO.getSourceCode()	&& null != existingPartyLienRestrictionDTO.getSourceCode() 
                                        		&& importedPartyLienRestrictionDTO.getSourceCode().equals(existingPartyLienRestrictionDTO.getSourceCode()))){
                                  flag = false;
                                  if(mergeAdditionalPartyInfo) {
                                      if(null != importedPartyLienRestrictionDTO.getLienholderId()
                                    		  && existingPartyLienRestrictionDTO.getLienholderId() == null){
                                    	  existingPartyLienRestrictionDTO.setLienholderId(importedPartyLienRestrictionDTO.getLienholderId());
                                    	  updateRequiredFlag = true;
                                      }
                                      if(null != importedPartyLienRestrictionDTO.getLienholder()
                                    		  && existingPartyLienRestrictionDTO.getLienholder() == null) {
                                    	  existingPartyLienRestrictionDTO.setLienholder(importedPartyLienRestrictionDTO.getLienholder());
                                    	  updateRequiredFlag = true;
                                      }
                                      if(null != importedPartyLienRestrictionDTO.getOtherSourceDescription()
                                    		  && existingPartyLienRestrictionDTO.getOtherSourceDescription() == null){
                                    	  existingPartyLienRestrictionDTO.setOtherSourceDescription(importedPartyLienRestrictionDTO.getOtherSourceDescription());
                                  		  updateRequiredFlag = true;
                                      }
                                      if(null != importedPartyLienRestrictionDTO.getReferenceNumber()
                                    		  && existingPartyLienRestrictionDTO.getReferenceNumber() == null){
                                    	  existingPartyLienRestrictionDTO.setReferenceNumber(importedPartyLienRestrictionDTO.getReferenceNumber());
                                  		  updateRequiredFlag = true;
                                      }
                                    if(null != importedPartyLienRestrictionDTO.getComment()
                                    		&& existingPartyLienRestrictionDTO.getComment() == null) {
                                    	existingPartyLienRestrictionDTO.setComment(importedPartyLienRestrictionDTO.getComment());
                                    	updateRequiredFlag = true;
                                    }
                                    if(null != importedPartyLienRestrictionDTO.getExternalSource() 
                                    		&& existingPartyLienRestrictionDTO.getExternalSource() == null){
                                    	existingPartyLienRestrictionDTO.setExternalSource(importedPartyLienRestrictionDTO.getExternalSource());
                                  		updateRequiredFlag = true;
                                    }
                                    if(null != importedPartyLienRestrictionDTO.getReceivedDate()
                                    		&& existingPartyLienRestrictionDTO.getReceivedDate() == null){
                                          	existingPartyLienRestrictionDTO.setReceivedDate(importedPartyLienRestrictionDTO.getReceivedDate());
                                          	updateRequiredFlag = true;
                                    }
                                  } else {
                                      if(null != importedPartyLienRestrictionDTO.getLienholderId()
                                    		  && (existingPartyLienRestrictionDTO.getLienholderId() == null
                                  	    		|| existingPartyLienRestrictionDTO.getLienholderId().
                                  	    		compareTo(importedPartyLienRestrictionDTO.getLienholderId()) != 0)){
                                    	  existingPartyLienRestrictionDTO.setLienholderId(importedPartyLienRestrictionDTO.getLienholderId());
                                    	  updateRequiredFlag = true;
                                      }
                                      if(null != importedPartyLienRestrictionDTO.getLienholder()){
                                    	  	existingPartyLienRestrictionDTO.setLienholder(importedPartyLienRestrictionDTO.getLienholder());
                                    	  	updateRequiredFlag = true;
                                      }
                                      if(null != importedPartyLienRestrictionDTO.getOtherSourceDescription()
                                    		  && (existingPartyLienRestrictionDTO.getOtherSourceDescription() == null 
                                  			  || !existingPartyLienRestrictionDTO.getOtherSourceDescription().equals(importedPartyLienRestrictionDTO.getOtherSourceDescription())) ){
                                    	  existingPartyLienRestrictionDTO.setOtherSourceDescription(importedPartyLienRestrictionDTO.getOtherSourceDescription());
                                  		  updateRequiredFlag = true;
                                      }
                                      if(null != importedPartyLienRestrictionDTO.getReferenceNumber()
                                    		  && (existingPartyLienRestrictionDTO.getReferenceNumber() == null 
                                  			  || !existingPartyLienRestrictionDTO.getReferenceNumber().equals(importedPartyLienRestrictionDTO.getReferenceNumber()) )){
                                    	  existingPartyLienRestrictionDTO.setReferenceNumber(importedPartyLienRestrictionDTO.getReferenceNumber());
                                  		  updateRequiredFlag = true;
                                      }
                                    if(null != importedPartyLienRestrictionDTO.getComment()
                                    		&& (existingPartyLienRestrictionDTO.getComment() == null
                                  			  || !existingPartyLienRestrictionDTO.getComment().equals(importedPartyLienRestrictionDTO.getComment()) )){
                                    	existingPartyLienRestrictionDTO.setComment(importedPartyLienRestrictionDTO.getComment());
                                    	updateRequiredFlag = true;
                                    }
                                    if(null != importedPartyLienRestrictionDTO.getExternalSource() 
                                    		&&(existingPartyLienRestrictionDTO.getExternalSource() == null
                                  			  || !existingPartyLienRestrictionDTO.getExternalSource().equals(importedPartyLienRestrictionDTO.getExternalSource()) )){
                                    	existingPartyLienRestrictionDTO.setExternalSource(importedPartyLienRestrictionDTO.getExternalSource());
                                  		updateRequiredFlag = true;
                                    }
                                    if(existingPartyLienRestrictionDTO.getReceivedDate() == null ||
                                    		(null != importedPartyLienRestrictionDTO.getReceivedDate()
                                    		&& importedPartyLienRestrictionDTO.getReceivedDate().compareTo(existingPartyLienRestrictionDTO.getReceivedDate()) != 0)){
                                          	existingPartyLienRestrictionDTO.setReceivedDate(importedPartyLienRestrictionDTO.getReceivedDate());
                                          	updateRequiredFlag = true;
                                    }
                                  }
                                  
                                  if(null != importedPartyLienRestrictionDTO.getStatus() && null != importedPartyLienRestrictionDTO.getStatus().getStatusCode()){
                                      if(null != existingPartyLienRestrictionDTO.getStatus().getRecordId()){
                                      	  if(existingPartyLienRestrictionDTO.getStatus().getStatusCode() == null
                                      			  || !existingPartyLienRestrictionDTO.getStatus().getStatusCode().equals(importedPartyLienRestrictionDTO.getStatus().getStatusCode())  ){
                                            existingPartyLienRestrictionDTO.getStatus().setStatusCode(importedPartyLienRestrictionDTO.getStatus().getStatusCode());
                                            updateRequiredFlag = true;
                                      	}
                                      }
                                      else{
                                            existingPartyLienRestrictionDTO.setStatus(importedPartyLienRestrictionDTO.getStatus());
                                            updateRequiredFlag = true;
                                      }
                                }

                                if(null != importedPartyLienRestrictionDTO.getPaymentDetails() && !importedPartyLienRestrictionDTO.getPaymentDetails().isEmpty()){
                                      if(null != existingPartyLienRestrictionDTO.getPaymentDetails() && !existingPartyLienRestrictionDTO.getPaymentDetails().isEmpty()){
                                            mergePartyLienRestriction(importedPartyLienRestrictionDTO.getPaymentDetails(),existingPartyLienRestrictionDTO.getPaymentDetails());
                                      }else{
                                            Collection<PartyLienPaymentDetailsDTO> lienPaymentDetailsDTOs = new ArrayList<PartyLienPaymentDetailsDTO>();
                                            lienPaymentDetailsDTOs.addAll(importedPartyLienRestrictionDTO.getPaymentDetails());
                                            existingPartyLienRestrictionDTO.setPaymentDetails(lienPaymentDetailsDTOs);
                                            updateRequiredFlag = true;
                                      }
                                }                                  

                            }
                      }
        		  }
        		  if(flag){
                      tempPartyLienRestrictionDTO.add(importedPartyLienRestrictionDTO);
                      updateRequiredFlag = true;
        		  }
        	  }
        	  if(!tempPartyLienRestrictionDTO.isEmpty()){
                  if(null != existingParty.getLiensRestrictions() && !existingParty.getLiensRestrictions().isEmpty()){
                        existingParty.getLiensRestrictions().addAll(tempPartyLienRestrictionDTO);
                  }else{
                        Collection<PartyLienRestrictionDTO> newPartyLienRestrictionDTO = new ArrayList<PartyLienRestrictionDTO>();
                        newPartyLienRestrictionDTO.addAll(tempPartyLienRestrictionDTO);
                        existingParty.setLiensRestrictions(newPartyLienRestrictionDTO);
                  }
        	  }
          }
  		logger.info("mergeLiensRestrictions update flag status : {}", updateRequiredFlag); 	  
        }
      /**
       * Merge Party Lien Restriction
       * @param importedPartyLienRestrictionDTOs
       * @param existingPartyLienRestrictionDTOs
       */
      private void mergePartyLienRestriction(Collection<PartyLienPaymentDetailsDTO> importedPartyLienPaymentDetailsDTOs,Collection<PartyLienPaymentDetailsDTO> existingPartyLienPaymentDetailsDTOs){
    	  Collection<PartyLienPaymentDetailsDTO> tempPartyLienPaymentDetailsDTO = new ArrayList<PartyLienPaymentDetailsDTO>();
    	  
    	  if(importedPartyLienPaymentDetailsDTOs != null && !importedPartyLienPaymentDetailsDTOs.isEmpty()){
          for(PartyLienPaymentDetailsDTO importedPartyLienPaymentDetailsDTO:importedPartyLienPaymentDetailsDTOs){
        	  boolean flag = true;
        	  for(PartyLienPaymentDetailsDTO existingPartyLienPaymentDetailsDTO:existingPartyLienPaymentDetailsDTOs){
        		  if(null != importedPartyLienPaymentDetailsDTO.getAgreementTypeCode() && null != existingPartyLienPaymentDetailsDTO.getAgreementTypeCode()&&
        				  importedPartyLienPaymentDetailsDTO.getAgreementTypeCode().equals(existingPartyLienPaymentDetailsDTO.getAgreementTypeCode())){
        			  flag = false;
        			  if(mergeAdditionalPartyInfo) {
        				  if(null != importedPartyLienPaymentDetailsDTO.getPaymentCategoryCode()
        						  && existingPartyLienPaymentDetailsDTO.getPaymentCategoryCode() == null){
        					  existingPartyLienPaymentDetailsDTO.setPaymentCategoryCode(importedPartyLienPaymentDetailsDTO.getPaymentCategoryCode());
        					  updateRequiredFlag = true;
        				  }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryMethodCode()
                        		  && existingPartyLienPaymentDetailsDTO.getRecoveryMethodCode() == null) {
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryMethodCode(importedPartyLienPaymentDetailsDTO.getRecoveryMethodCode());
                        	  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryPeriodCode()
                        		  && existingPartyLienPaymentDetailsDTO.getRecoveryPeriodCode() == null) {
                       		  existingPartyLienPaymentDetailsDTO.setRecoveryPeriodCode(importedPartyLienPaymentDetailsDTO.getRecoveryPeriodCode());
                       		  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryAmount()
                        		  && existingPartyLienPaymentDetailsDTO.getRecoveryAmount() == null) {
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryAmount(importedPartyLienPaymentDetailsDTO.getRecoveryAmount());
                        	  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryPercentage()
                        		  && existingPartyLienPaymentDetailsDTO.getRecoveryPercentage() == null) {
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryPercentage(importedPartyLienPaymentDetailsDTO.getRecoveryPercentage());
                        	  updateRequiredFlag = true;
                          }
        			  } else {
                          if(null != importedPartyLienPaymentDetailsDTO.getPaymentCategoryCode()
                        		  && (existingPartyLienPaymentDetailsDTO.getPaymentCategoryCode() == null
                        			  || !existingPartyLienPaymentDetailsDTO.getPaymentCategoryCode().equals(
                        					  importedPartyLienPaymentDetailsDTO.getPaymentCategoryCode()))){
                        	  existingPartyLienPaymentDetailsDTO.setPaymentCategoryCode(importedPartyLienPaymentDetailsDTO.getPaymentCategoryCode());
                        	  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryMethodCode()
                        		  && (existingPartyLienPaymentDetailsDTO.getRecoveryMethodCode() == null
                        			  ||  !existingPartyLienPaymentDetailsDTO.getRecoveryMethodCode().equals(
                        					  importedPartyLienPaymentDetailsDTO.getRecoveryMethodCode()))){
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryMethodCode(importedPartyLienPaymentDetailsDTO.getRecoveryMethodCode());
                        	  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryPeriodCode()
                        		  && (existingPartyLienPaymentDetailsDTO.getRecoveryPeriodCode() == null
                        			  || !existingPartyLienPaymentDetailsDTO.getRecoveryPeriodCode().equals(
                        					  importedPartyLienPaymentDetailsDTO.getRecoveryPeriodCode()))){
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryPeriodCode(importedPartyLienPaymentDetailsDTO.getRecoveryPeriodCode());
                        	  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryAmount() 
                        		  && (existingPartyLienPaymentDetailsDTO.getRecoveryAmount() == null
                        				  || existingPartyLienPaymentDetailsDTO.getRecoveryAmount().compareTo(
                        						  importedPartyLienPaymentDetailsDTO.getRecoveryAmount()) != 0 )){
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryAmount(importedPartyLienPaymentDetailsDTO.getRecoveryAmount());
                        	  updateRequiredFlag = true;
                          }
                          if(null != importedPartyLienPaymentDetailsDTO.getRecoveryPercentage()
                        		  && (existingPartyLienPaymentDetailsDTO.getRecoveryPercentage() == null
                        			  || existingPartyLienPaymentDetailsDTO.getRecoveryPercentage().compareTo(
                        					  importedPartyLienPaymentDetailsDTO.getRecoveryPercentage()) != 0 )){
                        	  existingPartyLienPaymentDetailsDTO.setRecoveryPercentage(importedPartyLienPaymentDetailsDTO.getRecoveryPercentage());
                        	  updateRequiredFlag = true;
                          }                          
                      }

        		  }
        	  }
        	  if(flag){
        		  tempPartyLienPaymentDetailsDTO.add(importedPartyLienPaymentDetailsDTO);
        		  updateRequiredFlag = true;
        	  }
          }
          		if(!tempPartyLienPaymentDetailsDTO.isEmpty()){
          			existingPartyLienPaymentDetailsDTOs.addAll(tempPartyLienPaymentDetailsDTO);
          		}
          }
          logger.info("mergePartyLienRestriction update flag status : {}", updateRequiredFlag); 	          
      }

        /**
         * Merger Address Collection
         * @param importedParty
         * @param existingParty
         */
        public void mergeAddressColletionDTO(PartyDTO importedParty, PartyDTO existingParty){
      	  Collection<AddressDTO> tempAddressDTO = new ArrayList<AddressDTO>();
      	  Collection<AddressDTO> newAddressDTOList = new ArrayList<AddressDTO>();
      	  Iterator<AddressDTO> itExistingAddress = null;
            if(null != importedParty.getAddressCollection() && null != importedParty.getAddressCollection().getAddresses() 
          		  && !importedParty.getAddressCollection().getAddresses().isEmpty() && null != existingParty.getAddressCollection()
          		  && null != existingParty.getAddressCollection().getAddresses()  && !existingParty.getAddressCollection().getAddresses().isEmpty()){
            	for(AddressDTO importedAddressDTO:importedParty.getAddressCollection().getAddresses()){
            		int matchCount = 0;
                    boolean flag = true;
                    outerLoop:
                    if(null != importedAddressDTO.getUsages() && !importedAddressDTO.getUsages().isEmpty()) {
                    	
                             	 itExistingAddress  = (existingParty.getAddressCollection().getAddresses() == null)?null:existingParty.getAddressCollection().getAddresses().iterator();                              	 
                                 while(itExistingAddress != null && itExistingAddress.hasNext()){
                                	 AddressDTO existingAddressDTO = itExistingAddress.next();
                                     for(AddressUsageDTO importedAddressUsageDTO:importedAddressDTO.getUsages()){
                                    	 int importedCount = importedAddressDTO.getUsages().size();
                                        for(AddressUsageDTO existingAddressUsageDTO:existingAddressDTO.getUsages()){
                                        	int existingCount = existingAddressDTO.getUsages().size();
                                              //if usage type code and agreement type code matches than update the existing address and exit from inner for loop
                                              if(null != importedAddressUsageDTO.getUsageTypeCd() && null != existingAddressUsageDTO.getUsageTypeCd() && 
                                                  importedAddressUsageDTO.getUsageTypeCd().equals(existingAddressUsageDTO.getUsageTypeCd()) &&
                                                  null != importedAddressUsageDTO.getAgreementTypeCd() && null != existingAddressUsageDTO.getAgreementTypeCd() &&
                                                  importedAddressUsageDTO.getAgreementTypeCd().equals(existingAddressUsageDTO.getAgreementTypeCd())){
                                            	  		matchCount++;
                                          			   	// New logic Added - 04/25/2014 @GR - Sync up usage types from the imported objects.
                                          			   	updateAddressUsage(importedAddressUsageDTO, existingAddressUsageDTO);                                            	  		
                                            	  		//After the address flooding, system would add the usage/agreement type. User would no longer be able to provide additional usage on an address
                                            	  		if(matchCount == importedCount || matchCount == existingCount){
                                            	  			updateAddress(importedAddressDTO,existingAddressDTO);
                                            	  			newAddressDTOList.add(existingAddressDTO);
                                            	  			existingParty.getAddressCollection().getAddresses().remove(existingAddressDTO); 
                                                            flag = false;
                                                            break outerLoop;
                                            	  		}
                                               		}
                                             	}  
                                            }
                             			}
  		                               if(flag){
  		                     				tempAddressDTO.add(importedAddressDTO);
  		                     				updateRequiredFlag = true;
  		                     				break outerLoop; 
  		                     			} 
                                        
                              }   
                    		else if(null != importedParty.getContext()){
                    				for(AddressDTO existingAddressDTO:existingParty.getAddressCollection().getAddresses()){
                    					updateAddress(importedAddressDTO,existingAddressDTO);
                    					//Just merge the first address from the existing party.
                    					break;
                    			}
                    		}	
                       }
         
            	//Add the existing addressUsages that did not have a match
            	
            	if(!existingParty.getAddressCollection().getAddresses().isEmpty()){
            		newAddressDTOList.addAll(existingParty.getAddressCollection().getAddresses());
            	}
            	
            	
                  if(!tempAddressDTO.isEmpty()){
                  	for(AddressDTO tempDto : tempAddressDTO){
                  		if(null != tempDto.getUsages() && !tempDto.getUsages().isEmpty()){
                  			 for(AddressUsageDTO tempAddressUsageDTO:tempDto.getUsages()){
                  				 for(AddressDTO newAddressDTO : newAddressDTOList){
                  					 for(AddressUsageDTO newAddressUsageDTO:newAddressDTO.getUsages()){
                  						//if usage type code and agreement type code matches then expire the usage.
                                           if(null != tempAddressUsageDTO.getUsageTypeCd() && null != newAddressUsageDTO.getUsageTypeCd() && 
                                          		 tempAddressUsageDTO.getUsageTypeCd().equals(newAddressUsageDTO.getUsageTypeCd()) &&
                                               null != tempAddressUsageDTO.getAgreementTypeCd() && null != newAddressUsageDTO.getAgreementTypeCd() &&
                                               tempAddressUsageDTO.getAgreementTypeCd().equals(newAddressUsageDTO.getAgreementTypeCd())){
                                        	   if( newAddressUsageDTO.getEndDateTime() != null ) continue;
                                          	 	newAddressUsageDTO.setEndDateTime(DateUtility.getSystemDateOnly());
                                          	 	updateRequiredFlag = true;
                                            }
                  					 }
                               	}
                  			 }
                  		}
                  	}
                  }
                  	//existingParty.getAddressCollection().setAddresses(new ArrayList<AddressDTO>());
                  	if(!newAddressDTOList.isEmpty()){
                  		tempAddressDTO.addAll(newAddressDTOList);
                  	}
                  	//existingParty.getAddressCollection().getAddresses().addAll(tempAddressDTO);
                    AddressCollectionDTO addressCollectionDTO = new AddressCollectionDTO();
                    addressCollectionDTO.setAddresses(tempAddressDTO);
                    existingParty.setAddressCollection(addressCollectionDTO);

               }else if(null != importedParty.getAddressCollection() && null != importedParty.getAddressCollection().getAddresses() && 
                        !importedParty.getAddressCollection().getAddresses().isEmpty()){
                  AddressCollectionDTO addressCollectionDTO = new AddressCollectionDTO();
                  Collection<AddressDTO> tempAddressDTOs = new ArrayList<AddressDTO>();
                  tempAddressDTOs.addAll(importedParty.getAddressCollection().getAddresses());
                  addressCollectionDTO.setAddresses(tempAddressDTOs);
                  existingParty.setAddressCollection(addressCollectionDTO);
                  updateRequiredFlag = true;
            }
    		logger.info("mergeAddressColletionDTO update flag status : {} ", updateRequiredFlag);            
         }
        
        private void updateAddressUsage(AddressUsageDTO importedAddressUsageDTO, AddressUsageDTO existingAddressUsageDTO){
    		/*Effective - End Dates special handling - They are set only when it is null regardless of the flag.
       		Since these are system dates and would be different everytime on the incoming party data*/
          if(existingAddressUsageDTO.getEffectiveDateTime()== null && importedAddressUsageDTO.getEffectiveDateTime() != null){
        	  existingAddressUsageDTO.setEffectiveDateTime(importedAddressUsageDTO.getEffectiveDateTime());
                updateRequiredFlag = true;
          }
          
          if(existingAddressUsageDTO.getEndDateTime() == null && importedAddressUsageDTO.getEndDateTime() != null){
        	  existingAddressUsageDTO.setEndDateTime(importedAddressUsageDTO.getEndDateTime());
                updateRequiredFlag = true;
          }	          	
        }
      /**
       * Update Address
       * @param importedAddressDTO
       * @param existingAddressDTO
       */
        private void updateAddress(AddressDTO importedAddressDTO,AddressDTO existingAddressDTO){
        	if(mergeAdditionalPartyInfo) {
        		if(null != importedAddressDTO.getAddressLine1() && existingAddressDTO.getAddressLine1() == null){
        				existingAddressDTO.setAddressLine1(importedAddressDTO.getAddressLine1());
        				updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getAddressLine2() && existingAddressDTO.getAddressLine2() == null){
                      existingAddressDTO.setAddressLine2(importedAddressDTO.getAddressLine2());
                      updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getPostalCode() && existingAddressDTO.getPostalCode() == null){
                      existingAddressDTO.setPostalCode(importedAddressDTO.getPostalCode());
                      updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getPostalServiceRegionId() && existingAddressDTO.getPostalServiceRegionId() == null){
                		existingAddressDTO.setPostalServiceRegionId(importedAddressDTO.getPostalServiceRegionId());
                		updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getCityName() && existingAddressDTO.getCityName() == null){
                		existingAddressDTO.setCityName(importedAddressDTO.getCityName());
                		updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getSubdivisionName() && existingAddressDTO.getSubdivisionName() == null){
                      existingAddressDTO.setSubdivisionName(importedAddressDTO.getSubdivisionName());
                      updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getCountryCode() && existingAddressDTO.getCountryCode() == null){         	
                      existingAddressDTO.setCountryCode(importedAddressDTO.getCountryCode());
                      updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getPostalSvcRegionCode() && existingAddressDTO.getPostalSvcRegionCode() == null){         	
                      existingAddressDTO.setPostalSvcRegionCode(importedAddressDTO.getPostalSvcRegionCode());
                      updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getManualPostalRegionName() && existingAddressDTO.getManualPostalRegionName() == null){                  	
                      existingAddressDTO.setManualPostalRegionName(importedAddressDTO.getManualPostalRegionName());
                      updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getLongitude() && existingAddressDTO.getLongitude() == null){
                    existingAddressDTO.setLongitude(importedAddressDTO.getLongitude());
                    updateRequiredFlag = true;
                }
                
                if(null != importedAddressDTO.getLatitude() && existingAddressDTO.getLatitude() == null){
                	existingAddressDTO.setLatitude(importedAddressDTO.getLatitude());
                	updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getLegalDescriptions() && !importedAddressDTO.getLegalDescriptions().isEmpty()
                		&& (existingAddressDTO.getLegalDescriptions() == null || existingAddressDTO.getLegalDescriptions().isEmpty()) ){
                	existingAddressDTO.setLegalDescriptions(importedAddressDTO.getLegalDescriptions());
                	updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getBuildings() && !importedAddressDTO.getBuildings().isEmpty()
                		&& (existingAddressDTO.getBuildings() == null || existingAddressDTO.getBuildings().isEmpty()) ){
                	existingAddressDTO.setBuildings(importedAddressDTO.getBuildings());
                	updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getProfileStatementList() && !importedAddressDTO.getProfileStatementList().isEmpty()
                		&& (existingAddressDTO.getProfileStatementList() == null || existingAddressDTO.getProfileStatementList().isEmpty() )){
                	existingAddressDTO.setProfileStatementList(importedAddressDTO.getProfileStatementList());
                	updateRequiredFlag = true;
                }
        	} else {
        		if(null != importedAddressDTO.getAddressLine1()){
        			if(existingAddressDTO.getAddressLine1() == null
        					|| !existingAddressDTO.getAddressLine1().equalsIgnoreCase(importedAddressDTO.getAddressLine1())){
        				existingAddressDTO.setAddressLine1(importedAddressDTO.getAddressLine1());
        				updateRequiredFlag = true;
        			}
                }
                if(null != importedAddressDTO.getAddressLine2()){
        			if(existingAddressDTO.getAddressLine2() == null
        					|| !existingAddressDTO.getAddressLine2().equalsIgnoreCase(importedAddressDTO.getAddressLine2())){                	
                      existingAddressDTO.setAddressLine2(importedAddressDTO.getAddressLine2());
                      updateRequiredFlag = true;
        			}
                }
                if(null != importedAddressDTO.getPostalCode()){
                	if(existingAddressDTO.getPostalCode() == null 
                			|| !existingAddressDTO.getPostalCode().equals(importedAddressDTO.getPostalCode())){
                      existingAddressDTO.setPostalCode(importedAddressDTO.getPostalCode());
                      updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getPostalServiceRegionId()){
                	if(existingAddressDTO.getPostalServiceRegionId() == null
                			|| existingAddressDTO.getPostalServiceRegionId().compareTo(importedAddressDTO.getPostalServiceRegionId()) != 0){
                		existingAddressDTO.setPostalServiceRegionId(importedAddressDTO.getPostalServiceRegionId());
                		updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getCityName()){
                	if(existingAddressDTO.getCityName() == null 
                			|| !existingAddressDTO.getCityName().equalsIgnoreCase(importedAddressDTO.getCityName())){
                		existingAddressDTO.setCityName(importedAddressDTO.getCityName());
                		updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getSubdivisionName()){
                	if(existingAddressDTO.getSubdivisionName() == null
                			|| !existingAddressDTO.getSubdivisionName().equalsIgnoreCase(importedAddressDTO.getSubdivisionName()) ){
                      existingAddressDTO.setSubdivisionName(importedAddressDTO.getSubdivisionName());
                      updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getCountryCode()){
                	if(existingAddressDTO.getCountryCode() == null
                			|| !existingAddressDTO.getCountryCode().equalsIgnoreCase(importedAddressDTO.getCountryCode()) ){         	
                      existingAddressDTO.setCountryCode(importedAddressDTO.getCountryCode());
                      updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getPostalSvcRegionCode()){
                	if(existingAddressDTO.getPostalSvcRegionCode() == null
                			|| !existingAddressDTO.getPostalSvcRegionCode().equalsIgnoreCase(importedAddressDTO.getPostalSvcRegionCode()) ){         	
                      existingAddressDTO.setPostalSvcRegionCode(importedAddressDTO.getPostalSvcRegionCode());
                      updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getManualPostalRegionName()){
                	if(existingAddressDTO.getManualPostalRegionName() == null
                			|| !existingAddressDTO.getManualPostalRegionName().equalsIgnoreCase(importedAddressDTO.getManualPostalRegionName()) ){                  	
                      existingAddressDTO.setManualPostalRegionName(importedAddressDTO.getManualPostalRegionName());
                      updateRequiredFlag = true;
                	}
                }
                if(null != importedAddressDTO.getLongitude()){
                    existingAddressDTO.setLongitude(importedAddressDTO.getLongitude());
                    updateRequiredFlag = true;
                }
                
                if(null != importedAddressDTO.getLatitude()){
                	existingAddressDTO.setLatitude(importedAddressDTO.getLatitude());
                	updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getLegalDescriptions() && !importedAddressDTO.getLegalDescriptions().isEmpty()){
                	existingAddressDTO.setLegalDescriptions(importedAddressDTO.getLegalDescriptions());
                	updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getBuildings() && !importedAddressDTO.getBuildings().isEmpty()){
                	existingAddressDTO.setBuildings(importedAddressDTO.getBuildings());
                	updateRequiredFlag = true;
                }
                if(null != importedAddressDTO.getProfileStatementList() && !importedAddressDTO.getProfileStatementList().isEmpty()){
                	existingAddressDTO.setProfileStatementList(importedAddressDTO.getProfileStatementList());
                	updateRequiredFlag = true;
                }
        	}
            existingAddressDTO.setPostalCodeExists(importedAddressDTO.isPostalCodeExists());
            existingAddressDTO.setVerifiedIndicator(importedAddressDTO.isVerifiedIndicator());
            existingAddressDTO.setOverrideIndicator(importedAddressDTO.isOverrideIndicator());
    		logger.info("updateAddress update flag status : {}", updateRequiredFlag);
        }

}
