package com.client.iip.integration.financials.disbursement;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentClaimParticipantData;
import com.fiserv.isd.iip.bc.financials.generalledger.batch.export.GLExportBatchDTO;
import com.fiserv.isd.iip.core.data.annotation.Parameter;
import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.data.annotation.Search;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;
import com.stoneriver.iip.claims.financial.ClaimParticipantSearchCriteria;

/**
 * @author Todd Beals
 */
@DaoInterface(id = "client.daointerface.clientDisbursementDao", daoInterfaceFactory = "iip-financials-daoInterfaceFactory")
public interface ClientDisbursementDAO {

	/**
	 * Retrieves the list of unpaid disbursements to export to an external
	 * vendor for payment based on current date.
	 * 
	 * @return list unpaid disbursements for export
	 */
	@Query(accessId = "client.financials.payment.retrieveDisbursementsForExport")
	Collection<DisbursementDTO> retrieveDisbursementsForExport(
			@Parameter(name = "startDate") Date startDate,
			@Parameter(name = "endDate") Date endDate);

	/**
	 * Retrieves a list of disbursements for a given effective date range and
	 * status type code.
	 * 
	 * @return list disbursements
	 */
	@Query(accessId = "client.financials.payment.retrieveDisbursementsByDateAndStatus")
	Collection<DisbursementDTO> retrieveDisbursementsByDateAndStatus(
			@Parameter(name = "startDate") Date startDate,
			@Parameter(name = "endDate") Date endDate,
			@Parameter(name = "statusCodes") List<String> statusCodes);

	/**
	 * Re Export All Disbursement for a given effective date range and status
	 * type code.
	 * 
	 * @return list disbursements
	 */
	@Query(accessId = "client.financials.payment.retrieveDisbursementsForReExport")
	Collection<DisbursementDTO> retrieveDisbursementsForReExport(
			@Parameter(name = "startDate") Timestamp startDate,
			@Parameter(name = "endDate") Timestamp endDate);
	
	/**
	 * Retrieves the all participants associated to a claim.
	 * 
	 * @param criteria ClaimParticipantSearchCriteria
	 * 
	 * @return Collection PaymentClaimParticipantData 
	 */
	@Search(accessId="client.financials.payment.retrieveAllClaimParticipants")
	Collection<PaymentClaimParticipantData> retrieveClaimParticipantData(ClaimParticipantSearchCriteria criteria);
	
	/**
	 * Retrieves GL Export Entries
	 * @param businessDate
	 * @return
	 */
	@Query(accessId = "financials.batch.generalledger.retrieveGeneralLedgerTransactionsExportDetails")
	Collection<GLExportBatchDTO> retrieveEntriesForGLExport(@Parameter(name = "businessDate") Date businessDate);
	
}
