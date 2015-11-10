package com.client.iip.integration.batch;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import com.client.iip.integration.financials.disbursement.ClientDisbursementService;
import com.client.iip.integration.financials.disbursement.ClientExportDisbursementsRequestDTO;
import com.client.iip.integration.financials.disbursement.ClientExportDisbursementsResultDTO;
import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

/**
 * The PaymentExportWriter will invoke the payment service to add additional information, call
 * the Mule Endpoint to convert to XML and drop it into the Configured File Path
 * @author gradhak
 */
public class PaymentExportProcessor implements ItemProcessor<DataTransferObject, ClientExportDisbursementsResultDTO> {

	private static final Logger logger = LoggerFactory.getLogger(PaymentExportProcessor.class);
	
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
	public ClientExportDisbursementsResultDTO process(
			DataTransferObject item) throws Exception {
		try{
			logger.info("Entering PaymentExportBatchTask");		
			//Fetch Job Parameters.
			Date endDate = stepExecution.getJobParameters().getDate("runDate");
			ClientExportDisbursementsRequestDTO requestDTO = new ClientExportDisbursementsRequestDTO();
			requestDTO.setEndDate(endDate);
			ClientExportDisbursementsResultDTO results = 
						MuleServiceFactory.getService(ClientDisbursementService.class).export(requestDTO);

			logger.info("Number of disbursements processed successfuly = {}", results.getTotalRecordCount());
			
			//Set step runtime execution stats.
			stepExecution.setReadCount(results.getTotalRecordCount().intValue());
			
			//Set step runtime execution stats.
			stepExecution.setWriteCount(results.getPaymentList()==null?0:results.getPaymentList().size());
			
			//set results to null when there is no records to export
			if(results.getTotalRecordCount().intValue() == 0) results = null;
			
			return results;
			
		}catch(Exception ex){
			logger.error("Exception occurred while Exporting Payments : ", ex);
			//BatchUtils.writeIntoBatchLog(stepExecution,"clm", "Failure when exporting payments", ex);
			throw new Exception("Disbursement export failed : " , ex);
		}
	}

	
}
