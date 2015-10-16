package com.client.iip.integration.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.party.ClientClaimPolicyPartyImportProcessor;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportStatusDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyUnitDTO;

/**
 * Policy Import processor to process policy details.
 * 
 * @author sudharsan.sriram
 *
 */
public class PolicyImportProcessor {

	private Logger logger = LoggerFactory.getLogger(PolicyImportProcessor.class);
	
	/**
	 * Process policy details from Policy import.
	 * @param policy ClientPolicyImportWrapperDTO.
	 * @return statusDTO PolicyImportStatusDTO.
	 */
	public PolicyImportStatusDTO processPolicy(ClientPolicyImportWrapperDTO policy){
		
		PolicyImportStatusDTO statusDTO = new PolicyImportStatusDTO();

		try{
			//Setup accept confirmation flag in Thread data context, This will be used by the confirmation validator to accept confirmations.
			IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
			IIPDataContext context = threadCtx.getDataContext()==null?new IIPDataContext():threadCtx.getDataContext();
			threadCtx.setDataContext(context);
			threadCtx.setAcceptAllConfirmations(true);
			logger.info("setting 'acceptConfirmations' flag to true");
			IIPThreadContext.RequestType reqType = IIPThreadContext.RequestType.valueOf("INTERFACE");
			threadCtx.setRequestType(reqType);
			logger.info("setting request type to "+reqType.name());	
			
			Map<Long, PartyDTO> partyMap = new HashMap<Long, PartyDTO>();
			//Create parties and associate participants with party
			if(!policy.isPartiesLoaded()){
				ClientClaimPolicyPartyImportProcessor partyProcessor = new ClientClaimPolicyPartyImportProcessor();
				partyProcessor.createParties(policy, partyMap);
				policy.setPartiesLoaded(true);
				//Associate policy units with party
				associateUnitPartyReference(policy, partyMap);					
			}
			
			if(policy.getPersons() != null && policy.getPersons().isEmpty()){
				policy.setPersons(null);
			}
	

			statusDTO.setImportStatusCd(PolicyImportStatusDTO.IMPORT_COMPLETE);
			statusDTO.setPolicy(policy.getPolicy());
			//reset the confirmations flag
			threadCtx.setAcceptAllConfirmations(false);			
		}
		catch(Exception ex){
			logger.error("Exception occurred during Policy Pull", ex);
			statusDTO.setImportStatusCd(PolicyImportStatusDTO.IMPORT_ERROR);
			throw new IIPCoreSystemException("Exception occurred during Policy Pull " + ex.toString());
		}

		return statusDTO;
	}
	
	/**
	 * Associate Unit Party reference.
	 * @param wrapper ClientPolicyImportWrapperDTO.
	 * @param partyMap Parties associated with policy.
	 */
	private void associateUnitPartyReference(ClientPolicyImportWrapperDTO wrapper, Map<Long, PartyDTO> partyMap) {
		PolicyImportDTO policy = wrapper.getPolicy();
		//Checking if Policy Units are available before traversing the Collection
		if(policy.getUnits() == null) {
			return;
		}

		Collection<PolicyUnitDTO> policyUnits = policy.getUnits();
		
		for(PolicyUnitDTO unit : policyUnits) {
			//PartyId of Unit needs to be set with partyId of person/organization
			if(unit.getPartyId() == null && unit.getObjectId()!=null) {
				Long recordId = Long.valueOf(unit.getObjectId());
				if(recordId != null){
					if(partyMap.containsKey(recordId)){
						PartyDTO party = partyMap.get(recordId);
						unit.setPartyId(party.getRecordId());
					}else{
						logger.info("Null party returned for participation");
						throw new IIPCoreSystemException(
						"Incorrect recordId for units. Please check your policy xml.");			
					}
					//Reset the recordId for unit
					//unit.setRecordId(null);
				}
			}
		}
	}	
	
}
