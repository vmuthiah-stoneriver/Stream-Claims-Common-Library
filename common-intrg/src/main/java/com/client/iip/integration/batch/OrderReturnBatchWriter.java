package com.client.iip.integration.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.client.iip.integration.documents.ClientDocumentDTO;
import com.client.iip.integration.documents.ClientDocumentService;
import com.client.iip.integration.orders.ClientOrderReturnDataDTO;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.search.ClaimSearchCriteriaDTO;
import com.stoneriver.iip.entconfig.base.tool.AgreementDataDTO;
import com.stoneriver.iip.entconfig.base.tool.AgreementDataValueDTO;
import com.stoneriver.iip.orders.api.OrdersDTO;
import com.stoneriver.iip.orders.api.OrdersService;

public class OrderReturnBatchWriter implements ItemWriter<Map<File, Object>> {
	
	/**
     * logger for this class.
     */
	private static final Logger logger = LoggerFactory.getLogger(OrderReturnBatchWriter.class);
	
	private List<File> processedFiles = new ArrayList<File>();	
	
	@Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;	
	
	/**
	 * @param items List of items to save.
	 * @throws Exception to be thrown.
	 */
	public void write(List<? extends Map<File, Object>> items) throws Exception {
		
		logger.debug("Inside Writer");
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
					ClientOrderReturnDataDTO orderReturnDTO = (ClientOrderReturnDataDTO) mapItems.getValue();
					try{
						//Get the Claim Id
						ClaimSearchCriteriaDTO criteria = new ClaimSearchCriteriaDTO();
						criteria.setClaimNumber(orderReturnDTO.getClaimNumber());
						OrdersDTO order = MuleServiceFactory.getService(OrdersService.class).retrieveOrder(orderReturnDTO.getOrderId());

						//Save Order
						MuleServiceFactory.getService(OrdersService.class).saveOrderReturnData(orderReturnDTO);
						//Now Import the order return documents.
						for(ClientDocumentDTO document : orderReturnDTO.getOrderDocs()){
							document.setAgreementIdDerived(order.getAgreementIdDerived());
							document.setDocumentReferenceNumber("Order-" + order.getOrderReferenceNumber());
							//Set Agreement data
							document.setAgreementData(new ArrayList<AgreementDataDTO>());
							document.getAgreementData().add(createAgreementDataValue("ord_id", orderReturnDTO.getOrderId().toString()));
							//Save the document
							MuleServiceFactory.getService(ClientDocumentService.class).createNewInboundAttachment(document);
						}
						logger.info("File Name: " + file.getName());
						if(stepExecutionListener.getJobExecutionCtx().get("processedFiles") != null 
								&& !((List<File>)stepExecutionListener.getJobExecutionCtx().get("processedFiles")).isEmpty()) {
							processedFiles = (List<File>)stepExecutionListener.getJobExecutionCtx().get("processedFiles");
						}						
						processedFiles.add(file);						
					}
					catch(Exception ex){
						logger.error("Exception occurred while processing File : " + file.getName(), ex);
						BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm",
								"Failure when processing file : " + file.getName(), ex);
					}

				}
				stepExecutionListener.getJobExecutionCtx().put("processedFiles", processedFiles);

		}else{
			logger.info("There are no items to be written");
		}
		
	}
	
	/**
	 * Create agreement data for the given code and value.
	 * @param code
	 * @param value
	 * @return AgreementDataDTO
	 */
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

}
