package com.client.iip.integration.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;

import com.client.iip.integration.claim.details.ClientClaimDetailsService;
import com.client.iip.integration.claim.imports.ClaimImportCompositeDTO;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.details.ClaimDetailRequestCriteria;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;

public class ClaimExportProcessor implements ItemProcessor<ClaimSearchResultDTO, ClaimImportCompositeDTO> {

	private static final Log logger = LogFactory.getLog(ClaimExportProcessor.class);
	
	@javax.annotation.Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;	

	/**
	 * Process the provided item, returning a potentially modified or new item for continued
	 * processing.  If the returned result is null, it is assumed that processing of the item
	 * should not continue.
	 * 
	 * @param ClaimSearchResult to be processed
	 * @return ClaimImportCompositeDTO for continued processing, null if processing of the 
	 *  provided item should not continue.
	 */
	public ClaimImportCompositeDTO process(ClaimSearchResultDTO result) throws Exception{
		ClaimImportCompositeDTO claimDTO = new ClaimImportCompositeDTO();
		try{
				ClaimDetailRequestCriteria request = new ClaimDetailRequestCriteria();
				request.setClaimNumber(result.getClaimNumber());
				//Obtain the full claim image		
				claimDTO = MuleServiceFactory.getService(ClientClaimDetailsService.class).retrieveClaimDetails(request);
				logger.info("After getting Claim detail : " + claimDTO.getClaimId());
				
		  }catch(Exception ex){
				logger.error("Exception occurred while processing claim : " + result.getClaimNumber(), ex);
				BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm", "Failure when processing claim : " + result.getClaimNumber(),  ex);
				//throw new Exception("Failure when processing claim : " + result.getClaimNumber(), ex);
		  }
		return claimDTO;
		
	}	
}
