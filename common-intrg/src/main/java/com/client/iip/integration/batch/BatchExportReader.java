package com.client.iip.integration.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fiserv.isd.iip.core.dto.DataTransferObject;

public class BatchExportReader implements ItemReader<DataTransferObject> {
	
	private boolean startNewBatchProcess = true;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized DataTransferObject read() throws Exception {
		// Return Dummy Object for the processor
		if(this.startNewBatchProcess){
			startNewBatchProcess = false;
			return new DataTransferObject();
		}
		return null;
	}	

}
