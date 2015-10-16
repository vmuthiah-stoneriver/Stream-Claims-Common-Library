package com.client.iip.integration.financials.disbursement;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO;

/**
 * Client specialization of DisbursementDTO to include claim payment details.
 * 
 * @author Todd Beals
 */

public class ClientExportDisbursementsResultDTO {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -3924616115976516676L;

	private Long totalRecordCount = 0L;
	private BigDecimal totalDisbursementAmount = BigDecimal.ZERO;
	private Date executionDate;
	private Date accountingDate;

	private Collection<PaymentHeaderDTO> paymentList;

	// private Collection<ClientDisbursementDTO> clientDisbursementList;

	public ClientExportDisbursementsResultDTO() {
		super();
	}

	/**
	 * Return the total number of disbursement records.
	 * 
	 * @return the totalRecordCount
	 */
	public Long getTotalRecordCount() {
		return totalRecordCount;
	}

	/**
	 * Set the total number of disbursement records.
	 * 
	 * @param totalRecordCount
	 *            the totalRecordCount to set
	 */
	public void setTotalRecordCount(Long totalRecordCount) {
		this.totalRecordCount = totalRecordCount;
	}

	/**
	 * Return the total disbursement amount for all disbursement records.
	 * 
	 * @return the totalDisbursementAmount
	 */
	public BigDecimal getTotalDisbursementAmount() {
		return totalDisbursementAmount;
	}

	/**
	 * Set the total disbursement amount for all disbursement records.
	 * 
	 * @param totalDisbursementAmount
	 *            the totalDisbursementAmount to set
	 */
	public void setTotalDisbursementAmount(BigDecimal totalDisbursementAmount) {
		this.totalDisbursementAmount = totalDisbursementAmount;
	}

	/**
	 * Get the Client Disbursement List
	 * 
	 * @return the clientDisbursementList
	 */
	/*
	 * public Collection<ClientDisbursementDTO> getClientDisbursementList() {
	 * return clientDisbursementList; }
	 */

	/**
	 * Set the Client Disbursement List
	 * 
	 * @param clientDisbursementList
	 *            the clientDisbursementList to set
	 */
	/*
	 * public void setClientDisbursementList( Collection<ClientDisbursementDTO>
	 * clientDisbursementList) { this.clientDisbursementList =
	 * clientDisbursementList; }
	 */

	/**
	 * Get the execution date
	 * 
	 * @return the executionDate
	 */
	public Date getExecutionDate() {
		return executionDate;
	}

	/**
	 * Set the execution date
	 * 
	 * @param executionDate
	 *            the executionDate to set
	 */
	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

	/**
	 * Get the accounting date
	 * 
	 * @return the accountingDate
	 */
	public Date getAccountingDate() {
		return accountingDate;
	}

	/**
	 * Set the accounting date
	 * 
	 * @param accountingDate
	 *            the accountingDate to set
	 */
	public void setAccountingDate(Date accountingDate) {
		this.accountingDate = accountingDate;
	}

	/**
	 * @return the paymentList
	 */
	public Collection<PaymentHeaderDTO> getPaymentList() {
		return paymentList;
	}

	/**
	 * @param paymentList
	 *            the paymentList to set
	 */
	public void setPaymentList(Collection<PaymentHeaderDTO> paymentList) {
		this.paymentList = paymentList;
	}

}
