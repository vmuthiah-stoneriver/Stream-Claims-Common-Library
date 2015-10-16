package com.client.iip.integration.core.util;

/*
 * @author Guru Radhakrishnan
 * 
 * XSL Extension for Generic translations and stream code lookup.
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.sa.ClientClaimCompanyLineOfBusinessInfoDTO;
import com.client.iip.integration.sa.ClientConfigService;
import com.client.iip.integration.sa.UnitDataTypeRoleCategoryDTO;
import com.fiserv.isd.iip.bc.address.PartyAddressService;
import com.fiserv.isd.iip.bc.address.api.AddressUsageDTO;
import com.fiserv.isd.iip.bc.address.api.PostalServiceRegionDetailsDTO;
import com.fiserv.isd.iip.bc.address.api.UsageCollectionDTO;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.lookup.ClmPlcyEndorsementFormCodeLookupDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitCoverageDTO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.sa.api.codes.ProfileStatementCodeDTO;


public class IIPXSLExtension {
	
	private static final Logger logger =
			LoggerFactory.getLogger(IIPXSLExtension.class);
	
	public Map<String, String> participantMap = new HashMap<String, String>();
	
	public AtomicInteger seq = new AtomicInteger();


	public String getCompanyNameFromCode(String companyCode){
		String compName = "None";
		
		if(companyCode != null){
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> compLOBs = 
					MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCompanyLOBInfo(companyCode);
			for(ClientClaimCompanyLineOfBusinessInfoDTO  comp : compLOBs){
				if(comp.getCompId().compareTo(new Long(companyCode)) == 0){
					compName = comp.getCompany();
					break;
				}
			}		
		}
		return compName;
	}
	
	public String getCompanyIdFromName(String companyName){
		String compId = "0";
		
		if(companyName != null){
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> compLOBs = 
					MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCompanyLOBInfo(companyName);
			for(ClientClaimCompanyLineOfBusinessInfoDTO  comp : compLOBs){
				if(comp.getCompany().equals(companyName)){
					compId = Long.toString(comp.getCompId());
					break;
				}
			}		
		}
		return compId;
	}	
	
	public String getCorporationNameFromCode(String corpCode){
		String corpName = "None";
		if(corpCode != null){
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> compLOBs = 
					MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCompanyLOBInfo(corpCode);
			for(ClientClaimCompanyLineOfBusinessInfoDTO corp : compLOBs){
				if(corp.getCorpId().compareTo(new Long(corpCode)) == 0){
					corpName = corp.getCorporation();
					break;
				}
			}
		}
		return corpName;
	}
	
	public String getCorporationIdFromName(String corpName){
		String corpId = "0";
		if(corpName != null){
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> compLOBs = 
					MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCompanyLOBInfo(corpName);
			for(ClientClaimCompanyLineOfBusinessInfoDTO corp : compLOBs){
				if(corp.getCorporation().equals(corpName)){
					corpId = Long.toString(corp.getCorpId());
					break;
				}
			}
		}
		return corpId;
	}	
	
	public String getCompanyLobIdFromNameLob(String compName, String lobCode){
		String companyLobId = "0";
		if(compName != null){
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> compLOBs = 
					MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCompanyLOBInfo(compName);
			for(ClientClaimCompanyLineOfBusinessInfoDTO comp : compLOBs){
				if(comp.getCompany().equals(compName) && comp.getLobCode().equals(lobCode)){
					companyLobId = Long.toString(comp.getCompanyLOBId());
					break;
				}
			}
		}
		return companyLobId;
	}	

	public String getLOBCodeFromCompanyLOBId(String companyLobId){
		String lobCode = "0";
		if(companyLobId != null){
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> compLOBs = 
					MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCompanyLOBInfo(companyLobId);
			for(ClientClaimCompanyLineOfBusinessInfoDTO  comp : compLOBs){
				if(comp.getCompanyLOBId().compareTo(new Long(companyLobId)) == 0){
					lobCode = comp.getLobCode();
					break;
				}
			}		
		}		
		return lobCode;
	}
	
	public String getParticipantRefNumber(String partyKey){
		String refNumber = "0";
		if(participantMap.get(partyKey) == null){
			refNumber = Integer.toString(seq.incrementAndGet());
			participantMap.put(partyKey,refNumber);			
		}else{
			refNumber = participantMap.get(partyKey);
		}
		return refNumber;
	}
	
	public String getJurisdictionIdFromStateCode(String stateCode){
		String jurisdictionId = "0";

		Long postalserviceRegionlId = 
				MuleServiceFactory.getService(PartyAddressService.class).retrievePostalServiceRegionIdByRegionAbrv(stateCode);
		
		postalserviceRegionlId = postalserviceRegionlId==null?new Long(0):postalserviceRegionlId;
		

		if(postalserviceRegionlId.compareTo(new Long(0)) > 0){
			Long jurId = 
					MuleServiceFactory.getService(EnterpriseConfigService.class).retrieveJurisdictionId(postalserviceRegionlId);
			jurisdictionId = Long.toString(jurId==null?0:jurId.longValue());
		}
		
		return jurisdictionId;
	}
	
	public String getPostalServiceRegionIdFromStateCode(String stateCode){
		String postalServiceRegionId = "0";
		Long postalId = 
				MuleServiceFactory.getService(PartyAddressService.class).retrievePostalServiceRegionIdByRegionAbrv(stateCode);
		
		postalServiceRegionId = Long.toString(postalId==null?0:postalId.longValue());
		
		return postalServiceRegionId;		
	}
	
	public String getPostalServiceRegionCodeFromId(String postalId){
		
		if(postalId == null || postalId.equals("")) {
			return "";
		}
		
		String postalServiceRegionCode = "0";
		PostalServiceRegionDetailsDTO postalRegionDTO = 
				MuleServiceFactory.getService(PartyAddressService.class).retrievePostalServiceRegionDetails(new Long(postalId));
		if(postalRegionDTO == null) {
			return "";
		}
		postalServiceRegionCode = postalRegionDTO.getPostalServiceRegionCode();
		
		return postalServiceRegionCode;
	}
	
	public String getProfileStatementIdByName(String profileName, String agreementType){
		String profileStatementId = "0";
		if(profileName != null){
			Collection<ProfileStatementCodeDTO> profiles = MuleServiceFactory.getService(ClientConfigService.class).
														retrieveProfileStatementCodes(agreementType);
			for(ProfileStatementCodeDTO profile:profiles){
				if(profile.getProfileStatementDescription().equals(profileName)){
					profileStatementId = profile.getRecordId().toString();
					break;
				}
			}
		}
		
		return profileStatementId;
		
	}
	
	
	
	public String getPolicyEndorsementCodeByFormNumber(String formNumber, String formSuffix, String lobCode){
		String endorsementCode = "0";
		Collection<ClmPlcyEndorsementFormCodeLookupDTO> endorsements = 
				MuleServiceFactory.getService(ClientConfigService.class).retrieveClmPlcyEndorsementFormInfoByLOB(lobCode);
		for(ClmPlcyEndorsementFormCodeLookupDTO endorsement: endorsements){
			if(endorsement.getFormNumber().equals(formNumber) && endorsement.getFormSuffix().equals(formSuffix)){
				endorsementCode = (String)endorsement.getCode();
				break;
			}
		}
		
		if(endorsementCode.equals("0")){
			logger.info("Policy Endorsement Not Mapped in Stream: " + formNumber + " Suffix: " + formSuffix);
		}
		
		return endorsementCode;			
	}
	
	public String getPolicyEndorsementNameByFormNumber(String formNumber, String formSuffix, String lobCode){
		String endorsementName = "0";
		Collection<ClmPlcyEndorsementFormCodeLookupDTO> endorsements = 
				MuleServiceFactory.getService(ClientConfigService.class).retrieveClmPlcyEndorsementFormInfoByLOB(lobCode);
		for(ClmPlcyEndorsementFormCodeLookupDTO endorsement: endorsements){
			if(endorsement.getFormNumber().equals(formNumber) && endorsement.getFormSuffix().equals(formSuffix)){
				endorsementName = (String)endorsement.getValue();
				break;
			}
		}
		
		if(endorsementName.equals("0")){
			logger.info("Policy Endorsement Not Mapped in Stream: " + formNumber + " Suffix: " + formSuffix);
		}
		
		return endorsementName;			
	}	
	
	public String isUsageContextAvailableForUsageType(String partyType, String usageType, String agreType){
		
		UsageCollectionDTO usageCollection = MuleServiceFactory.getService(PartyAddressService.class).retrieveAddressUsages(partyType, agreType);
		//Check if the usageType(phy_addr, mail_addr,...) is present in the collection.
		for(AddressUsageDTO usage : usageCollection.getUsages()){
			if(usage.getUsageTypeCd().equals(usageType)){
				return "true";
			}
		}
		
		return "false";
	}
	
	public String getUnitDataTypeByUnitLOB(String unitTypeCode, String subTypeCode, String lobCode){
		String unitDataTypeCode = "";
		
		Collection<UnitDataTypeRoleCategoryDTO> units = 
				MuleServiceFactory.getService(ClientConfigService.class).retriveClmPlcyUnitDataTypeRoleCategory(lobCode);
		for(UnitDataTypeRoleCategoryDTO unit: units){
			if(unit.getUnitTypeCode().equals(unitTypeCode) && unit.getUnitSubTypeCode().equals(subTypeCode)){
				unitDataTypeCode = unit.getUnitDataTypeCode();
				break;
			}
		}
		return unitDataTypeCode;			
	}
	
	public String getUnitCategoryByUnitLOB(String unitTypeCode, String subTypeCode, String lobCode){
		String unitCategoryCode = "";
		
		Collection<UnitDataTypeRoleCategoryDTO> units = 
				MuleServiceFactory.getService(ClientConfigService.class).retriveClmPlcyUnitDataTypeRoleCategory(lobCode);
		for(UnitDataTypeRoleCategoryDTO unit: units){
			if(unit.getUnitTypeCode().equals(unitTypeCode) && unit.getUnitSubTypeCode().equals(subTypeCode)){
				unitCategoryCode = unit.getUnitCategoryCode();
				break;
			}
		}
		return unitCategoryCode;			
	}
	
	public String getUnitRoleCategoryByUnitLOB(String unitTypeCode, String subTypeCode, String lobCode){
		String unitRoleCategoryCode = "";
		
		Collection<UnitDataTypeRoleCategoryDTO> units = 
				MuleServiceFactory.getService(ClientConfigService.class).retriveClmPlcyUnitDataTypeRoleCategory(lobCode);
		for(UnitDataTypeRoleCategoryDTO unit: units){
			if(unit.getUnitTypeCode().equals(unitTypeCode) && unit.getUnitSubTypeCode().equals(subTypeCode)){
				unitRoleCategoryCode = unit.getUnitRoleCategoryCode();
				break;
			}
		}
		return unitRoleCategoryCode;			
	}
	
	public String getClaimCoverageCode(String policyCoverageCode){
		ClaimPolicyUnitCoverageDTO coverage =  
				MuleServiceFactory.getService(ClientConfigService.class).retrieveClaimCoverageCodeForPolicyCovCode(policyCoverageCode);
		if(coverage == null){
			logger.info("Policy Coverage code Not Mapped in Stream: " + policyCoverageCode);
		}		
		return coverage == null?"":coverage.getCoverageCd();
	}
	
	public String getSQLSystemTimestamp() {		
		return DateUtility.getSQLSystemTimestamp().toString();
	}
}
