package com.client.iip.integration.financials.disbursement;

import java.util.Date;

import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO;

/**
 * Client specialization of PaymentHeaderDTO to include missing payment header
 * details.
 * 
 * @author Todd Beals
 */

public class ClientPaymentHeaderDTO extends PaymentHeaderDTO {

	private static final long serialVersionUID = 721201240374931118L;

	private Long lobId;
	private String lobCode;
	private String lobName;
	private Date accountingBusinessDate;

	public ClientPaymentHeaderDTO() {
		super();
	}

	/**
	 * @return the lobId
	 */
	public Long getLobId() {
		return lobId;
	}

	/**
	 * @param lobId the lobId to set
	 */
	public void setLobId(Long lobId) {
		this.lobId = lobId;
	}

	/**
	 * @return the lobCode
	 */
	public String getLobCode() {
		return lobCode;
	}

	/**
	 * @param lobCode the lobCode to set
	 */
	public void setLobCode(String lobCode) {
		this.lobCode = lobCode;
	}

	/**
	 * @return the lobName
	 */
	public String getLobName() {
		return lobName;
	}

	/**
	 * @param lobName the lobName to set
	 */
	public void setLobName(String lobName) {
		this.lobName = lobName;
	}

	/**
	 * @return the accountingBusinessDate
	 */
	public Date getAccountingBusinessDate() {
		return accountingBusinessDate;
	}

	/**
	 * @param accountingBusinessDate the accountingBusinessDate to set
	 */
	public void setAccountingBusinessDate(Date accountingBusinessDate) {
		this.accountingBusinessDate = accountingBusinessDate;
	}


	
}
