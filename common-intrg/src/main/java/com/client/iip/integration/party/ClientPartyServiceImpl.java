package com.client.iip.integration.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fiserv.isd.iip.bc.address.PartyAddressService;
import com.fiserv.isd.iip.bc.address.api.AddressCollectionDTO;
import com.fiserv.isd.iip.bc.address.api.AddressDTO;
import com.fiserv.isd.iip.bc.address.api.AddressUsageDTO;
import com.fiserv.isd.iip.bc.address.api.PartyAddressProfileStatementRequest;
import com.fiserv.isd.iip.bc.party.ClearPartyDuplicateService;
import com.fiserv.isd.iip.bc.party.PartyConstants;
import com.fiserv.isd.iip.bc.party.PartyProfileStatementMergeRequest;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.AddressChannelDTO;
import com.fiserv.isd.iip.bc.party.api.BlockDTO;
import com.fiserv.isd.iip.bc.party.api.DuplicatePartySearchResultComposite;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.bc.party.api.PartyLienRestrictionDTO;
import com.fiserv.isd.iip.bc.party.api.PartyPreferenceCompositeDTO;
import com.fiserv.isd.iip.bc.party.api.PhoneChannelDTO;
import com.fiserv.isd.iip.bc.party.helper.PartyCreditCardHelper;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleDriversDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleService;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleTrafficIncidentDTO;
import com.fiserv.isd.iip.bc.party.role.api.PartyRoleVendorDTO;
import com.fiserv.isd.iip.bc.party.search.DuplicateSearchResult;
import com.fiserv.isd.iip.bc.party.search.PartySearchDAO;
import com.fiserv.isd.iip.bc.party.search.PartySearchServiceImpl;
import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.meta.annotation.ServiceMethod;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.stoneriver.iip.entconfig.base.tool.AgreementDataDTO;
import com.stoneriver.iip.entconfig.base.tool.AgreementDataValueDTO;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementItem;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementRequest;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionAssociationDTO;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionService;
import com.stoneriver.iip.notepad.model.NotepadContextDataRequest;
import com.stoneriver.iip.notepad.model.NotepadDTO;
import com.stoneriver.iip.notepad.model.NotepadQueryResult;
import com.stoneriver.iip.notepad.model.NotepadTextDTO;
import com.stoneriver.iip.notepad.service.NotepadService;

@Service(id="integration.serviceObject.ClientPartyService")
public class ClientPartyServiceImpl implements ClientPartyService {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientPartyServiceImpl.class);

	private PartyCreditCardHelper partyCreditCardHelper;
	
	private static final String MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE = "mprc";
	private static final String MAX_VENDOR_RESULTS_SYSTEM_OPTION_CODE = "mvrc";
	
	// System option code 
	private static final String INCLUDE_VENDOR_BLOCK_CODE = "incl_block_vend";
	private static final String INCLUDE_CUSTOMER_BLOCK_CODE = "incl_block_cust";	

	private PartySearchDAO partySearchDao;
	private ClientPartyDAO clientPartyDao;
	private SystemOptionService systemOptionService;
	
	private LogicalDataSource lds;
	
	
	/**
	 * @return the lds
	 */
	public LogicalDataSource getLds() {
		return lds;
	}
	/**
	 * @param logicalDS the lds to set
	 */
	@Inject(PojoRef="partyLogicalDataSource")
	public void setLds(LogicalDataSource logicalDS) {
		this.lds = logicalDS;
	}
	
	/**
	 * @return the systemOptionService
	 */
	public SystemOptionService getSystemOptionService() {
		if(systemOptionService == null){
			systemOptionService =MuleServiceFactory.getService(SystemOptionService.class);
		}
		return systemOptionService;
	}
	
	
	/**
	 * @return the PartyCreditCardHelper
	 */
	public PartyCreditCardHelper getPartyCreditCardHelper() {
		return partyCreditCardHelper;
	}
	/**
	 * @param helper the PartyCreditCardHelper to set
	 */
	@Inject(PojoRef = "party.service.pojo.partyCreditCardHelper")
	public void setPartyCreditCardHelper(PartyCreditCardHelper helper) {
		this.partyCreditCardHelper = helper;
	}
	
	@ServiceMethod
	public Collection<ClientPartySearchResult> retrieveVendors(VendorExportRequest request) {
		SystemOptionAssociationDTO option = null;	
		try{
			option = MuleServiceFactory.getService(
				SystemOptionService.class).retrieveSystemOptionValue(
						MAX_VENDOR_RESULTS_SYSTEM_OPTION_CODE);
		}catch(Exception ex){
			//Fetch from max customer search threshold
			option = MuleServiceFactory.getService(
					SystemOptionService.class).retrieveSystemOptionValue(
							MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE);			
		}
		//Default to 500 if not configured.
		int maxResults = Integer.valueOf(option==null?"500":option.getStringValue());
		
		//Obtain All Party Events
		Collection<String> partyEvents = new ArrayList<String>();
		partyEvents.add("party_addpers");
		partyEvents.add("party_updpers");
		partyEvents.add("party_addorg");
		partyEvents.add("party_updorg");

		
        Collection<String> roles = new ArrayList<String>();
        roles.add(PartyConstants.ROLE_VENDOR);
       
		Collection<ClientPartySearchResult> partySearchResults = new ArrayList<ClientPartySearchResult>();
		ClientPartySearchCriteria criteria = new ClientPartySearchCriteria();
		//criteria.setPartyIdsFilter(partyIds);
		criteria.setPartyEventsFilter(partyEvents);
		criteria.setEventStartDate(DateUtility.toSQLTimestamp(request.getStartDate()));
		criteria.setEventEndDate(DateUtility.toSQLTimestamp(new Date()));
        criteria.setRolesFilter(roles);
        criteria.setIncludeAddress(true);
        criteria.setIncludeName(true);
        criteria.setIncudeLien(true);
        criteria.setIncludeCommunication(true);
        criteria.setIncludeTaxInfo(true);
        criteria.setIncludeProfileName("Registration Number");
        //Set the Vendor Extract Threshold
        //Modified to have consistent behavior - Send Error Response when results exceed the threshold - 11/04/2014 @GR
        //criteria.setSearchResultsCount(maxResults);
        // 11/18/2013 - @GR - Party Blocks Enhancement - Fetch all blocked vendors
        criteria.setIncludeBlockedVendors(true);
        
        partySearchResults = getClientPartyDao().search(criteria);
        
        IIPObjectError iipObjError;
	
		if (partySearchResults.size() > maxResults) {
			IIPCoreSystemException excp = new IIPCoreSystemException();
			excp.setErrorCode("SearchResultExceedsLimit");
			Object[] errorArgs = {Long.valueOf(maxResults)};
			excp.setErrorArgs(errorArgs);
			excp.setPayload(criteria);
			
			iipObjError = new IIPObjectError(
    				PartySearchServiceImpl.class.getName(), "search", null,
    				new String[]{"SearchResultExceedsLimit"}, errorArgs, MessageConstants.SEVERITY_INFO);
			excp.setError(iipObjError);
			
			throw excp;
		}
		
		//Enrich the response for Profile name attribute
		for(ClientPartySearchResult result:partySearchResults){
			if(result.getProfileValue() != null){
				ClientProfileDTO tmpProfile = new ClientProfileDTO();
				tmpProfile.setProfileName(result.getProfileName());
				tmpProfile.setProfileValue(result.getProfileValue());
				result.setProfile(tmpProfile);
			}
			
			if(result.isBlockExists()){
				//Fetch the Block Type
				Collection<BlockDTO> blocks = getClientPartyDao().retrievePartyBlocks(result.getPartyId());
				if(blocks != null && !blocks.isEmpty()){
					result.setBlockType(blocks.iterator().next().getBlockTypeCode());
				}
			}
		}
		
		return partySearchResults;
	}
	
	@ServiceMethod
	public PartyDTO retrievePartyDetails(Long partyId){
		PartyDTO returnParty = null;
		if(partyId != null) {
			returnParty = MuleServiceFactory.getService(PartyService.class).retrievePersonOrOrganization(partyId);
			
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
			
			AddressCollectionDTO addressCollection = MuleServiceFactory.getService(PartyAddressService.class).retrieveAddressCollection(partyId);
			returnParty.setAddressCollection(addressCollection);
			
			//Add Party Roles and Liens
			Collection<PartyRoleDTO> roles = MuleServiceFactory.getService(PartyRoleService.class).retrievePartyRoles(partyId);
			
			//06/17/2013 @GR - Fetch Role Profiles
			 for(PartyRoleDTO role : roles) {
				 
				 if(role instanceof PartyRoleVendorDTO){
					 PartyProfileStatementMergeRequest pstmtmreq = new PartyProfileStatementMergeRequest();
					 pstmtmreq.setPartyId(partyId);
					 ProfileStatementRequest pstmtreq = new ProfileStatementRequest();
					 pstmtreq.setAreaCode("role");
					 pstmtreq.setSubAreaCode("vend_role");
					 pstmtmreq.setProfileStatementRequest(pstmtreq);
					 
					 Collection<ProfileStatementItem> profileResponse = MuleServiceFactory.getService(PartyService.class).mergeProfileStatementResponse(pstmtmreq);
					 
					 ((PartyRoleVendorDTO) role).setProfileStatements(profileResponse);
					 
				 }
				 
				 if(role instanceof PartyRoleDriversDTO){
					 PartyProfileStatementMergeRequest pstmtmreq = new PartyProfileStatementMergeRequest();
					 pstmtmreq.setPartyId(partyId);
					 ProfileStatementRequest pstmtreq = new ProfileStatementRequest();
					 pstmtreq.setAreaCode("role");
					 pstmtreq.setSubAreaCode("driver_role");
					 pstmtmreq.setProfileStatementRequest(pstmtreq);
					 
					 Collection<ProfileStatementItem> profileResponse = MuleServiceFactory.getService(PartyService.class).mergeProfileStatementResponse(pstmtmreq);
					 
					 ((PartyRoleDriversDTO) role).setProfileStatements(profileResponse);
					 
					 Collection<PartyRoleTrafficIncidentDTO> trafficIncidentColl= ((PartyRoleDriversDTO)role).getTrafficIncidentsColl();
					 
						if(trafficIncidentColl != null && !trafficIncidentColl.isEmpty()) {
							for(PartyRoleTrafficIncidentDTO partyRoleTrafficIncidentDTO : trafficIncidentColl){
								 PartyProfileStatementMergeRequest psmreq = new PartyProfileStatementMergeRequest();
								 psmreq.setPartyId(partyId);
								 ProfileStatementRequest psreq = new ProfileStatementRequest();
								 psreq.setAreaCode("role");
								 psreq.setSubAreaCode("driver_role");
								 psreq.setSubAreaCategoryCode("drvr_acdnt");
								 psmreq.setProfileStatementRequest(psreq);
								 
								 Collection<ProfileStatementItem> pResponse = MuleServiceFactory.getService(PartyService.class).mergeProfileStatementResponse(psmreq);								
								 partyRoleTrafficIncidentDTO.setProfileStatements(pResponse);
								
							}
						}
					}
			 }
			
			Collection<PartyLienRestrictionDTO> retrievedLiens = MuleServiceFactory.getService(PartyService.class).retrievePartyLiensRestrictions(partyId);
	        returnParty.setPartyRoleCollection(roles);
	        returnParty.setLiensRestrictions(retrievedLiens);

	        //Add Party Preferences
	        PartyPreferenceCompositeDTO partyPreference = null;
	        if(returnParty.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_PERSON)){
	        	partyPreference = MuleServiceFactory.getService(PartyService.class).retrievePersonPreference(partyId);
	        }
	        
	        if(returnParty.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)){
	        	partyPreference = MuleServiceFactory.getService(PartyService.class).retrieveOrganizationPreference(partyId);
	        }
	        returnParty.setPartyPreference(partyPreference);
	        
	        //Add Notes
	        NotepadContextDataRequest notereq = new NotepadContextDataRequest();
	        notereq.setNoteContextId(partyId);
	        notereq.setAgreementTypeCd(PartyConstants.PARTY_CONTEXT);
	        NotepadService noteService = MuleServiceFactory.getService(NotepadService.class);
	        Collection<NotepadQueryResult> noteResults = noteService.searchNotes(notereq);
	        Collection<NotepadDTO> noteCollection = new ArrayList<NotepadDTO>();
	        for(NotepadQueryResult result : noteResults) {
	        	NotepadDTO note = new NotepadDTO();
	        	note = noteService.retrieveNotepad(result.getNoteId());
	        	for(NotepadTextDTO notepadText : note.getNotepadText()){
	        		notepadText.setNoteText(StringEscapeUtils.escapeXml(notepadText.getNoteText()));
	        	}

	        	noteCollection.add(note);
	        }
	        
	        if(returnParty.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_PERSON)){
	        	ClientPersonDTO clientPerson = new ClientPersonDTO();
	        	BeanUtils.copyProperties(returnParty, clientPerson);
	        	clientPerson.setNotes(noteCollection);
	        	returnParty = clientPerson;
	        }
	        
	        if(returnParty.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)){
	        	ClientOrganizationDTO clientOrganization = new ClientOrganizationDTO();
	        	BeanUtils.copyProperties(returnParty, clientOrganization);
	        	clientOrganization.setNotes(noteCollection);
	        	returnParty = clientOrganization;
	        }

		}
        return returnParty;

	}
	
	@ServiceMethod
	public PartyDTO saveParty(PartyDTO party){
		PartyDTO returnParty = null;
		NotepadService notepadService = MuleServiceFactory.getService(NotepadService.class);
		boolean updateRequired = true;
		PartyDTO existingParty = null;
		party.setRecordId(party.getRecordId() != null && party.getRecordId().compareTo(new Long(0)) == 0 ? null :party.getRecordId());
		if(party instanceof ClientPersonDTO){
			
			//Use Party Dup check to get the party record Id if recordId is null.
			if(party.getRecordId() == null){
				DuplicateSearchResult searchResult = new ClientClaimPolicyPartyImportProcessor().searchPersonDuplicates((ClientPersonDTO)party);
				if (searchResult != null) { party.setRecordId(searchResult.getPartyId()); }
			}
			//If Version is provided then it is a full image. No need to merge since it is an explicit update.
			if (party.getRecordId() != null && party.getVersion() == null) {
				existingParty = retrievePartyDetails(party.getRecordId());
				updateRequired = (new ClientPartyMerger()).mergePersonDTO((ClientPersonDTO)party, (ClientPersonDTO)existingParty);
				party = existingParty;
			}else{
				//Check for duplicate Name, address and communication usages for new Party or party image update
				ClientPartyValidator.getInstance().partyAttributeDupsFound(party);
			}
			
			if(updateRequired){
				returnParty = MuleServiceFactory.getService(PartyService.class).savePerson((ClientPersonDTO)party);
			}else{
				returnParty = existingParty;
			}
			((ClientPersonDTO)party).setMergeAdditionalPartyInfoOnly(false);

			Collection<NotepadDTO> notes = ((ClientPersonDTO)party).getNotes();
			if(notes != null){
				for(NotepadDTO notepadDTO : notes){
					if(notepadDTO.getRecordId() == null){
						notepadDTO.setAgreementIdDerived(returnParty.getRecordId());
						notepadDTO.setAgreementTypeCodeDerived(PartyConstants.PARTY_CONTEXT);
						Collection<AgreementDataDTO> agreementData = new ArrayList<AgreementDataDTO>();
						agreementData.add(createAgreementDataValue("party_id", returnParty.getRecordId().toString()));
						notepadDTO.setAgreementData(agreementData);
					}					
					notepadService.saveNotepad(notepadDTO);
				}
			}
		}
		
		if(party instanceof ClientOrganizationDTO){
			
			//Use Party Dup check to get the party record Id if recordId is null.
			if(party.getRecordId() == null){
				DuplicateSearchResult searchResult = new ClientClaimPolicyPartyImportProcessor().searchOrganizationDuplicates((ClientOrganizationDTO)party);
				if (searchResult != null) { party.setRecordId(searchResult.getPartyId()); }
			}			

			//If Version is provided then it is a full image. No need to merge since it is an explicit update.			
			if(party.getRecordId() != null && party.getVersion() == null) {
				existingParty = retrievePartyDetails(party.getRecordId());
				updateRequired = (new ClientPartyMerger()).mergeOrganizationDTO((ClientOrganizationDTO)party, (ClientOrganizationDTO)existingParty);
				party = existingParty;
			}else{
				//Check for duplicate Name, address and communication usages for new Party or party image update
				ClientPartyValidator.getInstance().partyAttributeDupsFound(party);
			}
			
			if(updateRequired){
				returnParty = MuleServiceFactory.getService(PartyService.class).saveOrganization((ClientOrganizationDTO)party);
			}else{
				returnParty = existingParty;
			}
			
			((ClientOrganizationDTO)party).setMergeAdditionalPartyInfoOnly(false);
			
			Collection<NotepadDTO> notes = ((ClientOrganizationDTO)party).getNotes();
			if(notes != null){
				for(NotepadDTO notepadDTO : notes){
					if(notepadDTO.getRecordId() == null){
						notepadDTO.setAgreementIdDerived(returnParty.getRecordId());
						notepadDTO.setAgreementTypeCodeDerived(PartyConstants.PARTY_CONTEXT);
						Collection<AgreementDataDTO> agreementData = new ArrayList<AgreementDataDTO>();
						agreementData.add(createAgreementDataValue("party_id", returnParty.getRecordId().toString()));
						notepadDTO.setAgreementData(agreementData);
					}
					notepadService.saveNotepad(notepadDTO);
				}
			}
		}
		
		/**
		 * 02/28/2013 @gradhak - Performance Fix - Decorate the response to sync up with Customer Detail Response
		 */
		AddressCollectionDTO addressCollection = returnParty.getAddressCollection();
		if ((addressCollection != null) && (CollectionUtils.isNotEmpty(addressCollection.getAddresses()))) {

			for (AddressDTO addressDTO : addressCollection.getAddresses()) {

				// Remove Usage Type Value
				for (AddressDTO addrDTO : addressCollection.getAddresses()) {

					for (AddressUsageDTO usageDTO : addrDTO.getUsages()) {
						usageDTO.setUsageTypeValue(null);
					}

				}
				// Build a request object to retrieve the Profile Statements
				// associated with the address
				PartyAddressProfileStatementRequest request = new PartyAddressProfileStatementRequest();

				if (party instanceof ClientPersonDTO) {
					request.setAreaCode("pers");
					request.setSubAreaCode("addr_pers");
				} else {
					request.setAreaCode("org");
					request.setSubAreaCode("addr_org");
				}
				request.setAddressId(addressDTO.getRecordId());
				Collection<ProfileStatementItem> profileStatements = MuleServiceFactory.getService(
						PartyAddressService.class).retrieveMergedAddressProfileStatement(request);

				addressDTO.setProfileStatementList(profileStatements);
			}
		}
		
		//Add Party Roles
		Collection<PartyRoleDTO> roles = MuleServiceFactory.getService(PartyRoleService.class).retrievePartyRoles(returnParty.getRecordId());
		
        returnParty.setPartyRoleCollection(roles);	
        
        //Add Credit card details
        getPartyCreditCardHelper().setCreditCardDetails(returnParty.getCreditCards());
        
        //Add Party Preferences
        PartyPreferenceCompositeDTO partyPreference = null;
        if(returnParty.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_PERSON)){
        	partyPreference = MuleServiceFactory.getService(PartyService.class).retrievePersonPreference(returnParty.getRecordId());
        }
        
        if(returnParty.getPartyTypeCode().equals(PartyConstants.PARTY_TYPE_ORGANIZATION)){
        	partyPreference = MuleServiceFactory.getService(PartyService.class).retrieveOrganizationPreference(returnParty.getRecordId());
        }
        
        returnParty.setPartyPreference(partyPreference); 
        
        //Set InteractionChannelAttribute
        for (PhoneChannelDTO phoneChannelDTO : returnParty.getPartyInteractionChannelCompositeDTO().getPhoneChannels()) {
				phoneChannelDTO.setOldPhoneNumber(phoneChannelDTO.getPhoneNumber());
			}
        for (AddressChannelDTO emailChannelDTO : returnParty.getPartyInteractionChannelCompositeDTO().getEmailChannels()) {
				emailChannelDTO.setOldInteractionChannelTypeAddress(emailChannelDTO.getInteractionChannelTypeAddress());
			}
        for (AddressChannelDTO otherChannelDTO : returnParty.getPartyInteractionChannelCompositeDTO().getOtherChannels()) {
				otherChannelDTO.setOldInteractionChannelTypeAddress(otherChannelDTO.getInteractionChannelTypeAddress());
			}
        
        //Add Liens
		Collection<PartyLienRestrictionDTO> retrievedLiens = MuleServiceFactory.getService(PartyService.class).retrievePartyLiensRestrictions(returnParty.getRecordId());
        returnParty.setLiensRestrictions(retrievedLiens);        
       

        return returnParty;
	}
	
	private AgreementDataDTO createAgreementDataValue(String code, String value) {
		AgreementDataDTO agreementData = new AgreementDataDTO();
		agreementData.setAgreementDataCd(code);
		Collection<AgreementDataValueDTO> agreementDataValue = new ArrayList<AgreementDataValueDTO>();
		AgreementDataValueDTO agreementDataValueDTO = new AgreementDataValueDTO();
		agreementDataValueDTO.setAgreementDataValue(value);
		agreementDataValue.add(agreementDataValueDTO);
		
		agreementData.setAgreementDataValue(agreementDataValue);
		
		return agreementData;
	}	
	
	
	/**
	 * return DAO object for customer search.
	 * @return PartySearchDAO
	 */
	public PartySearchDAO getPartySearchDao() {
		return partySearchDao;
	}

	/**
	 * Set PartySearchDAO object.
	 * @param value PartySearchDAO.
	 */
	@Inject(DaoRef = "party.daointerface.partySearchDao")
	public void setPartySearchDao(PartySearchDAO value) {
		this.partySearchDao = value;
	}	


	/**
	 * return DAO object for customer search.
	 * @return ClientPartySearchDAO.
	 */
	public ClientPartyDAO getClientPartyDao() {
		return clientPartyDao;
	}

	/**
	 * Set ClientPartySearchDAO object.
	 * @param value ClientPartySearchDAO.
	 */
	@Inject(DaoInterface="client.daointerface.clientPartyDao")
	public void setClientPartyDao(ClientPartyDAO value) {
		this.clientPartyDao = value;
	}
	
	/**
	 * Method to invoke Party search functionality.
	 * @param criteria PartySearchCriteria.
	 * @return Collection PartySearchResult.
	 */

	@ServiceMethod
	public Collection<ClientPartySearchResult> searchParty(
			ClientPartySearchCriteria criteria) {

		
		if ((criteria.getFirstNameFilter()!=null && criteria.getFirstNameFilter().trim().length()==0) 
				|| (criteria.getPartyTypeFilter() != null && criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_ORGANIZATION))) {
			criteria.setFirstNameFilter(null);
		}
		
		if ((criteria.getMiddleNameFilter()!=null && criteria.getMiddleNameFilter().trim().length()==0) 
				|| (criteria.getPartyTypeFilter() != null && criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_ORGANIZATION))) {
			criteria.setMiddleNameFilter(null);
		}
		
		if ((criteria.getLastNameFilter()!=null && criteria.getLastNameFilter().trim().length()==0) 
				|| (criteria.getPartyTypeFilter() != null && criteria.getPartyTypeFilter().equals(PartyConstants.PARTY_TYPE_ORGANIZATION))) {
			criteria.setLastNameFilter(null);
		}
		
		int maxResults = 0;
		
		SystemOptionAssociationDTO option = MuleServiceFactory.getService(
				SystemOptionService.class).retrieveSystemOptionValue(
						MAX_SEARCH_RESULTS_SYSTEM_OPTION_CODE);
		if(option != null && option.getStringValue() != null){
			maxResults = Integer.valueOf(option.getStringValue());
		}
		
		// Get the system option value for customer block and vendor block.
		List<String> sysOptCodes = new ArrayList<String>();
		sysOptCodes.add(INCLUDE_CUSTOMER_BLOCK_CODE);
		sysOptCodes.add(INCLUDE_VENDOR_BLOCK_CODE);
		Map<String, SystemOptionAssociationDTO> sysOptions = getSystemOptionService().retrieveSystemOptionCollection(sysOptCodes);
		
		boolean customerBlockEnabled = sysOptions.get(INCLUDE_CUSTOMER_BLOCK_CODE).getBooleanValue();
		boolean vendorBlockcEnabled = sysOptions.get(INCLUDE_VENDOR_BLOCK_CODE).getBooleanValue();
		
		criteria.setIncludeBlockedCustomers(true);
		criteria.setIncludeBlockedVendors(true);
		
		criteria.setMaxSearchResults(new Long(maxResults));
		//Performance Fix - LocationSearch should be false
		criteria.setLocationSearch(false);
		
		//Fix for DE7125
		
		//To find partyCount
		Collection<Long> partyCount = getPartySearchDao().findPartyCount(criteria);
		
		IIPObjectError iipObjError;
		
		Long actualPartyCount= 0L;
		
		//To find actualPartyCount
		if(partyCount != null && !partyCount.isEmpty()){	
			
			Iterator<Long> iterator = partyCount.iterator();
			while(iterator.hasNext()) {
				actualPartyCount = actualPartyCount + iterator.next();
			}
		}
				
		if (actualPartyCount == 0L) {
			//To throw SearchResultsEmpty error notification.
			IIPCoreSystemException ex = new IIPCoreSystemException();
            ex.setErrorCode(PartyConstants.SEARCH_RESULTS_EMPTY);
            ex.setPayload(criteria);
            
            iipObjError = new IIPObjectError(
    				PartySearchServiceImpl.class.getName(), "search", null,
    				new String[]{"SearchResultsEmpty"}, null, MessageConstants.SEVERITY_INFO);
			ex.setError(iipObjError);
			
            throw ex;
		}else {
			//To throw error notification if result exceeds limit.		
			if (actualPartyCount.intValue() > maxResults) {
				IIPCoreSystemException excp = new IIPCoreSystemException();
				excp.setErrorCode("SearchResultExceedsLimit");
				Object[] errorArgs = {Long.valueOf(maxResults)};
				excp.setErrorArgs(errorArgs);
				excp.setPayload(criteria);
			
				iipObjError = new IIPObjectError(
		    			PartySearchServiceImpl.class.getName(), "search", null,
		    			new String[]{"SearchResultExceedsLimit"}, errorArgs, MessageConstants.SEVERITY_INFO);
				excp.setError(iipObjError);
					
				throw excp;
			}
		}
		//End of fix for DE7125				

		
		Collection<ClientPartySearchResult> results = getClientPartyDao().search(criteria);
		
		Collection<ClientPartySearchResult> actualResults = removeBlockPartyResults(results, customerBlockEnabled, vendorBlockcEnabled);
		
	
		//Agument the response for Profile name attribute
		for(ClientPartySearchResult actualresult:actualResults){
			if(actualresult.getProfileValue() != null){
				ClientProfileDTO tmpProfile = new ClientProfileDTO();
				tmpProfile.setProfileName(actualresult.getProfileName());
				tmpProfile.setProfileValue(actualresult.getProfileValue());
				actualresult.setProfile(tmpProfile);
			}
		}		

		if(actualResults != null && actualResults.size() == 0){
			IIPCoreSystemException ex = new IIPCoreSystemException();
            ex.setErrorCode(PartyConstants.SEARCH_RESULTS_EMPTY);
            ex.setPayload(criteria);
            
            iipObjError = new IIPObjectError(
    				PartySearchServiceImpl.class.getName(), "search", null,
    				new String[]{"SearchResultsEmpty"}, null, MessageConstants.SEVERITY_INFO);
			ex.setError(iipObjError);
			
            throw ex;			
		}		
		return actualResults;

	}
	
	/**
	 * Remove the result for added blocks and user selection.
	 * 
	 *  Example Table:-
	 * 	partyId 	CustBlockID		VendBlockId		PartyName
	 *  -------		-----------		-----------		---------
	 * 	1000303		1000102			1000103			Block-Cust-Vend, Block
	 * 	1000301		1000101			(null)			Block-Cust, Block
	 * 	1000300		(null)			(null)			Block-Nothing, Block
	 * 	1000302		(null)			1000100			Block-Vend, Block
	 *
	 * 															- Party Result
	 * 10  -  BothOptionTrue 		- None Selected 			- 1000300
	 * 11  -  BothOptionTrue 		- Cust Only Selected		- 1000300, 1000301
	 * 100 -  BothOptionTrue 		- Vend Only Selected		- 1000300, 1000302
	 * 101 -  BothOptionTrue 		- Cust and Vend Selected	- 1000300, 1000301, 1000302, 1000303
	 * 110 -  OnlyCustOptionTrue  	- Not Selected				- 1000300, 1000302
	 * 111 -  OnlyCustOptionTrue  	- Selected					- 1000300, 1000301, 1000302, 1000303
	 * 
	 * @param criteria PartySearchCriteria
	 * @param results Collection of PartySearchResult
	 * @param includeCustomerBlock boolean
	 * @param includeVendorBlock boolean
	 * @return Collection of PartySearchResult
	 */
	private Collection<ClientPartySearchResult> removeBlockPartyResults(Collection<ClientPartySearchResult> results, 
		boolean customerBlockEnabled, boolean vendorBlockEnabled){
		Map<String, String> partyTypes = new HashMap<String, String>();
		partyTypes.put(PartyConstants.PARTY_TYPE_PERSON, "Person");
		partyTypes.put(PartyConstants.PARTY_TYPE_ORGANIZATION, "Organization");
		
		Collection<ClientPartySearchResult> actualResult = new ArrayList<ClientPartySearchResult>();
		
		Outer:
		for (ClientPartySearchResult result : results) {
			
			if(result.isBlockExists()){
				//Fetch the Block Type
				Collection<BlockDTO> blocks = getClientPartyDao().retrievePartyBlocks(result.getPartyId());
				if(vendorBlockEnabled || customerBlockEnabled){
					for(BlockDTO block: blocks){
						if(block.getBlockTypeCode().equals(PartyConstants.BLOCK_TYPE_VEND_BLK) && vendorBlockEnabled
								|| (block.getBlockTypeCode().equals("party_blk") && customerBlockEnabled))
							continue Outer;
					}
				}
				//If you are here then None of the Blocks matched the criteria, Take the first Block.
				if(blocks != null && !blocks.isEmpty()){
					result.setBlockType(blocks.iterator().next().getBlockTypeCode());
				}				
			}
				result.setPartyTypeName(partyTypes.get(result.getPartyTypeCode()));
				actualResult.add(result);
		}
		return actualResult;
	}	
	
	@ServiceMethod
	public DuplicatePartySearchResultComposite duplicatePartySearch(PartyDTO party) {
		return MuleServiceFactory.getService(ClearPartyDuplicateService.class).searchDuplicateParties(party, false /*fastReturn*/, true /*includeBlocks*/);
	}
	
	
}
