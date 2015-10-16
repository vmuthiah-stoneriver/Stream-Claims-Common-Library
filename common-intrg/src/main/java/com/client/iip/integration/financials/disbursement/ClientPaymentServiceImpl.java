package com.client.iip.integration.financials.disbursement;

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentService;
import com.fiserv.isd.iip.bc.financials.disbursement.external.helper.ExportPaymentHelper;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.DisbursementImportSummaryDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.importPayment.helper.ImportPaymentHelper;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;

/**
 * ClientPaymentServiceImpl is a specialized version of PaymentServiceImpl
 * 
 * @see com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentServiceImpl
 * 
 *      PaymentServiceImpl writes disbursement requests to a set of staging
 *      tables with the intent that Stream Clients will read/write data records
 *      from the tables using SQL. The ClientPaymentServiceImpl Service is
 *      designed in line with a Service Oriented Architecture where the data is
 *      sent/received as a set of services.
 */
@Pojo(id = "client.financials.disbursement.serviceobject.PaymentService")
public class ClientPaymentServiceImpl implements PaymentService {

	private ExportPaymentHelper exportPaymentHelper;
	private ImportPaymentHelper importPaymentHelper;

	/**
	 * The method process the valid disbursement and export to the external
	 * system.
	 * 
	 * @param dtos
	 *            Input collection of DisbursementDTO to be exported.
	 * @return Collection of disbursement processed by system.
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Collection<DisbursementDTO> exportPaymentToExternalSystem(
			Collection<DisbursementDTO> dtos) {
		Collection<DisbursementDTO> disbursementToReturn = null;
		if (dtos != null && dtos.size() > 0) {
			/*
			 * Pre-process disbursement to get valid collection of disbursement
			 * to be processed.
			 */
			disbursementToReturn = exportPaymentHelper.preProcess(dtos);
			// Process disbursement.
			disbursementToReturn = exportPaymentHelper
					.process(disbursementToReturn);
			/*
			 *  TD 42773 @GR - AP Export Performance Issue - We are updating disbursement Object in post process. There are queries on the same objects down stream
			 *  after post process.. Hibernate is flushing heavy to avoid dirty state causing significant slowness.
			 *  Commenting this here and moving this out at the end after the entire processing is completed.
			 */
			// Post Process disbursement to update the disbursement status.
			//disbursementToReturn = exportPaymentHelper
			//		.postProcess(disbursementToReturn);

		}
		return disbursementToReturn;
	}
	
	/*
	 * TD 42773 @GR - New method added for post processing
	 */
	public Collection<DisbursementDTO> postProcess(Collection<DisbursementDTO> dtos){
		
			return exportPaymentHelper.postProcess(dtos);
	}

	/**
	 * Persist import data into staging tables.
	 * 
	 * @param dto
	 *            DisbursementImportSummaryDTO
	 * @return DisbursementImportSummaryDTO
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public DisbursementImportSummaryDTO saveDisbursementImportSummary(
			DisbursementImportSummaryDTO dto) {
		return getImportPaymentHelper().saveDisbursementImportSummary(dto);
	}

	/**
	 * The method will receive Disbursement status updates from an external
	 * payment processor and processes the status update based on the valid
	 * status transitions for disbursement.
	 * 
	 * @param dtos
	 *            Input collection of staging dtos.
	 * @return collection of processed DisbursementImportSummaryDTOs
	 */
	public Collection<DisbursementImportSummaryDTO> importPaymentFromExternalSystem(
			Collection<DisbursementImportSummaryDTO> dtos) {

		if (dtos == null || dtos.isEmpty()) {
			return dtos;
		}

		return getImportPaymentHelper().process(dtos);
	}

	/**
	 * Getter for exportPaymentHelper.
	 * 
	 * @return the exportPaymentHelper
	 */
	public ExportPaymentHelper getExportPaymentHelper() {
		return exportPaymentHelper;
	}

	/**
	 * Setter for exportPaymentHelper.
	 * 
	 * @param exportPaymentHelperParam
	 *            the exportPaymentHelper to set
	 */
	@Inject(PojoRef = "client.financials.disbursement.pojo.exportPaymentPojo")
	public void setExportPaymentHelper(
			ExportPaymentHelper exportPaymentHelperParam) {
		this.exportPaymentHelper = exportPaymentHelperParam;
	}

	/**
	 * Getter method for importPaymentHelper.
	 * 
	 * @return the importPaymentHelper
	 */
	public ImportPaymentHelper getImportPaymentHelper() {
		return importPaymentHelper;
	}

	/**
	 * Setter method for importPaymentHelper.
	 * 
	 * @param value
	 *            the importPaymentHelper to set
	 */
	@Inject(PojoRef = "client.financials.disbursement.pojo.importPaymentPojo")
	public void setImportPaymentHelper(ImportPaymentHelper value) {
		this.importPaymentHelper = value;
	}

}
