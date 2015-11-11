package com.client.iip.integration.batch;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import com.client.iip.integration.financials.disbursement.ClientDisbursementService;
import com.fiserv.isd.iip.bc.financials.generalledger.batch.export.GeneralLedgerExportBatchDTO;
import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

public class GeneralLedgerExportProcessor implements ItemProcessor<DataTransferObject, Collection<GeneralLedgerExportBatchDTO>> {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneralLedgerExportProcessor.class);
	
	protected StepExecution stepExecution; 
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)
	 * Sort the Files before Job Step
	 */
	@BeforeStep
	public void beforeStep(StepExecution _stepExecution) {
		this.stepExecution = _stepExecution;
	}
	

	@Override
	public Collection<GeneralLedgerExportBatchDTO> process(
			DataTransferObject item) throws Exception {
		Collection<GeneralLedgerExportBatchDTO> results = new ArrayList<GeneralLedgerExportBatchDTO>();
		try{
			logger.info("Entering GLExportBatchTask");		

			results = MuleServiceFactory.getService(ClientDisbursementService.class).exportGL();

			logger.info("Number of GL exported successfuly = {}", results.size());
			
			//Set step runtime execution stats.(Dummy Reader size is added to context count, subtract one from it)
			stepExecution.setReadCount(results.size() - 1);
			stepExecution.setWriteCount(results.size()==0?results.size():results.size()-1);
			//Set to null to suppress dummy export file.
			if(results.size() == 0) results = null;
		}catch(Exception ex){
			logger.error("Failure when processing GLExport", ex);
			BatchUtils.writeIntoBatchLog(stepExecution, "clm", "GLExport processing Failure : " , ex);
			//throw new Exception("GLExport processing Failure : ", ex);
		}
		
		return results;
	}	

}
