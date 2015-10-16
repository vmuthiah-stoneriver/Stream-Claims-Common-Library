package com.client.iip.integration.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;

import com.client.iip.integration.party.ClientPartySearchResult;
import com.client.iip.integration.party.ClientPartyService;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

public class VendorExportProcessor implements ItemProcessor<ClientPartySearchResult, PartyDTO> {
	
	private static final Log logger = LogFactory.getLog(VendorExportProcessor.class);
	
	@javax.annotation.Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;	

	/**
	 * Process the provided item, returning a potentially modified or new item for continued
	 * processing.  If the returned result is null, it is assumed that processing of the item
	 * should not continue.
	 * 
	 * @param PartySearchResult to be processed
	 * @return PartyDTO for continued processing, null if processing of the 
	 *  provided item should not continue.
	 */
	public PartyDTO process(ClientPartySearchResult result) throws Exception{
		PartyDTO partyDTO = new PartyDTO();
		try{
				//Obtain the full party image for the party
				partyDTO = MuleServiceFactory.getService(ClientPartyService.class).retrievePartyDetails(result.getPartyId());
				logger.info("After getting party detail : " + partyDTO.getRecordId());

		  }catch(Exception ex){
				logger.error("Vendor Export Failed: ", ex);
				BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm", "Vendor Export Failure : " , ex);
				//throw new Exception("Vendor Export Failure : ", ex);
		  }
		
		return partyDTO;		
	}
	

}
