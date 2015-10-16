package com.client.iip.integration.financials.disbursement.helper;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.bc.financials.generalledger.GeneralLedgerExportHelper;
import com.fiserv.isd.iip.bc.financials.generalledger.batch.export.GeneralLedgerExportBatchDTO;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;

@Pojo(id = "client.financials.helper.generalledger.generalLedgerExportHelper")
public class ClientGeneralLedgerExportHelper extends GeneralLedgerExportHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientGeneralLedgerExportHelper.class);
	
	
	
	/**
	 * Create saveGLExportData.
	 * 
	 * @param gLExportDTOs
	 *            Collection
	 */
	public void saveGLExportData(Collection<GeneralLedgerExportBatchDTO> gLExportDTOs){
		//DTOUtils.saveEntities(lds, gLExportDTOs);
		
		// Update Exported GL transaction status to post.
		updateGLTransactionStatus();
	}	
	
}
