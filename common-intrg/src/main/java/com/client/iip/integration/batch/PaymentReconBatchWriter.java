package com.client.iip.integration.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.client.iip.integration.financials.disbursement.ClientDisbursementService;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportSummaryDTO;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

public class PaymentReconBatchWriter implements ItemWriter<Map<File, Object>> {
	
	/**
     * logger for this class.
     */
	private static final Logger logger = LoggerFactory.getLogger(PaymentReconBatchWriter.class);
	
	private List<File> processedFiles = new ArrayList<File>();	
	
	@Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;		
	  
	/**
	 * @param items List of items to save.
	 * @throws Exception to be thrown.
	 */
	public void write(List<? extends Map<File, Object>> items) throws Exception {
		
		logger.debug("INSIDE Writer");
		try{
			if(items != null && !items.isEmpty()){
					logger.info("There are items to be written with size: " + items.size());
	
					//Get the key item from HashMap
					for(Map<File, Object> map : items){
						logger.info("Item MapSize : " + map.size());
						//Setup Security Context
						BatchUtils.setupSecurityContext();
						logger.info("Security context setup completed");					
						Map.Entry<File, Object> mapItems = (Map.Entry<File, Object>) map.entrySet().iterator().next();
						File file = mapItems.getKey();
						List<DisbursementImportSummaryDTO> disbList = (List<DisbursementImportSummaryDTO>) mapItems.getValue();
						try{
							MuleServiceFactory.getService(ClientDisbursementService.class).importPayment(disbList);
							logger.info("File Name: " + file.getName());
							processedFiles.add(file);
						}
						catch(Exception ex){
							logger.error("Exception occurred while processing File : " + file.getName(), ex);
							BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(),"clm", 
									"Failure when processing file : " + file.getName(), ex);
							//throw new Exception("Failure when processing file : " + file.getName(), ex);
						}
	
					}
					stepExecutionListener.getJobExecutionCtx().put("processedFiles", processedFiles);
	
			}else{
				logger.info("There are no items to be written");
			}
		}catch(Exception ex){
			logger.error("PaymentReconBatchWriter Failure", ex);
			//BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm", "Resource write Failure : " + ex.toString());
			throw new Exception("PaymentReconBatchWriter Failed : ", ex);				
		}
		
	}	
	
}