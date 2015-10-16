package com.client.iip.integration.financials.disbursement;

import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportSummaryDTO;
import com.fiserv.isd.iip.bc.financials.generalledger.batch.export.GeneralLedgerExportBatchDTO;
import com.fiserv.isd.iip.core.service.ServiceEndpoint;

@ServiceEndpoint("integration.endpoint.clientDisbursementServiceInbound")
public interface ClientDisbursementService {

	
	ClientExportDisbursementsResultDTO export(ClientExportDisbursementsRequestDTO request);
	
	ClientExportDisbursementsResultDTO reExport(ClientExportDisbursementsRequestDTO request);
	
	Collection<DisbursementImportSummaryDTO> importPayment(Collection<DisbursementImportSummaryDTO> disbursementImportSummaryDTOs);
	
	Collection<GeneralLedgerExportBatchDTO> exportGL();
}
