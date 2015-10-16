package com.client.iip.integration.party;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.bc.address.api.AddressDTO;
import com.fiserv.isd.iip.bc.address.api.AddressUsageDTO;
import com.fiserv.isd.iip.bc.party.api.AddressChannelDTO;
import com.fiserv.isd.iip.bc.party.api.InteractionChannelAgreementTypeDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationDTO;
import com.fiserv.isd.iip.bc.party.api.OrganizationNameDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.bc.party.api.PartyInteractionChannelCompositeDTO;
import com.fiserv.isd.iip.bc.party.api.PersonDTO;
import com.fiserv.isd.iip.bc.party.api.PersonNameDTO;
import com.fiserv.isd.iip.bc.party.api.PhoneChannelDTO;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

public class ClientPartyValidator {
	
	private static final ClientPartyValidator validator = new ClientPartyValidator();
	
	private Logger logger = LoggerFactory.getLogger(ClientPartyMerger.class);
	
	public boolean partyAttributeDupsFound(PartyDTO importedParty){
		
		//Check Address Dups
		if(importedParty.getAddressCollection()!= null && checkAddressDups(importedParty.getAddressCollection().getAddresses())){
			IIPCoreSystemException excp = new IIPCoreSystemException();
			excp.setErrorCode("duplicatedEntries");
			Object[] errorArgs = {"AddressDTO"};
			excp.setErrorArgs(errorArgs);
				
			IIPObjectError iipObjError = new IIPObjectError(
					"ClientPartyValidator", "partyAddressDuplicate", null,
	    			new String[]{"duplicatedEntries"}, errorArgs, MessageConstants.SEVERITY_ERROR);
			excp.setError(iipObjError);
				
			throw excp;			
		}
		
		//Check Communication Dups
		if(checkInteractionChannelDups(importedParty.getPartyInteractionChannelCompositeDTO())){
			IIPCoreSystemException excp = new IIPCoreSystemException();
			excp.setErrorCode("duplicatedEntries");
			Object[] errorArgs = {"PartyInteractionChannelCompositeDTO"};
			excp.setErrorArgs(errorArgs);
				
			IIPObjectError iipObjError = new IIPObjectError(
					"ClientPartyValidator", "partyCommunicationDuplicate", null,
	    			new String[]{"duplicatedEntries"}, errorArgs, MessageConstants.SEVERITY_ERROR);
			excp.setError(iipObjError);
				
			throw excp;			
		}
		//Check Name Dups
		if( importedParty instanceof PersonDTO && checkNameDups(((PersonDTO)importedParty).getNames())
			|| (importedParty instanceof OrganizationDTO && checkNameDups(((OrganizationDTO)importedParty).getNames()))	
				){
			
			IIPCoreSystemException excp = new IIPCoreSystemException();
			excp.setErrorCode("duplicatedEntries");
			Object[] errorArgs = {importedParty instanceof PersonDTO?"PersonNameDTO":"OrganizationNameDTO"};
			excp.setErrorArgs(errorArgs);
				
			IIPObjectError iipObjError = new IIPObjectError(
					"ClientPartyValidator", "partyNameDuplicate", null,
	    			new String[]{"duplicatedEntries"}, errorArgs, MessageConstants.SEVERITY_ERROR);
			excp.setError(iipObjError);
				
			throw excp;				
			
		}
		
		return false;
	}
	
	private boolean checkNameDups(Collection<?> nameList){

	//Obtain the count of legal name type codes from the Collection.				
	 int legalNameCount = CollectionUtils.countMatches(nameList, new Predicate() {
	        public boolean evaluate(Object obj) {
	            if(obj instanceof PersonNameDTO 
	            		&& ((PersonNameDTO)obj).getPersonNameExpirationDate() == null 
	            		&& ((PersonNameDTO)obj).getNameTypeCode().equals("lgl_nm")){
	            	return true;
	            }else if(obj instanceof OrganizationNameDTO 
	            		&& ((OrganizationNameDTO)obj).getOrganizationNameExpirationDate() == null 
	            		&&  ((OrganizationNameDTO)obj).getNameTypeCode().equals("lgl_nm")){
	            	return true;
	            }else{
	            	return false;
	            }
	        }
	    });

	 	if(legalNameCount > 1){
			return  true;
		}		
		return false;
	}
	
	private boolean checkAddressDups(Collection<AddressDTO> addressList){

		Map<String, String> duplicateAddressMap = new HashMap<String, String>();
		
		for(AddressDTO address: addressList){
			StringBuffer addressString = new StringBuffer();
			addressString.append(address.getPostalCode());
			addressString.append(address.getAddressLine1());
			addressString.append(address.getAddressLine2());			
			addressString.append(address.getCityName());	
			addressString.append(address.getSubdivisionName());	
			addressString.append(address.getCountryCode());	
			addressString.append(address.getPostalSvcRegionCode());
			for(AddressUsageDTO usage: address.getUsages()){
				if(usage.getEndDateTime() != null)	continue;
				addressString.append(usage.getAgreementTypeCd());
				addressString.append(usage.getUsageTypeCd());
			}
			duplicateAddressMap.put(addressString.toString(), addressString.toString());
		}
		logger.info("Imported Party addressList Size: " + addressList.size());
		logger.info("Unique Address addressList Size: " + duplicateAddressMap.size());		
		if(duplicateAddressMap.size() != addressList.size()){
			return  true;
		}
		
		return false;
	}
	
	
	public boolean checkInteractionChannelDups(PartyInteractionChannelCompositeDTO channelDTO){

		Map<String, String> duplicateChannelMap = new HashMap<String, String>();
		
		//Retrieve the PhoneChannel to compare
		for(PhoneChannelDTO phone: channelDTO.getPhoneChannels()){
			StringBuffer channelString = new StringBuffer();
			channelString.append(phone.getInteractionChannelTypeCode());
			channelString.append(phone.getUsageTypeCode());
			channelString.append(phone.getPhoneNumber());
			for(InteractionChannelAgreementTypeDTO agreementType: phone.getInteractionChannelAgreementTypes()){
				if(agreementType.getEndDate() != null)	continue;
					channelString.append(agreementType.getAgreementTypeCode());
				}
				duplicateChannelMap.put(channelString.toString(), channelString.toString());
			}
		
			logger.info("Imported Party PhoneChannel Size: " + channelDTO.getPhoneChannels().size());
			logger.info("Unique Party PhoneChannel Size: " + duplicateChannelMap.size());		
			if(duplicateChannelMap.size() != channelDTO.getPhoneChannels().size()){
				return true;
			}

		//Clear the Map
		duplicateChannelMap.clear();
		//Retrieve the EmailChannel to compare
		for(AddressChannelDTO addressChannel: channelDTO.getEmailChannels()){
			StringBuffer channelString = new StringBuffer();
			channelString.append(addressChannel.getInteractionChannelTypeCode());
			channelString.append(addressChannel.getUsageTypeCode());
			channelString.append(addressChannel.getInteractionChannelTypeAddress());
			for(InteractionChannelAgreementTypeDTO agreementType: addressChannel.getInteractionChannelAgreementTypes()){
				if(agreementType.getEndDate() != null)	continue;
					channelString.append(agreementType.getAgreementTypeCode());
				}
				duplicateChannelMap.put(channelString.toString(), channelString.toString());			
			}

			logger.info("Imported Party emailChannel Size: " + channelDTO.getEmailChannels().size());
			logger.info("Unique Party emailChannel Size: " + duplicateChannelMap.size());		
			if(duplicateChannelMap.size() != channelDTO.getEmailChannels().size()){
				return true;
			}
		
		//Clear the Map
		duplicateChannelMap.clear();
		//Retrieve the OtherChannel to compare
		for(AddressChannelDTO addressChannel: channelDTO.getOtherChannels()){
			StringBuffer channelString = new StringBuffer();
			channelString.append(addressChannel.getInteractionChannelTypeCode());
			channelString.append(addressChannel.getUsageTypeCode());
			channelString.append(addressChannel.getInteractionChannelTypeAddress());
			for(InteractionChannelAgreementTypeDTO agreementType: addressChannel.getInteractionChannelAgreementTypes()){
				if(agreementType.getEndDate() != null)	continue;
					channelString.append(agreementType.getAgreementTypeCode());
				}
				duplicateChannelMap.put(channelString.toString(), channelString.toString());			
			}

			logger.info("Imported Party otherChannel Size: " + channelDTO.getOtherChannels().size());
			logger.info("Unique Party otherChannel Size: " + duplicateChannelMap.size());		
			if(duplicateChannelMap.size() != channelDTO.getOtherChannels().size()){
				return true;
			}
		
		return false;
	}	
	
	
	public static ClientPartyValidator getInstance(){
		return validator;
	}

}
